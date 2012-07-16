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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.recorder.IRecordingArtifact;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;

/**
 * substitute your own naming strategy to customize how Marathon finds and names
 * components
 */
public class MarathonNamingStrategy implements INamingStrategy {

    private static class NamedComponent {
        private static final ArrayList<NamedComponent> cache = new ArrayList<NamedComponent>();
        private static int cacheIndex = 0;
        private Component component;
        private String name;
        private NamedComponent children;
        private NamedComponent sibling;
        private MarathonNamingStrategy strategy;

        public static NamedComponent get(MarathonNamingStrategy namingStrategy, NamedComponent parent, Component component) {
            NamedComponent nc;
            if (cacheIndex >= cache.size()) {
                nc = new NamedComponent();
                cache.add(nc);
            } else {
                nc = (NamedComponent) cache.get(cacheIndex);
            }
            cacheIndex++;
            nc.init(namingStrategy, parent, component);
            return nc;
        }

        public static void reset(Object o) {
            cacheIndex = 0;
        }

        private NamedComponent() {
        }

        private void init(MarathonNamingStrategy strategy, NamedComponent parent, Component component) {
            this.strategy = strategy;
            this.component = component;
            children = null;
            sibling = null;
            if (component instanceof JInternalFrame)
                name = strategy.getName(component);
            else {
                String nsName = strategy.createName(component);
                if (nsName.startsWith("{"))
                    nsName = '\\' + nsName;
                name = createUniqueName(nsName, 0, strategy.getNameComponentMap());
            }
            if (parent != null) {
                if (parent.children == null)
                    parent.children = this;
                else {
                    NamedComponent nc = parent.children;
                    while (nc.sibling != null) {
                        nc = nc.sibling;
                    }
                    nc.sibling = this;
                }
            }
        }

        private String createUniqueName(String initialName, int startIndex, Map<String, NamedComponent> map) {
            String name = initialName + (startIndex == 0 ? "" : "" + startIndex);
            if (map.get(name) != null)
                return createUniqueName(initialName, ++startIndex, map);
            return strategy.escapeParenthesis(name);
        }

        public String getName() {
            return name;
        }

        public Component getComponent() {
            return component;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            return toString(sb, "");
        }

        private String toString(StringBuffer sb, String indent) {
            sb.append(indent).append("[").append(component.getClass().getName()).append("(").append(name).append(")").append("]")
                    .append("\n");
            if (sibling != null)
                sibling.toString(sb, indent);
            if (children != null)
                children.toString(sb, indent + "  ");
            return sb.toString();
        }
    }

    protected boolean useFieldNames = Boolean.getBoolean(Constants.PROP_USE_FIELD_NAMES);
    public static INamingStrategy namingStrategy;
    private Map<String, NamedComponent> nameComponentMap = new HashMap<String, NamedComponent>();
    private Map<Component, NamedComponent> componentNameMap = new HashMap<Component, NamedComponent>();
    protected Map<Object, String> objectFieldNameMap = new HashMap<Object, String>();
    private NamedComponent head = null;
    private Component container;

    public MarathonNamingStrategy() {
    }

