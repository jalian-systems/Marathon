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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * MPFSelection allows the user to select a MPF file if not given on the command
 * line.
 */
public class MPFSelection extends EscapeDialog implements IFileSelectedAction {
    private static final int MAX_SAVED_FILES = 10;
    private static final long serialVersionUID = 1L;
    public static final ImageIcon BANNER = new ImageIcon(MPFConfigurationUI.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/banner.gif"));;
    private JComboBox dirName = new JComboBox();
    private JButton browseButton = UIUtils.createBrowseButton();
    protected boolean isOKSelected = false;
    private JButton modifyButton = UIUtils.createEditButton();
    private JButton okButton = UIUtils.createSelectButton();
    private JButton newButton = UIUtils.createNewButton();
    private JButton cancelButton = UIUtils.createCancelButton();

    /**
     * Get the selection panel populated with the controls.
     * 
     * @return panel, the selection panel.
     */
    private JPanel getSelectionPanel() {
        PanelBuilder builder = new PanelBuilder(new FormLayout("left:p:none, 3dlu, fill:p:grow, 3dlu, d", "pref"));
        builder.setDefaultDialogBorder();
        CellConstraints cc1 = new CellConstraints();
        CellConstraints cc2 = new CellConstraints();
        loadFileNames();
        if (dirName.getItemCount() == 0)
            dirName.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        builder.addLabel("&Project directory:", cc1.xy(1, 1), dirName, cc2.xy(3, 1));
        builder.add(browseButton, cc1.xy(5, 1));
        browseButton.setMnemonic(KeyEvent.VK_R);
        FileSelectionListener fsl = new FileSelectionListener(this, new ProjectDirectoryFilter("Marathon Project Directories"),
                this, null, "Select Marathon Project Directory");
        fsl.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browseButton.addActionListener(fsl);
        return builder.getPanel();
    }

    /**
     * Load the recently used Marathon project directories from the user
     * preferences.
     */
    private void loadFileNames() {
        Preferences p = Preferences.userNodeForPackage(this.getClass());
        try {
            String[] keys = p.keys();
            for (int i = 0; i < keys.length; i++) {
                String fName = p.get("dirName" + i, null);
                if (fName == null)
                    continue;
                File file = new File(fName);
                if (isValidProjectDirectory(file))
                    dirName.addItem(fName);
            }
            if (dirName.getItemCount() > 0)
                dirName.setSelectedIndex(0);
        } catch (BackingStoreException e) {
            return;
        }
    }

    /**
     * Check whether given directory is a valid Marathon project directory
     * 
     * @param file
     * @return
     */
    private boolean isValidProjectDirectory(File file) {
        return file.exists() && file.isDirectory() && (new File(file, Constants.PROJECT_FILE)).exists();
    }

    /**
     * Store the current set of Marathon project directories into the user
     * preferences.
     */
    private void storeFileNames() {
        Preferences p = Preferences.userNodeForPackage(this.getClass());
        try {
            p.clear();
            p.flush();
            p = Preferences.userNodeForPackage(this.getClass());
            int itemCount = dirName.getItemCount();
            int selected = dirName.getSelectedIndex();
            p.put("dirName0", (String) dirName.getItemAt(selected));
            for (int i = itemCount - 1, j = 1; i >= 0 && i >= itemCount - MAX_SAVED_FILES; i--) {
                if (i != selected)
                    p.put("dirName" + j++, (String) dirName.getItemAt(i));
            }
        } catch (BackingStoreException e) {
            return;
        }
    }

    /**
     * Construct a MPFSelection frame.
     */
    public MPFSelection() {
        setModal(true);
        setTitle("Marathon - Select Directory");
        BannerPanel bannerPanel = new BannerPanel();
        String[] lines = { "Select a Marathon Project Directory" };
        BannerPanel.Sheet sheet = new BannerPanel.Sheet("Create and manage configuration", lines, BANNER);
        bannerPanel.addSheet(sheet, "main");
        getContentPane().add(bannerPanel, BorderLayout.NORTH);
        getContentPane().add(getSelectionPanel());
        dirName.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel comp = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (comp.getText().equals(""))
                    return comp;
                File file = new File(comp.getText());
                String fileName = file.getName() + " - " + (file.getParent() == null ? "." : file.getParent());
                comp.setText(fileName);
                comp.setToolTipText(file.toString());
                return comp;
            }
        });
        newButton.setMnemonic(KeyEvent.VK_N);
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MPFConfigurationUI configurationUI = new MPFConfigurationUI(MPFSelection.this);
                String fname = configurationUI.getProjectDirectory();
                if (fname != null)
                    filesSelected(new File[] { new File(fname) }, null);
            }
        });
        modifyButton.setMnemonic(KeyEvent.VK_E);
        if (dirName.getSelectedIndex() == -1)
            modifyButton.setEnabled(false);
        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fname = (String) dirName.getSelectedItem();
                MPFConfigurationUI configurationUI = new MPFConfigurationUI(fname, MPFSelection.this);
                fname = configurationUI.getProjectDirectory();
                if (fname != null)
                    filesSelected(new File[] { new File(fname) }, null);
            }
        });
        if (dirName.getSelectedIndex() == -1)
            okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isOKSelected = true;
                dispose();
            }
        });
        dirName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modifyButton.setEnabled(true);
                okButton.setEnabled(true);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        ButtonBarBuilder2 bbb = new ButtonBarBuilder2();
        bbb.addGlue();
        bbb.addButton(newButton);
        bbb.addButton(modifyButton);
        bbb.addUnrelatedGap();
        bbb.addButton(cancelButton);
        bbb.addButton(okButton);
        JPanel buttonPanel = bbb.getPanel();
        buttonPanel.setBorder(Borders.createEmptyBorder("0dlu, 0dlu, 3dlu, 7dlu"));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Return the selected MPF file.
     * 
     * @param arg
     *            , the filename given on command line
     * @return file, the selected file name
     */
    public String getProjectDirectory(String arg) {
        if (arg != null) {
            File file = new File(arg);
            if (!isValidProjectDirectory(file)) {
                JOptionPane.showMessageDialog(null, "Not a valid Marathon Project Directory");
            } else {
                if (findFile(file) == -1) {
                    dirName.addItem(file.toString());
                }
                dirName.setSelectedItem(arg);
                storeFileNames();
                return arg;
            }
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocation(20, 20);
        setVisible(true);
        if (isOKSelected) {
            storeFileNames();
            return (String) dirName.getSelectedItem();
        }
        return null;
    }

    /**
     * If the user selected a file using the 'browse' option, check whether the
     * file selected already exists in the fileName combo box.
     * 
     * @param file
     * @return index, the file name index into the combobox. -1 if a new file.
     */
    private int findFile(File file) {
        ComboBoxModel model = dirName.getModel();
        int size = model.getSize();
        for (int i = 0; i < size; i++) {
            String n = (String) dirName.getItemAt(i);
            try {
                if (new File(n).getCanonicalPath().equals(file.getCanonicalPath())) {
                    return i;
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.mpf.FileSelectedAction#filesSelected(java.io
     * .File[], java.lang.Object)
     */
    public void filesSelected(File[] files, Object cookie) {
        File file = files[0];
        if (isValidProjectDirectory(file)) {
            if (findFile(file) == -1) {
                dirName.addItem(file.toString());
            }
            dirName.setSelectedItem(file.toString());
        } else {
            JOptionPane.showMessageDialog(this, "Not a valid Marathon Project Directory");
        }
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }

}
