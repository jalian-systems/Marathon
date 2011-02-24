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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Properties;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMTable {
    DialogForTesting dialog;
    JTable table;
    MTable mTable;

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
        dialog = new DialogForTesting(this.getClass().getName());
        dialog.addTable();
        table = dialog.getTable();
        mTable = new MTable(dialog.getTable(), "foo", null, WindowMonitor.getInstance());
        dialog.show();
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
    }

    @Test
    public void testComponentResolver() throws Exception {
        MTable mTable = new MTable(dialog.getTable(), "foo", null, WindowMonitor.getInstance());
        assertEquals(2, mTable.getRowCount());
    }

    @Test
    public void testGetContent() throws Exception {
        MTable box = new MTable(dialog.getTable(), "table.name", null, WindowMonitor.getInstance());
        String[][] expected = { { "a", "b" }, { "c", "d" } };
        String[][] content = box.getContent();
        assertEquals(expected.length, content.length);
        for (int i = 0; i < expected[0].length; i++) {
            assertEquals(expected[0][i], content[0][i]);
            assertEquals(expected[1][i], content[1][i]);
        }
    }

    @Test
    public void testGetContentWithNullValues() throws Exception {
        JTable table = dialog.getTable();
        table.getModel().setValueAt(null, 1, 0);
        table.getModel().setValueAt(null, 1, 1);
        MTable box = new MTable(table, "table.name", null, WindowMonitor.getInstance());
        String[][] expected = { { "a", "b" }, { "", "" } };
        String[][] content = box.getContent();
        assertEquals(expected.length, content.length);
        for (int i = 0; i < expected[0].length; i++) {
            assertEquals(expected[0][i], content[0][i]);
            assertEquals(expected[1][i], content[1][i]);
        }
    }

    @Test
    public void testGetTextAllContent() throws Exception {
        JTable table = dialog.getTable();
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionInterval(0, 1);
        table.setColumnSelectionInterval(0, 1);
        assertEquals("all", mTable.getText());
    }

    @Test
    public void testGetTextNoSelection() throws Exception {
        JTable table = dialog.getTable();
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        assertEquals("", mTable.getText());
    }

    @Test
    public void testGetTextSingleCell() throws Exception {
        JTable table = dialog.getTable();
        dialog.show();
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        FireableMouseClickEvent event = new FireableMouseClickEvent(table);
        Rectangle r = table.getCellRect(1, 0, false);
        Point p = new Point((int) r.getCenterX(), (int) r.getCenterY());
        event.fire(p, 1);
        assertEquals("rows:[1],columns:[col1]", mTable.getText());
    }
}
