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
package net.sourceforge.marathon.mpf;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileSelectionListener implements ActionListener {
    private File previousDir;
    private IFileSelectedAction fsl;
    private FileFilter fileFilter;
    /*
     * Theoritically we need to get the parent so that we can popup the
     * JFileChooser relative to the parent . But there are some focus problems
     * when we pass the parent to the file chooser. Let us leave it here and
     * work on it when we get more time. FIXME: Find why this does not work
     * private Component parent;
     */
    private int mode = JFileChooser.FILES_ONLY;
    private boolean multipleSelection = false;
    private Object cookie;
    private Component parent;

    public FileSelectionListener(IFileSelectedAction fsl, FileFilter filter, Component parent, Object cookie) {
        this.parent = parent;
        this.fsl = fsl;
        fileFilter = filter;
        previousDir = new File(System.getProperty("user.home"));
        this.cookie = cookie;
    }

    public FileSelectionListener(IFileSelectedAction fsl, Component parent, Object cookie) {
        this(fsl, null, parent, cookie);
    }

    public void setFileSelectionMode(int mode) {
        this.mode = mode;
    }

    public void setMultipleSelection(boolean selection) {
        multipleSelection = selection;
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(previousDir);
        if (fileFilter != null)
            chooser.setFileFilter(fileFilter);
        chooser.setFileSelectionMode(mode);
        chooser.setMultiSelectionEnabled(multipleSelection);
        int selectedOption = chooser.showDialog(parent, "Select");
        previousDir = chooser.getCurrentDirectory();
        if (selectedOption == JFileChooser.APPROVE_OPTION) {
            if (multipleSelection)
                fsl.filesSelected(chooser.getSelectedFiles(), cookie);
            else
                fsl.filesSelected(new File[] { chooser.getSelectedFile() }, cookie);
        }
    }
}
