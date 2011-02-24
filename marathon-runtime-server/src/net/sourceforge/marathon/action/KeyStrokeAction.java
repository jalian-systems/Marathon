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

import javax.swing.KeyStroke;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.event.FireableKeyEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.KeyStrokeParser;
import net.sourceforge.marathon.util.Retry;

public class KeyStrokeAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private KeyStroke keyStroke;
    private char keyChar;
    private boolean withComponent = true;

    public KeyStrokeAction(String sequence, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(new ComponentId("KeyStrokeAction"), scriptModel, windowMonitor);
        KeyStrokeParser keyStrokeParser = new KeyStrokeParser(sequence);
        keyStroke = keyStrokeParser.getKeyStroke();
        keyChar = keyStrokeParser.getKeyChar();
        withComponent = false;
    }

    public KeyStrokeAction(KeyStroke ks, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(new ComponentId("KeyStrokeAction"), scriptModel, windowMonitor);
        this.keyStroke = ks;
        withComponent = false;
    }

    public KeyStrokeAction(ComponentId componentId, KeyStroke ks, char keyChar, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(componentId, scriptModel, windowMonitor);
        this.keyStroke = ks;
        this.keyChar = keyChar;
    }

    public KeyStrokeAction(ComponentId componentId, String sequence, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(componentId, scriptModel, windowMonitor);
        KeyStrokeParser keyStrokeParser = new KeyStrokeParser(sequence);
        keyStroke = keyStrokeParser.getKeyStroke();
        keyChar = keyStrokeParser.getKeyChar();
    }

    public void play(final ComponentFinder resolver) {
        if (withComponent) {
            MComponent component = resolver.getMComponentById(getComponentId());
            if (component.getComponent() != null) {
                waitForWindowActive(getParentWindow(component.getComponent()));
                component.getComponent().requestFocus();
                play(component.getComponent());
                return;
            }
        }
        // Delay and recheck just to make sure the app is not messing with
        // focus itself
        try {
            new Retry(new ComponentNotFoundException("Cannot find the component with focus, to receive the key stroke!",
                    scriptModel, windowMonitor), 1000, 3, new Retry.Attempt() {
                public void perform() {
                    if (resolver.getWindow().getFocusOwner() == null) {
                        retry();
                    }
                }
            });
        } catch (TestException e) {
            e.captureScreen();
            throw e;
        }
        play(resolver.getWindow().getFocusOwner());
    }

    public void play(Component component) {
        FireableKeyEvent event = new FireableKeyEvent(component, keyStroke.getModifiers());
        event.fire(keyStroke.getKeyCode(), keyChar);
        component.repaint();
        AWTSync.sync();
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForKeystroke(keyChar, keyStroke, getComponentId(),
                KeyStrokeParser.getTextForKeyChar(keyChar));
    }

    public int hashCode() {
        return keyStroke.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof KeyStrokeAction))
            return false;
        return keyStroke.equals(((KeyStrokeAction) obj).keyStroke);
    }

    public javax.swing.KeyStroke getKeyStroke() {
        return keyStroke;
    }

}
