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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;

public class MTreeNode extends MCellComponent {
    private String path;
    private int row = -1;

    public MTreeNode(Component component, String name, Object pathOrPoint, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
        if (pathOrPoint instanceof String || pathOrPoint instanceof Properties) {
            Properties props;
            if (pathOrPoint instanceof String)
                props = parseProperties((String) pathOrPoint, new String[][] { { "Path" } });
            else
                props = (Properties) pathOrPoint;
            path = props.getProperty("Path");
            if (path == null) {
                MTreeNode node = (MTreeNode) getCollectionComponent().findMatchingComponent(props);
                if (node == null)
                    throw new ComponentException("Could not find matching treenode for given property list: " + props,
                            finder.getScriptModel(), windowMonitor);

                path = node.getPath();
            }
            row = eventQueueRunner.invokeInteger(getTreeComponent(), "getRowForPath", new Object[] { getTreePath() },
                    new Class[] { TreePath.class });
            if (row < 0)
                throw new ComponentException("Could not find row for treepath for Tree(" + getMComponentName() + ") path: " + path,
                        finder.getScriptModel(), windowMonitor);
        } else {
            Point point = (Point) pathOrPoint;
            StringBuffer pathBuild = new StringBuffer("");
            TreePath treePath = (TreePath) eventQueueRunner.invoke(getTreeComponent(), "getClosestPathForLocation", new Object[] {
                Integer.valueOf((int) point.getX()), Integer.valueOf((int) point.getY()) }, new Class[] { Integer.TYPE, Integer.TYPE });
            if (treePath != null) {
                row = eventQueueRunner.invokeInteger(getTreeComponent(), "getRowForPath", new Object[] { treePath },
                        new Class[] { TreePath.class });
                boolean rootVisible = eventQueueRunner.invokeBoolean(getTreeComponent(), "isRootVisible");
                int start = rootVisible ? 0 : 1;
                Object[] objs = treePath.getPath();
                for (int i = start; i < objs.length; i++) {
                    String pathString;
                    if (objs[i].toString() == null)
                        pathString = "";
                    else
                        pathString = escapeSpecialCharacters(objs[i].toString());
                    pathBuild.append("/" + pathString);
                }
            }
            this.path = pathBuild.toString();
        }
    }

    public String getPath() {
        return path;
    }

