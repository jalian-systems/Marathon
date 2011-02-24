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
package net.sourceforge.marathon.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestPythonEscape {
    /**
     * check the single line quote string '' is used for single line strings. If
     * there are any \ characters inside the string, then we want to make sure
     * that it is interpreted literally, so we prepend an 'r' to the front.
     */
    @Test
    public void testSingleLineString() throws Exception {
        assertEquals("single line", "'this is a string'", PythonEscape.encode("this is a string"));
        assertEquals("single line with literals", "r'this\\s is\\w a \\nstring'", PythonEscape.encode("this\\s is\\w a \\nstring"));
        assertEquals("single line with single quotes", "'this \\'quoted\\' string'", PythonEscape.encode("this 'quoted' string"));
    }

    /**
     * Multiline strings are represented in python with triple quotes this
     * allows the string literal itself to actually span multiple lines. check
     * that these are used for multi line strings, and that if it contains any \
     * characters, that it is denoted as a raw python string ('r' as prefix)
     */
    @Test
    public void testMultiLineString() throws Exception {
        assertEquals("multiple lines", "'''this\nhas\nmany\nlines'''", PythonEscape.encode("this\nhas\nmany\nlines"));
        assertEquals("many lines, with literals", "r'''\\special\n\\multi\n\\line'''",
                PythonEscape.encode("\\special\n\\multi\n\\line"));
        assertEquals("multiple lines", "'''this\nhas\nmany\nlines with a \\'quoted\\' string'''",
                PythonEscape.encode("this\nhas\nmany\nlines with a 'quoted' string"));
    }
}
