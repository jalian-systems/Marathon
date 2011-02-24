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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class Path implements Serializable {
    private static final long serialVersionUID = 1L;
    File[] pathElements;

    public Path(String pathString) {
        if (pathString == null) {
            pathElements = new File[0];
            return;
        } else {
            parsePath(pathString);
        }
    }

    public File[] elements() {
        return pathElements;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < pathElements.length; i++) {
            File pathElement = pathElements[i];
            buf.append(pathElement.getPath());
            if ((i + 1) != pathElements.length) {
                buf.append(File.pathSeparator);
            }
        }
        return buf.toString();
    }

    private void parsePath(String pathString) {
        ArrayList<File> paths = new ArrayList<File>();
        StringTokenizer pathTokenizer = new StringTokenizer(pathString, File.pathSeparator, false);
        while (pathTokenizer.hasMoreTokens()) {
            String pathElement = pathTokenizer.nextToken();
            if ("".equals(pathElement.trim())) {
                continue;
            }
            paths.add(new File(pathElement));
        }
        exportPaths(paths);
    }

    public void addElement(String pathElement) {
        if (pathElement == null) {
            return;
        }
        if ("".equals(pathElement.trim())) {
            return;
        }
        if (pathElement.indexOf(File.pathSeparator) != -1) {
            throw new IllegalArgumentException("path element '" + pathElement + "' contains the path seperator");
        }
        List<File> paths = new ArrayList<File>();
        paths.addAll(Arrays.asList(pathElements));
        paths.add(new File(pathElement));
        exportPaths(paths);
    }

    private void exportPaths(List<File> paths) {
        pathElements = (File[]) paths.toArray(new File[0]);
    }
}
