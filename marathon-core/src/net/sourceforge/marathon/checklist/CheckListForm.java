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
package net.sourceforge.marathon.checklist;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.sourceforge.marathon.checklist.CheckList.CommentBox;
import net.sourceforge.marathon.checklist.CheckList.FailureNote;
import net.sourceforge.marathon.checklist.CheckList.Header;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CheckListForm extends JPanel {
    private static final long serialVersionUID = 1L;

    private transient DefaultFormBuilder builder;
    private CheckList checkList;

    private final Mode mode;

    private boolean dirty = false;

    public static enum Mode {
        DISPLAY(false, false), EDIT(true, false), ENTER(false, true);
        private boolean selectable;
        private boolean editable;

        Mode(boolean selectable, boolean editable) {
            this.setSelectable(selectable);
            this.setEditable(editable);
        }

        public void setSelectable(boolean selectable) {
            this.selectable = selectable;
        }

        public boolean isSelectable() {
            return selectable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isEditable() {
            return editable;
        }

    }

    public CheckListForm(CheckList checklist, Mode mode) {
        this.mode = mode;
        this.checkList = checklist;
        buildPanel();
    }

    private void buildPanel() {
        FormLayout layout = new FormLayout("pref:grow");
        builder = new DefaultFormBuilder(layout, this);
        builder.setDefaultDialogBorder();
        if (mode.isSelectable()) {
            builder.append("Name");
            builder.append(getNameField());
            builder.append("Description");
            JScrollPane pane = new JScrollPane(getDescriptionField());
            pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            builder.append(pane);
        } else {
            if (checkList.getName().equals(""))
                builder.appendSeparator("<No Name>");
            else
                builder.appendSeparator(checkList.getName());
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0, 1));
            panel.setBackground(Color.LIGHT_GRAY);
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            String text = checkList.getDescription();
            if (text.equals(""))
                text = "<No Description>";
            StringTokenizer tok = new StringTokenizer(text, "\n");
            while (tok.hasMoreTokens()) {
                JLabel label = new JLabel(tok.nextToken());
                panel.add(label);
            }
            builder.append(panel);
            builder.appendRow("3dlu");
            builder.nextRow();
        }
        Iterator<CheckList.CheckListItem> items = checkList.getItems();
        while (items.hasNext())
            builder.append(items.next().getPanel(mode.isSelectable(), mode.isEditable()));
    }

    private JTextArea getDescriptionField() {
        JTextArea textArea = new JTextArea(checkList.getDescription(), 5, 0);
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateDescription(e);
            }

            public void insertUpdate(DocumentEvent e) {
                updateDescription(e);
            }

            public void removeUpdate(DocumentEvent e) {
                updateDescription(e);
            }

            private void updateDescription(DocumentEvent e) {
                dirty = true;
                Document document = e.getDocument();
                try {
                    checkList.setDescription(document.getText(0, document.getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

        });
        textArea.setEditable(mode.isSelectable());
        return textArea;
    }

    private JTextField getNameField() {
        JTextField textField = new JTextField(checkList.getName(), 30);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateName(e);
            }

            public void insertUpdate(DocumentEvent e) {
                updateName(e);
            }

            public void removeUpdate(DocumentEvent e) {
                updateName(e);
            }

            private void updateName(DocumentEvent e) {
                dirty = true;
                Document document = e.getDocument();
                try {
                    checkList.setName(document.getText(0, document.getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

        });
        textField.setEditable(mode.isSelectable());
        return textField;
    }

    public void addTextArea(String label) {
        CommentBox commentBox = checkList.createCommentBox(label);
        builder.append(commentBox.getPanel(mode.isSelectable(), mode.isEditable()));
    }

    public void addChecklistItem(String label) {
        FailureNote failureNote = checkList.createFailureNote(label);
        builder.append(failureNote.getPanel(mode.isSelectable(), mode.isEditable()));
    }

    public void addHeader(String label) {
        Header header = checkList.createHeader(label);
        builder.append(header.getPanel(mode.isSelectable(), mode.isEditable()));
    }

    public CheckList getCheckList() {
        return checkList;
    }

    public void deleteSelected() {
        checkList.deleteSelected();
        rebuildPanel();
    }

    private void rebuildPanel() {
        removeAll();
        buildPanel();
        repaint();
    }

    public void moveUpSelected() {
        checkList.moveUpSelected();
        rebuildPanel();
    }

    public void moveDownSelected() {
        checkList.moveDownSelected();
        rebuildPanel();
    }

    public boolean isSelectable() {
        return mode.isSelectable();
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isDirty() {
        return dirty;

    }
}
