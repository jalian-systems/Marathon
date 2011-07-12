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
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.OSUtils;

public class MTree extends MCollectionComponent {


    public MTree(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    public int getRowCount() {
        JTree tree = getTree();
        TreeModel model = (TreeModel) eventQueueRunner.invoke(tree, "getModel");
        return getNodeCount(model, model.getRoot()) + 1;
    }

    private int getNodeCount(TreeModel model, Object root) {
        int count = model.getChildCount(root);
        for (int i = 0; i < model.getChildCount(root); i++) {
            Object node = model.getChild(root, i);
            count += getNodeCount(model, node);
        }
        return count;
    }

    private JTree getTree() {
        return (JTree) getComponent();
    }

    public String[][] getContent() {
        String[][] content = new String[1][getRowCount()];
        List<String> treeContent = new Vector<String>(getRowCount());
        TreeModel model = (TreeModel) eventQueueRunner.invoke(getTree(), "getModel");
        getTreeContent(model, model.getRoot(), treeContent);
        treeContent.toArray(content[0]);
        return content;
    }

    private void getTreeContent(TreeModel model, Object root, List<String> treeContent) {
        treeContent.add(root.toString());
        for (int i = 0; i < model.getChildCount(root); i++)
            getTreeContent(model, model.getChild(root, i), treeContent);
    }

    public boolean keyNeeded(KeyEvent e) {
        return super.keyNeeded(e, true);
    }

    public String getText() {
        int[] selectionRows = (int[]) eventQueueRunner.invoke(getTree(), "getSelectionRows");
        StringBuffer text = new StringBuffer("[");
        if (selectionRows != null)
            for (int i = 0; i < selectionRows.length; i++) {
                MTreeNode node = new MTreeNode(getTree(), getMComponentName(), selectionRows[i], finder, windowMonitor);
                text.append(node.getComponentInfo());
                if (i < selectionRows.length - 1)
                    text.append(", ");
            }
        text.append("]");
        return text.toString();
    }

    public void setText(String text) {
        Properties[] properties = PropertyHelper.fromStringToArray(text, new String[][] { { "Path" } });
        setCellSelection(properties);
    }

    public void setCellSelection(Properties[] properties) {
        if (properties.length == 0) {
            eventQueueRunner.invoke(getTree(), "setSelectionRows", new Object[] { new int[0] }, new Class[] { int[].class });
            return;
        }
        boolean first = true;
        for (int i = 0; i < properties.length; i++) {
            MTreeNode treeNode = new MTreeNode(getTree(), getMComponentName(), properties[i], finder, windowMonitor);
            selectItem(treeNode.getRow(), first);
            first = false;
        }
    }
    
    private void selectItem(int row, boolean firstItem) {
        swingWait();
        FireableMouseClickEvent event = new FireableMouseClickEvent(getComponent());
        Rectangle r = (Rectangle) eventQueueRunner.invoke(getTree(), "getRowBounds", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        if (r == null)
            throw new ComponentException("Failed to get rowBounds for tree '" + getMComponentName() + "' for row " + row, finder.getScriptModel(), windowMonitor);
        Point p = new Point((int) r.getCenterX(), (int) r.getCenterY());
        if (firstItem)
            event.fire(p, 1);
        else
            event.fire(p, 1, OSUtils.MOUSE_MENU_MASK);
        swingWait();
    }

    public int clickNeeded(MouseEvent e) {
        return isPopupTrigger(e) ? ClickAction.RECORD_CLICK : ClickAction.RECORD_NONE;
    }

    private class MTreeNodeIterator implements Iterator<MComponent> {
        private int totalItems = getVisibleRowCount();
        private int currentItem = 0;

        public boolean hasNext() {
            return currentItem < totalItems;
        }

        private int getVisibleRowCount() {
            return getTree().getRowCount();
        }

        public MComponent next() {
            return new MTreeNode(getTree(), getMComponentName(), currentItem++, finder, windowMonitor);
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove on CollectionComponent is not supported");
        }

    }

    public Iterator<MComponent> iterator() {
        return new MTreeNodeIterator();
    }
}
