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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import net.sourceforge.marathon.display.FileEventHandler;
import net.sourceforge.marathon.event.AWTSync;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import atunit.AtUnit;
import atunit.Mock;
import atunit.MockFramework;
import atunit.Unit;

import com.vlsolutions.swing.docking.ui.DockingUISettings;

@RunWith(AtUnit.class) @MockFramework(atunit.MockFramework.Option.EASYMOCK) public class TestNavigator {
    private String[] roots;
    @Unit private Navigator navigator;
    private JTree tree;

    @Mock FileEventHandler handler;

    @Before public void setUp() throws Exception {
        DockingUISettings.getInstance().installUI();
        createTestFiles();
        roots = new String[] { "./root1", "./root2" };
        navigator = new Navigator(roots, null, null, handler, null);
        navigator.getComponent();
        tree = navigator.getJTree();
    }

    private void createTestFiles() throws IOException {
        createDir("./root1");
        createDir("./root2");
        createDir("./root1/emptyDir");
        createFile("./root1/file1");
        createFile("./root1/file2");
        createFile("./root1/file3");
        createFile("./root1/readonlyfile").setReadOnly();
        createDir("./root1/Dir");
        createFile("./root1/Dir/file1");
        createFile("./root1/Dir/file2");
    }

    private File createFile(String name) throws IOException {
        File file = new File(name);
        file.createNewFile();
        return file;
    }

    private File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @After public void tearDown() throws Exception {
        deleteTestFiles();
    }

