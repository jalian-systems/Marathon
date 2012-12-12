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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.component.IPropertyAccessor;

import org.junit.Before;
import org.junit.Test;

public class TestObjectMap {

    private final class MockPropertyAccessor implements IPropertyAccessor {
        private List<String> values;
        private List<String> properties;

        public MockPropertyAccessor() {
        }

        public String getProperty(String name) {
            if (values == null || properties == null)
                return null;
            int index = properties.indexOf(name);
            if (index != -1)
                return values.get(index);
            return null;
        }

        public void setProperties(List<String> properties, List<String> values) {
            this.properties = properties;
            this.values = values;
        }

        public boolean isMatched(String method, String name, String value) {
            return false;
        }

    }

    private ObjectMap objectMap;
    private MockPropertyAccessor accessor;

    @Before public void setup() {
        System.setProperty(Constants.PROP_PROJECT_DIR, ".");
        File file = new File(System.getProperty(Constants.PROP_PROJECT_DIR), System.getProperty(Constants.PROP_OMAP_FILE, Constants.FILE_OMAP));
        file.delete();
        file.deleteOnExit();
        objectMap = new ObjectMap();
        accessor = new MockPropertyAccessor();
    }

    @SuppressWarnings("unchecked") @Test public void testGetTopLevelComponentThrowsAnExceptionWhenMoreThanOneComponentMatches() throws Exception {
        accessor.setProperties(cLS("class", "title"), cLS("ProxyDialog", "title"));
        objectMap.getTopLevelComponent(accessor, cLLS(cLS("class", "title")), cLS(), "");
        accessor.setProperties(cLS("class"), cLS("ProxyDialog"));
        objectMap.getTopLevelComponent(accessor, cLLS(cLS("class")), cLS(), "");
        objectMap.getTopLevelComponent(accessor, cLLS(cLS("class")), cLS(), "");
    }

    @SuppressWarnings("unchecked") @Test public void testFindComponentByName() throws Exception {
        accessor.setProperties(cLS("class", "title"), cLS("ProxyDialog", "title"));
        objectMap.getTopLevelComponent(accessor, cLLS(cLS("class", "title")), cLS(), "");
        OMapContainer container = objectMap.getTopLevelComponent(accessor, cLLS(cLS("class", "title")), cLS(), "");
        accessor.setProperties(cLS("name"), cLS("testObjectName"));
        objectMap.insertNameForComponent("testObject", accessor, cLS("name"), cLLS(cLS("name")), cLLS(), cLS(), container);
        OMapComponent omapComponent = objectMap.findComponentByName("testObject", container);
        assertNotNull(omapComponent);
        assertEquals("testObject", omapComponent.getName());
    }

    @Test public void testFindComponentName() {
    }

    @Test public void testInsertNameForComponent() {
    }

    private List<List<String>> cLLS(List<String>... lists) {
        return Arrays.asList(lists);
    }

    private List<String> cLS(String... strings) {
        return Arrays.asList(strings);
    }

}
