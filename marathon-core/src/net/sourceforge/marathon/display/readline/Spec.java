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

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class Spec {
    private ArrayList<ArrayList<Reaction>> reactionsPerChannel = new ArrayList<ArrayList<Reaction>>();
    private long asyncMask = 0;
    private long mask = 0;
    private volatile Reaction[][] cachedReactionsPerChannel = null;

    public Spec() {
    }

    public void addReaction(Reaction reaction) {
        if ((mask & ~asyncMask & reaction.asyncMask) != 0) {
            throw new IllegalArgumentException("Cannot use a synchronous channel in a non-head position");
        }
        if ((reaction.mask & ~reaction.asyncMask & asyncMask) != 0) {
            throw new IllegalArgumentException("Cannot use an asynchronous channel in the head position of a synchronous reaction");
        }
        cachedReactionsPerChannel = null;
        final int[] indices = reaction.indices;
        for (int i = 0; i < indices.length; i++) {
            final int index = indices[i];
            if (reactionsPerChannel.size() <= index) {
                reactionsPerChannel.ensureCapacity(index + 1);
                while (reactionsPerChannel.size() <= index) {
                    reactionsPerChannel.add(null);
                }
            }
            ArrayList<Reaction> reactions = reactionsPerChannel.get(index);
            if (reactions == null) {
                reactions = new ArrayList<Reaction>();
                reactionsPerChannel.set(index, reactions);
            }
            reactions.add(reaction);
        }
        asyncMask |= reaction.asyncMask;
        mask |= reaction.mask;
    }

    public Join createJoin() {
        return createJoin(Join.TRIVIAL_EXECUTOR);
    }

    private static final Reaction[] EMPTY_REACTIONS = new Reaction[0];

    public Join createJoin(final Executor executor) {
        if (cachedReactionsPerChannel == null) {
            final int length = reactionsPerChannel.size();
            final Reaction[][] localReactionsPerChannel = new Reaction[length][];
            for (int i = 0; i < length; ++i) {
                final ArrayList<Reaction> reactions = reactionsPerChannel.get(i);
                if (reactions != null) {
                    localReactionsPerChannel[i] = reactions.toArray(EMPTY_REACTIONS);
                }
            }
            cachedReactionsPerChannel = localReactionsPerChannel;
        }
        return new Join(asyncMask, cachedReactionsPerChannel, executor);
    }
}