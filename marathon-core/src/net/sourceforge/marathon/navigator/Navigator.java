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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.display.DisplayWindow;
import net.sourceforge.marathon.display.FileEventHandler;
import net.sourceforge.marathon.display.TextAreaOutput;
import net.sourceforge.marathon.testproperties.ui.TestPropertiesDialog;
import net.sourceforge.marathon.util.FilePatternMatcher;
import net.sourceforge.marathon.util.OSUtils;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarPanel;
import com.vlsolutions.swing.toolbars.VLToolBar;

/**
 * A class that provides a tree view of a file system.
 * 
 * @author kd
 * 
 */
public class Navigator implements Dockable, IFileEventListener {
    private static final Icon ICON_NAVIGATOR = new ImageIcon(
            TextAreaOutput.class.getResource("/net/sourceforge/marathon/display/icons/enabled/navigator.gif"));

    private static final DockKey DOCK_KEY = new DockKey("Navigator", "Navigator", "Project navigator", ICON_NAVIGATOR);

    static class RootFile extends File {
        private static final long serialVersionUID = 1L;
        private String description;

        public RootFile(String fileName) {
            this(fileName, null);
        }

        public RootFile(String fileName, String description) {
            super(fileName);
            this.description = description;
        }

        public String toString() {
            return description != null ? description : getName();
        }
    };

    private static String hideFilePattern = "";
    private static FilePatternMatcher hiddenFileMatcher;
    static {
        Preferences prefs = Preferences.userNodeForPackage(Constants.class);
        hideFilePattern = prefs.get(Constants.PREF_NAVIGATOR_HIDEFILES, "\\..* .*\\.class \\Q__init__.py\\E");
        hiddenFileMatcher = new FilePatternMatcher(hideFilePattern);
    }

    private final static class FileComparator implements Comparator<File> {
        public boolean equals(Object obj) {
            return false;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public int compare(File o1, File o2) {
            if (o1.isDirectory() == o2.isDirectory()) {
                return o1.getName().compareTo(o2.getName());
            }
            return o1.isDirectory() ? -1 : 1;
        }
    }

    private static class FileTypeFilter implements FileFilter {
        public boolean accept(File file) {
            if (file.isFile() && !hiddenFileMatcher.isMatch(file))
                return true;
            return false;
        }
    }

    private static class DirectoryTypeFilter implements FileFilter {
        public boolean accept(File file) {
            if (file.isDirectory() && !hiddenFileMatcher.isMatch(file))
                return true;
            return false;
        }
    }

    private static class NullFilter implements FileFilter {
        public boolean accept(File pathname) {
            return true;
        }
    }

    /**
     * A FileFilter that accepts both directories and files.
     */
    public static final NullFilter NULLFILTER = new NullFilter();
    /**
     * A FileFilter that accepts only files.
     */
    public static final FileTypeFilter FILEFILTER = new FileTypeFilter();
    /**
     * A FileFilter that accepts only directories.
     */
    public static final DirectoryTypeFilter DIRECTORYFILTER = new DirectoryTypeFilter();

    private class NavigatorMouseListener extends MouseAdapter {
        private boolean isPopupTrigger;

        public void mousePressed(MouseEvent e) {
            isPopupTrigger = e.isPopupTrigger();
            if (isPopupTrigger) {
                JTree tree = (JTree) e.getSource();
                TreePath clickPath = tree.getPathForLocation(e.getX(), e.getY());
                TreePath[] selectionPaths = tree.getSelectionPaths();
                for (int i = 0; selectionPaths != null && i < selectionPaths.length; i++) {
                    if (clickPath == selectionPaths[i]) {
                        return;
                    }
                }
                tree.setSelectionPath(clickPath);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() || isPopupTrigger) {
                fileMenu(e);
                return;
            }
            if (e.getClickCount() > 1) {
                fileAction(openAction, getSelectedFile(e), (e.getModifiersEx() & OSUtils.MOUSE_MENU_MASK) != 0);
                return;
            }
        }
    }

    private class TreeState {
        private List<File> expandedFolders;
        private File[] selectedFiles;

