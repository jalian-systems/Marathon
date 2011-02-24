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
import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.Properties;

import javax.swing.JTextField;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.component.MComponentMock;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAssertActionUtil {

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

    @Test
    public void testAssertingEnabled() {
        JTextField field = new JTextField();
        field.setBackground(Color.red);
        MComponent component = new MComponentMock(field, "foo");
        ComponentFinder resolver = ((MComponentMock) component).getDummyResolver();
        field.setEnabled(true);
        ActionTestCase.assertPasses(
                new AssertAction(new ComponentId("text.name"), AssertAction.ENABLED, ScriptModelServerPart.getModelServerPart(),
                        WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertFails(
                new AssertAction(new ComponentId("text.name"), AssertAction.DISABLED, ScriptModelServerPart.getModelServerPart(),
                        WindowMonitor.getInstance()), resolver);
        field.setEnabled(false);
        ActionTestCase.assertFails(
                new AssertAction(new ComponentId("text.name"), AssertAction.ENABLED, ScriptModelServerPart.getModelServerPart(),
                        WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertPasses(
                new AssertAction(new ComponentId("text.name"), AssertAction.DISABLED, ScriptModelServerPart.getModelServerPart(),
                        WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testCheckingColor() {
        JTextField field = new JTextField();
        field.setBackground(Color.red);
        MComponent component = new MComponentMock(field, "foo");
        AssertAction tag = new AssertAction(new ComponentId("foo"), Color.red, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance());
        tag.play(((MComponentMock) component).getDummyResolver());
        tag = new AssertAction(new ComponentId("foo"), Color.white, ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance());
        try {
            tag.play(((MComponentMock) component).getDummyResolver());
            fail("should throw a test exception");
        } catch (TestException e) {
            // this is expected
        }
        assertEquals("assert_p('foo', 'Background', '[r=255,g=255,b=255]')\n", tag.toScriptCode());
    }
}
