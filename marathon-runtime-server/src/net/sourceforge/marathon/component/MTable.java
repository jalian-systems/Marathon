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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.Retry;
import net.sourceforge.marathon.util.Retry.Attempt;

public class MTable extends MCollectionComponent {
    public MTable(JTable table, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(table, name, finder, windowMonitor);
    }

    private JTable getTable() {
        return (JTable) getComponent();
    }

    public int getRowCount() {
        return eventQueueRunner.invokeInteger(getTable(), "getRowCount");
    }

    public String[][] getContent() {
        JTable table = getTable();
        String[][] content = new String[getRowCount()][getColumnCount()];
        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[i].length; j++) {
                Object data = getValue(table, i, j);
                if (data == null)
                    data = "";
                content[i][j] = data.toString();
            }
        }
        return content;
    }

    private Object getValue(JTable table, int row, int col) {
        Object data = eventQueueRunner.invoke(table, "getValueAt", new Object[] { Integer.valueOf(row), Integer.valueOf(col) },
                new Class[] { Integer.TYPE, Integer.TYPE });
        return data;
    }

    public int getColumnCount() {
        return eventQueueRunner.invokeInteger(getTable(), "getColumnCount");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.MComponent#keyNeeded(java.awt.event
     * .KeyEvent)
     * 
     * This is needed to make testSelectInATableCell to work. On regular
     * recording this code is never used :-(. Still could not figure out how to
     * fix it properly.
     */
    public boolean keyNeeded(KeyEvent e) {
        return super.keyNeeded(e, true);
    }

    public String getText() {
        JTable table = getTable();
        boolean rowSelectionAllowed = (Boolean) eventQueueRunner.invokeBoolean(table, "getRowSelectionAllowed");
        boolean columnSelectionAllowed = (Boolean) eventQueueRunner.invokeBoolean(table, "getColumnSelectionAllowed");

        if (!rowSelectionAllowed && !columnSelectionAllowed)
            return null;

        int[] rows = (int[]) eventQueueRunner.invoke(table, "getSelectedRows");
        int[] columns = (int[]) eventQueueRunner.invoke(table, "getSelectedColumns");
        int rowCount = eventQueueRunner.invokeInteger(table, "getRowCount");
        int columnCount = eventQueueRunner.invokeInteger(table, "getColumnCount");

        if (rows.length == rowCount && columns.length == columnCount)
            return "all";

        if (rows.length == 0 && columns.length == 0)
            return "";

        StringBuffer text = new StringBuffer();

        text.append("rows:[");
        for (int i = 0; i < rows.length; i++) {
            text.append(rows[i]);
            if (i != rows.length - 1)
                text.append(",");
        }
        text.append("],");
        text.append("columns:[");
        for (int i = 0; i < columns.length; i++) {
            String columnName = (String) eventQueueRunner.invoke(table, "getColumnName",
                    new Object[] { Integer.valueOf(columns[i]) }, new Class[] { Integer.TYPE });
            text.append(escape(columnName));
            if (i != columns.length - 1)
                text.append(",");
        }
        text.append("]");
        return text.toString();
    }

    private String escape(String columnName) {
        return columnName.replaceAll("#", "##").replaceAll(",", "#;");
    }

    private static class Cell {
        public int row;
        public int col;

        public Cell(int r, int c) {
            row = r;
            col = c;
        }
    }

    public void setText(String text) {
        JTable table = getTable();
        boolean cellEditing = eventQueueRunner.invokeBoolean(table, "isEditing");
        if (cellEditing)
            return;
        if ("".equals(text)) {
            eventQueueRunner.invoke(table, "clearSelection");
            swingWait();
            return;
        }
        int[] rows;
        int[] cols;
        if ("all".equals(text)) {
            int rowCount = eventQueueRunner.invokeInteger(table, "getRowCount");
            int columnCount = eventQueueRunner.invokeInteger(table, "getColumnCount");
            rows = new int[rowCount];
            cols = new int[columnCount];
            for (int i = 0; i < rowCount; i++)
                rows[i] = i;
            for (int i = 0; i < columnCount; i++)
                cols[i] = i;
        } else {
            rows = parseRows(text);
            String[] colNames = parseCols(text);
            cols = new int[colNames.length];
            for (int i = 0; i < colNames.length; i++) {
                cols[i] = getColumnIndex(colNames[i]);
            }
        }

        selectRowsColumns(table, rows, cols);
    }

    public void setCellSelection(Properties[] properties) {
        Set<Integer> irows = new HashSet<Integer>();
        Set<Integer> icols = new HashSet<Integer>();
        for (Properties props : properties) {
            MTableCell cell = (MTableCell) findMatchingComponent(props);
            if (cell == null) {
                throw new ComponentException("Could not find matching table cell for given properties: " + props,
                        finder.getScriptModel(), windowMonitor);
            }
            irows.add(cell.getRow());
            icols.add(cell.getColumnIndex());
        }
        int[] rows = new int[irows.size()];
        int i = 0;
        for (Integer integer : irows) {
            rows[i++] = integer;
        }
        int[] cols = new int[icols.size()];
        i = 0;
        for (Integer integer : icols) {
            cols[i++] = integer;
        }
        selectRowsColumns(getTable(), rows, cols);
    }

    private void selectRowsColumns(final JTable table, int[] rows, int[] cols) {
        boolean rowSelectionAllowed = table.getRowSelectionAllowed();
        boolean columnSelectionAllowed = table.getColumnSelectionAllowed();

        List<Cell> cells = new ArrayList<MTable.Cell>();

        if (rowSelectionAllowed && columnSelectionAllowed) {
            for (int i = 0; i < cols.length; i++)
                cells.add(new Cell(rows[0], cols[i]));
            for (int i = 1; i < rows.length; i++)
                cells.add(new Cell(rows[i], cols[0]));
        } else if (rowSelectionAllowed) {
            for (int i = 0; i < rows.length; i++)
                cells.add(new Cell(rows[i], cols[0]));
        } else if (columnSelectionAllowed) {
            for (int i = 0; i < cols.length; i++)
                cells.add(new Cell(rows[0], cols[i]));
        } else {
        }
        final int maxRow = findMaxRow(cells);
        new Retry("Could not find row " + maxRow + " in table", ComponentFinder.RETRY_INTERVAL_MS,
                ComponentFinder.COMPONENT_SEARCH_RETRY_COUNT, new Attempt() {
                    @Override public void perform() {
                        int rowCount = eventQueueRunner.invokeInteger(table, "getRowCount");
                        if(maxRow >= rowCount)
                            retry();
                    }
                });
        for (Cell c : cells) {
            TableCellEditor cellEditor = (TableCellEditor) eventQueueRunner.invoke(table, "getCellEditor",
                    new Object[] { Integer.valueOf(c.row), Integer.valueOf(c.col) }, new Class[] { Integer.TYPE, Integer.TYPE });
            if (cellEditor instanceof DefaultCellEditor) {
                Component ec = ((DefaultCellEditor) cellEditor).getComponent();
                if (ec instanceof JCheckBox) {
                    createClick(c.row, c.col);
                }
            }
        }
        eventQueueRunner.invoke(table, "clearSelection");
        swingWait();
        for (Cell c : cells) {
            System.out.println("Creating click for " + c.row + " " + c.col + " table rows = " + table.getRowCount());
            createClick(c.row, c.col, OSUtils.MOUSE_MENU_MASK);
        }
        swingWait();
    }

    private int findMaxRow(List<Cell> cells) {
        int max = -1;
        for (Cell cell : cells) {
            if (cell.row > max)
                max = cell.row;
        }
        return max;
    }

    private int getColumnIndex(String columnName) {
        JTable table = getTable();
        int ncolumns = eventQueueRunner.invokeInteger(table, "getColumnCount");
        for (int i = 0; i < ncolumns; i++) {
            String column = (String) eventQueueRunner.invoke(table, "getColumnName", new Object[] { Integer.valueOf(i) },
                    new Class[] { Integer.TYPE });
            if (columnName.equals(escape(column)))
                return i;
        }
        throw new RuntimeException("Could not find column " + columnName + " in table " + getMComponentName());
    }

    private void createClick(int row, int column, int modifiers) {
        JTable table = getTable();
        FireableMouseClickEvent event = new FireableMouseClickEvent(table);
        Rectangle r = table.getCellRect(row, column, false);
        Point p = new Point((int) r.getCenterX(), (int) r.getCenterY());
        event.fire(p, 1, modifiers);
        swingWait();
    }

    private void createClick(int row, int column) {
        createClick(row, column, 0);
    }

    private class MTableCellIterator implements Iterator<MComponent> {
        private int totalRows = getRowCount();
        private int totalColumns = getColumnCount();
        private int currentRow = 0;
        private int currentCol = 0;

        public boolean hasNext() {
            return currentRow < totalRows && currentCol < totalColumns;
        }

        public MComponent next() {
            MTableCell tableCell = new MTableCell(getTable(), getMComponentName(), currentRow, currentCol, finder, windowMonitor);
            currentCol++;
            if (currentCol == totalColumns) {
                currentCol = 0;
                currentRow++;
            }
            return tableCell;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove on CollectionComponent is not supported");
        }

    }

    public Iterator<MComponent> iterator() {
        return new MTableCellIterator();
    }

    public boolean isMComponentEditable() {
        return false;
    }

    private int[] parseRows(String s) {
        String rowText = "";
        int i = s.indexOf("rows:");
        if (i != -1) {
            int j = s.indexOf("columns:");
            if (j == -1)
                rowText = s.substring(i + 5);
            else
                rowText = s.substring(i + 5, j);
            int k = rowText.indexOf('[');
            int l = rowText.indexOf(']');
            rowText = rowText.substring(k + 1, l);
        }
        StringTokenizer tokenizer = new StringTokenizer(rowText, ", ");
        List<String> rows = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) {
            rows.add(tokenizer.nextToken());
        }
        int irows[] = new int[rows.size()];
        for (int j = 0; j < irows.length; j++) {
            try {
                irows[j] = Integer.parseInt(rows.get(j));
            } catch (Throwable t) {
                return new int[0];
            }
        }
        return irows;
    }

    private String[] parseCols(String s) {
        String colText = "";
        int i = s.indexOf("columns:");
        if (i != -1) {
            colText = s.substring(i + 8);
            int k = colText.indexOf('[');
            int l = colText.indexOf(']');
            colText = colText.substring(k + 1, l);
        }
        List<String> cols = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(colText, ",");
        while (tokenizer.hasMoreElements()) {
            cols.add(tokenizer.nextToken());
        }
        return cols.toArray(new String[cols.size()]);
    }

}
