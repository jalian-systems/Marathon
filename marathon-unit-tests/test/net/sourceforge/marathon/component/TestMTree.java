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
package net.sourceforge.marathon.component;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class TestMTree {
    DialogForTesting dialog;
    private MTree tree;
    JTree jtree;

    @BeforeClass
    public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    @AfterClass
    public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    /*
     * root { child1 { child2 { child21 child22 }
     */
    @Before
    public void setUp() throws Exception {
        dialog = new DialogForTesting(this.getClass().getName());
        DefaultMutableTreeNode root = getTreeNode(T("root", T("child1", T("child2", T("child21", "child22")))));
        dialog.addTree("tree.name", root);
        jtree = dialog.getTree();
        tree = new MTree(jtree, "tree.name", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        jtree.expandRow(0);
        jtree.expandRow(2);
        dialog.show();
    }

    private Object T(String node, Object leafs) {
        return new Object[] { node, leafs };
    }

    private DefaultMutableTreeNode getTreeNode(Object nodes) {
        if (nodes instanceof String) {
            return createNode((String) nodes);
        }
        String nodeName = (String) ((Object[]) nodes)[0];
        Object[] nodesArray = (Object[]) ((Object[]) nodes)[1];
        DefaultMutableTreeNode rootNode = createNode(nodeName);
        for (int i = 0; i < nodesArray.length; i++) {
            rootNode.add(getTreeNode(nodesArray[i]));
        }
        return rootNode;
    }

    private DefaultMutableTreeNode createNode(String label) {
        return new DefaultMutableTreeNode(label);
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
    }

    @Test
    public void testRowCount() throws Exception {
        assertEquals("Nodes in the tree", 5, tree.getRowCount());
    }

    @Test
    public void testGetContent() throws Exception {
        String[][] expected = { { "root", "child1", "child2", "child21", "child22" } };
        String[][] content = tree.getContent();
        assertEquals(expected.length, content.length);
        for (int i = 0; i < expected[0].length; i++) {
            assertEquals(expected[0][i], content[0][i]);
        }
    }

    @Test
    public void testGetText() throws Exception {
        JTree jtree = dialog.getTree();
        dialog.setVisible(true);
        jtree.setSelectionRow(2);
        assertEquals("[/root/child2]", tree.getText());
    }

    @Test
    public void testGetTextMultipleNodesWithEscape() throws Exception {
        DefaultMutableTreeNode root = getTreeNode(T("root", T("c#,hi/ld1", T("child2", T("chi#|ld21", "child22")))));
        ((DefaultTreeModel) jtree.getModel()).setRoot(root);
        jtree.expandRow(0);
        jtree.expandRow(2);
        jtree.setSelectionRows(new int[] { 1, 3 });
        assertEquals("[/root/c#\\,hi\\\\/ld1, /root/child2/chi#|ld21]", tree.getText());
    }

    @Test
    public void testSetTextMultipleNodesWithEscape() throws Exception {
        DefaultMutableTreeNode root = getTreeNode(T("root", T("c#,hi/ld1", T("child2", T("chi#|ld21", "child22")))));
        ((DefaultTreeModel) jtree.getModel()).setRoot(root);
        jtree.expandRow(0);
        jtree.expandRow(2);
        tree.setText("[/root/c#\\,hi\\\\/ld1, /root/child2/chi#|ld21]");
        assertEquals(2, jtree.getSelectionRows().length);
    }
}
