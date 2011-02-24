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

import java.awt.Point;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class DummyResolver extends ComponentFinder {
    private MComponent component;

    public DummyResolver(MComponent component) {
        super(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(), new ResolversProvider(), ScriptModelServerPart
                .getModelServerPart(), WindowMonitor.getInstance());
        this.component = component;
    }

    public MComponent getMComponentById(ComponentId id) throws ComponentNotFoundException {
        return component;
    }

    public MComponent getMComponentById(ComponentId id, int retryCount) throws ComponentNotFoundException {
        return component;
    }

    public MComponent getComponent_test(String name) throws ComponentNotFoundException {
        return component;
    }

    public MComponent getComponent(Object obj) {
        return component;
    }

    public MComponent getComponent(Object object, Point location) {
        return component;
    }
}
