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

import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.MComponentMock;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSelect {
    private static final ComponentId FOO = new ComponentId("foo");

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
    public void testPlayInTextField() {
        MComponentMock component = new MComponentMock();
        new SelectAction(FOO, "abc", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(component
                .getDummyResolver());
        component.getHistory().assertNextMessage("setText(abc)");
        component.getHistory().assertEmpty();
    }

    @Test
    public void testEquals() throws Exception {
        ActionTestCase.testEquals(
                new SelectAction(FOO, "jira", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()),
                new SelectAction(FOO, "jira", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()),
                new SelectAction(FOO, "george", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()));
    }
}
