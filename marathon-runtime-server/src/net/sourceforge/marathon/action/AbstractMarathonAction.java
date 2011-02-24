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
package net.sourceforge.marathon.action;

import java.awt.Component;
import java.awt.Window;
import java.io.Serializable;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.EventQueueRunner;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.Retry;

public abstract class AbstractMarathonAction implements Serializable {
    private static final long serialVersionUID = 5806394300225618812L;
    private ComponentId componentId;
    protected int delayInMS = 0;
    protected final IScriptModelServerPart scriptModel;
    protected final WindowMonitor windowMonitor;

    public AbstractMarathonAction(ComponentId componentId, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this.componentId = componentId;
        this.scriptModel = scriptModel;
        this.windowMonitor = windowMonitor;
    }

    public void setDelay(int delayInMS) {
        this.delayInMS = delayInMS;
    }

    public abstract void play(ComponentFinder resolver);

    public void play(ComponentFinder resolver, int retryCount) {
        resolver.getMComponentById(getComponentId(), retryCount);
        play(resolver);
    }

    public ComponentId getComponentId() {
        return componentId;
    }

    public final String toString() {
        return toScriptCode();
    }

    public IScriptElement enscript(MComponent component) {
        return enscript(component.getWindowId());
    }

    public IScriptElement enscript(WindowId windowId) {
        final String script = AbstractMarathonAction.this.toScriptCode();
        return new DefaultRecordable(script, componentId, windowId);
    }

    public abstract String toScriptCode();

    /**
     * we are equal if our string representation is the same
     */
    public boolean equals(Object that) {
        if (this == that)
            return true;
        return that != null && this.getClass().equals(that.getClass()) && this.toString().equals(that.toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public static boolean objectEquals(Object a, Object b) {
        return a == b || (a != null && a.equals(b)) || (b != null && b.equals(a));
    }

    public void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }

    public void assertEquals(String message, Object expected, Object actual) {
        if (!objectEquals(expected, actual)) {
            AssertionFailedError e = new AssertionFailedError(message, expected, actual, scriptModel, windowMonitor);
            e.captureScreen();
            throw e;
        }
    }

    public void assertTrue(String message, boolean correct) {
        if (!correct) {
            throw new TestException(message, scriptModel, windowMonitor);
        }
    }

    public void playProtected(ComponentFinder resolver) {
        play(resolver);
    }

    private class RetryWindowActive extends Retry.Attempt {
        private Window window;

        RetryWindowActive(Window window) {
            this.window = window;
        }

        public void perform() {
            if (window.getFocusableWindowState() && !window.isFocusableWindow()) {
                retry();
            }
        }
    }

    private static class DefaultRecordable extends AbstractScriptElement {
        private static final long serialVersionUID = 1L;
        private String script;

        public DefaultRecordable(String script, ComponentId componentId, WindowId windowId) {
            super(componentId, windowId);
            this.script = script;
        }

        public String toScriptCode() {
            return Indent.getIndent() + script;
            // return Indent.getIndent() + script.replaceAll("\t", "");
        }

    }

    protected Window getParentWindow(Component component) {
        while (component != null && !(component instanceof Window && !windowMonitor.shouldIgnore((Window) component)))
            component = component.getParent();
        return (Window) component;
    }

    protected void waitForWindowActive(Window window) {
        if (window == null)
            return;
        try {
            new Retry(new ComponentNotFoundException("The window containing the component is not active", scriptModel,
                    windowMonitor), 1000, 60, new RetryWindowActive(window));
        } catch (TestException e) {
            e.captureScreen();
            throw e;
        }
        new EventQueueRunner().invoke(window, "toFront");
    }
}
