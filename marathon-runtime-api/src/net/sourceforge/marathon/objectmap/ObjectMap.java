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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.sourceforge.marathon.component.IPropertyAccessor;

public class ObjectMap extends ObjectMapModel {

    private static final Logger logger = Logger.getLogger(ObjectMap.class.getName());

    private static final List<String> LAST_RESORT_PROPERTIES = new ArrayList<String>();

    static {
        LAST_RESORT_PROPERTIES.add("component.class.name");
        LAST_RESORT_PROPERTIES.add("title");
    }
    
    public ObjectMap() {
    }

    public OMapContainer getTopLevelComponent(IPropertyAccessor pa, List<List<String>> rproperties, List<String> gproperties,
            String title) throws ObjectMapException {
        OMapContainer currentContainer ;
        List<OMapContainer> matched = new ArrayList<OMapContainer>();
        for (OMapContainer com : data) {
            if (com.isMatched(pa))
                matched.add(com);
        }
        if (matched.size() == 1) {
            currentContainer = matched.get(0);
            try {
                currentContainer.load();
                logger.info("Setting current container to: " + currentContainer);
            } catch (FileNotFoundException e) {
                logger.warning("File not found for container: " + title + ". Recreating object map file for container.");
                data.remove(currentContainer);
                currentContainer = createNewContainer(pa, rproperties, gproperties, title);
            }
        } else if (matched.size() == 0) {
            currentContainer = createNewContainer(pa, rproperties, gproperties, title);
        } else
            throw new ObjectMapException("More than one toplevel container matched for given properties");
        currentContainer.addTitle(title);
        return currentContainer;
    }

    private OMapContainer createNewContainer(IPropertyAccessor pa, List<List<String>> rproperties, List<String> gproperties, String title) {
        OMapContainer container = new OMapContainer(this, title);
        dirty = true;
        data.add(container);
        List<OMapRecognitionProperty> toplevelContainer = createPropertyList(pa, rproperties);
        container.setContainerRecognitionProperties(toplevelContainer);
        List<OMapProperty> generalProperties = getGeneralProperties(pa, gproperties, rproperties);
        container.setContainerGeneralProperties(generalProperties);
        logger.info("Created a new container: " + container);
        return container;
    }

    private List<OMapProperty> getGeneralProperties(IPropertyAccessor pa, List<String> gproperties, List<List<String>> rproperties) {
        ArrayList<OMapProperty> gprops = new ArrayList<OMapProperty>();
        Set<String> props = new HashSet<String>();
        props.addAll(gproperties);
        props.add("instanceOf");
        props.add("component.class.simpleName");
        props.add("component.class.name");
        props.add("oMapClassName");
        props.add("oMapClassSimpleName");
        for (List<String> list : rproperties) {
            for (String string : list) {
                props.add(string);
            }
        }
        for (String gprop : props) {
            String gpropValue = pa.getProperty(gprop);
            if (gpropValue != null) {
                OMapProperty o = new OMapProperty();
                o.setName(gprop);
                o.setValue(gpropValue);
                gprops.add(o);
            }
        }
        return gprops;
    }

    private List<OMapRecognitionProperty> createPropertyList(IPropertyAccessor pa, List<List<String>> properties) {
        List<OMapRecognitionProperty> omrpl = new ArrayList<OMapRecognitionProperty>();
        for (List<String> proplist : properties) {
            if (validProperties(pa, proplist)) {
                copyProperties(pa, proplist, omrpl);
                return omrpl;
            }
        }
        copyProperties(pa, LAST_RESORT_PROPERTIES, omrpl);
        return omrpl;
    }

    private void copyProperties(IPropertyAccessor pa, List<String> proplist, List<OMapRecognitionProperty> omrpl) {
        for (String p : proplist) {
            OMapRecognitionProperty omrp = new OMapRecognitionProperty();
            omrp.setMethod(IPropertyAccessor.METHOD_EQUALS);
            omrp.setName(p);
            omrp.setValue(pa.getProperty(p));
            omrpl.add(omrp);
        }
    }

    private boolean validProperties(IPropertyAccessor pa, List<String> proplist) {
        for (String p : proplist) {
            if (pa.getProperty(p) == null)
                return false;
        }
        return true;
    }

    public OMapComponent findComponentByName(String name, OMapContainer currentContainer) {
        OMapComponent omapComponent = currentContainer.findComponentByName(name);
        logger.info("findComponentByName(" + name + "): " + omapComponent);
        return omapComponent;
    }

    public OMapComponent findComponentByProperties(IPropertyAccessor w, OMapContainer currentContainer) throws ObjectMapException {
        OMapComponent omapComponent = currentContainer.findComponentByProperties(w);
        logger.info("findComponentByProperties(" + w.getProperty("component.class.name") + "): " + omapComponent);
        return omapComponent;
    }

    public OMapComponent insertNameForComponent(String name, IPropertyAccessor w, List<String> rprops,
            List<List<String>> rproperties, List<List<String>> nproperties, List<String> gproperties, OMapContainer currentContainer) {
        logger.info("insertNameForComponent(" + name + "): with container index: " + w.getProperty("indexInContainer"));
        OMapComponent omapComponent = currentContainer.insertNameForComponent(name, w, rprops, rproperties, nproperties,
                gproperties);
        logger.info("insertNameForComponent(" + name + "): " + omapComponent);
        if (omapComponent != null)
            dirty = true;
        return omapComponent;
    }

    public OMapComponent findComponentByProperties(IPropertyAccessor w, List<String> rprops, OMapContainer currentContainer) {
        OMapComponent omapComponent = currentContainer.findComponentByProperties(w, rprops);
        logger.info("findComponentByProperties(" + w.getProperty("component.class.name") + ", " + rprops + "): " + omapComponent);
        return omapComponent;
    }

    public void updateComponent(OMapComponent omapComponent, List<String> rprops, OMapContainer currentContainer) {
        logger.info("updateComponent(" + omapComponent.getName() + "): with properties: " + rprops);
        currentContainer.updateComponent(omapComponent, rprops);
    }

}