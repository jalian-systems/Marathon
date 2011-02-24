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
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import javax.swing.DefaultListModel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMList {
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
        dialog = new DialogForTesting(getName());
        dialog.setBounds(300, 200, 100, 100);
        dialog.addList("list.name", new String[] { "item0", "item1", "item2", "item3" });
        dialog.pack();
        dialog.show();
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
        dialog = null;
    }

    @Test
    public void testRowCount() {
        MList list = new MList(dialog.getList(), "list.name", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        assertEquals(4, list.getRowCount());
    }

    @Test
    public void testGetContent() {
        MList list = new MList(dialog.getList(), "list.name", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        String[][] expected = { { "item0", "item1", "item2", "item3" } };
        String[][] content = list.getContent();
        assertEquals(expected.length, content.length);
        for (int i = 0; i < expected[0].length; i++) {
            assertEquals(expected[0][i], content[0][i]);
        }
    }

    @Test
    public void testGetText() {
        MList list = new MList(dialog.getList(), "list.name", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        String expected = "[item2]";
        dialog.getList().setSelectedIndex(2);
        String actual = list.getText();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetTextWhenMultipleItemsAreSelected() {
        MList list = new MList(dialog.getList(), "list.name", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        String expected = "[item0, item3]";
        dialog.getList().setSelectedIndices(new int[] { 0, 3 });
        String actual = list.getText();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetTextWhenItemsNeedToBeEscaped() {
        dialog.getList().setModel(new DefaultListModel());
        DefaultListModel model = (DefaultListModel) dialog.getList().getModel();
        model.add(0, "item0");
        model.add(1, "item1");
        model.add(2, "item2");
        model.add(3, "item3");
        model.add(4, "this needs escaping ,{}:[]");
        MList list = new MList(dialog.getList(), "list.name", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        String expected = "[this needs escaping \\,\\{\\}\\:[]]";
        dialog.getList().setSelectedIndex(4);
        AWTSync.sync();
        String actual = list.getText();
        assertEquals(expected, actual);
    }

    @Test
    public void testOldSetTextWhenItemsNeedToBeEscaped() {
        dialog.setVisible(false);
        dialog.getList().setModel(new DefaultListModel());
        DefaultListModel model = (DefaultListModel) dialog.getList().getModel();
        model.add(0, "item0");
        model.add(1, "item1");
        model.add(2, "item2");
        model.add(3, "item3");
        model.add(4, "this-needs-esc#-aping");
        dialog.pack();
        dialog.show();
        ComponentFinder finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        finder.push(dialog);
        MComponent component = finder.getMComponentByComponent(dialog.getList());
        assertNotNull(component);
        component.setText("item0-this#_needs#_esc###_aping");
        AWTSync.sync();
        int[] indices = dialog.getList().getSelectedIndices();
        assertEquals(2, indices.length);
        assertEquals(0, indices[0]);
        assertEquals(4, indices[1]);
    }

    @Test
    public void testOldSetTextWhenAnItemIsAlreadySelected() {
        dialog.getList().setSelectedIndex(1);
        ComponentFinder finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        finder.push(dialog);
        MComponent component = finder.getMComponentByComponent(dialog.getList());
        assertNotNull(component);
        component.setText("item0-item2");
        AWTSync.sync();
        int[] indices = dialog.getList().getSelectedIndices();
        assertEquals(2, indices.length);
        assertEquals(0, indices[0]);
        assertEquals(2, indices[1]);
    }

    @Test
    public void testSetTextWhenItemsNeedToBeEscaped() {
        dialog.setVisible(false);
        dialog.getList().setModel(new DefaultListModel());
        DefaultListModel model = (DefaultListModel) dialog.getList().getModel();
        model.add(0, "item0");
        model.add(1, "item1");
        model.add(2, "item2");
        model.add(3, "item3");
        model.add(4, "this-needs-esc#-aping");
        dialog.pack();
        dialog.show();
        ComponentFinder finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        finder.push(dialog);
        MComponent component = finder.getMComponentByComponent(dialog.getList());
        assertNotNull(component);
        component.setText("item0-this#_needs#_esc###_aping");
        AWTSync.sync();
        int[] indices = dialog.getList().getSelectedIndices();
        assertEquals(2, indices.length);
        assertEquals(0, indices[0]);
        assertEquals(4, indices[1]);
    }

    @Test
    public void testSetTextWhenAnItemIsAlreadySelected() {
        dialog.getList().setSelectedIndex(1);
        ComponentFinder finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        finder.push(dialog);
        MComponent component = finder.getMComponentByComponent(dialog.getList());
        assertNotNull(component);
        component.setText("item0-item2");
        AWTSync.sync();
        int[] indices = dialog.getList().getSelectedIndices();
        assertEquals(2, indices.length);
        assertEquals(0, indices[0]);
        assertEquals(2, indices[1]);
    }
}
