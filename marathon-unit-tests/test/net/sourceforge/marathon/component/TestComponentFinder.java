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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;

class MockResolver extends ComponentResolver {
    public MockResolver(ComponentFinder finder) {
        super(finder, false, WindowMonitor.getInstance());
    }

    public MockResolver(ComponentFinder finder, boolean isRecording) {
        super(finder, isRecording, WindowMonitor.getInstance());
    }

    public boolean canHandle(Component component) {
        return true;
    }

    public boolean canHandle(Component component, Point location) {
        return true;
    }

    public Component getComponent(Component component, Point location) {
        return null;
    }

    public MComponent getMComponent(Component component, String name, Object obj) {
        return new MComponentMock();
    }
}

public class TestComponentFinder {
    private ComponentFinder _finder;
    private DialogForTesting _dialog;
    private DialogForTesting _dialog2;
    private MComponent _found;
    Object _mouseReceiver;

    @Before public void setUp() throws Exception {
        disableRetries();
        System.setProperty(Constants.PROP_COMPONENT_RESOLVERS, "");
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
        _dialog = new DialogForTesting(getName());
        _dialog.addButton("button.name", "button.name");
        _dialog.addTextField("field.name", "field1");
        _dialog.addComboBox("box.name", new String[] { "field1", "field2" });
        _dialog.addCheckBox("checkbox.name", "checkbox.name");
        _dialog.addMenu("menu.name", "menu.name", "menuItem.name", "menuItem.name");
        _dialog.addTable();
        _dialog.addList("list.name", new String[] { "item0", "item1", "item2", "item3" });
        _dialog.addTree("tree.name", new DefaultMutableTreeNode("root"));
        _dialog.addSpinner();
        _dialog.show();
        _finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        _finder.push(_dialog);
    }

    private String getName() {
        return this.getClass().getName();
    }

    @After public void tearDown() {
        _finder = null;
        _dialog.dispose();
        _dialog = null;
        if (_dialog2 != null) {
            _dialog2.dispose();
            _dialog2 = null;
        }
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_COMPONENT_RESOLVERS);
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Test public void testPluggableComponentResolvers() {
        final Class<? extends ComponentResolver> class1 = MockResolver.class;
        _finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider() {
            @Override public List<ComponentResolver> get() {
                ArrayList<ComponentResolver> l = new ArrayList<ComponentResolver>();
                try {
                    Constructor<? extends ComponentResolver> cr = class1.getConstructor(new Class[] { ComponentFinder.class,
                            boolean.class });
                    ComponentResolver res = cr.newInstance(new Object[] { finder, Boolean.valueOf(isRecording) });
                    l.add(res);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return l;
            }

        }, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        _finder.push(_dialog);
        MComponent component = _finder.getComponent_test("button.name");
        assertTrue("MockResolver is not used", component.getClass().equals(MComponentMock.class));
    }

    @Test public void testWeReturnTheRealComponent() {
        _finder = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(),
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()) {
            public Component getRealComponent(Component sourceComp, Point location) {
                return _dialog.getButton();
            }
        };
        assertEquals(new MButton(_dialog.getButton(), "button.name", null, WindowMonitor.getInstance()),
                _finder.getMComponentByComponent(_dialog.getTextField()));
    }

    @Test public void testComponentNotFound() {
        try {
            _finder.getComponent_test("not.here");
            fail("should have thrown an exception");
        } catch (ComponentNotFoundException e) {
            // correct
        }
    }

    @Test public void testUnexpandedMenuItemNotShown() {
        try {
            _finder.getComponent_test("menuItem.name");
            fail("should have thrown an exception");
        } catch (ComponentNotFoundException e) {
            // correct
        }
    }

    @Test public void testComponentsAreResolvedToCorrectMComponents() {
        assertEquals(8, _dialog.getContentPane().getComponentCount());
        checkResolves(_dialog.getTable(), "table.name", MTable.class);
        checkResolves(_dialog.getTable().getTableHeader(), "table.name.header", "col1", MTableHeaderItem.class);
        checkResolves(_dialog.getButton(), "button.name", MButton.class);
        checkResolves(_dialog.getTextField(), "field.name", MTextComponent.class);
        checkResolves(_dialog.getCheckBox(), "checkbox.name", MToggleButton.class);
    }

    private void checkResolves(JComponent component, String name, String info, Class<? extends MComponent> mClass) {
        MComponent wrapper = _finder.getComponent_test(name, info);
        assertEquals("wrong MComponent type", mClass, wrapper.getClass());
        assertSame("actual component", component, wrapper.getComponent());
    }

    private void checkResolves(JComponent component, String name, Class<? extends MComponent> mClass) {
        checkResolves(component, name, null, mClass);
    }

    @Test public void testFindMenuItems() {
        MMenu mmenu = (MMenu) _finder.getComponent_test("menu.name");
        assertEquals(_dialog.getMenu(), mmenu.getJMenu());
    }

