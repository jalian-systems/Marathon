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
import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;

public class ComponentFinder {
    public static int COMPONENT_SEARCH_RETRY_COUNT = Integer.parseInt(System.getProperty(
            Constants.PROP_COMPONENT_SEARCH_RETRY_COUNT, "600"));
    public static int RETRY_INTERVAL_MS = Integer.parseInt(System.getProperty(Constants.PROP_RETRY_INTERVAL_MS, "100"));
    private INamingStrategy<Component, Component> namingStrategy;
    private Stack<Component> windows = new Stack<Component>();
    private List<ComponentResolver> resolvers = new ArrayList<ComponentResolver>();
    private boolean rawRecording = false;
    private MComponent lastComponent;
    private Object lastObject;
    private Point lastLocation;
    private final IScriptModelServerPart scriptModel;
    private boolean recording;
    private final WindowMonitor windowMonitor;

    @SuppressWarnings("unused") private final static Logger logger = Logger.getLogger(ComponentFinder.class.getName());

    private ComponentResolver findResolver(Component component, Point location) {
        for (Iterator<ComponentResolver> iter = resolvers.iterator(); iter.hasNext();) {
            ComponentResolver element = (ComponentResolver) iter.next();
            if (element.canHandle(component, location))
                return element;
        }
        // Should not reach here
        return null;
    }

    public ComponentFinder(boolean isRecording, INamingStrategy<Component, Component> namingStrategy,
            ResolversProvider resolversProvider, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
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
        return getComponentByName(id, COMPONENT_SEARCH_RETRY_COUNT, true);
    }

    public MComponent getMComponentById(final ComponentId id, int retryCount) throws ComponentNotFoundException {
        return getComponentByName(id, retryCount, false);
    }

    private MComponent getComponentByName(ComponentId id, int retryCount, boolean isContainer) {
        if (getWindowInternal() == null) {
            throw new RuntimeException("you must specify a toplevel window before asking for component");
        }
        MComponent.invokeAndWait(new Runnable() {
            public void run() {
                namingStrategy.setTopLevelComponent(getWindowInternal(), true);
            }
        });
        try {
            Component c;
            String name = id.getName();
            if (name != null)
                c = namingStrategy.getComponent(name, retryCount, isContainer);
            else
                c = getComponentByProperties(id.getNameProps(), retryCount, isContainer);
            if (c == null)
                throw new Exception();
            Object info = id.getComponentInfo();
            if (info == null)
                info = id.getComponentInfoProps();
            return getMComponentByComponent(c, name, info);
        } catch (Exception e) {
            ComponentNotFoundException err = new ComponentNotFoundException("", scriptModel, windowMonitor);
            err.setMessage((e.getMessage() == null ? "" : e.getMessage() + "\n") + "Couldn't find component " + id + " in: "
                    + namingStrategy.getName(getWindowInternal()) + "\n" + namingStrategy.getVisibleComponentNames());
            err.captureScreen();
            throw err;
        }
    }

    private Component getComponentByProperties(Properties nameProps, int retryCount, boolean isContainer) {
        List<Component> components = getComponentsByProperties(nameProps, retryCount, isContainer);
        if (components == null || components.size() == 0)
            return null;
        return components.get(0);
    }

    private List<Component> getComponentsByProperties(final Properties nameProps, int retryCount, boolean isContainer) {
        String message = "More than one component matched for: " + nameProps;
        final ComponentNotFoundException err = new ComponentNotFoundException(message, null, null);
        final Object[] found = new Object[1];
        new Retry(err, ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
            public void perform() {
                List<Component> matchedComponents = findMatchedComponents(nameProps);
                if (matchedComponents.size() != 1) {
                    if (matchedComponents.size() == 0)
                        err.setMessage("No components matched for: " + nameProps);
                    else
                        err.setMessage("More than one component matched for: " + nameProps);
                    retry();
                } else
                    found[0] = matchedComponents;
            }
        });
        @SuppressWarnings("unchecked")
        List<Component> matchedComponents = (List<Component>) found[0];
        return matchedComponents;
    }

    private List<Component> findMatchedComponents(final Properties nameProps) {
        List<Component> l = new ArrayList<Component>();

        Set<Component> components = getAllAWTComponents();
        for (Component c : components) {
            MComponent mc = new MComponent(c, windowMonitor);
            if (mc.matched(nameProps)) {
                l.add(c);
            }
        }
        return l;
    }

    private Set<Component> getAllAWTComponents() {
        Set<Component> components = new HashSet<Component>();
        collectComponents(getWindowInternal(), components);
        return components;
    }

    private void collectComponents(Component current, Set<Component> components) {
        if (!current.isVisible() || !current.isShowing())
            return;
        components.add(current);
        if (current instanceof Container) {
            Component[] children = ((Container) current).getComponents();
            for (Component child : children) {
                collectComponents(child, components);
            }
            if (current instanceof Window) {
                Window[] ownedWindows = ((Window) current).getOwnedWindows();
                for (Window window : ownedWindows) {
                    collectComponents(window, components);
                }
            }
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
        return findMComponentByComponent(object, location);
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
            namingStrategy.setTopLevelComponent(window, true);
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
        namingStrategy.setTopLevelComponent(window, true);
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
        if (obj != null && obj instanceof Properties) {
            MComponent mComponent = resolver.getMComponent(component, name, null);
            if (mComponent instanceof MCollectionComponent) {
                return ((MCollectionComponent) mComponent).findMatchingComponent((Properties) obj);
            } else {
                throw new ComponentException("Given componentInfo for non collection component", scriptModel, windowMonitor);
            }
        }
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
        // TODO: Remove this sleep and ensure that we wait till the top most
        // window gets focus
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
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
        namingStrategy.setTopLevelComponent(getWindowInternal(), true);
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

    public void markUsed(MComponent component) {
        // Set toplevel container!!
        Component window = getTopLevelWindow(component.getComponent());
        if (window != null) {
            namingStrategy.setTopLevelComponent(window, true);
        }
        namingStrategy.markUsed(component.getMComponentName());
    }

    public boolean hasTopLevelWindow() {
        return windows.size() > 0;
    }

}
