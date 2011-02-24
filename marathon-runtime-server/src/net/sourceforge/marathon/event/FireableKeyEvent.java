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
package net.sourceforge.marathon.event;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.Snooze;

public class FireableKeyEvent extends FireableEvent {
    private static final int[] INPUT_MASKS = { InputEvent.ALT_GRAPH_MASK, InputEvent.ALT_MASK, InputEvent.META_MASK,
            InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK };
    private int modifiers;

    public FireableKeyEvent(Component component, int modifiers) {
        super(component);
        if (Boolean.valueOf(System.getProperty(Constants.PROP_APPLICATION_TOOLKIT_MENUMASK, "false")).booleanValue()) {
            if ((modifiers & InputEvent.CTRL_MASK) != 0) {
                modifiers = (modifiers & ~InputEvent.CTRL_MASK) | OSUtils.MENU_MASK;
            }
            if ((modifiers & InputEvent.META_MASK) != 0) {
                modifiers = (modifiers & ~InputEvent.META_MASK) | OSUtils.MENU_MASK;
            }
        }
        this.modifiers = modifiers;
    }

    public void fire(int keyCode, char ch) {
        eventQueueRunner.invoke(getComponent(), "requestFocusInWindow");
        AWTSync.sync();
        pressModifiers(modifiers);
        fireSingleKey(keyCode, ch);
        releaseModifiers(modifiers);
    }

