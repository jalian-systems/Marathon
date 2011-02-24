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

/**
 * Receive feedback about certain key events which occur while a script is
 * playing
 */
public interface IPlaybackListener {
    public static final int PAUSE = 1;
    public static final int CONTINUE = 2;

    /**
     * Called after the last statement in this script has been executed
     * 
     * @param result
     *            - the result object containing all playback events generated
     *            by this script
     * @param shutdown
     *            TODO
     */
    void playbackFinished(PlaybackResult result, boolean shutdown);

    /**
     * Called when a breakpoint has been reached, and execution has stopped just
     * before <code>line</code>
     * 
     * @param line
     */
    int lineReached(SourceLine line);

    /**
     * Called when a function returns in the script code
     * 
     * @param line
     */
    int methodReturned(SourceLine line);

    /**
     * Called when a function is called in the script code
     * 
     * @param line
     */
    int methodCalled(SourceLine line);

    int acceptChecklist(String fileName);

    int showChecklist(String filename);
}
