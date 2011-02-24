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
package net.sourceforge.marathon.screencapture;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import net.sourceforge.marathon.screencapture.ImagePanel.Annotation;

public class AnnotationPanel extends JTable {
    private final class AnnotationRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private JTextArea component = new JTextArea();

        public AnnotationRenderer() {
            component.setWrapStyleWord(true);
            component.setLineWrap(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            component.setText(((Annotation) value).getText());
            Dimension d = component.getSize();
            d.width = table.getWidth();
            component.setSize(d);
            table.setRowHeight(row, component.getPreferredSize().height);
            if (isSelected) {
                component.setBackground(getSelectionBackground());
                component.setForeground(getSelectionForeground());
            } else {
                if (row % 2 == 0) {
                    component.setBackground(new Color(0xFE, 0xF4, 0x9C));
                } else {
                    component.setBackground(new Color(0xA6, 0xE9, 0xF4));
                }
                component.setForeground(getForeground());
            }
            return component;
        }

    }

    private final class AnnotationEditor extends AbstractCellEditor implements TableCellEditor {
        private static final long serialVersionUID = 1L;

        private JTextArea component = new JTextArea();

        private Annotation annotation;

        private int row;

        private JTable table;

        public AnnotationEditor() {
            component.setWrapStyleWord(true);
            component.setLineWrap(true);
            component.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                    new AbstractAction("Enter") {
                        private static final long serialVersionUID = 1L;

                        public void actionPerformed(ActionEvent e) {
                            if (AnnotationPanel.this.isEditing() && row == AnnotationPanel.this.getEditingRow()) {
                                stopCellEditing();
                            }
                        }
                    });
            component.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    checkSize();
                }

                public void insertUpdate(DocumentEvent e) {
                    checkSize();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkSize();
                }

                private void checkSize() {
                    if (AnnotationPanel.this.isEditing() && row == AnnotationPanel.this.getEditingRow()) {
                        Dimension d = component.getSize();
                        d.width = table.getWidth();
                        component.setSize(d);
                        AnnotationPanel.this.setRowHeight(row, component.getPreferredSize().height);
                    }
                }

            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            annotation = (Annotation) value;
            component.setText(annotation.getText());
            Dimension d = component.getSize();
            d.width = table.getWidth();
            component.setSize(d);
            table.setRowHeight(row, component.getPreferredSize().height);
            if (row % 2 == 0) {
                component.setBackground(new Color(0xFE, 0xF4, 0x9C));
            } else {
                component.setBackground(new Color(0xA6, 0xE9, 0xF4));
            }
            component.setForeground(getForeground());
            this.row = row;
            this.table = table;
            return component;
        }

        public Object getCellEditorValue() {
            annotation.setText(component.getText());
            return annotation;
        }

        public void requestFocus() {
            component.requestFocusInWindow();
        }

        public boolean isCellEditable(EventObject e) {
            if (e instanceof MouseEvent)
                return ((MouseEvent) e).getClickCount() >= 2;
            return true;
        }
    }

    private final class AnnotationTableModel extends AbstractTableModel implements IAnnotationListener {
        private static final long serialVersionUID = 1L;
        private final ImagePanel imagePanel;
        private ArrayList<Annotation> annotations;

        public AnnotationTableModel(ImagePanel imagePanel) {
            this.imagePanel = imagePanel;
            this.imagePanel.addAnnotationListener(this);
            this.annotations = imagePanel.getAnnotations();
        }

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return annotations.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return annotations.get(rowIndex);
        }

        public String getColumnName(int column) {
            return "Annotations";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public Class<Annotation> getColumnClass(int columnIndex) {
            return Annotation.class;
        }

        public void annotationRemoved() {
            fireTableDataChanged();
        }

        public void annotationSelected(Annotation annotation) {
            int row = annotations.indexOf(annotation);
            if (row != -1)
                AnnotationPanel.this.getSelectionModel().setSelectionInterval(row, row);
        }

        public void annotationAdded(Annotation annotation) {
            fireTableDataChanged();
            int row = annotations.indexOf(annotation);
            AnnotationPanel.this.editCellAt(row, 0);
            AnnotationEditor editor = (AnnotationEditor) AnnotationPanel.this.getCellEditor();
            if (editor != null)
                editor.requestFocus();
        }
    }

    private static final long serialVersionUID = 1L;

    public AnnotationPanel(final ImagePanel imagePanel, boolean readOnly) {
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setModel(new AnnotationTableModel(imagePanel));
        if (!readOnly)
            setDefaultEditor(Annotation.class, new AnnotationEditor());
        setDefaultRenderer(Annotation.class, new AnnotationRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                int index = getSelectedRow();
                if (index == -1)
                    return;
                imagePanel.setSelectedAnnotation((Annotation) imagePanel.getAnnotations().get(index), false);
            }
        });
    }
}
