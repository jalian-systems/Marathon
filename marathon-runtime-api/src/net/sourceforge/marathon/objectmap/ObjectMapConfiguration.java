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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import net.sourceforge.marathon.Constants;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.JavaBeanDumper;
import org.yaml.snakeyaml.JavaBeanLoader;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.representer.Representer;

public class ObjectMapConfiguration {

    private static Logger logger = Logger.getLogger(ObjectMapConfiguration.class.getName());

    public static class PropertyList {
        private List<String> properties;
        private int priority;

        public PropertyList() {
        }

        public List<String> getProperties() {
            return properties;
        }

        public void setProperties(List<String> properties) {
            this.properties = properties;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public static PropertyList create(int priority, String... properties) {
            PropertyList pl = new PropertyList();
            pl.priority = priority;
            pl.properties = Arrays.asList(properties);
            return pl;
        }

        @Override public String toString() {
            return properties + " " + priority;
        }
    }

    public static class ObjectIdentity {
        private String className;
        private List<PropertyList> propertyLists;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<PropertyList> getPropertyLists() {
            return propertyLists;
        }

        public void setPropertyLists(List<PropertyList> propertyLists) {
            this.propertyLists = propertyLists;
        }

        public void addPropertyList(PropertyList pl) {
            if (propertyLists == null)
                propertyLists = new ArrayList<PropertyList>();
            propertyLists.add(pl);
        }

        @Override public String toString() {
            return "[" + className + " " + propertyLists + "]";
        }
    }

    private List<ObjectIdentity> namingProperties;
    private List<ObjectIdentity> recognitionProperties;
    private List<String> generalProperties;
    private List<ObjectIdentity> containerNamingProperties;
    private List<ObjectIdentity> containerRecognitionProperties;

    public List<ObjectIdentity> getNamingProperties() {
        return namingProperties;
    }

    public void setNamingProperties(List<ObjectIdentity> namingProperties) {
        this.namingProperties = namingProperties;
    }

    public List<ObjectIdentity> getRecognitionProperties() {
        return recognitionProperties;
    }

    public void setRecognitionProperties(List<ObjectIdentity> recognitionProperties) {
        this.recognitionProperties = recognitionProperties;
    }

    public List<String> getGeneralProperties() {
        return generalProperties;
    }

    public void setGeneralProperties(List<String> generalProperties) {
        this.generalProperties = generalProperties;
    }

    public List<ObjectIdentity> getContainerNamingProperties() {
        return containerNamingProperties;
    }

    public void setContainerNamingProperties(List<ObjectIdentity> containerNamingProperties) {
        this.containerNamingProperties = containerNamingProperties;
    }

    public List<ObjectIdentity> getContainerRecognitionProperties() {
        return containerRecognitionProperties;
    }

    public void setContainerRecognitionProperties(List<ObjectIdentity> containerRecognitionProperties) {
        this.containerRecognitionProperties = containerRecognitionProperties;
    }

    public void createDefault() {
        logger.info("Creating a default object map configuration. Loading from stream...");
        Reader reader = new InputStreamReader(ObjectMapConfiguration.class.getResourceAsStream("default-omap-configuration.yaml"));
        load(reader);
    }

    public List<List<String>> findNamingProperties(Component c) {
        return findProperties(c.getClass(), namingProperties);
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

    public List<List<String>> findRecognitionProperties(Component c) {
        return findProperties(c.getClass(), recognitionProperties);
    }

    public List<List<String>> findContainerNamingProperties(Component c) {
        return findProperties(c.getClass(), containerNamingProperties);
    }

    public List<List<String>> findContainerRecognitionProperties(Component c) {
        return findProperties(c.getClass(), containerRecognitionProperties);
    }

    public void load() throws IOException {
        try {
            FileReader reader = new FileReader(getConfigFile());
            load(reader);
        } catch (IOException e) {
            createDefault();
            save();
            FileReader reader = new FileReader(getConfigFile());
            load(reader);
        }
    }

    public void save() throws IOException {
        FileWriter writer = new FileWriter(getConfigFile());
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.AUTO);
        options.setIndent(4);
        Representer representer = new Representer();
        representer.getPropertyUtils().setBeanAccess(BeanAccess.DEFAULT);
        JavaBeanDumper dumper = new JavaBeanDumper(representer, options);
        dumper.dump(this, writer);
    }

    public File getConfigFile() {
        return new File(System.getProperty(Constants.PROP_PROJECT_DIR), System.getProperty(Constants.PROP_OMAP_CONFIGURATION_FILE,
                Constants.FILE_OMAP_CONFIGURATION));
    }

    private void load(Reader reader) {
        JavaBeanLoader<ObjectMapConfiguration> loader = new JavaBeanLoader<ObjectMapConfiguration>(ObjectMapConfiguration.class);
        ObjectMapConfiguration configuration = loader.load(reader);
        namingProperties = configuration.namingProperties;
        containerNamingProperties = configuration.containerNamingProperties;
        recognitionProperties = configuration.recognitionProperties;
        containerRecognitionProperties = configuration.containerRecognitionProperties;
        generalProperties = configuration.generalProperties;
    }

}
