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
package net.sourceforge.marathon.api;

import java.io.Serializable;

public class ComponentId implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String componentInfo;

    public ComponentId(String name) {
        this(name, null);
    }

    public ComponentId(String name, String componentInfo) {
        this.name = name;
        this.componentInfo = componentInfo;
    }

    public String getName() {
        return name;
    }

    public String getComponentInfo() {
        return componentInfo;
    }

    public String toString() {
        return "('" + name + "'" + (componentInfo != null ? ", '" + componentInfo + "'" : "") + ")";
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ComponentId))
            return false;
        final ComponentId componentId = (ComponentId) o;
        if (componentInfo != null ? !componentInfo.equals(componentId.componentInfo) : componentId.componentInfo != null)
            return false;
        if (!name.equals(componentId.name))
            return false;
        return true;
    }

    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 29 * result + (componentInfo != null ? componentInfo.hashCode() : 0);
        return result;
    }
}
