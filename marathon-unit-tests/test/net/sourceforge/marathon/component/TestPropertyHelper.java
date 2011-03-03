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

import java.util.Properties;

import org.junit.Test;

public class TestPropertyHelper {

    @Test
    public void testToStringSingleProperty() throws Exception {
        Properties p = new Properties();
        p.setProperty("Index", "1");
        assertEquals("1", PropertyHelper.toString(p, new String[] { "Index" }));
        p.clear();
        p.setProperty("Text", "Some Text");
        assertEquals("Some Text", PropertyHelper.toString(p, new String[] { "Text" }));
        p.clear();
        p.setProperty("Text", "Some Text With Escapable {},: Characters");
        assertEquals("Some Text With Escapable \\{\\}\\,\\: Characters", PropertyHelper.toString(p, new String[] { "Text" }));
    }

    @Test
    public void testToStringMultipleProperties() throws Exception {
        Properties p = new Properties();
        p.setProperty("Index", "1");
        p.setProperty("Text", "Some Text Goes Here");
        p.setProperty("Text2", "Some More Text Here");
        assertEquals("{1, Some Text Goes Here, Some More Text Here}",
                PropertyHelper.toString(p, new String[] { "Index", "Text", "Text2" }));
    }

    @Test
    public void testToStringArrayMultiProperties() throws Exception {
        Properties[] pa = new Properties[3];
        for (int i = 0; i < pa.length; i++) {
            pa[i] = setProps(i + 1);
        }
        assertEquals("[{1, Some Text Goes Here 1, Some More Text Here 1}, {2, " + "Some Text Goes Here 2, Some More Text Here 2}, "
                + "{3, Some Text Goes Here 3, Some More Text Here 3}]",
                PropertyHelper.toString(pa, new String[] { "Index", "Text", "Text2" }));
    }

    private Properties setProps(int i) {
        Properties p = new Properties();
        p.setProperty("Index", i + "");
        p.setProperty("Text", "Some Text Goes Here " + i);
        p.setProperty("Text2", "Some More Text Here " + i);
        return p;
    }

    @Test
    public void testFromStringSingleProperty() throws Exception {
        Properties p;
        p = PropertyHelper.fromString("  Some\\, Text  ", new String[][] { new String[] { "Text" } });
        assertEquals("Some, Text", p.getProperty("Text"));
        p = PropertyHelper.fromString("{Index: 1, Text: Some Text}", new String[][] { new String[] { "Text" },
                new String[] { "Index", "Text" } });
        assertEquals("1", p.getProperty("Index"));
        assertEquals("Some Text", p.getProperty("Text"));
    }

    @Test
    public void testMultipleProperties() throws Exception {
        Properties p;
        p = PropertyHelper.fromString("{1, Some Text}",
                new String[][] { new String[] { "Text" }, new String[] { "Index", "Text" } });
        assertEquals("1", p.getProperty("Index"));
        assertEquals("Some Text", p.getProperty("Text"));
    }

    @Test
    public void testMultiplePropertiesWithKeyValuePairs() throws Exception {
        Properties p;
        p = PropertyHelper.fromString("{Index: 1, Text: Some Text}", new String[][] { new String[] { "Text" },
                new String[] { "Index", "Text" } });
        assertEquals("1", p.getProperty("Index"));
        assertEquals("Some Text", p.getProperty("Text"));
    }

    @Test
    public void testMultiplePropertiesWithKeyValuePairsAndSpaces() throws Exception {
        Properties p;
        p = PropertyHelper.fromString("{Index: index (1), Text: Some Text}", new String[][] { new String[] { "Text" },
                new String[] { "Index", "Text" } });
        assertEquals("index (1)", p.getProperty("Index"));
        assertEquals("Some Text", p.getProperty("Text"));
        p = PropertyHelper.fromString("{title: Document (1)}", new String[][] { new String[] { "Text" },
                new String[] { "Index", "Text" } });
        assertEquals("Document (1)", p.getProperty("title"));
    }

    @Test
    public void testArrayOfPropertiesFromString() throws Exception {
        Properties[] expected = new Properties[3];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = setProps(i + 1);
        }
        Properties[] actual = PropertyHelper.fromStringToArray("[{1, Some Text Goes Here 1, Some More Text Here 1}, {2, "
                + "Some Text Goes Here 2, Some More Text Here 2}, " + "{3, Some Text Goes Here 3, Some More Text Here 3}]",
                new String[][] { new String[] { "Text" }, new String[] { "Index", "Text", "Text2" } });
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getProperty("Index"), actual[i].getProperty("Index"));
            assertEquals(expected[i].getProperty("Text"), actual[i].getProperty("Text"));
        }
    }

    @Test
    public void testArrayOfPropertiesFromString2() throws Exception {
        Properties[] actual = PropertyHelper.fromStringToArray("[Sports, Colors]", new String[][] { new String[] { "Text" },
                new String[] { "Index", "T\\ext" } });
        assertEquals(2, actual.length);
        assertEquals(1, actual[0].size());
        assertEquals(1, actual[1].size());
        assertEquals("Sports", actual[0].getProperty("Text"));
        assertEquals("Colors", actual[1].getProperty("Text"));
    }
    
    @Test
    public void testSpecialCharacters() throws Exception {
        Properties p = new Properties();
        p.put("Path", "/root/comma, within a string");
        String pstring = PropertyHelper.toString(new Properties[] { p }, new String[] { "Path" });
        assertEquals("[/root/comma\\, within a string]", pstring);
        Properties[] p2 = PropertyHelper.fromStringToArray(pstring, new String[][] { new String[] { "Path" } });
        assertEquals(p.getProperty("Path"), p2[0].getProperty("Path"));
    }
    
}
