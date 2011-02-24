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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;

import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDefaultComponentResolver {
    private DefaultComponentResolver resolver;
    private JColorChooser chooser = new JColorChooser();
    private JFileChooser fileChooser = new JFileChooser();

    @Before
    public void setUp() throws Exception {
        resolver = new DefaultComponentResolver(null, false, WindowMonitor.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        resolver = null;
    }

    @Test
    public void testCanHandle() {
        assertTrue(resolver.canHandle(chooser));
        assertTrue(resolver.canHandle(fileChooser));
    }

    @Test
    public void testAssertForChildrenReturnsTrueForJColorChooserComponents() {
        assertForJCCChildren(chooser);
    }

    @Test
    public void testAssertForChildrenReturnsTrueForJFileChooserComponents() {
        assertForJFCChildren(fileChooser);
    }

    private void assertForJCCChildren(Container chooser) {
        Component[] components = chooser.getComponents();
        for (int i = 0; i < components.length; i++) {
            assertTrue(resolver.canHandle(components[i]));
            assertTrue(resolver.getComponent(components[i], null) instanceof JColorChooser);
            if (components[i] instanceof Container)
                assertForJCCChildren((Container) components[i]);
        }
    }

    private void assertForJFCChildren(Container chooser) {
        Component[] components = chooser.getComponents();
        for (int i = 0; i < components.length; i++) {
            assertTrue(resolver.canHandle(components[i]));
            assertTrue(resolver.getComponent(components[i], null) instanceof JFileChooser);
            if (components[i] instanceof Container)
                assertForJFCChildren((Container) components[i]);
        }
    }

    @Test
    public void testGetComponent() {
        assertTrue(resolver.getComponent(chooser, null) instanceof JColorChooser);
        assertEquals(fileChooser, resolver.getComponent(fileChooser, null));
    }

    @Test
    public void testGetMComponent() {
        assertTrue(resolver.getMComponent(chooser, getName(), getClass()) instanceof MColorChooser);
        assertTrue(resolver.getMComponent(fileChooser, getName(), getClass()) instanceof MFileChooser);
    }

    private String getName() {
        return getClass().getName();
    }

}
