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

import java.util.List;
import java.util.Properties;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentException;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MCollectionComponent;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class SelectAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private String text;
    private List<Properties> cellList;

    public SelectAction(ComponentId id, String text, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(id, scriptModel, windowMonitor);
        this.text = text;
    }

    public SelectAction(ComponentId id, List<Properties> list, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(id, scriptModel, windowMonitor);
        this.cellList = list ;
    }

    public void play(ComponentFinder resolver) {
        MComponent component = resolver.getMComponentById(getComponentId());
        waitForWindowActive(getParentWindow(component.getComponent()));
        if (text != null) {
            component.setText(text);
        }
        else {
            if (component instanceof MCollectionComponent) {
                ((MCollectionComponent)component).setCellSelection(cellList.toArray(new Properties[cellList.size()]));
            } else {
                throw new ComponentException("Array of properties are expected only for collection components", scriptModel, windowMonitor);
            }
        }
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForSelect(getComponentId(), text);
    }

    public String getText() {
        return text;
    }

    public List<Properties> getCellList() {
        return cellList;
    }
}
