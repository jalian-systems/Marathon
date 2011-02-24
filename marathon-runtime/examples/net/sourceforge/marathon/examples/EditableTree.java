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
package net.sourceforge.marathon.examples;

/*  
 * This example is from javareference.com  
 * for more information visit,  
 * http://www.javareference.com  
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * This class creates a editable JTree and allows adding new nodes to it, which
 * are immediately made available for editing after adding
 * 
 * @author Rahul Sapkal(rahul@javareference.com)
 */
public class EditableTree extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JTree m_tree;
    private DefaultTreeModel m_model;
    private JButton m_addButton;

    /**
     * Constructor
     */
    public EditableTree() {
        setTitle("Editable JTree Demo");

        // constructing tree
        TreeNode rootNode = buildDummyTree();
        m_model = new DefaultTreeModel(rootNode);

        m_tree = new JTree(m_model);
        m_tree.setEditable(true);
        m_tree.setSelectionRow(0);

        JScrollPane scrollPane = new JScrollPane(m_tree);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        m_addButton = new JButton("Add Node");
        m_addButton.addActionListener(this);
        panel.add(m_addButton);
        getContentPane().add(panel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 400);
    }

    /**
     * This method builds a dummy tree and returns the root node
     * 
     * @return root node
     */
    public TreeNode buildDummyTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("JavaReference");
        DefaultMutableTreeNode forums = new DefaultMutableTreeNode("Forum");
        forums.add(new DefaultMutableTreeNode("Thread 1"));
        forums.add(new DefaultMutableTreeNode("Thread 2"));
        forums.add(new DefaultMutableTreeNode("Thread 3"));
        DefaultMutableTreeNode articles = new DefaultMutableTreeNode("Articles");
        articles.add(new DefaultMutableTreeNode("Article 1"));
        articles.add(new DefaultMutableTreeNode("Article 2"));
        DefaultMutableTreeNode examples = new DefaultMutableTreeNode("Examples");
        examples.add(new DefaultMutableTreeNode("Examples 1"));
        examples.add(new DefaultMutableTreeNode("Examples 2"));
        examples.add(new DefaultMutableTreeNode("Examples 3"));

        rootNode.add(forums);
        rootNode.add(articles);
        rootNode.add(examples);

        return rootNode;
    }

    /**
     * Handles the button action
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getSource().equals(m_addButton)) {
            DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) m_tree.getLastSelectedPathComponent();

            if (selNode != null) {
                // add new node as a child of a selected node at the end
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("New Node");
                m_model.insertNodeInto(newNode, selNode, selNode.getChildCount());

                // make the node visible by scroll to it
                TreeNode[] nodes = m_model.getPathToRoot(newNode);
                TreePath path = new TreePath(nodes);
                m_tree.scrollPathToVisible(path);

                // select the newly added node
                m_tree.setSelectionPath(path);

                // Make the newly added node editable
                m_tree.startEditingAtPath(path);
            }
        }
    }

    /**
     * Main method to run as an Application
     * 
     * @param argv
     */
    public static void main(String[] arg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new EditableTree().setVisible(true);
            }
        });
    }
}

// End EditableTree