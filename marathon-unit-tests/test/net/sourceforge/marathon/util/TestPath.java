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
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

public class TestPath {
    private static final String PS = File.pathSeparator;

    @Test
    public void testOnePath() throws Exception {
        String path = "path" + File.separator + "to" + File.separator + "file";
        Path p = new Path(path);
        File[] elements = p.elements();
        assertEquals("number of paths", 1, elements.length);
        assertEquals(path, elements[0].getPath());
    }

    @Test
    public void testMultiplePaths() throws Exception {
        String path1 = "path" + File.separator + "to" + File.separator + "afile";
        String path2 = "another" + File.separator + "pathto" + File.separator + "afile";
        String fullPath = path1 + PS + path2;
        Path p = new Path(fullPath);
        File[] elements = p.elements();
        assertEquals("number of path elements", 2, elements.length);
        assertEquals(path1, elements[0].getPath());
        assertEquals(path2, elements[1].getPath());
    }

    @Test
    public void testNullPath() throws Exception {
        Path p = new Path(null);
        assertEquals("number of path elements", 0, p.elements().length);
    }

    @Test
    public void testEmptyPath() throws Exception {
        Path p = new Path("  ");
        assertEquals("number of path elements in empty path", 0, p.elements().length);
    }

    @Test
    public void testMultipleEmptyPaths() throws Exception {
        Path p = new Path("one" + PS + "  " + PS + "two" + PS + PS + PS + "three");
        File[] paths = p.elements();
        assertEquals("number of real path elements", 3, paths.length);
        assertEquals("one", paths[0].getPath());
        assertEquals("two", paths[1].getPath());
        assertEquals("three", paths[2].getPath());
    }

    @Test
    public void testCreatesEmpytPathStringWhenNoElements() throws Exception {
        Path p = new Path("");
        assertEquals("", p.toString());
    }

    @Test
    public void testPathStringContainsAllPathElements() throws Exception {
        Path p = new Path("dude" + File.pathSeparator + "bag" + File.pathSeparator + "spawn");
        assertEquals("dude" + File.pathSeparator + "bag" + File.pathSeparator + "spawn", p.toString());
    }

    @Test
    public void testDoesNotAllowAddingANewPathElementContainingThePathSeperator() throws Exception {
        Path p = new Path("");
        try {
            p.addElement("dude" + File.pathSeparator + "bag");
            fail("should not be able to add a new path element wit the file seperator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddingNullPathElementsHasNoEffect() throws Exception {
        Path p = new Path("dude");
        p.addElement(null);
        assertEquals("added a null path element, should ignore", 1, p.elements().length);
    }

    @Test
    public void testAddingEmptyPathElementHasNoEffect() throws Exception {
        Path p = new Path("dude");
        p.addElement("");
        assertEquals("added an empty path element, should ignore", 1, p.elements().length);
    }

    @Test
    public void testAddingWhiteSpaceOnlyPathElementHasNoEffect() throws Exception {
        Path p = new Path("dude");
        p.addElement("	\n ");
        assertEquals("added an empty path element, should ignore", 1, p.elements().length);
    }
}
