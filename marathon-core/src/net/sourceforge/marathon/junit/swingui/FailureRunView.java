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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

/**
 * A view presenting the test failures as a list.
 */
public class FailureRunView implements TestRunView {
    private JList failureList;
    private ITestRunContext runContext;

    /**
     * Renders TestFailures in a JList
     */
    static class FailureListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private Icon failureIcon;
        private Icon errorIcon;

        FailureListCellRenderer() {
            super();
            loadIcons();
        }

        void loadIcons() {
            failureIcon = Icons.T_TESTFAIL;
            errorIcon = Icons.T_TESTERROR;
        }

        public Component getListCellRendererComponent(JList list, Object value, int modelIndex, boolean isSelected,
                boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, modelIndex, isSelected, cellHasFocus);
            TestFailure failure = (TestFailure) value;
            String text = failure.failedTest().toString();
            String msg = failure.exceptionMessage();
            if (msg != null)
                text += ":" + BaseTestRunner.truncate(msg);
            if (failure.isFailure()) {
                if (failureIcon != null)
                    setIcon(failureIcon);
            } else {
                if (errorIcon != null)
                    setIcon(errorIcon);
            }
            setText(text);
            setToolTipText(text);
            return c;
        }
    }

    public FailureRunView(ITestRunContext context) {
        runContext = context;
        failureList = new JList(runContext.getFailures());
        failureList.setFont(new Font("Dialog", Font.PLAIN, 12));
        failureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        failureList.setCellRenderer(new FailureListCellRenderer());
        failureList.setVisibleRowCount(5);
        failureList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                testSelected();
            }
        });
        failureList.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() > 1)
                    testOpened();
            }
        });
    }

    public Test getSelectedTest() {
        int index = failureList.getSelectedIndex();
        if (index == -1)
            return null;
        ListModel model = failureList.getModel();
        TestFailure failure = (TestFailure) model.getElementAt(index);
        return failure.failedTest();
    }

    public void activate() {
        testSelected();
    }

    public void addTab(JTabbedPane pane) {
        JScrollPane scrollPane = new JScrollPane(failureList);
        Icon errorIcon = Icons.FAILURES;
        pane.addTab("Failures", errorIcon, scrollPane, "The list of failed tests");
    }

    public void revealFailure(Test failure) {
        ListModel model = failureList.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            if (((TestFailure) model.getElementAt(i)).failedTest().equals(failure)) {
                failureList.setSelectedIndex(i);
                return;
            }
        }
    }

    public void aboutToStart(Test suite, TestResult result) {
    }

    public void runFinished(Test suite, TestResult result) {
    }

    protected void testSelected() {
        runContext.handleTestSelected(getSelectedTest());
    }

    protected void testOpened() {
        runContext.handleTestOpened(getSelectedTest());
    }

    public void reset(Test test) {
    }
}
