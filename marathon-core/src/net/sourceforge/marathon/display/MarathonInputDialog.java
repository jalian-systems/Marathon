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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.util.EscapeDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class MarathonInputDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private boolean ok;
    private JTextField inputField;
    private JLabel errorMsgLabel;
    private JButton okButton;

    private JButton cancelButton;

    public MarathonInputDialog(JFrame parent, String title) {
        super(parent, title, true);
        initialize();
    }

    private void initialize() {
        FormLayout layout = new FormLayout("3dlu, pref, 3dlu, pref:grow",
                "pref, 3dlu, pref, 3dlu, pref, fill:4dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();
        CellConstraints cc1 = new CellConstraints();

        errorMsgLabel = new JLabel("");
        errorMsgLabel.setIcon(new ImageIcon(MarathonInputDialog.class.getClassLoader().getResource(
                "net/sourceforge/marathon/display/icons/enabled/error.gif")));
        errorMsgLabel.setVisible(false);

        int row = 1;
        inputField = new JTextField(15);
        builder.addLabel(getFieldLabel(), cc.xy(2, row), inputField, cc1.xy(4, row));
        row += 2;
        okButton = createOKButton();
        okButton.setEnabled(false);
        ok = false;
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                String errorMessage = validateInput(inputField.getText());
                if (errorMessage != null) {
                	errorMsgLabel.setText(errorMessage);
                	okButton.setEnabled(false);
                	errorMsgLabel.setVisible(true);
                } else {
                	okButton.setEnabled(true);
                	errorMsgLabel.setVisible(false);
                }
            }

            public void insertUpdate(DocumentEvent e) {
                String errorMessage = validateInput(inputField.getText());
                if (errorMessage != null) {
                	errorMsgLabel.setText(errorMessage);
                	okButton.setEnabled(false);
                	errorMsgLabel.setVisible(true);
                } else {
                	okButton.setEnabled(true);
                	errorMsgLabel.setVisible(false);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                String errorMessage = validateInput(inputField.getText());
                if (errorMessage != null) {
                	errorMsgLabel.setText(errorMessage);
                	okButton.setEnabled(false);
                	errorMsgLabel.setVisible(true);
                } else {
                	okButton.setEnabled(true);
                	errorMsgLabel.setVisible(false);
                }
            }

        };
        inputField.getDocument().addDocumentListener(documentListener);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        });
        cancelButton = createCancelButton();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        builder.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xyw(2, row, 3));
        builder.add(errorMsgLabel, cc.xyw(1, row + 2, 4));

        getContentPane().add(builder.getPanel());

        pack();
        setLocationRelativeTo(getParent());
    }

    protected abstract String getFieldLabel();

	protected abstract JButton createOKButton() ;

	protected abstract JButton createCancelButton();

	protected abstract String validateInput(String inputText) ;

    public boolean isOk() {
		return ok;
	}
    
    public String getValue() {
    	return inputField.getText();
    }
    
    public void setValue(String text) {
    	inputField.setText(text);
    }
    
    public JTextField getInputField() {
		return inputField;
	}
    
    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}