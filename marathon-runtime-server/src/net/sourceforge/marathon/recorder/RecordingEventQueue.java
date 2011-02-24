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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.util.ContextMenuTriggers;
import net.sourceforge.marathon.util.OSUtils;

public class RecordingEventQueue implements AWTEventListener {
    private static ContextMenuWindow contextMenu;
    private AWTEvent currentMouseEvent;
    private final IRecorder recorder;
    private final ComponentFinder finder;
    private final IMarathonRuntime runtime;
    private final IScriptModelServerPart scriptModel;
    private final WindowMonitor windowMonitor;

    public void attach() {
        AWTSync.sync();
        Toolkit.getDefaultToolkit().addAWTEventListener(this,
                AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    public void detach() {
        AWTSync.sync();
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }

    public RecordingEventQueue(IRecorder recorder, ComponentFinder finder, IMarathonRuntime runtime,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this.recorder = recorder;
        this.finder = finder;
        this.runtime = runtime;
        this.scriptModel = scriptModel;
        this.windowMonitor = windowMonitor;
    }

    private void showPopup(MouseEvent mouseEvent) {
        if (isContextMenuOn())
            return;
        Component component = SwingUtilities.getDeepestComponentAt(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        if (component == null)
            return;
        if (component instanceof JMenuItem && (!(component instanceof JMenu) || ((JMenu) component).isSelected()))
            return;
        Point point = SwingUtilities.convertPoint(mouseEvent.getComponent(), mouseEvent.getPoint(), component);
        showPopup(component, point);
    }

    public static boolean isContextMenuOn() {
        return contextMenu != null && contextMenu.isShowing();
    }

    public ContextMenuWindow showPopup(Component component, Point point) {
        Component root = SwingUtilities.getRoot(component);
        if (root instanceof Window)
            contextMenu = new ContextMenuWindow((Window) root, recorder, finder, runtime, scriptModel, windowMonitor);
        else
            throw new RuntimeException("Unknown root for component");
        contextMenu.setComponent(component, point, true);
        if (component instanceof JMenu)
            contextMenu.show(((JMenu) component).getParent(), point.x, point.y);
        else
            contextMenu.show(component, point.x, point.y);
        RecordingEventListener.getInstance().focusLost(null);
        return contextMenu;
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) event;
            event = OSUtils.convert((MouseEvent) event);
            currentMouseEvent = event;
            if (event instanceof MouseEvent && !(event.getSource() instanceof IRecordingArtifact)) {
                MouseEvent mouseEvent = (MouseEvent) event;
                if (ContextMenuTriggers.isContextMenuSequence(mouseEvent)) {
                    e.consume();
                    showPopup(mouseEvent);
                    return;
                }
            }
        } else if (event instanceof KeyEvent) {
            if (currentMouseEvent == null) {
                /*
                 * The mouse is not moved since we started - create a dummy
                 * mouse event with the current focused component.
                 */
                Window windowWithFocus = WindowMonitor.getTopLevelWindowWithFocus();
                Component focusOwner = windowWithFocus.getFocusOwner();
                if (focusOwner == null)
                    return;
                currentMouseEvent = new MouseEvent(focusOwner, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 0, 0, 1,
                        false);
            }
            if (ContextMenuTriggers.isContextMenuKeySequence((KeyEvent) event)) {
                ((KeyEvent) event).consume();
                if (!(event.getSource() instanceof IRecordingArtifact)) {
                    showPopup((MouseEvent) currentMouseEvent);
                }
            }
        }
    }
}
