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
import java.util.logging.Logger;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.util.Indent;

public class WindowClosingAction implements Serializable {
    private static final long serialVersionUID = 1L;
    private WindowId id;
    private String scriptCodeForWindowClosing;
    private Window window;

    private final static Logger logger = Logger.getLogger(WindowClosingAction.class.getName());
    
    public WindowClosingAction(WindowId id, IScriptModelServerPart scriptModel) {
        // While recording
        this.id = id;
        scriptCodeForWindowClosing = scriptModel.getScriptCodeForWindowClosing(id);
    }

    public WindowClosingAction(Window window) {
        // While playing
        this.window = window ;
    }

    public void play(ComponentFinder resolver) {
        logger.info("Trying to close the window...: isShowing = " + window.isShowing() + " isVisible: " + window.isVisible() + " isValid: " + window.isValid());
        if (!window.isValid() || !window.isShowing() || !window.isVisible())
            return;
        EventQueue eventQueue = window.getToolkit().getSystemEventQueue();
        eventQueue.postEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
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
