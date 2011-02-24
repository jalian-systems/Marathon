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

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.api.InterruptionError;

public class DisplayEventQueue extends EventQueue {
    private IExceptionReporter reporter;
    private boolean attached = false;

    // FIXME: Do we really use this exception reporter? Confused.
    // Why should we have to hook up EventQueue for Marathon GUI?
    public DisplayEventQueue(IExceptionReporter reporter) {
        this.reporter = reporter;
    }

    public void attach() {
        if (attached)
            throw new UnsupportedOperationException("this event queue is already attached");
        sync();
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(this);
        attached = true;
    }

    protected void dispatchEvent(AWTEvent event) {
        try {
            super.dispatchEvent(event);
        } catch (Throwable t) {
            reporter.reportException(t);
        }
    }

    public void detach() {
        sync();
        this.pop();
    }

    public static void sync() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch (InterruptedException e) {
            throw new InterruptionError();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
