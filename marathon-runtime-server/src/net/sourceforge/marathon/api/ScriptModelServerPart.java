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

import net.sourceforge.marathon.Constants;

public abstract class ScriptModelServerPart {
    private static IScriptModelServerPart instance;

    public static void initialize() {
        String property = System.getProperty(Constants.PROP_PROJECT_SCRIPT_MODEL);
        if (property == null)
            throw new IllegalArgumentException("Script model not set");
        try {
            Class<?> klass = Class.forName(property);
            instance = (IScriptModelServerPart) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Script model " + property + " not found - check class path");
        }
    }

    public static IScriptModelServerPart getModelServerPart() {
        if (instance == null)
            initialize();
        return instance;
    }

}
