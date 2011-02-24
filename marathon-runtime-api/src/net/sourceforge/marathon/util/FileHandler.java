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

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import net.sourceforge.marathon.editor.IMarathonFileFilter;

public class FileHandler {
    private static final String NL = System.getProperty("line.separator");
    private File currentFile;
    private IMarathonFileFilter filter;
    private File fixtureDirectory;
    private File[] moduleDirectories;
    private File rootDirectory;
    private File testDirectory;
    private INameValidateChecker nameValidateChecker;

    public FileHandler(IMarathonFileFilter filter, File testDirectory, File fixtureDirectory, File[] moduleDirectories,
            INameValidateChecker nameValidateChecker) {
        this.filter = filter;
        this.testDirectory = testDirectory;
        this.fixtureDirectory = fixtureDirectory;
        this.moduleDirectories = moduleDirectories;
        this.nameValidateChecker = nameValidateChecker;
        rootDirectory = new File("");
    }

    public void clearCurrentFile() {
        setCurrentFile(null);
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public File getFile(String fileName) {
        try {
            File file;
            if (fileName.contains(testDirectory.getCanonicalPath())) {
                String relativeFileName = fileName.substring(testDirectory.getCanonicalPath().length() + 1, fileName.length());
                file = new File(testDirectory, relativeFileName);
                if (file.exists()) {
                    if (file.isFile())
                        return file;
                }
            } else
                for (int i = 0; i < moduleDirectories.length; i++) {
                    if (fileName.contains(moduleDirectories[i].getCanonicalPath())) {
                        String relativeFileName = fileName.substring(moduleDirectories[i].getCanonicalPath().length() + 1,
                                fileName.length());
                        file = new File(moduleDirectories[i], relativeFileName);
                    } else
                        file = new File(moduleDirectories[i], fileName);
                    if (file.exists()) {
                        if (file.isFile())
                            return file;
                    }
                }
            file = new File(fixtureDirectory, fileName);
            if (file.exists())
                return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public boolean isModuleFile() {
        for (int i = 0; i < moduleDirectories.length; i++) {
            if (rootDirectory.equals(moduleDirectories[i]))
                return true;
        }
        return false;
    }

    public boolean isProjectFile() {
        if (rootDirectory.equals(testDirectory) || rootDirectory.equals(fixtureDirectory))
            return true;
        for (int k = 0; k < moduleDirectories.length; k++) {
            if (rootDirectory.equals(moduleDirectories[k]))
                return true;
        }
        return false;
    }

    public boolean isTestFile() {
        return rootDirectory.equals(testDirectory);
    }

    public String readFile(File file) throws IOException {
        setCurrentFile(file);
        return readFile();
    }

    public File save(String script, Component parent) throws IOException {
        if (currentFile != null) {
            saveToFile(currentFile, script);
            return currentFile;
        } else {
            return saveAs(script, parent);
        }
    }

    public File saveAs(String script, Component parent) throws IOException {
        boolean saved = false;
        while (!saved) {
            File file = askForFile(parent);
            if (file == null)
                return null;
            int option = JOptionPane.YES_OPTION;
            if (file.exists()) {
                if (nameValidateChecker != null && !nameValidateChecker.okToOverwrite(file))
                    return null;
                option = JOptionPane.showConfirmDialog(parent, "File " + file.getName()
                        + " already exists. Do you want to overwrite?", "File exists", JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (option == JOptionPane.YES_OPTION) {
                setCurrentFile(file);
                saveToFile(currentFile, script);
                return file;
            }
            if (option == JOptionPane.CANCEL_OPTION)
                return null;
        }
        return null;
    }

    public File saveTo(File file, String script) throws IOException {
        if (file != null) {
            setCurrentFile(file);
            saveToFile(currentFile, script);
        }
        return file;
    }

    public void setCurrentDirectory(File directory) {
        try {
            rootDirectory = directory.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File askForFile(Component parent) {
        final File startDirectory = currentFile == null ? rootDirectory : currentFile.getParentFile();
        JFileChooser chooser = new JFileChooser(startDirectory, new FileSystemView() {
            public File createNewFolder(File containingDir) throws IOException {
                if (containingDir == null)
                    throw new IOException("Parent Directory is null");
                File newDirectory = new File(containingDir, "New Suite");
                int i = 1;
                while (newDirectory.exists() && i < 100) {
                    newDirectory = new File(containingDir, "New Suite" + i);
                    i++;
                }
                if (newDirectory.exists())
                    throw new IOException("Directory exists");
                newDirectory.mkdir();
                return newDirectory;
            }

            public File getDefaultDirectory() {
                return startDirectory;
            }

            public File getHomeDirectory() {
                return startDirectory;
            }

            public File[] getRoots() {
                return new File[] { startDirectory };
            }

            public boolean isRoot(File f) {
                return f.equals(startDirectory);
            }
        });
        chooser.addChoosableFileFilter(filter.getChooserFilter());
        chooser.setFileFilter(filter.getChooserFilter());
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(parent)) {
            File selectedFile = chooser.getSelectedFile();
            String suffix = filter.getSuffix();
            if (suffix == null)
                throw new RuntimeException("Could not find suffix needed for the script");
            if (selectedFile.getName().indexOf('.') == -1 && !selectedFile.getName().endsWith(suffix))
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + suffix);
            return selectedFile;
        } else {
            return null;
        }
    }

    private File getRootDir(File file) {
        try {
            String filePath = file.getCanonicalPath();
            if (filePath.startsWith(testDirectory.getCanonicalPath()))
                return testDirectory;
            for (int i = 0; i < moduleDirectories.length; i++) {
                if (filePath.startsWith(moduleDirectories[i].getCanonicalPath()))
                    return moduleDirectories[i];
            }
            if (filePath.startsWith(fixtureDirectory.getCanonicalPath()))
                return fixtureDirectory;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new File("");
    }

    private String readFile() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(currentFile), Charset.defaultCharset()));
        try {
            StringBuffer buffer = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + NL);
            }
            reader.close();
            String s = buffer.toString();
            return s;
        } finally {
            reader.close();
        }
    }

    private void saveToFile(File file, String script) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset());
        try {
            out.write(script);
        } finally {
            out.close();
        }
    }

    private void setCurrentFile(File file) {
        currentFile = file;
        if (file != null) {
            rootDirectory = getRootDir(file);
        }
    }

    public String getMode(String fileName) {
        if (fileName == null)
            return "text";
        String ext = "";
        if (fileName.startsWith("Untitled"))
            ext = filter.getSuffix().substring(1);
        else {
            int lastIndexOf = fileName.lastIndexOf('.');
            if (lastIndexOf == -1 || lastIndexOf == fileName.length() - 1)
                return "text";
            ext = fileName.substring(lastIndexOf + 1);
        }
        if (ext.equals("py"))
            return "python";
        if (ext.equals("rb"))
            return "ruby";
        if (ext.equals("xml"))
            return "xml";
        if (ext.equals("html"))
            return "html";
        return "text";
    }

    public boolean isFixtureFile() {
        return rootDirectory.equals(fixtureDirectory);
    }

    public String getFixture() {
        if (!isFixtureFile())
            throw new RuntimeException("Current file is not a fixture file");
        try {
            String rootPath = rootDirectory.getCanonicalPath();
            String filePath = currentFile.getCanonicalPath();
            if (!filePath.startsWith(rootPath))
                throw new RuntimeException("Fixture is not in fixture directory?");
            String fixtureFileName = filePath.substring(rootPath.length() + 1);
            int indexOfDot = fixtureFileName.lastIndexOf('.');
            return fixtureFileName.substring(0, indexOfDot);
        } catch (IOException e) {
            throw new RuntimeException("getFixture" + e.getMessage());
        }
    }

}
