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
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.OSUtils;

public class MList extends MCollectionComponent {
    public MList(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    private JList getList() {
        return (JList) getComponent();
    }

    public int getRowCount() {
        return getModel().getSize();
    }

    public int getItemCount() {
        return getRowCount();
    }

    public String[][] getContent() {
        ListModel model = getModel();
        int elementCount = model.getSize();
        String[][] content = new String[1][elementCount];
        for (int i = 0; i < elementCount; i++) {
            MComponent renderer = getRendererAt(i);
            if (renderer == null)
                content[0][i] = model.getElementAt(i).toString();
            else
                content[0][i] = renderer.getText();
        }
        return content;
    }

    private MComponent getRendererAt(int index) {
        if (finder == null) {
            return null;
        }
        ListCellRenderer renderer = (ListCellRenderer) eventQueueRunner.invoke(getList(), "getCellRenderer");
        boolean isSelected = eventQueueRunner.invokeBoolean(getList(), "isSelectedIndex", new Object[] { new Integer(index) },
                new Class[] { Integer.TYPE });
        ListModel model = getModel();
        Component rendererComponent = renderer.getListCellRendererComponent(getList(), model.getElementAt(index), index,
                isSelected, isSelected);
        return (rendererComponent == null ? null : finder.getMComponentByComponent(rendererComponent, "doesn't matter", null));
    }

    public String getText() {
        int selectedIndex = eventQueueRunner.invokeInteger(getList(), "getSelectedIndex");
        if (selectedIndex == -1)
            return "[]";
        StringBuffer text = new StringBuffer("[");
        int[] indices = (int[]) eventQueueRunner.invoke(getList(), "getSelectedIndices");
        for (int i = 0; i < indices.length; i++) {
            MListCell cellItem = new MListCell(getList(), getMComponentName(), indices[i], finder, windowMonitor);
            text.append(cellItem.getComponentInfo());
            if (i < indices.length - 1)
                text.append(", ");
        }
        text.append("]");
        return text.toString();
    }

    private ListModel getModel() {
        return (ListModel) eventQueueRunner.invoke(getList(), "getModel");
    }

    public void setText(String text) {
        Properties[] pa = PropertyHelper.fromStringToArray(text, new String[][] { new String[] { "Text" },
                new String[] { "Index", "Text" } });

        setCellSelection(pa);
    }

    public void setCellSelection(Properties[] pa) {
        if (pa.length == 0) {
            eventQueueRunner.invoke(getList(), "setSelectedIndices", new Object[] { new int[0] }, new Class[] { int[].class });
            return;
        }

        boolean first = true;
        for (int i = 0; i < pa.length; i++) {
            MListCell c = (MListCell) findMatchingComponent(pa[i]);
            if (c == null)
                throw new ComponentException("Could not find list cell component matching given property list: " + pa[i],
                        finder.getScriptModel(), windowMonitor);
            setSelectItem(c.getIndex(), first);
            first = false;
        }
    }

    private void setSelectItem(int index, boolean firstItem) {
        swingWait();
        FireableMouseClickEvent event = new FireableMouseClickEvent(getComponent());
        Rectangle r = (Rectangle) eventQueueRunner.invoke(getList(), "getCellBounds", new Object[] { new Integer(index),
                new Integer(index) }, new Class[] { Integer.TYPE, Integer.TYPE });
        Point p = new Point((int) r.getCenterX(), (int) r.getCenterY());
        eventQueueRunner.invoke(getList(), "ensureIndexIsVisible", new Object[] { new Integer(index) },
                new Class[] { Integer.TYPE });
        swingWait();
        if (firstItem)
            event.fire(p, 1);
        else
            event.fire(p, 1, OSUtils.MOUSE_MENU_MASK);
        swingWait();
    }

    private class MListCellIterator implements Iterator<MComponent> {
        private int totalItems = getRowCount();
        private int currentItem = 0;

        public boolean hasNext() {
            return currentItem < totalItems;
        }

        public MComponent next() {
            return new MListCell(getList(), getMComponentName(), currentItem++, finder, windowMonitor);
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove on CollectionComponent is not supported");
        }

    }

    public Iterator<MComponent> iterator() {
        return new MListCellIterator();
    }
}
