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
package net.sourceforge.marathon.recorder;

import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.component.ComponentFinder;

public abstract class AbstractContextMenu implements IContextMenu {

    private final IRecorder recorder;
    private final ComponentFinder finder;
    protected final ContextMenuWindow window;
    protected final IScriptModelServerPart scriptModel;
    protected final WindowMonitor windowMonitor;

    public AbstractContextMenu(ContextMenuWindow window, IRecorder recorder, ComponentFinder finder,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this.window = window;
        this.recorder = recorder;
        this.finder = finder;
        this.scriptModel = scriptModel;
        this.windowMonitor = windowMonitor;
    }

    public IRecorder getRecorder() {
        return recorder;
    }

    public ComponentFinder getFinder() {
        return finder;
    }

    public ContextMenuWindow getWindow() {
        return window;
    }
}
