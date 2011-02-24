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
package net.sourceforge.marathon.component;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JInternalFrame;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class ComponentFinder {
    public static int COMPONENT_SEARCH_RETRY_COUNT = Integer.parseInt(System.getProperty(
            Constants.PROP_COMPONENT_SEARCH_RETRY_COUNT, "600"));
    public static int RETRY_INTERVAL_MS = Integer.parseInt(System.getProperty(Constants.PROP_RETRY_INTERVAL_MS, "100"));
    private INamingStrategy namingStrategy;
    private Stack<Component> windows = new Stack<Component>();
    private List<ComponentResolver> resolvers = new ArrayList<ComponentResolver>();
    private boolean rawRecording = false;
    private MComponent lastComponent;
    private Object lastObject;
    private Point lastLocation;
    private final IScriptModelServerPart scriptModel;
    private boolean recording;
    private final WindowMonitor windowMonitor;

    private ComponentResolver findResolver(Component component, Point location) {
        for (Iterator<ComponentResolver> iter = resolvers.iterator(); iter.hasNext();) {
            ComponentResolver element = (ComponentResolver) iter.next();
            if (element.canHandle(component, location))
                return element;
        }
        // Should not reach here
        return null;
    }

    public ComponentFinder(boolean isRecording, INamingStrategy namingStrategy, ResolversProvider resolversProvider,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this.recording = isRecording;
        this.scriptModel = scriptModel;
        this.windowMonitor = windowMonitor;
        resolversProvider.setFinder(this);
        resolversProvider.setRecording(isRecording);
        resolversProvider.setWindowMonitor(windowMonitor);
        resolvers = resolversProvider.get();
        this.namingStrategy = namingStrategy;
    }

    public MComponent getMComponentById(final ComponentId id) throws ComponentNotFoundException {
        return getMComponentById(id, COMPONENT_SEARCH_RETRY_COUNT);
    }

    public MComponent getMContainerById(final ComponentId id) throws ComponentNotFoundException {
        return getComponentByName(id.getName(), id.getComponentInfo(), COMPONENT_SEARCH_RETRY_COUNT, true);
    }

    public MComponent getMComponentById(final ComponentId id, int retryCount) throws ComponentNotFoundException {
        return getComponentByName(id.getName(), id.getComponentInfo(), retryCount, false);
    }

    private MComponent getComponentByName(String name, String info, int retryCount, boolean isContainer) {
        if (getWindowInternal() == null) {
            throw new RuntimeException("you must specify a toplevel window before asking for component");
        }
        MComponent.invokeAndWait(new Runnable() {
            public void run() {
                namingStrategy.setTopLevelComponent(getWindowInternal());
            }
        });
        try {
            Component c = namingStrategy.getComponent(name, retryCount, isContainer);
            return getMComponentByComponent(c, name, info);
        } catch (Exception e) {
            ComponentNotFoundException err = new ComponentNotFoundException("", scriptModel, windowMonitor);
            err.setMessage((e.getMessage() == null ? "" : e.getMessage() + "\n") + "Couldn't find component " + name + " in: "
                    + namingStrategy.getName(getWindowInternal()) + "\n" + namingStrategy.getVisibleComponentNames());
            err.captureScreen();
            throw err;
        }
    }

    MComponent getComponent_test(String name) throws ComponentNotFoundException {
        return getComponent_test(name, null);
    }

    MComponent getComponent_test(String name, String info) throws ComponentNotFoundException {
        return getMComponentById(new ComponentId(name, info));
    }

    public MComponent getMComponentByComponent(Component obj) {
        return getMComponentByComponent(obj, null);
    }

    public MComponent getMComponentByComponent(Component object, Point location) {
        MComponent mComponent = findMComponentByComponent(object, location);
        if (mComponent != null && mComponent instanceof MNullComponent)
            namingStrategy.markUnused(object);
        return mComponent;
    }

    private MComponent findMComponentByComponent(Component object, Point location) {
        MComponent mComponent;
        if (rawRecording) {
            Component window = getTopLevelWindow(object);
            if (window == null) {
                object = getRealComponent((Component) object, location);
                window = getTopLevelWindow(object);
            }
            if (window == null) {
                return null;
            }
            namingStrategy.setTopLevelComponent(window);
            String name = namingStrategy.getName(object);
            if (name == null) {
                return null;
            }
            mComponent = new MUnknownComponent(object, name, this, windowMonitor);
            mComponent.setWindowId(WindowIdCreator.createWindowId(window, windowMonitor));
            return mComponent;
        }
        if (lastObject == object && lastLocation == location && (lastComponent != null && !lastComponent.effectsWindowName())) {
            return lastComponent;
        }
        Component component = getRealComponent((Component) object, location);
        if (component == null)
            return null;
        Component window = getTopLevelWindow(component);
        if (window == null) {
            return null;
        }
        namingStrategy.setTopLevelComponent(window);
        String name = namingStrategy.getName(component);
        if (name == null) {
            return null;
        }
        if (rawRecording)
            mComponent = new MUnknownComponent(component, name, this, windowMonitor);
        else
            mComponent = getMComponentByComponent(component, name, location);
        if (mComponent != null)
            mComponent.setWindowId(WindowIdCreator.createWindowId(window, windowMonitor));
        if (!(mComponent instanceof MCellComponent) && !(mComponent instanceof MCollectionComponent)) {
            lastComponent = mComponent;
            lastObject = object;
            lastLocation = location;
        } else {
            lastComponent = null;
            lastObject = null;
            lastLocation = null;
        }
        return mComponent;
    }

    protected Component getRealComponent(Component component, Point location) {
        if (isRecordingArtifact(component) || component instanceof Window) {
            return null;
        }
        ComponentResolver resolver = findResolver(component, location);
        return resolver.getComponent(component, location);
    }

    private boolean isRecordingArtifact(Component component) {
        while (component != null) {
            if (component instanceof IRecordingArtifact) {
                return true;
            }
            component = component.getParent();
        }
        return false;
    }

    public Component getTopLevelWindow(Component component) {
        while (component != null && !(component instanceof Window) && !(component instanceof JInternalFrame)) {
            component = component.getParent();
        }
        if (component instanceof JInternalFrame)
            return component;
        if (component != null && windowMonitor.shouldIgnore((Window) component))
            return getTopLevelWindow(component.getParent());
        return component;
    }

    public MComponent getMComponentByComponent(Component component, String name, Object obj) {
        ComponentResolver resolver = findResolver(component, null);
        MComponent mcomponent = resolver.getMComponent(component, name, obj);
        return mcomponent;
    }

    public Window getWindow() {
        if (windows.isEmpty())
            return null;
        Component c = (Component) windows.peek();
        if (c instanceof Window)
            return (Window) c;
        return (Window) getTopLevelWindow(c.getParent());
    }

    public Component getWindowInternal() {
        return (Component) (windows.isEmpty() ? null : windows.peek());
    }

    public void push(Component component) {
        windows.push(component);
    }

    public void pop() {
        windows.pop();
    }

    public void setRecording(boolean isRecording) {
        for (Iterator<ComponentResolver> iter = resolvers.iterator(); iter.hasNext();) {
            ComponentResolver element = iter.next();
            element.setRecording(isRecording);
        }
        this.recording = isRecording;
    }

    public boolean isRecording() {
        return recording;
    }

    public Map<String, Component> getAllComponents() {
        if (getWindowInternal() == null) {
            throw new RuntimeException("you must specify a toplevel window before asking for component");
        }
        namingStrategy.setTopLevelComponent(getWindowInternal());
        return namingStrategy.getAllComponents();
    }

    public void setRawRecording(boolean isRaw) {
        rawRecording = isRaw;
    }

    public boolean isRawRecording() {
        return rawRecording;
    }

    public IScriptModelServerPart getScriptModel() {
        return scriptModel;
    }

    public static void setRetryCount(int c) {
        COMPONENT_SEARCH_RETRY_COUNT = c;
    }

    public static int getRetryCount() {
        if ("true".equals(System.getProperty("marathon.unittests")))
            return 1;
        return COMPONENT_SEARCH_RETRY_COUNT;
    }

    public static int getRetryInterval() {
        return RETRY_INTERVAL_MS;
    }

}