    @Test public void testGetRealComponentJComboBox() {
        JComboBox box = _dialog.getComboBox();
        JComponent fooComponent = new JLabel();
        box.add(fooComponent);
        Component comp = _finder.getRealComponent(fooComponent, null);
        assertNotNull(comp);
        assertEquals(box, comp);
    }

    @Test public void testRecordingArtifactsNotFound() {
        class MyJButton extends JButton implements IRecordingArtifact {

            private static final long serialVersionUID = 1L;
        }
        MyJButton component = new MyJButton();
        assertNull("should have ignored recording artifact", _finder.getRealComponent(component, null));
    }

    @Test public void testChildrenOfRecordingArtifactsAreNotFound() {
        class RecordingJPanel extends JPanel implements IRecordingArtifact {

            private static final long serialVersionUID = 1L;
        }
        RecordingJPanel panel = new RecordingJPanel();
        JButton button = new JButton();
        panel.add(button);
        assertNull("found the child of a recording artifact", _finder.getRealComponent(button, null));
    }

    @Test public void testChildrenOfIgnoredWindowAreIgnored() {
        _dialog.setName(WindowMonitor.IGNORED_COMPONENT_NAME);
        assertNull("should ignore child of ignored window", _finder.getMComponentByComponent(_dialog.getButton()));
    }

    @Test public void testInvisibleComponentsNotFound() {
        JButton button = (JButton) _finder.getComponent_test("button.name").getComponent();
        button.setVisible(false);
        try {
            _finder.getComponent_test("button.name");
            fail("shouldn't find a hidden button");
        } catch (ComponentNotFoundException e) {
            // correct
        }
        button.setVisible(true);
        _finder.getComponent_test("button.name");
    }

    @Test public void testPushPopWindows() {
        _dialog2 = new DialogForTesting(getName() + ".2");
        _dialog2.addButton("other.name", "other.name");
        _dialog2.show();
        ComponentFinder resolver = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(),
                new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        assertNull(resolver.getWindow());
        resolver.push(_dialog);
        assertSame(_dialog, resolver.getWindow());
        assertSame(_dialog.getButton(), resolver.getComponent_test("button.name").getComponent());
        resolver.push(_dialog2);
        assertSame(_dialog2, resolver.getWindow());
        assertSame(_dialog2.getButton(), resolver.getComponent_test("other.name").getComponent());
        try {
            assertNotNull(resolver.getComponent_test("button.name"));
            fail("should not have been found");
        } catch (ComponentNotFoundException e) {
            // fine
        }
        resolver.pop();
        assertSame(_dialog, resolver.getWindow());
        assertSame(_dialog.getButton(), resolver.getComponent_test("button.name").getComponent());
        resolver.pop();
        assertNull(resolver.getWindow());
    }

    @Test public void testWindowTitle() {
        MButton button = (MButton) _finder.getMComponentByComponent(_dialog.getButton());
        assertEquals(this.getClass().getName(), button.getWindowId().getTitle());
    }

    @Test public void testResolveTableCellWithPoint() throws Exception {
        JTable table = _dialog.getTable();
        Point location = table.getCellRect(1, 1, false).getLocation();
        MTableCell cell = (MTableCell) _finder.getMComponentByComponent(_dialog.getTable(), location);
        assertEquals(new MTableCell(_dialog.getTable(), "table.name", "{1,col2}", null, WindowMonitor.getInstance()), cell);
    }

    @Test public void testResolveTableHeaderWithPoint() throws Exception {
        JTable table = _dialog.getTable();
        Point location = table.getTableHeader().getHeaderRect(1).getLocation();
        MTableHeaderItem header = (MTableHeaderItem) _finder.getMComponentByComponent(_dialog.getTable().getTableHeader(), location);
        assertEquals(
                new MTableHeaderItem(_dialog.getTable().getTableHeader(), "table.name.header", "col2", null,
                        WindowMonitor.getInstance()), header);
    }

    @Test public void testResolveTableCellWithNameAndCellSpecifier() throws Exception {
        MTableCell cell = (MTableCell) _finder.getMComponentById(new ComponentId("table.name", "{1,col2}"));
        assertEquals(new MTableCell(_dialog.getTable(), "table.name", "{1,col2}", null, WindowMonitor.getInstance()), cell);
    }

    @Test public void testResolveTableHeaderWithName() throws Exception {
        MTableHeaderItem header = (MTableHeaderItem) _finder.getMComponentById(new ComponentId("table.name.header", "col1"));
        assertEquals(
                new MTableHeaderItem(_dialog.getTable().getTableHeader(), "table.name.header", "col1", null,
                        WindowMonitor.getInstance()), header);
    }

    @Test(expected = ComponentNotFoundException.class) public void testInvalidColumnNameThrowsException() {
        _finder.getMComponentById(new ComponentId("table.name.header", "invalid"));
    }

    @Test public void testResolveAListWithName() throws Exception {
        MList list = (MList) _finder.getMComponentById(new ComponentId("list.name"));
        assertEquals(new MList(_dialog.getList(), "list.name", null, WindowMonitor.getInstance()), list);
    }

