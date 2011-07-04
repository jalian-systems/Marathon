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
public abstract class OldSimpleAction extends AbstractAction implements PropertyChangeListener {
    private static final long serialVersionUID = 4342290998523999163L;
    private Icon icon_enabled;
    private Icon icon_disabled;

    public OldSimpleAction(String name, char mneumonic) {
        super(name);
        if (mneumonic != 0)
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(mneumonic));
        putValue(Action.SHORT_DESCRIPTION, name);
    }

    public OldSimpleAction(String name, char mneumonic, Icon icon) {
        this(name, mneumonic);
        icon_enabled = icon;
    }

    public OldSimpleAction(String name, char mneumonic, Icon icon, Icon iconDisabled) {
        this(name, mneumonic, icon);
        addPropertyChangeListener(this);
        icon_disabled = iconDisabled;
    }

    public JMenuItem getMenuItem() {
        JMenuItem item = new JMenuItem(this);
        item.setIcon(icon_enabled);
        item.setDisabledIcon(icon_disabled);
        item.setPressedIcon(icon_disabled);
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
        if (icon_enabled == null) {
            button.setText((String) getValue(NAME));
            return button;
        }
        button.setText(null);
        button.setPressedIcon(icon_disabled);
        button.setDisabledIcon(icon_disabled);
        button.setIcon(icon_enabled);
        return button;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) {
            if (((Boolean) evt.getNewValue()).booleanValue())
                putValue(Action.SMALL_ICON, icon_enabled);
            else
                putValue(Action.SMALL_ICON, icon_disabled);
        }
    }

    public Icon getIconEnabled() {
        return icon_enabled;
    }

    public Icon getIconDisabled() {
        return icon_disabled;
    }
}
