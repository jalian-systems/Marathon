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

import java.util.Iterator;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.marathon.recorder.WindowMonitor;

public class MTableHeader extends MCollectionComponent {

    public MTableHeader(JTableHeader tableHeader, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(tableHeader, name, finder, windowMonitor);
    }

    public String[][] getContent() {
        TableColumnModel tcm = (TableColumnModel) eventQueueRunner.invoke(getComponent(), "getColumnModel");
        int columnCount = tcm.getColumnCount();
        String[][] content = new String[1][columnCount];
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            TableColumn tableColumn = tcm.getColumn(i);
            content[0][i] = tableColumn.getIdentifier().toString();
        }
        return content;
    }

    public int getRowCount() {
        TableColumnModel tcm = (TableColumnModel) eventQueueRunner.invoke(getComponent(), "getColumnModel");
        return tcm.getColumnCount();
    }

    private class MTableHeaderItemIterator implements Iterator<MComponent> {
        private int totalItems = getRowCount();
        private int currentItem = 0;

        public boolean hasNext() {
            return currentItem < totalItems;
        }

        public MComponent next() {
            return new MTableHeaderItem(getComponent(), getMComponentName(), currentItem++, finder, windowMonitor);
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove on CollectionComponent is not supported");
        }
    }

    public Iterator<MComponent> iterator() {
        return new MTableHeaderItemIterator();
    }

}
