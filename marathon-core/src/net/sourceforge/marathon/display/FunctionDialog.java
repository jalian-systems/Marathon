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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.api.module.Argument;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.navigator.Icons;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.VLToolBar;

class FunctionDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private static String ICON_PATH = "net/sourceforge/marathon/display/icons/enabled/";
    public static final Icon ICONS_FOLDER = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "folder.gif"));
    public static final Icon ICONS_FILE = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "file.gif"));
    public static final Icon ICONS_FUNCTION = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "function.gif"));
    private JTree tree;
    private OkHandler okHandler;
    private IEditor documentArea;
    private DisplayWindow window;
    private JPanel argumentPanel;
    private DefaultMutableTreeNode functionNode;
    private final String windowName;
    private JCheckBox filterByWindowName;
    private Module root;
    private JButton okButton;
    private JButton cancelButton;

    private final class OkHandler implements ActionListener, Serializable {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            handleEvent();
        }

        private void handleEvent() {
            if (FunctionDialog.this.functionNode == null)
                return;
            Component[] components = argumentPanel.getComponents();
            String[] arguments = getArguments(Arrays.asList(components));
            window.insertScript(ScriptModelClientPart.getModel().getFunctionCallForInsertDialog(
                    (Function) functionNode.getUserObject(), arguments));
            FunctionDialog.this.dispose();
        }

        public String[] getArguments(List<Component> valueFields) {
            String[] args = new String[valueFields.size() / 2];
            for (int i = 1; i < valueFields.size(); i += 2)
                if (valueFields.get(i) instanceof JTextField)
                    args[i / 2] = ((JTextField) valueFields.get(i)).getText();
                else
                    args[i / 2] = ((JComboBox) valueFields.get(i)).getSelectedItem().toString();
            return args;
        }

    }

    static class FunctionNodeRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            String fqn;
            if (userObject instanceof Module)
                fqn = ((Module) userObject).getName();
            else if (userObject instanceof Function)
                fqn = ((Function) userObject).getName();
            else
                fqn = userObject == null ? "Null" : userObject.toString();
            Component comp = super.getTreeCellRendererComponent(tree, fqn, selected, expanded, leaf, row, hasFocus);
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (userObject instanceof Module) {
                    if (((Module) userObject).isFile())
                        label.setIcon(ICONS_FILE);
                    else
                        label.setIcon(ICONS_FOLDER);
                } else if (userObject instanceof Function) {
                    label.setIcon(ICONS_FUNCTION);
                }
            }
            return comp;
        }
    }

    class FunctionNodeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = null;
            TreePath path = tree.getSelectionPath();
            String doc = "";
            boolean enabled = false;
            if (path != null) {
                node = (DefaultMutableTreeNode) path.getLastPathComponent();
                functionNode = node;
                Object userObject = node.getUserObject();
                if (userObject instanceof Module)
                    doc = ((Module) userObject).getDocumentation();
                else {
                    doc = ((Function) userObject).getDocumentation();
                    enabled = true;
                }
            }

            okButton.setEnabled(enabled);

            documentArea.setText(doc);
            documentArea.setCaretPosition(0);
            argumentPanel.removeAll();
            if (node != null && !node.getAllowsChildren()) {
                createArgumentPanel(node);
            } else
                functionNode = null;
            argumentPanel.setBorder(new EtchedBorder());
            argumentPanel.updateUI();
        }

        private void createArgumentPanel(DefaultMutableTreeNode node) {
            Function f = (Function) node.getUserObject();
            argumentPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(2, 2, 2, 2);
            constraints.gridwidth = 2;
            List<Argument> arguments = f.getArguments();
            int index = 0;
            for (Argument argument : arguments) {
                constraints.gridy = index++;
                constraints.gridx = 0;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                JLabel label = new JLabel(argument.getName() + ": ");
                argumentPanel.add(label, constraints);
                constraints.gridx = 2;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                if (argument.getDefault() == null && argument.getDefaultList() == null) {
                    JTextField value = new JTextField(20);
                    argumentPanel.add(value, constraints);
                } else if (argument.getDefault() != null) {
                    JTextField value = new JTextField(20);
                    value.setText(argument.getDefault());
                    argumentPanel.add(value, constraints);
                } else {
                    JComboBox value = new JComboBox(argument.getDefaultList().toArray());
                    value.setEditable(true);
                    value.setSelectedIndex(0);
                    argumentPanel.add(value, constraints);
                }
            }
        }
    }

    public FunctionDialog(DisplayWindow window, Module root, String windowName) {
        super((JFrame) null, "Insert Script", true);
        this.window = window;
        this.root = root;
        this.windowName = windowName;
        okButton = UIUtils.createOKButton();
        okHandler = new OkHandler();
        okButton.addActionListener(okHandler);
        cancelButton = UIUtils.createCancelButton();
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FunctionDialog.this.dispose();
            }
        });
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, createTree(), createFunctionPanel());
        pane.setDividerLocation(pane.getPreferredSize().width / 3);
        pane.setBorder(BorderFactory.createEmptyBorder());
        getContentPane().add(pane);
        okButton.setEnabled(false);
        tree.setSelectionRow(0);
        pack();
        setLocationRelativeTo(window);
    }

    private JComponent createTree() {
        ToolBarContainer treePanel = ToolBarContainer.createDefaultContainer(true, true, true, true, FlowLayout.RIGHT);
        treePanel.setBorder(new EtchedBorder());
        VLToolBar bar = new VLToolBar();
        JButton button = UIUtils.createExpandAllButton();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expandAll(tree, true);
            }
        });
        bar.add(button);
        button = UIUtils.createCollapseAllButton();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expandAll(tree, false);
            }
        });
        bar.add(button);
        button = UIUtils.createRefreshButton();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Module module = window.refreshModuleFunctions();
                DefaultMutableTreeNode rootNode = module.createTreeNode(filterByWindowName.isSelected() ? windowName : null);
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.setRoot(rootNode);
                root = module;
                expandAll(tree, true);
                tree.setSelectionRows(new int[] {});
                argumentPanel.removeAll();
            }
        });
        bar.add(button);
        treePanel.getToolBarPanelAt(BorderLayout.NORTH).add(bar, new ToolBarConstraints());
        filterByWindowName = new JCheckBox("Filter by window name", true);
        filterByWindowName.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                DefaultMutableTreeNode rootNode = root.refreshNode(filterByWindowName.isSelected() ? windowName : null);
                ((DefaultTreeModel) tree.getModel()).reload(rootNode);
                expandAll(tree, true);
                tree.setSelectionRows(new int[] {});
            }
        });
        treePanel.add(filterByWindowName, BorderLayout.SOUTH);
        tree = new JTree(root.createTreeNode(windowName));
        tree.setRootVisible(false);
        tree.getSelectionModel().addTreeSelectionListener(new FunctionNodeSelectionListener());
        tree.setCellRenderer(new FunctionNodeRenderer());
        expandAll(tree, true);
        treePanel.add(new JScrollPane(tree), BorderLayout.CENTER);
        return treePanel;
    }

    protected void dump(DefaultMutableTreeNode newRoot) {
        Object o = newRoot.getUserObject();
        String name;
        if (o instanceof Module)
            name = ((Module) o).getName();
        else
            name = ((Function) o).getName();
        System.out.print('(' + name);
        @SuppressWarnings("rawtypes")
        Enumeration children = newRoot.children();
        while (children.hasMoreElements())
            dump((DefaultMutableTreeNode) children.nextElement());
        System.out.print(')');

    }

    private JComponent createFunctionPanel() {
        JSplitPane functionPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        functionPane.setBorder(BorderFactory.createEmptyBorder());
        functionPane.setTopComponent(createDocumentPane());
        argumentPanel = new JPanel();
        functionPane.setBottomComponent(createEmptyArgumentPanel());
        functionPane.setDividerLocation(0.6);
        return functionPane;
    }

    private JComponent createEmptyArgumentPanel() {
        argumentPanel = new JPanel();
        return new JScrollPane(argumentPanel);
    }

    private Component createDocumentPane() {
        documentArea = window.getEditorProvider().get(false, 0, IEditorProvider.EditorType.OTHER);
        documentArea.setEditable(false);
        ((JTextArea) documentArea).setRows(10);
        ((JTextArea) documentArea).setColumns(80);
        documentArea.setMode("ruby");
        return documentArea.getComponent();
    }

    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        if (tree.isRootVisible())
            expandAll(tree, new TreePath(root), expand);
        else {
            TreePath parent = new TreePath(root);
            TreeNode node = (TreeNode) parent.getLastPathComponent();
            if (node.getChildCount() >= 0) {
                for (@SuppressWarnings("rawtypes")
                Enumeration e = node.children(); e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    expandAll(tree, path, expand);
                }
            }
        }
    }

    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (@SuppressWarnings("rawtypes")
            Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}
