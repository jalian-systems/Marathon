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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Properties;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.action.AbstractMarathonAction;
import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.SelectAction;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.Snooze;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * these are course-grained tests
 * 
 * We are trying to do a whole round trip - a action is played, the recorder
 * records it, and then generates another action from it
 * 
 * in fixtures like ClickTest, we can mock everything outside of the action here
 * we play a action and make sure it gets recorded in the same form
 * 
 * These are the tests that should all fail on a new platform / jdk - everything
 * else should be solid
 */
public class TestRecordingTags {
    private DialogForTesting dialog;
    private ScriptRecorder recorder;
    private static final ComponentId BUTTON_ID = new ComponentId("button.name");
    private RecordingEventListener listener;
    private ScriptListenerMock scriptListener;
    String i1;
    String i2;

    @BeforeClass public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    @AfterClass public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Before public void setUp() throws Exception {
        WindowMonitor.getInstance();
        scriptListener = new ScriptListenerMock();
        recorder = new ScriptRecorder(scriptListener);
        dialog = new DialogForTesting(this.getClass().getName());
        listener = new RecordingEventListener(recorder, null, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance());
        dialog.getResolver().setRecording(true);
        listener.startListening(dialog.getResolver());
        i1 = Indent.getIndent();
        i2 = i1 + i1;
    }

    public void runBare() throws Throwable {
        try {
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    @After public void tearDown() throws Exception {
        listener.stopListening();
        dialog.dispose();
        dialog = null;
    }

    @Test public void testClickAButton() throws Exception {
        dialog.addButton("button.name", "button.name");
        dialog.show();
        sleep();
        new ClickAction(BUTTON_ID, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(dialog
                .getResolver());
        AWTSync.sync();
        assertRecorded(i2 + "click('button.name')\n");
    }

    @Test public void testDoubleClickAButton() throws Exception {
        dialog.addButton("button.name", "button.name");
        dialog.show();
        sleep();
        new ClickAction(BUTTON_ID, 2, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(dialog
                .getResolver());
        AWTSync.sync();
        assertRecorded(i2 + "doubleclick('button.name')\n");
    }

    @Test public void testRightClickAButton() throws Exception {
    }

    @Test public void testSelectATextField() throws Exception {
        dialog.addTextField("text.name", "foo");
        dialog.addButton("button.name", "button.name");
        dialog.show();
        sleep();
        new SelectAction(new ComponentId("text.name"), "abc", ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()).play(dialog.getResolver());
        new ClickAction(BUTTON_ID, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(dialog
                .getResolver());
        assertRecorded(i2 + "select('text.name', 'abc')\n" + i2 + "click('button.name')\n");
    }

    @Test public void testSelectAComboBox() throws Exception {
        dialog.addComboBox("combo.name", new String[] { "a", "b", "c" });
        dialog.addButton("button.name", "button.name");
        dialog.show();
        new SelectAction(new ComponentId("combo.name"), "c", ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()).play(dialog.getResolver());
        new ClickAction(BUTTON_ID, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(dialog
                .getResolver());
        assertEquals("c", dialog.getComboBox().getSelectedItem());
        assertRecorded(i2 + "select('combo.name', 'c')\n" + i2 + "click('button.name')\n");
    }

    @Test public void testSelectInsideATable() throws Exception {
        showTable();
        play(new SelectAction(new ComponentId("table.name", "col1,1"), "NEW VALUE", ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()));
        play(new ClickAction(new ComponentId("table.name", "col2,1"), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()));
        assertEquals("NEW VALUE", dialog.getTable().getValueAt(1, 0));

        assertRecorded(i2 + "select('table.name', 'NEW VALUE', '{1, col1}')\n");
    }

    @Test public void testClickInsideATable() throws Exception {
        showTable();
        play(new ClickAction(new ComponentId("table.name", "col1,1"), 1, true, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()));
        play(new ClickAction(new ComponentId("table.name", "col1,1"), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()));

        assertRecorded(i2 + "select('table.name', '')\n" + i2 + "rightclick('table.name', '{1, col1}')\n");
    }

    // I don't know why this test exists in the current form. Don't we expect
    // double clicks to be
    public void xtestDoubleClickInsideATable() throws Exception {
        showTable();
        play(new ClickAction(new ComponentId("table.name", "col1,1"), 2, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()));
        assertRecorded(null);
    }

    @Test public void testSelectATabWhenWindowNameChanges() throws Exception {
        dialog.addTabbedPane("TabPane", "tab1", "tab2");
        dialog.getTabbedPane().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                dialog.setTitle(dialog.getTabbedPane().getTitleAt(dialog.getTabbedPane().getSelectedIndex()));
            }
        });
        dialog.setTitle(dialog.getTabbedPane().getTitleAt(dialog.getTabbedPane().getSelectedIndex()));
        dialog.show();
        JTabbedPane pane = dialog.getTabbedPane();
        Rectangle tabBounds = pane.getUI().getTabBounds(pane, 1);
        Point p = new Point(tabBounds.x + 1, tabBounds.y + 1);
        FireableMouseClickEvent e = new FireableMouseClickEvent(pane);
        e.fire(p, 1);
        new Snooze(1000);
        assertEquals(i1 + "if window('tab1'):\n" + i2 + "select('TabPane', 'tab2')\n" + i1 + "close()\n", scriptListener.script);
    }

    private void assertRecorded(String recorded) {
        if (recorded == null) {
            assertNull("Expected: null got: " + scriptListener.script, scriptListener.script);
        } else {
            String i1 = Indent.getIndent();
            assertEquals(i1 + "if window('" + this.getClass().getName() + "'):\n" + recorded + i1 + "close()\n",
                    scriptListener.script);
        }
    }

    private void play(AbstractMarathonAction action) {
        action.play(dialog.getResolver());
    }

    private void sleep() throws InterruptedException {
        Thread.sleep(10);
    }

    private void showTable() throws InterruptedException {
        dialog.addTable();
        dialog.show();
        sleep();
    }
}
