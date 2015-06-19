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
package net.sourceforge.marathon.component;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import net.sourceforge.marathon.recorder.WindowMonitor;

public class MFileChooser extends MComponent {

    private static final String homeDir;
    private static final String cwd;
    private static final String marathonDir;

    static {
        homeDir = getRealPath(System.getProperty("user.home", null));
        cwd = getRealPath(System.getProperty("user.dir", null));
        marathonDir = getRealPath(System.getProperty("marathon.project.dir", null));
    }

    private JFileChooser fileChooser;

    public MFileChooser(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
        fileChooser = (JFileChooser) component;
        if (finder != null && finder.isRecording())
            getFinder().markEntryNeeded(this);
    }

    private static String getRealPath(String path) {
        if (path == null)
            return null;
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

    public String getText() {
        File[] selectedfiles = null;
        boolean isMultiSelectionEnabled = eventQueueRunner.invokeBoolean(fileChooser, "isMultiSelectionEnabled");
        if (isMultiSelectionEnabled) {
            selectedfiles = (File[]) eventQueueRunner.invoke(fileChooser, "getSelectedFiles");
        } else {
            File selectedfile = (File) eventQueueRunner.invoke(fileChooser, "getSelectedFile");
            if (selectedfile != null) {
                selectedfiles = new File[] { selectedfile };
            }
        }
        if (selectedfiles == null || selectedfiles.length == 0 || !isApproveAction())
            return "";
        return encode(selectedfiles);
    }

    private boolean isApproveAction() {
        String value = (String) eventQueueRunner.invoke(fileChooser, "toString");
        return (value.indexOf("returnValue=APPROVE_OPTION") != -1);
    }

    public static String encode(File[] selectedfiles) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < selectedfiles.length; i++) {
            String encode = encode(selectedfiles[i]);
            if (encode != null)
                buffer.append(encode);
            if (i < selectedfiles.length - 1)
                buffer.append(';');
        }
        return buffer.toString();
    }

    public static String encode(File file) {
        String path;
        try {
            path = file.getCanonicalPath();

            String prefix = "";
            if (marathonDir != null && path.startsWith(marathonDir)) {
                prefix = "#M";
                path = path.substring(marathonDir.length());
            } else if (cwd != null && path.startsWith(cwd)) {
                prefix = "#C";
                path = path.substring(cwd.length());
            } else if (homeDir != null && path.startsWith(homeDir)) {
                prefix = "#H";
                path = path.substring(homeDir.length());
            }
            return (prefix + path).replace(File.separatorChar, '/');

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setText(final String text) {
        if (text.equals("")) {
            eventQueueRunner.invoke(fileChooser, "cancelSelection");
            return;
        }
        boolean isMultiSelectionEnabled = eventQueueRunner.invokeBoolean(fileChooser, "isMultiSelectionEnabled");
        if (isMultiSelectionEnabled) {
            File[] decode = decode(text);
            eventQueueRunner.invoke(fileChooser, "setSelectedFiles", new Object[] { decode }, new Class[] { File[].class });
        } else {
            File decodeFile = decodeFile(text);
            eventQueueRunner.invoke(fileChooser, "setSelectedFile", new Object[] { decodeFile }, new Class[] { File.class });
        }
        eventQueueRunner.invoke(fileChooser, "repaint");
        swingWait();
        eventQueueRunner.invoke(fileChooser, "approveSelection");
    }

    private File[] decode(String text) {
        ArrayList<File> files = new ArrayList<File>();
        StringTokenizer tokenizer = new StringTokenizer(text, ";");
        while (tokenizer.hasMoreElements()) {
            File file = decodeFile((String) tokenizer.nextElement());
            if (file != null)
                files.add(file);
        }
        return (File[]) files.toArray(new File[files.size()]);
    }

    private File decodeFile(String path) {
        String prefix = "";
        if (path.startsWith("#M")) {
            prefix = marathonDir;
            path = path.substring(2);
        } else if (path.startsWith("#C")) {
            prefix = cwd;
            path = path.substring(2);
        } else if (path.startsWith("#H")) {
            prefix = homeDir;
            path = path.substring(2);
        }

        return new File((prefix + path.replace('/', File.separatorChar)));
    }

    public boolean recordAlways() {
        return true;
    }
}
