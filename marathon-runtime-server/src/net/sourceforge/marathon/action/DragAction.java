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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class DragAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private int modifiers = 0;
    private Point start, end;

    public DragAction(ComponentId id, Point start, Point end, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(id, scriptModel, windowMonitor);
        this.modifiers = MouseEvent.BUTTON1_DOWN_MASK;
        this.start = start;
        this.end = end;
    }

    public DragAction(ComponentId id, MouseEvent e, Point start, Point end, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(id, scriptModel, windowMonitor);
        this.modifiers = e.getModifiersEx();
        this.start = start;
        this.end = end;
    }

    public DragAction(ComponentId id, int modifiers, Point start, Point end, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(id, scriptModel, windowMonitor);
        this.modifiers = modifiers;
        this.start = start;
        this.end = end;
    }

    private void requestFocus(Component c) {
        if (c != null)
            c.requestFocus();
    }

    public void play(ComponentFinder resolver) {
        MComponent component = resolver.getMComponentById(getComponentId());
        waitForWindowActive(getParentWindow(component.getComponent()));
        requestFocus(component.getComponent());
        component.drag(modifiers, start, end, delayInMS);
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForDrag(modifiers, start, end, getComponentId());
    }
}
