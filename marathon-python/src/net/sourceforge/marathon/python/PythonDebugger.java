/*******************************************************************************
 *  
 *  $Id: PythonDebugger.java 176 2008-12-22 11:04:49Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
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
package net.sourceforge.marathon.python;

import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.runtime.AbstractDebugger;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySyntaxError;

public class PythonDebugger extends AbstractDebugger implements IDebugger, IPlaybackListener {

    private static PythonDebugger debugger;
    private IPlaybackListener listener;
    private PythonScript script;
    private String lastEvent;
    private String lastFileName;
    private int lastLineNo;
    private static PyObject traceFunction;

    public PythonDebugger(PythonScript script) {
        this.script = script;
        setInstance(this);
        String pacakgeName = PythonDebugger.class.getPackage().getName();
        String className = PythonDebugger.class.getName();
        int index = className.lastIndexOf('.');
        className = className.substring(index + 1);
        script.interpreterExec("import sys");
        script.interpreterExec("from " + pacakgeName + " import " + className);
        script.interpreterExec("sys.settrace(PythonDebugger.traceFunction)");
        setTraceFunction(script.interpreterEval("PythonDebugger.traceFunction"));
        PyObject builtin = script.interpreterEval("__builtin__");
        PyObject acceptChecklist = script.interpreterEval("PythonDebugger.pyAcceptChecklist");
        builtin.__setattr__("__accept_checklist", acceptChecklist);
        PyObject showChecklist = script.interpreterEval("PythonDebugger.pyShowChecklist");
        builtin.__setattr__("__show_checklist", showChecklist);
    }

    private static void setTraceFunction(PyObject tf) {
        traceFunction = tf;
    }

    private static void setInstance(PythonDebugger d) {
        debugger = d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.marathon.python.IDebugger#run(java.lang.String)
     */
    public String run(String pscript) {
        script.interpreterExec("sys.settrace(PythonDebugger.traceFunction)");
        try {
            return script.interpreterEval(pscript).toString();
        } catch (PySyntaxError e) {
            script.interpreterExec(pscript);
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.python.IDebugger#setListener(net.sourceforge
     * .marathon.api.PlaybackListener)
     */
    public void setListener(IPlaybackListener listener) {
        this.listener = listener;
    }

    public int lineReached(SourceLine line) {
        if (listener.lineReached(line) == PAUSE)
            pause();
        return CONTINUE;
    }

    public int methodReturned(SourceLine line) {
        if (listener.methodReturned(line) == PAUSE)
            pause();
        return CONTINUE;
    }

    public void playbackFinished(PlaybackResult result, boolean shutdown) {
        listener.playbackFinished(result, false);
    }

    public static PyObject traceFunction(PyObject frame, PyObject event, PyObject arg) {
        if (debugger != null && debugger.listener != null) {
            PyObject code = frame.__getattr__("f_code");
            String methodName = code.__getattr__("co_name").toString();
            String fileName = code.__getattr__("co_filename").toString();
            int lineNo = ((PyInteger) frame.__getattr__("f_lineno")).getValue();

            debugger.trace(event.toString(), fileName, methodName, lineNo);
        }
        return traceFunction;
    }

    public static void pyAcceptChecklist(PyString fname) {
        debugger.acceptChecklist(fname.toString());
    }

    public static void pyShowChecklist(PyString fname) {
        debugger.showChecklist(fname.toString());
    }

    private void trace(String event, String fileName, String methodName, int lineNo) {
        if (isRepeat(event, fileName, lineNo) || shouldIgnore(fileName))
            return;

        SourceLine line = new SourceLine(fileName, methodName, lineNo);
        if (event.equals("line"))
            lineReached(line);
        else if (event.equals("return"))
            methodReturned(line);
        else if (event.equals("call"))
            methodCalled(line);
    }

    private boolean shouldIgnore(String fileName) {
        return fileName.equals("<string>");
    }

    private boolean isRepeat(String event, String fileName, int lineNo) {
        if (event.equals(lastEvent) && fileName.equals(lastFileName) && lineNo == lastLineNo)
            return true;
        lastEvent = event;
        lastFileName = fileName;
        lastLineNo = lineNo;
        return false;
    }

    public int methodCalled(SourceLine line) {
        if (listener.methodCalled(line) == PAUSE)
            pause();
        return CONTINUE;
    }

    public int acceptChecklist(String fileName) {
        if (listener.acceptChecklist(fileName) == PAUSE)
            pause();
        return 0;
    }

    public void setAcceptChecklist(boolean b) {
    }

    public int showChecklist(String fileName) {
        if (listener.showChecklist(fileName) == PAUSE)
            pause();
        return 0;
    }
}
