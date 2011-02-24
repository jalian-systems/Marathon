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
import static org.junit.Assert.assertTrue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.MessageList;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMTextField {
    private DialogForTesting dialog;
    private final String SPECIAL_CHAR_SUPPORTED = "='\"()!";
    protected boolean fieldSelected = false;

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
        dialog.setBounds(300, 200, 100, 100);
        dialog.addCheckBox("CheckBox", "We need focus on this");
        dialog.addTextField("name", "the text");
        dialog.addSpinner();
        dialog.show();
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
        dialog = null;
    }

    @Test
    public void testFormattedTextField() {
        MSpinner spinner = new MSpinner(dialog.getSpinner(), "my.spinner", dialog.getResolver(), WindowMonitor.getInstance());
        spinner.setText("10");
    }

    @Test
    public void testGetSetText() {
        dialog.getTextField().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TestMTextField.this.fieldSelected = true;
            }
        });
        MTextComponent mTextField = new MTextComponent(dialog.getTextField(), "foo.bar", null, WindowMonitor.getInstance());
        assertEquals("the text", mTextField.getText());
        mTextField.setText("abc\n");
        assertTrue("Text field should have been selected", fieldSelected);
        assertEquals("abc", mTextField.getText());
    }

    @Test
    public void testSettingSpecialCharacters() {
        MTextComponent mTextField = new MTextComponent(dialog.getTextField(), "foo.bar", null, WindowMonitor.getInstance());
        mTextField.setText(SPECIAL_CHAR_SUPPORTED);
        assertEquals(SPECIAL_CHAR_SUPPORTED, mTextField.getText());
    }

    @Test
    public void testSetTextToEmptyString() throws Exception {
        MTextComponent field = new MTextComponent(dialog.getTextField(), "foo.bar", null, WindowMonitor.getInstance());
        // set it to so
        field.setText("abc");
        assertEquals("abc", field.getText());
        // now set it to empty and see if it works out
        field.setText("");
        assertEquals("set text to empty string", "", field.getText());
    }

    /**
     * some controls rely on getting the focus lost event in order to set their
     * text
     */
    @Test
    public void testSetTextFiresFocusGainedEvent() {
        final MTextComponent mTextField = new MTextComponent(dialog.getTextField(), "foo.bar", null, WindowMonitor.getInstance());
        final MessageList list = new MessageList();
        dialog.getTextField().addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                list.add("focusGained (" + mTextField.getText() + ")");
            }

            public void focusLost(FocusEvent e) {
                list.add("focusLost (" + mTextField.getText() + ")");
            }
        });
        mTextField.setText("purple");
        list.assertNextMessage("focusGained (the text)");
    }

    @Test
    public void testSettingEmptyString() {
        MTextComponent mTextField = new MTextComponent(dialog.getTextField(), "foo", null, WindowMonitor.getInstance());
        mTextField.setText("purple");
        mTextField.setText("");
        assertEquals("", mTextField.getText());
    }

    public void xtestSettingTextOnANonEditable() {
        MTextComponent mTextField = new MTextComponent(dialog.getTextField(), "foo", null, WindowMonitor.getInstance());
        dialog.getTextField().setEditable(false);
        mTextField.setText("purple");
        mTextField.setText("");
        assertEquals("", mTextField.getText());
    }
}
