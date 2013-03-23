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
package net.sourceforge.marathon.junit.swingui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A tree model for a Test.
 */
public class TestTreeModel implements TreeModel {
    private Test root;
    private Vector<TreeModelListener> modelListeners = new Vector<TreeModelListener>();
    private Hashtable<Test, Test> failures = new Hashtable<Test, Test>();
    private Hashtable<Test, Test> errors = new Hashtable<Test, Test>();
    private Hashtable<Test, Test> runTests = new Hashtable<Test, Test>();

    /**
     * Constructs a tree model with the given test as its root.
     */
    public TestTreeModel(Test root) {
        super();
        this.root = root;
    }

    /**
     * adds a TreeModelListener
     */
    public void addTreeModelListener(TreeModelListener l) {
        if (!modelListeners.contains(l))
            modelListeners.addElement(l);
    }

    /**
     * Removes a TestModelListener
     */
    public void removeTreeModelListener(TreeModelListener l) {
        modelListeners.removeElement(l);
    }

    /**
     * Finds the path to a test. Returns the index of the test in its parent
     * test suite.
     */
    public int findTest(Test target, Test node, Vector<Test> path) {
        if (target.equals(node))
            return 0;
        TestSuite suite = isTestSuite(node);
        for (int i = 0; i < getChildCount(node); i++) {
            Test t = suite.testAt(i);
            int index = findTest(target, t, path);
            if (index >= 0) {
                path.insertElementAt(node, 0);
                if (path.size() == 1)
                    return i;
                return index;
            }
        }
        return -1;
    }

    /**
     * Fires a node changed event
     */
    public void fireNodeChanged(TreePath path, int index) {
        int[] indices = { index };
        Object[] changedChildren = { getChild(path.getLastPathComponent(), index) };
        TreeModelEvent event = new TreeModelEvent(this, path, indices, changedChildren);
        Enumeration<TreeModelListener> e = modelListeners.elements();
        while (e.hasMoreElements()) {
            TreeModelListener l = e.nextElement();
            l.treeNodesChanged(event);
        }
    }

    /**
     * Gets the test at the given index
     */
    public Object getChild(Object parent, int index) {
        TestSuite suite = isTestSuite(parent);
        if (suite != null)
            return suite.testAt(index);
        return null;
    }

    /**
     * Gets the number of tests.
     */
    public int getChildCount(Object parent) {
        TestSuite suite = isTestSuite(parent);
        if (suite != null)
            return suite.testCount();
        return 0;
    }

    /**
     * Gets the index of a test in a test suite
     */
    public int getIndexOfChild(Object parent, Object child) {
        TestSuite suite = isTestSuite(parent);
        if (suite != null) {
            int i = 0;
            for (Enumeration<Test> e = suite.tests(); e.hasMoreElements(); i++) {
                if (child.equals(e.nextElement()))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Returns the root of the tree
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Tests if the test is a leaf.
     */
    public boolean isLeaf(Object node) {
        return isTestSuite(node) == null;
    }

    /**
     * Tests if the node is a TestSuite.
     */
    TestSuite isTestSuite(Object node) {
        if (node instanceof TestSuite)
            return (TestSuite) node;
        if (node instanceof TestDecorator) {
            Test baseTest = ((TestDecorator) node).getTest();
            return isTestSuite(baseTest);
        }
        return null;
    }

    /**
     * Called when the value of the model object was changed in the view
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        // we don't support direct editing of the model
        System.err.println("TreeModel.valueForPathChanged: not implemented");
    }

    /**
     * Remembers a test failure
     */
    void addFailure(Test t) {
        failures.put(t, t);
    }

    /**
     * Remembers a test error
     */
    void addError(Test t) {
        errors.put(t, t);
    }

    /**
     * Remembers that a test was run
     */
    void addRunTest(Test t) {
        runTests.put(t, t);
    }

    /**
     * Returns whether a test was run
     */
    boolean wasRun(Test t) {
        return runTests.get(t) != null;
    }

    /**
     * Tests whether a test was an error
     */
    boolean isError(Test t) {
        return (errors != null) && errors.get(t) != null;
    }

    /**
     * Tests whether a test was a failure
     */
    boolean isFailure(Test t) {
        return (failures != null) && failures.get(t) != null;
    }

    /**
     * Resets the test results
     */
    void resetResults() {
        failures = new Hashtable<Test, Test>();
        runTests = new Hashtable<Test, Test>();
        errors = new Hashtable<Test, Test>();
    }
}
