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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;
import net.sourceforge.marathon.util.ValidationUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class IgnoreComponentsPanel extends ListPanel {
    public IgnoreComponentsPanel(JDialog parent) {
        super(parent);
    }

    public boolean isTraversalNeeded() {
        return false;
    }

    public boolean isAddArchivesNeeded() {
        return false;
    }

    public boolean isAddClassesNeeded() {
        return true;
    }

    public boolean isAddFoldersNeeded() {
        return false;
    }

    public String getPropertyKey() {
        return Constants.PROP_IGNORE_COMPONENTS;
    }

    public String getName() {
        return "Ignore Components";
    }

    public Icon getIcon() {
        return null;
    }

    public boolean isValidInput() {
        return true;
    }

    protected String getClassPath() {
        StringBuffer cp = new StringBuffer("");
        int size = classpathListModel.getSize();
        if (size == 0)
            return cp.toString();
        for (int i = 0; i < size - 1; i++) {
            cp.append(classpathListModel.getElementAt(i));
            cp.append(";");
        }
        cp.append(classpathListModel.getElementAt(size - 1));
        return cp.toString();
    }

    public void setProperties(Properties props) {
        String cp = props.getProperty(getPropertyKey(), "");
        if (cp.length() == 0)
            return;
        String[] elements = cp.split(";");
        for (int i = 0; i < elements.length; i++) {
            classpathListModel.add(elements[i]);
        }
    }

    class InputDialog extends EscapeDialog {
        private static final long serialVersionUID = 1L;
        private JTextField className = new JTextField(30);
        private JCheckBox ignoreChildren = new JCheckBox("Ignore Children", false);
        private JButton okButton = UIUtils.createOKButton();
        private JButton cancelButton = UIUtils.createCancelButton();

        public InputDialog(String title, boolean modal) {
            super(IgnoreComponentsPanel.this.getParent(), title, modal);
            getContentPane().add(getClassNamePanel());
            JPanel buttonPanel = ButtonBarFactory.buildRightAlignedBar(new JButton[] { okButton, cancelButton });
            buttonPanel.setBorder(Borders.createEmptyBorder("0dlu, 0dlu, 3dlu, 7dlu"));
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            okButton.setEnabled(false);
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!ValidationUtil.isValidClassName(className.getText())) {
                        JOptionPane.showMessageDialog(InputDialog.this, "Invalid class name", "Class Name",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    dispose();
                }
            });
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    className.setText("");
                    dispose();
                }
            });
            className.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    setOKButtonState();
                }

                private void setOKButtonState() {
                    if (className.getText().length() > 0)
                        okButton.setEnabled(true);
                    else
                        okButton.setEnabled(false);
                }

                public void insertUpdate(DocumentEvent e) {
                    setOKButtonState();
                }

                public void removeUpdate(DocumentEvent e) {
                    setOKButtonState();
                }
            });
            pack();
            setLocationRelativeTo(IgnoreComponentsPanel.this.getParent());
        }

        private JPanel getClassNamePanel() {
            PanelBuilder builder = new PanelBuilder(new FormLayout("left:p:none, 3dlu, fill:p:grow", "pref,pref"));
            builder.setDefaultDialogBorder();
            CellConstraints constraints = new CellConstraints();
            builder.addLabel("Class Name:", constraints.xy(1, 1));
            builder.add(className, constraints.xy(3, 1));
            builder.add(ignoreChildren, constraints.xy(3, 2));
            return builder.getPanel();
        }

        public String getClassName() {
            return className.getText();
        }

        public boolean getIgnoreChildren() {
            return ignoreChildren.isSelected();
        }

        @Override public JButton getOKButton() {
            return okButton;
        }

        @Override public JButton getCloseButton() {
            return cancelButton;
        }
    };

    public String getClassName() {
        InputDialog dialog = new InputDialog("Ignore Component Details", true);
        dialog.setVisible(true);
        String className = dialog.getClassName();
        if (className.equals(""))
            return null;
        return className + "(ignorechildren:" + dialog.getIgnoreChildren() + ")";
    }

    public boolean isSingleSelection() {
        return true;
    }
}
