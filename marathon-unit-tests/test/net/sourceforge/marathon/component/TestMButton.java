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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMButton {
    private DialogForTesting dialog;
    private MButton mButton;
    private MMenu mMenu;
    private MButton mMenuItem;
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
        dialog = new DialogForTesting(getName());
        dialog.addButton("name", "the text");
        mButton = new MButton(dialog.getButton(), "foo.bar", null, WindowMonitor.getInstance());
        dialog.getButton().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                record += "mouseClicked(" + e.getClickCount() + ")";
            }
        });
        dialog.getButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                record += "actionPerformed()";
            }
        });
        dialog.addMenu("menu name", "the menu text", "menu item name", "the menu item text");
        mMenu = new MMenu(dialog.getMenu(), "menu.foo", null, WindowMonitor.getInstance());
        dialog.getMenu().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                record += "menuMouseClicked(" + e.getClickCount() + ")";
            }
        });
        mMenuItem = new MButton(dialog.getMenuItem(), "menu.item.foo", null, WindowMonitor.getInstance());
        dialog.getMenuItem().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                record += "menuItemActionPerformed()";
            }
        });
        record = "";
        dialog.show();
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        mButton = null;
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                dialog.dispose();
            }
        });
        dialog = null;
    }

    @Test
    public void testClick() {
        mButton.click(1, false);
        assertEquals("actionPerformed()" + "mouseClicked(1)", record);
    }

    @Test
    public void testDoubleClick() {
        mButton.click(2, false);
        assertEquals("actionPerformed()" + "mouseClicked(1)" + "actionPerformed()" + "mouseClicked(2)", record);
    }

    @Test
    public void testGetText() {
        assertEquals("the text", mButton.getText());
        assertEquals("the menu text", mMenu.getText());
        assertEquals("the menu item text", mMenuItem.getText());
    }

    @Test
    public void testMenuAItemClick() throws Throwable {
        MenuSelectionManager.defaultManager().clearSelectedPath();
        mMenu.click(1, false);
        mMenuItem.click(1, false);
        assertEquals("menuMouseClicked(1)" + "menuItemActionPerformed()", record);
    }

    @Test
    public void testMenuClick() {
        MenuSelectionManager.defaultManager().clearSelectedPath();
        mMenu.click(1, false);
        assertEquals("menuMouseClicked(1)", record);
    }

}
