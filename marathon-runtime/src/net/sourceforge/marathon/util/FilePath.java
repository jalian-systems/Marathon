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
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * FilePath provides makes it easy to work with file paths.
 */
public class FilePath {
    ArrayList<String> pathElements = new ArrayList<String>();

    /**
     * Construct a FilePath with the given base directory. It is expected that
     * all paths passed to FilePath are returned by
     * {@link File#getCanonicalPath()}
     * 
     * @param base
     *            , path to the base directory
     * @throws Exception
     */
    public FilePath(String base) {
        StringTokenizer tok = new StringTokenizer(base, File.separator);
        while (tok.hasMoreTokens())
            pathElements.add(tok.nextToken());
    }

    public boolean isRelative(String currentPath) {
        FilePath current = new FilePath(currentPath);
        int baseIndex = 0;
        int currentIndex = 0;
        while (baseIndex < pathElements.size() && currentIndex < current.pathElements.size()
                && pathElements.get(baseIndex).equals(current.pathElements.get(currentIndex))) {
            baseIndex++;
            currentIndex++;
        }
        if (baseIndex == 0)
            // Files might exist on different drives on Windows
            return false;
        return true;
    }

    public String getRelative(String currentPath) {
        FilePath current = new FilePath(currentPath);
        int baseIndex = 0;
        int currentIndex = 0;
        while (baseIndex < pathElements.size() && currentIndex < current.pathElements.size()
                && pathElements.get(baseIndex).equals(current.pathElements.get(currentIndex))) {
            baseIndex++;
            currentIndex++;
        }
        if (baseIndex == 0 && currentPath.charAt(0) != File.separatorChar)
            // Files might exist on different drives on Windows
            return currentPath;
        if (baseIndex == pathElements.size() && currentIndex == current.pathElements.size())
            return ".";
        if (baseIndex < pathElements.size() && currentIndex == current.pathElements.size()) {
            StringBuilder rest = new StringBuilder();
            for (int i = baseIndex; i < pathElements.size() - 1; i++)
                rest.append("..").append(File.separator);
            rest.append("..");
            return rest.toString();
        }
        if (baseIndex == pathElements.size() && currentIndex < current.pathElements.size()) {
            StringBuilder rest = new StringBuilder();
            for (int i = currentIndex; i < current.pathElements.size() - 1; i++)
                rest.append(current.pathElements.get(i)).append(File.separator);
            rest.append(current.pathElements.get(current.pathElements.size() - 1));
            return rest.toString();
        }
        StringBuilder rest = new StringBuilder();
        for (int i = baseIndex; i < pathElements.size() - 1; i++)
            rest.append("..").append(File.separator);
        rest.append("..");
        for (int i = currentIndex; i < current.pathElements.size(); i++)
            rest.append(File.separator).append(current.pathElements.get(i));
        return rest.toString();
    }
}
