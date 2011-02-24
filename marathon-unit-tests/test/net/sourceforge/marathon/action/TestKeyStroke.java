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
package net.sourceforge.marathon.action;

import static org.junit.Assert.assertEquals;

import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.JTextField;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestKeyStroke {
    private static final int CONTROL = KeyEvent.VK_CONTROL;
    private static final int ALT = KeyEvent.VK_ALT;
    private static final int T = KeyEvent.VK_T;
    private DialogForTesting dialog;
    private KeyListenerMock keyListener;
    private JTextField textField;

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
        dialog = new DialogForTesting(getName());
        dialog.addTextField("textfield", "doesn't matter");
        textField = dialog.getTextField();
        textField.setText("text");
        dialog.show();
        textField.requestFocus();
        keyListener = new KeyListenerMock();
        textField.addKeyListener(keyListener);
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        try {
            dialog.dispose();
            assertEquals("text", textField.getText());
        } finally {
        }
    }

    @Test
    public void testEquals() throws Exception {
        ActionTestCase.testEquals(
                new KeyStrokeAction("Ctrl+Shift", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()),
                new KeyStrokeAction("Ctrl+Shift", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()),
                new KeyStrokeAction("Enter", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()));
    }

    @Test
    public void testPressEnter() throws Exception {
        strokeWith("Enter");
        assertPressed(KeyEvent.VK_ENTER);
        assertTyped(KeyEvent.VK_ENTER);
        assertReleased(KeyEvent.VK_ENTER);
        assertEmpty();
    }

    @Test
    public void testPressMultipleKeysInSequence() throws Exception {
        strokeWith("Ctrl+Alt+T");
        assertPressed(ALT);
        assertPressed(CONTROL);
        assertPressed(T);
        assertReleased(T);
        assertReleased(ALT);
        assertReleased(CONTROL);
        assertEmpty();
    }

    @Test
    public void testToPython() throws Exception {
        assertEquals("keystroke('KeyStrokeAction', 'Ctrl+Alt+T')\n",
                new KeyStrokeAction("Ctrl+Alt+T", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance())
                        .toScriptCode());
    }

    @Test
    public void testToPythonWithSingleKeyCombinationKeyStroke() throws Exception {
        assertEquals("keystroke('KeyStrokeAction', 'Enter')\n",
                new KeyStrokeAction("Enter", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance())
                        .toScriptCode());
    }

    private void assertReleased(int keycode) {
        keyListener.assertReleased(keycode);
    }

    private void assertTyped(int keycode) {
        keyListener.assertTyped(keycode);
    }

    private void assertPressed(int keycode) {
        keyListener.assertPressed(keycode);
    }

    private void strokeWith(String sequence) {
        new KeyStrokeAction(new ComponentId("textfield"), sequence, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()).play(dialog.getResolver());
    }

    private void assertEmpty() {
        keyListener.assertEmpty();
    }
}