    private void deleteTestFiles() {
        deleteRecursive(new File("./root1"));
        deleteRecursive(new File("./root2"));
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteRecursive(list[i]);
            }
        }
        file.delete();
    }

    @Test public void testGetSelectedFiles() throws IOException {
        navigator.collapseAll();
        tree.setSelectionRows(new int[] { 0, 1 });
        File[] expected = { new File("./root1"), new File("./root2") };
        File[] actual = navigator.getSelectedFiles();
        checkFiles(expected, actual);
    }

    private void checkFiles(File[] expected, File[] actual) throws IOException {
        assertEquals("Array size", expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals("Files are equal", expected[i].getCanonicalPath(), actual[i].getCanonicalPath());
        }
    }

    @Test public void testRenameFailsOnRoot() {
        boolean gotException = false;
        try {
            navigator.rename(new File("./root2"));
        } catch (IOException e) {
            gotException = true;
        }
        assertTrue("IOException Expected", gotException);
    }

    @Test public void testDeleteFilesFailsOnRoot() {
        tree.expandRow(0);
        boolean gotException = false;
        try {
            navigator.deleteFiles(new File[] { new File("./root1"), new File("./root1/file1") });
        } catch (IOException e) {
            gotException = true;
        }
        assertTrue("IOException Expected", gotException);
    }

    @Test public void testDeleteFiles() {
        EasyMock.reset(handler);
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        EasyMock.replay(handler);

        tree.expandRow(0);
        boolean gotException = false;
        try {
            File[] files = new File[] { new File("./root1/file1"), new File("./root1/file2"), new File("./root1/Dir"),
                    new File("./root1/readonlyfile") };
            navigator.deleteFiles(files, false);
        } catch (IOException e) {
            gotException = true;
        }
        assertFalse("IOException not expected", gotException);
        assertFalse("File does not exist", new File("./root1/file1").exists());
        assertFalse("File does not exist", new File("./root1/file2").exists());
        assertFalse("File does not exist", new File("./root1/Dir").exists());
        assertFalse("File does not exist", new File("./root1/readonly").exists());
    }

    @Test public void testDeleteFilesWithSomeFailures() {
        EasyMock.reset(handler);
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        handler.fireDeleteEvent(EasyMock.anyObject(File.class));
        EasyMock.replay(handler);

        tree.expandRow(0);
        try {
            File[] files = new File[] { new File("./root1/nonexistantfile"), new File("./root1/file2"), new File("./root1/Dir"),
                    new File("./root1/file1") };
            navigator.deleteFiles(files, false);
        } catch (IOException e) {
        }
        assertFalse("File does not exist", new File("./root1/file1").exists());
        assertFalse("File does not exist", new File("./root1/file2").exists());
        assertFalse("File does not exist", new File("./root1/Dir").exists());
    }

    @Test public void testMoveFailsOnRoot() {
        tree.expandRow(0);
        boolean gotException = false;
        try {
            File[] files = new File[] { new File("./root1"), new File("./root1/file2"), new File("./root1/Dir"),
                    new File("./root1/file1") };
            navigator.moveToDirectory(files, new File("./root1/emptyDir"));
        } catch (IOException e) {
            gotException = true;
        }
        assertTrue("IOException expected", gotException);
    }

    @Test public void testMove() throws IOException {
        EasyMock.reset(handler);
        handler.fireMoveEvent(EasyMock.anyObject(File.class), EasyMock.anyObject(File.class));
        handler.fireMoveEvent(EasyMock.anyObject(File.class), EasyMock.anyObject(File.class));
        handler.fireMoveEvent(EasyMock.anyObject(File.class), EasyMock.anyObject(File.class));
        EasyMock.replay(handler);

        tree.expandRow(0);
        boolean gotException = false;
        try {
            File[] files = new File[] { new File("./root1/file2"), new File("./root1/Dir"), new File("./root1/file1") };
            navigator.moveToDirectory(files, new File("./root1/emptyDir"));
        } catch (IOException e) {
            gotException = true;
        }
        assertFalse("IOException expected", gotException);
        File[] expected = new File[] { new File("./root1/emptyDir/file2"), new File("./root1/emptyDir/Dir"),
                new File("./root1/emptyDir/file1") };
        File[] actual = navigator.getSelectedFiles();
        checkFiles(expected, actual);
    }

    @Test public void testRefresh() throws IOException {
        navigator.collapseAll();
        tree.expandRow(0);
        File newFile = new File("./root1/newfile");
        newFile.createNewFile();
        navigator.refresh(new File[] { new File("./root1") });
        AWTSync.sync();
        TreePath path = tree.getPathForRow(6);
        assertNotNull(path);
        NavigatorTreeNode node = (NavigatorTreeNode) path.getLastPathComponent();
        File file = node.getFile();
        assertEquals("New file expected", file.getCanonicalPath(), newFile.getCanonicalPath());
    }

    @Test public void testPaste() throws IOException, InterruptedException, InvocationTargetException {
        EasyMock.reset(handler);
        handler.fireCopyEvent(EasyMock.anyObject(File.class), EasyMock.anyObject(File.class));
        EasyMock.replay(handler);

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                tree.expandRow(0);
                tree.setSelectionRow(1);
                navigator.copy(navigator.getSelectedFiles());
                try {
                    navigator.paste(new File("./root1/emptyDir"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        AWTSync.sync();
        File[] expected = new File[] { new File("./root1/emptyDir/Dir") };
        File[] actual = navigator.getSelectedFiles();
        checkFiles(expected, actual);
        assertTrue("File exist", new File("./root1/emptyDir/Dir").exists());
        assertTrue("File exist", new File("./root1/emptyDir/Dir/file1").exists());
        assertTrue("File exist", new File("./root1/emptyDir/Dir/file2").exists());
    }

    @Test public void testPasteRecursionError() throws IOException {
        tree.expandRow(0);
        tree.setSelectionRow(1);
        boolean gotException = false;
        navigator.copy(navigator.getSelectedFiles());
        try {
            navigator.paste(new File("./root1/Dir"));
        } catch (IOException e) {
            gotException = true;
        }
        assertTrue("Got exception", gotException);
    }

    @Test public void testGoIntoRootDirectory() throws IOException {
        tree.expandRow(0);
        TreePath path = tree.getPathForRow(0);
        NavigatorTreeNode node = (NavigatorTreeNode) path.getLastPathComponent();
        File file = node.getFile();
        assertEquals("Root before goInto", new File("./root1").getCanonicalPath(), file.getCanonicalPath());
        navigator.goInto(new File("./root2"));
        path = tree.getPathForRow(0);
        node = (NavigatorTreeNode) path.getLastPathComponent();
        file = node.getFile();
        assertEquals("Root after goInto", new File("./root2").getCanonicalPath(), file.getCanonicalPath());
    }

    @Test public void testGoIntoDirectoryWithExpandedChildren() throws IOException, InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                tree.expandRow(0);
                tree.expandRow(1);
                assertEquals("Tree count before goInto", 10, tree.getRowCount());
                navigator.goInto(new File("./root1/Dir"));
            }
        });
        AWTSync.sync();
        assertEquals("Tree count after goInto", 3, tree.getRowCount());
    }

    @Test public void testGoUp() {
        tree.expandRow(0);
        tree.expandRow(1);
        navigator.goInto(new File("./root1/Dir"));
        assertEquals("Tree count before goUp", 3, tree.getRowCount());
        navigator.goUp();
        assertEquals("Tree count after goUp", 9, tree.getRowCount());
    }

    @Test public void testHome() {
        tree.expandRow(0);
        tree.expandRow(1);
        navigator.goInto(new File("./root1/Dir"));
        assertEquals("Tree count before goUp", 3, tree.getRowCount());
        navigator.home();
        assertEquals("Tree count after goUp", 10, tree.getRowCount());
    }

    @Test public void testCollapseAll() {
        tree.expandRow(0);
        tree.expandRow(1);
        assertEquals("Tree count before collapseAll", 10, tree.getRowCount());
        navigator.collapseAll();
        assertEquals("Tree count after goUp", 2, tree.getRowCount());
    }

    @Test public void testExpandAll() {
        navigator.collapseAll();
        assertEquals("Tree count before expandAll", 2, tree.getRowCount());
        navigator.expandAll();
        assertEquals("Tree count after expandAll", 10, tree.getRowCount());
    }

    @Test public void testFileFilter() throws IOException {
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory() || file.getName().equals("file1"))
                    return true;
                return false;
            }
        };
        navigator = new Navigator(roots, filter, null, null, null);
        navigator.getComponent();
        tree = navigator.getJTree();
        navigator.expandAll();
        assertEquals("Tree count after expandAll", 6, tree.getRowCount());
    }

    @Test public void testCreateNewFile() {
        assertTrue("Too much of UI", true);
    }

    @Test public void testCreateNewFolder() {
        assertTrue("Too much of UI", true);
    }
}
