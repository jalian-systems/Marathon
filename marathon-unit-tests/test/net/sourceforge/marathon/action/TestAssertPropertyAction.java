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

import java.awt.Color;
import java.util.Properties;

import javax.swing.JTextField;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponentMock;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAssertPropertyAction {
    JTextField field = new JTextField();
    MComponentMock component = new MComponentMock(field, "text.name");
    ComponentFinder resolver = component.getDummyResolver();

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
        field.setBackground(Color.red);
    }

    @Test
    public void testBooleanProperties() {
        field.setEnabled(true);
        ActionTestCase.assertPasses(
                new AssertPropertyAction(new ComponentId("text.name"), "Enabled", "true", ScriptModelServerPart
                        .getModelServerPart(), WindowMonitor.getInstance()), resolver);
        field.setEnabled(false);
        ActionTestCase.assertPasses(new AssertPropertyAction(new ComponentId("text.name"), "Enabled", "false",
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
        field.setEditable(true);
        ActionTestCase.assertPasses(new AssertPropertyAction(new ComponentId("text.name"), "Editable", "true",
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
        field.setEditable(false);
        ActionTestCase.assertPasses(new AssertPropertyAction(new ComponentId("text.name"), "Editable", "false",
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testObjectProperties() {
        ActionTestCase.assertPasses(new AssertPropertyAction(new ComponentId("text.name"), "AlignmentX", "0.5",
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
        field.setSize(15, 10);
        ActionTestCase.assertPasses(new AssertPropertyAction(new ComponentId("text.name"), "Component.Size",
                "[width=15,height=10]", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
    }
}
