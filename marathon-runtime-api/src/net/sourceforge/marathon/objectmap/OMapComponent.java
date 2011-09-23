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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.tree.TreeNode;

import net.sourceforge.marathon.component.IPropertyAccessor;

public class OMapComponent implements TreeNode {

    private String name;
    private List<OMapRecognitionProperty> componentRecognitionProperties;
    private List<OMapProperty> generalProperties;
    private TreeNode parent;
    public static final List<String> LAST_RESORT_NAMING_PROPERTIES = new ArrayList<String>();
    public static final List<String> LAST_RESORT_RECOGNITION_PROPERTIES = new ArrayList<String>();

    static {
        OMapComponent.LAST_RESORT_NAMING_PROPERTIES.add("type");
        OMapComponent.LAST_RESORT_NAMING_PROPERTIES.add("indexInContainer");
        OMapComponent.LAST_RESORT_RECOGNITION_PROPERTIES.add("type");
        OMapComponent.LAST_RESORT_RECOGNITION_PROPERTIES.add("indexInContainer");
    }

    static public final Enumeration<TreeNode> EMPTY_ENUMERATION = new Enumeration<TreeNode>() {
        public boolean hasMoreElements() {
            return false;
        }

        public TreeNode nextElement() {
            throw new NoSuchElementException("No more elements");
        }
    };

    public OMapComponent(OMapContainer parent) {
        this.parent = parent;
    }

    public OMapComponent() {
        this(null);
    }

    public List<OMapRecognitionProperty> getComponentRecognitionProperties() {
        return componentRecognitionProperties;
    }

    public void setComponentRecognitionProperties(List<OMapRecognitionProperty> componentRecognitionProperties) {
        this.componentRecognitionProperties = componentRecognitionProperties;
    }

    public List<OMapProperty> getGeneralProperties() {
        return generalProperties;
    }

    public void setGeneralProperties(List<OMapProperty> generalProperties) {
        this.generalProperties = generalProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMatched(IPropertyAccessor pa) {
        for (OMapRecognitionProperty rp : componentRecognitionProperties) {
            if (!rp.isMatch(pa))
                return false;
        }
        return true;
    }

    @Override public String toString() {
        return "[" + name + " " + (componentRecognitionProperties == null ? "" : componentRecognitionProperties) + "]";
    }

    public TreeNode getChildAt(int childIndex) {
        throw new ArrayIndexOutOfBoundsException("node has no children");
    }

    public int getChildCount() {
        return 0;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return -1;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration<TreeNode> children() {
        return EMPTY_ENUMERATION;
    }

    public String findProperty(String property) {
        for (OMapProperty p : generalProperties) {
            if (p.getName().equals(property))
                return p.getValue();
        }
        return null;
    }

    public void addComponentRecognitionProperty(OMapRecognitionProperty property) {
        componentRecognitionProperties.add(property);
    }

    public boolean withLastResortProperties() {
        for (OMapRecognitionProperty p : componentRecognitionProperties) {
            if (!p.getName().equals("type") && !p.getName().equals("indexInContainer"))
                return false ;
        }
        return true;
    }
}