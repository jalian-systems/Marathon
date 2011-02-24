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
package net.sourceforge.marathon.util;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class PropertyList {
    class Property {
        private String key;
        private String description;
        private String value;
        private Class<?> klass;
        private String[] items;

        Property(String key, String description, String value, Class<?> klass) {
            this.key = key;
            this.description = description;
            this.value = value;
            this.klass = klass;
        }

        public Property(String key, String description, String value, Class<String[]> klass, String[] items) {
            this(key, description, value, klass);
            this.items = items;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public String getValue() {
            return value;
        }

        public Class<?> getKlass() {
            return klass;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String[] getItems() {
            return items;
        }
    }

    private ArrayList<Property> propList;

    public PropertyList() {
        propList = new ArrayList<Property>();
    }

    private void addProperty(String key, String description, String value, Class<?> klass) {
        propList.add(new Property(key, description, value, klass));
    }

    public void addColorProperty(String key, String description, Color color) {
        String value = "#" + Integer.toHexString((color.getRGB() & 0x00FFFFFF) | 0x1000000).substring(1);
        addProperty(key, description, value, Color.class);
    }

    public void addStringProperty(String key, String description, String value) {
        addProperty(key, description, value, String.class);
    }

    public void addFontProperty(String key, String description, Font font) {
        addProperty(key, description, font.getFontName(), Font.class);
    }

    public Property getProperty(String key) {
        for (Property prop : propList) {
            if (prop.getKey().equals(key))
                return prop;
        }
        return null;
    }

    public int getSize() {
        return propList.size();
    }

    public Property getProperty(int index) {
        return propList.get(index);
    }

    public void addIntegerProperty(String key, String description, int value) {
        addProperty(key, description, value + "", Integer.class);
    }

    public void addBooleanProperty(String key, String description, boolean value) {
        addProperty(key, description, value + "", Boolean.class);
    }

    public void addSelectionProperty(String key, String description, String value, String[] items) {
        addProperty(key, description, value, String[].class, items);
    }

    private void addProperty(String key, String description, String value, Class<String[]> klass, String[] items) {
        propList.add(new Property(key, description, value, klass, items));
    }

    public List<Property> getProperties() {
        return propList;
    }
}
