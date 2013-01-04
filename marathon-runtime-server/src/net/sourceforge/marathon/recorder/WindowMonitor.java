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
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.DelegatingNamingStrategy;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.WindowIdCreator;
import net.sourceforge.marathon.event.IPredicate;

public class WindowMonitor implements AWTEventListener {
    public static final String IGNORED_COMPONENT_NAME = "Ignore Me";
    private static WindowMonitor instance = null;
    private List<ITopLevelWindowListener> listeners = Collections.synchronizedList(new ArrayList<ITopLevelWindowListener>());
    private WindowEventList windowEventList;
    private List<Window> windows = new ArrayList<Window>();
    List<Window> hiddenWindows = new ArrayList<Window>();
    private INamingStrategy<Component> namingStrategy;
    private static Window windowWithFocus;

    private static Logger logger = Logger.getLogger(WindowMonitor.class.getName());

    private WindowMonitor() {
        logger.info("Creating window monitor instance");
    }

    public synchronized static WindowMonitor getInstance() {
        if (instance == null) {
            instance = new WindowMonitor();
            instance.namingStrategy = new DelegatingNamingStrategy<Component>();
            instance.namingStrategy.init();
            Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
            instance.windowEventList = new WindowEventList(instance, instance.namingStrategy);
            Window[] windows = getOpenedWindows();
            for (Window w : windows) {
                instance.topLevelWindowCreated(w);
            }
        }
        return instance;
    }

    private static Window[] getOpenedWindows() {
        Field field = null;
        try {
            field = KeyboardFocusManager.class.getDeclaredField("focusedWindow");
            field.setAccessible(true);
            Window w = (Window) field.get(null);
            logger.log(Level.INFO, "OpenedWindows(KeyboardFocusManager): " + w);
            if (w != null)
                return new Window[] { w };
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (field != null)
                field.setAccessible(false);
        }
        return new Window[0];
    }

    public void addTopLevelWindowListener(ITopLevelWindowListener w) {
        listeners.add(w);
    }

    public void removeTopLevelWindowListener(ITopLevelWindowListener listener) {
        listeners.remove(listener);
    }

    public Window getWindow(String title) {
        for (Iterator<Window> i = getWindows().iterator(); i.hasNext();) {
            Window window = i.next();
            if (title.equals(namingStrategy.getName(window))) {
                return window;
            }
        }
        return null;
    }

    public Window getWindow(IPredicate test) {
        for (Iterator<Window> i = getWindows().iterator(); i.hasNext();) {
            Window window = i.next();
            if (test.evaluate(window)) {
                return window;
            }
        }
        return null;
    }

    public List<Window> getWindows() {
        List<Window> list;
        synchronized (this) {
            list = new ArrayList<Window>(windows);
        }
        for (ListIterator<Window> i = list.listIterator(); i.hasNext();) {
            if (!isFocusable(i.next())) {
                i.remove();
            }
        }
        return list;
    }

    /**
     * we should not be able to see windows that have a modal dialog showing
     */
    private boolean isFocusable(Window window) {
        if (!window.isShowing() || shouldIgnore(window))
            return false;
        return true;
    }

    public boolean shouldIgnore(Window window) {
        return window.getName().startsWith("###") || IGNORED_COMPONENT_NAME.equals(window.getName())
                || window instanceof IRecordingArtifact || (window.getOwner() != null && shouldIgnore(window.getOwner()));
    }

    public Window waitForWindowToOpen(long timeout, String title, IScriptModelServerPart scriptModel) {
        SameTitle predicate = new SameTitle(title, this);
        windowEventList.waitForWindowToOpen(timeout, predicate, scriptModel);
        return predicate.getWindow();
    }

    public void topLevelWindowCreated(Window w) {
        synchronized (this) {
            if (!windows.contains(w))
                windows.add(w);
        }
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).topLevelWindowCreated(w);
        }
        windowEventList.topLevelWindowCreated(w); // needs to be last to receive
                                                  // message
    }

    public void topLevelWindowDestroyed(Window w) {
        synchronized (this) {
            if (windows.contains(w))
                windows.remove(w);
            else
                return;
        }
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).topLevelWindowDestroyed(w);
        }
        windowEventList.topLevelWindowDestroyed(w); // needs to be last to
                                                    // receive message
    }

    private static class SameTitle implements IPredicate {
        private Window window;
        private final String title;
        private final WindowMonitor windowMonitor;

        public SameTitle(String title, WindowMonitor windowMonitor) {
            this.title = title;
            this.windowMonitor = windowMonitor;
        }

        public boolean evaluate(Object obj) {
            if (title.startsWith("/") && !title.startsWith("//")) {
                if (!Pattern.matches(title.substring(1), windowMonitor.namingStrategy.getName((Window) obj)))
                    return false;
            } else {
                String titleString = title;
                if (title.startsWith("//"))
                    titleString = title.substring(1);
                if (!titleString.equals(windowMonitor.namingStrategy.getName((Window) obj)))
                    return false;
            }
            window = (Window) obj;
            return true;
        }

        public String toString() {
            return title;
        }

        public Window getWindow() {
            return window;
        }
    }

    private void componentHidden(ComponentEvent e) {
        if (e.getSource() instanceof Window) {
            instance.topLevelWindowDestroyed((Window) e.getSource());
            hiddenWindows.add((Window) e.getSource());
        }
    }

    private void componentShown(ComponentEvent e) {
        if (e.getSource() instanceof Window) {
            if (!windows.contains(e.getSource())) {
                instance.topLevelWindowCreated((Window) e.getSource());
                hiddenWindows.remove(e.getSource());
            }
        }
    }

    public List<Window> getAllWindows() {
        List<Window> allWindows = new ArrayList<Window>();
        allWindows.addAll(windows);
        for (Iterator<Window> iter = hiddenWindows.iterator(); iter.hasNext();) {
            Window window = iter.next();
            if (!allWindows.contains(window))
                allWindows.add(window);
        }
        return allWindows;
    }

    public WindowId getWindowId() {
        if (windows.size() == 0)
            return null;
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (!shouldIgnore(window))
                return WindowIdCreator.createWindowId(window, this);
        }
        return null;
    }

    public INamingStrategy<Component> getNamingStrategy() {
        return namingStrategy;
    }

    public static Window getTopLevelWindowWithFocus() {
        return windowWithFocus;
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof WindowEvent) {
            WindowEvent windowEvent = (WindowEvent) event;
            if (windowEvent.getID() == WindowEvent.WINDOW_OPENED) {
                topLevelWindowCreated(windowEvent.getWindow());
                setWindowWithFocus(windowEvent.getWindow());
            }
            else if (windowEvent.getID() == WindowEvent.WINDOW_CLOSED)
                topLevelWindowDestroyed(windowEvent.getWindow());
            else if (windowEvent.getID() == WindowEvent.WINDOW_GAINED_FOCUS)
                setWindowWithFocus(windowEvent.getWindow());
        } else if (event instanceof ComponentEvent) {
            ComponentEvent componentEvent = (ComponentEvent) event;
            if (componentEvent.getID() == ComponentEvent.COMPONENT_HIDDEN) {
                componentHidden(componentEvent);
            } else if (componentEvent.getID() == ComponentEvent.COMPONENT_SHOWN) {
                componentShown(componentEvent);
            }
        }
    }

    private static void setWindowWithFocus(Window window) {
        windowWithFocus = window;
    }
}
