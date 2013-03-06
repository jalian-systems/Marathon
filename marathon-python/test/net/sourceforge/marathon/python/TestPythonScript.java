/*******************************************************************************
 *  
 *  $Id: TestPythonScript.java 245 2009-01-06 13:25:53Z kd $
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.action.AbstractMarathonAction;
import net.sourceforge.marathon.action.TestException;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.Failure;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptException;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPythonScript implements IPlaybackListener {
    private static final Integer ONE = Integer.valueOf(1);
    private static final String DEFAULT_SCRIPT_NAME = "nestedfailure.py";
    public static TestPythonScript current;
    public static Object placeHolder;
    private static final String MOCK_PYTHON_SCRIPT = "from net.sourceforge.marathon.python import CurrentFixture\n"
            + "fixture = CurrentFixture()\n" + "def test():\n" + "	fixture.body.run()\n" + "	print 'printed from the interpretter'";
    private int setups;
    private int teardowns;
    public static Runnable body;
    private StringWriter interpreterOutput;
    private StringWriter interpreterError;
    private PlaybackResult result;
    private boolean setupErr;
    private IPlayer player = null;
    private String lastMethodname;
    private int lastLineno;

    @BeforeClass public static void setUpBeforeClass() throws Exception {
        createDir("./testDir");
        System.setProperty(Constants.PROP_PROJECT_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_FIXTURE_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_TEST_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_MODULE_DIRS, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.python.PythonScriptModel");
    }

    private static File createDir(String name) {
        File file = new File(name);
        if (file.exists())
            deleteRecursive(file);
        file.mkdir();
        return file;
    }

    @AfterClass public static void tearDownAfterClass() throws Exception {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_DIR);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_TEST_DIR);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
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

    @Before public void setUp() throws Exception {
        current = this;
        setups = 0;
        teardowns = 0;
        body = new Runnable() {
            public void run() {
            }
        };
        interpreterOutput = new StringWriter();
        interpreterError = new StringWriter();
    }

    public void setup() throws Exception {
        setups++;
        if (setupErr) {
            throw new Error("This is for testing - ignore this exception");
        }
    }

    @After public void tearDown() throws Exception {
        deleteDir("mycapture");
        new File("temp.py").delete();
        new File("temp$py.class").delete();
        if (player != null)
            player.halt();
    }

    public void teardown() throws Exception {
        teardowns++;
        deleteDir("mycapture");
        new File("temp.py").delete();
        new File("temp$py.class").delete();
    }

    private void deleteDir(String name) {
        deleteDir(new File(name));
    }

    private void deleteDir(File directory) {
        File[] list = directory.listFiles();
        if (list == null)
            return;
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile())
                list[i].delete();
            else
                deleteDir(list[i]);
        }
        directory.delete();
    }

    @Test public void testPythonSyntaxErrorDuringScriptSetup() throws Exception {
        try {
            createScript("BOGUS SYNTAX DUDE')", "invalid.py");
            fail("should have thrown a marathon exception when loading bogus script");
        } catch (ScriptException e) {
            assertTrue("error message", e.getMessage().startsWith("Syntax"));
        }
    }

    @Test public void testPythonSyntaxvalidButNamingErrorOccuredDuringScriptSetup() throws Exception {
        try {
            createScript("invalidvariable.blowup()", "bogus.py");
        } catch (ScriptException e) {
            String message = e.getMessage();
            assertTrue("error message", message.startsWith("NameError"));
            assertTrue("no file info", message.lastIndexOf("File \"bogus.py\", line 1") > 0);
        }
    }

    /**
     * In the version of python we are using (currently 2.1), their is no
     * keyword for the boolean values true and false. We therefore want to make
     * sure that we set them inside all places that use the marathon runtime
     */
    @Test public void testTrueAndFalseAreDefinedInMarathonRuntime() throws Exception {
        String[] codeTest = { "def test():", "    if true != 1:", "        fail('true was not equal to 1')", "    if false != 0:",
                "        fail('false was not equal to 0')" };
        createScriptAndExecute(TrivialFixture.convertCode(TrivialFixture.codeFixture, codeTest));
        assertEquals("true and false constants were not configured in the core modules", 0, result.failureCount());
    }

    /**
     * Jython implements some modules as python, and others as java. we want to
     * make sure that the python implemented modules are available. try
     * importing some common modules that should be indicative
     * 
     * @throws Exception
     */
    @Test public void testStandardPythonModulesAreAvailable() throws Exception {
        String[] codeTest = { "def test():", "    import string", "    import re", "    from bdb import Bdb", "    import copy" };
        createScriptAndExecute(TrivialFixture.convertCode(TrivialFixture.codeFixture, codeTest));
        if (result.hasFailure()) {
            Failure failure = result.failures()[0];
            fail("script contained at least one failure <" + failure + ">");
        }
    }

    @Test public void testNoTestFunctionDefined() throws Exception {
        try {
            createScriptAndExecute("");// , "notestfunction.py");
            fail("setting script should fail when there is no test function defined");
        } catch (ScriptException e) {
            assertEquals("there is no function test() defined in " + DEFAULT_SCRIPT_NAME, e.getMessage());
        }
    }

    @Test public void testSetupAndTeardownDuringNormalExecution() throws Exception {
        createScriptAndExecute(MOCK_PYTHON_SCRIPT);
        checkSetupCalled();
        checkTeardownCalled();
    }

    @Test public void testTeardownCalledWhenErrorInSetup() throws Exception {
        setupErr = true;
        createScriptAndExecute(MOCK_PYTHON_SCRIPT);
        checkSetupCalled();
        checkTeardownCalled();
    }

    @Test public void testTeardownCalledWhenErrorInMainScript() throws Exception {
        body = new Runnable() {
            public void run() {
                throw new Error("This is for testing - ignore this exception");
            }
        };
        createScriptAndExecute(MOCK_PYTHON_SCRIPT);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        checkSetupCalled();
        checkTeardownCalled();
    }

    @Test public void testCatchesInterpreterOutput() throws Exception {
        createScriptAndExecute(MOCK_PYTHON_SCRIPT);
        if (!interpreterOutput.toString().trim().endsWith("printed from the interpretter"))
            assertEquals("interpretter output", "printed from the interpretter", interpreterOutput.toString().trim());
    }

    @Test public void testAttachPlaybackListenerAndExecute() throws Exception {
        createScriptAndExecute(TrivialFixture.convertCode(TrivialFixture.codeFixture, TrivialFixture.codeDummyTest));
        assertEquals("callprotected", lastMethodname);
        assertEquals(15, lastLineno);
    }

    /**
     * For some reason, Jython keeps a static definition of modules so that
     * unless they are explicitly told to be redefined, any changes in module
     * definitions will not be reflected until the virtual machine is restarted
     * -- even though the file containing the module definition has changed.
     * This is unhelpful especially when you are using marathon to develop
     * external testing libraries which are subject to change. Having to start,
     * stop and then restart again is a major pain. So here we ensure that
     * modules are reloaded with each script invocation.
     * 
     * We do this by modifying a static variable in the the reloadme module, and
     * then making sure that it has its initial value reset the second time we
     * execute the script
     * 
     * I would prefer that we not have to rely on an external file, as I feel
     * that it makes the testcase less explicit. But I can't find a quick way to
     * make jython search for module definitions in memory. Anyone who wants to
     * fix this, feel free. In the mean time, checkout reloadme.py in src/libpy
     * 
     */
    @Test public void testPythonModulesAreReloadedInBetweenScriptInvocations() throws Exception {
        String content = "from net.sourceforge.marathon.python import CurrentFixture\n" + "fixture = CurrentFixture()\n"
                + "import reloadme\n" + "def test():\n" + "	reloadme.incr()\n" + "	fixture.setPlaceHolder(reloadme.staticint)";
        createScriptAndExecute(content);
        assertEquals("static integer", ONE, placeHolder);
        // execute again, and static integer should have been reset
        createScriptAndExecute(content);
        assertEquals("static integer", ONE, placeHolder);
    }

    /**
     * check the python stacktrace is propagated to the playback result when a
     * user calls fail(). In otherwords this is not an error, rather some sore
     * of assertion failure. and furthermore, that it has the marathon core
     * files filtered out
     */
    @Test public void testAddFailureInsidePython() throws Exception {
        String[] codeTest = { "def first():", "    second()", "", "def second():", "    third()", "", "def third():",
                "    fail('this is a failure')", "", "def test():", "    first()" };
        createScriptAndExecute(TrivialFixture.convertCode(TrivialFixture.codeFixture, codeTest));
        Failure[] failures = result.failures();
        assertNotNull("failures", failures);
        assertEquals(1, result.failureCount());
        Failure failure = failures[0];
        assertEquals("this is a failure", failure.getMessage());
        SourceLine[] traceback = failure.getTraceback();
        assertNotNull("traceback", traceback);
        assertEquals("traceback size", 10, traceback.length);
        assertSourceLine(traceback, 3, "third", 21);
        assertSourceLine(traceback, 4, "second", 18);
        assertSourceLine(traceback, 5, "first", 15);
        assertSourceLine(traceback, 6, "test", 24);
    }

    public static class ThrowError extends AbstractMarathonAction {
        private static final long serialVersionUID = 1L;
        private String message;

        public ThrowError(String message) {
            super(new ComponentId("ThrowError"), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
            this.message = message;
        }

        public void play(ComponentFinder resolver) {
            throw new TestException(message, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance(), true);
        }

        public String toScriptCode() {
            return null;
        }

    }

    /**
     * The mechanism for raising failures (as opposed to errors) inside java
     * code, is to throw a TestException. we need to make sure of two things: a)
     * that this exception is translated into a failure, and b) that the stack
     * trace is reasonable to indicate where this failure occured. the java
     * stack trace is not included because the it is more like a "native method"
     * and so the failure inside the java code was planned, and part of its
     * normal function
     */
    @Test public void testAddFailureInsideJava() throws Exception {
        String[] codeTest = { "def faillikeamofo(message):",
                "    marathon.play(net.sourceforge.marathon.python.TestPythonScript.ThrowError(message))", "", "def test():",
                "    faillikeamofotwice()", "", "def faillikeamofotwice():", "    faillikeamofo('failure 1')",
                "    faillikeamofo('failure 2')", "" };

        createScriptAndExecute(TrivialFixture.convertCode(TrivialFixture.codeFixture, codeTest));
        assertEquals("failure count", 2, result.failureCount());
        SourceLine[] one = result.failures()[0].getTraceback();
        SourceLine[] two = result.failures()[1].getTraceback();
        assertNotNull("traceback 1", one);
        assertNotNull("traceback 2", two);
        assertEquals("traceback 1 length", 8, one.length);
        assertSourceLine(one, 2, "faillikeamofo", 15);
        assertSourceLine(one, 3, "faillikeamofotwice", 21);
        assertSourceLine(one, 4, "test", 18);
        assertEquals("traceback 2 length", 8, two.length);
        assertSourceLine(two, 2, "faillikeamofo", 15);
        assertSourceLine(two, 3, "faillikeamofotwice", 22);
        assertSourceLine(two, 4, "test", 18);
    }

    /**
     * this is the case where an error is raised inside python, which not
     * caught, except for the top level of the script. We want to verify that
     * the message is saved intact, and that we get a valid stacktrace including
     * and up to the line where the error occured
     */
    @Test public void testUncaughtPythonExceptionInsideScript() throws Exception {
        String[] codeTest = { "def callthrowstuff():", "    throwstuff()", "", "def test():", "    callthrowstuff()", "",
                "def throwstuff():", "    raise NameError, 'dingleberry'" };
        createScriptAndExecute(TrivialFixture.convertCode(TrivialFixture.codeFixture, codeTest));
        assertEquals("failure count", 1, result.failureCount());
        Failure failure = result.failures()[0];
        assertEquals("failure message", "<type 'exceptions.NameError'>: dingleberry", failure.getMessage());
        SourceLine[] traceback = failure.getTraceback();
        assertNotNull("traceback", traceback);
        assertEquals("traceback length", 6, traceback.length);
        assertSourceLine(traceback, 0, "throwstuff", 21);
        assertSourceLine(traceback, 1, "callthrowstuff", 15);
        assertSourceLine(traceback, 2, "test", 18);
    }

    /**
     * when an error, not a failure, occurs inside java code, we want the
     * ability to capture, and record this fact in the output, as well as get a
     * reasonable stack trace, which includes both the python source elements as
     * well as the java source elements
     */
    @Test public void testUncaughtThrowableInsideJava() throws Exception {
    }

    private synchronized void createScriptAndExecute(String content) throws Exception {
        IScript script = createScript(content, DEFAULT_SCRIPT_NAME);
        result = new PlaybackResult();
        player = script.getPlayer(this, result);
        player.play(true);
        wait();
    }

    public synchronized void playbackFinished(PlaybackResult result, boolean shutdown) {
        notify();
    }

    public int lineReached(SourceLine line) {
        lastMethodname = line.functionName;
        lastLineno = line.lineNumber;
        return CONTINUE;
    }

    private void assertSourceLine(SourceLine[] traceback, int elementNumber, String methodName, int lineNumber) {
        SourceLine line = traceback[elementNumber];
        assertEquals("traceback element filename", TestPythonScript.DEFAULT_SCRIPT_NAME, line.fileName);
        assertEquals("traceback element " + elementNumber + " linenumber", lineNumber, line.lineNumber);
        assertEquals("traceback element " + elementNumber + " function name", methodName, line.functionName);
    }

    private IScript createScript(String content, String fileName) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        IScript script = new PythonScript(interpreterOutput, interpreterError, content, fileName, new ComponentFinder(
                Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance(),
                MarathonAppType.JAVA);
        return script;
    }

    private void checkTeardownCalled() {
        assertEquals("times torn down", 1, teardowns);
    }

    private void checkSetupCalled() {
        assertEquals("times setup up called", 1, setups);
    }

    public int methodReturned(SourceLine line) {
        return CONTINUE;
    }

    public int methodCalled(SourceLine line) {
        return CONTINUE;
    }

    public int acceptChecklist(String fileName) {
        return 0;
    }

    public int showChecklist(String filename) {
        return 0;
    }
}
