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
package net.sourceforge.marathon.navigator;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.UIUtils;

import com.vlsolutions.swing.toolbars.VLToolBar;

/**
 * DefaultActions provides a set of default actions to the Navigator tree.
 */
public class DefaultActions {
    protected Navigator navigator;

    /**
     * Construct a DefaultActions object.
     * 
     * @param navigator
     */
    public DefaultActions(Navigator navigator) {
        this.navigator = navigator;
    }

    /**
     */
    protected class NewFileAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;
        boolean createDirectory = false;

        public NewFileAction() {
            this(false);
        }

        public NewFileAction(boolean createDirectory) {
            super(navigator, createDirectory ? "Folder" : "File",
                    createDirectory ? Icons.NEWFOLDER_ENABLED : Icons.NEWFILE_ENABLED, createDirectory ? Icons.NEWFOLDER_DISABLED
                            : Icons.NEWFILE_DISABLED);
            this.createDirectory = createDirectory;
        }

        public void actionPerformed(ActionEvent event, File[] files) {
            File startDirectory;
            try {
                startDirectory = files[0].isDirectory() ? files[0].getCanonicalFile() : files[0].getParentFile().getCanonicalFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try {
                if (createDirectory)
                    navigator.createNewFolder(startDirectory, null);
                else
                    navigator.createNewFile(startDirectory, null, new String[] {}, "");
            } catch (IOException e) {
                JOptionPane.showConfirmDialog(null, "Can't create " + (createDirectory ? "Folder: " : "File: ") + e.getMessage(),
                        "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean getEnabledState(File[] files) {
            if (files != null && files.length == 1)
                return true;
            return false;
        }
    }

    /**
     */
    protected class NewFolderAction extends NewFileAction {
        private static final long serialVersionUID = 1L;

        public NewFolderAction() {
            super(true);
        }
    }

    /**
     * Copy files to clipboard
     */
    protected class CopyAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public CopyAction() {
            super(navigator, "Copy", Icons.COPY_ENABLED, Icons.COPY_DISABLED);
            navigator.getActionMap().put("COPY", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, OSUtils.MENU_MASK), "COPY");
        }

        public void actionPerformed(ActionEvent e, File[] files) {
            navigator.copy(files);
        }

        public boolean getEnabledState(File[] files) {
            if (files != null)
                return true;
            return false;
        }
    }

