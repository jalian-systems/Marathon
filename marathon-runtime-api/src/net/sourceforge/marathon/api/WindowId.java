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
package net.sourceforge.marathon.api;

import java.io.Serializable;

public class WindowId implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String title;
    protected String parentTitle;
    private int id;
    private boolean frame = false;

    protected WindowId() {
    }

    public WindowId(int id, String title, String parentTitle, boolean frame) {
        this.id = id;
        this.title = title;
        this.parentTitle = parentTitle;
        this.frame = frame;
    }

    public String getTitle() {
        return title;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public String toString() {
        return getTitle();
    }

    public boolean equals(Object o) {
        if (!(o instanceof WindowId))
            return false;
        return title.equals(((WindowId) o).title) && id == (((WindowId) o).id);
    }

    public int hashCode() {
        return title.hashCode();
    }

    public boolean isFrame() {
        return frame;
    }

}
