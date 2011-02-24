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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

public class PropertyAccessor {
    private static final Pattern LISTACCESSPATTERN = Pattern.compile("(\\w+)|(\\.)|\\[([^\\]]*)\\]");
    final protected static EventQueueRunner eventQueueRunner = new EventQueueRunner();

    public Object getPropertyObject(Object o, String property) {
        Matcher matcher = LISTACCESSPATTERN.matcher(property);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            String indexString = matcher.group(3);
            if (methodName != null) {
                Method m = getAccessMethod(o, methodName);
                if (m == null) {
                    Field f = getAccessField(o, methodName);
                    if (f == null)
                        return null;
                    boolean accessible = f.isAccessible();
                    try {
                        o = f.get(o);
                    } catch (Throwable t) {
                        return null ;
                    } finally {
                        f.setAccessible(accessible);
                    }
                } else {
                    boolean accessible = m.isAccessible();
                    m.setAccessible(true);
                    try {
                        o = eventQueueRunner.invokeMethod(m, o, new Object[] {});
                        if (o.getClass().isArray())
                            o = unboxPremitiveArray(o);
                    } catch (Throwable t) {
                        return null;
                    } finally {
                        m.setAccessible(accessible);
                    }
                }
            } else if (indexString != null) {
                if (o instanceof List)
                    o = getListItem((List<?>) o, indexString);
                else if (o instanceof Map)
                    o = getMapItem((Map<?, ?>) o, indexString);
                else
                    o = null;
            }
            if (o == null)
                return null;
        }
        return o;
    }

    private Field getAccessField(Object o, String methodName) {
        try {
            return o.getClass().getDeclaredField(methodName);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        }
        return null ;
    }

    private Object getMapItem(Map<?, ?> m, String indexString) {
        Iterator<?> iterator = m.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<?, ?> e = (Entry<?, ?>) iterator.next();
            if (indexString.equals(e.getKey().toString()))
                return e.getValue();
        }
        return null;
    }

    private Object getListItem(List<?> l, String indexString) {
        int index = Integer.parseInt(indexString);
        Object o = null;
        if (index < l.size())
            o = l.get(index);
        return o;
    }

    private Method getAccessMethod(Object o, String name) {
        try {
            return o.getClass().getMethod(name, new Class[] {});
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            name = camelCase(name);
            try {
                return o.getClass().getMethod("get" + name, new Class[] {});
            } catch (SecurityException e1) {
            } catch (NoSuchMethodException e1) {
                try {
                    return o.getClass().getMethod("is" + name, new Class[] {});
                } catch (SecurityException e2) {
                } catch (NoSuchMethodException e2) {
                }
            }
        }
        return null;
    }

    private String camelCase(String name) {
        char first = name.charAt(0);
        if (Character.isUpperCase(first))
            return name;
        return "" + Character.toUpperCase(first) + name.substring(1);
    }

    /**
     * The default behavior for {@link JComponent#toString()} is to give the
     * class name and parameters in string format. here we try to remove the
     * class name so that the string is more readable.
     * 
     * @param object
     * @return
     */
    public static String removeClassName(Object object) {
        if (object == null)
            return "null";
        if (object.getClass().isArray()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[");
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                buffer.append(removeClassName(Array.get(object, i)));
                if (i != length - 1)
                    buffer.append(", ");
            }
            buffer.append("]");
            return buffer.toString();
        }
        if (object.getClass().isPrimitive() || object instanceof String)
            return object.toString();
        try {
            return object.toString().replaceFirst(object.getClass().getName(), "");
        } catch (Throwable t) {
            return object.toString();
        }
    }

    public static Object unboxPremitiveArray(Object r) {
        int length = Array.getLength(r);
        ArrayList<Object> list = new ArrayList<Object>();
        for (int i = 0; i < length; i++) {
            Object e = Array.get(r, i);
            if (e != null && e.getClass().isArray())
                list.add(unboxPremitiveArray(e));
            else
                list.add(e);
        }
        return list;
    }

}
