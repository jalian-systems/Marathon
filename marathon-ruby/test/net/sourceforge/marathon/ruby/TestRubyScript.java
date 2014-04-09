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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.Failure;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestRubyScript {

    private StringWriter out;
    private StringWriter err;
    private PlaybackResult result;

    private static final String[] SCRIPT_CONTENTS_ERROR_FROM_RUBY = { "print 'I am here'", "def my_function",
            "  raise NameError, 'Name error thrown'", "end" };

    private static final String[] SCRIPT_CONTENTS_ERROR_FROM_JAVA = { "print 'I am here'", "def my_function",
            "  include_class '" + TestRubyScript.class.getCanonicalName() + "'", "  TestRubyScript.throwError", "end" };

    public static void throwError() throws Exception {
        throw new Exception("Error from java");
    }

    @Before public void setUp() throws Exception {
        out = new StringWriter();
        err = new StringWriter();
        result = new PlaybackResult();
        createDir("./testDir");
        System.setProperty(Constants.PROP_PROJECT_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_FIXTURE_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_TEST_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_MODULE_DIRS, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, RubyScriptModel.class.getName());
        System.setProperty(Constants.PROP_PROJECT_NAME, "test_project");
        System.setProperty(Constants.PROP_HOME, "marathon-home");
    }

    private static File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @After public void tearDown() throws Exception {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_DIR);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_TEST_DIR);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        properties.remove(Constants.PROP_PROJECT_NAME);
        properties.remove(Constants.PROP_HOME);
        System.setProperties(properties);
        deleteRecursive(new File("./testDir"));
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteRecursive(list[i]);
            }
        }
        file.delete();
    }

    @Test public void testResultsLoaded() throws Throwable {
        try {
            RubyScript script = new RubyScript(out, err, converToCode(SCRIPT_CONTENTS_ERROR_FROM_RUBY), new File(System.getProperty(Constants.PROP_PROJECT_DIR), "dummyfile.rb").getAbsolutePath(),
                    new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                            ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), false,
                    WindowMonitor.getInstance(), MarathonAppType.JAVA);
            Ruby interpreter = script.getInterpreter();
            assertTrue("Collector not defined", interpreter.isClassDefined("Collector"));
            RubyClass collectorClass = interpreter.getClass("Collector");
            IRubyObject presult = JavaEmbedUtils.javaToRuby(interpreter, result);
            IRubyObject collector = collectorClass.newInstance(interpreter.getCurrentContext(), new IRubyObject[0], null);
            IRubyObject rubyObject = interpreter.evalScriptlet("proc { my_function }");
            try {
                collector.callMethod(interpreter.getCurrentContext(), "callprotected", new IRubyObject[] { rubyObject, presult });
            } catch (Throwable t) {

            }
            System.err.println(out);
            assertEquals(1, result.failureCount());
            Failure[] failures = result.failures();
            assertEquals(new File(System.getProperty(Constants.PROP_PROJECT_DIR), "dummyfile.rb").getAbsolutePath(), failures[0].getTraceback()[0].fileName);
            assertEquals("my_function", failures[0].getTraceback()[0].functionName);
        } catch (Throwable t) {
            System.err.println("TestRubyScript.testResultsCapturesJavaError(): " + out.toString());
            System.err.println("TestRubyScript.testResultsCapturesJavaError(): " + out.toString());
            throw t;
        }
    }

    @Test @Ignore public void testResultsCapturesJavaError() throws Exception {
        RubyScript script = new RubyScript(out, err, converToCode(SCRIPT_CONTENTS_ERROR_FROM_JAVA), new File(System.getProperty(Constants.PROP_PROJECT_DIR), "dummyfile.rb").getAbsolutePath(),
                new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                        ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), false,
                WindowMonitor.getInstance(), MarathonAppType.JAVA);
        Ruby interpreter = script.getInterpreter();
        assertTrue("Collector not defined", interpreter.isClassDefined("Collector"));
        RubyClass collectorClass = interpreter.getClass("Collector");
        IRubyObject presult = JavaEmbedUtils.javaToRuby(interpreter, result);
        IRubyObject collector = collectorClass.newInstance(interpreter.getCurrentContext(), new IRubyObject[0], null);
        IRubyObject rubyObject = interpreter.evalScriptlet("proc { my_function }");
        try {
            collector.callMethod(interpreter.getCurrentContext(), "callprotected", new IRubyObject[] { rubyObject, presult });
        } catch (Throwable t) {

        }
        assertEquals(1, result.failureCount());
        Failure[] failures = result.failures();
        assertTrue(failures[0].getTraceback()[0].fileName.endsWith("TestRubyScript.java"));
        assertEquals("throwError", failures[0].getTraceback()[0].functionName);
    }

    private String converToCode(String[] scriptContents) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < scriptContents.length; i++) {
            sb.append(scriptContents[i]).append("\n");
        }
        return sb.toString();
    }

}
