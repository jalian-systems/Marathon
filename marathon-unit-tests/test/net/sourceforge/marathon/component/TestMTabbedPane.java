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
package net.sourceforge.marathon.component;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ComponentId;

public class TestMTabbedPane {
    private DialogForTesting tabDialog;
    private MTabbedPane tabPane;

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
        tabDialog = new DialogForTesting(this.getClass().getName());
        tabDialog.addTabbedPane("tabbedpane.name", "tab1", "tab2");
        tabDialog.show();
        tabPane = (MTabbedPane) tabDialog.getResolver().getMComponentById(new ComponentId("tabbedpane.name"));
    }

    @After
    public void tearDown() throws Exception {
        tabDialog.dispose();
        tabDialog = null;
    }

    @Test
    public void testGetSetText() {
        assertEquals("tab1", tabPane.getText());
        tabPane.setText("tab2");
        assertEquals("tab2", tabPane.getText());
    }

    @Test
    public void testTabSelectionRunsInAWTThread() throws Exception {
        AWTThreadGrabber awtThreadGrabber = new AWTThreadGrabber();
        JTabbedPane pane = tabDialog.getTabbedPane();
        TabbedPaneListener listener = new TabbedPaneListener();
        pane.addChangeListener(listener);
        tabPane.setText("tab1");
        tabPane.setText("tab2");
        assertSame("ran in wrong thread", awtThreadGrabber.awtThread, listener.ranIn);
    }

    @Test
    public void testSettingAnInvalidTab() throws Exception {
        try {
            tabPane.setText("tab3");
            fail("should not be able to select invalidtab");
        } catch (ComponentException e) {
        }
    }

    private static class TabbedPaneListener implements ChangeListener {
        Thread ranIn;

        public void stateChanged(ChangeEvent e) {
            ranIn = Thread.currentThread();
        }
    }

    private static class AWTThreadGrabber implements Runnable {
        Thread awtThread;

        public AWTThreadGrabber() throws Exception {
            SwingUtilities.invokeAndWait(this);
        }

        public void run() {
            awtThread = Thread.currentThread();
        }
    }
}
