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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;

/**
 * this is a simple action class that allows you to specify stuff we care about
 * in the constructor each instance will have to subclass it to provide an
 * actionPeformed method as well
 */
public abstract class AbstractSimpleAction extends AbstractAction implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    private Icon iconEnabled;
    private Icon iconDisabled;

    public AbstractSimpleAction(String name, String description, char mneumonic, Icon icon, Icon iconDisabled) {
        super(name);
        if (mneumonic != 0)
            putValue(Action.MNEMONIC_KEY, new Integer(mneumonic));
        if (description.equals(""))
            putValue(Action.SHORT_DESCRIPTION, name);
        else
            putValue(Action.SHORT_DESCRIPTION, description);
        iconEnabled = icon;
        addPropertyChangeListener(this);
        this.iconDisabled = iconDisabled;
        putValue(Action.SMALL_ICON, iconEnabled);
    }

    public JMenuItem getMenuItem() {
        JMenuItem item = new JMenuItem(this);
        item.setIcon(iconEnabled);
        item.setDisabledIcon(iconDisabled);
        item.setPressedIcon(iconDisabled);
        return item;
    }

    public JButton getButton() {
        JButton button = new JButton(this) {
            private static final long serialVersionUID = 1L;

            public boolean isFocusTraversable() {
                return false;
            }
        };
        button.setName((String) getValue(NAME));
        if (iconEnabled == null) {
            button.setText((String) getValue(NAME));
            return button;
        }
        button.setText(null);
        button.setPressedIcon(iconDisabled);
        button.setDisabledIcon(iconDisabled);
        button.setIcon(iconEnabled);
        return button;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) {
            if (((Boolean) evt.getNewValue()).booleanValue())
                putValue(Action.SMALL_ICON, iconEnabled);
            else
                putValue(Action.SMALL_ICON, iconDisabled);
        }
    }

    public Icon getIconEnabled() {
        return iconEnabled;
    }

    public Icon getIconDisabled() {
        return iconDisabled;
    }
}
