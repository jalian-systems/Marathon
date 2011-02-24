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
package net.sourceforge.marathon.runtime;

import java.util.Properties;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.api.module.Module;

public class ScriptStub implements IScript {
    private boolean fail;
    public PlayerStub playerStub;

    public ScriptStub(boolean fail) {
        this.fail = fail;
    }

    public IPlayer getPlayer(final IPlaybackListener playbackListener, final PlaybackResult result) {
        if (fail) {
            result.addFailure("failed", new SourceLine[0]);
        }
        playerStub = new PlayerStub(playbackListener, result);
        return playerStub;
    }

    public void runFixtureSetup() {
    }

    public void runFixtureTeardown() {
    }

    public Module getModuleFuctions() {
        return null;
    }

    public String[][] getArgumentsFor(DefaultMutableTreeNode node) {
        return null;
    }

    public String getFunctionDocumentation(DefaultMutableTreeNode node) {
        return null;
    }

    public void exec(String function) {
    }

    public IDebugger getDebugger() {
        return null;
    }

    public void attachPlaybackListener(IPlaybackListener listener) {
    }

    public Runnable playbackBody(boolean shouldRunFixture, Thread playbackThread) {
        return null;
    }

    public String evaluate(String code) {
        return null;
    }

    public boolean isCustomAssertionsAvailable() {
        return false;
    }

    public String[][] getCustomAssertions(Object component) {
        return null;
    }

    public void setDataVariables(Properties dataVariables) {
        // TODO Auto-generated method stub

    }
}
