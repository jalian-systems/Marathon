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
package net.sourceforge.marathon.display;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sourceforge.marathon.util.AbstractSimpleAction;

public class ActionInjector {

    private final Object o;

    public ActionInjector(Object o) {
        this.o = o;
    }

    public void injectActions() {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                ISimpleAction annotation = field.getAnnotation(ISimpleAction.class);
                if (annotation == null)
                    continue;
                AbstractSimpleAction action = createAction(field.getName(), annotation);
                field.set(o, action);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    private AbstractSimpleAction createAction(String fieldName, ISimpleAction annotation) throws SecurityException,
            NoSuchMethodException {
        String name = findName(fieldName);
        String commandName = annotation.value();
        if (commandName.equals("")) {
            commandName = expand(name);
        }
        char mneumonic = annotation.mneumonic();
        String action = annotation.action();
        if (action.equals(""))
            action = findAction(name);
        final Method m = findMethod(action);
        String iconS = annotation.icon();
        if (iconS.equals(""))
            iconS = name;
        Icon icon = findEnabledIcon(iconS);
        String iconDisabledS = annotation.iconDisabled();
        if (iconDisabledS.equals(""))
            iconDisabledS = name;
        Icon iconDisabled = findDisabledIcon(iconDisabledS);
        String description = annotation.description();
        if (description.equals(""))
            description = commandName;
        return new AbstractSimpleAction(commandName, description, mneumonic, icon, iconDisabled) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                try {
                    m.invoke(o);
                } catch (IllegalArgumentException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }
        };
    }

    private String expand(String fieldName) {
        StringBuffer sb = new StringBuffer();
        char[] charArray = fieldName.substring(1).toCharArray();
        sb.append(Character.toUpperCase(fieldName.charAt(0)));
        for (char c : charArray) {
            if (Character.isUpperCase(c))
                sb.append(' ');
            sb.append(c);
        }
        return sb.toString();
    }

    private String findName(String fieldName) {
        if (fieldName.endsWith("Action"))
            fieldName = fieldName.substring(0, fieldName.length() - 6);
        return fieldName;
    }

    private Icon findDisabledIcon(String name) {
        Icon icon = findIcon(disabledIconPath(), name);
        if (icon == null)
            return findIcon(disabledIconPath(), "empty");
        return icon;
    }

    private String disabledIconPath() {
        return o.getClass().getPackage().getName().replace('.', '/') + "/icons/disabled/";
    }

    private Icon findEnabledIcon(String name) {
        Icon icon = findIcon(enabledIconPath(), name);
        if (icon == null)
            return findIcon(enabledIconPath(), "empty");
        return icon;
    }

    private String enabledIconPath() {
        return o.getClass().getPackage().getName().replace('.', '/') + "/icons/enabled/";
    }

    private Icon findIcon(String path, String name) {
        URL resource = this.getClass().getClassLoader().getResource(path + name.toLowerCase() + ".gif");
        if (resource == null)
            return null;
        return new ImageIcon(resource);
    }

    private Method findMethod(String action) throws SecurityException, NoSuchMethodException {
        return o.getClass().getMethod(action);
    }

    private String findAction(String fieldName) {
        return "on" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

}
