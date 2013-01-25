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
package net.sourceforge.marathon.action;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.KeyStroke;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.KeyStrokeParser;

public class ClickAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private int numberOfClicks;
    private int record_click = RECORD_CLICK;
    private int modifiers = 0;
    private Point position;
    private int hoverDelay;
    public static final int RECORD_EX = 2;
    public static final int RECORD_CLICK = 1;
    public static final int RECORD_NONE = 0;
    private ActionType actionType;

    public static enum ActionType {
        CLICK, MOUSE_PRESSED, HOVER, MOUSE_RELEASED
    }

    public ClickAction(ComponentId id, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this(id, 1, scriptModel, windowMonitor);
    }

    public ClickAction(ComponentId id, int numberOfClicks, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this(id, numberOfClicks, false, scriptModel, windowMonitor);
    }

    public ClickAction(ComponentId componentId, int clickCount, boolean popupTrigger, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(componentId, scriptModel, windowMonitor);
        numberOfClicks = clickCount;
        if (popupTrigger)
            this.modifiers = InputEvent.BUTTON3_DOWN_MASK;
        else
            this.modifiers = InputEvent.BUTTON1_DOWN_MASK;
        this.position = null;
        if (actionType == null)
            actionType = ActionType.CLICK;
    }

    public ClickAction(ComponentId componentId, Point position, int clickCount, String modifiers, ActionType actionType,
            boolean isPopupTrigger, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this(componentId, clickCount, scriptModel, windowMonitor);
        this.position = position;
        this.actionType = actionType;
        this.record_click = RECORD_EX;
        setMenuModifiersFromText(modifiers, isPopupTrigger);
    }

    public ClickAction(ComponentId componentId, MouseEvent e, int record_click, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        this(componentId, e.getClickCount(), (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0, scriptModel, windowMonitor);
        this.record_click = record_click;
        this.modifiers = e.getModifiersEx();
        this.position = e.getPoint();
    }

    protected void requestFocus(Component c) {
        if (c != null)
            c.requestFocus();
    }

    public void play(ComponentFinder resolver) {
        MComponent component = resolver.getMComponentById(getComponentId());
        waitForWindowActive(getParentWindow(component.getComponent()));
        requestFocus(component.getComponent());
        if (actionType == ActionType.MOUSE_PRESSED) {
            component.mousePressed(modifiers, position);
        } else if (actionType == ActionType.MOUSE_RELEASED) {
            component.mouseReleased(modifiers, position);
        } else if (numberOfClicks == 0 || actionType == ActionType.HOVER)
            component.hover(hoverDelay);
        else
            component.click(numberOfClicks, modifiers, position);
    }

    public int getClickCount() {
        return numberOfClicks;
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForClick(getComponentId(), numberOfClicks, modifiers, record_click, position);
    }

    public static String getModifiersText(int modifiers) {
        String keyModifierText = KeyStrokeParser.getKeyModifierText(modifiers);
        if (keyModifierText.equals(""))
            return "";
        return keyModifierText.substring(0, keyModifierText.length() - 1);
    }

    private void setMenuModifiersFromText(String menuModifiersTxt, boolean isPopupTrigger) {
        modifiers = 0;
        if (menuModifiersTxt == null) {
            if (isPopupTrigger)
                modifiers |= InputEvent.BUTTON3_DOWN_MASK;
            else
                modifiers |= InputEvent.BUTTON1_DOWN_MASK;
            return;
        }
        String button = menuModifiersTxt.substring(menuModifiersTxt.lastIndexOf('+') + 1);
        if (button.equals("Button1"))
            modifiers |= InputEvent.BUTTON1_DOWN_MASK;
        else if (button.equals("Button2"))
            modifiers |= InputEvent.BUTTON2_DOWN_MASK;
        else if (button.equals("Button3"))
            modifiers |= InputEvent.BUTTON3_DOWN_MASK;
        else {
            if (isPopupTrigger) {
                modifiers |= InputEvent.BUTTON3_DOWN_MASK;
                menuModifiersTxt += "+Button3";
            } else {
                modifiers |= InputEvent.BUTTON1_DOWN_MASK;
                menuModifiersTxt += "+Button1";
            }
        }
        String modifiersText = menuModifiersTxt.substring(0, menuModifiersTxt.lastIndexOf('+'));
        KeyStroke ks = new KeyStrokeParser(modifiersText + "+A").getKeyStroke();
        modifiers |= getDownModifierMask(ks.getModifiers());
    }

    private int getDownModifierMask(int modifiers) {
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

    public void setHoverDelay(int delay) {
        hoverDelay = delay;
    }

}
