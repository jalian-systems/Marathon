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

import java.util.Properties;

import javax.swing.JTable;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.DummyResolver;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.component.MTable;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAssertColumnCount {
    private static final ComponentId ID = new ComponentId("text.name");

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
    public void testAssertColumnCount() {
        JTable table = new JTable(1, 3);
        MComponent component = new MTable(table, "table", null, WindowMonitor.getInstance());
        ComponentFinder resolver = new DummyResolver(component);
        ActionTestCase.assertPasses(
                new AssertColumnCount(ID, 3, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertFails(
                new AssertColumnCount(ID, 4, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testToPython() {
        assertEquals("assert_p('text.name', 'ColumnCount', '3')\n",
                new AssertColumnCount(ID, 3, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance())
                        .toScriptCode());
    }
}
