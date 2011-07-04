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
package net.sourceforge.marathon.recorder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.component.MComponent.AssertPropertyInstance;

public class AssertionTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;
    private final Object object;

    private final String property;

    public AssertionTreeNode(Object object) {
        this(object, null);
    }

    private AssertionTreeNode(Object object, String property) {
        super(new Object[] { object, property });
        this.object = object;
        this.property = property;
    }

    public boolean isLeaf() {
        return isPrimitive(object);
    }

    private boolean isPrimitive(Object object) {
        return object == null || object.getClass() == Boolean.class || object.getClass() == Character.class
                || object.getClass() == Byte.class || object.getClass() == Short.class || object.getClass() == Integer.class
                || object.getClass() == Long.class || object.getClass() == Float.class || object.getClass() == Double.class
                || object.getClass() == Void.class || object.getClass() == String.class;
    }

    public int getChildCount() {
        if (isLeaf())
            return 0;
        if (object instanceof List)
            return ((List<?>) object).size() + 1;
        if (object instanceof Map)
            return ((Map<?, ?>) object).size() + 1;
        if (object instanceof MComponent)
            return ((MComponent) object).getMethods().size();
        return getMethods(object).size();
    }

    public TreeNode getChildAt(int index) {
        if (object instanceof List)
            return getNodeForList((List<?>) object, index);
        if (object instanceof Map)
            return getNodeForMap((Map<?, ?>) object, index);
        Method method;
        if (object instanceof MComponent) {
            Object o = ((MComponent) object).getMethods().get(index);
            if (o instanceof AssertPropertyInstance)
                return getNodeForAssertPropertyInstance((AssertPropertyInstance) o);
            method = (Method) o;
        } else
            method = (Method) getMethods(object).get(index);
        return getNodeForMethod(method);
    }

    private TreeNode getNodeForAssertPropertyInstance(AssertPropertyInstance o) {
        return getNewNode(o.value, o.prop.displayName);
    }

    private TreeNode getNodeForMap(Map<?, ?> map, int index) {
        if (index == 0)
            return getNewNode(Integer.valueOf(map.size()), "size");
        Entry<?, ?> entry = (Entry<?, ?>) map.entrySet().toArray()[index - 1];
        return getNewNode(entry.getValue(), "[" + entry.getKey().toString() + "]");
    }

    private TreeNode getNodeForList(List<?> l, int index) {
        if (index == 0)
            return getNewNode(Integer.valueOf(l.size()), "size");
        return getNewNode(l.get(index - 1), "[" + (index - 1) + "]");
    }

    private TreeNode getNodeForMethod(Method method) {
        Object r = null;
        try {
            method.setAccessible(true);
            r = method.invoke(object, new Object[] {});
        } catch (Throwable t) {
            t.printStackTrace();
        }
        String p = getPropertyName(method.getName());
        return getNewNode(r, p);
    }

    private TreeNode getNewNode(Object r, String p) {
        if (r != null && r.getClass().isArray()) {
            r = MComponent.unboxPremitiveArray(r);
        } else if (r instanceof Collection) {
            r = MComponent.unboxPremitiveArray(((Collection<?>) r).toArray());
        }
        AssertionTreeNode node = new AssertionTreeNode(r, p);
        if (isPrimitive(r))
            node.setAllowsChildren(false);
        return node;
    }

    private String getPropertyName(String name) {
        if (name.startsWith("is"))
            return name.substring(2);
        return name.substring(3);
    }

    private boolean isValidMethod(Method method) {
        String name = method.getName();
        if ((name.startsWith("get") || name.startsWith("is")) && method.getParameterTypes().length == 0
                && !method.getName().equals("getClass")) {
            return true;
        }
        return false;
    }

    private ArrayList<Method> getMethods(Object object) {
        ArrayList<Method> list = new ArrayList<Method>();
        Method[] methods = object.getClass().getMethods();
        sort(methods);
        for (int i = 0; i < methods.length; i++) {
            if (isValidMethod(methods[i]))
                list.add(methods[i]);
        }
        return list;
    }

    private void sort(Method[] methods) {
        Arrays.sort(methods, new Comparator<Object>() {

            public int compare(Object obj1, Object obj2) {
                if (obj1 instanceof Method && obj2 instanceof Method) {
                    Method m1 = (Method) obj1;
                    Method m2 = (Method) obj2;
                    return m1.getName().compareTo(m2.getName());
                }
                return 0;
            }

        });
    }

    public String getDisplayNode() {
        String d = object == null ? "null" : getObjectRepr();
        if (d.length() > 60)
            d = d.substring(0, 56) + "...";
        return d;
    }

    public String getDisplayValue() {
        return object == null ? "null" : getObjectRepr();
    }

    public String getValue() {
        return object == null ? null : getObjectRepr();
    }

    public String getProperty() {
        return property;
    }

    private String getObjectRepr() {
        return MComponent.removeClassName(object);
    }

    public Object getObject() {
        return object;
    }
}
