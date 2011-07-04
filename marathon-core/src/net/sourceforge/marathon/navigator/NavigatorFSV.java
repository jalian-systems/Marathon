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
package net.sourceforge.marathon.navigator;

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

final class NavigatorFSV extends FileSystemView {
    private File[] rootDirectory;

    public NavigatorFSV(File[] rootDirectory) {
        this.rootDirectory = new File[rootDirectory.length];
        for (int i = 0; i < rootDirectory.length; i++) {
            try {
                this.rootDirectory[i] = rootDirectory[i].getCanonicalFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public File createNewFolder(File containingDir) throws IOException {
        if (containingDir == null)
            throw new IOException("Parent Directory is null");
        File newDirectory = new File(containingDir, "NewFolder");
        int i = 1;
        while (newDirectory.exists() && i < 100) {
            newDirectory = new File(containingDir, "NewFolder" + i);
            i++;
        }
        if (newDirectory.exists())
            throw new IOException("Directory exists");
        if (!newDirectory.mkdir())
            throw new IOException("Unable to create folder: " + newDirectory);
        return newDirectory;
    }

    public File getDefaultDirectory() {
        return rootDirectory[0];
    }

    public File getHomeDirectory() {
        return rootDirectory[0];
    }

    public File[] getRoots() {
        return rootDirectory;
    }

    public boolean isRoot(File f) {
        for (int i = 0; i < rootDirectory.length; i++) {
            if (f.equals(rootDirectory[i]))
                return true;
        }
        return false;
    }
}
