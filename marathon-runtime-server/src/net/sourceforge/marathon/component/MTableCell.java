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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.TestException;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class MTableCell extends MCellComponent {
    private static final String[][] DEFAULT_PROPERTIES = new String[][] { { "Row", "Column" } };
    private int row;
    private String column;

    public MTableCell(JTable table, String name, Object obj, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(table, name, finder, windowMonitor);
        if (obj instanceof Point) {
            lookupRowAndColumn((Point) obj);
        } else if (obj instanceof String) {
            Properties props = parseProperties((String) obj, DEFAULT_PROPERTIES);
            if (props.containsKey("Row") && props.containsKey("Column")) {
                row = Integer.parseInt(props.getProperty("Row"));
                column = props.getProperty("Column");
            } else {
                MTableCell matched = (MTableCell) getCollectionComponent().findMatchingComponent(props);
                if (matched == null)
                    throw new ComponentException("Could not find matching table cell for given properties: " + props,
                            finder.getScriptModel(), windowMonitor);
                if (matched != null) {
                    row = matched.getRow();
                    column = matched.getColumn();
                }
            }
        } else {
            setSelectedCellInfo();
        }
    }

    public MTableCell(JTable table, String name, int currentRow, int currentCol, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(table, name, finder, windowMonitor);
        this.row = currentRow;
        column = (String) eventQueueRunner.invoke(table, "getColumnName", new Object[] { Integer.valueOf(currentCol) },
                new Class[] { Integer.TYPE });
    }

    public String getColumn() {
        return column;
    }

    public String getComponentInfo() {
        return createPropertyMapString(DEFAULT_PROPERTIES[0]);
    }

    public void setSelectedCellInfo() {
        JTable table = getTableComponent();
        int selectedColumn = eventQueueRunner.invokeInteger(table, "getSelectedColumn");
        column = (String) eventQueueRunner.invoke(table, "getColumnName", new Object[] { Integer.valueOf(selectedColumn) },
                new Class[] { Integer.TYPE });
        row = eventQueueRunner.invokeInteger(table, "getSelectedRow");
    }

    private void lookupRowAndColumn(Point location) {
        // convert incoming points to the table's coordinate system so that it
        // can
        // be used to find which cell we're talking about.
        // nope, don't do that!
        row = eventQueueRunner.invokeInteger(getTableComponent(), "rowAtPoint", new Object[] { location },
                new Class[] { Point.class });
        int column = eventQueueRunner.invokeInteger(getTableComponent(), "columnAtPoint", new Object[] { location },
                new Class[] { Point.class });
        if (row < 0 || column < 0) {
            throw new ComponentException("Invalid Point for MTableCell : " + location, finder.getScriptModel(), windowMonitor);
        }
        this.column = (String) eventQueueRunner.invoke(getTableComponent(), "getColumnName",
                new Object[] { Integer.valueOf(column) }, new Class[] { Integer.TYPE });
    }

    public void click(int numberOfClicks, int modifiers, Point position) {
        if (position == null) {
            Rectangle rect = (Rectangle) eventQueueRunner.invoke(getTableComponent(), "getCellRect",
                    new Object[] { Integer.valueOf(row), Integer.valueOf(getColumnIndex()), Boolean.valueOf(false) }, new Class[] {
                            Integer.TYPE, Integer.TYPE, Boolean.TYPE });
            if (rect == null) {
                throw new ComponentException("Unable to get cellRect for " + this, finder.getScriptModel(), windowMonitor);
            }
            position = rect.getLocation();
        }
        super.click(numberOfClicks, modifiers, position);
    }

    public String getText() {
        if (isBeingEdited()) {
            MComponent editor = getEditor();
            if (editor == null)
                return "";
            return editor.getText();
        } else {
            MComponent renderer = getRenderer();
            if (renderer == null)
                return "";
            return renderer.getText();
        }
    }

    public void setText(String text) {
        MComponent editor = getEditor();
        if (editor == null) {
            return;
        }
        editor.setText(text, true);
        int col = getColumnIndex();
        eventQueueRunner.invoke(getTableComponent(), "setRowSelectionInterval",
                new Object[] { Integer.valueOf(row), Integer.valueOf(row) }, new Class[] { Integer.TYPE, Integer.TYPE });
        eventQueueRunner.invoke(getTableComponent(), "setColumnSelectionInterval",
                new Object[] { Integer.valueOf(col), Integer.valueOf(col) }, new Class[] { Integer.TYPE, Integer.TYPE });
        swingWait();
    }

    public boolean isMComponentEditable() {
        return eventQueueRunner.invokeBoolean(getTableComponent(), "isCellEditable",
                new Object[] { Integer.valueOf(row), Integer.valueOf(getColumnIndex()) },
                new Class[] { Integer.TYPE, Integer.TYPE });
    }

    protected int getColumnIndex() {
        int column = 0;
        try {
            column = Integer.parseInt(this.column);
        } catch (NumberFormatException nfe) {
            // if we cannot parse the column index value as a number, we try
            // looking up the column by name
            try {
                TableColumnModel tableColumnModel = (TableColumnModel) eventQueueRunner.invoke(getTableComponent(),
                        "getColumnModel");
                if (tableColumnModel == null)
                    throw new ComponentException("Unable to get columnModel for table '" + getMComponentName() + "'",
                            finder.getScriptModel(), windowMonitor);
                column = tableColumnModel.getColumnIndex(this.column);
            } catch (IllegalArgumentException ex) {
                column = tryGettingItFromTable();
            }
        }
        return column;
    }

    private int tryGettingItFromTable() {
        int columnCount = eventQueueRunner.invokeInteger(getTableComponent(), "getColumnCount");
        for (int i = 0; i < columnCount; i++) {
            String name = (String) eventQueueRunner.invoke(getTableComponent(), "getColumnName",
                    new Object[] { Integer.valueOf(i) }, new Class[] { Integer.TYPE });
            if (name != null && name.equals(column))
                return i;
        }
        throw new TestException("Problem looking up column with the name \"" + column + "\" in table \"" + getMComponentName()
                + "\".", finder.getScriptModel(), windowMonitor, true);
    }

    public int getRow() {
        return row;
    }

    public MComponent getRenderer() {
        int col = getColumnIndex();
        TableCellRenderer renderer = (TableCellRenderer) eventQueueRunner.invoke(getTableComponent(), "getCellRenderer",
                new Object[] { Integer.valueOf(row), Integer.valueOf(col) }, new Class[] { Integer.TYPE, Integer.TYPE });
        boolean isSelected = isSelected();
        Object object = getValue(getTableComponent(), row, col);
        Component rendererComponent = renderer.getTableCellRendererComponent(getTableComponent(), object, isSelected, isSelected,
                row, col);
        return finder.getMComponentByComponent(rendererComponent, "doesn't matter", null);
    }

    private Object getValue(JTable table, int row, int col) {
        Object data = eventQueueRunner.invoke(table, "getValueAt", new Object[] { Integer.valueOf(row), Integer.valueOf(col) },
                new Class[] { Integer.TYPE, Integer.TYPE });
        return data;
    }

    public MComponent getEditor() {
        int col = getColumnIndex();
        Component editorComponent = (Component) eventQueueRunner.invoke(getTableComponent(), "getEditorComponent");
        if (editorComponent == null) {
            eventQueueRunner.invoke(getTableComponent(), "editCellAt", new Object[] { Integer.valueOf(row), Integer.valueOf(col) },
                    new Class[] { Integer.TYPE, Integer.TYPE });
            editorComponent = (Component) eventQueueRunner.invoke(getTableComponent(), "getEditorComponent");
        }
        if (editorComponent == null) {
            System.err.println("Warning: Editor component not found for an table cell with select() call:\n" + "\tfor table: "
                    + getTable().getMComponentName() + " at row: " + row + " column: " + column);
            return null;
        }
        return finder.getMComponentByComponent(editorComponent, "doesn't matter", null);
    }

    private boolean isSelected() {
        return eventQueueRunner.invokeBoolean(getTableComponent(), "isCellSelected",
                new Object[] { Integer.valueOf(row), Integer.valueOf(getColumnIndex()) },
                new Class[] { Integer.TYPE, Integer.TYPE });
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MTableCell))
            return false;
        if (!super.equals(o))
            return false;
        final MTableCell mTableCell = (MTableCell) o;
        if (row != mTableCell.row)
            return false;
        if (!column.equals(mTableCell.column))
            return false;
        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + row;
        result = 29 * result + column.hashCode();
        return result;
    }

    private JTable getTableComponent() {
        return (JTable) getComponent();
    }

    public String toString() {
        return super.toString() + "[" + column + ", " + row + "]";
    }

    public boolean isBeingEdited() {
        return eventQueueRunner.invokeBoolean(getTableComponent(), "isEditing")
                && eventQueueRunner.invokeInteger(getTableComponent(), "getEditingRow") == row
                && eventQueueRunner.invokeInteger(getTableComponent(), "getEditingColumn") == getColumnIndex();
    }

    public MCollectionComponent getCollectionComponent() {
        return new MTable(getTableComponent(), getMComponentName(), finder, windowMonitor);
    }

    public boolean keyNeeded(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_ENTER)
            return false;
        if (isBeingEdited()) {
            if (getEditor() != null)
                return getEditor().keyNeeded(e);
        }
        return super.keyNeeded(e, true);
    }

    public int clickNeeded(MouseEvent e) {
        if (e.isPopupTrigger())
            return ClickAction.RECORD_CLICK;
        if (!isMComponentEditable() && e.getClickCount() >= 2)
            return ClickAction.RECORD_CLICK;
        JTable table = getTableComponent();
        boolean rowSelection = (Boolean) eventQueueRunner.invoke(table, "getRowSelectionAllowed");
        boolean columnSelection = (Boolean) eventQueueRunner.invoke(table, "getColumnSelectionAllowed");
        return (!rowSelection && !columnSelection) ? ClickAction.RECORD_CLICK : ClickAction.RECORD_NONE;
    }

    public void setCurrentSelection() {
        eventQueueRunner.invoke(getTableComponent(), "setRowSelectionInterval",
                new Object[] { Integer.valueOf(row), Integer.valueOf(row) }, new Class[] { Integer.TYPE, Integer.TYPE });
        int columnIndex = getColumnIndex();
        eventQueueRunner.invoke(getTableComponent(), "setColumnSelectionInterval", new Object[] { Integer.valueOf(columnIndex),
                Integer.valueOf(columnIndex) }, new Class[] { Integer.TYPE, Integer.TYPE });
    }

    protected Class<?>[] getPropertyAccessMethodParameters(String property) {
        if (property.equals("Text"))
            return null;
        else if (property.equals("RowSelected") || property.equals("ColumnSelected"))
            return new Class[] { Integer.TYPE };
        else
            return new Class[] { Integer.TYPE, Integer.TYPE };
    }

    protected Object[] getPropertyAccessMethodArguments(String property) {
        if (property.equals("Text"))
            return null;
        else if (property.equals("RowSelected"))
            return new Object[] { Integer.valueOf(row) };
        else if (property.equals("ColumnSelected"))
            return new Object[] { Integer.valueOf(getColumnIndex()) };
        else
            return new Object[] { Integer.valueOf(row), Integer.valueOf(getColumnIndex()) };
    }

    public Point getLocation() {
        Rectangle rect = (Rectangle) eventQueueRunner.invoke(getTableComponent(), "getCellRect",
                new Object[] { Integer.valueOf(row), Integer.valueOf(getColumnIndex()), Boolean.valueOf(false) }, new Class[] {
                        Integer.TYPE, Integer.TYPE, Boolean.TYPE });
        if (rect != null)
            return rect.getLocation();
        return new Point(-1, -1);
    }

    public Dimension getSize() {
        Rectangle rect = (Rectangle) eventQueueRunner.invoke(getTableComponent(), "getCellRect",
                new Object[] { Integer.valueOf(row), Integer.valueOf(getColumnIndex()), Boolean.valueOf(false) }, new Class[] {
                        Integer.TYPE, Integer.TYPE, Boolean.TYPE });
        if (rect != null)
            return rect.getSize();
        return new Dimension(0, 0);
    }

    protected String getCollectionComponentAccessMethodName() {
        return "getTable";
    }

    public MCollectionComponent getTable() {
        return getCollectionComponentWithWindowID();
    }

}
