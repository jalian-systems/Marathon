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
package net.sourceforge.marathon.display;

public class State {
    public final static State STOPPED_WITH_APP_CLOSED = new State("Stopped with application closed");
    public final static State STOPPED_WITH_APP_OPEN = new State("Stopped with application open");
    public final static State PLAYING = new State("Playing");
    public final static State RECORDING = new State("Recording");
    public static final State RECORDINGPAUSED = new State("Recording Paused");
    public static final State PLAYINGPAUSED = new State("Playing Paused");
    private String name;

    private State(String name) {
        this.name = name;
    }

    public boolean isStoppedWithAppClosed() {
        return this == STOPPED_WITH_APP_CLOSED;
    }

    public boolean isStoppedWithAppOpen() {
        return this == STOPPED_WITH_APP_OPEN;
    }

    public boolean isStopped() {
        return isStoppedWithAppClosed() || isStoppedWithAppOpen();
    }

    public boolean isPlaying() {
        return this == PLAYING;
    }

    public boolean isRecording() {
        return this == RECORDING;
    }

    public boolean isRecordingPaused() {
        return this == RECORDINGPAUSED;
    }

    public boolean isPlayingPaused() {
        return this == PLAYINGPAUSED;
    }

    public String toString() {
        return name;
    }
}
