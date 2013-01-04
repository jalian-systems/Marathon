package net.sourceforge.marathon.objectmap;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.ISubpanelProvider;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.IPropertyAccessor;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.mpf.DescriptionPanel;
import net.sourceforge.marathon.mpf.ISubPropertiesPanel;
import net.sourceforge.marathon.objectmap.OMapComponent;
import net.sourceforge.marathon.objectmap.ObjectMapException;
import net.sourceforge.marathon.objectmap.ObjectMapConfiguration.ObjectIdentity;
import net.sourceforge.marathon.objectmap.ObjectMapConfiguration.PropertyList;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.runtime.JavaRuntime;
import net.sourceforge.marathon.util.Retry;

public class ObjectMapNamingStrategy implements INamingStrategy<Component>, ISubpanelProvider {

    private static String description = "Using Object Map\n"
            + "\n\n"
            + "Using object map provides the most flexible solution for creating tests. When this lookup strategy is used, Marathon records the generated component names and their associated recognition properties in text files. These object map files are formatted in YAML format and can be edited with any text editor (including the Marathon's editor).\n"
            + "\n"
            + "If you are using MarathonITE, you can modify object names and properties using Object Map editor.\n"
            + "\n"
            + "MarathonITE also includes 'Use Object Map (enhanced)' option, that provides debug mode information for creating object maps."
            + "\n";

    private static final String MODULE = "Object Map";

    private Component container;

    protected IObjectMapService omapService;

    protected WindowMonitor windowMonitor;

    protected IOMapContainer topContainer;

    protected ILogger runtimeLogger;

    public ObjectMapNamingStrategy() {
    }

