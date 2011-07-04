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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.EscapeDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FixtureDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private boolean ok;
    private JTextField className;
    private JLabel errorMsgLabel;
    private String errorMessage;
    private JTextArea description;
    private JTextArea programArguments;
    private JButton okButton;

    private static final ImageIcon OK_ICON = new ImageIcon(FixtureDialog.class.getResource("icons/enabled/ok.gif"));;
    private static final ImageIcon CANCEL_ICON = new ImageIcon(FixtureDialog.class.getResource("icons/enabled/cancel.gif"));

    public FixtureDialog(JFrame parent) {
        super(parent, "Create New Fixture", true);
        initialize();
    }

    private void initialize() {
        FormLayout layout = new FormLayout("3dlu, pref, 3dlu, pref:grow",
                "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:4dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();
        CellConstraints cc1 = new CellConstraints();

        errorMessage = "";
        errorMsgLabel = new JLabel("");
        errorMsgLabel.setIcon(new ImageIcon(FixtureDialog.class.getClassLoader().getResource(
                "net/sourceforge/marathon/display/icons/enabled/error.gif")));
        errorMsgLabel.setVisible(false);

        int row = 1;
        className = new JTextField(15);
        className.setText(System.getProperty(Constants.PROP_APPLICATION_MAINCLASS));
        builder.addLabel("Main class name: ", cc.xy(2, row, "left, top"), className, cc1.xy(4, row));
        row += 2;
        description = new JTextArea(5, 40);
        description.setLineWrap(true);
        builder.add(new JLabel("Description: "), cc.xy(2, row, "left, top"));
        builder.add(new JScrollPane(description), cc1.xywh(4, row, 1, 1));
        row += 2;
        programArguments = new JTextArea(5, 40);
        programArguments.setLineWrap(true);
        builder.add(new JLabel("Program Arguments: "), cc.xy(2, row, "left, top"));
        builder.add(new JScrollPane(programArguments), cc1.xywh(4, row, 1, 1));
        row += 2;
        okButton = new JButton("OK", OK_ICON);
        okButton.setEnabled(true);
        ok = false;
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                validateClassName();
            }

            public void insertUpdate(DocumentEvent e) {
                validateClassName();
            }

            public void removeUpdate(DocumentEvent e) {
                validateClassName();
            }

        };
        className.getDocument().addDocumentListener(documentListener);
        programArguments.getDocument().addDocumentListener(documentListener);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel", CANCEL_ICON);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        getRootPane().setDefaultButton(okButton);
        setCloseButton(cancelButton);
        builder.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xyw(2, row, 3));
        builder.add(errorMsgLabel, cc.xyw(1, 6, 4));

        getContentPane().add(builder.getPanel());

        pack();
        setLocationRelativeTo(getParent());
    }

    private void validateClassName() {
        if (isValidClassName(className.getText(), programArguments.getText())) {
            okButton.setEnabled(true);
            errorMsgLabel.setVisible(false);
        } else {
            okButton.setEnabled(false);
            errorMsgLabel.setVisible(true);
        }
        errorMsgLabel.setText(errorMessage);
    }

    private boolean isValidClassName(String className, String args) {
        if (className.contains("..")) {
            errorMessage = "Invalid class name";
            return false;
        }
        String[] parts = className.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (!isValidJavaIdentifier(parts[i])) {
                errorMessage = "Invalid class name";
                return false;
            }
        }
        if (args.contains("\n")) {
            errorMessage = "Error in arguments. New lines are not supported.";
            return false;
        }
        return true;
    }

    private boolean isValidJavaIdentifier(String name) {
        char[] cs = name.toCharArray();
        if (cs.length == 0)
            return false;
        if (!Character.isJavaIdentifierStart(cs[0]))
            return false;
        for (int i = 1; i < cs.length; i++) {
            if (!Character.isJavaIdentifierPart(cs[i]))
                return false;
        }
        return true;
    }

    public boolean isOk() {
        return ok;
    }

    public String getDescription() {
        return description.getText();
    }

    public String getClassName() {
        return className.getText();
    }

    public String getProgramArguments() {
        return programArguments.getText();
    }
}