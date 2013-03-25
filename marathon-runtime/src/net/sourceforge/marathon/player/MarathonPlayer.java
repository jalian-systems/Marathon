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
package net.sourceforge.marathon.player;

import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.InterruptionError;
import net.sourceforge.marathon.api.MarathonRuntimeException;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.rmilite.RemoteInvocationException;

/**
 * This handle the play back threading issue and immediated playback stop
 * (interrupt) handling
 */
public final class MarathonPlayer implements IPlayer, Runnable, IPlaybackListener {
    private IPlaybackListener listener;
    private Thread playbackThread;
    private boolean paused = true;
    private IScript script;
    private PlaybackResult result;
    private boolean shouldRunFixture;
    private boolean isPlaybackFinishedCalled = false;
    public static boolean exitIsNotAnError = false;
    private boolean acceptChecklist;

    public MarathonPlayer(IScript script, IPlaybackListener listener, PlaybackResult result) {
        setExitNotAnError();
        this.listener = listener;
        this.script = script;
        this.result = result;
        script.attachPlaybackListener(this);
        playbackThread = new Thread(this, "Marathon Playback Thread");
        synchronized (this) {
            playbackThread.start();
            InterruptionError.wait(this);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (!isPlaybackFinishedCalled) {
                    if (!exitIsNotAnError)
                        MarathonPlayer.this.result.addFailure(
                                "Application under test aborted - use set_no_fail_on_exit to suppress this error",
                                new SourceLine[] { new SourceLine("internal", "System.exit", 1) });
                    MarathonPlayer.this.playbackFinished(MarathonPlayer.this.result, true);
                }
            }
        });
    }

    private static void setExitNotAnError() {
        exitIsNotAnError = false ;
    }
    
    public void halt() {
        try {
            playbackThread.interrupt();
        } catch (RemoteInvocationException e) {
            throw new MarathonRuntimeException();
        }
    }

    public synchronized void play(boolean shouldRunFixture) {
        paused = false;
        this.shouldRunFixture = shouldRunFixture;
        notify();
    }

    public void run() {
        synchronized (this) {
            notify();
            InterruptionError.wait(this);
        }
        try {
            script.playbackBody(shouldRunFixture, playbackThread).run();
        } catch(Throwable t) {
            result.addFailure(t.getMessage(), new SourceLine[0]);
        } finally {
            playbackFinished(result, false);
        }
    }

    public void playbackFinished(PlaybackResult result, boolean shutdown) {
        if (!isPlaybackFinishedCalled) {
            listener.playbackFinished(result, shutdown);
            isPlaybackFinishedCalled = true;
        }
    }

    public synchronized int lineReached(SourceLine line) {
        while (paused) {
            InterruptionError.wait(this);
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptionError();
        }
        return listener.lineReached(line);
    }

    public int methodReturned(SourceLine line) {
        while (paused) {
            InterruptionError.wait(this);
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptionError();
        }
        return listener.methodReturned(line);
    }

    public int methodCalled(SourceLine line) {
        while (paused) {
            InterruptionError.wait(this);
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptionError();
        }
        return listener.methodCalled(line);
    }

    public int acceptChecklist(String fileName) {
        if (acceptChecklist) {
            listener.acceptChecklist(fileName);
            return PAUSE;
        }
        return CONTINUE;
    }

    public int showChecklist(String fileName) {
        return listener.showChecklist(fileName);
    }

    public void setAcceptCheckList(boolean b) {
        acceptChecklist = b;
    }
}
