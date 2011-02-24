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

    /**
     * Return the relative path name from base to the given directory name
     * 
     * @param current
     * @return
     */
    public String getRelative2(String current) {
        String currentToken = null;
        StringTokenizer tok = new StringTokenizer(current, File.separator);
        int baseIndex = 0;
        for (baseIndex = 0; baseIndex < pathElements.size() && tok.hasMoreTokens(); baseIndex++) {
            if (!(currentToken = tok.nextToken()).equals(pathElements.get(baseIndex)))
                break;
        }
        if (baseIndex == 0 && current.charAt(0) != '/')
            // Files might exist on different drives on Windows
            return current;
        if (!tok.hasMoreElements()
                && (baseIndex < pathElements.size() - 1 || (baseIndex == pathElements.size() - 1 && currentToken
                        .equals(pathElements.get(baseIndex))))) {
            // Base: /home/marathon/workspace Current: /home/marathon return: ..
            String rest = "";
            for (int i = baseIndex; i < pathElements.size() - 1; i++)
                rest += ".." + File.separator;
            rest += "..";
            if (baseIndex == 0)
                // Base: /home/marathon Current: /var
                rest += File.separator + currentToken;
            return rest;
        }
        if (baseIndex == pathElements.size() && tok.hasMoreElements()) {
            // Base: /home/marathon Current: /home/marathon/workspace return:
            // workspace
            String rest = tok.nextToken();
            while (tok.hasMoreTokens())
                rest += File.separator + tok.nextToken();
            return rest;
        }
        if (baseIndex == pathElements.size() && !tok.hasMoreTokens())
            return ".";
        // Both the paths forked somewhere midway
        // Base: /home/marathon/current/workspace Current:
        // /home/marathon/previous/workspace
        // Return: ../../previous/workspace
        String rest = "";
        for (int i = baseIndex; i < pathElements.size(); i++)
            rest += ".." + File.separator;
        rest += currentToken;
        while (tok.hasMoreTokens())
            rest += File.separator + tok.nextToken();
        return rest;
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
        if (baseIndex == 0 || currentPath.charAt(0) != File.separatorChar)
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
            String rest = "";
            for (int i = baseIndex; i < pathElements.size() - 1; i++)
                rest += ".." + File.separator;
            rest += "..";
            return rest;
        }
        if (baseIndex == pathElements.size() && currentIndex < current.pathElements.size()) {
            String rest = "";
            for (int i = currentIndex; i < current.pathElements.size() - 1; i++)
                rest += current.pathElements.get(i) + File.separator;
            rest += current.pathElements.get(current.pathElements.size() - 1);
            return rest;
        }
        String rest = "";
        for (int i = baseIndex; i < pathElements.size() - 1; i++)
            rest += ".." + File.separator;
        rest += "..";
        for (int i = currentIndex; i < current.pathElements.size(); i++)
            rest += File.separator + current.pathElements.get(i);
        return rest;
    }
}
