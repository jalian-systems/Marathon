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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JTable;
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

public class TestTablePerformance {
    private DialogForTesting dialog;
    private JTable table;
    private Object[][] tableData;
    @SuppressWarnings("unused") private String record;
    private ComponentFinder finder;

    @BeforeClass public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    @AfterClass public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Before public void setUp() throws Exception {
        dialog = new DialogForTesting(this.getClass().getName());
        dialog.addButton("button.name", "I dare you");
        tableData = new Object[][] { { "Value1A", "Value1B", "Value1C", Boolean.FALSE },
                { "Value2A", "Value2B", "Value2C", Boolean.TRUE }, };
        tableData = new Object[88000][4];
        for (int i = 1; i <= 88000; i++) {
            Object[] td = tableData[i - 1];
            td[0] = "Value" + i + "A";
            td[1] = "Value" + i + "B";
            td[2] = "Value" + i + "C";
            td[3] = Boolean.valueOf(i % 2 == 0);
        }
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
        finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
    }

    @After public void tearDown() throws Exception {
        table = null;
        dialog.dispose();
        dialog = null;
        finder = null;
        tableData = null;
    }

    @Test public void testGetText() throws Exception {
        for (int i = 40000; i <= 41000; i++) {
            MTableCell cell_x_1 = tableCell("{" + i + ",Column3}");
            assertEquals("Value" + (i+1) + "C", cell_x_1.getText());
        }
    }

    @Test public void testSetText() throws Exception {
        MTableCell cell_1_1 = tableCell("{1,Col\\,two\\,2}");
        cell_1_1.setText("cell_1_1 new value");
        assertEquals("cell_1_1 new value", cell_1_1.getText());
    }

    private MTableCell tableCell(String cellSpec) {
        return new MTableCell(table, "table.name", cellSpec, finder, WindowMonitor.getInstance());
    }
}
