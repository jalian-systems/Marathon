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

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.util.Indent;

public class WindowClosingAction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final WindowId id;
    private String scriptCodeForWindowClosing;

    public WindowClosingAction(WindowId id, IScriptModelServerPart scriptModel) {
        this.id = id;
        scriptCodeForWindowClosing = scriptModel.getScriptCodeForWindowClosing(id);
    }

    public void play(ComponentFinder resolver) {
        Window win = resolver.getWindow();
        EventQueue eventQueue = win.getToolkit().getSystemEventQueue();
        AWTSync.sync();
        eventQueue.postEvent(new WindowEvent(win, WindowEvent.WINDOW_CLOSING));
        AWTSync.sync();
    }

    public IScriptElement enscript() {
        return new IScriptElement() {
            private static final long serialVersionUID = 1L;

            public ComponentId getComponentId() {
                return null;
            }

            public WindowId getWindowId() {
                return id;
            }

            public String toScriptCode() {
                return Indent.getIndent() + scriptCodeForWindowClosing;
            }

            public boolean isUndo() {
                return false;
            }

            public IScriptElement getUndoElement() {
                return null;
            }

        };
    }

}
