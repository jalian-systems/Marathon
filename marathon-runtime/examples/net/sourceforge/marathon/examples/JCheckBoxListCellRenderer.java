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
package net.sourceforge.marathon.examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class JCheckBoxListCellRenderer extends JPanel implements ListCellRenderer, Serializable {
    private static final long serialVersionUID = 1L;

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    private final JCheckBox checkBox = new JCheckBox();
    private final JLabel label = new JLabel();

    private final ISelectionProvider provider;

    /**
     * Constructs a default renderer object for an item in a list.
     * 
     * @param jCheckBoxList
     */
    public JCheckBoxListCellRenderer(ISelectionProvider provider) {
        super();
        this.provider = provider;
        setLayout(new BorderLayout());
        add(checkBox, BorderLayout.WEST);
        add(label);
        setOpaque(true);
        setBorder(getNoFocusBorder());
    }

    private static Border getNoFocusBorder() {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        } else {
            return noFocusBorder;
        }
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        setComponentOrientation(list.getComponentOrientation());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        if (value instanceof Icon) {
            label.setIcon((Icon) value);
            label.setText("");
        } else {
            label.setIcon(null);
            label.setText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        checkBox.setSelected(provider.isSelected(index));

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);

        return this;
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName == "text")
            super.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    /**
     * Overridden for performance reasons. See the <a
     * href="#override">Implementation Note</a> for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }
}
