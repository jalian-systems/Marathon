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
package net.sourceforge.marathon.extensions;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.event.FireableKeyEvent;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.OSUtils;

/**
 * This is just a copy of the MTextField. We changed the gettext and settext to
 * returns encrypted(!) data.
 * 
 * As much as possible we try to create the mouse/keyboard events during
 * settext. In some cases we can just use the model to fetch and set the data.
 */
public class MPasswordField extends MComponent {
    public MPasswordField(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    public String getText() {
        return "password:" + getTextComponent().getText();
    }

    public void setText(String text) {
        if (text.startsWith("password:"))
            text = text.substring(9);
        new FireableMouseClickEvent(getTextComponent()).fire(1);
        swingWait();
        selectAllText();
        FireableKeyEvent keyEvent = new FireableKeyEvent(getTextComponent(), 0);
        if (text.length() > 0) {
            keyEvent.fire(text);
        } else {
            keyEvent.fire(KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
        }
        if (!getTextComponent().getText().equals(text)) {
            // Needed for non-editable fields and fields that do not
            // allow keyevents
            swingWait();
            getTextComponent().setText(text);
        }
    }

    private JTextComponent getTextComponent() {
        return (JTextComponent) getComponent();
    }

    private void selectAllText() {
        new FireableKeyEvent(getTextComponent(), OSUtils.MENU_MASK).fire("a");
        swingWait();
        if (getTextComponent().getSelectionStart() != 0
                || getTextComponent().getSelectionEnd() != getTextComponent().getText().length()) {
            throw new Error("clicking and then hitting ctrl-A should select everything");
        }
    }
}
