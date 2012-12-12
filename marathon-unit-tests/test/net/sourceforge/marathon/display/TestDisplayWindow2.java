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
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.IEditorProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import atunit.AtUnit;
import atunit.Container;
import atunit.Container.Option;
import atunit.Mock;
import atunit.MockFramework;
import atunit.Unit;

import com.google.inject.Inject;

import edu.stanford.ejalbert.BrowserLauncher;

@RunWith(AtUnit.class)
@Container(Option.GUICE)
@MockFramework(atunit.MockFramework.Option.EASYMOCK)
@Ignore
public class TestDisplayWindow2 {

    @Mock
    BrowserLauncher browserLauncher;
    @Mock
    Display display;
    @Mock
    IScriptModelClientPart scriptModel;
    @Mock
    FixtureSelector fixtureSelector;
    @Mock
    IRuntimeFactory runtimeFactory;
    @Mock
    TextAreaOutput outputPane;
    @Mock
    ResultPane resultPane;
    @Mock
    CallStack callStack;
    @Mock
    IEditorProvider editorProvider;
    @Mock
    IEditor editor;

    @Inject
    @Unit
    DisplayWindow displayWindow;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        createDir("./testDir");
        System.setProperty(Constants.PROP_PROJECT_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_FIXTURE_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_TEST_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_MODULE_DIRS, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_HOME, new File("./testDir").getCanonicalPath());
        new File("./testDir/readme.txt").createNewFile();
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    private static File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_DIR);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_TEST_DIR);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_HOME);
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

    @Before
    public void setUp() throws Exception {
        expect(outputPane.getComponent()).andReturn(new JTextArea());
        replay(outputPane);
        resultPane.addSelectionListener(displayWindow.resultPaneSelectionListener);
        expect(resultPane.getComponent()).andReturn(new JTextArea());
        replay(resultPane);
        displayWindow.initDesktop();
    }

    @After
    public void tearDown() throws Exception {
    }

    private void swingWait() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }

    @Test
    public void testOnShowReport() throws InterruptedException, InvocationTargetException {
        displayWindow.setResultReporterHTMLFile(new File("."));
        browserLauncher.openURLinBrowser(new File(".").toURI().toString());
        replay(browserLauncher);
        displayWindow.onShowReport();
        swingWait();
        verify(browserLauncher);
    }

    @Test
    public void testOnSelectFixture() {
        displayWindow.setDefaultFixture("default");
        expect(fixtureSelector.selectFixture((JFrame) anyObject(), (String[]) anyObject(), eq("default"))).andReturn("fixture1");
        expect(scriptModel.getFixtures()).andReturn(new String[] { "default", "fixture1" });
        reset(display);
        display.setDefaultFixture("fixture1");
        replay(scriptModel);
        replay(fixtureSelector);
        replay(display);
        displayWindow.onSelectFixture();
        assertEquals("fixture1", displayWindow.getFixture());
        assertEquals("fixture1", displayWindow.getDefaultFixture());
        verify(scriptModel);
        verify(display);
        verify(fixtureSelector);
    }

    @Test
    public void testOnPlay() {
        reset(display);
        display.play(new EditorConsole(displayWindow.displayView), null);
        reset(outputPane);
        outputPane.clear();
        replay(outputPane);
        replay(display);
        reset(resultPane);
        resultPane.clear();
        replay(resultPane);
        reset(callStack);
        callStack.clear();
        replay(callStack);
        displayWindow.onPlay();
        verify(display);
        assertNull(displayWindow.getResultReporter());
        assertFalse(displayWindow.displayView.isDebugging());
    }

    @Test
    public void testOnPlayWithGenerateReportsOn() {
        reset(display);
        display.play(new EditorConsole(displayWindow.displayView), null);
        reset(outputPane);
        outputPane.clear();
        replay(outputPane);
        replay(display);
        reset(resultPane);
        resultPane.clear();
        replay(resultPane);
        reset(callStack);
        callStack.clear();
        replay(callStack);
        displayWindow.setGenerateReports(true);
        displayWindow.onPlay();
        verify(display);
        assertNull(displayWindow.getResultReporter());
        assertFalse(displayWindow.displayView.isDebugging());
    }

    @Test
    public void testOnDebug() {
        reset(display);
        display.play(new EditorConsole(displayWindow.displayView), null);
        reset(outputPane);
        outputPane.clear();
        replay(outputPane);
        replay(display);
        reset(resultPane);
        resultPane.clear();
        replay(resultPane);
        reset(callStack);
        callStack.clear();
        replay(callStack);
        displayWindow.onDebug();
        verify(display);
        assertNull(displayWindow.getResultReporter());
        assertTrue(displayWindow.displayView.isDebugging());
    }

    @Test
    public void testOnSlowPlay() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnPause() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnResumeRecording() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnResumePlaying() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnRecord() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnEt() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnStop() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnRawRecord() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnOpenApplication() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnCloseApplication() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnInsertScript() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnInsertChecklist() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnProjectSettings() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnManageChecklists() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnEditorSettings() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnShortcutKeys() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnNewTestcase() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnNewModule() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnNewFixture() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnSave() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnSaveAs() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnSaveAll() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnExit() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnHelpAbout() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnReleaseNotes() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnChangeLog() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnVisitWebsite() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnUndo() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnRedo() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnCut() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnCopy() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnPaste() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnSearch() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnFindNext() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnFindPrevious() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnPreferences() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnResetWorkspace() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnSaveWorkspace() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnToggleBreakpoint() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnClearAllBreakpoints() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnStepInto() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnStepOver() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnStepReturn() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnPlayerConsole() {
        fail("Not yet implemented");
    }

    @Test
    public void testOnRecorderConsole() {
        fail("Not yet implemented");
    }

}
