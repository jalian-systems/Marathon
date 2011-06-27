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
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.component.ComponentException;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.PropertyWrapper;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;

public class ObjectMapNamingStrategy implements INamingStrategy, AWTEventListener {

    private static final List<String> LAST_RESORT_NAMING_PROPERTIES = new ArrayList<String>();
    private static final List<String> LAST_RESORT_RECOGNITION_PROPERTIES = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(ObjectMapNamingStrategy.class.getName());

    static {
        LAST_RESORT_NAMING_PROPERTIES.add("type");
        LAST_RESORT_NAMING_PROPERTIES.add("indexInContainer");
        LAST_RESORT_RECOGNITION_PROPERTIES.add("type");
        LAST_RESORT_RECOGNITION_PROPERTIES.add("indexInContainer");
    }
    private Map<PropertyWrapper, String> componentNameMap = new HashMap<PropertyWrapper, String>();

    private ObjectMapConfiguration configuration;
    private Component container;

    private int indexInContainer;

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
        Set<Entry<PropertyWrapper, String>> set = componentNameMap.entrySet();
        for (Entry<PropertyWrapper, String> entry : set) {
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
                Set<Entry<PropertyWrapper, String>> entrySet = componentNameMap.entrySet();
                for (Entry<PropertyWrapper, String> entry : entrySet) {
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
        PropertyWrapper current = findPropertyWrapper(component);
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
        return omapComponent.getName();
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
        PropertyWrapper wrapper = new PropertyWrapper(pcontainer, getWindowMonitor());
        try {
            objectMap.setTopLevelComponent(wrapper, configuration.findContainerRecognitionProperties(pcontainer),
                    configuration.findContainerNamingProperties(pcontainer), configuration.getGeneralProperties(),
                    getTitle(pcontainer));
            componentNameMap.clear();
            indexInContainer = 0;
            createNames(null, wrapper, 0);
            needUpdate = false;
        } catch (ObjectMapException e) {
            throw new ComponentException(e.getMessage(), null, null);
        }
    }

    private boolean componentCanUse(PropertyWrapper current, List<String> rprops) {
        for (String rprop : rprops) {
            if (current.getProperty(rprop) == null)
                return false;
        }
        return true;
    }

    private boolean componentMatches(PropertyWrapper current, PropertyWrapper wrapper, List<String> rprops) {
        for (String rprop : rprops) {
            if (wrapper.getProperty(rprop) == null)
                return false;
            if (!wrapper.getProperty(rprop).equals(current.getProperty(rprop)))
                return false;
        }
        return true;
    }

    private String createName(PropertyWrapper w) {
        Component component = w.getComponent();
        if (component instanceof Window || component instanceof JInternalFrame)
            return getWindowName(component);
        List<List<String>> propertyList = configuration.findNamingProperties(w.getComponent());
        for (List<String> properties : propertyList) {
            String name = createName(w, properties);
            OMapComponent compByName = objectMap.findComponentByName(name);
            if (name != null && !"".equals(name) && !componentNameMap.containsValue(name) && compByName == null) {
                return name;
            } else if (name != null && componentNameMap.containsValue(name) && compByName == null) {
                PropertyWrapper wrapper = findPropertyWrapper(name);
                logger.info("Name already used name = " + name + " for " + wrapper.getComponent().getClass());
            }
        }
        return createName(w, LAST_RESORT_NAMING_PROPERTIES);
    }

    private PropertyWrapper findPropertyWrapper(String name) {
        Set<Entry<PropertyWrapper, String>> entrySet = componentNameMap.entrySet();
        for (Entry<PropertyWrapper, String> entry : entrySet) {
            if (entry.getValue().equals(name))
                return entry.getKey();
        }
        return null;
    }

    private String createName(PropertyWrapper w, List<String> properties) {
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

    private void createNames(PropertyWrapper parent, PropertyWrapper current, int indexInParent) throws ObjectMapException {
        Component c = current.getComponent();
        if (!c.isVisible() || c instanceof IRecordingArtifact)
            return;
        String name;
        current.setIndexInParent(indexInParent);
        current.setParentName(componentNameMap.get(parent));
        current.setIndexInContainer(indexInContainer);
        if (parent != null && parent.getComponent() instanceof Container) {
            LayoutManager layout = ((Container) parent.getComponent()).getLayout();
            if (layout != null) {
                setLayoutData(current, layout);
                setPrecedingLabel(current, parent);
            }
        }
        if (parent != null)
            setFieldName(current, parent.getComponent());
        OMapComponent omapComponent = objectMap.findComponentByProperties(current);
        if (omapComponent == null)
            name = createName(current);
        else
            name = omapComponent.getName();
        indexInContainer++;
        current.setMComponentName(name);
        componentNameMap.put(current, name);
        if (!(c instanceof Container))
            return;
        logger.info("Adding components for: " + current);
        Component[] components = ((Container) c).getComponents();
        int i;
        for (i = 0; i < components.length; i++) {
            PropertyWrapper wrapper = new PropertyWrapper(components[i], getWindowMonitor());
            createNames(current, wrapper, i);
        }
        if (c instanceof Window) {
            Window[] ownedWindows = ((Window) c).getOwnedWindows();
            for (int j = 0; j < ownedWindows.length; j++) {
                PropertyWrapper wrapper = new PropertyWrapper(ownedWindows[j], getWindowMonitor());
                createNames(current, wrapper, i + j);
            }
        }
    }

    private void setFieldName(PropertyWrapper currentWrapper, Component container) {
        Component current = currentWrapper.getComponent();
        while (container != null) {
            String name = findField(current, container);
            if (name != null) {
                currentWrapper.setFieldName(name);
                return;
            }
            container = container.getParent();
        }
    }

    private String findField(Component current, Component container) {
        Field[] declaredFields = container.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                Object o = field.get(container);
                if (o == current)
                    return field.getName();
            } catch (Throwable t) {
            } finally {
                field.setAccessible(accessible);
            }
        }
        return null;
    }

