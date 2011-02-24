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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.MessageList;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.util.ContextMenuTriggers;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRecordingEventQueue {
    private DialogForTesting dialog;
    private MessageList events;
    private RecordingEventQueue eventQueue;

    @BeforeClass
    public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_RECORDER_MOUSETRIGGER);
        properties.remove(Constants.PROP_RECORDER_KEYTRIGGER);
        System.setProperties(properties);
    }

    @AfterClass
    public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Before
    public void setUp() throws Exception {
        dialog = new DialogForTesting(this.getClass().getName());
        dialog.addButton("name", "text");
        events = new MessageList();
        eventQueue = new RecordingEventQueue(null, null, null, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()) {
            public ContextMenuWindow showPopup(Component c, Point point) {
                events.add("popup shown " + c.getClass());
                return null;
            }
        };
        eventQueue.attach();
        dialog.show();
    }

    @After
    public void tearDown() throws Exception {
        eventQueue.detach();
        dialog.dispose();
        dialog = null;
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_RECORDER_MOUSETRIGGER);
        properties.remove(Constants.PROP_RECORDER_KEYTRIGGER);
        System.setProperties(properties);
    }

    @Test
    public void testPopupAppears() throws InterruptedException, InvocationTargetException {
        Properties p = System.getProperties();
        p.remove(Constants.PROP_RECORDER_MOUSETRIGGER);
        System.setProperties(p);
        ContextMenuTriggers.setContextMenuModifiers();
        int contextMenuModifiers = ContextMenuTriggers.getContextMenuModifiers();
        dialog.getButton().requestFocus();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        click(contextMenuModifiers);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        events.assertNextMessage("popup shown " + JButton.class);
        events.assertEmpty();
    }

    @Test
    public void testPopupAppearsWithHotKey() {
        click(InputEvent.BUTTON1_DOWN_MASK);
        type(ContextMenuTriggers.getContextMenuKeyCode(), ContextMenuTriggers.getContextMenuKeyModifiers());
        events.assertNextMessage("popup shown " + JButton.class);
        events.assertEmpty();
    }

    @Test
    public void testPopupAppearsWithCustomHotKey() {
        System.setProperty(Constants.PROP_RECORDER_KEYTRIGGER, "Ctrl+F8");
        ContextMenuTriggers.setContextMenuKey();
        click(InputEvent.BUTTON1_DOWN_MASK);
        type(ContextMenuTriggers.getContextMenuKeyCode(), ContextMenuTriggers.getContextMenuKeyModifiers());
        events.assertNextMessage("popup shown " + JButton.class);
        events.assertEmpty();
    }

    @Test
    public void testPopupAppearsWithCustomClick() {
        System.setProperty(Constants.PROP_RECORDER_MOUSETRIGGER, "Alt+Button1");
        ContextMenuTriggers.setContextMenuModifiers();
        click(InputEvent.BUTTON1_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        events.assertNextMessage("popup shown " + JButton.class);
        events.assertEmpty();
    }

    @Test
    public void testPopupAppearsWithCustomClickWithTwoModifierKeys() {
        System.setProperty(Constants.PROP_RECORDER_MOUSETRIGGER, "Meta+Alt+Button1");
        ContextMenuTriggers.setContextMenuModifiers();
        click(InputEvent.BUTTON1_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        events.assertNextMessage("popup shown " + JButton.class);
        events.assertEmpty();
    }

    @Test
    public void testPopupAppearsWithCustomClickWithButton3() {
        System.setProperty(Constants.PROP_RECORDER_MOUSETRIGGER, "Alt+Button3");
        ContextMenuTriggers.setContextMenuModifiers();
        click(InputEvent.BUTTON3_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        events.assertNextMessage("popup shown " + JButton.class);
        events.assertEmpty();
    }

    @Test
    public void testPopupDoesNotAppearForNonPopupTriggers() {
        click(0);
        events.assertEmpty();
    }

    @Test
    public void testPopupDoesNotAppearForNonPopupTriggers0() {
        click(0);
        events.assertEmpty();
    }

    private void click(int modifiers) {
        Point p = dialog.getButton().getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, dialog);
        EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        MouseEvent theEvent = new MouseEvent(dialog, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), modifiers, p.x, p.y, 1,
                false);
        systemEventQueue.postEvent(theEvent);
        AWTSync.sync();
    }

    private void type(int keyCode, int modifiers) {
        KeyEvent event = new KeyEvent(dialog, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, (char) 0);
        Point p = dialog.getButton().getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, dialog);
        EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        // systemEventQueue.postEvent(new MouseEvent(dialog,
        // MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
        // modifiers, p.x, p.y, 1, false));
        systemEventQueue.postEvent(event);
        AWTSync.sync();
    }

}