    /*
     * Marathon 0.93 onwards considers a JList as a single object - MListCell is
     * maintained for the purposes of executing the older scripts.
     * 
     * @Test public void testResolveListCellWithNameAndCellSpecifier() throws
     * Exception { MListCell cell = (MListCell) _finder.getComponent(new
     * ComponentId("list.name", "1")); assertEquals(new
     * MListCell(_dialog.getList(), "list.name", "1", null), cell); }
     * 
     * @Test public void testResolveListCellWithPoint() throws Exception { JList
     * list = _dialog.getList(); Point location = list.getCellBounds(1,
     * 1).getLocation(); MListCell cell = (MListCell) _finder.getComponent(list,
     * location); assertEquals(new MListCell(list, "list.name", "1", null),
     * cell); }
     */

    @Test public void testTableCellEditorsResolveToTheirParentTable() {
        JTable table = _dialog.getTable();
        table.editCellAt(1, 1);
        TableCellEditor editor = table.getCellEditor(1, 1);
        Component component = editor.getTableCellEditorComponent(table, "shizzow", true, 1, 1);
        assertSame(table, component.getParent());
        MComponent expected = new MTableCell(table, "table.name", "{1,col2}", null, WindowMonitor.getInstance());
        assertEquals(expected, _finder.getMComponentByComponent(component, new Point(0, 0)));
    }

    @Test public void testComboAlsoResolvesToTableCell() {
        JTable table = _dialog.getTable();
        JComboBox comboBox = new JComboBox();
        JButton button = new JButton();
        table.add(comboBox);
        comboBox.add(button);
        Point location = new Point(0, 0);
        assertEquals(table, _finder.getRealComponent(button, location));
    }

    @Test public void testGettingTree() {
        MComponent component = _finder.getComponent_test("tree.name");
        assertTrue(component instanceof MTree);
        component = _finder.getMComponentById(new ComponentId("tree.name", "/root"));
        assertNotNull(component);
    }

    @Test public void testTabbedPaneWhenInScrollTabLayout() throws Exception {
        _dialog.dispose();
        _dialog = new DialogForTesting(getName());
        _dialog.addTabbedPane("tabbed.name", "tab1", "tab2");
        final JTabbedPane pane = _dialog.getTabbedPane();
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        _dialog.show();
        pane.addMouseListener(new TabbedPaneMouseListener());
        pane.setSelectedIndex(0);
        Rectangle tabBounds = pane.getUI().getTabBounds(pane, 1);
        Point p = SwingUtilities.convertPoint(pane, (tabBounds.x + tabBounds.width) / 2, (tabBounds.y + tabBounds.height) / 2,
                _dialog);
        FireableMouseClickEvent e = new FireableMouseClickEvent(_dialog);
        e.fire(p, 1);
        Thread.sleep(500);
        // assertFalse (_mouseReceiver instanceof JTabbedPane);
        MComponent component = _finder.getMComponentByComponent((Component) _mouseReceiver);
        assertEquals("Component class", MTabbedPane.class, component.getClass());
    }

    /**
     * it should resolve to an MComponent (as opposed to null) so you can do
     * things like assertRowCount, etc
     */
    @Test public void testTableWOComponentInfoResolvesToMTable() {
        MComponent mComponent = _finder.getMComponentByComponent(_dialog.getTable(), "foo", null);
        assertEquals(MTable.class, mComponent.getClass());
        assertEquals(_dialog.getTable(), mComponent.getComponent());
    }

    @Test public void testFailedComponentSearchCollectsInvisibleComponents() throws Exception {
        JButton button = _dialog.getButton();
        button.setVisible(false);
        try {
            _finder.getComponent_test("button.name");
            fail("found invisible component, bad.");
        } catch (ComponentNotFoundException e) {
        } // expected
    }

    @Test public synchronized void testComponentsWhichBecomeVisibleDuringComponentLookupAreStillFound() throws Exception {
        resetRetries();
        final JButton button = _dialog.getButton();
        button.setVisible(false);
        new Thread() {
            public void run() {
                synchronized (TestComponentFinder.this) {
                    try {
                        _found = _finder.getComponent_test("button.name");
                    } finally {
                        TestComponentFinder.this.notify();
                    }
                }
            }
        }.start();
        Thread.sleep(50);
        button.setVisible(true);
        wait(5000);
        assertEquals("component", new MButton(button, "button.name", null, WindowMonitor.getInstance()), _found);
    }

    private final class TabbedPaneMouseListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            _mouseReceiver = e.getSource();
        }
    }

    private static final int originalRetryCount = ComponentFinder.COMPONENT_SEARCH_RETRY_COUNT;
    private static final int originalInterval = ComponentFinder.RETRY_INTERVAL_MS;

    public static void disableRetries() {
        ComponentFinder.COMPONENT_SEARCH_RETRY_COUNT = 1;
        ComponentFinder.RETRY_INTERVAL_MS = 0;
    }

    public static void resetRetries() {
        ComponentFinder.COMPONENT_SEARCH_RETRY_COUNT = originalRetryCount;
        ComponentFinder.RETRY_INTERVAL_MS = originalInterval;
    }
}
