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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.Snooze;

public class FireableMouseClickEvent extends FireableEvent {
    private int numberOfClicks;
    private boolean isPopupTrigger;

    public FireableMouseClickEvent(Component component, int numberOfClicks, boolean popupTrigger) {
        super(component);
        this.numberOfClicks = numberOfClicks;
        isPopupTrigger = popupTrigger;
    }

    public FireableMouseClickEvent(Component component) {
        this(component, 1, false);
    }

    public void fire() {
        fire(numberOfClicks);
    }

    public void fireExited() {
        AWTSync.sync();
        Dimension dimension = (Dimension) eventQueueRunner.invoke(getComponent(), "getSize");
        Point point = new Point(dimension.width / 2, dimension.height / 2);
        postEvent(createMouseEvent(MouseEvent.MOUSE_EXITED, 1, point.x, point.y, 0));
    }

    public void fire(int numberOfClicks) {
        Dimension dimension = (Dimension) eventQueueRunner.invoke(getComponent(), "getSize");
        Point point = new Point(dimension.width / 2, dimension.height / 2);
        fire(point, numberOfClicks);
    }

    public void fire(Point point, int numberOfClicks) {
        fire(point, numberOfClicks, 0);
    }

    public void fire(Point point, int numberOfClicks, int modifiers) {
        if (point == null) {
            Dimension dimension = (Dimension) eventQueueRunner.invoke(getComponent(), "getSize");
            point = new Point(dimension.width / 2, dimension.height / 2);
        }
        final int x = point.x;
        final int y = point.y;

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
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    scrollComponentIntoView(getComponent(), new Point(x, y));
                }
            });
        } catch (Exception e) {
        }
        postEvent(createMouseEvent(MouseEvent.MOUSE_ENTERED, 0, x, y, modifiers));
        postEvent(createMouseEvent(MouseEvent.MOUSE_MOVED, 0, x, y, modifiers));
        for (int i = 0; i < numberOfClicks; i++) {
            postEvent(createMouseEvent(MouseEvent.MOUSE_PRESSED, i + 1, x, y, modifiers));
            new Snooze(5);
            postEvent(createMouseEvent(MouseEvent.MOUSE_RELEASED, i + 1, x, y, modifiers));
            postEvent(createMouseEvent(MouseEvent.MOUSE_CLICKED, i + 1, x, y, modifiers));
            new Snooze(10);
        }
        postEvent(createMouseEvent(MouseEvent.MOUSE_MOVED, 0, x, y, modifiers));
        postEvent(createMouseEvent(MouseEvent.MOUSE_EXITED, 1, x, y, modifiers));
    }

    static void scrollComponentIntoView(Component component, Point point) {
        try {
            if (component instanceof JComponent) {
                JComponent jcomponent = (JComponent) component;
                scrollComponentIntoView(component.getParent(), component.getLocation());
                Rectangle visibleRect = jcomponent.getVisibleRect();
                if (!visibleRect.contains(point)) {
                    jcomponent.scrollRectToVisible(new Rectangle(point, visibleRect.getSize()));
                }
            }
        } catch (Exception e) {
            // Ignore - this is only beutification anyhow. - Unsimulatable bug
            // reported on Help forum
        }
    }

    private MouseEvent createMouseEvent(int id, int click, int x, int y, int modifiers) {
        if (isPopupTrigger)
            modifiers |= InputEvent.BUTTON3_DOWN_MASK;
        else
            modifiers |= InputEvent.BUTTON1_DOWN_MASK;
        if (id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_CLICKED || id == MouseEvent.MOUSE_RELEASED)
            return new MouseEvent(getComponent(), id, System.currentTimeMillis(), modifiers, x, y, click, isPopupTrigger,
                    isPopupTrigger ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        else
            return new MouseEvent(getComponent(), id, System.currentTimeMillis(), 0, x, y, 0, false);
    }
}
