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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMTableCell {
    private DialogForTesting dialog;
    private JTable table;
    private Object[][] tableData;
    private String record;
    private ComponentFinder finder;

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
        dialog.addButton("button.name", "I dare you");
        tableData = new Object[][] { { "Value1A", "Value1B", "Value1C", Boolean.FALSE },
                { "Value2A", "Value2B", "Value2C", Boolean.TRUE }, };
        String[] columnNames = new String[] { "Column1", "Col,two,2", "Column3", "Column4" };
        dialog.addTable("table", true, tableData, columnNames);
        table = dialog.getTable();
        table.setModel(new DefaultTableModel(tableData, columnNames) {
            private static final long serialVersionUID = 1L;

            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3)
                    return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        });
        // setup listener so that we know it's been clicked
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getPoint() == null || table == null)
                    return;
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                record += "clicked " + e.getClickCount() + " times on table" + "[row=" + row + ",column=" + column + "]";
            }
        });
        record = "";
        dialog.pack();
        dialog.show();
        finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(),
                new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        table = null;
        dialog.dispose();
        dialog = null;
        finder = null;
        tableData = null;
    }

    @Test
    public void testConstructMTableCellWithPoint() throws Exception {
        Point cellLocation = table.getCellRect(1, 1, false).getLocation();
        assertEquals(tableCell("Col,two,2,1"),
                new MTableCell(table, table.getName(), cellLocation, null, WindowMonitor.getInstance()));
    }

    @Test
    public void testConstructMTableCellWithInvalidPoint() throws Exception {
        Rectangle bounds = table.getBounds();
        Point notInTable = new Point((int) bounds.getWidth() + 10, (int) bounds.getHeight() + 10);
        try {
            new MTableCell(table, table.getName(), notInTable, dialog.getResolver(), WindowMonitor.getInstance());
            fail("Should throw an exception with an invalid point");
        } catch (ComponentException e) {
        }
    }

    @Test
    public void testGetText() throws Exception {
        MTableCell cell_2_1 = tableCell("Column3,1");
        assertEquals("Value2C", cell_2_1.getText());
        MTableCell cell_0_0 = tableCell("Column1,0");
        assertEquals("Value1A", cell_0_0.getText());
    }

    @Test
    public void testGetTextWithNullValues() throws Exception {
        table.getModel().setValueAt(null, 1, 2);
        MTableCell cell_2_1 = tableCell("Column3,1");
        assertEquals("", cell_2_1.getText());
    }

    @Test
    public void testSetText() throws Exception {
        MTableCell cell_1_1 = tableCell("Col,two,2,1");
        cell_1_1.setText("cell_1_1 new value");
        assertEquals("cell_1_1 new value", cell_1_1.getText());
    }

    @Test
    public void testSetTextOnBooleanCell() throws Exception {
        MTableCell cell_3_1 = tableCell("Column4,1");
        cell_3_1.getEditor().getComponent().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("MTableCellTest.testSetTextOnBooleanCell()");
            }
        });
        cell_3_1.setText("true");
        assertTrue(cell_3_1.getText().startsWith("true"));
    }

    @Test
    public void testGoingEditable() {
        MTableCell cell_1_1 = tableCell("Col,two,2,1");
        assertTrue(!table.isCellSelected(1, 1));
        cell_1_1.click(1, false);
        assertTrue(table.isCellSelected(1, 1));
        assertTrue(table.isCellEditable(1, 1));
        assertTrue(!table.isEditing());
        cell_1_1.click(2, false);
        assertTrue(table.isEditing());
    }

    @Test
    public void testIntegerColumnIndex() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        assertEquals("Value2C", cell_2_1.getText());
        MTableCell cell_0_0 = tableCell("0,0");
        assertEquals("Value1A", cell_0_0.getText());
    }

    @Test
    public void testEquals() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        MTableCell cell_2_1_again = tableCell("2,1");
        MTableCell cell_0_0 = tableCell("0,0");
        assertEquals(cell_2_1, cell_2_1_again);
        assertEquals(cell_0_0, cell_0_0);
        assertTrue(!cell_0_0.equals(cell_2_1));
    }

    @Test
    public void testOldGetComponentInfo() throws Exception {
        MTableCell cell_2_1 = tableCell("Column3,1");
        assertEquals("{1, Column3}", cell_2_1.getComponentInfo());
    }

    @Test
    public void testGetComponentInfo() throws Exception {
        MTableCell cell_2_1 = tableCell("{1, Column3}");
        assertEquals("{1, Column3}", cell_2_1.getComponentInfo());
    }

    @Test
    public void testClick() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        cell_2_1.click(1);
        assertEquals("clicked 1 times on table[row=1,column=2]", record);
    }

    @Test
    public void testDoubleClick() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        cell_2_1.click(2, false);
        assertEquals("clicked 1 times on table[row=1,column=2]clicked 2 times on table[row=1,column=2]", record);
    }

    @Test
    public void testGetTextWhenEditing() {
        MTableCell cell_2_1 = tableCell("2,1");
        cell_2_1.click(2, false);
        assertTrue(table.isEditing());
        JTextField editorComponent = (JTextField) table.getCellEditor().getTableCellEditorComponent(table, "yellow", true, 1, 2);
        MTextComponent editorMComponent = new MTextComponent(editorComponent, "foo", null, WindowMonitor.getInstance());
        assertEquals("yellow", editorMComponent.getText());
        assertEquals("yellow", cell_2_1.getText());
    }

    @Test
    public void testIsBeingEdited() {
        MTableCell cell_2_1 = tableCell("2,1");
        assertTrue(!cell_2_1.isBeingEdited());
        cell_2_1.click(2, false);
        assertTrue(cell_2_1.isBeingEdited());
    }

    @Test
    public void testGetTextNotClickingOnRenderer() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        cell_2_1.getText();
        assertEquals("", record);
    }

    @Test
    public void testGetTextNotClickingOnEditor() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        cell_2_1.click(2, false);
        record = "";
        cell_2_1.getText();
        assertEquals("", record);
    }

    @Test
    public void testGetTextOnSecondCellNotClickingWhenEditingOne() throws Exception {
        MTableCell cell_2_1 = tableCell("2,1");
        cell_2_1.click(2, false);
        record = "";
        MTableCell cell_1_1 = tableCell("1,1");
        cell_1_1.getText();
        assertEquals("", record);
    }

    private MTableCell tableCell(String cellSpec) {
        return new MTableCell(table, "table.name", cellSpec, finder, WindowMonitor.getInstance());
    }
}
