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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.KeyStroke;

public class KeyStrokeParser {

    private static Map<Object, Object> keyCodes;
    static {
        initKeyCodes();
    }
    private char keyChar;
    private KeyStroke keyStroke;

    public KeyStrokeParser(String sequence) {
        parseSequence(sequence);
    }

    private void parseSequence(String sequence) {
        if (sequence.length() == 1) {
            keyChar = sequence.charAt(0);
            keyStroke = KeyStroke.getKeyStroke(keyChar);
            return;
        }
        int modifiers = 0;
        int key = 0;
        StringTokenizer toke = new StringTokenizer(sequence, "+");
        while (toke.hasMoreTokens()) {
            String keyText = toke.nextToken();
            Integer keycode = (Integer) keyCodes.get(keyText);
            if (keycode == null && keyText.equals("Meta"))
                keycode = (Integer) keyCodes.get("Command");
            if (keycode == null && keyText.equals("Command"))
                keycode = (Integer) keyCodes.get("Meta");
            if (keycode == null)
                throw new RuntimeException("don't know what key is represented by " + sequence);
            if (toke.hasMoreTokens()) {
                modifiers |= getModifier(keycode.intValue());
            } else {
                key = keycode.intValue();
            }
        }
        keyStroke = KeyStroke.getKeyStroke(key, modifiers);
        if (modifiers == 0 && key < 128) {
            keyChar = (char) key;
        } else {
            keyChar = keyStroke.getKeyChar();
        }
    }

    private int getModifier(int keycode) {
        switch (keycode) {
        case KeyEvent.VK_SHIFT:
            return InputEvent.SHIFT_MASK;
        case KeyEvent.VK_CONTROL:
            return InputEvent.CTRL_MASK;
        case KeyEvent.VK_ALT:
            return InputEvent.ALT_MASK;
        case KeyEvent.VK_META:
            return InputEvent.META_MASK;
        default:
            throw new RuntimeException(OSUtils.keyEventGetKeyText(keycode) + " is not a valid modifier");
        }
    }

    private synchronized static void initKeyCodes() {
        keyCodes = new HashMap<Object, Object>();
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = field.getName();
            if (fieldName.startsWith("VK")) {
                int keyCode = 0;
                try {
                    keyCode = field.getInt(null);
                } catch (IllegalArgumentException e) {
                    throw new Error("could not read key codes from VM!");
                } catch (IllegalAccessException e) {
                    throw new Error("could not read key codes from VM!");
                }
                keyCodes.put(OSUtils.keyEventGetKeyText(keyCode), Integer.valueOf(keyCode));
                keyCodes.put(fieldName, Integer.valueOf(keyCode));
            }
        }
        keyCodes.put("Ctrl", Integer.valueOf(KeyEvent.VK_CONTROL));
        keyCodes.put("Shift", Integer.valueOf(KeyEvent.VK_SHIFT));
        keyCodes.put("Enter", Integer.valueOf(KeyEvent.VK_ENTER));
        keyCodes.put("Alt", Integer.valueOf(KeyEvent.VK_ALT));
        keyCodes.put("Enter", Integer.valueOf(KeyEvent.VK_ENTER));
        keyCodes.put("Command", Integer.valueOf(KeyEvent.VK_META));
        keyCodes.put("Space", Integer.valueOf(KeyEvent.VK_SPACE));

        keyCodes.put(Integer.valueOf(KeyEvent.VK_CONTROL), "Ctrl");
        keyCodes.put(Integer.valueOf(KeyEvent.VK_SHIFT), "Shift");
        keyCodes.put(Integer.valueOf(KeyEvent.VK_ENTER), "Enter");
        keyCodes.put(Integer.valueOf(KeyEvent.VK_ALT), "Alt");
        keyCodes.put(Integer.valueOf(KeyEvent.VK_ENTER), "Enter");
        keyCodes.put(Integer.valueOf(KeyEvent.VK_META), "Command");
        keyCodes.put(Integer.valueOf(KeyEvent.VK_SPACE), "Space");
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public char getKeyChar() {
        return keyChar;
    }

    public static String getKeyModifierText(int modifiers) {
        if (modifiers == 0)
            return "";
        StringBuffer sb = new StringBuffer();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            sb.append("Ctrl+");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            sb.append("Alt+");
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            sb.append("Shift+");
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            sb.append("Meta+");
        }
        return sb.toString();
    }

    public static String getTextForKeyChar(char keyChar) {
        int keycode;
        switch (keyChar) {
        case ' ':
            keycode = KeyEvent.VK_SPACE;
            break;
        case '\b':
            keycode = KeyEvent.VK_BACK_SPACE;
            break;
        case '\t':
            keycode = KeyEvent.VK_TAB;
            break;
        case '\n':
            keycode = KeyEvent.VK_ENTER;
            break;
        case '\u0018':
            keycode = KeyEvent.VK_CANCEL;
            break;
        case '\u001b':
            keycode = KeyEvent.VK_ESCAPE;
            break;
        case '\u007f':
            keycode = KeyEvent.VK_DELETE;
            break;
        default:
            return "" + keyChar;
        }
        return (String) keyCodes.get(Integer.valueOf(keycode));
    }

}