        private void saveTreeState(DefaultMutableTreeNode node) {
            saveExpandedFileList(node);
            selectedFiles = getSelectedFiles();
        }

        private void saveExpandedFileList(DefaultMutableTreeNode node) {
            TreePath path = new TreePath(model.getPathToRoot(node));
            Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(path);
            List<File> expandedFiles = getFilesFromPath(expandedPaths);
            expandedFolders = expandedFiles;
            if (node.getUserObject() instanceof File)
                expandedFolders.add((File) node.getUserObject());
        }

        private void restoreTreeState() {
            restoreExpandedFileList();
            restoreSelectedFiles();
        }

        private void restoreExpandedFileList() {
            for (int i = 0; i < expandedFolders.size(); i++)
                expandNode(findNode(expandedFolders.get(i)));
        }

        private void restoreSelectedFiles() {
            if (selectedFiles == null)
                return;
            List<TreePath> paths = new Vector<TreePath>();
            for (int i = 0; i < selectedFiles.length; i++) {
                DefaultMutableTreeNode node = findNode(selectedFiles[i]);
                if (node != null)
                    paths.add(new TreePath(model.getPathToRoot(node)));
            }
            tree.setSelectionPaths(paths.toArray(new TreePath[paths.size()]));
        }
    }

    private TreeState treeState;
    private List<Component> menuItems;
    private RootFile[] rootFiles;
    private JTree tree;
    private DefaultTreeModel model;
    private File renameFile;
    private File[] copiedFiles;
    private VLToolBar toolbar = null;
    private NavigatorFileAction selectAction = null;
    private NavigatorFileAction openAction = null;
    private FileFilter filter;
    private EventListenerList listeners = new EventListenerList();

    private ToolBarContainer component;

    private final FileEventHandler fileEventHandler;

    private final DisplayWindow displayWindow;

    /**
     * Construct a navigator object. If the Action items in the
     * <code>menuItems</code> and <code>toolbar</code> are derived from
     * <code>NavigatorAbstractAction</code>, special processing will be done to
     * enable and disable the corresponding items.
     * 
     * A default toolbar and popup menu are provided in case, any one of these
     * parameters is <code>null</code>.
     * 
     * @param rootDirectories
     *            The roots of the file system hierarchy
     * @param menuItems
     *            A list of menu items shown in the popup menu.
     * @param toolbar
     *            Toolbar attached to the internal frame.
     * @throws IOException
     *             if any of the directories is not accessible.
     */
    public Navigator(String[] rootDirectories, FileFilter filter, String[] rootNames, FileEventHandler fileEventhandler,
            DisplayWindow displayWindow) throws IOException {
        this.filter = filter;
        this.fileEventHandler = fileEventhandler;
        this.displayWindow = displayWindow;
        if (this.filter == null)
            this.filter = FILEFILTER;
        this.rootFiles = new RootFile[rootDirectories.length];
        for (int i = 0; i < rootDirectories.length; i++) {
            if (rootNames == null)
                rootFiles[i] = new RootFile(rootDirectories[i]);
            else
                rootFiles[i] = new RootFile(rootDirectories[i], rootNames[i]);
            if (!rootFiles[i].exists() || !rootFiles[i].isDirectory()) {
                throw new IOException("Invalid directory " + rootDirectories[i]);
            }
        }
        model = getTreeModel();
        tree = getTree();
    }

    public void setActions(NavigatorFileAction openAction, NavigatorFileAction selectAction) {
        this.openAction = openAction;
        this.selectAction = selectAction;
    }

    public void setToolbar(VLToolBar bar) {
        toolbar = bar;
    }

    public void setMenuItems(List<Component> menuItems) {
        this.menuItems = menuItems;
    }

