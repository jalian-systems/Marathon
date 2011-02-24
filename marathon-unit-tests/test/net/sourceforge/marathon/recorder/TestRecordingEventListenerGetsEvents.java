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

import static org.junit.Assert.fail;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.runtime.RecorderMock;
import net.sourceforge.marathon.util.Retry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * this class tests half of the recordingEventListener's functionality - it
 * ensures that by putting an event in the eventQueue, it gets shot into the
 * appropriate method in recordingEventListener
 * 
 * the rest is tested in recordingEventListenerTest
 */
public class TestRecordingEventListenerGetsEvents {
    private DialogForTesting dialog;
    private RecordingEventListener listener;
    private StringBuffer events;

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
        events = new StringBuffer();
        listener = new RecordingEventListener(new RecorderMock(), null, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()) {
            protected void mousePressed(MComponent component, MouseEvent e) {
                events.append("mouse pressed on " + getName(component) + " with " + e.getClickCount() + " clicks");
            }

            public void focusGained(MComponent component) {
                events.append("focus gained on " + getName(component));
            }

            public void focusLost(MComponent component) {
                events.append("focus lost");
            }

            protected void keyPressed(MComponent component, KeyEvent e, boolean isMenuActive) {
                events.append("key pressed on " + getName(component));
            }
        };
        listener.startListening(new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()));
        dialog = new DialogForTesting(this.getClass().getName());
        dialog.addButton("button.name", "button.name");
        dialog.addTextField("text.name", "");
        dialog.addComboBox("combo.name", new String[] { "choiceA", "choiceB", "choiceC" });
        dialog.show();
        waitForIt();
        try {
            new Retry("timed out while waiting for recordingEventListener to start picking up events", 50, 60, new Retry.Attempt() {
                public void perform() {
                    fireMouseEvent(dialog.getButton(), MouseEvent.MOUSE_PRESSED, 1);
                    if (events.toString().indexOf("mouse pressed on button.name") == -1) {
                        retry();
                    }
                }
            });
        } catch (Exception e) {
            listener.stopListening();
            throw e;
        }
        waitForIt();
        events.setLength(0);
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
        dialog = null;
        listener.stopListening();
        events = null;
    }

    private String getName(MComponent component) {
        return component != null ? component.getMComponentName() : null;
    }

    @Test
    public void testMousePressed() {
        fireMouseEvent(dialog.getButton(), MouseEvent.MOUSE_PRESSED, 1);
        waitForIt();
        assertContains("mouse pressed on button.name");
    }

    @Test
    public void testMouseReleased() {
        fireMouseEvent(dialog.getButton(), MouseEvent.MOUSE_PRESSED, 1);
        waitForIt();
        assertContains("mouse pressed on button.name with 1 clicks");
    }

    @Test
    public void testDoubleClick() {
        fireMouseEvent(dialog.getButton(), MouseEvent.MOUSE_PRESSED, 2);
        waitForIt();
        assertContains("mouse pressed on button.name with 2 clicks");
    }

    @Test
    public void testFocusGained() {
        fireFocusEvent(dialog.getButton(), FocusEvent.FOCUS_LOST);
        fireFocusEvent(dialog.getButton(), FocusEvent.FOCUS_GAINED);
        waitForIt();
        assertContains("focus gained on button.name");
    }

    /*
     * Removed focusLost event from RecordingEventListener public void
     * testFocusLost() { fireFocusEvent(dialog.getButton(),
     * FocusEvent.FOCUS_LOST); waitForIt(); assertContains("focus lost"); }
     */

    @Test
    public void testKeyPressed() {
        dialog.getTextField().requestFocus();
        fireKeyEvent(dialog.getTextField(), KeyEvent.KEY_PRESSED);
        waitForIt();
        assertContains("key pressed on text.name");
    }

    private void waitForIt() {
        AWTSync.sync();
    }

    private void fireFocusEvent(Component component, int id) {
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new FocusEvent(component, id));
    }

    private void fireMouseEvent(Component component, int id, int click) {
        MouseEvent event = new MouseEvent(component, id, System.currentTimeMillis(), 0, 1, 1, click, false);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
    }

    private void fireKeyEvent(Component component, int id) {
        KeyEvent event = new KeyEvent(component, id, System.currentTimeMillis(), 0, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
    }

    private void assertContains(String text) {
        if (events.toString().indexOf(text) == -1) {
            fail("couldn't find (" + text + ") in (" + events + ")");
        }
    }
}
