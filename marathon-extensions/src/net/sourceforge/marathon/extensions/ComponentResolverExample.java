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
package net.sourceforge.marathon.extensions;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JPasswordField;
import javax.swing.JScrollBar;

import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentResolver;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;

/**
 * This is a sample component resolver. For the time being this provides A
 * resolver for JScrollBar that ignores any clicks performed on the scroll bar A
 * resolver for JPasswordField that encrypts (!) the password field TBD: A
 * resolver supporting composite components like tables, lists
 * 
 */
public class ComponentResolverExample extends ComponentResolver {
    /**
     * There is nothing much to be done here
     * 
     * @param finder
     * @param windowMonitor
     */
    public ComponentResolverExample(ComponentFinder finder, boolean isRecording, WindowMonitor windowMonitor) {
        super(finder, isRecording, windowMonitor);
    }

    /**
     * canHandle should return true for the components that is supported by this
     * resolver. In case of scrollbar we check for the component itself as well
     * as parent (in case of the scroll buttons the parent will be a scrollbar.
     */
    public boolean canHandle(Component component, Point location) {
        if (component instanceof JScrollBar || component.getParent() instanceof JScrollBar)
            return true;
        if (component instanceof JPasswordField)
            return true;
        return false;
    }

    /**
     * Straight forward. We support JPasswordField - so return the corresponding
     * MComponent. Scrollbars need to be ignored - so we return back a null.
     */
    public MComponent getMComponent(Component component, String name, Object obj) {
        if (component instanceof JPasswordField)
            return new MPasswordField(component, name, getFinder(), windowMonitor);
        return null;
    }

    /**
     * In case of a JPasswordField we need to record the stuff - so return the
     * component itself. Scrollbars will be ignored because of the return null.
     */
    public Component getComponent(Component component, Point location) {
        if (component instanceof JPasswordField)
            return component;
        return null;
    }
}