    private JTree getTree() {
        JTree tree = new JTree();
        tree.setExpandsSelectedPaths(true);
        tree.setModel(model);
        tree.setCellRenderer(new NavigatorCellRenderer());
        tree.setRootVisible(false);
        tree.addMouseListener(new NavigatorMouseListener());
        tree.setEditable(false);
        tree.setInvokesStopCellEditing(true);
        tree.setCellEditor(new NavigatorCellEditor());
        tree.getCellEditor().addCellEditorListener(new CellEditorListener() {
            public void editingCanceled(ChangeEvent e) {
                renameCancelled();
            }

            public void editingStopped(ChangeEvent e) {
                renameStopped();
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                File[] selectedFiles = getSelectedFiles();
                updateToolBar(selectedFiles);
                if (selectedFiles != null && selectedFiles.length > 0)
                    fileAction(selectAction, selectedFiles[0], false);
            }

            private void updateToolBar(File[] selectedFiles) {
                if (toolbar == null)
                    return;
                Component[] components = toolbar.getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (components[i] instanceof JButton) {
                        JButton button = (JButton) components[i];
                        NavigatorAbstractAction action = ((NavigatorAbstractAction) button.getAction());
                        if (button.getAction() instanceof NavigatorAbstractAction)
                            action.setEnabled(action.getEnabledState(selectedFiles));
                    }
                }
            }
        });
        return tree;
    }

