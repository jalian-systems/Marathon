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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.util.EscapeDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FileSelectionDialog extends EscapeDialog implements IFileSelectedAction {
    private static final long serialVersionUID = 1L;
    private JTextField dirField = new JTextField(30);
    private String fileName = null;
    private JButton okButton;
    private JDialog parent;

    public FileSelectionDialog(JDialog parent, String fileType, String[] extensions) {
        super(parent, "Select File/Folder", true);
        this.parent = parent;
        FormLayout layout = new FormLayout("3dlu, left:pref:grow, 3dlu, pref:grow, 3dlu, fill:pref, 3dlu",
                "3dlu, pref, 3dlu, pref, 3dlu");
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints constraints = new CellConstraints();
        JLabel label = new JLabel("Name: ");
        builder.add(label, constraints.xy(2, 2));
        builder.add(dirField, constraints.xy(4, 2));
        dirField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateOKState();
            }

            public void insertUpdate(DocumentEvent e) {
                updateOKState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateOKState();
            }

            private void updateOKState() {
                if (dirField.getText().equals(""))
                    okButton.setEnabled(false);
                else
                    okButton.setEnabled(true);
            }
        });
        JButton browse = new JButton("Browse...");
        FileSelectionListener browseListener;
        if (fileType != null) {
            browseListener = new FileSelectionListener(this, new FileExtensionFilter(fileType, extensions), this, null);
            browseListener.setMultipleSelection(true);
        } else {
            browseListener = new FileSelectionListener(this, this, null);
            browseListener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        browse.addActionListener(browseListener);
        okButton = new JButton("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fileName = dirField.getText();
                dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
        builder.add(browse, constraints.xy(6, 2));
        builder.add(buttonPanel, constraints.xyw(2, 4, 5));
        getContentPane().add(builder.getPanel());
        getRootPane().setDefaultButton(okButton);
        setCloseButton(cancelButton);
        pack();
    }

    public String getSelectedFiles() {
        dirField.setText("");
        fileName = null;
        setLocationRelativeTo(parent);
        setVisible(true);
        return fileName;
    }

    public void filesSelected(File[] files, Object cookie) {
        StringBuffer fileList = new StringBuffer();
        for (int i = 0; i < files.length - 1; i++) {
            fileList.append(files[i]).append(File.pathSeparator);
        }
        fileList.append(files[files.length - 1]);
        dirField.setText(fileList.toString());
    }
}
