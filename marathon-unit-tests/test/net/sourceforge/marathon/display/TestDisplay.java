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
package net.sourceforge.marathon.display;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Constants.MarathonMode;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.display.Display.IDisplayProperties;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.junit.StdOutConsole;
import net.sourceforge.marathon.providers.DisplayEventQueueProvider;
import net.sourceforge.marathon.providers.PlaybackResultProvider;
import net.sourceforge.marathon.providers.RecorderProvider;
import net.sourceforge.marathon.runtime.RuntimeStub;
import net.sourceforge.marathon.util.FileHandler;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.Snooze;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import atunit.AtUnit;
import atunit.Container;
import atunit.Container.Option;
import atunit.Mock;
import atunit.MockFramework;
import atunit.Unit;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;

@RunWith(AtUnit.class) @Container(Option.GUICE) @MockFramework(atunit.MockFramework.Option.EASYMOCK) public class TestDisplay
        implements Module {
    private @Unit @Inject Display display;
    private MockDisplayView view;
    private MockFileHandler fileHandler;
    private @Mock @Inject IRuntimeFactory runtimeFactoryStub;
    private RuntimeStub runtimeStub = new RuntimeStub();
    private IEditor editor;

    public TestDisplay() {
    }

    @Before public void setUp() throws Exception {
        System.setProperty(Constants.PROP_PROJECT_DIR, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_FIXTURE_DIR, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_TEST_DIR, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_MODULE_DIRS, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_HOME, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
        view = new MockDisplayView();
        display.setView(view);
        fileHandler = new MockFileHandler();
        display.setDefaultFixture("default");
        view.newFile();
        editor = view.getEditor();
        editor.setData("filehandler", fileHandler);
        expect(runtimeFactoryStub.createRuntime((MarathonMode)anyObject(), (String)anyObject(), (IConsole)anyObject())).andReturn(runtimeStub);
        replay(runtimeFactoryStub);
    }

    @After public void tearDown() throws Exception {
        try {
            display.destroy();
        } finally {
        }
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_DIR);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_TEST_DIR);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_HOME);
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
        reset(runtimeFactoryStub);
    }

    @Test public void testSuccessfulPlay() throws Exception {
        editor.setText("#{{{ Marathon\ndef test():\nthis is the tape");
        editor.setData("filename", "dummy");
        display.play(new StdOutConsole());
        AWTSync.sync();
        verify(runtimeFactoryStub);
        assertEquals("#{{{ Marathon\ndef test():\nthis is the tape\n" + Indent.getIndent() + "pass\n",
                runtimeStub.lastContent);
        assertEquals("dummy", runtimeStub.lastFilename);
        assertState(State.STOPPED_WITH_APP_CLOSED);
    }

    @Test public void testPlayWithException() throws Exception {
        runtimeStub.scriptsFail = true ;
        editor.setText("#{{{ Marathon\ndef test():\n");
        editor.setData("filename", "dummy");
        synchronized (this) {
            display.play(new StdOutConsole());
        }
        assertState(State.STOPPED_WITH_APP_OPEN);
    }

    @Test public void testRecord() {
        editor.setText("");
        editor.setData("filename", "dummy");
        display.record(new StdOutConsole());
        assertState(State.RECORDING);
        assertTrue(runtimeStub.isRecording);
        assertEquals("", editor.getText());
        editor.insertScript("foobar");
        assertEquals("foobar", editor.getText());
        display.stop();
        assertTrue(!runtimeStub.isRecording);
    }

    @Test public void testPauseResume() {
        editor.setText("");
        editor.setData("filename", "dummy");
        display.record(new StdOutConsole());
        assertState(State.RECORDING);
        assertTrue(runtimeStub.isRecording);
        assertEquals("", editor.getText());
        editor.insertScript("foobar");
        assertEquals("foobar", editor.getText());
        display.pauseRecording();
        assertState(State.RECORDINGPAUSED);
        display.resume();
        assertState(State.RECORDING);
        display.stop();
        assertTrue(!runtimeStub.isRecording);
    }

    @Test public void testCannotStopUnlessPlaying() throws Exception {
        try {
            display.stop();
            fail("cannot interrupt playback while not playing");
        } catch (IllegalStateException e) {
        } // it's all good
    }

    @Test public void testSave() {
        view.newFile();
        assertTrue(((MockDisplayView) view).title.matches("Marathon - " + "Untitled" + "[0-9]*"));
        editor.setText("this is it");
        editor.setData("filename", "dummy");
        fileHandler._file = new File("TestFile.xml");
        view.save();
        assertEquals("this is it", fileHandler._script);
        assertEquals("Marathon - TestFile.xml", ((MockDisplayView) view).title);
    }

    @Test public void testSaveAs() {
        editor.setText("this is it");
        editor.setData("filename", "dummy");
        fileHandler._file = new File("TestFile.xml");
        view.saveAs();
        assertEquals("this is it", fileHandler._script);
        assertEquals("Marathon - TestFile.xml", ((MockDisplayView) view).title);
    }

    @Test public void testScriptMunging() throws Exception {
        editor.setText("#{{{ Marathon\ndef test():\n\n\n");
        editor.setData("filename", "dummy");
        display.play(new StdOutConsole());
        assertEquals("#{{{ Marathon\ndef test():\n\n\n\n" + Indent.getIndent() + "pass\n",
                runtimeStub.lastContent);
    }

    @Test public void testNew() {
        String testHeader = "#{{{ Marathon\nfrom default import *\n#}}}\n\ndef test():\n";
        assertEquals(testHeader, editor.getText());
        editor.setCaretPosition(54);
        display.record(new StdOutConsole());
        assertState(State.RECORDING);
        editor.insertScript("bingo");
        display.stop();
        assertState(State.STOPPED_WITH_APP_CLOSED);
        assertEquals(testHeader + "bingo", editor.getText());
        fileHandler._file = new File("TestFile.xml");
        view.save();
        assertEquals("Marathon - TestFile.xml", ((MockDisplayView) view).title);
        view.newFile();
        assertEquals(null, fileHandler._file);
        assertTrue(((MockDisplayView) view).title.matches("Marathon - " + "Untitled" + "[0-9]*"));
        assertEquals(testHeader, editor.getText());
    }

    /**
     * should ignore messages for other files - for now
     */
    @Test public void testAboutToExecute() {
        String fileName = new File("hippo.xml").getAbsolutePath();
        fileHandler._file = new File(fileName);
        aboutToExecute("foo", "foo", 5);
        assertEquals(0, editor.getCaretLine());
        aboutToExecute("foo", fileName, 5);
        assertEquals(5, editor.getCaretLine() + 1);
        aboutToExecute("foo", "foo", 6);
        assertEquals(5, editor.getCaretLine() + 1);
        aboutToExecute("foo", fileName, 1);
        assertEquals(0, editor.getCaretLine());
    }

    private void aboutToExecute(String methodname, String filename, int lineno) {
        display.lineReached(new SourceLine(filename, methodname, lineno));
    }

    @Test public void testTracingAfterPlay() {
        editor.setText("#{{{ Marathon\nfrom xx import *\n#}}}\ndef test():\n");
        editor.setData("filename", "dummy");
        display.play(new StdOutConsole());
        aboutToExecute("foo", "dummy", 5);
        assertEquals(5, editor.getCaretLine() + 1);
        display.showResult(new PlaybackResult());
        assertEquals(1, editor.getCaretLine() + 1);
    }

    @Test public void testOutputIsUpdated() throws IOException, InterruptedException, InvocationTargetException {
        display.openApplication(view.getConsole());
        agentOutput("this is a hiccup");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        assertEquals("this is a hiccup", view.getOutputPane().getText());
    }

    public void xtestOutputIsResetCorrectly() throws IOException, InterruptedException, InvocationTargetException {
        editor.setText("#{{{ Marathon\ndef test():\n");
        editor.setData("filename", "dummy");
        display.play(new StdOutConsole());
        agentOutput("fire 1");
        agentOutput("fire 2");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        display.showResult(new PlaybackResult());
        assertEquals("fire 1fire 2", view.getOutputPane().getText());
        display.play(new StdOutConsole());
        assertEquals("", view.getOutputPane().getText());
        agentOutput("fire 3");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        assertEquals("fire 3", view.getOutputPane().getText());
    }

    @Test public void testDefaultImport() {
        assertEquals("#{{{ Marathon\nfrom default import *\n#}}}\n\ndef test():\n", editor.getText());
        view.newFile();
        assertEquals("#{{{ Marathon\nfrom default import *\n#}}}\n\ndef test():\n", editor.getText());
    }

    @Test public void testExec() {
        editor.setText("");
        editor.setData("filename", "dummy");
        display.record(new StdOutConsole());
        assertState(State.RECORDING);
        assertTrue(runtimeStub.isRecording);
        assertEquals("", editor.getText());
        display.insertScript("foobar");
        AWTSync.sync();
        assertEquals(Indent.getDefaultIndent() + "foobar\n", editor.getText());
        display.stop();
        assertTrue(!runtimeStub.isRecording);
    }

    @Test public void testExecWithPackage() {
        editor.setText("");
        editor.setData("filename", "dummy");
        display.record(new StdOutConsole());
        assertState(State.RECORDING);
        assertTrue(runtimeStub.isRecording);
        assertEquals("", editor.getText());
        display.insertScript("foo.bar");
        AWTSync.sync();
        // No more inserting in the script - this goes to header
        // assertEquals(Indent.getDefaultIndent() + "from foo import bar\n" +
        // Indent.getDefaultIndent() + "bar\n", editor.getText());
        assertEquals(Indent.getDefaultIndent() + "bar\n", editor.getText());
        display.stop();
        assertTrue(!runtimeStub.isRecording);
    }

    @Test public void testExecWithDotInArguments() {
        editor.setText("");
        editor.setData("filename", "dummy");
        display.record(new StdOutConsole());
        assertState(State.RECORDING);
        assertTrue(runtimeStub.isRecording);
        assertEquals("", editor.getText());
        display.insertScript("foo.bar('a.txt')");
        AWTSync.sync();

        // No more inserting in the script - this goes to header
        // assertEquals(Indent.getDefaultIndent() + "from foo import bar\n" +
        // Indent.getDefaultIndent() + "bar('a.txt')\n",
        // editor.getText());
        assertEquals(Indent.getDefaultIndent() + "bar('a.txt')\n",
                editor.getText());
        display.stop();
        assertTrue(!runtimeStub.isRecording);
    }

    private void agentOutput(String string) {
        view.getConsole().writeScriptOut(string.toCharArray(), 0, string.length());
    }

    private static class MockFileHandler extends FileHandler {
        private MockFileHandler() throws IOException {
            super(new MarathonFileFilter(".py", ScriptModelClientPart.getModel()), new File(
                    System.getProperty(Constants.PROP_TEST_DIR)), new File(System.getProperty(Constants.PROP_FIXTURE_DIR)),
                    Constants.getMarathonDirectories(Constants.PROP_MODULE_DIRS), null);
        }

        private File _file;
        private String _script;

        public File getCurrentFile() {
            return _file;
        }

        public File save(String script, Component parent, String fileName) {
            return saveAs(script, parent, fileName);
        }

        public File saveAs(String script, Component parent, String fileName) {
            _script = script;
            return _file;
        }

        public void clearCurrentFile() {
            _file = null;
        }
    }

    private void assertState(State state) {
        if (state != display.getState()) {
            new Snooze(10);
            assertEquals("Display state", state, display.getState());
        }
    }

    public void configure(Binder binder) {
        binder.bind(Properties.class).annotatedWith(IDisplayProperties.class).toInstance(System.getProperties());
        binder.bind(RecorderProvider.class).toInstance(new RecorderProvider());
        binder.bind(PlaybackResultProvider.class).toInstance(new PlaybackResultProvider());
        binder.bind(DisplayEventQueueProvider.class).toInstance(new DisplayEventQueueProvider());
    }
}