    public void init() {
        runtimeLogger = RuntimeLogger.getRuntimeLogger();
        omapService = getObjectMapService();
        try {
            omapService.load();
            runtimeLogger.info(MODULE, "Loaded object map omapService");
        } catch (IOException e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            runtimeLogger.error(MODULE, "Error in creating naming strategy:" + e.getMessage(), w.toString());
            JOptionPane.showMessageDialog(null, "Error in creating naming strategy:" + e.getMessage(), "Error in NamingStrategy",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Component getComponent(final String name, int retryCount, boolean isContainer) {
        Component c;
        if (isContainer)
            c = getContainer(name, retryCount, "Could not find container (InternalFrame) for: " + name);
        else
            c = getComponent(name, retryCount);
        return c;
    }

    public String getName(Component component) {
        String n;
        if (component instanceof Window || component instanceof JInternalFrame)
            n = getWindowName(component);
        else
            n = getOMapComponent(component).getName();
        return n;
    }

    public OMapComponent getOMapComponent(Component component) {
        MComponent current = new MComponent(component, windowMonitor);
        List<OMapComponent> omapComponents = omapService.findComponentsByProperties(getWrapper(current), topContainer);
        OMapComponent omapComponent;
        if (omapComponents.size() == 1) {
            return omapComponents.get(0);
        }
        if (omapComponents.size() > 1) {
            String message = "More than one component matched for " + getPropertyDisplayList(component);
            StringBuilder msg = new StringBuilder(message);
            msg.append("\n    The matched object map entries are:\n");
            for (OMapComponent omc : omapComponents) {
                msg.append("        ").append(omc.toString()).append("\n");
            }
            omapComponent = findClosestMatch(component, omapComponents, msg);
            if (omapComponent != null) {
                runtimeLogger.warning(MODULE, message, msg.toString());
                return omapComponent;
            }
            runtimeLogger.error(MODULE, message, msg.toString());
            throw new ComponentNotFoundException("More than one component matched: " + omapComponents, null, null);
        }
        StringBuilder msg = new StringBuilder();
        omapComponent = findClosestMatch(component, msg);
        if (omapComponent != null) {
            runtimeLogger.warning(MODULE, "Could not find object map entry for component: " + getPropertyDisplayList(component), msg.toString());
            return omapComponent;
        }
        List<String> rprops = findUniqueRecognitionProperties(current, component);
        current.setMComponentName(createName(current));
        List<String> values = new ArrayList<String>();
        for (String k : rprops) {
            values.add(current.getProperty(k));
        }
        List<List<String>> rproperties = findRecognitionProperties(current.getProperty("component.class.name"));
        List<List<String>> nproperties = findNamingProperties(current.getProperty("component.class.name"));
        List<String> gproperties = omapService.getGeneralProperties();
        omapComponent = omapService.insertNameForComponent(current.getProperty("MComponentName"), getWrapper(current), rprops,
                rproperties, nproperties, gproperties, topContainer);
        return omapComponent;
    }

    protected OMapComponent findClosestMatch(Component component, StringBuilder msg) {
        return null ;
    }

    protected OMapComponent findClosestMatch(Component component, List<OMapComponent> matched, StringBuilder msg) {
        return null;
    }

    public Map<String, Component> getAllComponents() {
        HashMap<String, Component> componentMap = new HashMap<String, Component>();
        Set<Component> components = getAllAWTComponents();
        for (Component component : components) {
            String name = null;
            MComponent w = new MComponent(component, windowMonitor);
            try {
                OMapComponent oMapComponent = omapService.findComponentByProperties(getWrapper(w), topContainer);
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
        omapService.save();
    }

    public void setTopLevelComponent(Component pcontainer) {
        container = pcontainer;
        MComponent wrapper = findMComponent(container);
        try {
            topContainer = omapService.getTopLevelComponent(getContainerWrapper(wrapper),
                    findContainerRecognitionProperties(getContainerClassName(wrapper)),
                    omapService.getGeneralProperties(), getTitle(container));
        } catch (Exception e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            runtimeLogger.error("Object Map", e.getMessage(), w.toString());
        }
    }

    protected String getContainerClassName(MComponent wrapper) {
        return wrapper.getComponent().getClass().getName();
    }

    public void setDirty() {
        omapService.setDirty(true);
    }

    public void markUsed(String name) {
        omapService.markUsed(name, topContainer);
    }

    private void createVisibleStructure(Component c, StringBuilder sb, String indent) {
        if (!c.isVisible() || c instanceof IRecordingArtifact)
            return;
        MComponent wrapper = new MComponent(c, windowMonitor);
        String name = null;
        try {
            OMapComponent oMapComponent = omapService.findComponentByProperties(getWrapper(wrapper), topContainer);
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
        final OMapComponent omapComponent = omapService.findComponentByName(name, topContainer);
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
                    found[0] = matchedComponents;
                    if (matchedComponents.size() != 1) {
                        if (matchedComponents.size() == 0) {
                            err.setMessage("No components matched for: " + name + " with properties: " + omapComponent);
                        } else
                            err.setMessage("More than one component matched for: " + name + " with properties: " + omapComponent);
                        retry();
                    }
                }
            });
        } catch (ComponentNotFoundException e) {
            @SuppressWarnings("unchecked")
            List<Component> matchedComponents = (List<Component>) found[0];
            StringBuilder msg = new StringBuilder("Could not find a component with name: '" + omapComponent.getName() + "'\n");
            msg.append("    Searched with: " + omapComponent.getComponentRecognitionProperties() + "\n");
            msg.append("    and found " + matchedComponents.size() + " components\n");
            if (matchedComponents.size() > 0) {
                for (Component component : matchedComponents) {
                    msg.append("        " + getPropertyDisplayList(component) + "\n");
                }
            }
            Component match = findClosestMatch(omapComponent, msg);
            if (match != null) {
                runtimeLogger.warning(MODULE, "Could not find a component with name: " + omapComponent.getName(), msg.toString());
                return match;
            }
            runtimeLogger.error(MODULE, "Could not find a component with name: " + omapComponent.getName(), msg.toString());
            throw e;
        }
        @SuppressWarnings("unchecked")
        List<Component> matchedComponents = (List<Component>) found[0];
        return matchedComponents.get(0);
    }

    protected Properties getPropertyDisplayList(Component component) {
        MComponent mc = new MComponent(component, windowMonitor);
        Properties props = new Properties();
        List<List<String>> properties = findRecognitionProperties(component.getClass().getName());
        properties.add(OMapComponent.LAST_RESORT_RECOGNITION_PROPERTIES);
        for (List<String> list : properties) {
            for (String prop : list) {
                String v = mc.getProperty(prop);
                if (v != null)
                    props.setProperty(prop, v);
            }
        }
        properties = findNamingProperties(component.getClass().getName());
        properties.add(OMapComponent.LAST_RESORT_NAMING_PROPERTIES);
        for (List<String> list : properties) {
            for (String prop : list) {
                String v = mc.getProperty(prop);
                if (v != null)
                    props.setProperty(prop, v);
            }
        }
        List<String> list = omapService.getGeneralProperties();
        for (String prop : list) {
            String v = mc.getProperty(prop);
            if (v != null)
                props.setProperty(prop, v);
        }
        return props;
    }

    protected Component findClosestMatch(OMapComponent omapComponent, StringBuilder msg) {
        return null;
    }

    private MComponent findMComponent(Component pcontainer) {
        ComponentFinder finder = JavaRuntime.getInstance().getFinder();
        if (finder == null)
            return new MComponent(pcontainer, getWindowMonitor());
        return finder.getMComponentByComponent(pcontainer, "No Name", null);
    }

    protected boolean componentCanUse(MComponent current, List<String> rprops) {
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
        List<List<String>> propertyList = findNamingProperties(w.getProperty("component.class.name"));
        String name = null;
        for (List<String> properties : propertyList) {
            name = createName(w, properties);
            if (name == null || name.equals(""))
                continue;
            if (omapService.findComponentByName(name, topContainer) == null)
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
        while (omapService.findComponentByName(name, topContainer) != null) {
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

    protected List<String> findUniqueRecognitionProperties(MComponent current, Component component) {
        List<List<String>> rproperties = findRecognitionProperties(current.getProperty("component.class.name"));
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

    protected Set<Component> getAllAWTComponents() {
        Set<Component> components = new HashSet<Component>();
        collectComponents(container, components);
        return components;
    }

    private void collectComponents(Component current, Set<Component> components) {
        if(!current.isVisible() || !current.isShowing())
            return;
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
        List<List<String>> windowNamingProperties = findContainerNamingProperties(c.getClass().getName());
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

    public IObjectMapService getObjectMapService() {
        return new ObjectMapService();
    }

    public IPropertyAccessor getWrapper(MComponent c) {
        return c;
    }

    public IPropertyAccessor getContainerWrapper(MComponent wrapper) {
        return wrapper;
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

    public List<List<String>> findNamingProperties(String cName) {
        return findProperties(findClass(cName), omapService.getNamingProperties());
    }

    public Class<?> findClass(String cName) {
        try {
            return Class.forName(cName);
        } catch (ClassNotFoundException e) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(cName);
            } catch (ClassNotFoundException e1) {
                return JComponent.class;
            }
        }
    }

    private List<List<String>> findProperties(Class<?> class1, List<ObjectIdentity> list) {
        List<PropertyList> selection = new ArrayList<PropertyList>();
        while (class1 != null) {
            for (ObjectIdentity objectIdentity : list) {
                if (objectIdentity.getClassName().equals(class1.getName()))
                    selection.addAll(objectIdentity.getPropertyLists());
            }
            class1 = class1.getSuperclass();
        }
        Collections.sort(selection, new Comparator<PropertyList>() {
            public int compare(PropertyList o1, PropertyList o2) {
                return o2.getPriority() - o1.getPriority();
            }
        });
        List<List<String>> sortedList = new ArrayList<List<String>>();
        for (PropertyList pl : selection) {
            sortedList.add(new ArrayList<String>(pl.getProperties()));
        }
        return sortedList;
    }

    public List<List<String>> findRecognitionProperties(String c) {
        return findProperties(findClass(c), omapService.getRecognitionProperties());
    }

    public List<List<String>> findContainerNamingProperties(String c) {
        return findProperties(findClass(c), omapService.getContainerNamingProperties());
    }

    public List<List<String>> findContainerRecognitionProperties(String c) {
        return findProperties(findClass(c), omapService.getContainerRecognitionProperties());
    }

}