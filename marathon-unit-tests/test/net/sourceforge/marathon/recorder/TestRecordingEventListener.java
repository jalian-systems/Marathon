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

import java.awt.AWTException;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.MessageList;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.component.MComponentMock;
import net.sourceforge.marathon.component.WindowIdMock;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.runtime.RecorderMock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * this class tests half of the recordingEventListener's functionality - it
 * assumes that if an event is thrown into the event queue it will make it's way
 * into the appropriate method, so it calls these "appropriate methods" directly
 * passing in MComponents instead of AWT Events
 * 
 * the event based stuff is tested in RecordingEventListenerGetsEventsTest
 */
public class TestRecordingEventListener {
    private RecordingEventListener listener;
    private MessageList events = new MessageList();
    private JButton button = new JButton();
    private MComponentMock mComponentMock;
    private RecorderMock builder;

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
        mComponentMock = new MComponentMock(button, "button.name");
        mComponentMock.setText("foobar");
        events.clear();
        builder = new RecorderMock();
        listener = new RecordingEventListener(builder, null, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPressReleaseOfSameComponent() throws AWTException {
        JCheckBox neededForMouseEvent = new JCheckBox();
        MouseEvent e = new MouseEvent(neededForMouseEvent, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                InputEvent.BUTTON1_DOWN_MASK, 0, 0, 1, false);
        listener.mousePressed(mComponentMock, e);
        builder.assertNext(createClick(1));
    }

    private MockRecordedClick createClick(int clicks) {
        return new MockRecordedClick(mComponentMock.getComponentId(), new WindowIdMock("foo window"), clicks);
    }

    @Test
    public void testDoubleClick() {
        JCheckBox neededForMouseEvent = new JCheckBox();
        MouseEvent e = new MouseEvent(neededForMouseEvent, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                InputEvent.BUTTON1_DOWN_MASK, 0, 0, 2, false);
        listener.mousePressed(mComponentMock, e);
        builder.assertNext(createClick(2));
    }

    private MockRecordedSelect createSelect(String text) {
        return createSelect(mComponentMock, text);
    }

    private MockRecordedSelect createSelect(MComponent component, String text) {
        return new MockRecordedSelect(component.getComponentId(), new WindowIdMock("foo window"), text);
    }

    @Test
    public void testTextNotSetInTextField() {
        listener.focusGained(mComponentMock);
        listener.focusLost(null);
        builder.assertEmpty();
    }

    @Test
    public void testForgetSelectOnSecondRecord() {
        listener.startListening(new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()));
        try {
            listener.focusGained(mComponentMock);
            mComponentMock.setText("abc");
            listener.focusLost(null);
            builder.assertNext(createSelect("abc"));
            builder.assertEmpty();
            listener.stopListening();
            listener.startListening(new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor
                    .getInstance()));
            listener.focusLost(null);
            builder.assertEmpty();
        } finally {
            listener.stopListening();
        }
    }

}
