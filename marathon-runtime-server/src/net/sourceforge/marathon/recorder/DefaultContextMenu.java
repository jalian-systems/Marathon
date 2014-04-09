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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.action.AssertContent;
import net.sourceforge.marathon.action.AssertPropertyAction;
import net.sourceforge.marathon.action.WaitPropertyAction;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MCollectionComponent;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.util.UIUtils;

public class DefaultContextMenu extends AbstractContextMenu implements IContextMenu {

    static class AssertionTreeNodeRenderer implements TreeCellRenderer {
        private Color bgSel;
        private Color fgSel;
        private Color bgNonSel;
        private Color fgNonSel;
        private SimpleAttributeSet valueStyle;
        private SimpleAttributeSet propertyStyle;
        private Color valueForegroundColor = new Color(0x00, 0x00, 0xa4);

        public AssertionTreeNodeRenderer() {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            fgSel = renderer.getTextSelectionColor();
            fgNonSel = renderer.getTextNonSelectionColor();
            bgSel = renderer.getBackgroundSelectionColor();
            bgNonSel = renderer.getBackgroundNonSelectionColor();
            valueStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(valueStyle, valueForegroundColor);
            propertyStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(propertyStyle, fgNonSel);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            JTextPane pane = new JTextPane();
            if (sel) {
                pane.setBackground(bgSel);
                pane.setForeground(fgSel);
            } else {
                pane.setBackground(bgNonSel);
                pane.setForeground(fgNonSel);
            }
            AssertionTreeNode node = (AssertionTreeNode) value;
            pane.setText("");
            try {
                pane.getDocument().insertString(pane.getDocument().getLength(), node.getProperty() + " {", propertyStyle);
                pane.getDocument().insertString(pane.getDocument().getLength(),
                        node.getDisplayNode().replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r"), valueStyle);
                pane.getDocument().insertString(pane.getDocument().getLength(), "}", propertyStyle);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            return pane;
        }
    }

    protected static final int ASSERT_ACTION = 1;
    protected static final int WAIT_ACTION = 2;
    private JTextArea textArea;
    protected JTree assertionTree;
    private DefaultMutableTreeNode rootNode;
    protected MComponent mcomponent;
    protected DefaultTreeModel assertionTreeModel;
    private JButton insertAssertionButton;
    private JButton insertWaitButton;

    public DefaultContextMenu(ContextMenuWindow window, IRecorder recorder, ComponentFinder finder,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(window, recorder, finder, scriptModel, windowMonitor);
    }

    public Component getContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JSplitPane splitPane = getAssertionPanel();
        JPanel buttonPanel = getButtonPanel();
        mainPanel.add(splitPane, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        insertAssertionButton = UIUtils.createInsertAssertionButton();
        insertAssertionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recordAction(ASSERT_ACTION);
            }
        });
        insertWaitButton = UIUtils.createInsertWaitButton();
        insertWaitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recordAction(WAIT_ACTION);
            }
        });
        buttonPanel.add(insertWaitButton);
        buttonPanel.add(insertAssertionButton);
        return buttonPanel;
    }

    private JSplitPane getAssertionPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setTopComponent(new JScrollPane(getTree()));
        textArea = new JTextArea(4, 0);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        splitPane.setBottomComponent(new JScrollPane(textArea));
        return splitPane;
    }

    private JTree getTree() {
        assertionTree = new JTree(rootNode);
        assertionTree.setRootVisible(false);
        assertionTree.setShowsRootHandles(true);
        assertionTree.setModel(getTreeModel());
        assertionTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                AssertionTreeNode lastPathComponent = (AssertionTreeNode) e.getPath().getLastPathComponent();
                textArea.setText(lastPathComponent.getDisplayValue());
            }
        });
        assertionTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                if (assertionTree.getSelectionCount() > 0) {
                    insertWaitButton.setEnabled(true);
                    insertAssertionButton.setEnabled(true);
                } else {
                    insertWaitButton.setEnabled(false);
                    insertAssertionButton.setEnabled(false);
                }
            }
        });
        assertionTree.setCellRenderer(new AssertionTreeNodeRenderer());
        assertionTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (assertionTree.getSelectionCount() > 0) {
                        recordAction(ASSERT_ACTION);
                    }
                }
            }
        });
        return assertionTree;
    }

    private DefaultTreeModel getTreeModel() {
        getRoot();
        assertionTreeModel = new DefaultTreeModel(rootNode);
        return assertionTreeModel;
    }

    protected TreeNode getRoot() {
        rootNode = new AssertionTreeNode(mcomponent);
        return rootNode;
    }

    private void recordAction(int action) {
        TreePath[] selectionPaths = assertionTree.getSelectionPaths();
        for (int i = 0; i < selectionPaths.length; i++) {
            TreePath path = selectionPaths[i];
            Object[] objects = path.getPath();
            final StringBuffer sb = new StringBuffer();
            MComponent forComponent = mcomponent;
            for (int j = 1; j < objects.length; j++) {
                final AssertionTreeNode node = (AssertionTreeNode) objects[j];
                if (node.getObject() instanceof MComponent) {
                    forComponent = (MComponent) node.getObject();
                    sb.setLength(0);
                    continue;
                }
                sb.append(node.getProperty());
                if (j < objects.length - 1) {
                    if (!((AssertionTreeNode) objects[j + 1]).getProperty().startsWith("["))
                        sb.append(".");
                } else {
                    IScriptElement enscript;
                    String property = sb.toString();
                    if (action == ASSERT_ACTION) {
                        if (property.equals("Content") && forComponent instanceof MCollectionComponent)
                            enscript = new AssertContent(forComponent.getComponentId(),
                                    ((MCollectionComponent) forComponent).getContent(), scriptModel, windowMonitor)
                                    .enscript(forComponent);
                        else {
                            String value;
                            if (property.equals("Text"))
                                value = forComponent.getComparableObject().toString();
                            else
                                value = forComponent.getProperty(property);
                            enscript = new AssertPropertyAction(forComponent.getComponentId(), property, value, scriptModel,
                                    windowMonitor).enscript(forComponent);
                        }
                    } else {
                        String value;
                        if (property.equals("Text"))
                            value = forComponent.getComparableObject().toString();
                        else
                            value = forComponent.getProperty(property);
                        enscript = new WaitPropertyAction(forComponent.getComponentId(), property, value, scriptModel,
                                windowMonitor).enscript(forComponent);
                    }
                    if (getFinder() != null)
                        getFinder().markUsed(forComponent);
                    getRecorder().record(enscript);
                }
            }
        }
    }

    public void setComponent(Component component, Point point, boolean isTriggered) {
        mcomponent = getFinder().getMComponentByComponent(component, point);
        if (mcomponent == null) {
            return;
        }
        assertionTreeModel.setRoot(getRoot());
        insertWaitButton.setEnabled(false);
        insertAssertionButton.setEnabled(false);
        assertionTree.setSelectionRow(0);
    }

    public String getName() {
        return "Assertions";
    }

}
