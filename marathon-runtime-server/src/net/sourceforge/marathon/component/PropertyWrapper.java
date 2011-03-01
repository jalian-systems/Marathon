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
package net.sourceforge.marathon.component;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sourceforge.marathon.recorder.WindowMonitor;

public final class PropertyWrapper extends MComponent implements IPropertyAccessor {
    private String precedingLabel;
    private int indexInParent;
    private String parentName;
    private int indexInContainer;
    private String mcomponentName = null;
    private Object layoutData;
    private String fieldName;

    public PropertyWrapper(Component component, WindowMonitor windowMonitor) {
        super(component, "No Name", null, windowMonitor);
    }

    public String getType() {
        String name = getComponent().getClass().getName();
        if (name.startsWith("javax.swing")) {
            return name.substring("javax.swing.".length());
        }
        return name;
    }

    public String getName() {
        return getComponent().getName();
    }

    public String getButtonText() {
        if (getComponent() instanceof AbstractButton)
            return ((AbstractButton) getComponent()).getText();
        return null;
    }

    public String getButtonIconFile() {
        if (!(getComponent() instanceof AbstractButton))
            return null;
        AbstractButton button = (AbstractButton) getComponent();
        Icon icon = button.getIcon();
        if (icon != null && icon instanceof ImageIcon) {
            String description = ((ImageIcon) icon).getDescription();
            if (description != null && description.length() != 0)
                return mapFromImageDescription(description);
        }
        return null;
    }

    private String mapFromImageDescription(String description) {
        try {
            String name = new URL(description).getPath();
            if (name.lastIndexOf('/') != -1)
                name = name.substring(name.lastIndexOf('/') + 1);
            if (name.lastIndexOf('.') != -1)
                name = name.substring(0, name.lastIndexOf('.'));
            return name;
        } catch (MalformedURLException e) {
            return description;
        }
    }

    public String getLabelText() {
        if (getComponent() instanceof JLabel)
            return "lbl:" + ((JLabel) getComponent()).getText();
        return null;
    }

    public String getLabeledBy() {
        if (getComponent() instanceof JComponent) {
            try {
                JLabel label = (JLabel) ((JComponent) getComponent()).getClientProperty("labeledBy");
                if (label != null && label.getText() != null) {
                    String name = label.getText().trim();
                    if (name.endsWith(":")) {
                        name = name.substring(0, name.length() - 1).trim();
                    }
                    return name;
                }
            } catch (ClassCastException e) {
            }
        }
        return null;
    }

    public void setPrecedingLabel(String precedingLabel) {
        this.precedingLabel = precedingLabel;
    }

    public String getPrecedingLabel() {
        return precedingLabel;
    }

    public void setIndexInParent(int indexInParent) {
        this.indexInParent = indexInParent;
    }

    public int getIndexInParent() {
        return indexInParent;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setIndexInContainer(int indexInContainer) {
        this.indexInContainer = indexInContainer;
    }

    public int getIndexInContainer() {
        return indexInContainer;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertyWrapper other = (PropertyWrapper) obj;
        if (component == null) {
            if (other.component != null)
                return false;
        } else if (!component.equals(other.component))
            return false;
        return true;
    }

    public void setMComponentName(String name) {
        this.mcomponentName = name;
    }

    @Override public String getMComponentName() {
        if (this.mcomponentName != null)
            return this.mcomponentName;
        return super.getMComponentName();
    }

    public void setLayoutData(Object layoutData) {
        this.layoutData = layoutData;
    }

    public Object getLayoutData() {
        return layoutData;
    }

    public String getOMapClassName() {
        if (component instanceof Frame || component instanceof Window || component instanceof Dialog
                || component instanceof JInternalFrame) {
            String className = component.getClass().getName();
            Package pkg = component.getClass().getPackage();
            if (pkg == null)
                return className;
            String pkgName = pkg.getName();
            if (!pkgName.startsWith("javax.swing") && !pkgName.startsWith("java.awt"))
                return className;
            if (className.equals("javax.swing.ColorChooserDialog"))
                return className;
            if (component instanceof JDialog) {
                Component[] components = ((JDialog) component).getContentPane().getComponents();
                if (components.length == 1 && components[0] instanceof JFileChooser)
                    return JFileChooser.class.getName() + "#Dialog";
                if (components.length == 1 && components[0] instanceof JOptionPane)
                    return JOptionPane.class.getName() + "#Dialog";
            }
            return null;
        }
        return null;
    }

    public String getOMapClassSimpleName() {
        if (component instanceof Frame || component instanceof Window || component instanceof Dialog
                || component instanceof JInternalFrame) {
            String className = component.getClass().getName();
            String simpleName = component.getClass().getSimpleName();
            Package pkg = component.getClass().getPackage();
            if (pkg == null)
                return simpleName;
            String pkgName = pkg.getName();
            if (!pkgName.startsWith("javax.swing") && !pkgName.startsWith("java.awt"))
                return simpleName;
            if (className.equals("javax.swing.ColorChooserDialog"))
                return simpleName;
            if (component instanceof JDialog) {
                Component[] components = ((JDialog) component).getContentPane().getComponents();
                if (components.length == 1 && components[0] instanceof JFileChooser)
                    return JFileChooser.class.getSimpleName() + "#Dialog";
                if (components.length == 1 && components[0] instanceof JOptionPane)
                    return JOptionPane.class.getSimpleName() + "#Dialog";
            }
            return null;
        }
        return null;
    }

    public String getInstanceOf() {
        Class<?> klass = component.getClass();
        while (klass != null && klass.getPackage() != null && !klass.getPackage().getName().startsWith("javax.swing")
                && !klass.getPackage().getName().startsWith("java.awt")) {
            klass = klass.getSuperclass();
        }
        return klass == null ? null : klass.getName();
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getInternalFrameIndex() {
        if (component instanceof JInternalFrame) {
            JInternalFrame[] frames = ((JInternalFrame) component).getDesktopPane().getAllFrames();
            Arrays.sort(frames, new Comparator<JInternalFrame>() {
                public int compare(JInternalFrame o1, JInternalFrame o2) {
                    return o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX();
                }
            });
            for (int i = 0; i < frames.length; i++) {
                if (frames[i] == component) {
                    System.out.println("PropertyWrapper.getInternalFrameIndex(): " + frames[i].getTitle() + " index = " + i);
                    return i;
                }
            }
        }
        return -1 ;
    }
}