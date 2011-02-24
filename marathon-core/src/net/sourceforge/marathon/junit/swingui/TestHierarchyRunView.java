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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.framework.TestResult;

/**
 * A hierarchical view of a test run. The contents of a test suite is shown as a
 * tree.
 */
public class TestHierarchyRunView implements TestRunView {
    private TestSuitePanel treeBrowser;
    private ITestRunContext testContext;

    public TestHierarchyRunView(ITestRunContext context, Test suite) {
        testContext = context;
        treeBrowser = new TestSuitePanel();
        treeBrowser.getTree().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                testSelected();
            }
        });
        treeBrowser.getTree().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() > 1)
                    testOpened();
            }
        });
        treeBrowser.showTestTree(suite);
    }

    public void addTab(JTabbedPane pane) {
        Icon treeIcon = Icons.HIERARCHY;
        pane.addTab("Test Hierarchy", treeIcon, treeBrowser, "The test hierarchy");
    }

    public Test getSelectedTest() {
        return treeBrowser.getSelectedTest();
    }

    public void activate() {
        testSelected();
    }

    public void revealFailure(Test failure) {
        JTree tree = treeBrowser.getTree();
        TestTreeModel model = (TestTreeModel) tree.getModel();
        Vector<Test> vpath = new Vector<Test>();
        int index = model.findTest(failure, (Test) model.getRoot(), vpath);
        if (index >= 0 && vpath.size() != 0) {
            Object[] path = new Object[vpath.size() + 1];
            vpath.copyInto(path);
            Object last = path[vpath.size() - 1];
            path[vpath.size()] = model.getChild(last, index);
            TreePath selectionPath = new TreePath(path);
            tree.setSelectionPath(selectionPath);
            tree.makeVisible(selectionPath);
            tree.scrollPathToVisible(selectionPath);
        }
    }

    public void aboutToStart(Test suite, TestResult result) {
        treeBrowser.showTestTree(suite);
        result.addListener(treeBrowser);
    }

    public void runFinished(Test suite, TestResult result) {
        result.removeListener(treeBrowser);
    }

    protected void testSelected() {
        testContext.handleTestSelected(getSelectedTest());
    }

    protected void testOpened() {
        testContext.handleTestOpened(getSelectedTest());
    }

    public void reset(Test suite) {
        treeBrowser.showTestTree(suite);
    }
}
