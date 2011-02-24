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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.plaf.basic.BasicComboPopup;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.MessageList;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMComboBox {
    private DialogForTesting dialog;
    private MComboBox mComboBox;

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
        dialog.setBounds(300, 200, 100, 100);
        dialog.addComboBox("name", new String[] { "a", "b", "c" });
        mComboBox = new MComboBox(dialog.getComboBox(), "foo.bar", dialog.getResolver(), WindowMonitor.getInstance());
        dialog.show();
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        mComboBox = null;
        dialog.dispose();
        dialog = null;
    }

    @Test
    public void testGetSetText() {
        assertEquals("a", mComboBox.getText());
        mComboBox.setText("b");
        assertEquals("b", mComboBox.getText());
        mComboBox.setText("c");
        assertEquals("c", mComboBox.getText());
        try {
            mComboBox.setText("not here");
            fail("should have failed");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testGetsAndSetsTextFromDirectlyEdittableComboBox() throws Exception {
        dialog.getComboBox().setEditable(true);
        /*
         * MComboBox is not sending an ENTER when editing the field. Hence, no
         * action event is generated.
         */
        // dialog.getComboBox().addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        // actionDone = true;
        // }
        // });
        mComboBox.setText("not in popup list");
        // assertTrue("ComboBox should have been selected", actionDone);
        assertEquals("not in popup list", mComboBox.getText());
    }

    @Test
    public void testEventsGetFiredOnSetText() {
        MessageList events = new MessageList();
        MockComponentListener listener = new MockComponentListener(events, "popup");
        BasicComboPopup popup = (BasicComboPopup) dialog.getComboBox().getAccessibleContext().getAccessibleChild(0);
        popup.getList().addFocusListener(listener);
        popup.getList().addMouseListener(listener);
        assertEquals("a", mComboBox.getText());
        events.assertEmpty();
        mComboBox.setText("b");
        events.assertNextMessageInList("popup:mouseEntered");
        events.assertNextMessageInList("popup:mousePressed");
        events.assertNextMessageInList("popup:mouseReleased");
    }

    @Test
    public void testGetContent() {
        MComboBox box = new MComboBox(dialog.getComboBox(), "box.name", dialog.getResolver(), WindowMonitor.getInstance());
        String[][] expected = { { "a", "b", "c" } };
        String[][] content = box.getContent();
        assertEquals(expected.length, content.length);
        for (int i = 0; i < expected[0].length; i++) {
            assertEquals(expected[0][i], content[0][i]);
        }
    }

    @Test
    public void testNPE() {
        dialog.getComboBox().setEditable(false);
        dialog.getComboBox().setModel(new DefaultComboBoxModel());
        assertNull(mComboBox.getText());
    }

}
