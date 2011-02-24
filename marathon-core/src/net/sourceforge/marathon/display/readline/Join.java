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

import java.util.LinkedList;
import java.util.concurrent.Executor;

public final class Join {
    public static final Executor TRIVIAL_EXECUTOR = new Executor() {
        public void execute(Runnable command) {
            (new Thread(command)).start();
        }
    };

    final Executor executor;
    private final LinkedList<Object>[] writes;
    private final long asyncMask;
    private long mask = 0;
    private final Reaction[][] reactionsPerChannel;

    public Join(final long asyncMask, final Reaction[][] reactionsPerChannel, Executor executor) {
        @SuppressWarnings("unchecked")
        final LinkedList<Object>[] writes = new LinkedList[reactionsPerChannel.length];
        for (int i = 0; i < writes.length; ++i) {
            if (reactionsPerChannel[i] != null) {
                writes[i] = new LinkedList<Object>();
            }
        }
        this.asyncMask = asyncMask;
        this.reactionsPerChannel = reactionsPerChannel;
        this.writes = writes;
        this.executor = executor;
    }

    private void sendRaw(int index, Object message) {
        Reaction selectedReaction = null;
        Object[] args = null;
        synchronized (this) {
            final LinkedList<Object> writing = writes[index];
            if (writing == null) {
                throw new IndexOutOfBoundsException();
            }
            writing.addLast(message);
            mask |= 1L << index;
            final Reaction[] reactions = reactionsPerChannel[index];
            for (Reaction reaction : reactions) {
                if ((reaction.mask & mask) == reaction.mask) {
                    final int[] indices = reaction.indices;
                    args = new Object[indices.length];
                    for (int i = 0; i < indices.length; ++i) {
                        final int readIndex = indices[i];
                        final LinkedList<Object> reading = writes[readIndex];
                        args[i] = reading.removeFirst();
                        if (reading.isEmpty()) {
                            mask &= ~(1L << readIndex);
                        }
                    }
                    selectedReaction = reaction;
                    break;
                }
            }
        }
        if (selectedReaction != null) {
            selectedReaction.dispatch(this, args);
        }
    }

    public boolean isAsync(int channel) {
        return ((1L << channel) & asyncMask) != 0;
    }

    public void send(int channel, Object message) {
        if (isAsync(channel)) {
            sendRaw(channel, message);
        } else {
            sendRaw(channel, new AsyncCall(message));
        }
    }

    public void send(Enum<?> channel, Object message) {
        send(channel.ordinal(), message);
    }

    public Object call(int channel, Object message) {
        if (isAsync(channel)) {
            sendRaw(channel, message);
            return null;
        } else {
            SyncCall request = new SyncCall(message);
            sendRaw(channel, request);
            return request.call();
        }
    }

    public Object call(Enum<?> channel, Object message) {
        return call(channel.ordinal(), message);
    }

    static abstract class Call {
        private final Object message;

        public Call(Object message) {
            this.message = message;
        }

        public Object getMessage() {
            return message;
        }

        public abstract void activate(Join join, SyncReaction reaction, Object[] args);
    }

    private static class AsyncCall extends Call {
        public AsyncCall(Object message) {
            super(message);
        }

        public void activate(final Join join, final SyncReaction reaction, final Object[] args) {
            join.executor.execute(new Runnable() {
                public void run() {
                    reaction.react(join, args);
                }
            });
        }
    }

    private static class SyncCall extends Call {
        private Join join = null;
        private SyncReaction reaction = null;
        private Object[] args = null;

        public SyncCall(Object message) {
            super(message);
        }

        public synchronized void activate(Join join, SyncReaction reaction, Object[] args) {
            this.join = join;
            this.reaction = reaction;
            this.args = args;
            notifyAll();
        }

        public synchronized Object call() {
            boolean interrupted = false;
            try {
                while (reaction == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            return reaction.react(join, args);
        }
    }

    public void execute(Runnable runnable) {
        executor.equals(runnable);
    }
}
