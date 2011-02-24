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

import net.sourceforge.marathon.action.AbstractScriptElement;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.PythonEscape;

public class MockRecordedClick extends AbstractScriptElement {
    private static final long serialVersionUID = 1L;
    private int clicks;

    public MockRecordedClick(ComponentId componentId, WindowId windowId, int clicks) {
        super(componentId, windowId);
        this.clicks = clicks;
    }

    public String toScriptCode() {
        if (clicks > 1) {
            return Indent.getIndent() + "doubleclick(" + PythonEscape.encode(getComponentId().getName()) + finish();
        } else {
            String shiz = Indent.getIndent() + "click(" + PythonEscape.encode(getComponentId().getName()) + finish();
            return shiz;
        }
    }

    protected String finish() {
        if (getComponentId().getComponentInfo() != null) {
            return ", " + PythonEscape.encode(getComponentId().getComponentInfo()) + ")\n";
        } else {
            return ")\n";
        }
    }

    public int getClickCount() {
        return clicks;
    }

    public boolean canOverride(IScriptElement other) {
        if (other instanceof MockRecordedClick) {
            MockRecordedClick click = (MockRecordedClick) other;
            return click.clicks < clicks;
        }
        return false;
    }

}
