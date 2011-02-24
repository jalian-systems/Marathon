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
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class MTableHeaderItem extends MCellComponent {

    private int index;
    private String selectedHeader;
    private MTableHeader tableHeaderCollectionComponent;

    public MTableHeaderItem(Component component, String name, String info, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
        Properties props = parseProperties(info, new String[][] { { "Index", "Text" }, { "Text" } });
        if (props == null) {
            // Backward compatibility
            this.selectedHeader = info;
            this.index = getIndexFromHeader();
            if (index < 0)
                throw new ComponentException("Invalid headername " + info + " for TableHeader", finder.getScriptModel(),
                        windowMonitor);
            return;
        }

        String indexString = props.getProperty("Index");
        if (indexString != null) {
            index = Integer.parseInt(indexString);
            if (!validIndex()) {
                throw new ComponentException("Invalid index in property list " + info + " for TableHeader(" + getMComponentName()
                        + ")", finder.getScriptModel(), windowMonitor);
            }
            selectedHeader = getHeaderFromIndex();
            return;
        }
        selectedHeader = props.getProperty("SelectedHeader");
        if (selectedHeader != null) {
            index = getIndexFromHeader();
            if (index < 0)
                throw new ComponentException("Invalid property list " + info + " for TableHeader(" + getMComponentName() + ")",
                        finder.getScriptModel(), windowMonitor);
            return;
        }
        MTableHeaderItem match = (MTableHeaderItem) getCollectionComponent().findMatchingComponent(props);
        if (match == null)
            throw new ComponentException("Could not find tableheaderitem with the given property list " + props,
                    finder.getScriptModel(), windowMonitor);
        if (match != null) {
            this.selectedHeader = match.selectedHeader;
            this.index = match.index;
            return;
        }
    }

    private String getHeaderFromIndex() {
        TableColumnModel tcm = (TableColumnModel) eventQueueRunner.invoke(getComponent(), "getColumnModel");
        return tcm.getColumn(index).getIdentifier().toString();
    }

    public MTableHeaderItem(Component header, String name, int index, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(header, name, finder, windowMonitor);
        TableColumnModel tcm = (TableColumnModel) eventQueueRunner.invoke(header, "getColumnModel");
        this.index = index;
        this.selectedHeader = tcm.getColumn(index).getIdentifier().toString();
    }

    public String getComponentInfo() {
        if (getCollectionComponent().hasDuplicates())
            return createPropertyMapString(new String[] { "Index", "Text" });
        else
            return createPropertyMapString(new String[] { "Text" });
    }

    private JTableHeader getJTableHeader() {
        return (JTableHeader) getComponent();
    }

    private int getIndexFromHeader() {
        JTableHeader header = (JTableHeader) getComponent();
        TableColumnModel tcm = (TableColumnModel) eventQueueRunner.invoke(header, "getColumnModel");
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            TableColumn tableColumn = tcm.getColumn(i);
            if (tableColumn.getIdentifier().equals(selectedHeader)) {
                return i;
            }
        }
        return -1;
    }

    private boolean validIndex() {
        JTableHeader header = (JTableHeader) getComponent();
        TableColumnModel tcm = (TableColumnModel) eventQueueRunner.invoke(header, "getColumnModel");
        return index >= 0 && index < tcm.getColumnCount();
    }

    public void click(int numberOfClicks, int modifiers, Point position) {
        Rectangle rect = (Rectangle) eventQueueRunner.invoke(getJTableHeader(), "getHeaderRect",
                new Object[] { new Integer(index) }, new Class[] { Integer.TYPE });
        Point p = rect.getLocation();
        super.click(numberOfClicks, modifiers, p);
    }

    public String getText() {
        return selectedHeader == null ? "" : selectedHeader;
    }

    public int getIndex() {
        return index;
    }

    public MComponent getTableHeader() {
        return getCollectionComponentWithWindowID();
    }

    public MCollectionComponent getCollectionComponent() {
        if (tableHeaderCollectionComponent == null)
            tableHeaderCollectionComponent = new MTableHeader(getJTableHeader(), getMComponentName(), finder, windowMonitor);
        return tableHeaderCollectionComponent;
    }

    protected String getCollectionComponentAccessMethodName() {
        return "getTableHeader";
    }

    public void setCurrentSelection() {
    }

    public int clickNeeded(MouseEvent e) {
        return ClickAction.RECORD_CLICK;
    }

    protected boolean oldFormat(String info) {
        return false;
    }
}
