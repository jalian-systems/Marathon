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
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MarathonModuleDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private boolean ok;
    private JTextField functionName;
    private JLabel errorMsgLabel;
    private String errorMessage;
    private JTextArea description;
    private JComboBox moduleDirCombo;
    private String suffix = ".rb";
    private JComboBox moduleFileCombo;
    private JButton okButton;

    private boolean needModuleFile = true;
    private JButton cancelButton;

    public MarathonModuleDialog(JFrame parent, String title, String suffix) {
        super(parent, title, true);
        this.suffix = suffix;
        initialize();
    }

    public MarathonModuleDialog(JDialog parent, String title, String suffix) {
        super(parent, title, true);
        this.suffix = suffix;
        initialize();
    }

    public MarathonModuleDialog(JDialog parent, String title, String suffix, boolean needModuleFile) {
        super(parent, title, true);
        this.suffix = suffix;
        this.needModuleFile = needModuleFile;
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
        errorMsgLabel.setIcon(new ImageIcon(MarathonModuleDialog.class.getClassLoader().getResource(
                "net/sourceforge/marathon/display/icons/enabled/error.gif")));
        errorMsgLabel.setVisible(false);

        int row = 1;
        functionName = new JTextField(15);
        builder.addLabel("&Module function name: ", cc.xy(2, row), functionName, cc1.xy(4, row));
        row += 2;
        description = new JTextArea(5, 30);
        description.setLineWrap(true);
        builder.addLabel("&Description: ", cc.xy(2, row, "left, top"), new JScrollPane(description), cc1.xy(4, row));
        row += 2;
        okButton = UIUtils.createOKButton();
        okButton.setEnabled(false);
        ok = false;
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                validateModuleName();
            }

            public void insertUpdate(DocumentEvent e) {
                validateModuleName();
            }

            public void removeUpdate(DocumentEvent e) {
                validateModuleName();
            }

        };
        functionName.getDocument().addDocumentListener(documentListener);
        description.getDocument().addDocumentListener(documentListener);
        if (needModuleFile) {
            moduleDirCombo = createModuleDirCombo();
            builder.addLabel("M&odule Directory: ", cc.xy(2, row), moduleDirCombo, cc1.xy(4, row));
            row += 2;
            moduleFileCombo = createModuleFileCombo();
            ((JTextField) moduleFileCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(documentListener);
            builder.addLabel("Mod&ule File: ", cc.xy(2, row), moduleFileCombo, cc1.xy(4, row));
            row += 2;

            // Set the module dir to the first modules dir
            moduleDirCombo.setSelectedIndex(0);
        }
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        });
        cancelButton = UIUtils.createCancelButton();
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

    private void validateModuleName() {
        if (isValidModuleName(functionName.getText(), description.getText())) {
            okButton.setEnabled(true);
            errorMsgLabel.setVisible(false);
        } else {
            okButton.setEnabled(false);
            errorMsgLabel.setVisible(true);
        }
        errorMsgLabel.setText(errorMessage);
    }

    private static class ModuleFileComboModel extends AbstractListModel implements ComboBoxModel, ActionListener {
        private static final long serialVersionUID = 1L;

        private final JComboBox dirCombo;
        private String suffix;

        private String[] listFiles = new String[0];

        private Object selectedItem;

        public ModuleFileComboModel(JComboBox dirCombo, String suffix) {
            this.dirCombo = dirCombo;
            this.suffix = suffix;
            dirCombo.addActionListener(this);
        }

        public int getSize() {
            return listFiles.length;
        }

        public Object getElementAt(int index) {
            return listFiles[index];
        }

        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void actionPerformed(ActionEvent event) {
            ModuleDirElement e = (ModuleDirElement) dirCombo.getSelectedItem();
            populateFiles(e.file);
            fireContentsChanged(this, 0, 0);
        }

        private void populateFiles(File file) {
            listFiles = file.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile() && name.endsWith(suffix);
                }
            });
        }
    }

    private JComboBox createModuleFileCombo() {
        final JComboBox cb = new JComboBox(new ModuleFileComboModel(moduleDirCombo, suffix));
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                cb.getEditor().setItem(cb.getSelectedItem());
            }
        });
        cb.setEditable(true);
        return cb;
    }

    private static class ModuleDirElement {
        private File file;
        private String prefix;

        @Override public String toString() {
            return file.toString();
        }
    }

    private static class ModuleDirComboModel extends AbstractListModel implements ComboBoxModel {
        private static final long serialVersionUID = 1L;

        private Object selectedItem;

        private List<ModuleDirElement> dirs = new ArrayList<ModuleDirElement>();

        public ModuleDirComboModel(String[] moduleDirs) {
            for (int i = 0; i < moduleDirs.length; i++) {
                populateDirs(new File(moduleDirs[i]), new File(moduleDirs[i]), "");
            }
        }

        private void populateDirs(File parent, File file, String prefix) {
            if (file.exists() && file.isDirectory()) {
                ModuleDirElement element = new ModuleDirElement();
                element.file = file;
                element.prefix = prefix;
                dirs.add(element);
                File[] listFiles = file.listFiles();
                for (int i = 0; i < listFiles.length; i++) {
                    populateDirs(parent, listFiles[i], "  " + prefix);
                }
            }
        }

        public int getSize() {
            return dirs.size();
        }

        public Object getElementAt(int index) {
            return dirs.get(index);
        }

        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

    }

    private JComboBox createModuleDirCombo() {
        String[] moduleDirs = Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS);
        JComboBox cb = new JComboBox(new ModuleDirComboModel(moduleDirs));
        cb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                if (value != null) {
                    ModuleDirElement element = (ModuleDirElement) value;
                    if (index == -1)
                        value = element.file.getName();
                    else
                        value = element.prefix + element.file.getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        return cb;
    }

    public boolean isOk() {
        return ok;
    }

    public File getModuleDirectory() {
        return ((ModuleDirElement) moduleDirCombo.getSelectedItem()).file;
    }

    public String getFileName() {
        String selectedItem = (String) moduleFileCombo.getSelectedItem();
        if (selectedItem == null) {
            selectedItem = ((JTextField) moduleFileCombo.getEditor().getEditorComponent()).getText();
        }
        if (selectedItem.endsWith(suffix))
            return selectedItem;
        else
            return selectedItem + suffix;
    }

    public String getFunctionName() {
        return functionName.getText();
    }

    public String getDescription() {
        return description.getText();
    }

    private boolean isValidModuleName(String moduleName, String description) {
        errorMessage = "";
        if (moduleName.length() == 0) {
            errorMessage = "Module name cannot be empty";
            return false;
        }
        if (moduleName.equals("test")) {
            errorMessage = "Module name cannot be test";
            return false;
        }
        if (isNumber(moduleName)) {
            errorMessage = "Module name cannot be a number";
            return false;
        }
        if (moduleName.contains(" ")) {
            errorMessage = "Module name cannot contain spaces";
            return false;
        }
        if (Pattern.matches("[\\\\\\W]*", moduleName)) {
            errorMessage = "Module name can contain only alpha-numeric characters.";
            return false;
        }
        if (!Pattern.matches("[^[_]|^[[a-z]|[A-Z]]].*", moduleName)) {
            errorMessage = "Module name should begin only with a alphabet or an underscore.";
            return false;
        }
        if (!Pattern.matches("[^[_]|^[[a-z]|[A-Z]]][\\\\\\w]*", moduleName)) {
            errorMessage = "Module name should not contain symbols.";
            return false;
        }
        if (needModuleFile) {
            String selectedItem = ((JTextField) moduleFileCombo.getEditor().getEditorComponent()).getText();
            if (selectedItem == null || selectedItem.length() == 0) {
                errorMessage = "File name should be provided";
                return false;
            }
        }
        return true;
    }

    private boolean isNumber(String string) {
        return Pattern.matches("^\\d+$", string);
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}