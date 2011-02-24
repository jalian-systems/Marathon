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

import net.sourceforge.marathon.recorder.WindowMonitor;

/**
 * The <code>ComponentResolver</code> class provides the interface required to
 * extend Marathon to support custom Components.
 * 
 * Each component resolver is associated with a set of {@link MComponent}s.
 */
public abstract class ComponentResolver {
    private ComponentFinder finder;
    private boolean isRecording;
    protected final WindowMonitor windowMonitor;

    /**
     * Constructs a component resolver.
     * 
     * @param finder
     *            , a component finder
     * @param isRecording
     *            , are we recording or playing.
     * @param windowMonitor
     */
    public ComponentResolver(ComponentFinder finder, boolean isRecording, WindowMonitor windowMonitor) {
        this.finder = finder;
        this.isRecording = isRecording;
        this.windowMonitor = windowMonitor;
    }

    /**
     * Returns the ComponentFinder attached to this resolver.
     * 
     * @return
     */
    public ComponentFinder getFinder() {
        return finder;
    }

    /**
     * Can this resolver handle this component?
     * 
     * @param component
     * @return
     */
    public boolean canHandle(Component component) {
        return canHandle(component, null);
    }

    /**
     * Can this resolver handle this component. When Marathon receives an event
     * from a component, it invokes each <code>ComponentResolver</code> in the
     * given order to find the resolver that can handle the component. If none
     * of the resolvers can handle this component, Marathon falls back on
     * {@link DefaultComponentResolver}
     * 
     * @param component
     * @param location
     * @return
     */
    abstract public boolean canHandle(Component component, Point location);

    /**
     * <code>getComponent</code> method returns the component for which the
     * current event needs to be recorded.
     * 
     * Note that the component on which event occured might not be the one on
     * which the event need to be recorded. For example, when the user clicks on
     * the up/down arrows on a JSpinner, we still want to record an event on the
     * MSpinner rather than the given buttons.
     * 
     * @param component
     *            - The onscreen component that received an event
     * @param location
     *            - The coordinates of the mouse pointer
     * @return the actual component for which the event should be recorded
     */
    abstract public Component getComponent(Component component, Point location);

    /**
     * <code>getMComponent</code> method returns the corresponding MComponent
     * for a given component.
     * 
     * This is called with the component returned by getComponent. You need to
     * wrap it up with a corresponding <code>MComponent</code> and return it.
     * 
     * @param component
     *            - The Java/Swing component
     * @param name
     *            - The name of the component as resolved by naming strategy
     * @param obj
     *            - Extra information (point-location!)
     * @return the MComponent
     */
    abstract public MComponent getMComponent(Component component, String name, Object obj);

    /**
     * Are we recording?
     * 
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Set the recording flag.
     * 
     * @param isRecording
     */
    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }
}
