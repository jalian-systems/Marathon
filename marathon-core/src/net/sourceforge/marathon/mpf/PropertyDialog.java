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
package net.sourceforge.marathon.mpf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PropertyDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private JTextField propertyField = new JTextField(30);
    private JTextField valueField = new JTextField(30);
    private String key = null;
    private String value = null;
    private JButton okButton = null;
    private JButton cancelButton;

    public PropertyDialog(JDialog parent) {
        super(parent, "Create Property", true);
        setLocationRelativeTo(parent);
        FormLayout layout = new FormLayout("3dlu, left:pref:grow, 3dlu, pref:grow, 3dlu",
                "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints constraints = new CellConstraints();
        builder.addLabel("Property name:", constraints.xy(2, 2));
        builder.add(propertyField, constraints.xy(4, 2));
        propertyField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                okButton.setEnabled(propertyField.getText().length() > 0);
            }

            public void insertUpdate(DocumentEvent e) {
                okButton.setEnabled(propertyField.getText().length() > 0);
            }

            public void removeUpdate(DocumentEvent e) {
                okButton.setEnabled(propertyField.getText().length() > 0);
            }
        });
        builder.addLabel("Value:", constraints.xy(2, 4));
        builder.add(valueField, constraints.xy(4, 4));
        okButton = UIUtils.createOKButton();
        okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                key = propertyField.getText();
                value = valueField.getText();
                dispose();
            }
        });
        cancelButton = UIUtils.createCancelButton();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
        builder.add(buttonPanel, constraints.xyw(2, 6, 3));
        getContentPane().add(builder.getPanel());
        pack();
    }

    public PropertyDialog(JDialog parent, Property property) {
        this(parent);
        propertyField.setText(property.getProperty());
        valueField.setText(property.getValue());
    }

    public Property getProperty() {
        setVisible(true);
        if (key == null)
            return null;
        return new Property(key, value);
    }

    public String getValue() {
        return value;
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}
