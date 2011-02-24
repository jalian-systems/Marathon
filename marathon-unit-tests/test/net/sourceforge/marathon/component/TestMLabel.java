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

import java.util.Properties;

import javax.swing.JLabel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ComponentId;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMLabel {
    private JLabel label;
    private DialogForTesting dialog;

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
        label = new JLabel("this is a crock");
        label.setName("label.name");
        dialog = new DialogForTesting(getName());
        dialog.getContentPane().add(label);
        dialog.show();
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
    }

    @Test
    public void testGetText() {
        MLabel label = (MLabel) dialog.getResolver().getMComponentById(new ComponentId("label.name"));
        assertEquals("this is a crock", label.getText());
    }

    public void xtestSetText() {
        MLabel label = (MLabel) dialog.getResolver().getMComponentById(new ComponentId("label.name"));
        label.setText("This is a new one");
        assertEquals("This is a new one", label.getText());
    }
}
