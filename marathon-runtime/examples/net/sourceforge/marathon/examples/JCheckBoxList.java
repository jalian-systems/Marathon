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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

public class JCheckBoxList extends JList implements ISelectionProvider {
    private static final long serialVersionUID = 1L;

    private Dimension checkBoxSize = new JCheckBox().getPreferredSize();

    public JCheckBoxList() {
        super();
        initialize();
    }

    public JCheckBoxList(ListModel dataModel) {
        super(dataModel);
        initialize();
    }

    public JCheckBoxList(final Object[] data) {
        super(data);
        initialize();
    }

    public JCheckBoxList(final Vector<?> listData) {
        super(listData);
        initialize();
    }

    private void initialize() {
        setCellRenderer(new JCheckBoxListCellRenderer(this));
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                Rectangle cellBounds = getCellBounds(index, index);
                Rectangle rect = new Rectangle(cellBounds.x, cellBounds.y, checkBoxSize.width, checkBoxSize.height);
                if (rect.contains(e.getPoint())) {
                    toggleSelection(index);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("CheckBoxList");

                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(new JCheckBoxList(new String[] { "One", "Two", "Three" }));
                frame.getContentPane().add(panel);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    private class State {
        boolean state;
    }

    private Vector<State> selectionState = new Vector<State>(10, 5);

    public boolean isSelected(int index) {
        if (index >= selectionState.size()) {
            for (int i = selectionState.size(); i <= index; i++)
                selectionState.add(new State());
        }
        return selectionState.get(index).state;
    }

    private void toggleSelection(int index) {
        if (index >= selectionState.size()) {
            for (int i = selectionState.size(); i <= index; i++)
                selectionState.add(new State());
        }
        State state = selectionState.get(index);
        state.state = !state.state;
        repaint(getCellBounds(index, index));
    }
}