    private DefaultTreeModel getTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        for (int i = 0; i < rootFiles.length; i++) {
            boolean added = false;
            for (int j = 0; j < rootNode.getChildCount(); j++) {
                if (rootFiles[i].getName().compareTo(rootNode.getChildAt(j).toString()) < 0) {
                    rootNode.insert(getNodeForDirectory(rootFiles[i]), j);
                    added = true;
                    break;
                }
            }
            if (!added) {
                rootNode.add(getNodeForDirectory(rootFiles[i]));
            }
        }
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        model.setAsksAllowsChildren(true);
        return model;
    }

    /**
     * Get the embeddable component.
     * 
     * @return a <code>JComponent</code> that can be embedded into a panel.
     */
    public Component getComponent() {
        if (component != null)
            return component;
        if (toolbar == null)
            toolbar = new DefaultActions(this).getToolBar();
        if (menuItems == null)
            menuItems = new DefaultActions(this).getMenuItems();
        component = ToolBarContainer.createDefaultContainer(true, false, false, false, FlowLayout.RIGHT);
        ToolBarPanel tpanel = component.getToolBarPanelAt(BorderLayout.NORTH);
        tpanel.add(toolbar, new ToolBarConstraints(0, 0));
        component.add(new JScrollPane(tree), BorderLayout.CENTER);
        tree.setSelectionRow(0);
        expandAll();
        forceSelectionEvent();
        return component;
    }

    public JTree getJTree() {
        return tree;
    }

    private void getNodeForDirectory(DefaultMutableTreeNode root, File directory) {
        DefaultMutableTreeNode node = getNodeForDirectory(directory);
        root.add(node);
    }

    private DefaultMutableTreeNode getNodeForDirectory(File directory) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(directory, true);
        File[] list = directory.listFiles(DIRECTORYFILTER);
        if (list == null)
            return null;
        Arrays.sort(list, new FileComparator());
        for (int i = 0; i < list.length; i++) {
            getNodeForDirectory(node, list[i]);
        }
        FileFilter mergeFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return FILEFILTER.accept(pathname) && filter.accept(pathname);
            }
        };
        list = directory.listFiles(mergeFilter);
        Arrays.sort(list, new FileComparator());
        for (int i = 0; i < list.length; i++) {
            node.add(new DefaultMutableTreeNode(list[i], false));
        }
        return node;
    }

    private File[] getRoots() {
        return rootFiles;
    }

    private void fileAction(NavigatorFileAction a, File file, boolean useSystemApplication) {
        if (a == null || file == null)
            return;
        a.actionPerformed(file, useSystemApplication);
    }

    private File getSelectedFile(MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        DefaultMutableTreeNode node;
        if (path == null || (node = (DefaultMutableTreeNode) path.getLastPathComponent()) == null)
            return null;
        return (File) node.getUserObject();
    }

    private void fileMenu(MouseEvent e) {
        final File[] selectedFiles = getSelectedFiles();
        JPopupMenu menu = new JPopupMenu();
        for (Iterator<Component> iter = menuItems.iterator(); iter.hasNext();) {
            Component element = iter.next();
            if (element instanceof JMenu) {
                updateMenu((JMenu) element, selectedFiles);
            } else if (element instanceof JMenuItem) {
                updateMenuItem(element, selectedFiles);
            }
            menu.add(element);
        }
        menu.show(tree, e.getX(), e.getY());
    }

    private void updateMenuItem(Component element, File[] selectedFiles) {
        JMenuItem menuItem = (JMenuItem) element;
        if (menuItem.getAction() instanceof NavigatorAbstractAction) {
            NavigatorAbstractAction action = ((NavigatorAbstractAction) menuItem.getAction());
            action.setEnabled(action.getEnabledState(selectedFiles));
        }
    }

    private void updateMenu(JMenu menu, File[] selectedFiles) {
        Component[] components = menu.getMenuComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JMenu)
                updateMenu((JMenu) components[i], selectedFiles);
            else if (components[i] instanceof JMenuItem) {
                updateMenuItem(components[i], selectedFiles);
            }
        }
    }

    /**
     * Get the list of currently selected files.
     * 
     * @return Selected files
     */
    public File[] getSelectedFiles() {
        return getFilesFromPath(tree.getSelectionPaths());
    }

    private File[] getFilesFromPath(TreePath[] paths) {
        if (paths == null)
            return null;
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            files[i] = (File) node.getUserObject();
        }
        return files;
    }

    private List<File> getFilesFromPath(Enumeration<TreePath> paths) {
        List<File> files = new Vector<File>();
        while (paths != null && paths.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths.nextElement().getLastPathComponent();
            if (node.getUserObject() != null)
                files.add((File) node.getUserObject());
        }
        return files;
    }

    private void updateView(File file) {
        DefaultMutableTreeNode node = getValidNodeInHierarchy(file);
        if (node == null)
            return;
        saveTreeState(node);
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
        int position = parentNode.getIndex(node);
        DefaultMutableTreeNode newParentNode = getNodeForDirectory((File) node.getUserObject());
        model.removeNodeFromParent(node);
        model.insertNodeInto(newParentNode, parentNode, position);
        restoreTreeState();
        return;
    }

    private void restoreTreeState() {
        treeState.restoreTreeState();
    }

    private void saveTreeState(DefaultMutableTreeNode node) {
        treeState = new TreeState();
        treeState.saveTreeState(node);
    }

    private void expandNode(DefaultMutableTreeNode node) {
        if (node == null)
            return;
        TreePath path = new TreePath(model.getPathToRoot(node));
        tree.expandPath(path);
    }

    private void collapseNode(DefaultMutableTreeNode node) {
        if (node == null)
            return;
        TreePath path = new TreePath(model.getPathToRoot(node));
        tree.collapsePath(path);
    }

    private DefaultMutableTreeNode getValidNodeInHierarchy(File file) {
        DefaultMutableTreeNode node = null;
        if (file.isFile())
            file = file.getParentFile();
        while (file != null && node == null) {
            node = findNode(file);
            file = file.getParentFile();
        }
        return node;
    }

    private DefaultMutableTreeNode findNode(File file) {
        DefaultMutableTreeNode node = null;
        try {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            for (int i = 0; i < root.getChildCount(); i++) {
                if ((node = findNode((DefaultMutableTreeNode) root.getChildAt(i), file.getCanonicalFile())) != null)
                    return node;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return node;
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode root, File file) throws IOException {
        if (((File) root.getUserObject()).getCanonicalFile().equals(file))
            return root;
        DefaultMutableTreeNode node = null;
        for (int i = 0; i < root.getChildCount(); i++) {
            if ((node = findNode((DefaultMutableTreeNode) root.getChildAt(i), file)) != null)
                return node;
        }
        return node;
    }

    public void makeVisible(File file) {
        refresh(new File[] { file.getParentFile() });
        TreeNode node = findNode(file);
        TreeNode[] nodes = model.getPathToRoot(node);
        if (nodes == null)
            return;
        TreePath path = new TreePath(nodes);
        tree.makeVisible(path);
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
    }

    /**
     * Rename given file.
     * 
     * @param file
     * @throws IOException
     */
    public void rename(File file) throws IOException {
        if (isRoot(file))
            throw new IOException("Can not rename root directories");
        renameFile = file;
        DefaultMutableTreeNode node = findNode(file);
        tree.setEditable(true);
        TreeNode[] nodes = model.getPathToRoot(node);
        TreePath path = new TreePath(nodes);
        tree.startEditingAtPath(path);
        return;
    }

    private void renameStopped() {
        tree.setEditable(false);
        TreePath path = tree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (((File) node.getUserObject()).exists()) {
            JOptionPane.showConfirmDialog(null, "Rename operation failed: file with that name exists", "Error",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            int position = parent.getIndex(node);
            model.removeNodeFromParent(node);
            model.insertNodeInto(new DefaultMutableTreeNode(renameFile, renameFile.isDirectory()), parent, position);
            makeVisible(renameFile);
        } else if (!renameFile.renameTo((File) node.getUserObject())) {
            JOptionPane.showConfirmDialog(null, "Rename operation failed", "Error", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            int position = parent.getIndex(node);
            model.removeNodeFromParent(node);
            model.insertNodeInto(new DefaultMutableTreeNode(renameFile, renameFile.isDirectory()), parent, position);
            makeVisible(renameFile);
        } else {
            makeVisible((File) node.getUserObject());
            fileEventHandler.fireRenameEvent(renameFile, (File) node.getUserObject());
        }
    }

    private void renameCancelled() {
        tree.setEditable(false);
    }

    private void createFile(File startDirectory, File[] roots, boolean isDirectory, String[] validExtensions,
            String defaultExtension) throws IOException {
        if (roots == null)
            roots = getRoots();
        String approveText = "Create New File";
        if (isDirectory)
            approveText = "Create New Folder";
        JFileChooser fileChooser = getFileChooser(startDirectory, roots, validExtensions);
        int option = fileChooser.showDialog(null, approveText);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (selected.exists()) {
                throw new IOException("Another Folder/File with the same name exists");
            }
            if (isDirectory) {
                if (!selected.mkdir())
                    throw new IOException("Directory " + selected.getName() + " Can not be created");
            } else {
                if (validExtensions != null) {
                    boolean validExtension = false;
                    for (int i = 0; i < validExtensions.length; i++) {
                        if (selected.getName().endsWith(validExtensions[i])) {
                            validExtension = true;
                            break;
                        }
                    }
                    if (!validExtension && defaultExtension != null) {
                        selected = new File(selected.getParentFile(), selected.getName() + defaultExtension);
                    }
                }
                selected.createNewFile();
            }
            updateView(selected.getParentFile());
            makeVisible(selected);
            fileEventHandler.fireNewEvent(selected);
        }
    }

    private JFileChooser getFileChooser(File startDirectory, File[] roots, final String[] validExtensions) {
        JFileChooser fileChooser = new JFileChooser(startDirectory, new NavigatorFSV(roots));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(true);
        if (validExtensions != null && validExtensions.length > 0) {
            fileChooser.setAcceptAllFileFilterUsed(false);
            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory() || validExtensions.length == 0)
                        return true;
                    for (int i = 0; i < validExtensions.length; i++) {
                        if (f.getName().endsWith(validExtensions[i]))
                            return true;
                    }
                    return false;
                }

                public String getDescription() {
                    if (validExtensions.length > 0) {
                        String description = validExtensions[0];
                        for (int i = 1; i < validExtensions.length; i++) {
                            description += " " + validExtensions[i];
                        }
                        return description;
                    }
                    return "";
                }
            };
            fileChooser.setFileFilter(filter);
        }
        return fileChooser;
    }

    /**
     * Delete the given files.
     * 
     * @param files
     *            Files to be deleted
     * @throws IOException
     *             If an error occurs while deleting. However, the deleting
     *             continues for the rest of the files. A consolidated error
     *             message is thrown back.
     */
    public void deleteFiles(File[] files) throws IOException {
        deleteFiles(files, true);
    }

    /**
     * Delete the given files.
     * 
     * @param files
     *            Files to be deleted
     * @throws IOException
     *             If an error occurs while deleting. However, the deleting
     *             continues for the rest of the files. A consolidated error
     *             message is thrown back.
     */
    void deleteFiles(File[] files, boolean confirm) throws IOException {
        if (containsRoot(files))
            throw new IOException("Can not delete root directories");
        StringBuffer failedMessages = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            int option = deleteSingle(files[i], confirm, failedMessages);
            if (option == 2)
                confirm = false;
            else if (option == 3)
                break;
        }
        if (!failedMessages.toString().equals(""))
            throw new IOException("Problems encountered during delete\n" + failedMessages.toString());
    }

    private int deleteSingle(File file, boolean confirm, StringBuffer failedMessages) {
        Object[] options = { "Yes", "No", "Yes to all", "Cancel" };
        int option = JOptionPane.OK_OPTION;
        if (confirm) {
            String message = "Do you want to really delete " + file.getName() + "?";
            if (file.isDirectory())
                message = "Do you want to really delete " + file.getName() + " and all it's contents?";
            option = JOptionPane.showOptionDialog(null, message, "Confirm", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        if (option == 0 || option == 2) {
            deleteRecursive(file, failedMessages);
            updateView(file.getParentFile());
        }
        fileEventHandler.fireDeleteEvent(file);
        return option;
    }

    private void deleteRecursive(File file, StringBuffer failedMessages) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteRecursive(list[i], failedMessages);
            }
        }
        if (file.exists() && !file.delete()) {
            failedMessages.append("Folder " + file.getName() + " can not be deleted\n");
        }
    }

    private boolean containsRoot(File[] files) {
        boolean bRoot = false;
        for (int i = 0; i < files.length; i++) {
            if ((bRoot = isRoot(files[i])) == true)
                break;
        }
        return bRoot;
    }

    private boolean isRoot(File file) {
        boolean bRoot = false;
        try {
            file = file.getCanonicalFile();
            File[] roots = getRoots();
            for (int i = 0; i < roots.length; i++) {
                if (file.equals(roots[i].getCanonicalFile())) {
                    bRoot = true;
                    break;
                }
            }
        } catch (Exception e) {
            bRoot = true;
        }
        return bRoot;
    }

    /**
     * Move the given files into a given directory.
     * 
     * @param files
     *            Files selected for moving
     * @param destination
     *            The destination directory.
     */
    public void moveToDirectory(File[] files, File destDir) throws IOException {
        if (containsRoot(files))
            throw new IOException("Can not move root directories");
        if (destDir != null)
            moveFiles(files, destDir, false);
    }

    /**
     * Move the given files into a directory. The directory can be choosen
     * starting from the roots provided.
     * 
     * @param files
     *            Files selected for moving
     * @param roots
     *            The roots for the operation. If null, the default roots will
     *            be used.
     */
    public void move(File[] files, File[] roots) throws IOException {
        if (containsRoot(files))
            throw new IOException("Can not move root directories");
        File destDir = getDestinationDirectory(roots);
        moveToDirectory(files, destDir);
    }

    private File getDestinationDirectory(File[] roots) {
        if (roots == null)
            roots = getRoots();
        JFileChooser fileChooser = new JFileChooser(roots[0], new NavigatorFSV(roots));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showDialog(null, "OK");
        File destDir = null;
        if (option == JFileChooser.APPROVE_OPTION) {
            destDir = fileChooser.getSelectedFile();
        }
        return destDir;
    }

    private void moveFiles(File[] files, File destDir, boolean isCopy) {
        DefaultMutableTreeNode destNode = findNode(destDir);
        List<File> newSelection = new Vector<File>();
        for (int i = 0; i < files.length; i++) {
            if (isCopy)
                copy(files[i], destDir, destNode, newSelection);
            else
                move(files[i], destDir, destNode, newSelection);
        }
        updateView(destDir);
        TreePath[] selectionPath = new TreePath[newSelection.size()];
        int i = 0;
        Iterator<File> iterator = newSelection.iterator();
        while (iterator.hasNext()) {
            selectionPath[i++] = new TreePath(model.getPathToRoot(findNode(iterator.next())));
        }
        tree.setSelectionPaths(selectionPath);
    }

    private boolean move(File file, File destDir, DefaultMutableTreeNode destNode, List<File> selection) {
        File newFile = new File(destDir, file.getName());
        if (file.renameTo(newFile) == false) {
            JOptionPane.showConfirmDialog(null, "File " + file.getName() + " Couldn't be moved", "Error",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        DefaultMutableTreeNode node = findNode(file);
        model.removeNodeFromParent(node);
        if (destNode != null) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFile);
            model.insertNodeInto(newNode, destNode, 0);
            selection.add(newFile);
        }
        fileEventHandler.fireMoveEvent(file, newFile);
        return true;
    }

    /**
     * Refresh the view by fetching the information from the file system.
     * 
     * @param files
     */
    public void refresh(File[] files) {
        for (int i = 0; i < files.length; i++) {
            updateView(files[i]);
        }
    }

    /**
     * Refresh the whole navigator tree
     */
    public void refresh() {
        refresh(rootFiles);
    }

    /**
     * Copy the given files into an (internal) list that is used for subsequent
     * <code>
     * paste</code> operation.
     * 
     * @param files
     *            Files to be copied.
     */
    public void copy(File[] files) {
        copiedFiles = files;
        forceSelectionEvent();
    }

    private void forceSelectionEvent() {
        int[] selectionRows = tree.getSelectionRows();
        tree.setSelectionRow(-1);
        tree.setSelectionRows(selectionRows);
    }

    /**
     * Paste the copied files into the destination
     * 
     * @param file
     *            The destination directory
     */
    public void paste(File file) throws IOException {
        File destination;
        if (file.isFile())
            destination = file.getParentFile();
        else
            destination = file;
        for (int i = 0; i < copiedFiles.length; i++) {
            if (destination.getCanonicalPath().startsWith(copiedFiles[i].getCanonicalPath()))
                throw new IOException("Recursion detected");
        }
        moveFiles(copiedFiles, destination, true);
        copiedFiles = null;
        forceSelectionEvent();
    }

    private boolean copy(File file, File destDir, DefaultMutableTreeNode destNode, List<File> selection) {
        File newFile = new File(destDir, file.getName());
        while (newFile.exists()) {
            newFile = new File(destDir, "CopyOf" + newFile.getName());
        }
        if (copyContents(file, newFile) == false) {
            return false;
        }
        if (destNode != null) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFile);
            model.insertNodeInto(newNode, destNode, 0);
            selection.add(newFile);
        }
        fileEventHandler.fireCopyEvent(file, newFile);
        return true;
    }

    private boolean copyContents(File file, File newFile) {
        if (file.isDirectory()) {
            newFile.mkdir();
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                copyContents(list[i], new File(newFile, list[i].getName()));
            }
            return true;
        }
        try {
            FileReader reader = new FileReader(file);
            FileWriter writer = new FileWriter(newFile);
            char[] cbuf = new char[8192];
            int nchar;
            while ((nchar = reader.read(cbuf)) > 0)
                writer.write(cbuf, 0, nchar);
            writer.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Go into the directory. The selected directory will become the new root.
     * 
     * @param directory
     *            the new root directory.
     */
    public void goInto(File directory) {
        DefaultMutableTreeNode node = findNode(directory);
        saveTreeState(node);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++)
            model.removeNodeFromParent((DefaultMutableTreeNode) root.getFirstChild());
        model.insertNodeInto(node, root, 0);
        restoreTreeState();
    }

    /**
     * Go up the hierarchy. The parent of the current will become the new root.
     * Does not have any effect, if already at the root.
     */
    public void goUp() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        if (root.getChildCount() > 1)
            return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(0);
        File directory = (File) node.getUserObject();
        if (isRoot(directory)) {
            home();
            return;
        }
        DefaultMutableTreeNode parentNode = getNodeForDirectory(directory.getParentFile());
        if (parentNode == null)
            return;
        saveTreeState(node);
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            model.removeNodeFromParent((DefaultMutableTreeNode) root.getFirstChild());
        }
        model.insertNodeInto(parentNode, root, 0);
        restoreTreeState();
    }

    /**
     * Go up the hierarchy till root is reached.
     */
    public void home() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        saveTreeState(root);
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            model.removeNodeFromParent((DefaultMutableTreeNode) root.getFirstChild());
        }
        for (int i = 0; i < rootFiles.length; i++)
            model.insertNodeInto(getNodeForDirectory(rootFiles[i]), root, i);
        restoreTreeState();
    }

    /**
     * Collapse the directory hierarchy. After this call only the root
     * directories will be visible.
     */
    public void collapseAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            collapse((DefaultMutableTreeNode) root.getChildAt(i));
        }
    }

    private void collapse(DefaultMutableTreeNode node) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++)
            collapse((DefaultMutableTreeNode) node.getChildAt(i));
        collapseNode(node);
    }

    /**
     * Expand the directory heirarchy starting from root.
     */
    public void expandAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            expand(root.getChildAt(i));
        }
    }

    private void expand(TreeNode node) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++)
            expand(node.getChildAt(i));
        expandNode((DefaultMutableTreeNode) node);
    }

    /**
     * Create a new file.
     * 
     * @param startDirectory
     *            Starting directory in the file system
     * @param roots
     *            The file system roots. These roots will override the
     *            directories provided in the constructor.
     * @throws IOException
     *             If an error occurs
     */
    public void createNewFile(File startDirectory, File[] roots, String[] validExtensions, String defaultExtension)
            throws IOException {
        boolean isDirectory = false;
        createFile(startDirectory, roots, isDirectory, validExtensions, defaultExtension);
    }

    /**
     * Create a new file.
     * 
     * @param startDirectory
     *            Starting directory in the file system
     * @param roots
     *            The file system roots. These roots will override the
     *            directories provided in the constructor.
     * @throws IOException
     *             If an error occurs
     */
    public void createNewFolder(File startDirectory, File[] roots) throws IOException {
        boolean isDirectory = true;
        createFile(startDirectory, roots, isDirectory, null, null);
    }

    /**
     * Get the file list that was copied.
     * 
     * @return The file list selected during last copy operation. Can be null.
     */
    public File[] getCopiedFiles() {
        return copiedFiles;
    }

    public static String getHideFilePattern() {
        return hideFilePattern;
    }

    public static void setHideFilePattern(String hideFilePattern) {
        if (hideFilePattern == null)
            Navigator.hideFilePattern = "\\..* .*\\.class \\Q__init__.py\\E";
        else
            Navigator.hideFilePattern = hideFilePattern;
        hiddenFileMatcher = new FilePatternMatcher(hideFilePattern);
    }

    public DockKey getDockKey() {
        return DOCK_KEY;
    }

    public void addNavigatorListener(IFileEventListener l) {
        listeners.add(IFileEventListener.class, l);
    }

    public ActionMap getActionMap() {
        return tree.getActionMap();
    }

    public InputMap getInputMap() {
        return tree.getInputMap();
    }

    public void fileRenamed(File from, File to) {
        updateView(to);
    }

    public void fileDeleted(File file) {
        updateView(file);
    }

    public void fileCopied(File from, File to) {
        updateView(to);
    }

    public void fileMoved(File from, File to) {
        updateView(to);
    }

    public void fileCreated(File file) {
        updateView(file);
    }

    public void fileUpdated(File file) {
        updateView(file);
    }

    public void editTestProperties(File file) {
        TestPropertiesDialog propsDialog = new TestPropertiesDialog(displayWindow, file);
        propsDialog.setVisible(true);
        if (propsDialog.isSaved())
            fileEventHandler.fireUpdateEvent(file);
    }

}
