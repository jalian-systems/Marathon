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

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Method;

import javax.swing.KeyStroke;
import javax.swing.UIManager;

public class OSUtils {

    public static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    public static final int MOUSE_MENU_MASK;
    private static final String JAVA_VERSION = System.getProperty("java.version");
    private static final String OS_NAME = System.getProperty("os.name");

    static {
        if ((MENU_MASK & InputEvent.META_MASK) != 0)
            MOUSE_MENU_MASK = InputEvent.META_DOWN_MASK;
        else if ((MENU_MASK & InputEvent.CTRL_MASK) != 0)
            MOUSE_MENU_MASK = InputEvent.CTRL_DOWN_MASK;
        else
            MOUSE_MENU_MASK = MENU_MASK;
    }

    /**
     * Converts a string to a keystroke. The string should be of the form
     * <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i> is any
     * combination of A for Alt, C for Control, S for Shift or M for Meta, and
     * <i>shortcut</i> is either a single character, or a keycode name from the
     * <code>KeyEvent</code> class, without the <code>VK_</code> prefix. Using ^
     * for modifier uses Platform specific Menu Shortcut mask.
     * 
     * @param keyStroke
     *            A string description of the key stroke
     */
    public static KeyStroke parseKeyStroke(String keyStroke) {
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if (keyStroke == null)
            return null;
        int modifiers = 0;
        int index = keyStroke.indexOf('+');
        if (index != -1) {
            for (int i = 0; i < index; i++) {
                switch (Character.toUpperCase(keyStroke.charAt(i))) {
                case 'A':
                    modifiers |= InputEvent.ALT_MASK;
                    break;
                case 'C':
                    modifiers |= InputEvent.CTRL_MASK;
                    break;
                case 'M':
                    modifiers |= InputEvent.META_MASK;
                    break;
                case 'S':
                    modifiers |= InputEvent.SHIFT_MASK;
                    break;
                case '^':
                    modifiers |= Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
                    break;
                }
            }
        }
        String key = keyStroke.substring(index + 1);
        if (key.length() == 1) {
            char ch = Character.toUpperCase(key.charAt(0));
            if (modifiers == 0)
                return KeyStroke.getKeyStroke(ch);
            else
                return KeyStroke.getKeyStroke(ch, modifiers);
        } else if (key.length() == 0) {
            return null;
        } else {
            int ch;
            try {
                ch = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
            } catch (Exception e) {
                return null;
            }
            return KeyStroke.getKeyStroke(ch, modifiers);
        }
    }

    /**
     * Sets the LAF for the application dynamically looking for JGoodies Looks
     * library.
     * 
     */
    public static void setLookAndFeel() {
        if (System.getProperty("java.vendor").startsWith("Apple")) {
            if (Boolean.getBoolean("marathon.useAppleMenuBar") || System.getProperty("marathon.useAppleMenuBar") == null)
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            return;
        }
        try {
            Class<?> classOptions = Class.forName("com.jgoodies.looks.Options");
            Method method = classOptions.getMethod("setUseNarrowButtons", new Class[] { boolean.class });
            method.invoke(null, new Object[] { Boolean.TRUE });
            method = classOptions.getMethod("setUseSystemFonts", new Class[] { boolean.class });
            method.invoke(null, new Object[] { Boolean.TRUE });
            method = classOptions.getMethod("getCrossPlatformLookAndFeelClassName", new Class[] {});
            String looksClass = (String) method.invoke(null, new Object[] {});
            UIManager.setLookAndFeel(looksClass);
        } catch (Throwable e1) {
            // Do nothing - go ahead with the current Look and Feel
        }
    }

    public static boolean isMac() {
        return OS_NAME.startsWith("Mac");
    }

    public static boolean isJava5OrLater() {
        return !JAVA_VERSION.startsWith("1.4");
    }

    public static String keyEventGetKeyText(int keycode) {
        if (keycode == KeyEvent.VK_TAB)
            return "Tab";
        if (keycode == KeyEvent.VK_CONTROL)
            return "Ctrl";
        if (keycode == KeyEvent.VK_ALT)
            return "Alt";
        if (keycode == KeyEvent.VK_SHIFT)
            return "Shift";
        if (keycode == KeyEvent.VK_META)
            return "Command";
        if (keycode == KeyEvent.VK_SPACE)
            return "Space";
        if (keycode == KeyEvent.VK_BACK_SPACE)
            return "Backspace";
        if (keycode == KeyEvent.VK_HOME)
            return "Home";
        if (keycode == KeyEvent.VK_END)
            return "End";
        if (keycode == KeyEvent.VK_DELETE)
            return "Delete";
        if (keycode == KeyEvent.VK_PAGE_UP)
            return "Pageup";
        if (keycode == KeyEvent.VK_PAGE_DOWN)
            return "Pagedown";
        if (keycode == KeyEvent.VK_UP)
            return "Up";
        if (keycode == KeyEvent.VK_DOWN)
            return "Down";
        if (keycode == KeyEvent.VK_LEFT)
            return "Left";
        if (keycode == KeyEvent.VK_RIGHT)
            return "Right";
        if (keycode == KeyEvent.VK_ENTER)
            return "Enter";
        return KeyEvent.getKeyText(keycode);
    }

    public static String inputEventGetModifiersExText(int modifiers) {
        StringBuffer sb = new StringBuffer();

        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0)
            sb.append("Ctrl+");
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0)
            sb.append("Command+");
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0)
            sb.append("Alt+");
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0)
            sb.append("Shift+");
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0)
            sb.append("Button1+");
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0)
            sb.append("Button2+");
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0)
            sb.append("Button3+");
        String text = sb.toString();
        if (text.equals(""))
            return text;
        return text.substring(0, text.length() - 1);
    }

    public static boolean isWindowsOS() {
        return File.pathSeparatorChar == ';';
    }

    public static MouseEvent convert(MouseEvent e) {
        if (isMac()) {
            int checkMask1 = MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;
            int checkMask2 = MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK;
            int modifiers = e.getModifiersEx();
            if ((modifiers & checkMask1) == checkMask1 || (modifiers & checkMask2) == checkMask2) {
                modifiers &= ~(checkMask1 | checkMask2);
                modifiers |= MouseEvent.BUTTON3_DOWN_MASK;
                e = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), modifiers, e.getX(), e.getY(),
                        e.getClickCount(), true, e.getButton());
            }
        }
        return e;
    }
}
