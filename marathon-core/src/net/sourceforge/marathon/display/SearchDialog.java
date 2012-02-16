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

package net.sourceforge.marathon.display;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.ISearchDialog;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SearchDialog extends EscapeDialog implements ISearchDialog {
    private static final long serialVersionUID = 1L;
    private JComboBox findCombo;
    private JComboBox replaceCombo;
    private JRadioButton forwardDirection;
    private JRadioButton backwardDirection;
    private JRadioButton allLines;
    private JRadioButton selectedLines;
    private JCheckBox caseSensitive;
    private JCheckBox wrapSearch;
    private JCheckBox wholeWord;
    private JCheckBox regularExpressions;
    private JButton close;
    private JButton find;
    private JButton replaceFind;
    private JButton replace;
    private JButton replaceAll;
    private IEditor editor;
    private JLabel messageLabel;

    public static final ImageIcon ERROR_ICON = new ImageIcon(SearchDialog.class.getResource("icons/enabled/error.gif"));
    public static final ImageIcon WARN_ICON = new ImageIcon(SearchDialog.class.getResource("icons/enabled/warn.gif"));

    public SearchDialog(IEditor editor, JFrame parent) {
        super(parent, "Find/Replace", false);
        this.editor = editor;
        setAlwaysOnTop(true);
        initComponents();
        getContentPane().add(buildPanel());
        pack();
        setLocationRelativeTo(getParent());
        updateButtons();
    }

    @Override public void setVisible(boolean b) {
        findCombo.requestFocusInWindow();
        super.setVisible(b);
    }

    private void initComponents() {
        findCombo = new JComboBox();
        findCombo.setEditable(true);
        findCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateButtons();
            }
        });
        Component c = findCombo.getEditor().getEditorComponent();
        if (c instanceof JTextField) {
            ((JTextField) c).getDocument().addDocumentListener(new DocumentListener() {
                public void removeUpdate(DocumentEvent e) {
                    updateButtons();
                }

                public void insertUpdate(DocumentEvent e) {
                    updateButtons();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateButtons();
                }
            });
        }
        replaceCombo = new JComboBox();
        replaceCombo.setEditable(true);
        forwardDirection = new JRadioButton("Forward");
        forwardDirection.setMnemonic(KeyEvent.VK_O);
        backwardDirection = new JRadioButton("Backward");
        backwardDirection.setMnemonic(KeyEvent.VK_A);
        forwardDirection.setSelected(true);
        ButtonGroup g = new ButtonGroup();
        g.add(forwardDirection);
        g.add(backwardDirection);
        allLines = new JRadioButton("All");
        allLines.setSelected(true);
        allLines.setMnemonic(KeyEvent.VK_L);
        selectedLines = new JRadioButton("Selected lines");
        selectedLines.setMnemonic(KeyEvent.VK_E);
        g = new ButtonGroup();
        g.add(allLines);
        g.add(selectedLines);
        caseSensitive = new JCheckBox("Case sensitive");
        caseSensitive.setMnemonic(KeyEvent.VK_C);
        wrapSearch = new JCheckBox("Wrap search");
        wrapSearch.setMnemonic(KeyEvent.VK_P);
        wholeWord = new JCheckBox("Whole word");
        wholeWord.setMnemonic(KeyEvent.VK_W);
        regularExpressions = new JCheckBox("Regular expressions");
        regularExpressions.setMnemonic(KeyEvent.VK_R);
        regularExpressions.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                forwardDirection.setEnabled(!regularExpressions.isSelected());
                backwardDirection.setEnabled(!regularExpressions.isSelected());
                caseSensitive.setEnabled(!regularExpressions.isSelected());
                wholeWord.setEnabled(!regularExpressions.isSelected());
            }
        });
        close = UIUtils.createCloseButton();
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeSearch();
                setVisible(false);
            }
        });
        find = UIUtils.createFindButton();
        find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                find();
            }
        });
        replaceFind = UIUtils.createReplaceFindButton();
        replaceFind.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replaceFind();
            }
        });
        replace = UIUtils.createReplaceButton();
        replace.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replace();
            }
        });
        replaceAll = UIUtils.createReplaceAllButton();
        replaceAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replaceAll();
            }
        });
        messageLabel = new JLabel();
    }

    protected void updateButtons() {
        String findText = (String) findCombo.getEditor().getItem();
        if (findText == null || findText.length() == 0) {
            setFindButtonsState(false);
        } else {
            setFindButtonsState(true);
        }
    }

    private void setFindButtonsState(boolean b) {
        find.setEnabled(b);
        replaceFind.setEnabled(b);
        replace.setEnabled(b);
        replaceAll.setEnabled(b);
    }

    private Component buildPanel() {
        FormLayout layout = new FormLayout("pref:grow, 3dlu, pref:grow", "pref, pref, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        builder.add(buildTextPanel(), cc.xyw(1, 1, 3));
        builder.add(buildDirectionOrScopePanel("Direction", forwardDirection, backwardDirection), cc.xyw(1, 2, 1));
        builder.add(buildDirectionOrScopePanel("Scope", allLines, selectedLines), cc.xyw(3, 2, 1));
        builder.add(buildOptionsPanel(), cc.xyw(1, 3, 3));
        builder.add(buildButtonBar(), cc.xyw(1, 5, 3));
        builder.add(messageLabel, cc.xyw(1, 7, 3));
        builder.add(ButtonBarFactory.buildRightAlignedBar(close), cc.xyw(1, 9, 3));
        return builder.getPanel();
    }

    private Component buildButtonBar() {
        FormLayout layout = new FormLayout("d:grow, d, 3dlu, d", "pref, 3dlu, pref");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        builder.add(find, cc.xyw(2, 1, 1));
        builder.add(replaceFind, cc.xyw(4, 1, 1));
        builder.add(replace, cc.xyw(2, 3, 1));
        builder.add(replaceAll, cc.xyw(4, 3, 1));
        return builder.getPanel();
    }

    private Component buildTextPanel() {
        FormLayout layout = new FormLayout("left:pref, 3dlu, min:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("&Find:", findCombo);
        builder.append("&Replace with:", replaceCombo);
        return builder.getPanel();
    }

    private Component buildDirectionOrScopePanel(String text, JRadioButton fd, JRadioButton bd) {
        FormLayout layout = new FormLayout("pref:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator(text);
        builder.append(fd);
        builder.append(bd);
        return builder.getPanel();
    }

    private Component buildOptionsPanel() {
        FormLayout layout = new FormLayout("pref, 3dlu, pref:grow", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        builder.addSeparator("Options", cc.xyw(1, 1, 3));
        builder.add(caseSensitive, cc.xyw(1, 3, 1));
        builder.add(wrapSearch, cc.xyw(3, 3, 1));
        builder.add(wholeWord, cc.xyw(1, 5, 1));
        builder.add(regularExpressions, cc.xyw(3, 5, 1));
        return builder.getPanel();
    }

    protected void closeSearch() {
        editor.closeSearch();
    }

    protected void find() {
        addToCombo(findCombo);
        int b;
        if (regularExpressions.isSelected())
            b = editor.find(findCombo.getSelectedItem().toString(), true, allLines.isSelected(), true, wrapSearch.isSelected(),
                    false, regularExpressions.isSelected());
        else
            b = editor.find(findCombo.getSelectedItem().toString(), forwardDirection.isSelected(), allLines.isSelected(),
                    caseSensitive.isSelected(), wrapSearch.isSelected(), wholeWord.isSelected(), regularExpressions.isSelected());
        updateMessage(b);
        updateButtons();
    }

    private void updateMessage(int b) {
        if (b == IEditor.FIND_SUCCESS) {
            messageLabel.setText("");
            messageLabel.setIcon(null);
        } else if (b == IEditor.FIND_FAILED) {
            messageLabel.setText("Search string not found");
            messageLabel.setIcon(ERROR_ICON);
        } else if (b == IEditor.FIND_WRAPPED) {
            messageLabel.setText("Search wrapped");
            messageLabel.setIcon(WARN_ICON);
        }
    }

    private void addToCombo(JComboBox combo) {
        int n = combo.getItemCount();
        Object selectedItem = combo.getSelectedItem();
        if (selectedItem == null)
            selectedItem = "";
        for (int i = 0; i < n; i++) {
            if (combo.getItemAt(i).equals(selectedItem))
                return;
        }
        combo.addItem(selectedItem);
    }

    protected void replaceFind() {
        addToCombo(findCombo);
        addToCombo(replaceCombo);
        String findText = "";
        if (findCombo.getSelectedItem() != null)
            findText = findCombo.getSelectedItem().toString();
        String replaceText = "";
        if (replaceCombo.getSelectedItem() != null)
            replaceText = replaceCombo.getSelectedItem().toString();
        int b;
        if (regularExpressions.isSelected())
            b = editor.replaceFind(findText, replaceText, true, allLines.isSelected(), true, wrapSearch.isSelected(), false,
                    regularExpressions.isSelected());
        else
            b = editor.replaceFind(findText, replaceText, forwardDirection.isSelected(), allLines.isSelected(),
                    caseSensitive.isSelected(), wrapSearch.isSelected(), wholeWord.isSelected(), regularExpressions.isSelected());
        updateMessage(b);
    }

    protected void replace() {
        addToCombo(findCombo);
        addToCombo(replaceCombo);
        String findText = "";
        if (findCombo.getSelectedItem() != null)
            findText = findCombo.getSelectedItem().toString();
        String replaceText = "";
        if (replaceCombo.getSelectedItem() != null)
            replaceText = replaceCombo.getSelectedItem().toString();
        editor.replace(findText, replaceText, forwardDirection.isSelected(), allLines.isSelected(), caseSensitive.isSelected(),
                wrapSearch.isSelected(), wholeWord.isSelected(), regularExpressions.isSelected());
    }

    protected void replaceAll() {
        addToCombo(findCombo);
        addToCombo(replaceCombo);
        if (regularExpressions.isSelected())
            editor.replaceAll(findCombo.getSelectedItem().toString(), replaceCombo.getSelectedItem().toString(), true, false,
                    regularExpressions.isSelected());
        else
            editor.replaceAll(findCombo.getSelectedItem().toString(), replaceCombo.getSelectedItem().toString(),
                    caseSensitive.isSelected(), wholeWord.isSelected(), regularExpressions.isSelected());
    }

    public void setSearchText(String selectedText) {
        findCombo.setSelectedItem(selectedText);
    }

    public void setSelectedLine(boolean b) {
        selectedLines.setSelected(b);
    }

    public void setEditor(IEditor editor) {
        this.editor = editor;
    }

    @Override public JButton getOKButton() {
        return find;
    }

    @Override public JButton getCloseButton() {
        return close;
    }
}
