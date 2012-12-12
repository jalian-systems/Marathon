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

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ProjectPanel implements IPropertiesPanel, IFileSelectedAction {
    public static final Icon ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/prj_obj.gif"));;
    private JTextField nameField;
    private JTextField dirField;
    private JTextArea descriptionField;
    private MPFConfigurationUI parent;
    private JButton browse;
    private JCheckBox useToolkitMenumask;
    private JCheckBox useDevelopmentMode;
    private String testDir;
    private String fixtureDir;
    private String moduleDir;
    private String checklistDir;
    private JPanel panel;

    public ProjectPanel(MPFConfigurationUI configurationUI) {
        parent = configurationUI;
    }

    public JPanel createPanel() {
        initComponents();
        PanelBuilder builder = new PanelBuilder(new FormLayout("left:pref, 3dlu, pref:grow, 3dlu, fill:pref",
                "pref, 3dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 3dlu, pref"));
        builder.setDefaultDialogBorder();
        CellConstraints labelConstraints = new CellConstraints();
        CellConstraints compConstraints = new CellConstraints();
        builder.addLabel("&Name: ", labelConstraints.xy(1, 1), nameField, compConstraints.xywh(3, 1, 3, 1));
        builder.addLabel("Directory: ", labelConstraints.xy(1, 3), dirField, compConstraints.xy(3, 3));
        builder.add(browse, compConstraints.xy(5, 3));
        JScrollPane scrollPane = new JScrollPane(descriptionField, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel label = builder.addLabel("&Description: ", labelConstraints.xy(1, 5, CellConstraints.LEFT, CellConstraints.TOP),
                scrollPane, compConstraints.xywh(3, 5, 3, 1));
        label.setLabelFor(descriptionField);
        builder.add(useToolkitMenumask, labelConstraints.xyw(2, 7, 2));
        builder.add(useDevelopmentMode, labelConstraints.xyw(2, 9, 2));
        return builder.getPanel();
    }

    private void initComponents() {
        nameField = new JTextField(20);
        dirField = new JTextField(20);
        dirField.setEditable(false);
        dirField.setFocusable(false);
        browse = UIUtils.createBrowseButton();
        browse.setMnemonic('o');
        FileSelectionListener fileSelectionListener = new FileSelectionListener(this, null, parent, null, "Select Project Directory");
        fileSelectionListener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browse.addActionListener(fileSelectionListener);
        descriptionField = new JTextArea(4, 30);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        useToolkitMenumask = new JCheckBox("Use platform specific Command/Control key while playing", false);
        useToolkitMenumask.setMnemonic(KeyEvent.VK_U);
        useDevelopmentMode = new JCheckBox("Use test development mode", false);
        useDevelopmentMode.setMnemonic(KeyEvent.VK_S);
    }

    public String getName() {
        return "Project";
    }

    public Icon getIcon() {
        return ICON;
    }

    public void getProperties(Properties props) {
        props.setProperty(Constants.PROP_PROJECT_NAME, nameField.getText());
        props.setProperty(Constants.PROP_PROJECT_DIR, dirField.getText().replace(File.separatorChar, '/'));
        props.setProperty(Constants.PROP_PROJECT_DESCRIPTION, descriptionField.getText());

        if (testDir != null)
            props.setProperty(Constants.PROP_TEST_DIR, testDir);
        if (fixtureDir != null)
            props.setProperty(Constants.PROP_FIXTURE_DIR, fixtureDir);
        if (moduleDir != null)
            props.setProperty(Constants.PROP_MODULE_DIRS, moduleDir);
        if (checklistDir != null)
            props.setProperty(Constants.PROP_CHECKLIST_DIR, checklistDir);
        props.setProperty(Constants.PROP_APPLICATION_TOOLKIT_MENUMASK, Boolean.toString(useToolkitMenumask.isSelected()));
        props.setProperty(Constants.PROP_APPLICATION_DEVMODE, Boolean.toString(useDevelopmentMode.isSelected()));
    }

    public void setProperties(Properties props) {
        // Also store the directory props and give them back
        testDir = props.getProperty(Constants.PROP_TEST_DIR);
        fixtureDir = props.getProperty(Constants.PROP_FIXTURE_DIR);
        moduleDir = props.getProperty(Constants.PROP_MODULE_DIRS);
        checklistDir = props.getProperty(Constants.PROP_CHECKLIST_DIR);
        nameField.setText(props.getProperty(Constants.PROP_PROJECT_NAME, ""));
        nameField.setCaretPosition(0);
        dirField.setText(props.getProperty(Constants.PROP_PROJECT_DIR, "").replace('/', File.separatorChar));
        dirField.setCaretPosition(0);
        if (!dirField.getText().equals("")) {
            browse.setEnabled(false);
        }
        descriptionField.setText(props.getProperty(Constants.PROP_PROJECT_DESCRIPTION, ""));
        descriptionField.setCaretPosition(0);
        useToolkitMenumask.setSelected(Boolean.valueOf(props.getProperty(Constants.PROP_APPLICATION_TOOLKIT_MENUMASK, "false"))
                .booleanValue());
        useDevelopmentMode.setSelected(Boolean.valueOf(props.getProperty(Constants.PROP_APPLICATION_DEVMODE, "false"))
                .booleanValue());
    }

    public boolean isValidInput() {
        if (nameField.getText() == null || nameField.getText().equals("")) {
            JOptionPane.showMessageDialog(parent, "Project name can't be empty", "Project Name", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        if (dirField.getText() == null || dirField.getText().equals("")) {
            JOptionPane.showMessageDialog(parent, "Project directory can't be empty", "Project Directory",
                    JOptionPane.ERROR_MESSAGE);
            dirField.requestFocus();
            return false;
        }
        return true;
    }

    public void filesSelected(File[] files, Object cookie) {
        dirField.setText(files[0].getAbsolutePath());
    }

    public JPanel getPanel() {
        if (panel == null)
            panel = createPanel();
        return panel;
    }
}
