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

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.MComponent;

public class UndoRecordable extends AbstractScriptElement {
    private IScriptElement scriptElement;

    private static final long serialVersionUID = 1L;

    public UndoRecordable(MComponent component, IScriptElement element) {
        super(component.getComponentId(), component.getWindowId());
        this.scriptElement = element;
    }

    public UndoRecordable(WindowId id, IScriptElement enscript) {
        super(new ComponentId("No Name"), id);
        this.scriptElement = enscript;
    }

    public String toScriptCode() {
        return null;
    }

    public IScriptElement getUndoElement() {
        return scriptElement;
    }

    public boolean isUndo() {
        return true;
    }
}
