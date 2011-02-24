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
package net.sourceforge.marathon.examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CheckBoxList extends JList {
    private static final long serialVersionUID = 1L;

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public CheckBoxList() {
        setCellRenderer(new CellRenderer());

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());

                if (index != -1) {
                    JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    repaint();
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    protected class CellRenderer implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JCheckBox checkbox = (JCheckBox) value;
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return checkbox;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            private JCheckBox[] boxes;

            public void run() {
                boxes = new JCheckBox[10];
                for (int i = 0; i < boxes.length; i++) {
                    boxes[i] = new JCheckBox("Checkbox - " + i);
                }
                initUI();
            }

            private void initUI() {
                JFrame frame = new JFrame("CheckBoxList");
                CheckBoxList list = new CheckBoxList();
                list.setModel(new AbstractListModel() {
                    private static final long serialVersionUID = 1L;

                    public int getSize() {
                        return boxes.length;
                    }

                    public Object getElementAt(int index) {
                        return boxes[index];
                    }
                });
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(list, BorderLayout.NORTH);
                panel.add(new JButton("Click Me"), BorderLayout.SOUTH);
                frame.getContentPane().add(panel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}