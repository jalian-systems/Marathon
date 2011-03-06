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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class ComponentId implements Serializable {
    private static final String INFO_KEY = ComponentId.class.getName() + ".info";
    private static final String NAME_KEY = ComponentId.class.getName() + ".name";
    private static final long serialVersionUID = 1L;
    private final Properties nameProps = new Properties();
    private final Properties componentInfoProps = new Properties();

    public ComponentId(String name) {
        this(name, null);
    }

    public ComponentId(Object name, Object componentInfo) {
        if (name instanceof String)
            nameProps.put(NAME_KEY, name);
        else if (name instanceof Map) {
            @SuppressWarnings("unchecked")
            Set<Entry<Object, Object>> values = ((Map<Object, Object>) name).entrySet();
            for (Entry<Object, Object> entry : values) {
                nameProps.put(entry.getKey().toString(), entry.getValue().toString());
            }
        } else {
            throw new RuntimeException("Invalid component id");
        }
        if (componentInfo != null) {
            if (componentInfo instanceof String) {
                componentInfoProps.put(INFO_KEY, componentInfo);
            } else if (componentInfo instanceof Map) {
                @SuppressWarnings("unchecked")
                Set<Entry<Object, Object>> values = ((Map<Object, Object>) componentInfo).entrySet();
                for (Entry<Object, Object> entry : values) {
                    componentInfoProps.put(entry.getKey().toString(), entry.getValue().toString());
                }
            } else {
                throw new RuntimeException("Invalid component id");
            }
        }
    }

    public String getName() {
        return nameProps.getProperty(NAME_KEY);
    }

    public String getComponentInfo() {
        return componentInfoProps.getProperty(INFO_KEY);
    }

    public String toString() {
        return "('" + nameProps + "'" + (componentInfoProps != null ? ", '" + componentInfoProps + "'" : "") + ")";
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ComponentId))
            return false;
        final ComponentId componentId = (ComponentId) o;
        if (componentInfoProps != null ? !componentInfoProps.equals(componentId.componentInfoProps)
                : componentId.componentInfoProps != null)
            return false;
        if (!nameProps.equals(componentId.nameProps))
            return false;
        return true;
    }

    public int hashCode() {
        int result;
        result = nameProps.hashCode();
        result = 29 * result + (componentInfoProps != null ? componentInfoProps.hashCode() : 0);
        return result;
    }

    public Object getComponentInfoProps() {
        if (componentInfoProps.size() == 0)
            return null ;
        if (componentInfoProps.size() > 1)
            return componentInfoProps;
        return componentInfoProps.get(INFO_KEY) == null ? componentInfoProps : null;
    }

    public Properties getNameProps() {
        if (componentInfoProps.size() > 1)
            return nameProps;
        return nameProps.get(NAME_KEY) == null ? nameProps : null;
    }
}
