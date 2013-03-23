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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestSuite;

/**
 * A Panel showing a test suite as a tree.
 */
public class TestSuitePanel extends JPanel implements TestListener {
    private static final long serialVersionUID = 1L;
    private JTree tree;
    private JScrollPane pane;
    private TestTreeModel model;

    static class TestTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;
        private Icon testErrorIcon;
        private Icon testOKIcon;
        private Icon testFailIcon;
        private Icon testIcon;
        private Icon tsuiteIcon;

        TestTreeCellRenderer() {
            super();
            loadIcons();
        }

        void loadIcons() {
            testErrorIcon = Icons.T_TESTERROR;
            testOKIcon = Icons.T_TESTOK;
            testFailIcon = Icons.T_TESTFAIL;
            testIcon = Icons.T_TEST;
            tsuiteIcon = Icons.T_TSUITE;
        }

        String stripParenthesis(Object o) {
            String text = o.toString();
            int pos = text.indexOf('(');
            if (pos < 1)
                return text;
            return text.substring(0, pos);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            TreeModel model = tree.getModel();
            if (model instanceof TestTreeModel) {
                TestTreeModel testModel = (TestTreeModel) model;
                Test t = (Test) value;
                String s = "";
                if (testModel.isFailure(t)) {
                    if (testFailIcon != null)
                        setIcon(testFailIcon);
                    s = " - Failed";
                } else if (testModel.isError(t)) {
                    if (testErrorIcon != null)
                        setIcon(testErrorIcon);
                    s = " - Error";
                } else if (testModel.wasRun(t)) {
                    if (testOKIcon != null)
                        setIcon(testOKIcon);
                    s = " - Passed";
                } else {
                    Icon icon = testIcon;
                    if (testModel.isTestSuite(t) != null)
                        icon = tsuiteIcon;
                    if (icon != null)
                        setIcon(icon);
                    s = " - Not run";
                }
                if (c instanceof JComponent)
                    ((JComponent) c).setToolTipText(getText() + s);
            }
            setText(stripParenthesis(value));
            return c;
        }
    }

    public TestSuitePanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(300, 100));
        tree = new JTree();
        tree.setModel(null);
        tree.setRowHeight(20);
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        pane = new JScrollPane(tree);
        add(pane, BorderLayout.CENTER);
    }

    public void addError(final Test test, final Throwable t) {
        model.addError(test);
        fireTestChanged(test, true);
    }

    public void addFailure(final Test test, final AssertionFailedError t) {
        model.addFailure(test);
        fireTestChanged(test, true);
    }

    /**
     * A test ended.
     */
    public void endTest(Test test) {
        model.addRunTest(test);
        fireTestChanged(test, true);
    }

    /**
     * A test started.
     */
    public void startTest(Test test) {
    }

    /**
     * Returns the selected test or null if multiple or none is selected
     */
    public Test getSelectedTest() {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            TestSuite suite = new TestSuite("SelectedTests");
            for (int i = 0; i < paths.length; i++) {
                Test t = (Test) paths[i].getLastPathComponent();
                suite.addTest(t);
            }
            return suite;
        }
        return null;
    }

    /**
     * Returns the Tree
     */
    public JTree getTree() {
        return tree;
    }

    /**
     * Shows the test hierarchy starting at the given test
     */
    public void showTestTree(Test root) {
        model = new TestTreeModel(root);
        tree.setModel(model);
        tree.setCellRenderer(new TestTreeCellRenderer());
    }

    private void fireTestChanged(final Test test, final boolean expand) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Vector<Test> vpath = new Vector<Test>();
                int index = model.findTest(test, (Test) model.getRoot(), vpath);
                if (index >= 0 && vpath.size() > 0) {
                    Object[] path = new Object[vpath.size()];
                    vpath.copyInto(path);
                    TreePath treePath = new TreePath(path);
                    model.fireNodeChanged(treePath, index);
                    if (expand) {
                        Object[] fullPath = new Object[vpath.size() + 1];
                        vpath.copyInto(fullPath);
                        fullPath[vpath.size()] = model.getChild(treePath.getLastPathComponent(), index);
                        TreePath fullTreePath = new TreePath(fullPath);
                        tree.scrollPathToVisible(fullTreePath);
                    }
                }
            }
        });
    }
}
