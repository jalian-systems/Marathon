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
package net.sourceforge.marathon.objectmap;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.runtime.JavaRuntime;
import net.sourceforge.marathon.util.Retry;

public class ObjectMapNamingStrategy implements INamingStrategy, AWTEventListener {

    private static final Logger logger = Logger.getLogger(ObjectMapNamingStrategy.class.getName());

    private Map<MComponent, String> componentNameMap = new HashMap<MComponent, String>();

    private ObjectMapConfiguration configuration;
    private Component container;

    private boolean needUpdate = true;

    private ObjectMap objectMap;

    private WindowMonitor windowMonitor;

    public ObjectMapNamingStrategy() {
        configuration = new ObjectMapConfiguration();
        try {
            configuration.load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error in creating naming strategy", "Error in NamingStrategy",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
        objectMap = new ObjectMap();
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.COMPONENT_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent event) {
        needUpdate = true;
    }

    public Map<String, Component> getAllComponents() {
        HashMap<String, Component> componentMap = new HashMap<String, Component>();
        Set<Entry<MComponent, String>> set = componentNameMap.entrySet();
        for (Entry<MComponent, String> entry : set) {
            componentMap.put(entry.getValue(), entry.getKey().getComponent());
        }
        return componentMap;
    }

    public Component getComponent(final String name, int retryCount, boolean isContainer) {
        if (isContainer)
            return getContainer(name, retryCount, "Could not find container (InternalFrame) for: " + name);
        else
            return getComponent(name, retryCount);
    }

    private Component getComponent(final String name, int retryCount) {
        final OMapComponent omapComponent = objectMap.findComponentByName(name);
        if (omapComponent == null) {
            return getContainer(name, retryCount, "Could not find component/container (InternalFrame) for: " + name);
        }
        String message = "More than one component matched for: " + name + " with properties: " + omapComponent;
        final ComponentNotFoundException err = new ComponentNotFoundException(message, null, null);
        final Object[] found = new Object[1];
        new Retry(err, ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
            public void perform() {
                List<Component> matchedComponents = findComponent(omapComponent);
                if (matchedComponents.size() != 1) {
                    if (matchedComponents.size() == 0)
                        err.setMessage("No components matched for: " + name + " with properties: " + omapComponent);
                    else
                        err.setMessage("More than one component matched for: " + name + " with properties: " + omapComponent);
                    needUpdate = true ;
                    setTopLevelComponent(container);
                    retry();
                } else
                    found[0] = matchedComponents;
            }
        });
        @SuppressWarnings("unchecked")
        List<Component> matchedComponents = (List<Component>) found[0];
        return matchedComponents.get(0);
    }

