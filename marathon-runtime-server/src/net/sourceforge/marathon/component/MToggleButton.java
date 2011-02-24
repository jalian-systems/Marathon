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

import java.awt.event.MouseEvent;

import javax.swing.JToggleButton;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class MToggleButton extends MButton {
    public MToggleButton(JToggleButton button, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(button, name, finder, windowMonitor);
    }

    public String getText() {
        boolean isSelected = eventQueueRunner.invokeBoolean(getTottleButtonComponent(), "isSelected");
        return Boolean.toString(isSelected);
    }

    public void setText(String text) {
        boolean value = Boolean.valueOf(text).booleanValue();
        boolean isSelected = eventQueueRunner.invokeBoolean(getTottleButtonComponent(), "isSelected");
        if (value != isSelected) {
            click(1);
        }
    }

    private JToggleButton getTottleButtonComponent() {
        return (JToggleButton) getComponent();
    }

    public int clickNeeded(MouseEvent e) {
        if (isPopupTrigger(e)) {
            return ClickAction.RECORD_CLICK;
        }
        return ClickAction.RECORD_NONE;
    }

    public boolean recordOnMouseRelease() {
        return true;
    }
}
