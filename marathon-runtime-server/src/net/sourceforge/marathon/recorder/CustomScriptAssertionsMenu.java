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
package net.sourceforge.marathon.recorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.marathon.action.AbstractScriptElement;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.UIUtils;

public class CustomScriptAssertionsMenu extends AbstractContextMenu implements IRecordingArtifact {

    public static final class RawStringScriptElement extends AbstractScriptElement {
        private static final long serialVersionUID = 1L;
        private final String content;

        private RawStringScriptElement(String content, WindowId id) {
            super(null, id);
            this.content = content;
        }

        public String toScriptCode() {
            return Indent.getIndent() + content + "\n";
        }

    }

    private static final class AssertionListModel extends AbstractListModel {
        private static final long serialVersionUID = 1L;
        private String[][] assertions;

        public Object getElementAt(int index) {
            return assertions[index];
        }

        public int getSize() {
            return assertions == null ? 0 : assertions.length;
        }

        public void setData(String[][] customAssertions) {
            assertions = customAssertions;
            if (assertions != null)
                fireContentsChanged(this, 0, assertions.length);
            else
                fireContentsChanged(this, 0, 0);
        }
    }

    private IMarathonRuntime runtime;
    private JTextArea descriptionArea;
    private JList assertionList;
    private AssertionListModel model;
    private JButton insertButton;

    public CustomScriptAssertionsMenu(ContextMenuWindow window, IRecorder recorder, ComponentFinder finder,
            IMarathonRuntime runtime, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(window, recorder, finder, scriptModel, windowMonitor);
        this.runtime = runtime;
    }

    public Component getContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createCustomAssertionPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private Component createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        insertButton = UIUtils.createInsertButton();
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getRecorder().record(
                        new RawStringScriptElement(((String[]) assertionList.getSelectedValue())[2], runtime.getTopWindowId()));
            }
        });
        insertButton.setEnabled(false);
        buttonPanel.add(insertButton);
        return buttonPanel;
    }

    private Component createCustomAssertionPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setTopComponent(new JScrollPane(getAssertionList()));
        descriptionArea = new JTextArea(4, 0);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        splitPane.setBottomComponent(new JScrollPane(descriptionArea));
        return splitPane;
    }

    private Component getAssertionList() {
        assertionList = new JList();
        assertionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assertionList.setBorder(BorderFactory.createTitledBorder("Custom Assertions"));
        model = new AssertionListModel();
        assertionList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                String name = ((String[]) value)[0];
                return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            }
        });
        assertionList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                if (assertionList.getSelectedIndex() == -1)
                    insertButton.setEnabled(false);
                else {
                    descriptionArea.setText(((String[]) assertionList.getSelectedValue())[1]);
                    insertButton.setEnabled(true);
                }
            }
        });
        assertionList.setModel(model);
        return assertionList;
    }

    public String getName() {
        return "Custom";
    }

    public void setComponent(final Component component, Point point, boolean isTriggered) {
        final MComponent mcomponent = getFinder().getMComponentByComponent(component);
        new Thread() {
            public void run() {
                String[][] customAssertions = runtime.getCustomAssertions(mcomponent);
                model.setData(customAssertions);
            }
        }.start();
    }

}
