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
package net.sourceforge.marathon.recorder;

import static org.junit.Assert.assertTrue;

import java.awt.Dialog;
import java.awt.Window;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ScriptModelServerPart;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestWindowMonitor {

    @BeforeClass
    public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    @AfterClass
    public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Before
    public void setUp() throws Exception {
        WindowMonitor.getInstance();
        new MockWindow(getName());
    }

    private String getName() {
        return getClass().getName();
    }

    @Test
    public void testWithRealWindow() {
        JDialog window = new JDialog();
        window.setTitle("testWithRealWindow");
        try {
            window.pack();
            window.setVisible(true);
            WindowMonitor.getInstance().waitForWindowToOpen(1000, window.getTitle(), ScriptModelServerPart.getModelServerPart());
            assertTrue(window.isShowing());
        } finally {
            window.dispose();
        }
    }

    @Test
    public void testWithRegex() {
        JDialog window = new JDialog();
        window.setTitle("testWithRealWindow");
        try {
            window.pack();
            window.setVisible(true);
            WindowMonitor.getInstance().waitForWindowToOpen(1000, "/testWith[Rr]eal[wW]indow",
                    ScriptModelServerPart.getModelServerPart());
            assertTrue(window.isShowing());
        } finally {
            window.dispose();
        }
    }

    @Test
    public void testDuplicateWindowTitles() {
        JDialog window = new JDialog();
        JDialog window1 = new JDialog();
        window.setTitle("testDuplicateWindowTitles");
        try {
            window.pack();
            window.setVisible(true);
            window1.setTitle(window.getTitle());
            window1.pack();
            window1.setVisible(true);
            WindowMonitor.getInstance().waitForWindowToOpen(1000, window1.getTitle() + "(1)",
                    ScriptModelServerPart.getModelServerPart());
            assertTrue(window1.isShowing());
        } finally {
            window.dispose();
            window1.dispose();
        }
    }

    // public void testDontSeeIgnoredWindows() {
    // monitor.topLevelWindowCreated(window);
    // assertNotNull(monitor.getWindow(window.getTitle()));
    // monitor.waitForWindowToOpen(0, window.getTitle());
    // window.setName("Ignore Me");
    // assertNull(monitor.getWindow(window.getTitle()));
    // try {
    // monitor.waitForWindowToOpen(0, window.getTitle());
    // fail("should be invisible");
    // } catch (WindowNotFoundException e) {
    // }
    // }
    //
    // public void testDontSeeHiddenWindows() {
    // monitor.topLevelWindowCreated(window);
    // assertNotNull(monitor.getWindow(window.getTitle()));
    // monitor.waitForWindowToOpen(0, window.getTitle());
    // window.setShowing(false);
    // assertNull(monitor.getWindow(window.getTitle()));
    // try {
    // monitor.waitForWindowToOpen(0, window.getTitle());
    // fail("should be invisible");
    // } catch (WindowNotFoundException e) {
    // }
    // }

    @Test
    public void testShouldIgnoreOnRecordingArtifacts() {
        JFrame frame = new JFrame();
        assertTrue(!WindowMonitor.getInstance().shouldIgnore(frame));
        Dialog recordingArtifact = new MockRecordingArtifactWindow("Recording Artifact");
        assertTrue(WindowMonitor.getInstance().shouldIgnore(recordingArtifact));
        Window recordingArtifactChild = new MockWindow("child of recording artifact", recordingArtifact);
        assertTrue(WindowMonitor.getInstance().shouldIgnore(recordingArtifactChild));
    }

    @Test
    public void testShouldIgnoreOnIgnoredNames() {
        new JFrame();
        Dialog window1 = new MockWindow("window1");
        assertTrue(!WindowMonitor.getInstance().shouldIgnore(window1));
        window1.setName(WindowMonitor.IGNORED_COMPONENT_NAME);
        assertTrue(WindowMonitor.getInstance().shouldIgnore(window1));
        Dialog window2 = new MockWindow("window2", window1);
        assertTrue(WindowMonitor.getInstance().shouldIgnore(window2));
    }

    public static class MockWindow extends JDialog {
        private static final long serialVersionUID = 1L;
        private boolean _showing = true;

        public MockWindow(String title) {
            setTitle(title);
        }

        public MockWindow(String title, Dialog owner) {
            super(owner);
            setTitle(title);
        }

        public boolean isShowing() {
            return _showing;
        }

        public void setShowing(boolean showing) {
            _showing = showing;
        }
    }

    public static class MockRecordingArtifactWindow extends JDialog implements IRecordingArtifact {
        private static final long serialVersionUID = 1L;
        private boolean _showing = true;

        public MockRecordingArtifactWindow(String title) {
            setTitle(title);
        }

        public boolean isShowing() {
            return _showing;
        }

        public void setShowing(boolean showing) {
            _showing = showing;
        }
    }
}