    /**
     * Paste files from the clipboard to the current directory
     */
    protected class PasteAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public PasteAction() {
            super(navigator, "Paste", Icons.PASTE_ENABLED, Icons.PASTE_DISABLED);
            navigator.getActionMap().put("PASTE", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, OSUtils.MENU_MASK), "PASTE");
        }

        public void actionPerformed(ActionEvent event, File[] files) {
            try {
                navigator.paste(files[0]);
            } catch (IOException e) {
                JOptionPane
                        .showConfirmDialog(null, e.getMessage(), "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean getEnabledState(File[] files) {
            if (files != null && navigator.getCopiedFiles() != null && files.length == 1)
                return true;
            return false;
        }
    }

    /**
     * Delete the selected files
     */
    protected class DeleteAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public DeleteAction() {
            super(navigator, "Delete", Icons.DELETE_ENABLED, Icons.DELETE_DISABLED);
            navigator.getActionMap().put("DELETE", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
        }

        public void actionPerformed(ActionEvent event, File[] files) {
            try {
                navigator.deleteFiles(files);
            } catch (IOException e) {
                JOptionPane
                        .showConfirmDialog(null, e.getMessage(), "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean getEnabledState(File[] files) {
            if (files != null)
                return true;
            return false;
        }
    }

    /**
     * Move the files from clipboard to the current folder.
     */
    protected class MoveAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public MoveAction() {
            super(navigator, "Move");
        }

        public void actionPerformed(ActionEvent event, File[] files) {
            try {
                navigator.move(files, null);
            } catch (IOException e) {
                JOptionPane
                        .showConfirmDialog(null, e.getMessage(), "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean getEnabledState(File[] files) {
            if (files != null)
                return true;
            return false;
        }
    }

    /**
     * Rename the selected file/folder.
     */
    protected class RenameAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public RenameAction() {
            super(navigator, "Rename");
            navigator.getActionMap().put("RENAME", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "RENAME");
        }

        public void actionPerformed(ActionEvent event, File[] files) {
            try {
                navigator.rename(files[0]);
            } catch (IOException e) {
                JOptionPane
                        .showConfirmDialog(null, e.getMessage(), "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean getEnabledState(File[] files) {
            if (files != null && files.length == 1)
                return true;
            return false;
        }
    }

    /**
     * Refresh the navigator tree to synchronize with the filesystem.
     */
    protected class RefreshAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public RefreshAction() {
            super(navigator, "Refresh", Icons.REFRESH_ENABLED, Icons.REFRESH_DISABLED);
            navigator.getActionMap().put("REFRESH", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "REFRESH");
        }

        public void actionPerformed(ActionEvent e, File[] file) {
            navigator.refresh(file);
        }

        public boolean getEnabledState(File[] files) {
            if (files != null)
                return true;
            return false;
        }
    }

    /**
     * Go into the selected folder. The selected folder becomes the root for the
     * view.
     */
    protected class GoIntoAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public GoIntoAction() {
            super(navigator, "Go into", Icons.GOINTO_ENABLED, Icons.GOINTO_DISABLED);
        }

        public void actionPerformed(ActionEvent e, File[] files) {
            File directory;
            if (files[0].isDirectory())
                directory = files[0];
            else
                directory = files[0].getParentFile();
            navigator.goInto(directory);
        }

        public boolean getEnabledState(File[] files) {
            if (files != null && files.length == 1)
                return true;
            return false;
        }
    }

    /**
     * Go up a level.
     */
    protected class GoUpAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public GoUpAction() {
            super(navigator, "Go up", Icons.GOUP_ENABLED, Icons.GOUP_DISABLED);
        }

        public void actionPerformed(ActionEvent e, File[] file) {
            navigator.goUp();
        }

        public boolean getEnabledState(File[] files) {
            return true;
        }
    }

    /**
     * Set the default roots as roots.
     */
    protected class HomeAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public HomeAction() {
            super(navigator, "Home", Icons.HOME_ENABLED, Icons.HOME_DISABLED);
        }

        public void actionPerformed(ActionEvent e, File[] file) {
            navigator.home();
        }

        public boolean getEnabledState(File[] files) {
            return true;
        }
    }

    /**
     * Expand the Navigator tree.
     */
    protected class ExpandAllAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public ExpandAllAction() {
            super(navigator, "Expand all");
            navigator.getActionMap().put("EXPAND_ALL", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke('+'), "EXPAND_ALL");
        }

        public void actionPerformed(ActionEvent e, File[] file) {
            navigator.expandAll();
        }

        public boolean getEnabledState(File[] files) {
            return true;
        }
    }

    /**
     * Collapse the Navigator tree - at the end of this action only the root(s)
     * are visible.
     */
    protected class CollapseAllAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;

        public CollapseAllAction() {
            super(navigator, "Collapse all", Icons.COLLAPSEALL_ENABLED, Icons.COLLAPSEALL_DISABLED);
            navigator.getActionMap().put("COLLAPSE_ALL", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "COLLAPSE_ALL");
        }

        public void actionPerformed(ActionEvent e, File[] file) {
            navigator.collapseAll();
        }

        public boolean getEnabledState(File[] files) {
            return true;
        }
    }

    protected class PropertiesAction extends NavigatorAbstractAction {
        public PropertiesAction() {
            super(navigator, "Properties", Icons.PROPERTIES_ENABLED, Icons.PROPERTIES_ENABLED);
            navigator.getActionMap().put("PROPERTIES", this);
            navigator.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "PROPERTIES");
        }

        private static final long serialVersionUID = 1L;

        @Override public void actionPerformed(ActionEvent e, File[] file) {
            if (file.length != 1)
                return;
            navigator.editTestProperties(file[0]);
        }

        @Override public boolean getEnabledState(File[] files) {
            if (files.length == 1 && files[0].isFile()
                    && files[0].getPath().startsWith(System.getProperty(Constants.PROP_TEST_DIR)))
                return true;
            return false;
        }
    }

    /**
     * Create a JMenu containing the new file/folder actions.
     * 
     * @return menu, a JMenu with new file/folder actions.
     */
    protected JMenu createNewMenu() {
        JMenu newMenu = new JMenu("New");
        newMenu.add(new JMenuItem(new NewFolderAction()));
        newMenu.add(new JMenuItem(new NewFileAction()));
        return newMenu;
    }

    /**
     * Get the Menu items applicable for the current selection
     * 
     * @return list, a list of menu items
     */
    public List<Component> getMenuItems() {
        List<Component> menuItems = new Vector<Component>();
        menuItems.add(createNewMenu());
        menuItems.add(new JSeparator());
        menuItems.add(new JMenuItem(new CopyAction()));
        menuItems.add(new JMenuItem(new PasteAction()));
        menuItems.add(new JMenuItem(new DeleteAction()));
        menuItems.add(new JMenuItem(new MoveAction()));
        menuItems.add(new JMenuItem(new RenameAction()));
        menuItems.add(new JSeparator());
        menuItems.add(new JMenuItem(new ExpandAllAction()));
        menuItems.add(new JMenuItem(new CollapseAllAction()));
        menuItems.add(new JSeparator());
        menuItems.add(new JMenuItem(new GoIntoAction()));
        menuItems.add(new JMenuItem(new GoUpAction()));
        menuItems.add(new JMenuItem(new HomeAction()));
        menuItems.add(new JSeparator());
        menuItems.add(new JMenuItem(new RefreshAction()));
        menuItems.add(new JSeparator());
        menuItems.add(new JMenuItem(new PropertiesAction()));
        return menuItems;
    }

    /**
     * Get the toolbar to be displayed in the Navigator view.
     * 
     * @return toolbar, contains the actions
     */
    public VLToolBar getToolBar() {
        VLToolBar bar = new VLToolBar();
        bar.add(getActionButton(new GoUpAction()));
        bar.add(getActionButton(new HomeAction()));
        bar.addSeparator();
        bar.add(getActionButton(new CollapseAllAction()));
        bar.addSeparator();
        bar.add(getActionButton(new CopyAction()));
        bar.add(getActionButton(new PasteAction()));
        return bar;
    }

    private JButton getActionButton(Action action) {
        JButton button = UIUtils.createActionButton(action);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        if (action.getValue(Action.SMALL_ICON) != null)
            button.setText(null);
        return button;
    }

}
