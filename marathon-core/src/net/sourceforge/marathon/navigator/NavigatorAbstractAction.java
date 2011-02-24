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
package net.sourceforge.marathon.navigator;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;

import net.sourceforge.marathon.display.OldSimpleAction;

public abstract class NavigatorAbstractAction extends OldSimpleAction {
    private static final long serialVersionUID = -2726821864269261800L;
    private Navigator navigator;

    abstract public void actionPerformed(ActionEvent e, File[] file);

    abstract public boolean getEnabledState(File[] files);

    public void actionPerformed(ActionEvent e) {
        File[] files = navigator.getSelectedFiles();
        if (getEnabledState(files))
            actionPerformed(e, files);
    }

    public NavigatorAbstractAction(Navigator navigator, String name) {
        this(navigator, name, null);
    }

    public NavigatorAbstractAction(Navigator navigator, String name, Icon icon) {
        this(navigator, name, icon, null);
    }

    public NavigatorAbstractAction(Navigator navigator, String name, Icon icon_enabled, Icon icon_disabled) {
        super(name, (char) 0, icon_enabled, icon_disabled);
        putValue(Action.SMALL_ICON, icon_enabled);
        this.navigator = navigator;
    }
}
