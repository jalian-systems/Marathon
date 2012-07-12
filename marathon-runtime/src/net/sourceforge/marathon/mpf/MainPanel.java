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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.UIUtils;
import net.sourceforge.marathon.util.ValidationUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MainPanel implements IPropertiesPanel, IFileSelectedAction, ISubPropertiesPanel {
    public static final Icon icon = new ImageIcon(MainPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/main_obj.gif"));
    private JTextField mainClassField;
    private JTextArea programArgsField;
    private JTextArea vmArgsField;
    private JTextField workingDirField;
    private JButton browseVM;
    private JTextField vmCommandField;
    private Component parent;
    private JButton browse;
    private JPanel panel;

    public MainPanel(Component parent) {
        this.parent = parent;
    }

    public JPanel createPanel() {
        initComponents();
        PanelBuilder builder = new PanelBuilder(new FormLayout("left:pref, 3dlu, fill:pref:grow, 3dlu, fill:pref",
                "pref, 3dlu, pref, 3dlu, fill:pref:grow, 3dlu, fill:pref:grow, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
        builder.setDefaultDialogBorder();
        CellConstraints labelConstraints = new CellConstraints();
        CellConstraints compConstraints = new CellConstraints();
        builder.addLabel("Class &Name: ", labelConstraints.xy(1, 1), mainClassField, compConstraints.xywh(3, 1, 3, 1));
        JScrollPane scrollPane = new JScrollPane(programArgsField, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel label = builder.addLabel("Pro&gram Arguments: ",
                labelConstraints.xy(1, 5, CellConstraints.LEFT, CellConstraints.TOP), scrollPane, compConstraints.xywh(3, 5, 3, 1));
        label.setLabelFor(programArgsField);
        scrollPane = new JScrollPane(vmArgsField, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        label = builder.addLabel("&VM Arguments: ", labelConstraints.xy(1, 7, CellConstraints.LEFT, CellConstraints.TOP),
                scrollPane, compConstraints.xywh(3, 7, 3, 1));
        label.setLabelFor(vmArgsField);
        builder.addLabel("&Working Directory: ", labelConstraints.xy(1, 9), workingDirField, compConstraints.xy(3, 9));
        builder.add(browse, labelConstraints.xy(5, 9));
        browse.setMnemonic(KeyEvent.VK_B);
        builder.addLabel("&Java Executable: ", labelConstraints.xy(1, 11), vmCommandField, compConstraints.xy(3, 11));
        builder.add(browseVM, labelConstraints.xy(5, 11));
        return builder.getPanel();
    }

    private void initComponents() {
        mainClassField = new JTextField(20);
        programArgsField = new JTextArea(4, 30);
        programArgsField.setLineWrap(true);
        programArgsField.setWrapStyleWord(true);
        vmArgsField = new JTextArea(4, 30);
        vmArgsField.setLineWrap(true);
        vmArgsField.setWrapStyleWord(true);
        vmCommandField = new JTextField(20);
        browseVM = UIUtils.createBrowseButton();
        browseVM.setMnemonic('o');
        FileSelectionListener fileSelectionListenerVM = new FileSelectionListener(this, null, parent, vmCommandField, "Select Java Executable");
        browseVM.addActionListener(fileSelectionListenerVM);
        workingDirField = new JTextField(20);
        browse = UIUtils.createBrowseButton();
        browse.setMnemonic('r');
        FileSelectionListener fileSelectionListener = new FileSelectionListener(this, null, parent, workingDirField, "Select Working Directory");
        fileSelectionListener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browse.addActionListener(fileSelectionListener);
    }

    public String getName() {
        return "Main";
    }

    public Icon getIcon() {
        return icon;
    }

    public void getProperties(Properties props) {
        props.setProperty(Constants.PROP_APPLICATION_MAINCLASS, mainClassField.getText());
        props.setProperty(Constants.PROP_APPLICATION_ARGUMENTS, programArgsField.getText());
        props.setProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS, vmArgsField.getText());
        props.setProperty(Constants.PROP_APPLICATION_VM_COMMAND, vmCommandField.getText());
        props.setProperty(Constants.PROP_APPLICATION_WORKING_DIR, workingDirField.getText());
    }

    public void setProperties(Properties props) {
        mainClassField.setText(props.getProperty(Constants.PROP_APPLICATION_MAINCLASS, ""));
        mainClassField.setCaretPosition(0);
        programArgsField.setText(props.getProperty(Constants.PROP_APPLICATION_ARGUMENTS, ""));
        programArgsField.setCaretPosition(0);
        vmArgsField.setText(props.getProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS, ""));
        vmArgsField.setCaretPosition(0);
        vmCommandField.setText(props.getProperty(Constants.PROP_APPLICATION_VM_COMMAND, ""));
        vmCommandField.setCaretPosition(0);
        workingDirField.setText(props.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, ""));
        workingDirField.setCaretPosition(0);
    }

    public boolean isValidInput() {
        if (mainClassField.getText() == null || mainClassField.getText().equals("")) {
            JOptionPane.showMessageDialog(parent, "Main class can't be empty", "Main Class", JOptionPane.ERROR_MESSAGE);
            mainClassField.requestFocus();
            return false;
        }
        if (!ValidationUtil.isValidClassName(mainClassField.getText())) {
            JOptionPane.showMessageDialog(parent, "Invalid class name given for main class", "Main Class",
                    JOptionPane.ERROR_MESSAGE);
            mainClassField.requestFocus();
            return false;
        }
        if (mainClassField.getText().indexOf('.') == -1) {
            int r = JOptionPane
                    .showConfirmDialog(
                            parent,
                            "There is no package given for the main class. You need to give fully qualified class name. Do you want to continue?",
                            "Main Class", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.OK_OPTION) {
                mainClassField.requestFocus();
                return false;
            }
        }
        if (programArgsField.getText().indexOf('\n') != -1 || programArgsField.getText().indexOf('\r') != -1) {
            JOptionPane.showMessageDialog(parent, "Can not have new lines in Program Arguments", "Program Arguments",
                    JOptionPane.ERROR_MESSAGE);
            programArgsField.requestFocus();
            return false;
        }
        if (vmArgsField.getText().indexOf('\n') != -1 || vmArgsField.getText().indexOf('\r') != -1) {
            JOptionPane.showMessageDialog(parent, "Can not have new lines in VM Arguments", "VM Arguments",
                    JOptionPane.ERROR_MESSAGE);
            vmArgsField.requestFocus();
            return false;
        }
        return true;
    }

    public void filesSelected(File[] files, Object cookie) {
        ((JTextField) cookie).setText(files[0].getAbsolutePath());
    }

    public JPanel getPanel() {
        if (panel == null)
            panel = createPanel();
        return panel;
    }

    public int getMnemonic() {
        return KeyEvent.VK_M;
    }

}
