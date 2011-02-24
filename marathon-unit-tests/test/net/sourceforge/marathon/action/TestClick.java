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
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JLabel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.MComponentMock;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestClick {
    private MComponentMock component;

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
        component = new MComponentMock();
    }

    @Test
    public void testSingleClick() throws Exception {
        click(1);
        component.getHistory().assertNextMessage("click(1, false)");
        component.getHistory().assertEmpty();
    }

    @Test
    public void testDoubleClick() throws Exception {
        click(2);
        component.getHistory().assertNextMessage("click(2, false)");
        component.getHistory().assertEmpty();
    }

    @Test
    public void testAllTypesOfClicks() throws Exception {
        click(null, 1, null, false);
        component.getHistory().assertNextMessage("click(1, false)");
        click(null, 1, null, true);
        component.getHistory().assertNextMessage("click(1, true)");

        click(new Point(5, 5), 1, null, false);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"\")");

        click(new Point(5, 5), 1, null, true);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"+Button3\")");

        click(new Point(5, 5), 1, "Alt", true);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"Alt+Button3\")");
    }

    @Test
    public void testPopupClick() throws Exception {
        click(1, true);
        component.getHistory().assertNextMessage("click(1, true)");
        component.getHistory().assertEmpty();
    }

    @Test
    public void testClickEx() throws Exception {
        MouseEvent e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK, 5, 5, 1, false);
        click(1, e, ClickAction.RECORD_EX);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"Alt\")");
        e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK, 5, 5, 1, false);
        click(1, e, ClickAction.RECORD_EX);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"Ctrl\")");
        e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, 5, 5, 1, false);
        click(1, e, ClickAction.RECORD_EX);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"Ctrl+Button3\")");
        e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.BUTTON1_DOWN_MASK, 5, 5, 1, false);
        click(1, e, ClickAction.RECORD_EX);
        component.getHistory().assertNextMessage("click(1, 5, 5, \"\")");
    }

    @Test
    public void testClickWithPositionToPython() throws Exception {
        MouseEvent e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.BUTTON1_DOWN_MASK, 5, 5, 1, false);
        ClickAction normalClick = new ClickAction(new ComponentId("button.name"), e, ClickAction.RECORD_EX,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        String expected = "click('button.name', 5, 5)\n";
        assertEquals(expected, normalClick.toScriptCode());

        e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK, 5, 5, 1, false);
        ClickAction ctrlClick = new ClickAction(new ComponentId("button.name"), e, ClickAction.RECORD_EX,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        expected = "click('button.name', 5, 5, 'Ctrl')\n";
        assertEquals(expected, ctrlClick.toScriptCode());

        e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, 5, 5, 1, false);
        ClickAction ctrlRightClick = new ClickAction(new ComponentId("button.name"), e, ClickAction.RECORD_EX,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        expected = "rightclick('button.name', 5, 5, 'Ctrl')\n";
        assertEquals(expected, ctrlRightClick.toScriptCode());

        e = new MouseEvent(new JLabel("DummyLabelComponent"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, 5, 5, 1, false);
        ClickAction ctrlAltRightClick = new ClickAction(new ComponentId("button.name"), e, ClickAction.RECORD_EX,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        expected = "rightclick('button.name', 5, 5, 'Ctrl+Alt')\n";
        assertEquals(expected, ctrlAltRightClick.toScriptCode());
    }

    @Test
    public void testToPython() {
        ClickAction normalClick = new ClickAction(new ComponentId("button.name"), 1, false,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        String expected = "click('button.name')\n";
        assertEquals(expected, normalClick.toScriptCode());
        ClickAction doubleClick = new ClickAction(new ComponentId("button.name"), 2, false,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        expected = "doubleclick('button.name')\n";
        assertEquals(expected, doubleClick.toScriptCode());
        ClickAction rightClick = new ClickAction(new ComponentId("button.name"), 2, true,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        expected = "rightclick('button.name', 2)\n";
        assertEquals(expected, rightClick.toScriptCode());
    }

    private void click(int clickCount, boolean isPopupTrigger) {
        new ClickAction(new ComponentId("button.name"), clickCount, isPopupTrigger, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()).play(component.getDummyResolver());
    }

    @Test
    public void testEquals() {
        ComponentId foo = new ComponentId("foo");
        ActionTestCase.testEquals(new ClickAction(foo, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()),
                new ClickAction(foo, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), new ClickAction(
                        new ComponentId("bar"), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()));
        assertTrue(!new ClickAction(foo, 1, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance())
                .equals(new ClickAction(foo, 2, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance())));
    }

    private void click(int numberOfClicks) {
        click(numberOfClicks, false);
    }

    private void click(int clickCount, MouseEvent e, int record_click) {
        new ClickAction(new ComponentId("button.name"), e, record_click, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()).play(component.getDummyResolver());
    }

    private void click(Point position, int clickCount, String modifiers, boolean isPopupTrigger) {
        new ClickAction(new ComponentId("button.name"), position, clickCount, modifiers, isPopupTrigger,
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(component.getDummyResolver());

    }
}
