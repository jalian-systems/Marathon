/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.ruby;

import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.runtime.AbstractDebugger;

import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyDebugger extends AbstractDebugger implements IDebugger {

    private final Ruby interpreter;
    private IPlaybackListener listener;
    private String lastEvent;
    private String lastFile;
    private int lastLine;
    private static final String RUBYJAVASUPPORT = System.getProperty("jruby.home") + "/lib/ruby/site_ruby/1.8/builtin";

    public RubyDebugger(Ruby pinterpreter) {
        this.interpreter = pinterpreter;
        addEventHook();
    }

    public String run(String script) {
        try {
            script = asciize(script);
            IRubyObject evalScriptlet = interpreter.evalScriptlet(script, interpreter.getCurrentContext().getCurrentScope());
            if (evalScriptlet instanceof RubyString)
                return RubyScriptModel.inspect(evalScriptlet.toString());
            else
                return evalScriptlet.inspect().toString();
        } catch (Throwable t) {
            System.err.println("Script:");
            System.err.println(script);
            t.printStackTrace();
        }
        return "";
    }

    private String asciize(String script) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = script.getBytes();
        for (byte b : bytes) {
            if (b < 0) {
                sb.append("\\").append(Integer.toOctalString(b & 0xff));
            } else
                sb.append((char) b);
        }
        return sb.toString();
    }

    public void setListener(IPlaybackListener listener) {
        this.listener = listener;
    }

    public void event(String event, String file, Number line, String name) {
        if (listener != null && !shouldIgnore(file) && !repeat(event, file, line.intValue())) {
            if (event.equals("line"))
                if (listener.lineReached(new SourceLine(file, name, line.intValue())) == IPlaybackListener.PAUSE)
                    pause();
            if (event.equals("return"))
                if (listener.methodReturned(new SourceLine(file, name, line.intValue())) == IPlaybackListener.PAUSE)
                    pause();
            if (event.equals("call"))
                if (listener.methodCalled(new SourceLine(file, name, line.intValue())) == IPlaybackListener.PAUSE)
                    pause();
        }
    }

    private boolean shouldIgnore(String file) {
        return file.equals("<string>") || file.startsWith(RUBYJAVASUPPORT);
    }

    private boolean repeat(String event, String file, Integer line) {
        if (event.equals(lastEvent) && file.equals(lastFile) && line.intValue() == lastLine)
            return true;
        lastEvent = event;
        lastFile = file;
        lastLine = line.intValue();
        return false;
    }

    public void acceptChecklist(String filename) {
        if (listener.acceptChecklist(filename) == IPlaybackListener.PAUSE)
            pause();
    }

    public void showChecklist(String filename) {
        if (listener.showChecklist(filename) == IPlaybackListener.PAUSE)
            pause();
    }

    private void addEventHook() {
        String script = "set_trace_func proc { |event, file, line, id, binding, classname| "
                + "$marathon_trace_func.event(event, file, java.lang.Integer.new(line), classname.to_s) }";
        interpreter.defineReadonlyVariable("$marathon_trace_func", JavaEmbedUtils.javaToRuby(interpreter, this));
        interpreter.evalScriptlet(script);
    }
}