    private String escapeParenthesis(String name) {
        name = name.replaceAll("#", "##");
        name = name.replaceAll("\\(", "#{");
        name = name.replaceAll("\\)", "#}");
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.INamingStrategy#setTopLevelComponent
     * (java.awt.Component)
     */
    public void setTopLevelComponent(Component pcontainer) {
        if (pcontainer != container) {
            reset();
            container = pcontainer;
        }
        createNames();
    }

    private void reset() {
        nameComponentMap.clear();
        componentNameMap.clear();
        objectFieldNameMap.clear();
        NamedComponent.reset(this);
    }

    private synchronized void createNames() {
        if (!container.isVisible()) {
            return;
        }
        initializeNamedComponentMap(null, container);
    }

    private void initializeNamedComponentMap(NamedComponent parent, Component component) {
        if (component instanceof IRecordingArtifact)
            return;
        NamedComponent namedComponent;
        if (!componentNameMap.containsKey(component)) {
            namedComponent = NamedComponent.get(this, parent, component);
            nameComponentMap.put(namedComponent.getName(), namedComponent);
            componentNameMap.put(namedComponent.getComponent(), namedComponent);
        } else {
            namedComponent = (NamedComponent) componentNameMap.get(component);
        }
        if (component instanceof Container) {
            getFieldNames(component);
            Container parentContainer = (Container) component;
            int componentCount = parentContainer.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component c = parentContainer.getComponent(i);
                initializeNamedComponentMap(namedComponent, c);
            }
        }
        if (component instanceof Window) {
            Window[] ownedWindows = ((Window) component).getOwnedWindows();
            for (int i = 0; i < ownedWindows.length; i++) {
                if (ownedWindows[i].isVisible()) {
                    initializeNamedComponentMap(namedComponent, ownedWindows[i]);
                }
            }
        }
        if (parent == null) {
            head = namedComponent;
        }
    }

    private void getFieldNames(Object o) {
        Class<?> c = o.getClass();
        getFieldNames(o, c);
        getFieldNames(o, c.getSuperclass());
    }

    private void getFieldNames(Object o, Class<?> c) {
        if (c == null)
            return;
        Field[] fields = c.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (Component.class.isAssignableFrom(fields[i].getType())) {
                    fields[i].setAccessible(true);
                    objectFieldNameMap.put(fields[i].get(o), fields[i].getName());
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.INamingStrategy#getComponent(java.
     * lang.String)
     */
    public Component getComponent(final String name, int retryCount, boolean isContainer) {
        final Component[] found = new Component[1];
        new Retry("Could not find component", ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
            public void perform() {
                if ((found[0] = findComponent(name)) == null) {
                    retry();
                }
            }
        });
        return found[0];
    }

    private Component findComponent(String name) {
        NamedComponent c = lookupComponent(name);
        if (c == null && container != null) {
            createNames();
            c = lookupComponent(name);
        }
        if (c == null || !c.getComponent().isVisible() || !c.getComponent().isShowing())
            return null;
        return c.getComponent();
    }

    private NamedComponent lookupComponent(String name) {
        if (name.startsWith("{"))
            return lookupComponent(PropertyHelper.fromString(name, new String[][] {}));
        else
            return (NamedComponent) nameComponentMap.get(name);
    }

    private NamedComponent lookupComponent(Properties props) {
        Collection<NamedComponent> values = nameComponentMap.values();
        for (Iterator<NamedComponent> iterator = values.iterator(); iterator.hasNext();) {
            NamedComponent nc = (NamedComponent) iterator.next();
            if (matched(nc.getComponent(), props))
                return nc;
        }
        return null;
    }

    public Component getComponent(final Properties nameProps, int retryCount, boolean isContainer) {
        final Component[] found = new Component[1];
        new Retry("Could not find component", ComponentFinder.RETRY_INTERVAL_MS, retryCount, new Retry.Attempt() {
            public void perform() {
                if ((found[0] = findComponent(nameProps)) == null) {
                    retry();
                }
            }
        });
        return found[0];
    }
    
    private Component findComponent(Properties props) {
        NamedComponent c = lookupComponent(props);
        if (c == null && container != null) {
            createNames();
            c = lookupComponent(props);
        }
        if (c == null || !c.getComponent().isVisible() || !c.getComponent().isShowing())
            return null;
        return c.getComponent();
    }

    private boolean matched(Component component, Properties props) {
        Iterator<Entry<Object, Object>> elements = props.entrySet().iterator();
        while (elements.hasNext()) {
            Entry<Object, Object> entry = elements.next();
            MComponent temp = getPropertyWrapperObject(component);
            String actual = temp.getProperty(entry.getKey().toString());
            if (actual == null)
                actual = "null";
            if (!actual.equals(entry.getValue()))
                return false;
        }
        return true;
    }

