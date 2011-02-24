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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.KeyStroke;

import net.sourceforge.marathon.Constants;

public class ContextMenuTriggers {
    private static int menuModifiers;
    private static int contextMenuKey;
    private static int contextMenuKeyModifiers;

    static {
        Preferences prefs = Preferences.userNodeForPackage(Constants.class);
        System.setProperty(Constants.PROP_RECORDER_MOUSETRIGGER, prefs.get(Constants.PREF_RECORDER_MOUSE_TRIGGER, ""));
        System.setProperty(Constants.PROP_RECORDER_KEYTRIGGER, prefs.get(Constants.PREF_RECORDER_KEYBOARD_TRIGGER, ""));
        ContextMenuTriggers.setContextMenuModifiers();
        ContextMenuTriggers.setContextMenuKey();
    }

    public static void setContextMenuModifiers() {
        String menuModifiersTxt = System.getProperty(Constants.PROP_RECORDER_MOUSETRIGGER);
        if (menuModifiersTxt == null || menuModifiersTxt.equals("")) {
            menuModifiers = OSUtils.MOUSE_MENU_MASK | InputEvent.BUTTON3_DOWN_MASK;
            return;
        }
        setMenuModifiersFromText(menuModifiersTxt);
    }

    private static void setMenuModifiersFromText(String menuModifiersTxt) {
        String button = menuModifiersTxt.substring(menuModifiersTxt.lastIndexOf('+') + 1);
        menuModifiers = 0;
        if (button.equals("Button1"))
            menuModifiers |= InputEvent.BUTTON1_DOWN_MASK;
        else if (button.equals("Button2"))
            menuModifiers |= InputEvent.BUTTON2_DOWN_MASK;
        else if (button.equals("Button3"))
            menuModifiers |= InputEvent.BUTTON3_DOWN_MASK;
        else
            throw new RuntimeException("Unknow button " + button + " in setting mouse trigger");
        String modifiers = menuModifiersTxt.substring(0, menuModifiersTxt.lastIndexOf('+'));
        KeyStroke ks = new KeyStrokeParser(modifiers + "+A").getKeyStroke();
        menuModifiers |= getDownModifierMask(ks.getModifiers());
    }

    public static int getContextMenuModifiers() {
        return menuModifiers;
    }

    public static boolean isContextMenuSequence(MouseEvent e) {
        return (e.getID() == MouseEvent.MOUSE_PRESSED) && (e.getModifiersEx() == getContextMenuModifiers());
    }

    public static int getContextMenuKeyCode() {
        return contextMenuKey;
    }

    public static int getContextMenuKeyModifiers() {
        return contextMenuKeyModifiers;
    }

    public static void setContextMenuKey() {
        String keyText = System.getProperty(Constants.PROP_RECORDER_KEYTRIGGER);
        if (keyText == null || keyText.equals("")) {
            contextMenuKey = KeyEvent.VK_F8;
            contextMenuKeyModifiers = OSUtils.MOUSE_MENU_MASK;
        } else {
            KeyStroke ks = new KeyStrokeParser(keyText).getKeyStroke();
            contextMenuKey = ks.getKeyCode();
            contextMenuKeyModifiers = getDownModifierMask(ks.getModifiers());
        }
    }

    private static int getDownModifierMask(int modifiers) {
        int contextMenuKeyModifiers = 0;
        if ((modifiers & InputEvent.CTRL_MASK) != 0)
            contextMenuKeyModifiers |= InputEvent.CTRL_DOWN_MASK;
        if ((modifiers & InputEvent.ALT_MASK) != 0)
            contextMenuKeyModifiers |= InputEvent.ALT_DOWN_MASK;
        if ((modifiers & InputEvent.META_MASK) != 0)
            contextMenuKeyModifiers |= InputEvent.META_DOWN_MASK;
        if ((modifiers & InputEvent.SHIFT_MASK) != 0)
            contextMenuKeyModifiers |= InputEvent.SHIFT_DOWN_MASK;
        return contextMenuKeyModifiers;
    }

    public static boolean isContextMenuKey(KeyEvent event) {
        return event.getKeyCode() == getContextMenuKeyCode() && event.getModifiersEx() == getContextMenuKeyModifiers();
    }

    public static boolean isContextMenuKeySequence(KeyEvent event) {
        return event.getID() == KeyEvent.KEY_PRESSED && isContextMenuKey(event);
    }

}
