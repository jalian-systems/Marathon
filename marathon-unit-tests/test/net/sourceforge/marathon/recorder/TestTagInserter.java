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

import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.Indent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTagInserter {
    private TagInserter inserter;

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
        inserter = new TagInserter();
    }

    @Test
    public void testNormalInserting() {
        inserter.add(new WindowActionMock("foo"), new RecordableMock("button1"));
        inserter.add(new WindowActionMock("bar"), new RecordableMock("button2"));
        String i1 = Indent.getIndent();
        String i2 = i1 + i1;
        assertEquals(i1 + "if window('foo'):\n" + i2 + "mock('button1')\n" + i1 + "close()\n" + "\n" + i1 + "if window('bar'):\n"
                + i2 + "mock('button2')\n" + i1 + "close()\n", inserter.getRootTag().toScriptCode());
    }

}