    private Component getContainer(final String name, int retryCount, String message) {
        final ComponentNotFoundException err = new ComponentNotFoundException(message, null, null);
        final Object[] found = new Object[1];
        new Retry(err, ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
            public void perform() {
                Component c = findContainerForName(name);
                if (c == null) {
                    setTopLevelComponent(container);
                    retry();
                } else
                    found[0] = c;
            }

            private Component findContainerForName(String name) {
                Set<Entry<MComponent, String>> entrySet = componentNameMap.entrySet();
                for (Entry<MComponent, String> entry : entrySet) {
                    if (name.equals(entry.getValue()))
                        return entry.getKey().getComponent();
                }
                return null;
            }
        });
        Component c = (Component) found[0];
        boolean isStrict = System.getProperty(Constants.PROP_OMAP_RESOLVE_MODE, "strict").equals("strict");
        // TODO: YUK!!!
        if (!isStrict || c instanceof JInternalFrame)
            return c;
        return null;
    }

    public String getName(Component component) {
        if (component instanceof Window || component instanceof JInternalFrame)
            return getWindowName(component);
        MComponent current = findPropertyWrapper(component);
        if (current == null)
            return null;
        OMapComponent omapComponent;
        try {
            omapComponent = objectMap.findComponentByProperties(current);
        } catch (ObjectMapException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Unable to find component", 
                    JOptionPane.ERROR_MESSAGE);
            throw new ComponentNotFoundException(e.getMessage(), null, null);
        }
        if (omapComponent == null) {
            List<String> rprops = findUniqueRecognitionProperties(current);
            List<List<String>> rproperties = configuration.findRecognitionProperties(current.getComponent());
            List<List<String>> nproperties = configuration.findNamingProperties(current.getComponent());
            List<String> gproperties = configuration.getGeneralProperties();
            omapComponent = objectMap.insertNameForComponent(current.getMComponentName(), current, rprops, rproperties,
                    nproperties, gproperties);
        }
        return omapComponent.getName();
    }

    public OMapComponent getOMapComponent(Component component) {
        MComponent current = findPropertyWrapper(component);
        if (current == null)
            return null;
        OMapComponent omapComponent;
        try {
            omapComponent = objectMap.findComponentByProperties(current);
        } catch (ObjectMapException e) {
            throw new ComponentNotFoundException(e.getMessage(), null, null);
        }
        if (omapComponent == null) {
            List<String> rprops = findUniqueRecognitionProperties(current);
            List<List<String>> rproperties = configuration.findRecognitionProperties(current.getComponent());
            List<List<String>> nproperties = configuration.findNamingProperties(current.getComponent());
            List<String> gproperties = configuration.getGeneralProperties();
            omapComponent = objectMap.insertNameForComponent(current.getMComponentName(), current, rprops, rproperties,
                    nproperties, gproperties);
        }
        return omapComponent;
    }

    public String getVisibleComponentNames() {
        if (container == null)
            return "";
        setTopLevelComponent(container);
        StringBuilder sb = new StringBuilder();
        createVisibleStructure(container, sb, "");
        return sb.toString();
    }

    public void saveIfNeeded() {
        objectMap.save();
    }

    public void setTopLevelComponent(Component pcontainer) {
        if (container == pcontainer && !needUpdate)
            return;
        logger.info("Updating object map: " + (needUpdate ? "Toplevel container changed" : "Container contents changed"));
        container = pcontainer;
        MComponent wrapper = findMComponent(pcontainer);
        try {
            objectMap
                    .setTopLevelComponent(wrapper, configuration.findContainerRecognitionProperties(pcontainer),
                            configuration.findContainerNamingProperties(pcontainer), configuration.getGeneralProperties(),
                            getTitle(pcontainer));
        } catch (ObjectMapException e) {
            e.printStackTrace();
        }
        componentNameMap.clear();
        try {
            createNames(null, wrapper, 0);
        } catch (ObjectMapException e) {
            e.printStackTrace();
        }
        needUpdate = false;
    }

    private MComponent findMComponent(Component pcontainer) {
        ComponentFinder finder = JavaRuntime.getInstance().getFinder();
        if(finder == null)
            return new MComponent(pcontainer, getWindowMonitor());
        return finder.getMComponentByComponent(pcontainer, "No Name", null);
    }

    private boolean componentCanUse(MComponent current, List<String> rprops) {
        for (String rprop : rprops) {
            if (current.getProperty(rprop) == null)
                return false;
        }
        return true;
    }

    private boolean componentMatches(MComponent current, MComponent wrapper, List<String> rprops) {
        for (String rprop : rprops) {
            if (wrapper.getProperty(rprop) == null)
                return false;
            if (!wrapper.getProperty(rprop).equals(current.getProperty(rprop)))
                return false;
        }
        return true;
    }

    private String createName(MComponent w) {
        Component component = w.getComponent();
        if (component instanceof Window || component instanceof JInternalFrame)
            return getWindowName(component);
        List<List<String>> propertyList = configuration.findNamingProperties(w.getComponent());
        String name = null;
        for (List<String> properties : propertyList) {
            name = createName(w, properties);
            if (name == null || name.equals(""))
                continue;
            if (!componentNameMap.containsValue(name) && objectMap.findComponentByName(name) == null)
                return name;
        }

        for (List<String> properties : propertyList) {
            name = createName(w, properties);
            if (name != null && !name.equals(""))
                break;
        }

        if (name == null || name.equals(""))
            return createName(w, OMapComponent.LAST_RESORT_NAMING_PROPERTIES);

        String original = name;
        int index = 2;
        while (componentNameMap.containsValue(name) || objectMap.findComponentByName(name) != null) {
            name = original + "_" + index++;
        }
        return name;
    }

    private String createName(MComponent w, List<String> properties) {
        StringBuilder sb = new StringBuilder();
        for (String property : properties) {
            String v = w.getProperty(property);
            if (v == null)
                return null;
            sb.append(v).append('_');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private void createNames(MComponent parent, MComponent current, int indexInParent) throws ObjectMapException {
        Component c = current.getComponent();
        if (!c.isVisible() || c instanceof IRecordingArtifact)
            return;
        String name;
        OMapComponent omapComponent = objectMap.findComponentByProperties(current);
        if (omapComponent == null)
            name = createName(current);
        else
            name = omapComponent.getName();
        current.setMComponentName(name);
        componentNameMap.put(current, name);
        if (!(c instanceof Container))
            return;
        logger.info("Adding components for: " + current);
        Component[] components = ((Container) c).getComponents();
        int i;
        for (i = 0; i < components.length; i++) {
            MComponent wrapper = findMComponent(components[i]);
            createNames(current, wrapper, i);
        }
        if (c instanceof Window) {
            Window[] ownedWindows = ((Window) c).getOwnedWindows();
            for (int j = 0; j < ownedWindows.length; j++) {
                MComponent wrapper = findMComponent(ownedWindows[j]);
                createNames(current, wrapper, i + j);
            }
        }
    }

    private void createVisibleStructure(Component c, StringBuilder sb, String indent) {
        if (!c.isVisible() || c instanceof IRecordingArtifact)
            return;
        MComponent wrapper = findPropertyWrapper(c);
        sb.append(indent).append(c.getClass().getName() + "[" + wrapper.getMComponentName() + "]\n");
        if (c instanceof Container) {
            for (Component component : ((Container) c).getComponents())
                createVisibleStructure(component, sb, "    " + indent);
        }
    }

    private String createWindowName(MComponent w, List<String> properties) {
        StringBuilder sb = new StringBuilder();
        for (String property : properties) {
            String v = w.getProperty(property);
            if (v == null)
                return null;
            sb.append(v).append(':');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private List<Component> findComponent(OMapComponent omapComponent) {
        List<Component> matchedComponents = new ArrayList<Component>();
        Set<MComponent> set = componentNameMap.keySet();
        for (MComponent MComponent : set) {
            if (omapComponent.isMatched(MComponent)) {
                matchedComponents.add(MComponent.getComponent());
                break;
            }
        }
        return matchedComponents;
    }

    public MComponent findPropertyWrapper(Component component) {
        Set<MComponent> wrappers = componentNameMap.keySet();
        for (MComponent MComponent : wrappers) {
            if (MComponent.getComponent() == component)
                return MComponent;
        }
        logger.warning("ObjectMapNamingStrategy.getName(): Unexpected failure for findPropertyWrapper for component: " + component
                + " Trying again...");
        needUpdate = true;
        setTopLevelComponent(container);
        wrappers = componentNameMap.keySet();
        for (MComponent MComponent : wrappers) {
            if (MComponent.getComponent() == component)
                return MComponent;
        }
        logger.warning("ObjectMapNamingStrategy.getName(): Unexpected failure for findPropertyWrapper for component: " + component);
        return null;
    }

    private List<String> findUniqueRecognitionProperties(MComponent current) {
        logger.info("Finding unique properties for: " + current.getMComponentName());
        List<List<String>> rproperties = configuration.findRecognitionProperties(current.getComponent());
        Set<MComponent> wrappers = componentNameMap.keySet();
        for (List<String> rprops : rproperties) {
            if (!componentCanUse(current, rprops)) {
                logger.info("Skipping " + rprops + ": Can not use");
                continue;
            }
            boolean matched = false;
            for (MComponent wrapper : wrappers) {
                if (wrapper == current)
                    continue;
                if ((matched = componentMatches(current, wrapper, rprops))) {
                    logger.info("Skipping matched with " + wrapper.getMComponentName());
                    break;
                }
            }
            if (!matched)
                return rprops;
        }
        return OMapComponent.LAST_RESORT_RECOGNITION_PROPERTIES;
    }

    private String getTitle(Component window) {
        String title = null;
        if (window instanceof Frame) {
            title = ((Frame) window).getTitle();
        } else if (window instanceof Dialog) {
            title = ((Dialog) window).getTitle();
        } else if (window instanceof JInternalFrame) {
            title = ((JInternalFrame) window).getTitle();
        }
        return title == null ? "<NoTitle>" : title;
    }

    private WindowMonitor getWindowMonitor() {
        if (windowMonitor == null)
            windowMonitor = WindowMonitor.getInstance();
        return windowMonitor;
    }

    private String getWindowName(Component c) {
        MComponent wrapper = findMComponent(c);
        List<List<String>> windowNamingProperties = configuration.findContainerNamingProperties(c);
        String title = null;
        for (List<String> list : windowNamingProperties) {
            title = createWindowName(wrapper, list);
            if (title != null)
                break;
        }
        if (title == null) {
            // Last resort generation of window name
            title = getTitle(c);
        }
        if (c instanceof Window) {
            int index = 0;
            List<Window> windows = getWindowMonitor().getWindows();
            for (ListIterator<Window> iterator = windows.listIterator(); iterator.hasNext();) {
                Window w = (Window) iterator.next();
                if (w.equals(c))
                    break;
                if (getTitle(w).equals(title))
                    index++;
            }
            if (index > 0)
                title = title + "(" + index + ")";
        }
        return title;
    }

    public Component getComponent(final Properties nameProps, int retryCount, boolean isContainer) {
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
                    setTopLevelComponent(container);
                    retry();
                } else
                    found[0] = matchedComponents;
            }
        });
        @SuppressWarnings("unchecked")
        List<Component> matchedComponents = (List<Component>) found[0];
        return matchedComponents.get(0);
    }

    private List<Component> findMatchedComponents(final Properties nameProps) {
        List<Component> l = new ArrayList<Component>();
        Set<Entry<MComponent, String>> entrySet = componentNameMap.entrySet();
        for (Entry<MComponent, String> entry : entrySet) {
            if (entry.getKey().matched(nameProps)) {
                l.add(entry.getKey().getComponent());
            }
        }
        return l;
    }

    public void setDirty() {
        objectMap.setDirty(true);
    }

    public void markUsed(String name) {
        OMapComponent oMapComponent = objectMap.findComponentByName(name);
        if (oMapComponent != null)
            oMapComponent.markUsed(true);
        objectMap.setDirty(true);
    }
}
