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
package net.sourceforge.marathon.recorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.module.Argument;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

public class ModuleFunctionsMenu extends AbstractContextMenu implements IContextMenu, IRecordingArtifact {

    private static class ParameterDialog extends EscapeDialog implements IRecordingArtifact {
        private static final long serialVersionUID = 1L;
        protected static final int OK = 1;
        private static final int CANCEL = 2;
        private ArrayList<Component> valueFields;
        protected int retValue;
        private JButton ok;
        private JButton cancel;

        public ParameterDialog(Window parent, Function function) {
            valueFields = new ArrayList<Component>();
            setTitle("Parameters");
            Container argumentPanel = getArgumentPanel(function);
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(argumentPanel, BorderLayout.CENTER);
            JPanel buttonBar = createButtonBar();
            contentPane.add(buttonBar, BorderLayout.SOUTH);
            setLocationRelativeTo(parent);
            setModal(true);
            pack();
        }

        private JPanel createButtonBar() {
            JPanel buttonBar = new JPanel();
            buttonBar.setLayout(new GridLayout(1, 2));
            ok = UIUtils.createOKButton();
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    retValue = OK;
                    ParameterDialog.this.dispose();
                }
            });
            buttonBar.add(ok);
            cancel = UIUtils.createCancelButton();
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    retValue = CANCEL;
                    ParameterDialog.this.dispose();
                }
            });
            buttonBar.add(cancel);
            return buttonBar;
        }

        private Container getArgumentPanel(Function function) {
            Container argumentPanel = new JPanel();
            argumentPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(2, 2, 2, 2);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridwidth = 2;
            int index = 0;
            List<Argument> arguments = function.getArguments();
            for (Argument argument : arguments) {
                constraints.gridy = index++;
                constraints.gridx = 0;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                JLabel label = new JLabel(argument.getName());
                argumentPanel.add(label, constraints);
                constraints.gridx = 2;
                constraints.fill = GridBagConstraints.HORIZONTAL;

                if (argument.getDefault() == null && argument.getDefaultList() == null) {
                    JTextField value = new JTextField(20);
                    argumentPanel.add(value, constraints);
                    valueFields.add(value);
                } else if (argument.getDefault() != null) {
                    JTextField value = new JTextField(20);
                    value.setText(argument.getDefault());
                    argumentPanel.add(value, constraints);
                    valueFields.add(value);
                } else {
                    JComboBox value = new JComboBox(argument.getDefaultList().toArray());
                    value.setEditable(true);
                    value.setSelectedIndex(0);
                    argumentPanel.add(value, constraints);
                    valueFields.add(value);
                }
            }
            return argumentPanel;
        }

        public String[] getArguments() {
            String[] args = new String[valueFields.size()];
            for (int i = 0; i < valueFields.size(); i++)
                if (valueFields.get(i) instanceof JTextField)
                    args[i] = ((JTextField) valueFields.get(i)).getText();
                else
                    args[i] = ((JComboBox) valueFields.get(i)).getSelectedItem().toString();
            return args;
        }

        public int showDialog() {
            setVisible(true);
            return retValue;
        }

        @Override public JButton getOKButton() {
            return ok;
        }

        @Override public JButton getCloseButton() {
            return cancel;
        }
    }

    private static class FunctionNodeRenderer extends DefaultTreeCellRenderer {
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
            return comp;
        }
    }

    private class FunctionNodeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = null;
            TreePath path = tree.getSelectionPath();
            String doc = "";
            if (path != null) {
                node = (DefaultMutableTreeNode) path.getLastPathComponent();
                functionNode = node;
                Object userObject = node.getUserObject();
                if (userObject instanceof Module)
                    doc = ((Module) userObject).getDocumentation();
                else
                    doc = ((Function) userObject).getDocumentation();
            }

            documentArea.setText(doc);
            documentArea.setCaretPosition(0);
            insertButton.setEnabled(node != null && !node.getAllowsChildren());
            functionNode = node;
        }
    }

    private JTree tree;
    private JTextArea documentArea;
    private IMarathonRuntime runtime;
    private DefaultMutableTreeNode functionNode;
    private JButton insertButton;

    public ModuleFunctionsMenu(ContextMenuWindow window, IRecorder recorder, ComponentFinder finder, IMarathonRuntime runtime,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(window, recorder, finder, scriptModel, windowMonitor);
        this.runtime = runtime;
    }

    public Component getContent() {
        Module topModule = runtime.getModuleFunctions();
        DefaultMutableTreeNode root = topModule.createTreeNode(null);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createFunctionPanel(root), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        insertButton = UIUtils.createInsertButton();
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertButton.setEnabled(false);
                final String[] args;
                Component root = SwingUtilities.getRoot(tree);
                Window parent = null;
                if (root instanceof Window)
                    parent = (Window) root;
                Function f = (Function) functionNode.getUserObject();
                if (f.getArguments().size() == 0)
                    args = new String[0];
                else {
                    ParameterDialog dialog = new ParameterDialog(parent, f);
                    if (dialog.showDialog() != ParameterDialog.OK)
                        return;
                    args = dialog.getArguments();
                }
                Thread thread = new Thread() {
                    public void run() {
                        // We need to run in a non-dispatch thread, because the
                        // script might run things in
                        // the dispatch thread
                        try {
                            window.setIgnoreMouseEvents(true);
                            String script = scriptModel.getFunctionCallForInsertDialog((Function) functionNode.getUserObject(),
                                    args);
                            runtime.exec(script);
                            getRecorder().recordInsertScriptElement(runtime.getTopWindowId(), script);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(getWindow(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        } finally {
                            try {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    public void run() {
                                        window.setIgnoreMouseEvents(false);
                                        insertButton.setEnabled(true);
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();

            }
        });
        insertButton.setEnabled(false);
        buttonPanel.add(insertButton);
        return buttonPanel;
    }

    public String getName() {
        return "Modules";
    }

    public void setComponent(Component component, Point point, boolean isTriggered) {
    }

    private JSplitPane createFunctionPanel(DefaultMutableTreeNode root) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setTopComponent(new JScrollPane(createTree(root)));
        documentArea = new JTextArea(4, 0);
        documentArea.setEditable(false);
        documentArea.setLineWrap(true);
        documentArea.setWrapStyleWord(true);
        splitPane.setBottomComponent(new JScrollPane(documentArea));
        return splitPane;
    }

    private JComponent createTree(DefaultMutableTreeNode root) {
        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.getSelectionModel().addTreeSelectionListener(new FunctionNodeSelectionListener());
        tree.setCellRenderer(new FunctionNodeRenderer());
        tree.expandRow(tree.getRowCount() - 1);
        return new JScrollPane(tree);
    }
}
