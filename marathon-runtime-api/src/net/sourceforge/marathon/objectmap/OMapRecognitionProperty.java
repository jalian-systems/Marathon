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
package net.sourceforge.marathon.objectmap;

import net.sourceforge.marathon.component.IPropertyAccessor;

public class OMapRecognitionProperty {
    public static final String METHOD_EQUALS_IGNORE_CASE = "equalsIgnoreCase";
    public static final String METHOD_EQUALS = "equals";
    public static final String METHOD_MATCHES = "matches";
    public static final String METHOD_STARTS_WITH = "startsWith";
    public static final String METHOD_ENDS_WITH = "endsWith";
    public static final String METHOD_CONTAINS = "contains";

    private String name;
    private String value;
    private String method;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isMatch(IPropertyAccessor pa) {
        String paValue = pa.getProperty(name);
        if (paValue == null)
            return false;
        if (method.equals(METHOD_ENDS_WITH))
            return paValue.endsWith(value);
        else if (method.equals(METHOD_EQUALS))
            return paValue.equals(value);
        else if (method.equals(METHOD_EQUALS_IGNORE_CASE))
            return paValue.equalsIgnoreCase(value);
        else if (method.equals(METHOD_MATCHES))
            return paValue.matches(value);
        else if (method.equals(METHOD_STARTS_WITH))
            return paValue.startsWith(value);
        else if (method.equals(METHOD_CONTAINS))
            return paValue.contains(value);
        return false;
    }
    
    @Override public String toString() {
        return "[" + name + ", " + value + ", " + method + "]";
    }

    public static String[] getMethodOptions() {
        return new String[] { METHOD_CONTAINS, METHOD_ENDS_WITH, METHOD_EQUALS, METHOD_EQUALS_IGNORE_CASE, METHOD_MATCHES, METHOD_STARTS_WITH };
    }
}