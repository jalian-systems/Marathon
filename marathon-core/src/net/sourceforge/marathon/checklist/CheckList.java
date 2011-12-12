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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.screencapture.ImagePanel;
import net.sourceforge.marathon.screencapture.ImagePanel.Annotation;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CheckList {
    public static abstract class CheckListItem {
        private transient JPanel panel;
        private String label;

        private static CheckListItem selectedItem;

        public CheckListItem(String label) {
            setLabel(label);
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public CheckListItem() {
            this.label = null;
        }

        protected void setMouseListener(JComponent c) {
            c.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedItem != null)
                        selectedItem.deselect();
                    selectedItem = CheckListItem.this;
                    CheckListItem.this.select();
                }
            });
        }

        protected abstract JPanel createPanel(boolean selectable, boolean editable);

        public JPanel getPanel(boolean selectable, boolean editable) {
            if (panel == null) {
                panel = createPanel(selectable, editable);
                if (selectable)
                    setMouseListener(panel);
            }
            return panel;
        }

        public void deselect() {
            panel.setBorder(BorderFactory.createEmptyBorder());
        }

        public void select() {
            panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        }

        public String getLabel() {
            return label;
        }

        public abstract String getType();

        public int getSelected() {
            return 0;
        }

        public String getText() {
            return null;
        }

        public void setSelected(int selected) {
        }

        public void setText(String text) {
        }
    }

    public static class FailureNote extends CheckListItem {
        private static final String TYPE = "checklist";
        private JTextArea textArea;
        private int selected = 0;
        private JRadioButton success;
        private JRadioButton fail;
        private JRadioButton notes;
        private String text = "";

        public FailureNote(String label) {
            super(label);
        }

        public FailureNote() {
        }

        protected JPanel createPanel(final boolean selectable, final boolean editable) {
            FormLayout layout = new FormLayout("pref,3dlu,pref:grow,pref,pref,pref,pref,pref,pref,pref", "pref,pref");
            final JPanel panel = new JPanel();
            DefaultFormBuilder builder = new DefaultFormBuilder(layout, panel);

            JLabel jlabel = new JLabel(getLabel());
            builder.append(jlabel);
            builder.nextColumn(2);
            ButtonGroup group = new ButtonGroup();
            success = new JRadioButton("Success");
            fail = new JRadioButton("Fail");
            notes = new JRadioButton("Notes");
            group.add(success);
            group.add(fail);
            group.add(notes);

            builder.append(success);
            builder.append(fail);
            builder.append(notes);

            textArea = new JTextArea();
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    try {
                        text = e.getDocument().getText(0, e.getDocument().getLength());
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    try {
                        text = e.getDocument().getText(0, e.getDocument().getLength());
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                public void removeUpdate(DocumentEvent e) {
                    try {
                        text = e.getDocument().getText(0, e.getDocument().getLength());
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

            });
            textArea.setRows(4);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            builder.append(scroll, 10);

            success.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (success.isSelected())
                        selected = 1;
                    textArea.setEnabled(!success.isSelected());
                    if (panel != null)
                        panel.repaint();
                }
            });
            fail.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (fail.isSelected())
                        selected = 3;
                    textArea.setEnabled(!success.isSelected());
                    if (panel != null)
                        panel.repaint();
                }
            });
            notes.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (notes.isSelected())
                        selected = 2;
                    textArea.setEnabled(!success.isSelected());
                    if (panel != null)
                        panel.repaint();
                }
            });
            success.setEnabled(editable);
            fail.setEnabled(editable);
            notes.setEnabled(editable);
            textArea.setEditable(editable);
            textArea.setText(text);
            if (selected == 1)
                success.setSelected(true);
            else if (selected == 3)
                fail.setSelected(true);
            else if (selected == 2)
                notes.setSelected(true);
            else
                success.setSelected(editable);

            if (selectable) {
                setMouseListener(success);
                setMouseListener(fail);
                setMouseListener(notes);
                setMouseListener(textArea);
                setMouseListener(jlabel);
            }
            return builder.getPanel();
        }

        @Override
        public String toString() {
            return "<failureNote label = \"" + getLabel() + "\" />";
        }

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void setSelected(int selected) {
            this.selected = selected;
        }

        @Override
        public int getSelected() {
            return selected;
        }

        @Override
        public void setText(String text) {
            this.text = text;
        }
    }

    public static class CommentBox extends CheckListItem {
        private static final String TYPE = "comments";
        private JTextArea textArea;
        private String text = "";

        public CommentBox(String label) {
            super(label);
        }

        public CommentBox() {
        }

        protected JPanel createPanel(boolean selectable, boolean editable) {
            FormLayout layout = new FormLayout("pref:grow", "pref, pref");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            JLabel jlabel = new JLabel(getLabel());
            builder.append(jlabel);
            textArea = new JTextArea();
            textArea.setRows(4);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            builder.append(scroll);
            JPanel panel = builder.getPanel();
            textArea.setEditable(editable);
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    try {
                        text = e.getDocument().getText(0, e.getDocument().getLength());
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    try {
                        text = e.getDocument().getText(0, e.getDocument().getLength());
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                public void removeUpdate(DocumentEvent e) {
                    try {
                        text = e.getDocument().getText(0, e.getDocument().getLength());
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

            });
            textArea.setText(text);
            if (selectable) {
                setMouseListener(jlabel);
                setMouseListener(textArea);
            }
            return panel;
        }

        @Override
        public String toString() {
            return "<commentBox label = \"" + getLabel() + "\" />";
        }

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Header extends CheckListItem {
        private static final String TYPE = "header";

        public Header(String label) {
            super(label);
        }

        public Header() {
        }

        @Override
        protected JPanel createPanel(boolean selectable, boolean editable) {
            FormLayout layout = new FormLayout("pref:grow", "pref");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.appendSeparator(getLabel());
            builder.appendUnrelatedComponentsGapRow();

            JPanel panel = builder.getPanel();
            return panel;
        }

        @Override
        public String toString() {
            return "<header label = \"" + getLabel() + "\" />";
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }

    private ArrayList<CheckList.CheckListItem> checkListItems;
    private String name;
    private String description;
    private String captureFile;
    private File dataFile;

    public CheckList() {
        checkListItems = new ArrayList<CheckList.CheckListItem>();
    }

    public static CheckList read(InputStream in) throws Exception {
        XMLDecoder decoder = new XMLDecoder(in);
        return (CheckList) decoder.readObject();
    }

    public void add(CheckListItem item) {
        checkListItems.add(item);
    }

    public void save(OutputStream out) {
        XMLEncoder encoder1;
        encoder1 = new XMLEncoder(out);
        encoder1.writeObject(this);
        encoder1.close();
    }

    public Iterator<CheckList.CheckListItem> getItems() {
        return checkListItems.iterator();
    }

    public CommentBox createCommentBox(String label) {
        CommentBox commentBox = new CommentBox(label);
        add(commentBox);
        return commentBox;
    }

    public FailureNote createFailureNote(String label) {
        FailureNote note = new FailureNote(label);
        add(note);
        return note;
    }

    public Header createHeader(String label) {
        Header header = new Header(label);
        add(header);
        return header;
    }

    public void deleteSelected() {
        if (CheckListItem.selectedItem != null) {
            CheckListItem.selectedItem.deselect();
            checkListItems.remove(CheckListItem.selectedItem);
            CheckListItem.selectedItem = null;
        }
    }

    public CheckListItem getSelected() {
        return CheckListItem.selectedItem;
    }

    public void moveUpSelected() {
        if (CheckListItem.selectedItem != null) {
            int index = checkListItems.indexOf(CheckListItem.selectedItem);
            if (index == -1 || index == 0)
                return;
            checkListItems.remove(index);
            checkListItems.add(index - 1, CheckListItem.selectedItem);
        }
    }

    public void moveDownSelected() {
        if (CheckListItem.selectedItem != null) {
            int index = checkListItems.indexOf(CheckListItem.selectedItem);
            if (index == -1 || index == checkListItems.size() - 1)
                return;
            checkListItems.remove(index);
            checkListItems.add(index + 1, CheckListItem.selectedItem);
        }
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCheckListItems(ArrayList<CheckList.CheckListItem> checkListItems) {
        this.checkListItems = checkListItems;
    }

    public ArrayList<CheckList.CheckListItem> getCheckListItems() {
        return checkListItems;
    }

    private String getStatus() {
        int status = 0;
        for (CheckListItem item : checkListItems) {
            if (status < item.getSelected())
                status = item.getSelected();
        }
        if (status == 3)
            return "Fail";
        else if (status == 2)
            return "Notes";
        return "OK";
    }

    public void saveXML(String indent, OutputStream baos, int index) {
        PrintWriter printWriter = new PrintWriter(baos);
        indent += "  ";
        printWriter.print(indent + "<checklist ");
        printWriter.print("name=\"" + quoteCharacters(getName()) + "\" ");
        printWriter.print("index=\"" + index + "\" ");
        printWriter.print("description=\"" + quoteCharacters(getDescription()) + "\" ");
        if (captureFile != null)
            printWriter.print("capture=\"" + captureFile + "\" ");
        printWriter.print("status=\"" + getStatus() + "\" ");
        printWriter.println(">");

        for (CheckListItem item : checkListItems) {
            printWriter.print("<checkitem type=\"" + item.getType() + "\" ");
            printWriter.print("label=\"" + quoteCharacters(item.getLabel()) + "\" ");
            int selected = item.getSelected();
            if (selected != 0)
                printWriter.print("selected=\"" + selected + "\" ");
            String text = item.getText();
            if (text == null) {
                printWriter.println("/>");
            } else {
                printWriter.println(" text=\"");
                printWriter.print(quoteCharacters(text));
                printWriter.println("\" />");
            }
        }
        if (captureFile != null) {
            File file = new File(System.getProperty(Constants.PROP_IMAGE_CAPTURE_DIR), captureFile);
            try {
                ImagePanel imagePanel = new ImagePanel(new FileInputStream(file), false);
                ArrayList<Annotation> annotations = imagePanel.getAnnotations();
                printWriter.println(indent + "  " + "<annotations>");
                for (Annotation a : annotations) {
                    printWriter.println(indent + "    " + "<annotation x=\"" + a.x + "\" y=\"" + a.y + "\" w=\"" + a.width
                            + "\" h=\"" + a.height + "\" text=\"" + quoteCharacters(a.getText()) + "\"/>");
                }
                printWriter.println(indent + "  " + "</annotations>");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        printWriter.println(indent + "</checklist>");
        printWriter.close();
    }

    private static String quoteCharacters(String s) {
        StringBuffer result = null;
        for (int i = 0, max = s.length(), delta = 0; i < max; i++) {
            char c = s.charAt(i);
            String replacement = null;

            if (c == '&') {
                replacement = "&amp;";
            } else if (c == '<') {
                replacement = "&lt;";
            } else if (c == '\r') {
                replacement = "&#13;";
            } else if (c == '>') {
                replacement = "&gt;";
            } else if (c == '"') {
                replacement = "&quot;";
            } else if (c == '\'') {
                replacement = "&apos;";
            }

            if (replacement != null) {
                if (result == null) {
                    result = new StringBuffer(s);
                }
                result.replace(i + delta, i + delta + 1, replacement);
                delta += (replacement.length() - 1);
            }
        }
        if (result == null) {
            return s;
        }
        return result.toString();
    }

    /* Make XMLEncoder happy by not using standard bean property get/set methods */
    public void setCaptureFile(String file) {
        this.captureFile = file;
    }

    /* Make XMLEncoder happy by not using standard bean property get/set methods */
    public void xsetDataFile(File file) {
        this.dataFile = file;
    }

    /* Make XMLEncoder happy by not using standard bean property get/set methods */
    public File xgetDataFile() {
        return dataFile;
    }

    public String getCaptureFile() {
        return captureFile;
    }
}
