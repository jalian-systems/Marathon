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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ISubpanelProvider;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.mpf.DescriptionPanel;
import net.sourceforge.marathon.mpf.ISubPropertiesPanel;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.runtime.JavaRuntime;
import net.sourceforge.marathon.util.Retry;

public class ObjectMapNamingStrategy implements INamingStrategy, ISubpanelProvider {

    private static String description = "Using Object Map\n"
            + "\n\n"
            + "Using object map provides the most flexible solution for creating tests. When this lookup strategy is used, Marathon records the generated component names and their associated recognition properties in text files. These object map files are formatted in YAML format and can be edited with any text editor (including the Marathon's editor).\n"
            + "\n"
            + "If you are using MarathonITE, you can modify object names and properties using Object Map editor.\n"
            + "\n"
            + "MarathonITE also includes 'Use Object Map (enhanced)' option, that provides debug mode information for creating object maps."
            + "\n";

    private static final Logger logger = Logger.getLogger(ObjectMapNamingStrategy.class.getName());

    private ObjectMapConfiguration configuration;
    private Component container;

    private ObjectMap objectMap;

    private WindowMonitor windowMonitor;

    private OMapContainer topContainer;

    public ObjectMapNamingStrategy() {
        configuration = new ObjectMapConfiguration();
        try {
            configuration.load();
            logger.info("Loaded object map configuration");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error in creating naming strategy:" + e.getMessage(), "Error in NamingStrategy",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
        objectMap = new ObjectMap();
        logger.info("Created an object map");
    }

    public Component getComponent(final String name, int retryCount, boolean isContainer) {
        Component c;
        if (isContainer)
            c = getContainer(name, retryCount, "Could not find container (InternalFrame) for: " + name);
        else
            c = getComponent(name, retryCount);
        logger.info("get_component(" + name + "): " + c.getClass());
        return c;
    }

    public String getName(Component component) {
        String n;
        if (component instanceof Window || component instanceof JInternalFrame)
            n = getWindowName(component);
        else
            n = getOMapComponent(component).getName();
        logger.info("get_component(" + component.getClass() + "): " + n);
        return n;
    }

    public OMapComponent getOMapComponent(Component component) {
        MComponent current = new MComponent(component, windowMonitor);
        OMapComponent omapComponent = null;
        try {
            omapComponent = objectMap.findComponentByProperties(current, topContainer);
        } catch (ObjectMapException e) {
            throw new ComponentNotFoundException(e.getMessage(), null, null);
        }
        if (omapComponent == null) {
            List<String> rprops = findUniqueRecognitionProperties(current, component);
            current.setMComponentName(createName(current));
            List<String> values = new ArrayList<String>();
            for (String k : rprops) {
                values.add(current.getProperty(k));
            }
            List<List<String>> rproperties = configuration.findRecognitionProperties(current.getProperty("component.class.name"));
            List<List<String>> nproperties = configuration.findNamingProperties(current.getProperty("component.class.name"));
            List<String> gproperties = configuration.getGeneralProperties();
            omapComponent = objectMap.insertNameForComponent(current.getProperty("MComponentName"), current, rprops, rproperties,
                    nproperties, gproperties, topContainer);
            logger.info("Inserted: " + omapComponent);
        }
        logger.info("get_omap_component(" + component.getClass() + "): " + omapComponent);
        return omapComponent;
    }

    public Map<String, Component> getAllComponents() {
        HashMap<String, Component> componentMap = new HashMap<String, Component>();
        Set<Component> components = getAllAWTComponents();
        for (Component component : components) {
            String name = null;
            MComponent w = new MComponent(component, windowMonitor);
            try {
                OMapComponent oMapComponent = objectMap.findComponentByProperties(w, topContainer);
                if (oMapComponent != null)
                    name = oMapComponent.getName();
            } catch (ObjectMapException e) {
            }
            if (name == null)
                name = "<" + createName(w) + ">";
            componentMap.put(name, component);
        }
        return componentMap;
    }

    public String getVisibleComponentNames() {
        if (container == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("\nThe component with names surrounded with < and > are not in the object map and are not available with get_component.\n");
        createVisibleStructure(container, sb, "");
        return sb.toString();
    }

    public void saveIfNeeded() {
        objectMap.save();
    }

    public void setTopLevelComponent(Component pcontainer) {
        container = pcontainer;
        MComponent wrapper = findMComponent(container);
        try {
            topContainer = objectMap.getTopLevelComponent(wrapper,
                    configuration.findContainerRecognitionProperties(container.getClass().getName()),
                    configuration.getGeneralProperties(), getTitle(container));
            logger.info("Set top level container to: " + topContainer.getFileName());
        } catch (ObjectMapException e) {
            logger.warning(e.getMessage());
        }
    }

    public void setDirty() {
        objectMap.setDirty(true);
    }

    public void markUsed(String name) {
        OMapComponent oMapComponent = objectMap.findComponentByName(name, topContainer);
        if (oMapComponent != null)
            oMapComponent.markUsed(true);
        objectMap.setDirty(true);
    }

    public ISubPropertiesPanel[] getSubPanels(JDialog parent) {
        ISubPropertiesPanel p1 = new ISubPropertiesPanel() {
            public void setProperties(Properties props) {
            }

            public boolean isValidInput() {
                return true;
            }

            public void getProperties(Properties props) {
            }

            public JPanel getPanel() {
                return new DescriptionPanel(description);
            }

            public String getName() {
                return "Use Object Map";
            }

            public Icon getIcon() {
                return null;
            }

            public int getMnemonic() {
                return 0;
            }
        };
        return new ISubPropertiesPanel[] { p1 };
    }

    private void createVisibleStructure(Component c, StringBuilder sb, String indent) {
        if (!c.isVisible() || c instanceof IRecordingArtifact)
            return;
        MComponent wrapper = new MComponent(c, windowMonitor);
        String name = null;
        try {
            OMapComponent oMapComponent = objectMap.findComponentByProperties(wrapper, topContainer);
            if (oMapComponent != null)
                name = oMapComponent.getName();
        } catch (ObjectMapException e) {
        }
        if (name == null)
            name = "<" + createName(wrapper) + ">";
        sb.append(indent).append(c.getClass().getName() + "[" + name + "]\n");
        if (c instanceof Container) {
            for (Component component : ((Container) c).getComponents())
                createVisibleStructure(component, sb, "    " + indent);
        }
    }

    private Component getContainer(final String name, int retryCount, String message) {
        final ComponentNotFoundException err = new ComponentNotFoundException(message, null, null);
        final Object[] found = new Object[1];
        new Retry(err, ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
            public void perform() {
                Component c = findContainerForName(name);
                if (c == null) {
                    retry();
                } else
                    found[0] = c;
            }

            private Component findContainerForName(String name) {
                Set<Component> components = getAllAWTComponents();
                for (Component c : components) {
                    if (c instanceof JInternalFrame || c instanceof Window) {
                        String n = createName(new MComponent(c, windowMonitor));
                        if (n.equals(name))
                            return c;
                    }
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

    public Component getComponent(final String name, int retryCount) {
        final OMapComponent omapComponent = objectMap.findComponentByName(name, topContainer);
        if (omapComponent == null) {
            return getContainer(name, retryCount, "Could not find component/container (InternalFrame) for: " + name);
        }
        String message = "More than one component matched for: " + name + " with properties: " + omapComponent;
        final ComponentNotFoundException err = new ComponentNotFoundException(message, null, null);
        final Object[] found = new Object[1];
        try {
            new Retry(err, ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
                public void perform() {
                    List<Component> matchedComponents = findComponent(omapComponent);
                    if (matchedComponents.size() != 1) {
                        if (matchedComponents.size() == 0) {
                            err.setMessage("No components matched for: " + name + " with properties: " + omapComponent);
                        } else
                            err.setMessage("More than one component matched for: " + name + " with properties: " + omapComponent);
                        retry();
                    } else
                        found[0] = matchedComponents;
                }
            });
        } catch (ComponentNotFoundException e) {
            throw e;
        }
        @SuppressWarnings("unchecked")
        List<Component> matchedComponents = (List<Component>) found[0];
        return matchedComponents.get(0);
    }

    private MComponent findMComponent(Component pcontainer) {
        ComponentFinder finder = JavaRuntime.getInstance().getFinder();
        if (finder == null)
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

    private boolean componentMatches(MComponent current, MComponent other, List<String> rprops) {
        for (String rprop : rprops) {
            if (other.getProperty(rprop) == null)
                return false;
            if (!other.getProperty(rprop).equals(current.getProperty(rprop)))
                return false;
        }
        return true;
    }

    private String createName(MComponent w) {
        Component component = w.getComponent();
        if (component instanceof Window || component instanceof JInternalFrame)
            return getWindowName(component);
        List<List<String>> propertyList = configuration.findNamingProperties(w.getProperty("component.class.name"));
        String name = null;
        for (List<String> properties : propertyList) {
            name = createName(w, properties);
            if (name == null || name.equals(""))
                continue;
            if (objectMap.findComponentByName(name, topContainer) == null)
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
        while (objectMap.findComponentByName(name, topContainer) != null) {
            name = original + "_" + index++;
        }
        return name;
    }

    private String createName(MComponent w, List<String> properties) {
        StringBuilder sb = new StringBuilder();
        for (String property : properties) {
            String v = w.getProperty(property);
            if (v == null || v.equals(""))
                return null;
            sb.append(v).append('_');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString().trim();
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
        return sb.toString().trim();
    }

    private List<Component> findComponent(OMapComponent omapComponent) {
        List<Component> matchedComponents = new ArrayList<Component>();
        Set<Component> set = getAllAWTComponents();
        for (Component c : set) {
            if (omapComponent.isMatched(new MComponent(c, windowMonitor))) {
                matchedComponents.add(c);
            }
        }
        return matchedComponents;
    }

    private List<String> findUniqueRecognitionProperties(MComponent current, Component component) {
        List<List<String>> rproperties = configuration.findRecognitionProperties(current.getProperty("component.class.name"));
        Set<Component> components = getAllAWTComponents();
        for (List<String> rprops : rproperties) {
            if (!componentCanUse(current, rprops)) {
                continue;
            }
            boolean matched = false;
            for (Component c : components) {
                MComponent wrapper = new MComponent(c, windowMonitor);
                if (c == component)
                    continue;
                if ((matched = componentMatches(current, wrapper, rprops))) {
                    break;
                }
            }
            if (!matched) {
                return rprops;
            }
        }
        return OMapComponent.LAST_RESORT_RECOGNITION_PROPERTIES;
    }

    private Set<Component> getAllAWTComponents() {
        Set<Component> components = new HashSet<Component>();
        collectComponents(container, components);
        return components;
    }

    private void collectComponents(Component current, Set<Component> components) {
        components.add(current);
        if (current instanceof Container) {
            Component[] children = ((Container) current).getComponents();
            for (Component child : children) {
                collectComponents(child, components);
            }
            if (current instanceof Window) {
                Window[] windows = ((Window) current).getOwnedWindows();
                for (Window window : windows) {
                    collectComponents(window, components);
                }
            }
        }
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
        List<List<String>> windowNamingProperties = configuration.findContainerNamingProperties(c.getClass().getName());
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
}
