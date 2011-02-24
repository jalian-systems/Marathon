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

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.JButton;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.MButton;
import net.sourceforge.marathon.component.WindowIdMock;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.util.Indent;

public class TestScriptRecorder {
    private ScriptRecorder recorder;
    private ScriptListenerMock listener;
    private String i1;
    private String i2;

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
        i1 = Indent.getIndent();
        i2 = i1 + i1;
        listener = new ScriptListenerMock();
        recorder = new ScriptRecorder(listener);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRecordWindowTagOnClick() {
        MButton button = getMButton("dialog1", "button1");
        recorder.record(click(button, 1));
        recorder.record(click(button, 2));
        AWTSync.sync();
        assertScriptEquals(i1 + "if window('dialog1'):\n" + i2 + "click('button1')\n" + i2 + "doubleclick('button1')\n" + i1
                + "close()\n");
    }

    private MockRecordedClick click(MButton button, int clicks) {
        return new MockRecordedClick(button.getComponentId(), new WindowIdMock("dialog1"), clicks);
    }

    @Test
    public void testRecordWindowTagOnAssert() {
        recorder.record(recordable("button1", "dialog1", "assertEnabled('button1', true)\n"));
        AWTSync.sync();
        assertScriptEquals(i1 + "if window('dialog1'):\n" + i2 + "assertEnabled('button1', true)\n" + i1 + "close()\n");
    }

    private IScriptElement recordable(final String component, final String windowTitle, final String python) {
        return new IScriptElement() {
            private static final long serialVersionUID = 1L;

            public String toScriptCode() {
                return Indent.getIndent() + python;
            }

            public ComponentId getComponentId() {
                return new ComponentId(component);
            }

            public WindowId getWindowId() {
                return new WindowIdMock(windowTitle);
            }

            public boolean isUndo() {
                return false;
            }

            public IScriptElement getUndoElement() {
                return null;
            }
        };
    }

    @Test
    public void testRecordWindowTagOnSelect() {
        MButton button = new MButton(new JButton("text"), "button1", null, WindowMonitor.getInstance());
        WindowId windowId = new WindowIdMock("dialog1");
        button.setWindowId(windowId);
        recorder.record(new MockRecordedSelect(button.getComponentId(), windowId, "text"));
        AWTSync.sync();
        assertScriptEquals(i1 + "if window('dialog1'):\n" + i2 + "select('button1', 'text')\n" + i1 + "close()\n");
    }

    @Test
    public void testRecordKeyStroke() throws Exception {
        recorder.record(recordable("o", "dialog1", "keystroke('Ctrl+Shift+B')\n"));
        AWTSync.sync();
        assertScriptEquals(i1 + "if window('dialog1'):\n" + i2 + "keystroke('Ctrl+Shift+B')\n" + i1 + "close()\n");
    }

    @Test
    public void testSimpleSwitchBetweenWindows() {
        recorder.record(new MockRecordedClick(new ComponentId("button1"), new WindowIdMock("dialog1"), 1));
        recorder.record(new MockRecordedClick(new ComponentId("button2"), new WindowIdMock("dialog2"), 1));
        AWTSync.sync();
        assertScriptEquals(i1 + "if window('dialog1'):\n" + i2 + "click('button1')\n" + i1 + "close()\n" + "\n" + i1
                + "if window('dialog2'):\n" + i2 + "click('button2')\n" + i1 + "close()\n");
    }

    private void assertScriptEquals(String script) {
        assertEquals(script, listener.script);
    }

    private MButton getMButton(String dialogTitle, String name) {
        MButton button = new MButton(null, name, null, WindowMonitor.getInstance());
        button.setWindowId(new WindowIdMock(dialogTitle));
        return button;
    }
}
