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
package net.sourceforge.marathon.util;

import java.util.StringTokenizer;

public class ValidationUtil {

    public static boolean isValidClassName(String className) {
        if (className.contains(".."))
            return false;
        StringTokenizer tok = new StringTokenizer(className, ".");
        while (tok.hasMoreTokens()) {
            if (!ValidationUtil.isValidIdentifier(tok.nextToken()))
                return false;
        }
        return true;
    }

    public static boolean isValidIdentifier(String part) {
        char[] cs = part.toCharArray();
        if (cs.length == 0 || !Character.isJavaIdentifierStart(cs[0]))
            return false;
        for (int i = 1; i < cs.length; i++) {
            if (!Character.isJavaIdentifierPart(cs[i]))
                return false;
        }
        return true;
    }

    public static boolean isValidMethodName(String text) {
        return isValidIdentifier(text);
    }

}
