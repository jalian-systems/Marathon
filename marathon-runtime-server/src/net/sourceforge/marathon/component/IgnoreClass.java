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

/**
 * Objects of this class represent Components that we want to ignore. The
 * {@link DefaultComponentResolver} return a <code>MNullComponent</code> for
 * classes given in this list whose behavior is to record only special
 * keystrokes and mouse clicks.
 * 
 * If an unhandled Component is received that is not in the list that component
 * is wrapped in a {@link MUnknownComponent} whose behavior is to record all the
 * keystrokes and mouse events.
 * 
 */
public class IgnoreClass {
    String className;
    boolean checkChildren;

    /**
     * Construct one.
     * 
     * @param klass
     * @param checkChildren
     */
    public IgnoreClass(Class<?> klass, boolean checkChildren) {
        this.className = klass.getName();
        this.checkChildren = checkChildren;
    }

    /**
     * Constructs from a class name in the form of a string.
     * 
     * @param className
     * @param checkChildren
     * @throws ClassNotFoundException
     */
    public IgnoreClass(String className, boolean checkChildren) throws ClassNotFoundException {
        this(Class.forName(className), checkChildren);
    }

    /**
     * One more constructor for ease of use.
     * 
     * @param className
     * @throws ClassNotFoundException
     */
    public IgnoreClass(String className) throws ClassNotFoundException {
        this(className, false);
    }

    /**
     * Yet another constructor.
     * 
     * @param klass
     */
    public IgnoreClass(Class<?> klass) {
        this(klass, false);
    }

    /**
     * Check whether a given component is in the ignore list. if isChildren is
     * true, if any component in the heirarchy matches we ignore the component.
     * 
     * @param c
     * @return
     */
    public boolean matches(Component c) {
        if (c == null)
            return false;
        if (className.equals(c.getClass().getName()))
            return true;
        if (checkChildren)
            return matches(c.getParent());
        return false;
    }
}