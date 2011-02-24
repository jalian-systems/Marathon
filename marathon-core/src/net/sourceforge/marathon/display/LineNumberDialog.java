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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.util.EscapeDialog;

public class LineNumberDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private JTextField lineNumberField = new JTextField();
    private int lineNumber = -1;
    private JButton gotoButton;
    private int lastLine;

    public LineNumberDialog(Frame owner) {
        super(owner, "Goto Line", true);
        JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(200, 70));
        getContentPane().add(content);
        content.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        JLabel lineNumLabel = new JLabel("Line Number:   ");
        inputPanel.add(lineNumLabel);
        lineNumberField.setPreferredSize(new Dimension(100, 20));
        lineNumberField.setMaximumSize(lineNumberField.getPreferredSize());
        lineNumberField.getDocument().addDocumentListener(new DocumentListener() {

            public void removeUpdate(DocumentEvent e) {
                updateGoto();
            }

            public void insertUpdate(DocumentEvent e) {
                updateGoto();
            }

            public void changedUpdate(DocumentEvent e) {
                updateGoto();
            }

            private void updateGoto() {
                int l = -1;
                try {
                    l = Integer.parseInt(lineNumberField.getText());
                } catch (NumberFormatException e) {
                    l = -1;
                }
                gotoButton.setEnabled(l >= 1 && l <= lastLine);
            }
        });
        inputPanel.add(lineNumberField);
        content.add(inputPanel, BorderLayout.CENTER);
        // Add button panel
        Dimension buttonSize = new Dimension(90, 25);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        gotoButton = new JButton();
        gotoButton.setPreferredSize(buttonSize);
        gotoButton.setMaximumSize(buttonSize);
        gotoButton.setAction(new AbstractAction("Goto") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                try {
                    lineNumber = Integer.parseInt(lineNumberField.getText());
                } catch (NumberFormatException e1) {
                    // if the number entered is not valid, just return so the
                    // user can enter a valid value
                    return;
                }
                setVisible(false);
            }
        });
        JButton cancelButton = new JButton();
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.setMaximumSize(buttonSize);
        cancelButton.setAction(new AbstractAction("Cancel") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                lineNumber = -1;
                setVisible(false);
            }
        });
        setCloseButton(cancelButton);
        buttonPanel.add(gotoButton);
        buttonPanel.add(cancelButton);
        content.add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(gotoButton);
        pack();
    }

    public void setVisible(boolean b) {
        lineNumberField.setText(lineNumber + "");
        lineNumberField.selectAll();
        lineNumberField.requestFocusInWindow();
        super.setVisible(b);
    }

    // lineNumber will be null if the dialog was canceled
    public int getLineNumber() {
        return lineNumber;
    }

    public void setMaxLineNumber(int lastLine) {
        this.lastLine = lastLine;
    }

    public void setLine(int line) {
        this.lineNumber = line;
    }
}
