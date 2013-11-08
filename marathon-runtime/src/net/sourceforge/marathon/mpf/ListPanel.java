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
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.marathon.util.MPFUtils;
import net.sourceforge.marathon.util.UIUtils;
import net.sourceforge.marathon.util.ValidationUtil;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class ListPanel implements IPropertiesPanel {
    protected MovableItemListModel classpathListModel = new MovableItemListModel();
    private JList classpathList = null;
    private JButton removeButton = null;
    private JButton upButton = null;
    private JButton downButton = null;
    private JButton addJarsButton = null;
    private JButton addFoldersButton = null;
    private JButton addClassesButton = null;
    protected JDialog parent;
    private boolean replaceProjectDir = false;
    private JPanel panel;

    final class BrowseActionListener implements ActionListener {
        private FileSelectionDialog fileSelectionDialog;

        BrowseActionListener(String title, String fileType, String[] extensions) {
            fileSelectionDialog = new FileSelectionDialog(title, parent, fileType, extensions);
        }

        public void actionPerformed(ActionEvent e) {
            String fileString = fileSelectionDialog.getSelectedFiles();
            if (fileString == null)
                return;
            String[] selectedFiles = fileString.split(File.pathSeparator);
            addToList(selectedFiles);
        }
    }

    public ListPanel(JDialog parent) {
        this(parent, false);
    }

    public ListPanel(JDialog parent, boolean replaceProjectDir) {
        this.parent = parent;
        this.replaceProjectDir = replaceProjectDir;
    }

    public abstract boolean isAddArchivesNeeded();

    public abstract boolean isAddFoldersNeeded();

    public abstract boolean isAddClassesNeeded();

    public abstract boolean isSingleSelection();

    public boolean isTraversalNeeded() {
        return true;
    }

    private JPanel getButtonStackPanel() {
        ButtonStackBuilder builder = new ButtonStackBuilder();
        ArrayList<JButton> buttons = new ArrayList<JButton>();
        if (upButton != null)
            buttons.add(upButton);
        if (downButton != null)
            buttons.add(downButton);
        if (addJarsButton != null)
            buttons.add(addJarsButton);
        if (addFoldersButton != null)
            buttons.add(addFoldersButton);
        if (addClassesButton != null)
            buttons.add(addClassesButton);
        buttons.add(removeButton);
        JButton[] aButtons = new JButton[buttons.size()];
        buttons.toArray(aButtons);
        builder.addButtons(aButtons);
        return builder.getPanel();
    }

    protected void addToList(String className) {
        MovableItemListModel model = (MovableItemListModel) classpathList.getModel();
        model.add(className);
        classpathList.setSelectedIndex(model.getSize() - 1);
    }

    public JPanel createPanel() {
        initComponents();
        PanelBuilder builder = getBuilder();
        builder.setBorder(Borders.DIALOG_BORDER);
        return builder.getPanel();
    }

    protected PanelBuilder getBuilder() {
        JScrollPane scrollPane = new JScrollPane(classpathList);
        FormLayout layout = new FormLayout("pref, 3dlu, fill:d:grow, 3dlu, center:pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.appendRow("fill:p:grow");
        CellConstraints constraints = new CellConstraints();
        builder.add(scrollPane, constraints.xyw(1, 1, 3));
        builder.add(getButtonStackPanel(), constraints.xy(5, 1));
        return builder;
    }

    private void initComponents() {
        classpathList = new JList(classpathListModel);
        classpathList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classpathList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selectedIndex = classpathList.getSelectedIndex();
                int itemCount = classpathListModel.getSize();
                boolean enable = selectedIndex != -1;
                removeButton.setEnabled(enable);
                if (upButton != null)
                    upButton.setEnabled(selectedIndex != 0 && enable && itemCount > 1);
                if (downButton != null)
                    downButton.setEnabled(selectedIndex != itemCount - 1 && enable && itemCount > 1);
            }
        });
        classpathList.setCellRenderer(new DirectoryFileRenderer());
        if (isSingleSelection()) {
            classpathList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        if (isTraversalNeeded()) {
            upButton = UIUtils.createUpButton();
            upButton.addActionListener(new UpDownListener(classpathList, true));
            upButton.setMnemonic(KeyEvent.VK_U);
            upButton.setEnabled(false);
            downButton = UIUtils.createDownButton();
            downButton.addActionListener(new UpDownListener(classpathList, false));
            downButton.setMnemonic(KeyEvent.VK_D);
            downButton.setEnabled(false);
        }
        if (isAddArchivesNeeded()) {
            addJarsButton = UIUtils.createAddArchivesButton();
            addJarsButton.addActionListener(new BrowseActionListener("Select Zip/Jar files", "Java Archives", new String[] {
                    ".jar", ".zip" }));
            addJarsButton.setMnemonic(KeyEvent.VK_H);
        }
        if (isAddFoldersNeeded()) {
            addFoldersButton = UIUtils.createAddFoldersButton();
            addFoldersButton.addActionListener(new BrowseActionListener("Select Folders", null, null));
            addFoldersButton.setMnemonic('F');
        }
        if (isAddClassesNeeded()) {
            addClassesButton = getAddClassButton();
            addClassesButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String className = getClassName();
                    addToList(className);
                }
            });
            addClassesButton.setMnemonic(KeyEvent.VK_C);
        }
        removeButton = UIUtils.createRemoveButton();
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selectedIndices = classpathList.getSelectedIndices();
                if (selectedIndices != null) {
                    for (int selectedIndex : selectedIndices) {
                        if (selectedIndex < classpathListModel.getSize())
                            classpathListModel.remove(selectedIndex);
                        boolean enable = classpathListModel.getSize() != 0;
                        removeButton.setEnabled(enable);
                        if (upButton != null)
                            upButton.setEnabled(enable);
                        if (downButton != null)
                            downButton.setEnabled(enable);
                    }
                }
            }
        });
        removeButton.setEnabled(false);
    }

    protected JButton getAddClassButton() {
        return UIUtils.createAddClassButton();
    }

    private void addToList(String[] list) {
        MovableItemListModel model = (MovableItemListModel) classpathList.getModel();
        for (int i = 0; i < list.length; i++) {
            model.add(new File(list[i]));
        }
        classpathList.setSelectedIndex(model.getSize() - 1);
    }

    public abstract String getPropertyKey();

    public void getProperties(Properties props) {
        props.setProperty(getPropertyKey(), getClassPath(props));
    }

    protected String getClassPath(Properties props) {
        StringBuffer cp = new StringBuffer("");
        int size = classpathListModel.getSize();
        if (size == 0)
            return cp.toString();
        for (int i = 0; i < size; i++) {
            Object elementAt = classpathListModel.getElementAt(i);
            if (elementAt instanceof File) {
                if (replaceProjectDir)
                    cp.append(MPFUtils.encodeProjectDir((File) elementAt, props));
                else
                    cp.append(((File) elementAt).toString());
            } else
                cp.append(elementAt);
            if (i != size - 1)
                cp.append(";");
        }
        return cp.toString();
    }

    public void setProperties(Properties props) {
        String cp = props.getProperty(getPropertyKey(), "");
        if (cp.length() == 0)
            return;
        String[] elements = cp.split(";");
        for (int i = 0; i < elements.length; i++) {
            if (replaceProjectDir)
                classpathListModel.add(new File(MPFUtils.decodeProjectDir(elements[i], props)));
            else
                classpathListModel.add(elements[i]);
        }
    }

    public String getClassName() {
        String className;
        while (true) {
            className = JOptionPane.showInputDialog(parent, "Class Name", "Resolver Class", JOptionPane.PLAIN_MESSAGE);
            if (className == null || ValidationUtil.isValidClassName(className))
                break;
            JOptionPane.showMessageDialog(parent, "Invalid class name", "Class Name", JOptionPane.ERROR_MESSAGE);
        }
        return className;
    }

    public JDialog getParent() {
        return parent;
    }

    public JPanel getPanel() {
        if (panel == null)
            panel = createPanel();
        return panel;
    }
}
