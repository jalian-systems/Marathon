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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class ClassPathHelper {

    public static String getClassPath(Class<?> klass) {
        String packageName = klass.getPackage().getName();
        return getClassPath(packageName, klass.getName());
    }

    public static String getClassPath(String packageName, String name) {
        name = name.replace('.', '/');
        URL url = ClassPathHelper.class.getResource("/" + name + ".class");
        if (url == null)
            return null;
        String resource = null;
        try {
            resource = URLDecoder.decode(url.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        if (resource.startsWith("jar:"))
            resource = resource.substring(4);
        if (resource.startsWith("file:"))
            resource = resource.substring(5);
        int index = resource.indexOf('!');
        if (index != -1)
            resource = resource.substring(0, index);
        else {
            String packagePath = packageName.replace('.', '/');
            index = resource.indexOf(packagePath);
            if (index != -1)
                resource = resource.substring(0, index - 1);
        }
        if (OSUtils.isWindowsOS()) {
            resource = resource.substring(1);
        }
        return resource.replace('/', File.separatorChar);
    }

    public static String getClassPath(String name) {
        int lastIndexOf = name.lastIndexOf('.');
        String packageName;
        if (lastIndexOf == -1)
            packageName = "";
        else
            packageName = name.substring(0, lastIndexOf);
        return getClassPath(packageName, name);
    }
}
