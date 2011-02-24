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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Window;
import java.util.Properties;

import javax.swing.JDialog;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.MessageList;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.WindowIdCreator;
import net.sourceforge.marathon.component.WindowIdMock;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.Snooze;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestWindowAction {
    private DialogForTesting dialog;
    private MessageList events;

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
        dialog = new DialogForTesting("Main Dialog");
        dialog.addButton("button1.name", "button1");
        dialog.addMessageBoxButton("button2.name", "button2", "Message Dialog");
        events = new MessageList();
        events.addActionListener(dialog.getButton(), "button pressed");
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
        dialog = null;
        events = null;
    }

    @Test
    public void testOwnsItsChildren() {
        WindowIdMock parentId = new WindowIdMock("parent");
        WindowIdMock childId = new WindowIdMock("child", "parent");
        WindowElement parentAction = new WindowElement(parentId);
        WindowElement childAction = new WindowElement(childId);
        assertTrue("Parent owns the child", parentAction.owns(childAction));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testOwnsIfOwnedByItsParentAndStillAvailable() {
        JDialog grandParent = new JDialog();
        grandParent.setTitle("grandparent");
        JDialog parent = new JDialog(grandParent, "parent");
        JDialog child = new JDialog(parent, "child");
        grandParent.show();
        parent.show();
        new Snooze(500);
        WindowMonitor windowMonitor = WindowMonitor.getInstance();
        WindowId parentId = WindowIdCreator.createWindowId(parent, windowMonitor);
        child.show();
        new Snooze(500);
        WindowId childId = WindowIdCreator.createWindowId(child, windowMonitor);
        WindowElement parentAction = new WindowElement(parentId);
        WindowElement childAction = new WindowElement(childId);
        Window w = windowMonitor.getWindow(parentId.getTitle());
        assertEquals("parent", windowMonitor.getNamingStrategy().getName(w));
        assertTrue("Parent owns the child", parentAction.owns(childAction));
        child.dispose();
        parent.dispose();
        grandParent.dispose();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDoesntOwnIfOwnedByParentAndNotAvailable() {
        JDialog grandParent = new JDialog();
        grandParent.setTitle("grandparent");
        grandParent.setName("grandparent");
        JDialog parent = new JDialog(grandParent, "parent");
        parent.setName("parent");
        JDialog child = new JDialog(parent, "child");
        child.setName("child");
        System.out.println("Showing grandparent");
        grandParent.show();
        AWTSync.sync();
        System.out.println("Showing parent");
        parent.show();
        AWTSync.sync();
        new Snooze(500);
        WindowId parentId = WindowIdCreator.createWindowId(parent, WindowMonitor.getInstance());
        parent.dispose();
        AWTSync.sync();
        new Snooze(500);
        System.out.println("Showing child");
        child.show();
        AWTSync.sync();
        new Snooze(500);
        WindowId childId = WindowIdCreator.createWindowId(child, WindowMonitor.getInstance());
        WindowElement parentAction = new WindowElement(parentId);
        WindowElement childAction = new WindowElement(childId);
        assertTrue("Parent does not own the child", !parentAction.owns(childAction));
        child.dispose();
        AWTSync.sync();
        grandParent.dispose();
        AWTSync.sync();
    }

    @Test
    public void testToScriptCode() {
        WindowElement tag = new WindowActionMock("mytitle");
        tag.add(new RecordableMock("tag1"));
        tag.add(new RecordableMock("tag2"));
        String i1 = Indent.getIndent();
        String i2 = i1 + i1;
        assertEquals(i1 + "if window('mytitle'):\n" + i2 + "mock('tag1')\n" + i2 + "mock('tag2')\n" + i1 + "close()\n",
                tag.toScriptCode());
    }
}
