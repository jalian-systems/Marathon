package net.sourceforge.marathon.navigator;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class NavigatorTreeNode implements MutableTreeNode {

    private File f;
    private ArrayList<NavigatorTreeNode> children;
    private NavigatorTreeNode parent;
    private boolean expanded;

    private class NavigatorNodeEnumeration implements Enumeration<NavigatorTreeNode> {
        int current = 0;

        public boolean hasMoreElements() {
            return current < children.size();
        }

        public NavigatorTreeNode nextElement() {
            return children.get(current++);
        }

    }

    public NavigatorTreeNode(File f) {
        this.f = f;
    }

    public NavigatorTreeNode() {
        this.f = null;
    }

    public NavigatorTreeNode(File file, NavigatorTreeNode parent) {
        this(file);
        this.parent = parent;
    }

    public NavigatorTreeNode getChildAt(int childIndex) {
        if (children == null)
            populateChildren();
        return children.get(childIndex);
    }

    public int getChildCount() {
        if (children == null)
            populateChildren();
        return children.size();
    }

    public NavigatorTreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        if (children == null)
            populateChildren();
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return f == null || f.isDirectory();
    }

    public boolean isLeaf() {
        return !getAllowsChildren();
    }

    public Enumeration<NavigatorTreeNode> children() {
        if (children == null)
            populateChildren();
        return new NavigatorNodeEnumeration();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void add(NavigatorTreeNode node) {
        if (children == null) {
            children = new ArrayList<NavigatorTreeNode>();
        }
        children.add(node);
        node.setParent(this);
    }

    public File getFile() {
        return f;
    }

    public void insert(MutableTreeNode child, int index) {
        if (child instanceof NavigatorTreeNode) {
            if (children == null)
                populateChildren();
            children.add(index, (NavigatorTreeNode) child);
            child.setParent(this);
            return;
        }
        throw new UnsupportedOperationException("Can't insert the nodes of type " + child.getClass() + " into NavigatorNode");
    }

    public void remove(int index) {
        children.remove(index);
    }

    public void remove(MutableTreeNode node) {
        children.remove(node);
    }

    public void setUserObject(Object object) {
        if (object instanceof File)
            f = (File) object;
        else
            throw new UnsupportedOperationException("Can't set a object to object of type " + object.getClass());
    }

    public void removeFromParent() {
        if (parent == null) {
            throw new UnsupportedOperationException("Can't remove from null parent");
        }
        parent.remove(this);
    }

    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof NavigatorTreeNode) {
            parent = (NavigatorTreeNode) newParent;
            return;
        }
        throw new UnsupportedOperationException("Can't set a parent to object of type " + newParent.getClass());
    }

    private void populateChildren() {
        if (f == null)
            throw new UnsupportedOperationException("Should not happen...");
        if (!f.isDirectory()) {
            children = new ArrayList<NavigatorTreeNode>();
            return;
        }
        File[] files;
        FileFilter filter = Navigator.getFilter();
        if (filter == null)
            files = f.listFiles();
        else
            files = f.listFiles(filter);
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                if (o1.isDirectory() == o2.isDirectory()) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
                return o1.isDirectory() ? -1 : 1;
            }
        });
        children = new ArrayList<NavigatorTreeNode>();
        for (int i = 0; i < files.length; i++) {
            children.add(new NavigatorTreeNode(files[i], this));
        }
    }

    @Override public String toString() {
        return f == null ? "null" : f.toString();
    }

    public void refresh() {
        children = null;
    }

    public NavigatorTreeNode dup() {
        NavigatorTreeNode node = new NavigatorTreeNode(f, parent);
        node.children = children;
        node.expanded = expanded;
        return node;
    }
}
