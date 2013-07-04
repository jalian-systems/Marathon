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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public class FileUtils {

    public static void copyFiles(File src, File dest, FilenameFilter filter) {
        File[] files = src.listFiles(filter);
        if(files == null) {
            System.err.println("copyFiles: No files in src directory " + src);
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File srcFile = files[i];
            File destFile = new File(dest, srcFile.getName());
            try {
                copyFile(srcFile, destFile);
            } catch (IOException e) {
                System.err.println("Copy file failed: src = " + srcFile + " dest = " + destFile);
            }
        }
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        FileInputStream is = new FileInputStream(srcFile);
        FileOutputStream os = new FileOutputStream(destFile);
        int n;
        byte[] b = new byte[1024];
        while ((n = is.read(b)) != -1) {
            os.write(b, 0, n);
        }
        os.close();
        is.close();
    }

    public static File findFile(String home, String filename) {
        return findFile(new File(home), filename);
    }

    private static File findFile(File file, String filename) {
        if(file.getName().equals(filename))
            return file ;
        if(file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (File file2 : listFiles) {
                File found = findFile(file2, filename);
                if(found != null)
                    return found ;
            }
        }
        return null;
    }

}
