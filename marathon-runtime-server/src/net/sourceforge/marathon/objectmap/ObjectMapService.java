package net.sourceforge.marathon.objectmap;

import java.io.IOException;
import java.util.List;

import net.sourceforge.marathon.component.IPropertyAccessor;
import net.sourceforge.marathon.objectmap.OMapComponent;
import net.sourceforge.marathon.objectmap.OMapContainer;
import net.sourceforge.marathon.objectmap.ObjectMap;
import net.sourceforge.marathon.objectmap.ObjectMapConfiguration;
import net.sourceforge.marathon.objectmap.ObjectMapConfiguration.ObjectIdentity;
import net.sourceforge.marathon.objectmap.ObjectMapException;

public class ObjectMapService implements IObjectMapService {
    
    protected ObjectMapConfiguration configuration = new ObjectMapConfiguration();
    protected ObjectMap objectMap = new ObjectMap();

    public IOMapContainer getTopLevelComponent(IPropertyAccessor pa, List<List<String>> rproperties,
            List<String> gproperties, String title, boolean createIfNeeded) throws ObjectMapException {
        synchronized (objectMap) {
            final OMapContainer topLevelComponent = objectMap.getTopLevelComponent(pa, rproperties, gproperties, title, createIfNeeded);
            return new IOMapContainer() {
                public OMapContainer getOMapContainer(ObjectMap objectMap) {
                    return topLevelComponent;
                }
                
                @Override public String toString() {
                    return topLevelComponent.toString();
                }

                public List<String> getUsedRecognitionProperties() {
                    return topLevelComponent.getUsedRecognitionProperties();
                }
            };
        }
    }


    public IOMapContainer getTopLevelComponent(IPropertyAccessor pa) throws ObjectMapException {
        synchronized (objectMap) {
            final OMapContainer topLevelComponent = objectMap.getTopLevelComponent(pa);
            return new IOMapContainer() {
                public OMapContainer getOMapContainer(ObjectMap objectMap) {
                    return topLevelComponent;
                }
                
                @Override public String toString() {
                    return topLevelComponent.toString();
                }

                public List<String> getUsedRecognitionProperties() {
                    return topLevelComponent.getUsedRecognitionProperties();
                }
            };
        }
    }

    public void save() {
        synchronized (objectMap) {
            objectMap.save();
        }
    }

    public void setDirty(boolean b) {
        synchronized (objectMap) {
            objectMap.setDirty(b);
        }
    }

    public OMapComponent findComponentByName(String name, IOMapContainer container) {
        OMapContainer oMapContainer = container.getOMapContainer(objectMap);
        synchronized (oMapContainer) {
            return objectMap.findComponentByName(name, oMapContainer);
        }
    }

    public OMapComponent findComponentByProperties(IPropertyAccessor w, IOMapContainer container)
            throws ObjectMapException {
        OMapContainer oMapContainer = container.getOMapContainer(objectMap);
        synchronized (oMapContainer) {
            return objectMap.findComponentByProperties(w, oMapContainer);
        }
    }

    public OMapComponent insertNameForComponent(String name, IPropertyAccessor w, List<String> rprops,
            List<List<String>> rproperties, List<List<String>> nproperties, List<String> gproperties, IOMapContainer container) {
        OMapContainer oMapContainer = container.getOMapContainer(objectMap);
        synchronized (oMapContainer) {
            return objectMap.insertNameForComponent(name, w, rprops, rproperties, nproperties, gproperties, oMapContainer);
        }
    }

    public OMapComponent findComponentByProperties(IPropertyAccessor w, List<String> rprops, IOMapContainer container) {
        OMapContainer oMapContainer = container.getOMapContainer(objectMap);
        synchronized (oMapContainer) {
            return objectMap.findComponentByProperties(w, rprops, oMapContainer);
        }
    }

    public List<String> getGeneralProperties() {
        return configuration.getGeneralProperties();
    }

    public void load() throws IOException {
        configuration.load();
    }

    public void markUsed(String name, IOMapContainer topContainer) {
        OMapContainer oMapContainer = topContainer.getOMapContainer(objectMap);
        synchronized (oMapContainer) {
            objectMap.markUsed(name, oMapContainer);
        }
    }

    public List<OMapComponent> findComponentsByProperties(IPropertyAccessor wrapper, IOMapContainer topContainer) {
        OMapContainer oMapContainer = topContainer.getOMapContainer(objectMap);
        synchronized (oMapContainer) {
            return objectMap.findComponentsByProperties(wrapper, oMapContainer);
        }
    }

    public List<ObjectIdentity> getNamingProperties() {
        return configuration.getNamingProperties();
    }

    public List<ObjectIdentity> getRecognitionProperties() {
        return configuration.getRecognitionProperties();
    }

    public List<ObjectIdentity> getContainerNamingProperties() {
        return configuration.getContainerNamingProperties();
    }

    public List<ObjectIdentity> getContainerRecognitionProperties() {
        return configuration.getContainerRecognitionProperties();
    }
}
