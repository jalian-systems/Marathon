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

import java.awt.Rectangle;
import java.awt.Window;
import java.io.Serializable;

import javax.swing.JFrame;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.EventQueueRunner;

public class WindowStateAction extends AbstractMarathonAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final WindowState state;
    private final WindowId id;

    public WindowStateAction(WindowId id, WindowState state, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(new ComponentId("WindowStateAction"), scriptModel, windowMonitor);
        this.id = id;
        this.state = state;
    }

    public void play(ComponentFinder resolver) {
        Rectangle bounds = state.getBounds();
        if (bounds == null)
            return;
        EventQueueRunner eqRunner = new EventQueueRunner();
        Window window = windowMonitor.getWindow(id.getTitle());
        if (window instanceof JFrame) {
            eqRunner.invoke(window, "setExtendedState", new Object[] { JFrame.NORMAL}, new Class[] {Integer.class});
        }
        eqRunner.invoke(window, "setBounds", new Object[] { bounds }, new Class[] { Rectangle.class });
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForWindowState(id, state);
    }

    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that instanceof WindowStateAction && this.id.equals(((WindowStateAction) that).id))
            return true;
        return false;
    }
    
    @Override public int hashCode() {
        return id.hashCode();
    }
}
