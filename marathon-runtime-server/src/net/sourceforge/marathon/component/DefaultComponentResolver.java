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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.recorder.WindowMonitor;

/**
 * An implementation of {@link ComponentResolver} that handles all standard
 * Swing components. If a component can't be handled and if it is in the
 * ignoreList a {@link MNullComponent} is created. Otherwise, a
 * {@link MUnknownComponent} is created.
 * 
 */
public class DefaultComponentResolver extends ComponentResolver {
    private ArrayList<IgnoreClass> ignoreClasses = new ArrayList<IgnoreClass>();

    /**
     * Constructs the default component resolver.
     * 
     * @param finder
     * @param isRecording
     * @param windowMonitor
     */
    public DefaultComponentResolver(ComponentFinder finder, boolean isRecording, WindowMonitor windowMonitor) {
        super(finder, isRecording, windowMonitor);
        initIgnoreClasses();
    }

    /**
     * Initialize the ignoreClasses list.
     */
    private void initIgnoreClasses() {
        StringTokenizer tokenizer = new StringTokenizer(System.getProperty(Constants.PROP_IGNORE_COMPONENTS, ""), "();:");
        while (tokenizer.hasMoreTokens()) {
            String className = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens())
                tokenizer.nextToken();
            boolean ignoreChildren = false;
            if (tokenizer.hasMoreTokens())
                ignoreChildren = Boolean.valueOf(tokenizer.nextToken()).booleanValue();
            else {
                System.err.println("Warning: IgnoreClasses could not be parsed properly");
            }
            try {
                ignoreClasses.add(new IgnoreClass(className, ignoreChildren));
            } catch (ClassNotFoundException e) {
                System.err.println("Warning: IgnoreComponents - could not find class for " + className);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.ComponentResolver#canHandle(java.awt
     * .Component)
     * 
     * We handle all components
     */
    public boolean canHandle(Component component) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.ComponentResolver#canHandle(java.awt
     * .Component, java.awt.Point)
     * 
     * We handle all components wherever they are.
     */
    public boolean canHandle(Component component, Point location) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.ComponentResolver#getComponent(java
     * .awt.Component, java.awt.Point)
     */
    public Component getComponent(Component component, Point location) {
        Component parent = component.getParent();
        Component grandparent = parent != null ? parent.getParent() : null;
        Component greatgrandparent = grandparent != null ? grandparent.getParent() : null;

        Component realComponent = component;
        if (getColorChooser(component) != null) {
            realComponent = getColorChooser(component);
        } else if (getFileChooser(component) != null) {
            realComponent = getFileChooser(component);
        } else if (component.getClass().getName().indexOf("ScrollableTabPanel") > 0) {
            // See: testTabbedPaneWhenInScrollTabLayout
            realComponent = grandparent;
        } else if (component instanceof JTableHeader) {
        } else if (component instanceof JProgressBar) {
        } else if (component instanceof JSlider) {
        } else if (parent instanceof JTable) {
            setLocationForTable((JTable) parent, location);
            realComponent = getComponent(parent, location);
        } else if (parent instanceof JComboBox) {
            realComponent = getComponent(parent, location);
        } else if (greatgrandparent instanceof ComboPopup) {
            realComponent = null;
            if (greatgrandparent instanceof BasicComboPopup)
                realComponent = getComponent(((BasicComboPopup) greatgrandparent).getInvoker(), location);
        } else if (component instanceof ComboPopup) {
            realComponent = null;
            if (component instanceof BasicComboPopup)
                realComponent = getComponent(((BasicComboPopup) component).getInvoker(), location);
        } else if (parent instanceof JSpinner) {
            realComponent = parent;
        } else if (grandparent instanceof JSpinner) {
            realComponent = grandparent;
        } else if (grandparent instanceof JTree) {
            realComponent = grandparent;
        } else if (parent instanceof JTree) {
            realComponent = parent;
        }
        return realComponent;
    }

    private JColorChooser getColorChooser(Component component) {
        Component parent = component;
        while (parent != null) {
            if (parent instanceof JColorChooser)
                return (JColorChooser) parent;
            parent = parent.getParent();
        }
        return null;
    }

    private JFileChooser getFileChooser(Component component) {
        Component parent = component;
        while (parent != null) {
            if (parent instanceof JFileChooser)
                return (JFileChooser) parent;
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Do we need to ignore this component?
     * 
     * @param component
     * @return
     */
    private boolean ignoreClass(Component component) {
        for (Iterator<IgnoreClass> iter = ignoreClasses.iterator(); iter.hasNext();) {
            IgnoreClass element = (IgnoreClass) iter.next();
            if (element.matches(component))
                return true;
        }
        return false;
    }

    /**
     * Sets the location to a point the table.
     * 
     * @param table
     * @param location
     */
    private void setLocationForTable(JTable table, Point location) {
        if (location != null) {
            Rectangle cellRect = table.getCellRect(table.getEditingRow(), table.getEditingColumn(), false);
            location.setLocation(cellRect.getLocation());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.ComponentResolver#getMComponent(java
     * .awt.Component, java.lang.String, java.lang.Object)
     */
    public MComponent getMComponent(Component component, String name, Object obj) {
        if (component instanceof JColorChooser) {
            return new MColorChooser(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JFileChooser) {
            return new MFileChooser(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JTableHeader) {
            JTableHeader header = (JTableHeader) component;
            if (obj instanceof Point) {
                Point location = (Point) obj;
                return new MTableHeaderItem(header, name, header.columnAtPoint(location), getFinder(), windowMonitor);
            } else
                return new MTableHeaderItem(header, name, obj.toString(), getFinder(), windowMonitor);
        } else if (component instanceof JMenu && component.getParent().getClass() != JPopupMenu.class) {
            return new MMenu(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JProgressBar) {
            return new MProgressBar(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JSlider) {
            return new MSlider(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JSpinner) {
            return new MSpinner(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JLabel) {
            return new MLabel((JLabel) component, name, getFinder(), windowMonitor);
        } else if (component instanceof JToggleButton) {
            return new MToggleButton((JToggleButton) component, name, getFinder(), windowMonitor);
        } else if (component instanceof AbstractButton) {
            return new MButton((AbstractButton) component, name, getFinder(), windowMonitor);
        } else if (component instanceof JTextComponent) {
            if (component instanceof JEditorPane)
                return new MEditorPane((JEditorPane) component, name, obj, getFinder(), windowMonitor);
            return new MTextComponent(component, name, getFinder(), windowMonitor);
        } else if (component instanceof JComboBox) {
            return new MComboBox((JComboBox) component, name, getFinder(), windowMonitor);
        } else if (component instanceof JTabbedPane) {
            return new MTabbedPane((JTabbedPane) component, name, getFinder(), windowMonitor);
        } else if (component instanceof JTable) {
            if (isRecording() && obj == null) {
                JTable table = (JTable) component;
                Rectangle rect;
                if (table.isEditing())
                    rect = table.getCellRect(table.getEditingRow(), table.getEditingColumn(), false);
                else
                    rect = table.getCellRect(table.getSelectedRow(), table.getSelectedColumn(), false);
                if (rect.equals(new Rectangle()))
                    return new MTable((JTable) component, name, getFinder(), windowMonitor);
                obj = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
            } else {
                if (obj == null) {
                    return new MTable((JTable) component, name, getFinder(), windowMonitor);
                }
            }
            try {
                return new MTableCell((JTable) component, name, obj, getFinder(), windowMonitor);
            } catch (ComponentException e) {
                if (obj instanceof String)
                    throw e;
                return new MTable((JTable) component, name, getFinder(), windowMonitor);
            }
        } else if (component instanceof JList) {
            JList list = (JList) component;
            if (isRecording() && obj == null) {
                Rectangle rect = list.getCellBounds(list.getSelectedIndex(), list.getSelectedIndex());
                if (rect == null)
                    return new MList(component, name, getFinder(), windowMonitor);
                obj = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
            } else {
                if (obj == null || list.getModel().getSize() == 0) {
                    return new MList(component, name, getFinder(), windowMonitor);
                }
            }
            return new MListCell((JList) component, name, obj, getFinder(), windowMonitor);
        } else if (component instanceof JTree) {
            if (isRecording() && obj == null) {
                JTree tree = (JTree) component;
                int[] rows = tree.getSelectionRows();
                if (rows == null || rows.length != 1) {
                    return new MTree(component, name, getFinder(), windowMonitor);
                }
                Rectangle rect = tree.getRowBounds(rows[0]);
                if (rect == null)
                    return new MTree(component, name, getFinder(), windowMonitor);
                obj = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
            } else {
                if (obj == null)
                    return new MTree(component, name, getFinder(), windowMonitor);
            }
            return new MTreeNode(component, name, obj, getFinder(), windowMonitor);
        } else if (component instanceof JScrollBar) {
            return new MNullComponent(component, name, getFinder(), windowMonitor);
        } else if (ignoreClass(component)) {
            return new MNullComponent(component, name, getFinder(), windowMonitor);
        } else {
            return new MUnknownComponent(component, name, getFinder(), windowMonitor);
        }
    }
}
