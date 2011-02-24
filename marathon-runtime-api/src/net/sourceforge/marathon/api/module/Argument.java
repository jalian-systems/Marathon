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
package net.sourceforge.marathon.api.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Argument implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        STRING, REGEX, NUMBER, NONE, BOOLEAN
    }

    private final String name;
    private final String defaultValue;
    private final List<String> defaultList;
    private final Type type;
    private static final List<String> trueList = new ArrayList<String>();
    private static final List<String> falseList = new ArrayList<String>();

    static {
        trueList.add("true");
        trueList.add("false");
        falseList.add("false");
        falseList.add("true");
    }

    public Argument(String name) {
        this.name = name;
        this.defaultValue = null;
        this.type = Type.NONE;
        this.defaultList = null;
    }

    public Argument(String name, String defaultValue, Type type) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
        this.defaultList = null;
    }

    public Argument(String name, List<String> defaultList, Type type) {
        this.name = name;
        this.defaultList = defaultList;
        this.type = type;
        this.defaultValue = null;
    }

    public String getName() {
        return name;
    }

    public String getDefault() {
        if (defaultValue == null || type == Type.BOOLEAN)
            return null;
        return defaultValue;
    }

    public List<String> getDefaultList() {
        if (type == Type.BOOLEAN) {
            if (defaultValue.equals("true"))
                return trueList;
            else
                return falseList;
        }
        return defaultList;
    }

    @Override public String toString() {
        if (defaultValue == null)
            return name;
        return name + "(= " + defaultValue + ")";
    }

    public String encode(String text) {
        if (type == Type.REGEX)
            return "/" + text + "/";
        else if (type == Type.STRING)
            return "\"" + text + "\"";
        else
            return text ;
    }
}