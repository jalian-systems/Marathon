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
package net.sourceforge.marathon.display.readline;

import net.sourceforge.marathon.display.readline.Join.Call;

public abstract class SyncReaction extends Reaction {
    public SyncReaction(int[] indices) {
        super(indices.clone(), false);
    }

    public SyncReaction(Enum<?> head, Enum<?>... channels) {
        super(head, channels, false);
    }

    @Override
    void dispatch(Join join, final Object[] args) {
        final Call call = (Call) args[0];
        args[0] = call.getMessage();
        call.activate(join, this, args);
    }

    public abstract Object react(Join join, Object[] args);
}