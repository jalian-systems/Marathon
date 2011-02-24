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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import junit.framework.TestFailure;
import junit.runner.BaseTestRunner;

/**
 * A view that shows a stack trace of a failure
 */
public class MarathonFailureDetailView {
    private JList list;
    private List<IStackMessageProcessor> messageProcessors = new Vector<IStackMessageProcessor>();

    /**
     * A ListModel representing the scanned failure stack trace.
     */
    static class StackTraceListModel extends AbstractListModel {
        private static final long serialVersionUID = 1L;
        private Vector<String> lines = new Vector<String>(20);

        public Object getElementAt(int index) {
            return lines.elementAt(index);
        }

        public int getSize() {
            return lines.size();
        }

        public void setTrace(String trace) {
            scan(trace);
            fireContentsChanged(this, 0, lines.size());
        }

        public void clear() {
            lines.removeAllElements();
            fireContentsChanged(this, 0, lines.size());
        }

        private void scan(String trace) {
            lines.removeAllElements();
            StringTokenizer st = new StringTokenizer(trace, "\n\r", false);
            while (st.hasMoreTokens())
                lines.add(st.nextToken());
        }
    }

    /**
     * Renderer for stack entries
     */
    static class StackEntryRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list, Object value, int modelIndex, boolean isSelected,
                boolean cellHasFocus) {
            String text = ((String) value).replace('\t', ' ');
            Component c = super.getListCellRendererComponent(list, text, modelIndex, isSelected, cellHasFocus);
            setText(text);
            setToolTipText(text);
            return c;
        }
    }

    /**
     * Returns the component used to present the trace
     */
    public Component getComponent() {
        if (list == null) {
            list = new JList(new StackTraceListModel());
            list.setFont(new Font("Dialog", Font.PLAIN, 12));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setVisibleRowCount(5);
            list.setCellRenderer(new StackEntryRenderer());
            list.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        processMessage((String) list.getSelectedValue());
                    }
                }
            });
        }
        return list;
    }

    public void addMessageProcessor(IStackMessageProcessor p) {
        messageProcessors.add(p);
    }

    protected void processMessage(String msg) {
        if (msg == null)
            return;
        Iterator<IStackMessageProcessor> iterator = messageProcessors.iterator();
        while (iterator.hasNext())
            ((IStackMessageProcessor) iterator.next()).processMessage(msg);
    }

    /**
     * Shows a TestFailure
     */
    public void showFailure(TestFailure failure) {
        getModel().setTrace(BaseTestRunner.getFilteredTrace(failure.trace()));
    }

    /**
     * Clears the output.
     */
    public void clear() {
        getModel().clear();
    }

    private StackTraceListModel getModel() {
        return (StackTraceListModel) list.getModel();
    }
}
