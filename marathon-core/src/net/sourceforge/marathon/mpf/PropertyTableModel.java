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
package net.sourceforge.marathon.mpf;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;

import net.sourceforge.marathon.Constants;

public class PropertyTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private String[] columnNames = { "Property", "Value" };
    private List<Property> dataList;

    public PropertyTableModel() {
        dataList = new ArrayList<Property>();
    }

    public void removeRow(int selectedRow) {
        dataList.remove(selectedRow);
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        return dataList.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Property prop = (Property) dataList.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return prop.getProperty();
        case 1:
            return prop.getValue();
        default:
            return "";
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public void addRow(Property property) {
        dataList.add(property);
        fireTableDataChanged();
    }

    public List<Property> getDataList() {
        return dataList;
    }

    public void getProperties(Properties props) {
        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            Property property = (Property) dataList.get(i);
            props.setProperty(Constants.PROP_PROPPREFIX + property.getProperty(), property.getValue());
        }
    }

    public Property getPropertyAt(int index) {
        return (Property) dataList.get(index);
    }

    public void setProperties(Properties props) {
        Enumeration<Object> enumeration = props.keys();
        while (enumeration.hasMoreElements()) {
            String property = (String) enumeration.nextElement();
            if (property.startsWith(Constants.PROP_PROPPREFIX)) {
                addRow(new Property(property.substring(Constants.PROP_PROPPREFIX.length()), props.getProperty(property)));
            }
        }
    }

    public void updateRow(int index, Property property) {
        dataList.set(index, property);
        fireTableDataChanged();
    }
}
