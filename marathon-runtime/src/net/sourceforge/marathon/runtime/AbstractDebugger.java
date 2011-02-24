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

import net.sourceforge.marathon.api.IDebugger;

public abstract class AbstractDebugger implements IDebugger {
    private String commandToExecute;
    private String returnValue = null;
    private Object commandLock = new Object();

    public void pause() {
        while (true) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (commandToExecute == null)
                    break;
                try {
                    returnValue = run(commandToExecute);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                synchronized (commandLock) {
                    commandLock.notifyAll();
                }
                commandToExecute = null;
            }
        }
    }

    public void resume() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    public String evaluateScriptWhenPaused(String script) {
        commandToExecute = script;
        returnValue = "";
        synchronized (commandLock) {
            resume();
            try {
                commandLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }
}
