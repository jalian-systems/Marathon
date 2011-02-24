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

import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.JButton;
import javax.swing.JMenu;

public class TestMMenu {
    private MMenu mmenu;

    @Before
    public void setUp() throws Exception {
        JMenu menu = new JMenu("File");
        menu.add("New");
        menu.add(new JButton("Open"));
        menu.addSeparator();
        JMenu child = new JMenu("Recent Files");
        child.add("MMenu.java");
        JMenu grandChild = new JMenu("Edit Box");
        grandChild.add("Edit...");
        child.add(grandChild);
        menu.add(child);
        mmenu = new MMenu(menu, "menu.name", null, WindowMonitor.getInstance());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetRowCount() {
        assertEquals("Number of chidren do not match", 6, mmenu.getRowCount());
    }

    @Test
    public void testGetContent() {
        String[][] expected = { { "Enabled,New", "Enabled,Open", "Enabled,Recent Files", "Enabled,MMenu.java", "Enabled,Edit Box",
                "Enabled,Edit..." } };
        String[][] actual = mmenu.getContent();
        assertEquals("Length do not match", expected[0].length, actual[0].length);
        for (int i = 0; i < actual[0].length; i++) {
            assertEquals("Item at " + i + " does not match", expected[0][i], actual[0][i]);
        }
    }

    @Test
    public void testEnabledDisabled() {
        String[][] expected = { { "Enabled,New", "Disabled,Open", "Enabled,Recent Files", "Enabled,MMenu.java", "Enabled,Edit Box",
                "Enabled,Edit..." } };
        mmenu.getJMenu().getMenuComponent(1).setEnabled(false);
        String[][] actual = mmenu.getContent();
        assertEquals("Length do not match", expected[0].length, actual[0].length);
        for (int i = 0; i < actual[0].length; i++) {
            assertEquals("Item at " + i + " does not match", expected[0][i], actual[0][i]);
        }
    }
}
