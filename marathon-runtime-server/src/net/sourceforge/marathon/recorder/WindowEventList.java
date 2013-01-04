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
package net.sourceforge.marathon.recorder;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.InterruptionError;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.event.IPredicate;

class WindowEventList {
    private boolean listening = false;
    private LinkedList<WindowMonitorEvent> list = new LinkedList<WindowMonitorEvent>();
    private WindowMonitor windowMonitor;
    private final INamingStrategy<Component> namingStrategy;

    public WindowEventList(WindowMonitor windowMonitor, INamingStrategy<Component> namingStrategy) {
        this.windowMonitor = windowMonitor;
        this.namingStrategy = namingStrategy;
    }

    public synchronized void topLevelWindowCreated(Window w) {
        if (ignore(w)) {
            return;
        }
        if (listening) {
            list.add(new WindowMonitorEvent(w, WindowMonitorEvent.OPENED, namingStrategy));
            notify();
        }
    }

    public synchronized void topLevelWindowDestroyed(Window w) {
        if (ignore(w)) {
            return;
        }
        if (listening) {
            list.add(new WindowMonitorEvent(w, WindowMonitorEvent.CLOSED, namingStrategy));
            notify();
        }
    }

    public void waitForWindowToOpen(long timeout, final IPredicate test, IScriptModelServerPart scriptModel) {
        waitForWindow(timeout, new IPredicate() {
            public boolean evaluate(Object obj) {
                return windowMonitor.getWindow(test) != null;
            }

            public String toString() {
                return "waiting for window(" + test + ") to open";
            }
        }, scriptModel);
    }

    private void waitForWindow(long timeout, IPredicate test, IScriptModelServerPart scriptModel) {
        // If you find in it the first shot, return
        if (test.evaluate(null)) {
            return;
        }
        // else check for timeout and keep doing it.
        long endTime = now() + timeout;
        list.clear();
        synchronized (this) {
            listening = true;
        }
        while (!test.evaluate(null)) {
            synchronized (this) {
                long remainingTime = endTime - now();
                if (remainingTime <= 0) {
                    WindowNotFoundException e = new WindowNotFoundException("timed out while " + test + "\nopen windows:"
                            + getWindowList(), scriptModel, windowMonitor);
                    e.captureScreen();
                    throw e;
                }
                try {
                    wait(Math.min(500, remainingTime));
                } catch (InterruptedException e) {
                    throw new InterruptionError();
                }
            }
        }
        synchronized (this) {
            list.clear();
            listening = false;
        }
    }

    private List<String> getWindowList() {
        List<String> list = new ArrayList<String>();
        for (Iterator<Window> i = windowMonitor.getWindows().iterator(); i.hasNext();) {
            list.add(namingStrategy.getName((Window) i.next()));
        }
        return list;
    }

    private boolean ignore(Window window) {
        return !(window instanceof Dialog);
    }

    private long now() {
        return System.currentTimeMillis();
    }

    static class WindowMonitorEvent {
        public static final boolean OPENED = true;
        public static final boolean CLOSED = false;
        private Window _window;
        private boolean _windowOpened;
        private final INamingStrategy<Component> namingStrategy;

        public WindowMonitorEvent(Window window, boolean windowOpened, INamingStrategy<Component> namingStrategy) {
            _window = window;
            _windowOpened = windowOpened;
            this.namingStrategy = namingStrategy;
        }

        public Window getWindow() {
            return _window;
        }

        public boolean windowOpened() {
            return OPENED == _windowOpened;
        }

        public boolean windowClosed() {
            return !windowOpened();
        }

        public String toString() {
            String className = _window.getClass().getName().substring(_window.getClass().getName().lastIndexOf('.') + 1);
            return "window (" + namingStrategy.getName(_window) + ":" + className + ") "
                    + (windowOpened() ? "opened" : "closed");
        }
    }
}
