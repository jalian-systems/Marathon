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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.Action;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.editor.rsta.RSTAEditorProvider;

import org.easymock.IAnswer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

import static org.easymock.EasyMock.* ;

@RunWith(AtUnit.class)
@Container(Option.GUICE)
@MockFramework(atunit.MockFramework.Option.EASYMOCK)
public class TestDisplayWindow implements Module {
    private @Unit
    @Inject
    DisplayWindow view;
    @Mock
    IRuntimeFactory runtimeFactory;
    @Mock
    Display display;
    
    private Action play;
    private Action record;
    private Action stop;
    private Action open;
    private Action close;
    private Action exit;
    private Action pause;
    private Action resume;
    private Action insert;

    @BeforeClass
    public static void setupClass() throws IOException {
        String dirName = "./testDir";
        createDir(dirName);
        createFixtureFile(dirName, "default.py");
    }

    @AfterClass
    public static void teardownClass() {
        deleteRecursive(new File("./testDir"));
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_DIR);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_TEST_DIR);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_HOME);
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }
    
    @After
    public void tearDown() throws Exception {
        if (view != null)
            (view).dispose();
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
        createDir("./testDir");
        view.onNewTestcase();
        view.displayView.setState(State.STOPPED_WITH_APP_CLOSED);
        play = view.playAction;
        record = view.recordAction;
        stop = view.stopAction;
        open = view.openApplicationAction;
        close = view.closeApplicationAction;
        exit = view.exitAction;
        pause = view.pauseAction;
        resume = view.resumeRecordingAction;
        insert = view.getInsertAction();
    }

    private static File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    private static void createFixtureFile(String dirName, String fixtureName) throws IOException {
        File fixtureFile = new File(dirName, fixtureName);
        fixtureFile.createNewFile();
    }

    @Test
    public void testStoppedWithAppClosedState() throws InterruptedException, InvocationTargetException {
        view.displayView.setState(State.STOPPED_WITH_APP_CLOSED);
        assertEnabled(open);
        assertEnabled(play);
        assertEnabled(record);
        assertDisabled(insert);
        assertDisabled(stop);
        assertDisabled(close);
        assertEnabled(exit);
        assertDisabled(pause);
        assertDisabled(resume);
        assertDisabled(view.saveAction);
        assertEnabled(view.saveAsAction);
        assertDisabled(view.saveAllAction);
    }

    @Test
    public void testStoppedWithAppOpenState() throws InterruptedException, InvocationTargetException {
        view.displayView.setState(State.STOPPED_WITH_APP_OPEN);
        assertEnabled(close);
        assertEnabled(play);
        assertEnabled(record);
        assertDisabled(insert);
        assertEnabled(exit);
        assertDisabled(stop);
        assertDisabled(open);
        assertDisabled(pause);
        assertDisabled(resume);
        assertDisabled(view.saveAction);
        assertEnabled(view.saveAsAction);
        assertDisabled(view.saveAllAction);
    }

    @Test
    public void testPlayingState() {
        view.displayView.setState(State.PLAYING);
        assertEnabled(stop);
        assertEnabled(exit);
        assertDisabled(play);
        assertDisabled(record);
        assertDisabled(insert);
        assertDisabled(open);
        assertDisabled(close);
        assertDisabled(pause);
        assertDisabled(resume);
        assertFileActionsEnabled(false);
    }

    @Test
    public void testRecordingState() {
        reset(display);
        IAnswer<? extends net.sourceforge.marathon.api.module.Module> arg0 = new IAnswer<net.sourceforge.marathon.api.module.Module>() {

            public net.sourceforge.marathon.api.module.Module answer() throws Throwable {
                return null;
            }
        };
        expect(display.getModuleFuctions()).andAnswer(arg0);
        replay(display);
        view.displayView.setState(State.RECORDING);
        assertEnabled(stop);
        assertEnabled(exit);
        assertDisabled(play);
        assertDisabled(record);
        assertDisabled(insert);
        assertDisabled(open);
        assertDisabled(close);
        assertEnabled(pause);
        assertDisabled(resume);
        assertFileActionsEnabled(false);
    }

    @Test
    public void testPauseState() {
        view.displayView.setState(State.RECORDINGPAUSED);
        assertDisabled(stop);
        assertEnabled(exit);
        assertDisabled(play);
        assertDisabled(record);
        assertDisabled(insert);
        assertDisabled(open);
        assertDisabled(close);
        assertDisabled(pause);
        assertEnabled(resume);
        assertFileActionsEnabled(false);
    }

    private void assertEnabled(Action a) {
        assertEquals(true, a.isEnabled());
    }

    private void assertDisabled(Action a) {
        assertEquals(false, a.isEnabled());
    }

    private void assertFileActionsEnabled(boolean expected) {
        assertEquals(expected, view.saveAction.isEnabled());
        assertEquals(expected, view.saveAsAction.isEnabled());
    }

    public void configure(Binder binder) {
        try {
            System.setProperty(Constants.PROP_PROJECT_DIR, new File("./testDir").getCanonicalPath());
            System.setProperty(Constants.PROP_HOME, new File("./testDir").getCanonicalPath());
            System.setProperty(Constants.PROP_FIXTURE_DIR, new File("./testDir").getCanonicalPath());
            System.setProperty(Constants.PROP_TEST_DIR, new File("./testDir").getCanonicalPath());
            System.setProperty(Constants.PROP_MODULE_DIRS, new File("./testDir").getCanonicalPath());
            System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            binder.bind(BrowserLauncher.class).toInstance(new BrowserLauncher());
            binder.bind(IEditorProvider.class).toInstance(new RSTAEditorProvider());
        } catch (BrowserLaunchingInitializingException e) {
            e.printStackTrace();
        } catch (UnsupportedOperatingSystemException e) {
            e.printStackTrace();
        }
        binder.bind(IScriptModelClientPart.class).toInstance(ScriptModelClientPart.getModel());
        binder.bind(FixtureSelector.class).toInstance(new FixtureSelector());
    }
}
