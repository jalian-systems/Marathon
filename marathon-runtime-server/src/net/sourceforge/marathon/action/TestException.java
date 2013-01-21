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
package net.sourceforge.marathon.action;

import java.io.File;
import java.io.FilenameFilter;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class TestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    protected final IScriptModelServerPart scriptModel;
    private final WindowMonitor windowMonitor;
    private boolean abortTestCase;

    public TestException(String message, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor, boolean abortTestCase) {
        super(message);
        this.scriptModel = scriptModel;
        this.windowMonitor = windowMonitor;
        this.abortTestCase = abortTestCase;
    }

    public void captureScreen() {
        String captureDir = System.getProperty(Constants.PROP_IMAGE_CAPTURE_DIR);
        if (captureDir != null) {
            File dir = new File(captureDir);
            File[] files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.matches(".*error[0-9]*.png");
                }
            });
            if (files == null)
                files = new File[0];
            String errorFile = captureDir + File.separator + "error" + Integer.toString(files.length + 1) + ".png";
            new ScreenCaptureAction(errorFile, scriptModel, windowMonitor).play(null);
        }
    }
    
    public boolean isAbortTestCase() {
        return abortTestCase;
    }
}
