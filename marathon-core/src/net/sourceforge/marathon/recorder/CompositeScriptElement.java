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

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptElement;

public abstract class CompositeScriptElement implements IScriptElement {
    private static final long serialVersionUID = 5400907759785664633L;
    private RecordableList list = new RecordableList();

    public void add(IScriptElement tag) {
        list.add(tag);
    }

    public void addFirst(IScriptElement tag) {
        list.addFirst(tag);
    }

    public String toScriptCode() {
        return list.toScriptCode();
    }

    public RecordableList getChildren() {
        return list;
    }

    /**
     * this is different than for action - action just compares text - we don't
     * want to compare our children's text, so unless this is the same instance,
     * containers are not the same objects
     */
    public boolean equals(Object that) {
        return this == that;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public abstract boolean owns(CompositeScriptElement child);

    public ComponentId getComponentId() {
        return null;
    }

    public boolean canOverride(IScriptElement other) {
        return false;
    }
}