    public MTreeNode(JTree tree, String name, int row, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(tree, name, finder, windowMonitor);
        this.row = row;
        TreePath treePath = (TreePath) eventQueueRunner.invoke(tree, "getPathForRow", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        StringBuffer pathBuild = new StringBuffer("");
        boolean rootVisible = eventQueueRunner.invokeBoolean(tree, "isRootVisible");
        int start = rootVisible ? 0 : 1;
        if (treePath != null) {
            Object[] objs = treePath.getPath();
            for (int i = start; i < objs.length; i++) {
                String pathString;
                if (objs[i].toString() == null)
                    pathString = "";
                else
                    pathString = escapeSpecialCharacters(objs[i].toString());
                pathBuild.append("/" + pathString);
            }
        }
        this.path = pathBuild.toString();
    }

    private void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    public void click(int numberOfClicks, int modifiers, Point position) {
        if (row == -1)
            return;
        if (position == null) {
            Rectangle rect = (Rectangle) eventQueueRunner.invoke(getTreeComponent(), "getRowBounds",
                    new Object[] { Integer.valueOf(row) }, new Class[] { Integer.TYPE });
            if (rect == null)
                throw new RuntimeException("Leaf " + getComponentInfo() + "for tree " + getMComponentName() + " not visible...");
            position = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
        }
        super.click(numberOfClicks, modifiers, position);
    }

    public String getComponentInfo() {
        return createPropertyMapString(new String[] { "Path" });
    }

    private TreePath getTreePath() {
        try {
            new Retry("Search for cell component", ComponentFinder.getRetryInterval(), ComponentFinder.getRetryCount(),
                    new Retry.Attempt() {
                        public void perform() {
                            if (findTreePath(new StringBuffer()) == null)
                                retry();
                        }
                    });
        } catch (Exception e) {
            return null;
        }
        StringBuffer searchedPath = new StringBuffer();
        TreePath treePath = findTreePath(searchedPath);
        assertTrue("no tree node corresponding to <" + searchedPath + "> in <" + this.path + ">", treePath != null);
        return treePath;
    }

    private TreePath findTreePath(StringBuffer searchedPath) {
        String[] tokens = path.substring(1).split("(?<!\\\\)/");
        TreeModel treeModel = (TreeModel) eventQueueRunner.invoke(getTreeComponent(), "getModel");
        Object rootNode = treeModel.getRoot();
        assertTrue("invalid path specifier <" + path + ">", tokens.length >= 1);
        boolean rootVisible = eventQueueRunner.invokeBoolean(getTreeComponent(), "isRootVisible");
        int start = rootVisible ? 1 : 0;
        TreePath treePath = new TreePath(rootNode);
        if (rootVisible) {
            String rootNodeText = unescapeSpecialCharacters(tokens[0]);
            searchedPath.append("/" + rootNodeText);
            assertTrue("JTree does not have a root node!", rootNode != null);
            assertTrue(
                    "JTree root node does not match: Expected </" + getPathText(treePath) + "> Actual: <" + searchedPath.toString()
                            + ">", searchedPath.toString().equals("/" + getPathText(treePath)));
        }
        for (int i = start; i < tokens.length; i++) {
            String childText = unescapeSpecialCharacters(tokens[i]);
            searchedPath.append("/" + childText);
            boolean matched = false;
            eventQueueRunner.invoke(getTreeComponent(), "expandPath", new Object[] { treePath }, new Class[] { TreePath.class });
            swingWait();
            for (int j = 0; j < treeModel.getChildCount(treePath.getLastPathComponent()); j++) {
                Object child = treeModel.getChild(treePath.getLastPathComponent(), j);
                TreePath childPath = treePath.pathByAddingChild(child);
                if (childText.equals(getPathText(childPath))) {
                    treePath = childPath;
                    matched = true;
                    break;
                }
            }
            if (!matched)
                return null ;
        }
        return treePath;
    }

    private JTree getTreeComponent() {
        return (JTree) getComponent();
    }

    // get the object specified by path and return its corresponding text value
    private String getPathText(TreePath path) {
        JTree tree = getTreeComponent();
        Object lastPathComponent = path.getLastPathComponent();
        if (lastPathComponent == null || lastPathComponent.toString() == null)
            return "";
        int rowPath = eventQueueRunner.invokeInteger(tree, "getRowForPath", new Object[] { path }, new Class[] { TreePath.class });
        String value = (String) eventQueueRunner.invoke(tree, "convertValueToText", new Object[] { lastPathComponent,
                Boolean.valueOf(true), Boolean.valueOf(true), Boolean.valueOf(true), Integer.valueOf(rowPath), Boolean.valueOf(true) }, new Class[] {
                Object.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE });
        return value;
    }

    public MCollectionComponent getCollectionComponent() {
        return new MTree(getTreeComponent(), getMComponentName(), getFinder(), windowMonitor);
    }

    public String escapeSpecialCharacters(String name) {
        return name.replaceAll("/", "\\\\/");
    }

    public String unescapeSpecialCharacters(String name) {
        return name.replaceAll("\\\\/", "/");
    }

    public boolean keyNeeded(KeyEvent e) {
        return super.keyNeeded(e, true);
    }

    public int clickNeeded(MouseEvent e) {
        if (isPopupTrigger(e))
            return ClickAction.RECORD_CLICK;
        int toggleClickCount = eventQueueRunner.invokeInteger(getTreeComponent(), "getToggleClickCount");
        if (toggleClickCount == 0)
            return ClickAction.RECORD_CLICK;
        return ClickAction.RECORD_NONE;
    }

    public void setCurrentSelection() {
        TreePath path = getTreePath();
        eventQueueRunner.invoke(getTreeComponent(), "setSelectionPath", new Object[] { path }, new Class[] { TreePath.class });
    }

    public String getText() {
        if (row == -1 || finder == null)
            return null;
        boolean isEditing = eventQueueRunner.invokeBoolean(getTreeComponent(), "isEditing");
        String text = isEditing ? getEditor().getText() : getRenderer().getText();
        if (text != null)
            return escapeSpecialCharacters(text);
        return null;
    }

    private MComponent getRenderer() {
        JTree tree = getTreeComponent();
        TreeCellRenderer renderer = (TreeCellRenderer) eventQueueRunner.invoke(tree, "getCellRenderer");
        boolean isSelected = eventQueueRunner.invokeBoolean(tree, "isRowSelected", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        boolean isExpanded = eventQueueRunner.invokeBoolean(tree, "isExpanded", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        TreePath treePath = (TreePath) eventQueueRunner.invoke(tree, "getPathForRow", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        boolean isLeaf = false;
        Component rendererComponent = renderer.getTreeCellRendererComponent(tree, treePath.getLastPathComponent(), isSelected,
                isExpanded, isLeaf, row, true);
        return finder.getMComponentByComponent(rendererComponent, "doesn't matter", null);
    }

    protected MComponent getEditor() {
        JTree tree = getTreeComponent();
        TreeCellEditor cellEditor = (TreeCellEditor) eventQueueRunner.invoke(tree, "getCellEditor");
        boolean isSelected = eventQueueRunner.invokeBoolean(tree, "isRowSelected", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        boolean isExpanded = eventQueueRunner.invokeBoolean(tree, "isExpanded", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        TreePath treePath = (TreePath) eventQueueRunner.invoke(tree, "getPathForRow", new Object[] { Integer.valueOf(row) },
                new Class[] { Integer.TYPE });
        boolean isLeaf = false;
        Component editor = cellEditor.getTreeCellEditorComponent(tree, treePath.getLastPathComponent(), isSelected, isExpanded,
                isLeaf, row);
        if (editor instanceof Container) {
            editor = ((Container) editor).getComponent(0);
        }
        return finder.getMComponentByComponent(editor, "doesn't matter", null);
    }

    public boolean isMComponentEditable() {
        return true;
    }

    public void setText(String text) {
        if (row == -1)
            return;
        getTreeComponent().startEditingAtPath(getTreePath());
        MComponent editor = getEditor();
        if (editor == null) {
            System.err.println("Warning: Editor component not found for an table cell with select() call:\n" + "\tfor table: "
                    + getTree().getMComponentName() + " at path: " + getComponentInfo());
            return;
        }
        editor.setText(text, true);
        eventQueueRunner.invoke(getTreeComponent(), "stopEditing");
        swingWait();
    }

    protected Class<?>[] getPropertyAccessMethodParameters(String property) {
        return new Class[] { Integer.TYPE };
    }

    protected Object[] getPropertyAccessMethodArguments(String property) {
        return new Object[] { Integer.valueOf(row) };
    }

    public Point getLocation() {
        Rectangle bounds = (Rectangle) eventQueueRunner.invoke(getTreeComponent(), "getRowBounds",
                new Object[] { Integer.valueOf(row) }, new Class[] { Integer.TYPE });
        return bounds.getLocation();
    }

    public Dimension getSize() {
        Rectangle bounds = (Rectangle) eventQueueRunner.invoke(getTreeComponent(), "getRowBounds",
                new Object[] { Integer.valueOf(row) }, new Class[] { Integer.TYPE });
        return bounds.getSize();
    }

    protected String getCollectionComponentAccessMethodName() {
        return "getTree";
    }

    public MCollectionComponent getTree() {
        return getCollectionComponentWithWindowID();
    }

    public int getRow() {
        return row;
    }

    @Override public String toString() {
        return super.toString() + "[" + getText() + "]";
    }
}
