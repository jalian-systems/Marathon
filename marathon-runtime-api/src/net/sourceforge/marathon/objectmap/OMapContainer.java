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

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.component.IPropertyAccessor;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.representer.Representer;

public class OMapContainer implements TreeNode {

    private List<OMapRecognitionProperty> containerRecognitionProperties;
    private List<OMapProperty> containerGeneralProperties;
    private List<String> containerTitles;
    private String fileName;
    private List<OMapComponent> components;

    private Map<String, OMapComponent> nameComponentMap;
    private TreeNode parent;
    private boolean loaded;

    private static final Logger logger = Logger.getLogger(OMapContainer.class.getName());

    public OMapContainer(ObjectMapModel parent, String title) {
        this.parent = parent;
        containerTitles = new ArrayList<String>();
        components = new ArrayList<OMapComponent>();
        nameComponentMap = new HashMap<String, OMapComponent>();
        if (parent != null) {
            fileName = createFileName(title);
            loaded = true;
        }
    }

    private String createFileName(String title) {
        try {
            if (title.length() < 3)
                title = title + "___" ;
            else if (title.length() > 64)
                title = title.substring(0, 64);
            return File.createTempFile(sanitize(title) + "_", ".yaml", omapDirectory()).getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitize(String title) {
        StringBuilder sb = new StringBuilder();
        char[] cs = title.toCharArray();
        for (char c : cs) {
            if (!valid(c))
                c = '_' ;
            sb.append(c);
                    
        }
        return sb.toString();
    }

    private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"',
    ':' };

    private boolean valid(char c) {
        for (char ic : ILLEGAL_CHARACTERS) {
            if (c == ic)
                return false ;
        }
        return true;
    }

    private File omapDirectory() {
        File omapDirectory = new File(Constants.getMarathonProjectDirectory(), System.getProperty(Constants.PROP_OMAP_DIR,
                Constants.DIR_OMAP));
        if (!omapDirectory.exists())
            omapDirectory.mkdirs();
        return omapDirectory;
    }

    public OMapContainer() {
        this(null, "");
    }

    public List<OMapRecognitionProperty> getContainerRecognitionProperties() {
        return containerRecognitionProperties;
    }

    public List<OMapProperty> getContainerGeneralProperties() {
        return containerGeneralProperties;
    }

    public List<String> getContainerTitles() {
        return containerTitles;
    }

    public void setContainerRecognitionProperties(List<OMapRecognitionProperty> toplevelContainer) {
        this.containerRecognitionProperties = toplevelContainer;
    }

    public void setContainerGeneralProperties(List<OMapProperty> containerGeneralProperties) {
        this.containerGeneralProperties = containerGeneralProperties;
    }

    public void setContainerTitles(List<String> containerTitles) {
        this.containerTitles = containerTitles;
    }

    public void addTitle(String title) {
        if (containerTitles.contains(title))
            return;
        containerTitles.add(title);
    }

    private void createMap() {
        nameComponentMap = new HashMap<String, OMapComponent>();
        for (OMapComponent omapComponent : components) {
            nameComponentMap.put(omapComponent.getName(), omapComponent);
        }
    }

    public boolean isMatched(IPropertyAccessor pa) {
        for (OMapRecognitionProperty rp : containerRecognitionProperties) {
            if (!rp.isMatch(pa))
                return false;
        }
        return true;
    }

    public OMapComponent insertNameForComponent(String name, IPropertyAccessor w, List<String> rprops,
            List<List<String>> rproperties, List<List<String>> nproperties, List<String> gproperties) {
        OMapComponent omapComponent = new OMapComponent(this);
        omapComponent.setName(name);
        List<OMapRecognitionProperty> omapRProps = new ArrayList<OMapRecognitionProperty>();
        for (String rprop : rprops) {
            OMapRecognitionProperty rproperty = new OMapRecognitionProperty();
            rproperty.setName(rprop);
            rproperty.setMethod(OMapRecognitionProperty.METHOD_EQUALS);
            rproperty.setValue(w.getProperty(rprop));
            omapRProps.add(rproperty);
        }
        omapComponent.setComponentRecognitionProperties(omapRProps);
        List<OMapProperty> others = new ArrayList<OMapProperty>();
        List<String> otherProps = flattenLists(rprops, rproperties, nproperties, gproperties);
        for (String otherProp : otherProps) {
            String v = w.getProperty(otherProp);
            if (v != null) {
                OMapProperty p = new OMapProperty();
                p.setName(otherProp);
                p.setValue(v);
                others.add(p);
            }
        }
        omapComponent.setGeneralProperties(others);
        components.add(omapComponent);
        nameComponentMap.put(name, omapComponent);
        return omapComponent;
    }

    private List<String> flattenLists(List<String> rpropList, List<List<String>> rproperties, List<List<String>> nproperties,
            List<String> gproperties) {
        Set<String> props = new HashSet<String>();
        for (List<String> nprops : nproperties) {
            for (String nprop : nprops) {
                props.add(nprop);
            }
        }
        for (List<String> rprops : rproperties) {
            for (String rprop : rprops) {
                props.add(rprop);
            }
        }
        for (String gprop : gproperties) {
            props.add(gprop);
        }
        props.add("instanceOf");
        props.add("component.class.name");
        props.add("oMapClassName");
        props.add("component.class.simpleName");
        return new ArrayList<String>(props);
    }

    public OMapComponent findComponentByName(String name) {
        return nameComponentMap.get(name);
    }

    public OMapComponent findComponentByProperties(IPropertyAccessor w) throws ObjectMapException {
        List<OMapComponent> matched = new ArrayList<OMapComponent>();
        for (OMapComponent omapComponent : components) {
            if (omapComponent.isMatched(w))
                matched.add(omapComponent);
        }
        if (matched.size() == 1)
            return matched.get(0);
        else if (matched.size() == 0)
            return null;
        throw new ObjectMapException("More than one component matched: " + matched);
    }

    @Override public String toString() {
        return containerRecognitionProperties == null ? null : containerRecognitionProperties.toString() + components;
    }

    public void setParent(TreeNode objectMapModel) {
        this.parent = objectMapModel;
    }

    public TreeNode getChildAt(int childIndex) {
        if (childIndex < components.size())
            return components.get(childIndex);
        return null;
    }

    public int getChildCount() {
        return components.size();
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        if (node == null || node.getParent() != this)
            return -1;
        return components.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return false;
    }

    public Enumeration<TreeNode> children() {
        return new Enumeration<TreeNode>() {
            int index = 0;

            public boolean hasMoreElements() {
                return index < components.size();
            }

            public TreeNode nextElement() {
                return components.get(index++);
            }
        };
    }

    public String findProperty(String property) {
        for (OMapProperty p : containerGeneralProperties) {
            if (p.getName().equals(property))
                return p.getValue();
        }
        return null;
    }

    public void addContainerRecognitionProperty(OMapRecognitionProperty property) {
        containerRecognitionProperties.add(property);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void save() throws IOException {
        logger.info("Saving object map container " + containerRecognitionProperties);
        if (components.size() == 0) {
            logger.info("Nothing to save. skipping...");
            return;
        }
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(FlowStyle.AUTO);

        Representer representer = new Representer() {
            @Override protected Set<Property> getProperties(Class<? extends Object> type) throws IntrospectionException {
                Set<Property> properties = super.getProperties(type);
                Property parentProperty = null;
                for (Property property : properties) {
                    if (property.getName().equals("parent"))
                        parentProperty = property;
                }
                if (parentProperty != null)
                    properties.remove(parentProperty);
                return properties;
            }
        };
        new Yaml(representer, options).dump(components, new FileWriter(new File(omapDirectory(), fileName)));
    }

    @SuppressWarnings("unchecked") public void load() throws FileNotFoundException {
        if (loaded)
            return;
        logger.info("Loading container from " + fileName);
        components = (List<OMapComponent>) new Yaml().load(new FileReader(new File(omapDirectory(), fileName)));
        for (OMapComponent container : components) {
            container.setParent(this);
        }
        createMap();
        loaded = true;
    }

    public void removeBinding(String name) {
        OMapComponent oMapComponent = nameComponentMap.get(name);
        if (oMapComponent == null)
            return;
        nameComponentMap.remove(name);
        components.remove(oMapComponent);
    }

}