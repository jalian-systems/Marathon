package net.sourceforge.marathon.mpf;

import java.awt.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JOptionPane;

public class ModelInfo extends AbstractListModel implements ComboBoxModel {
    private static final long serialVersionUID = 1L;

    /**
     * Information of plug ins such as ScriptModel, Launcher.
     */
    public static class PlugInModelInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public String className;
    
        public PlugInModelInfo(String description, String className) {
            this.name = description;
            this.className = className;
        }
        
        @Override public String toString() {
            return name;
        }
    }

    private final String pluginName;
    private final Component parent;
    private List<PlugInModelInfo> scriptmodels = new ArrayList<PlugInModelInfo>();
    private Object selectedItem;

    public ModelInfo(String pluginName, Component parent) {
        this.pluginName = pluginName;
        this.parent = parent;
        initializePlugin();
    }

    private void initializePlugin() {
        Enumeration<URL> systemResources = null;
        try {
            systemResources = ClassLoader.getSystemResources(pluginName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "No resource found for " + pluginName + ".", "No " + pluginName + " Support",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
        while (systemResources.hasMoreElements()) {
            Properties props = new Properties();
            try {
                URL url = systemResources.nextElement();
                props.load(url.openStream());
                Set<Entry<Object, Object>> entries = props.entrySet();
                for (Entry<Object, Object> entry : entries) {
                    scriptmodels.add(new PlugInModelInfo((String) entry.getValue(), (String) entry.getKey()));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (scriptmodels.size() == 0) {
            JOptionPane.showMessageDialog(parent, "No Marathon " + pluginName + " found.", "No " + pluginName + " Support",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public PlugInModelInfo getPluginModel(String className) {
        for (int i = 0; i < scriptmodels.size(); i++) {
            if (scriptmodels.get(i).className.equals(className))
                return scriptmodels.get(i);
        }
        return null;
    }

    public int getSize() {
        return scriptmodels.size();
    }

    public Object getElementAt(int index) {
        return scriptmodels.get(index);
    }

    public void setSelectedItem(Object anItem) {
        this.selectedItem = anItem ;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

}