    public void fire(String text) {
        eventQueueRunner.invoke(getComponent(), "requestFocusInWindow");
        AWTSync.sync();
        pressModifiers(modifiers);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int keyCode = getKeyCode(ch);
            // Send the shift when appropriate
            if (Character.isUpperCase(ch) && (modifiers & InputEvent.SHIFT_MASK) == 0) {
                pressModifiers(InputEvent.SHIFT_MASK);
            }
            fireSingleKey(keyCode, ch);
            new Snooze(1);
            // Release the shift.
            if (Character.isUpperCase(ch) && (modifiers & InputEvent.SHIFT_MASK) == 0) {
                releaseModifiers(InputEvent.SHIFT_MASK);
            }
        }
        releaseModifiers(modifiers);
    }

    private void fireSingleKey(int keyCode, char ch) {
        postEvent(new KeyEvent(getComponent(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, ch));
        if (ch != KeyEvent.CHAR_UNDEFINED) {
            postEvent(new KeyEvent(getComponent(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers,
                    KeyEvent.VK_UNDEFINED, ch));
        }
        postEvent(new KeyEvent(getComponent(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, ch));
    }

    private int getKeyCode(char ch) {
        if (Character.isLetterOrDigit(ch)) {
            return Character.isLowerCase(ch) ? Character.toUpperCase(ch) : ch;
        }
        switch (ch) {
        case '\'':
            return KeyEvent.VK_QUOTE;
        case '"':
            return KeyEvent.VK_QUOTEDBL;
        case '(':
            return KeyEvent.VK_LEFT_PARENTHESIS;
        case ')':
            return KeyEvent.VK_RIGHT_PARENTHESIS;
        case '!':
            return KeyEvent.VK_EXCLAMATION_MARK;
        case '$':
            modifiers = KeyEvent.VK_SHIFT;
            return KeyEvent.VK_4;
        case '%':
            modifiers = KeyEvent.VK_SHIFT;
            return KeyEvent.VK_5;
        case '\t':
            return KeyEvent.VK_TAB;
        }
        return ch;
    }

    /**
     * Send the modifier key press event for the modifiers.
     */
    private void pressModifiers(int modifiers) {
        int mods = 0;
        for (int i = 0; i < INPUT_MASKS.length; i++) {
            int inputMask = INPUT_MASKS[i];
            if ((modifiers & inputMask) > 0) {
                mods = mods | inputMask;
                int keyCode = getKeyCode(inputMask);
                postEvent(new KeyEvent(getComponent(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), mods, keyCode,
                        KeyEvent.CHAR_UNDEFINED));
            }
        }
    }

    /**
     * Send the modifier key release event for the modifiers.
     */
    private void releaseModifiers(int modifiers) {
        int mods = modifiers;
        for (int i = 0; i < INPUT_MASKS.length; i++) {
            int inputMask = INPUT_MASKS[i];
            if ((modifiers & inputMask) > 0) {
                mods = mods & (-1 ^ inputMask);
                int keyCode = getKeyCode(inputMask);
                postEvent(new KeyEvent(getComponent(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), mods, keyCode,
                        KeyEvent.CHAR_UNDEFINED));
            }
        }
    }

    private int getKeyCode(int inputCode) {
        switch (inputCode) {
        case InputEvent.ALT_GRAPH_MASK:
            return KeyEvent.VK_ALT_GRAPH;
        case InputEvent.ALT_MASK:
            return KeyEvent.VK_ALT;
        case InputEvent.META_MASK:
            return KeyEvent.VK_META;
        case InputEvent.CTRL_MASK:
            return KeyEvent.VK_CONTROL;
        case InputEvent.SHIFT_MASK:
            return KeyEvent.VK_SHIFT;
        default:
            throw new RuntimeException("unknown input code");
        }
    }

    /**
     * Check if the keyCode is a typed key.
     * 
     * @param keyCode
     *            key code to be checked.
     * @return true if the keyCode is a typed key.
     */
    public static final boolean isTypedChar(int keyCode) {
        switch (keyCode) {
        case KeyEvent.VK_ACCEPT:
        case KeyEvent.VK_ADD:
        case KeyEvent.VK_AGAIN:
        case KeyEvent.VK_ALL_CANDIDATES:
        case KeyEvent.VK_ALPHANUMERIC:
        case KeyEvent.VK_ALT:
        case KeyEvent.VK_ALT_GRAPH:
        case KeyEvent.VK_CANCEL:
        case KeyEvent.VK_CAPS_LOCK:
        case KeyEvent.VK_CLEAR:
        case KeyEvent.VK_CODE_INPUT:
        case KeyEvent.VK_COMPOSE:
        case KeyEvent.VK_CONTROL:
        case KeyEvent.VK_CONVERT:
        case KeyEvent.VK_COPY:
        case KeyEvent.VK_CUT:
        case KeyEvent.VK_DELETE:
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_END:
        case KeyEvent.VK_F1:
        case KeyEvent.VK_F10:
        case KeyEvent.VK_F11:
        case KeyEvent.VK_F12:
        case KeyEvent.VK_F13:
        case KeyEvent.VK_F14:
        case KeyEvent.VK_F15:
        case KeyEvent.VK_F16:
        case KeyEvent.VK_F17:
        case KeyEvent.VK_F18:
        case KeyEvent.VK_F19:
        case KeyEvent.VK_F2:
        case KeyEvent.VK_F20:
        case KeyEvent.VK_F21:
        case KeyEvent.VK_F22:
        case KeyEvent.VK_F23:
        case KeyEvent.VK_F24:
        case KeyEvent.VK_F3:
        case KeyEvent.VK_F4:
        case KeyEvent.VK_F5:
        case KeyEvent.VK_F6:
        case KeyEvent.VK_F7:
        case KeyEvent.VK_F8:
        case KeyEvent.VK_F9:
        case KeyEvent.VK_FINAL:
        case KeyEvent.VK_FIND:
        case KeyEvent.VK_FULL_WIDTH:
        case KeyEvent.VK_HALF_WIDTH:
        case KeyEvent.VK_HELP:
        case KeyEvent.VK_HIRAGANA:
        case KeyEvent.VK_HOME:
        case KeyEvent.VK_INSERT:
        case KeyEvent.VK_JAPANESE_HIRAGANA:
        case KeyEvent.VK_JAPANESE_KATAKANA:
        case KeyEvent.VK_JAPANESE_ROMAN:
        case KeyEvent.VK_KANA:
        case KeyEvent.VK_KANJI:
        case KeyEvent.VK_KATAKANA:
        case KeyEvent.VK_KP_DOWN:
        case KeyEvent.VK_KP_LEFT:
        case KeyEvent.VK_KP_RIGHT:
        case KeyEvent.VK_KP_UP:
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_META:
        case KeyEvent.VK_MODECHANGE:
        case KeyEvent.VK_NONCONVERT:
        case KeyEvent.VK_NUM_LOCK:
        case KeyEvent.VK_PAGE_DOWN:
        case KeyEvent.VK_PAGE_UP:
        case KeyEvent.VK_PASTE:
        case KeyEvent.VK_PAUSE:
        case KeyEvent.VK_PREVIOUS_CANDIDATE:
        case KeyEvent.VK_PRINTSCREEN:
        case KeyEvent.VK_PROPS:
        case KeyEvent.VK_ROMAN_CHARACTERS:
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_SCROLL_LOCK:
        case KeyEvent.VK_SHIFT:
        case KeyEvent.VK_STOP:
        case KeyEvent.VK_UNDEFINED:
        case KeyEvent.VK_UNDO:
        case KeyEvent.VK_UP:
            return false;
        }
        return true;
    }
}
