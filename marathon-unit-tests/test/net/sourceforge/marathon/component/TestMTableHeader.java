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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class TestMTableHeader {
    private DialogForTesting dialog;
    private JTableHeader header;
    private String result;

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
    public void setUp() {
        result = new String();
        dialog = new DialogForTesting("testdialog");
        dialog.addTable();
        header = dialog.getTable().getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                int ci = header.columnAtPoint(p);
                if (ci >= 0) {
                    TableColumn column = header.getColumnModel().getColumn(ci);
                    result += column.getHeaderValue().toString();
                }
            }
        });
        dialog.show();
    }

    @After
    public void tearDown() {
        dialog.dispose();
    }

    @Test
    public void testTableHeaderExists() throws Exception {
        TableColumnModel model = new DefaultTableColumnModel();
        TableColumn column = new TableColumn(0);
        column.setIdentifier("header_name");
        model.addColumn(column);
        JTableHeader tableHeader = new JTableHeader(model);
        MTableHeaderItem header = new MTableHeaderItem(tableHeader, "table_name", "header_name", null, WindowMonitor.getInstance());
        assertEquals("header_name", header.getText());
    }

    @Test
    public void testClick() {
        MTableHeaderItem mheader = new MTableHeaderItem(dialog.getTable().getTableHeader(), "table.name.header", "col2", null,
                WindowMonitor.getInstance());
        mheader.click(1, false);
        assertEquals(result, "col2");
    }
}
