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

import static org.junit.Assert.assertEquals;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMTreeNode {
    private DialogForTesting dialog;
    private JTree tree;
    private String record;

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

    @Before
    public void setUp() throws Exception {
        dialog = new DialogForTesting(this.getClass().getName());
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode level1child1 = new DefaultMutableTreeNode("level1 - 1");
        rootNode.add(level1child1);
        DefaultMutableTreeNode level1child2 = new DefaultMutableTreeNode("level1 - 2");
        rootNode.add(level1child2);
        DefaultMutableTreeNode level2child1 = new DefaultMutableTreeNode("level2 - 1");
        level1child2.add(level2child1);
        dialog.addTree("tree", rootNode);
        tree = dialog.getTree();
        // setup listener so that we know it's been clicked
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getPoint() == null || tree == null)
                    return;
                TreePath path = tree.getSelectionPath();
                if (e.isPopupTrigger())
                    record += "right ";
                record += "clicked " + e.getClickCount() + " times on tree" + path.toString();
            }
        });
        record = "";
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                tree.expandRow(0);
                int rowCount = tree.getRowCount();
                for (int i = 0; i < rowCount; i++)
                    tree.expandRow(i);
            }
        });

        dialog.pack();
        dialog.show();
    }

    @After
    public void tearDown() throws Exception {
        tree = null;
        dialog.dispose();
        dialog = null;
    }

    @Test
    public void testClickNode() throws Exception {
        tree.expandPath(tree.getPathForRow(2));
        tree.scrollPathToVisible(tree.getPathForRow(2));
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "/root/level1 - 2/level2 - 1", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        node.click(1, false);
        assertEquals("clicked 1 times on tree[root, level1 - 2, level2 - 1]", record);
    }

    @Test
    public void testOldGetComponentInfo() throws Exception {
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "/root/level1 - 2/level2 - 1", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        assertEquals("/root/level1 - 2/level2 - 1", node.getComponentInfo());
    }

    @Test
    public void testGetComponentInfo() throws Exception {
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "/root/level1 - 2/level2 - 1", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        assertEquals("/root/level1 - 2/level2 - 1", node.getComponentInfo());
    }

    @Test
    public void testGetComponentInfoWithText() throws Exception {
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "{Path: /root/level1 - 2/level2 - 1}", new ComponentFinder(
                Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        assertEquals("/root/level1 - 2/level2 - 1", node.getComponentInfo());
    }

    @Test
    public void testClickNodeNoRoot() throws Exception {
        tree.setRootVisible(false);
        tree.expandPath(tree.getPathForRow(1));
        tree.scrollPathToVisible(tree.getPathForRow(1));
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "/level1 - 2/level2 - 1", null, WindowMonitor.getInstance());
        node.click(1, false);
        assertEquals("clicked 1 times on tree[root, level1 - 2, level2 - 1]", record);
    }

    @Test
    public void testGetComponentInfoNoRoot() throws Exception {
        tree.setRootVisible(false);
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "/level1 - 2/level2 - 1", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        assertEquals("/level1 - 2/level2 - 1", node.getComponentInfo());
    }

    @Test
    public void testRightClickNode() throws Exception {
        tree.expandPath(tree.getPathForRow(2));
        tree.scrollPathToVisible(tree.getPathForRow(2));
        tree.setSelectionRow(3);
        MTreeNode node = new MTreeNode(tree, "doesn't matter", "/root/level1 - 2/level2 - 1", null, WindowMonitor.getInstance());
        node.click(1, true);
        assertEquals("right clicked 1 times on tree[root, level1 - 2, level2 - 1]", record);
    }
}
