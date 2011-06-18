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
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import net.sourceforge.marathon.Constants;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.representer.Representer;

public class ObjectMapModel implements TreeNode {

    protected List<OMapContainer> data;

    protected boolean dirty = false;

    private final static Logger logger = Logger.getLogger(ObjectMapModel.class.getName());

    public ObjectMapModel() {
        load();
    }

    @SuppressWarnings("unchecked") private void load() {
        try {
            data = (List<OMapContainer>) new Yaml().load(new FileReader(getOMapFile()));
            for (OMapContainer container : data) {
                container.setParent(this);
            }
        } catch (FileNotFoundException e) {
            data = new ArrayList<OMapContainer>();
            dirty = true;
            logger.info("Creating a new ObjectMap");
        }
    }

    public TreeNode getChildAt(int childIndex) {
        if (childIndex < data.size())
            return data.get(childIndex);
        return null;
    }

    public int getChildCount() {
        return data.size();
    }

    public TreeNode getParent() {
        return null;
    }

    public int getIndex(TreeNode node) {
        if (node == null || node.getParent() != this)
            return -1;
        return data.indexOf(node);
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
                return index < data.size();
            }

            public TreeNode nextElement() {
                return data.get(index++);
            }
        };
    }

    public void save() {
        logger.info("Saving object map");
        if (!isRecording()) {
            logger.info("Not in recording mode. Skipping save...");
            return;
        }
        if (!dirty) {
            logger.info("Object map is not modified. Skipping save.");
            return;
        }
        try {
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
            new Yaml(representer, options).dump(data, new FileWriter(getOMapFile()));
            for (OMapContainer container : data) {
                container.save();
            }
        } catch (IOException e) {
            System.err.println("Unable to save object map");
            e.printStackTrace();
        } catch (YAMLException e1) {
            System.err.println("Unable to save object map: " + this);
            e1.printStackTrace();
            throw e1;
        }
        dirty = false;
    }

    private boolean isRecording() {
        return System.getProperty("marathon.mode", "other").equals("recording");
    }

    public File getOMapFile() {
        return new File(System.getProperty(Constants.PROP_PROJECT_DIR),
                System.getProperty(Constants.PROP_OMAP_FILE, Constants.FILE_OMAP));
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean b) {
        dirty = b;
    }

    @Override public String toString() {
        return "{objectMap: " + data + "}";
    }

    public void loadAll() {
        for (OMapContainer container : data) {
            try {
                container.load();
            } catch (FileNotFoundException e) {
                logger.warning("Unable to find file for container: " + container + " Ignoring it.");
                data.remove(container);
            }
        }
    }
}