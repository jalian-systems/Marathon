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
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import net.sourceforge.marathon.event.FireableKeyEvent;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.OSUtils;

public class MTextComponent extends MComponent {
    public MTextComponent(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    public String getText() {
        return (String) eventQueueRunner.invoke(getTextComponent(), "getText");
    }

    public void setText(String text) {
        setText(text, false);
    }

    public void setText(String text, boolean isCellEditing) {
        new FireableMouseClickEvent(getTextComponent()).fire(2);
        swingWait();
        if (!selectAllText()) {
            eventQueueRunner.invoke(getTextComponent(), "setText", new Object[] { text }, new Class[] { String.class });
            return;
        }
        swingWait();
        FireableKeyEvent keyEvent = new FireableKeyEvent(getTextComponent(), 0);
        if (text.length() > 0) {
            keyEvent.fire(text);
        } else {
            keyEvent.fire(KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
        }
        swingWait();
        if (isCellEditing)
            keyEvent.fire(KeyEvent.VK_ENTER, (char) 13);
        swingWait();
    }

    public boolean isMComponentEditable() {
        boolean isEditable = eventQueueRunner.invokeBoolean(getTextComponent(), "isEditable");
        return isEditable && getComponent().isEnabled();
    }

    private JTextComponent getTextComponent() {
        return (JTextComponent) getComponent();
    }

    private boolean selectAllText() {
        new FireableKeyEvent(getTextComponent(), OSUtils.MENU_MASK).fire("a");
        swingWait();
        int length = 0;
        if (getText() != null)
            length = getText().length();
        int selectionStart = eventQueueRunner.invokeInteger(getTextComponent(), "getSelectionStart");
        int selectionEnd = eventQueueRunner.invokeInteger(getTextComponent(), "getSelectionEnd");
        if (selectionStart != 0 || selectionEnd != length) {
            System.err.println("Warning: clicking and then hitting " + OSUtils.inputEventGetModifiersExText(OSUtils.MENU_MASK)
                    + "+A " + " should select everything on " + getMComponentName());
            System.err.println("Using setText fallback");
            return false;
        }
        return true;
    }

    public boolean keyNeeded(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && getTextComponent() instanceof JTextField)
            return true;
        return super.keyNeeded(e);
    }
}
