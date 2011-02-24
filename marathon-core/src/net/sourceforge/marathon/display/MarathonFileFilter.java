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
package net.sourceforge.marathon.display;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.editor.IMarathonFileFilter;

public class MarathonFileFilter extends FileFilter implements IMarathonFileFilter {
    private String suffix;
    private final IScriptModelClientPart scriptModel;

    public MarathonFileFilter(String sourceFileSuffix, IScriptModelClientPart scriptModel) {
        suffix = sourceFileSuffix;
        this.scriptModel = scriptModel;
    }

    public boolean accept(File f) {
        if (f.isDirectory() && !f.getName().startsWith("."))
            return true;
        return !f.isDirectory() && scriptModel.isSourceFile(f);
    }

    public String getDescription() {
        return "Marathon Source Files";
    }

    public FileFilter getChooserFilter() {
        return this;
    }

    public String getSuffix() {
        return suffix;
    }
}