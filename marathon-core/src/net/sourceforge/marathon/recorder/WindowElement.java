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

import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.ObjectComparator;

public class WindowElement extends CompositeScriptElement {
    private static final long serialVersionUID = 1L;
    private WindowId windowId;

    public WindowElement(WindowId windowId) {
        this.windowId = windowId;
    }

    public WindowId getWindowId() {
        return windowId;
    }

    public String getTitle() {
        return windowId.getTitle();
    }

    public boolean owns(CompositeScriptElement child) {
        if (!(child instanceof WindowElement) || getTitle().equals(((WindowElement) child).windowId.getParentTitle()))
            return true;
        return false;
    }

    public String toScriptCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Indent.getIndent() + ScriptModelClientPart.getModel().getScriptCodeForWindow(windowId));
        Indent.incIndent();
        buffer.append(super.toScriptCode());
        Indent.decIndent();
        buffer.append(Indent.getIndent() + ScriptModelClientPart.getModel().getScriptCodeForWindowClose(windowId));
        return buffer.toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof WindowElement) {
            WindowElement that = (WindowElement) obj;
            new ObjectComparator();
            return ObjectComparator.compare(that.windowId, this.windowId) == 0;
        }
        return false;
    }

    public int hashCode() {
        return windowId.hashCode();
    }

    public boolean isUndo() {
        return false;
    }

    public IScriptElement getUndoElement() {
        return null;
    }
}