    private void setPrecedingLabel(PropertyWrapper current, PropertyWrapper parent) {
        Component component = current.getComponent();
        if (component instanceof JLabel || component instanceof JScrollPane || component instanceof JViewport
                || component instanceof JPanel)
            return;
        String labelText = findLabel(component, (Container) parent.getComponent());
        if (labelText != null && labelText.endsWith(":")) {
            labelText = labelText.substring(0, labelText.length() - 1).trim();
        }
        current.setPrecedingLabel(labelText);
    }

    private String findLabel(Component component, Container container) {
        if (component == null || container == null)
            return null;
        Component[] allComponents = container.getComponents();
        // Find labels in the same row (LTR)
        // In the same row: labelx < componentx, labely >= componenty
        for (Component label : allComponents) {
            if (label instanceof JLabel) {
                if (label.getX() < component.getX() && label.getY() >= component.getY()
                        && label.getY() <= component.getY() + component.getHeight()) {
                    String text = ((JLabel) label).getText();
                    if (text == null)
                        return null;
                    return text.trim();
                }
            }
        }
        // Find labels in the same column
        // In the same row: labelx < componentx, labely >= componenty
        for (Component label : allComponents) {
            if (label instanceof JLabel) {
                if (label.getY() < component.getY() && label.getX() >= component.getX()
                        && label.getX() <= component.getX() + component.getWidth()) {
                    String text = ((JLabel) label).getText();
                    if (text == null)
                        return null;
                    return text.trim();
                }
            }
        }
        return null;
    }

    private void createVisibleStructure(Component c, StringBuilder sb, String indent) {
        if (!c.isVisible() || c instanceof IRecordingArtifact)
            return;
        PropertyWrapper wrapper = findPropertyWrapper(c);
        sb.append(indent).append(c.getClass().getName() + "[" + wrapper.getMComponentName() + "]\n");
        if (c instanceof Container) {
            for (Component component : ((Container) c).getComponents())
                createVisibleStructure(component, sb, "    " + indent);
        }
    }

    private String createWindowName(PropertyWrapper w, List<String> properties) {
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
        Set<PropertyWrapper> set = componentNameMap.keySet();
        for (PropertyWrapper propertyWrapper : set) {
            if (omapComponent.isMatched(propertyWrapper)) {
                matchedComponents.add(propertyWrapper.getComponent());
                break;
            }
        }
        return matchedComponents;
    }

    private PropertyWrapper findPropertyWrapper(Component component) {
        Set<PropertyWrapper> wrappers = componentNameMap.keySet();
        for (PropertyWrapper propertyWrapper : wrappers) {
            if (propertyWrapper.getComponent() == component)
                return propertyWrapper;
        }
        logger.warning("ObjectMapNamingStrategy.getName(): Unexpected failure for findPropertyWrapper for component: " + component
                + " Trying again...");
        needUpdate = true;
        setTopLevelComponent(container);
        wrappers = componentNameMap.keySet();
        for (PropertyWrapper propertyWrapper : wrappers) {
            if (propertyWrapper.getComponent() == component)
                return propertyWrapper;
        }
        logger.warning("ObjectMapNamingStrategy.getName(): Unexpected failure for findPropertyWrapper for component: " + component);
        return null;
    }

    private List<String> findUniqueRecognitionProperties(PropertyWrapper current) {
        logger.info("Finding unique properties for: " + current.getMComponentName());
        List<List<String>> rproperties = configuration.findRecognitionProperties(current.getComponent());
        Set<PropertyWrapper> wrappers = componentNameMap.keySet();
        for (List<String> rprops : rproperties) {
            if (!componentCanUse(current, rprops)) {
                logger.info("Skipping " + rprops + ": Can not use");
                continue;
            }
            boolean matched = false;
            for (PropertyWrapper wrapper : wrappers) {
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
        return LAST_RESORT_RECOGNITION_PROPERTIES;
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
        PropertyWrapper wrapper = new PropertyWrapper(c, windowMonitor);
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

    private void setLayoutData(PropertyWrapper current, LayoutManager layout) {
        try {
            Method method = layout.getClass().getMethod("getConstraints", Component.class);
            Object layoutData = method.invoke(layout, current.getComponent());
            current.setLayoutData(layoutData);
            return;
        } catch (Exception e) {
        }
        if (layout instanceof GridLayout) {
            int columns = ((GridLayout) layout).getColumns();
            int indexInParent = current.getIndexInParent();
            current.setLayoutData(new Point(indexInParent / columns, indexInParent % columns));
        }
    }

    public void markUnused(Component c) {
        PropertyWrapper propertyWrapper = findPropertyWrapper(c);
        if (propertyWrapper == null)
            return;
        String name = componentNameMap.get(propertyWrapper);
        if (name == null)
            return;
        objectMap.removeBinding(name);
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
        Set<Entry<PropertyWrapper, String>> entrySet = componentNameMap.entrySet();
        for (Entry<PropertyWrapper, String> entry : entrySet) {
            if (entry.getKey().matched(nameProps)) {
                l.add(entry.getKey().getComponent());
            }
        }
        return l;
    }

}
