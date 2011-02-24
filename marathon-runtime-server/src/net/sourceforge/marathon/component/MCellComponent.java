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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.recorder.WindowMonitor;

/**
 * <code>MCellComponent</code> represents Components that are part of a
 * Collection component. Examples in the standard Swing components include Table
 * cells, list items and tree nodes.
 * 
 * You may want to create a subclass of <code>MCellComponent</code> if you are
 * writing a Component resolver for a component that can be considered to be
 * made up of contained components.
 */

public abstract class MCellComponent extends MComponent {
    /**
     * Constructs a Cell component given the component being wrapped and a name.
     * Note that the component is usually a collection component like table or a
     * tree.
     * 
     * @param component
     * @param name
     * @param windowMonitor
     */
    public MCellComponent(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    /**
     * Return the containing component
     * 
     * @return collection component containing this component.
     */
    public abstract MCollectionComponent getCollectionComponent();

    /**
     * Same as above, but sets the WindowId into the collection component.
     * 
     * @return
     */
    public MCollectionComponent getCollectionComponentWithWindowID() {
        MCollectionComponent c = getCollectionComponent();
        c.setWindowId(getWindowId());
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.MComponent#clickNeeded(java.awt.event
     * .MouseEvent)
     */
    public int clickNeeded(MouseEvent e) {
        return isPopupTrigger(e) || e.getClickCount() > 1 ? ClickAction.RECORD_CLICK : ClickAction.RECORD_NONE;
    }

    /**
     * Set the current selection to this cell component. Needed when used with
     * drag and drop.
     */
    public abstract void setCurrentSelection();

    public List<Object> getMethods() {
        ArrayList<Object> l = new ArrayList<Object>();
        if (getText() != null)
            addMethod(l, "getText");
        for (int i = 0; i < propertyList.size(); i++) {
            AssertProperty prop = (AssertProperty) propertyList.get(i);
            if (prop.forClass == null || prop.forClass.isInstance(getComponent()))
                l.add(new AssertPropertyInstance(prop, getPropertyObject(prop.property)));
        }
        addMethod(l, getCollectionComponentAccessMethodName());
        return l;
    }

    protected String createPropertyMapString(String[] properties) {
        Properties props = new Properties();
        for (int i = 0; i < properties.length; i++) {
            String value = getProperty(properties[i]);
            if (value == null)
                value = "null";
            props.setProperty(properties[i], value);
        }
        return PropertyHelper.toString(props, properties);
    }

    protected Properties parseProperties(String info, String[][] defaultProperties) {
        if (oldFormat(info))
            return null;
        Properties props = PropertyHelper.fromString(info, defaultProperties);
        return props;
    }

    protected abstract boolean oldFormat(String info);

    protected abstract String getCollectionComponentAccessMethodName();

}
