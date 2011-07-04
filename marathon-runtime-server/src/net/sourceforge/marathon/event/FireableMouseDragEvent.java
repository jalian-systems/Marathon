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

package net.sourceforge.marathon.event;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.Snooze;

public class FireableMouseDragEvent extends FireableEvent {
    private Point start, end;

    public FireableMouseDragEvent(Component component, Point start, Point end) {
        super(component);
        this.start = start;
        this.end = end;
    }

    /**
     * @param subDelayInMS
     *            A debugging delay added between steps of sub-tasks such as
     *            begin drag, middle drag, end drag.
     */
    public void fire(int modifiers, int subDelayInMS) {
        eventQueueRunner.invoke(getComponent(), "requestFocusInWindow");
        AWTSync.sync();
        if (Boolean.valueOf(System.getProperty(Constants.PROP_APPLICATION_TOOLKIT_MENUMASK, "false")).booleanValue()) {
            if ((modifiers & InputEvent.CTRL_MASK) != 0) {
                modifiers = (modifiers & ~InputEvent.CTRL_MASK) | OSUtils.MENU_MASK;
            }
            if ((modifiers & InputEvent.META_MASK) != 0) {
                modifiers = (modifiers & ~InputEvent.META_MASK) | OSUtils.MENU_MASK;
            }
            if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
                modifiers = (modifiers & ~InputEvent.CTRL_DOWN_MASK) | OSUtils.MOUSE_MENU_MASK;
            }
            if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
                modifiers = (modifiers & ~InputEvent.META_DOWN_MASK) | OSUtils.MOUSE_MENU_MASK;
            }
        }
        FireableMouseClickEvent.scrollComponentIntoView(getComponent(), end);
        FireableMouseClickEvent.scrollComponentIntoView(getComponent(), start);

        // Assume that the start point is within the component.
        // But the end point might not be:
        postEvent(createMouseEvent(MouseEvent.MOUSE_ENTERED, 0, start.x, start.y, modifiers));
        boolean doneExit = false; // set to true when we have simulated the
                                  // mouse exit

        postEvent(createMouseEvent(MouseEvent.MOUSE_PRESSED, 1, start.x, start.y, modifiers));
        new Snooze(10);
        postEvent(createMouseEvent(MouseEvent.MOUSE_DRAGGED, 1, start.x, start.y, modifiers));

        if (subDelayInMS > 0) {
            new Snooze(subDelayInMS);
            AWTSync.sync();
        }

        new Snooze(10);
        int midX = (start.x + end.x) / 2, midY = (start.y + end.y) / 2;
        if (!doneExit && !componentContains(midX, midY)) {
            postEvent(createMouseEvent(MouseEvent.MOUSE_EXITED, 1, midX, midY, modifiers));
            doneExit = true;
        }
        postEvent(createMouseEvent(MouseEvent.MOUSE_DRAGGED, 1, midX, midY, modifiers));

        if (subDelayInMS > 0) {
            new Snooze(subDelayInMS);
            AWTSync.sync();
        }

        new Snooze(10);
        if (!doneExit && !componentContains(end.x, midY)) {
            postEvent(createMouseEvent(MouseEvent.MOUSE_EXITED, 1, end.x, end.y, modifiers));
            doneExit = true;
        }
        postEvent(createMouseEvent(MouseEvent.MOUSE_DRAGGED, 1, end.x, end.y, modifiers));

        if (subDelayInMS > 0) {
            new Snooze(subDelayInMS);
            AWTSync.sync();
        }

        new Snooze(10);
        postEvent(createMouseEvent(MouseEvent.MOUSE_RELEASED, 1, end.x, end.y, modifiers));

        // No, drags shouldn't generate clicks.
        // If a gesture has been determined a drag by Marathon and recorded as
        // such, it should
        // be beyond the tolerance for small drags within a click.
        // postEvent(createMouseEvent(MouseEvent.MOUSE_CLICKED, 1, end.x, end.y,
        // modifiers));

        if (!doneExit) {
            new Snooze(10);
            postEvent(createMouseEvent(MouseEvent.MOUSE_EXITED, 1, end.x, end.y, modifiers));
            doneExit = true;
        }
    }

    private boolean componentContains(int midX, int midY) {
        return eventQueueRunner.invokeBoolean(getComponent(), "contains", new Object[] { Integer.valueOf(midX), Integer.valueOf(midY) },
                new Class[] { Integer.TYPE, Integer.TYPE });
    }

    private MouseEvent createMouseEvent(int id, int click, int x, int y, int modifiers) {
        modifiers |= MouseEvent.BUTTON1_DOWN_MASK;
        if (id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_CLICKED || id == MouseEvent.MOUSE_RELEASED
                || id == MouseEvent.MOUSE_DRAGGED)
            return new MouseEvent(getComponent(), id, System.currentTimeMillis(), modifiers, x, y, click, false, MouseEvent.BUTTON1);
        else
            return new MouseEvent(getComponent(), id, System.currentTimeMillis(), 0, x, y, 0, false);
    }
}
