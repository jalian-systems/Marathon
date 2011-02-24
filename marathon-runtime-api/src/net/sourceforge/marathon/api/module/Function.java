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
import java.util.List;

public class Function implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Argument> arguments;
    private final String doc;
    private String window;
    private Module parent;

    public Function(String fname, List<Argument> arguments, String doc, Module parent) {
        name = fname;
        this.arguments = arguments;
        this.doc = doc;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override public String toString() {
        return name ;
    }

    public String getDocumentation() {
        if (doc != null)
            return doc;
        return "";
    }

    public void setWindow(String window) {
        this.window = window ;
    }
    
    public String getWindow() {
        return window;
    }

    public Module getParent() {
        return parent;
    }

    public String[][] getArgumentArray() {
        String[] argArray = new String[arguments.size()];
        int defaultStart = -1;
        for (int i = 0; i < arguments.size(); i++) {
            argArray[i] = arguments.get(i).getName();
            if (arguments.get(i).getDefault() != null && defaultStart == -1)
                defaultStart = i;
        }
        String[] defaults = new String[0];
        if (defaultStart != -1) {
            defaults = new String[arguments.size() - defaultStart];
            for (int i = defaultStart; i < arguments.size(); i++)
                defaults[i - defaultStart] = arguments.get(i).getDefault();
        }
        return new String[][] { argArray, defaults };
    }
}