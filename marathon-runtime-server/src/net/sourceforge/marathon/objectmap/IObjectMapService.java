package net.sourceforge.marathon.objectmap;

import java.io.IOException;
import java.util.List;

import net.sourceforge.marathon.component.IPropertyAccessor;
import net.sourceforge.marathon.objectmap.OMapComponent;
import net.sourceforge.marathon.objectmap.ObjectMapException;
import net.sourceforge.marathon.objectmap.ObjectMapConfiguration.ObjectIdentity;

public interface IObjectMapService {

    public abstract IOMapContainer getTopLevelComponent(IPropertyAccessor pa, List<List<String>> rproperties,
            List<String> gproperties, String title, boolean createIfNeeded) throws ObjectMapException;

    public abstract IOMapContainer getTopLevelComponent(IPropertyAccessor mcontainer) throws ObjectMapException;

    public abstract void save();

    public abstract void setDirty(boolean b);

    public abstract OMapComponent findComponentByName(String name, IOMapContainer currentContainer);

    public abstract OMapComponent findComponentByProperties(IPropertyAccessor w, IOMapContainer currentContainer)
            throws ObjectMapException;

    public abstract OMapComponent insertNameForComponent(String name, IPropertyAccessor w, List<String> rprops,
            List<List<String>> rproperties, List<List<String>> nproperties, List<String> gproperties,
            IOMapContainer currentContainer);

    public abstract OMapComponent findComponentByProperties(IPropertyAccessor w, List<String> rprops,
            IOMapContainer currentContainer);

    public abstract List<String> getGeneralProperties();

    public List<ObjectIdentity> getNamingProperties();

    public List<ObjectIdentity> getRecognitionProperties();

    public List<ObjectIdentity> getContainerNamingProperties();

    public List<ObjectIdentity> getContainerRecognitionProperties();
    
    public abstract void load() throws IOException;

    public abstract void markUsed(String name, IOMapContainer topContainer);

    public abstract List<OMapComponent> findComponentsByProperties(IPropertyAccessor wrapper, IOMapContainer topContainer);

}