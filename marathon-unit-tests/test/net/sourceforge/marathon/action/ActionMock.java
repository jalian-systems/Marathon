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

import junit.framework.Assert;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class ActionMock extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private boolean played = false;
    private String name;

    public ActionMock(String name) {
        super(new ComponentId(name), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        this.name = name;
    }

    public void play(ComponentFinder facade) {
        if (played) {
            Assert.fail("already played");
        }
        played = true;
    }

    public boolean hasPlayed() {
        return played;
    }

    public String toScriptCode() {
        return "mock('" + name + "')\n";
    }
}
