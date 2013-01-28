package net.sourceforge.marathon.objectmap;

import java.util.List;

import net.sourceforge.marathon.objectmap.OMapContainer;
import net.sourceforge.marathon.objectmap.ObjectMap;

public interface IOMapContainer {

    public abstract OMapContainer getOMapContainer(ObjectMap objectMap);

    public abstract List<String> getUsedRecognitionProperties();

}
