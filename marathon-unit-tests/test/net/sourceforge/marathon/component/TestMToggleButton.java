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

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMToggleButton {
    private DialogForTesting dialog;
    private MToggleButton mCheckbox;
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
        dialog.addCheckBox("name", "the text");
        mCheckbox = new MToggleButton(dialog.getCheckBox(), "foo.bar", null, WindowMonitor.getInstance());
        dialog.getCheckBox().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                record += "mouseClicked(" + e.getClickCount() + ")";
            }
        });
        dialog.getCheckBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                record += "actionPerformed()";
            }
        });
        record = "";
        dialog.show();
    }

    @After
    public void tearDown() throws Exception {
        mCheckbox = null;
        dialog.dispose();
        dialog = null;
    }

    @Test
    public void testClick() {
        dialog.show();
        mCheckbox.click(1);
        assertEquals("actionPerformed()" + "mouseClicked(1)", record);
    }

    @Test
    public void testDoubleClick() {
        dialog.show();
        mCheckbox.click(2);
        assertEquals("actionPerformed()" + "mouseClicked(1)" + "actionPerformed()" + "mouseClicked(2)", record);
    }

    @Test
    public void testGetText() {
        assertEquals("false", mCheckbox.getText());
    }

    @Test
    public void testCheckBoxStateWithGetText() {
        dialog.show();
        mCheckbox.click(1);
        assertEquals("true", mCheckbox.getText());
        mCheckbox.click(1);
        assertEquals("false", mCheckbox.getText());
    }

    @Test
    public void testGetSetText() {
        assertEquals("false", mCheckbox.getText());
        mCheckbox.setText("true");
        assertEquals("true", mCheckbox.getText());
    }

    @Test
    public void testNullTextDoesIsHidden() {
        dialog.getCheckBox().setText(null);
        dialog.show();
        assertEquals("false", mCheckbox.getText());
    }

    @Test
    public void testSetChangesState() {
        dialog.show();
        dialog.getCheckBox().setSelected(true);
        assertEquals("true", mCheckbox.getText());
    }
}
