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

public abstract class Reaction {
    final int[] indices;
    final long mask;
    final long asyncMask;

    private static int[] toIndices(Enum<?> head, Enum<?>[] channels) {
        final int[] indices = new int[channels.length + 1];
        indices[0] = head.ordinal();
        for (int i = 0; i < channels.length; ++i) {
            indices[i + 1] = channels[i].ordinal();
        }
        return indices;
    }

    Reaction(Enum<?> head, Enum<?>[] channels, boolean isAsync) {
        this(toIndices(head, channels), isAsync);
    }

    Reaction(int[] indices, boolean isAsync) {
        long mask = 0;
        for (int i = 0; i < indices.length; ++i) {
            final int index = indices[i];
            if (index < 0 || index > 63) {
                throw new IndexOutOfBoundsException();
            }
            if ((mask & (1L << index)) != 0) {
                throw new IllegalArgumentException("Duplicate channels in reaction");
            }
            mask |= 1L << index;
        }
        this.indices = indices;
        this.mask = mask;
        if (isAsync) {
            this.asyncMask = mask;
        } else {
            this.asyncMask = mask & ~(1L << indices[0]);
        }
    }

    abstract void dispatch(Join join, Object[] args);
}