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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

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

public class TestMListCell {
    private DialogForTesting dialog;
    private JList list;
    private String[] listData;

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
        dialog.addButton("button.name", "I dare you");
        listData = new String[] { "item0", "item1", "item2", "item3" };
        dialog.addList("list", listData);
        list = dialog.getList();
        dialog.pack();
        dialog.show();
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        list = null;
        dialog.dispose();
        dialog = null;
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Test
    public void testConstructMListCellWithPoint() throws Exception {
        Point cellLocation = list.getCellBounds(1, 1).getLocation();
        assertEquals(createListCell("1"), new MListCell(list, list.getName(), cellLocation, new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance()));
    }

    @Test
    public void testConstructMListCellWithInvalidPointStillSelectsLastElement() throws Exception {
        Dimension bounds = list.getSize();
        Point notInList = new Point((int) bounds.getWidth() + 30, (int) bounds.getHeight() + 30);
        assertEquals(createListCell("3"), new MListCell(list, list.getName(), notInList, new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance()));
    }

    @Test
    public void testGetText() throws Exception {
        MListCell cell_1 = createListCell("1");
        assertEquals(listData[1], cell_1.getText());
        MListCell cell_3 = createListCell("3");
        assertEquals(listData[3], cell_3.getText());
    }

    @Test
    public void testSetText() throws Exception {
    }

    @Test
    public void testGoingEditable() {
    }

    @Test
    public void testGetTextWhenEditing() {
    }

    @Test
    public void testClick() {
        MListCell cell_1 = createListCell("1");
        MListMouseAdapter adapter = new MListMouseAdapter();
        list.addMouseListener(adapter);
        cell_1.click(1, false);
        assertEquals("Number of clicks", 1, adapter.events.size());
        MouseEvent event = (MouseEvent) adapter.events.get(0);
        assertEquals("Type 2", MouseEvent.MOUSE_CLICKED, event.getID());
        assertEquals("Click count", 1, event.getClickCount());
    }

    @Test
    public void testEquals() throws Exception {
        MListCell cell_0 = createListCell("0");
        MListCell cell_0_again = createListCell("0");
        MListCell cell_1 = createListCell("1");
        assertEquals(cell_0, cell_0_again);
        assertEquals(cell_0, cell_0);
        assertTrue(!cell_0.equals(cell_1));
    }

    @Test
    public void testOldGetComponentInfo() throws Exception {
        MListCell list_1 = createListCell("1");
        assertEquals("item1", list_1.getComponentInfo());
    }

    @Test
    public void testGetComponentInfo() throws Exception {
        MListCell list_1 = createListCell("item1");
        assertEquals("item1", list_1.getComponentInfo());
    }

    private MListCell createListCell(String cellIndex) {
        return new MListCell(list, "list.name", cellIndex, new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
    }

    @Test
    public void testJListGetTextFromRenderer() {
        JList jList = new JList(new Object[] { Code.ONE, Code.TWO });
        jList.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                // we know that the value is Code
                return new JLabel(buildTextValue((Code) value));
            }
        });
        MListCell listCell = new MListCell(jList, "list.name", "0", new ComponentFinder(Boolean.FALSE,
                WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), WindowMonitor.getInstance());
        assertEquals("Text should be from the cell renderer", buildTextValue(Code.ONE), listCell.getText());
    }

    private String buildTextValue(Code val) {
        return val.getCd() + ":" + val.getDesc();
    }

    private class MListMouseAdapter extends MouseAdapter {
        List<MouseEvent> events;

        MListMouseAdapter() {
            events = new ArrayList<MouseEvent>();
        }

        public void mouseClicked(MouseEvent e) {
            events.add(e);
        }
    }

    private static class Code {
        private static final Code ONE = new Code("one", "desc_one");
        private static final Code TWO = new Code("two", "desc_two");
        private String cd;
        private String desc;

        Code(String cd, String desc) {
            this.cd = cd;
            this.desc = desc;
        }

        public String toString() {
            return desc;
        }

        public String getCd() {
            return cd;
        }

        public String getDesc() {
            return desc;
        }
    }
}
