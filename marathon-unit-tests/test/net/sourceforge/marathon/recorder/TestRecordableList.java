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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.Indent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRecordableList {
    private RecordableMock action1;
    private RecordableMock action2;
    private RecordableList actionList;
    private int temporaryWindowIndex;

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
        temporaryWindowIndex = 0;
        action1 = mock();
        action2 = mock();
        actionList = new RecordableList();
        actionList.add(action1);
        actionList.add(action2);
    }

    @Test
    public void testSize() {
        assertEquals(2, actionList.size());
    }

    @Test
    public void testGet() {
        assertSame(action1, actionList.get(0));
    }

    @Test
    public void testEquals() throws Exception {
        RecordableList other = new RecordableList();
        assertTrue(!actionList.equals(other));
        other.add(action1);
        assertTrue(!actionList.equals(other));
        other.add(action2);
        assertEquals(actionList, other);
        assertEquals(actionList.hashCode(), other.hashCode());
    }

    @Test
    public void testLast() {
        assertSame(action2, actionList.last());
        assertNull(new RecordableList().last());
    }

    @Test
    public void testRemoveLast() {
        actionList.removeLast();
        assertEquals(1, actionList.size());
    }

    @Test
    public void testToPython() {
        assertEquals(Indent.getIndent() + "mock('action1')\n" + Indent.getIndent() + "mock('action2')\n", actionList.toScriptCode());
    }

    @Test
    public void testManyWindowsToPython() {
        WindowElement w1 = new WindowActionMock("w1");
        WindowElement w2 = new WindowActionMock("w2");
        WindowElement w3 = new WindowActionMock("w3");
        w1.add(action1);
        w2.add(action2);
        w3.add(mock());
        w2.add(w3);
        w2.add(mock());
        RecordableList list = new RecordableList();
        list.add(w1);
        list.add(w2);
        String i1 = Indent.getIndent();
        String i2 = Indent.getIndent() + Indent.getIndent();
        String i3 = Indent.getIndent() + Indent.getIndent() + Indent.getIndent();
        assertEquals(i1 + "if window('w1'):\n" + i2 + "mock('action1')\n" + i1 + "close()\n" + "\n" + i1 + "if window('w2'):\n"
                + i2 + "mock('action2')\n" + "\n" + i2 + "if window('w3'):\n" + i3 + "mock('action3')\n" + i2 + "close()\n" + "\n"
                + i2 + "mock('action4')\n" + i1 + "close()\n", list.toScriptCode());
    }

    private RecordableMock mock() {
        return new RecordableMock("action" + ++temporaryWindowIndex);
    }
}