    private String internalGetName(Component component) {
        NamedComponent namedComponent = (NamedComponent) componentNameMap.get(component);
        if (namedComponent != null && namedComponent.getComponent().isVisible())
            return namedComponent.getName();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.INamingStrategy#getVisibleComponentNames
     * ()
     */
    public String getVisibleComponentNames() {
        if (head == null)
            createNames();
        if (head == null)
            return "<None Found>";
        return head.toString(new StringBuffer(), "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.component.INamingStrategy#getAllComponents()
     */
    public Map<String, Component> getAllComponents() {
        createNames();
        Map<String, Component> m = new HashMap<String, Component>(nameComponentMap.size());
        Iterator<String> iterator = nameComponentMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            m.put(key, ((NamedComponent) nameComponentMap.get(key)).getComponent());
        }
        return m;
    }

    private Map<String, NamedComponent> getNameComponentMap() {
        return nameComponentMap;
    }

    public void setUseFieldNames(boolean b) {
        useFieldNames = b;
    }

    private WindowMonitor windowMonitor;

    protected String createName(Component component) {
        if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            if (button.getText() != null && button.getText().length() != 0) {
                return button.getText();
            }
            Icon icon = button.getIcon();
            if (icon != null && icon instanceof ImageIcon) {
                String description = ((ImageIcon) icon).getDescription();
                if (description != null && description.length() != 0)
                    return mapFromImageDescription(description);
            }
        }
        if (component instanceof JComponent) {
            try {
                JLabel label = (JLabel) ((JComponent) component).getClientProperty("labeledBy");
                if (label != null && label.getText() != null) {
                    String name = label.getText().trim();
                    if (name.endsWith(":")) {
                        name = name.substring(0, name.length() - 1).trim();
                    }
                    return name;
                }
            } catch (ClassCastException e) {
                // just continue
            }
            String name = component.getName();
            if (name != null && !name.equals(""))
                return name;
        }
        if (useFieldNames && objectFieldNameMap.get(component) != null)
            return (String) objectFieldNameMap.get(component);
        String name = component.getClass().getName();
        return mapToCommonName(name);
    }

    private String mapToCommonName(String name) {
        int index;
        if ((index = name.lastIndexOf('.')) == -1)
            return name;
        String packageName = name.substring(0, index);
        String className = name.substring(name.lastIndexOf('.') + 1);
        if (packageName.equals("javax.swing") && className.charAt(0) == 'J')
            return className.substring(1);
        return className;
    }

    private String mapFromImageDescription(String description) {
        try {
            String name = new URL(description).getPath();
            if (name.lastIndexOf('/') != -1)
                name = name.substring(name.lastIndexOf('/') + 1);
            if (name.lastIndexOf('.') != -1)
                name = name.substring(0, name.lastIndexOf('.'));
            return name;
        } catch (MalformedURLException e) {
            return description;
        }
    }

    public String getName(Component component) {
        if (component instanceof JInternalFrame || component instanceof Window)
            return getNameForWindow(component);
        String name = internalGetName(component);
        if (name == null && container != null) {
            createNames();
            name = internalGetName(component);
        }
        return name;
    }

    private String getNameForWindow(Component window) {
        String title = getTitle(window);
        int index = 0;
        List<Window> windows = getWindowMonitor().getWindows();
        for (ListIterator<Window> iterator = windows.listIterator(); iterator.hasNext();) {
            Window w = (Window) iterator.next();
            if (w.equals(window))
                break;
            if (getTitle(w).equals(title))
                index++;
        }
        if (index > 0)
            title = title + "(" + index + ")";
        return title;
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
        return title == null ? "" : title;
    }

    public MComponent getPropertyWrapperObject(Component component) {
        return new MComponent(component, getWindowMonitor());
    }

    private WindowMonitor getWindowMonitor() {
        if (windowMonitor == null)
            windowMonitor = WindowMonitor.getInstance();
        return windowMonitor;
    }

    public void saveIfNeeded() {
    }

    public void markUnused(Component object) {
    }

}
