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
import java.io.File;
import java.io.FilenameFilter;

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

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.util.UIUtils;

public class ChecklistMenu extends AbstractContextMenu {

    private final class CheckListFileModel extends AbstractListModel {
        private static final long serialVersionUID = 1L;
        File[] items;

        private CheckListFileModel() {
            initItems();
        }

        private void initItems() {
            if (checklistDir != null)
                items = checklistDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (dir.equals(checklistDir) && name.endsWith(".xml"))
                            return true;
                        return false;
                    }
                });
            else
                items = new File[0];
        }

        public Object getElementAt(int index) {
            return items[index];
        }

        public int getSize() {
            return items.length;
        }

    }

    private JTextArea descriptionArea;
    private final File checklistDir;
    private JList checkList;
    private net.sourceforge.marathon.recorder.ChecklistMenu.CheckListFileModel model;
    private JButton insertButton;

    public ChecklistMenu(ContextMenuWindow window, IRecorder recorder, ComponentFinder finder, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(window, recorder, finder, scriptModel, windowMonitor);
        String dir = System.getProperty(Constants.PROP_CHECKLIST_DIR);
        if (dir != null) {
            File file = new File(dir);
            if (file.isDirectory())
                checklistDir = file;
            else
                checklistDir = null;
        } else
            checklistDir = null;
    }

    public Component getContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createChecklistPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private Component createChecklistPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setTopComponent(new JScrollPane(getCheckList()));
        descriptionArea = new JTextArea(4, 0);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        splitPane.setBottomComponent(new JScrollPane(descriptionArea));
        return splitPane;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        insertButton = UIUtils.createInsertButton();
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getWindow().setVisible(false);
                getRecorder().insertChecklist(((File) checkList.getSelectedValue()).getName());
            }
            
        });
        insertButton.setEnabled(false);
        buttonPanel.add(insertButton);
        return buttonPanel;
    }

    private JList getCheckList() {
        checkList = new JList();
        checkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        checkList.setBorder(BorderFactory.createTitledBorder("Check Lists"));
        model = new CheckListFileModel();
        checkList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                String name = ((File) value).getName();
                name = name.substring(0, name.length() - 4);
                return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            }
        });
        checkList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                if (checkList.getSelectedIndex() == -1)
                    insertButton.setEnabled(false);
                else {
                    insertButton.setEnabled(true);

                }
            }
        });
        checkList.setModel(model);
        return checkList;
    }

    public String getName() {
        return "Checklists";
    }

    public void setComponent(Component component, Point point, boolean isTriggered) {
    }

}
