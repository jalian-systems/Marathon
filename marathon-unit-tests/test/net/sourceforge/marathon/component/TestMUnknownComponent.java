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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JLabel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class TestMUnknownComponent {
    public static class UnknownComponentResolver extends ComponentResolver {
        public UnknownComponentResolver(ComponentFinder finder, boolean isRecording, WindowMonitor windowMonitor) {
            super(finder, isRecording, windowMonitor);
        }

        public boolean canHandle(Component component, Point location) {
            return true;
        }

        public Component getComponent(Component component, Point location) {
            return component;
        }

        public MComponent getMComponent(Component component, String name, Object obj) {
            return new MUnknownComponent(component, name, getFinder(), windowMonitor);
        }

    }

    private JLabel label;
    private DialogForTesting dialog;

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
        System.setProperty(Constants.PROP_COMPONENT_RESOLVERS, UnknownComponentResolver.class.getName());
        label = new JLabel("this is a crock");
        label.setName("label.name");
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                System.out.println(".mousePressed()");
            }
        });
        dialog = new DialogForTesting(this.getClass().getName());
        dialog.getContentPane().add(label);
        dialog.show();
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty(Constants.PROP_COMPONENT_RESOLVERS, "");
        dialog.setVisible(false);
    }

    @Test
    public void testGetTextReturnsNull() {
        MUnknownComponent component = (MUnknownComponent) dialog.getResolver(UnknownComponentResolver.class).getMComponentById(
                new ComponentId("label.name"));
        assertEquals(null, component.getText());
    }

    @Test
    public void testSetTextThrowsException() {
        MUnknownComponent component = (MUnknownComponent) dialog.getResolver(UnknownComponentResolver.class).getMComponentById(
                new ComponentId("label.name"));
        try {
            component.setText("This should throw an exception");
            fail("Unknown component setText should throw an exception");
        } catch (UnsupportedOperationException e) {

        } catch (Exception e) {
            fail("Unknown component threw an unknown exception " + e);
        }
    }
}
