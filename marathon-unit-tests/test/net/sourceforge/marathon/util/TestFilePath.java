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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class TestFilePath {
    private static final String FS = File.separator;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetRelativeWhenBaseIsShorter() throws Exception {
        FilePath path = new FilePath(FS + "home" + FS + "marathon");
        assertEquals("workspace", path.getRelative(FS + "home" + FS + "marathon" + FS + "workspace"));
    }

    @Test
    public void testGetRelativeWhenBaseIsShorter2() throws Exception {
        FilePath path = new FilePath(FS + "home" + FS + "marathon");
        assertEquals("current" + FS + "workspace",
                path.getRelative(FS + "home" + FS + "marathon" + FS + "current" + FS + "workspace"));
    }

    @Test
    public void testGetRelativeWhenBaseIsLonger() throws Exception {
        FilePath path = new FilePath(FS + "home" + FS + "marathon" + FS + "workspace");
        assertEquals("..", path.getRelative(FS + "home" + FS + "marathon"));
    }

    @Test
    public void testGetRelativeWhenBaseIsLonger2() throws Exception {
        FilePath path = new FilePath(FS + "home" + FS + "marathon" + FS + "workspace");
        assertEquals(".." + FS + "..", path.getRelative(FS + "home"));
    }

    @Test
    public void testGetRelativeWhenFilesCantHaveASingleRootOnWindows() throws Exception {
        String dir = FS + "Documents and Settings";
        FilePath path = new FilePath("C:" + dir + FS + "Marathon");
        assertEquals("D:" + dir, path.getRelative("D:" + dir));
    }

    @Test
    public void testGetRelativeWhenEqual() throws Exception {
        String dir = FS + "home" + FS + "marathon" + FS + "workspace";
        FilePath path = new FilePath(dir);
        assertEquals(".", path.getRelative(dir));
    }

    @Test
    public void testGetRelativeWhenPathsForked() throws Exception {
        String base = FS + "home" + FS + "marathon" + FS + "current" + FS + "workspace";
        String current = FS + "home" + FS + "marathon" + FS + "previous" + FS + "workspace";
        FilePath path = new FilePath(base);
        assertEquals(".." + FS + ".." + FS + "previous" + FS + "workspace", path.getRelative(current));
    }

    @Test
    public void testGetRelativeWhenPathsForked2() throws Exception {
        String base = FS + "home" + FS + "marathon" + FS + "current" + FS + "workspace";
        String current = FS + "home" + FS + "marathon" + FS + "current" + FS + "workspace2";
        FilePath path = new FilePath(base);
        assertEquals(".." + FS + "workspace2", path.getRelative(current));
    }

    @Test
    public void testGetRelativeWhenPathDifferesFromRoot() throws Exception {
        String base = FS + "home" + FS + "marathon" + FS + "workspace";
        String current = FS + "Applications";
        FilePath path = new FilePath(base);
        assertEquals(".." + FS + ".." + FS + ".." + FS + "Applications", path.getRelative(current));
    }
}
