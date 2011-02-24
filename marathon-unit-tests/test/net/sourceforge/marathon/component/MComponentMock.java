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
import java.awt.Point;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;

import net.sourceforge.marathon.MessageList;
import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class MComponentMock extends MComponent {
    private MessageList messages = new MessageList();
    private JLabel label = new JLabel(""); // Use a JLabel to ensure
                                           // String

    // encoding is correct

    public MComponentMock() {
        super(null, null, null, WindowMonitor.getInstance());
    }

    public MComponentMock(Component component, String name) {
        super(component, name, null, WindowMonitor.getInstance());
    }

    public ComponentFinder getDummyResolver() {
        ComponentFinder resolver = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(),
                new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()) {
            public MComponent getMComponentByComponent(Component obj) {
                return MComponentMock.this;
            }

            public MComponent getMComponentById(ComponentId id) {
                return MComponentMock.this;
            }

            public MComponent getMComponentByComponent(Component object, Point location) {
                return MComponentMock.this;
            }

            public Window getWindow() {
                return new JDialog();
            }
        };
        return resolver;
    }

    /**
     * By pass the getWindow() of the default Componentresolver so that we can
     * test it
     * 
     * @return
     */
    public ComponentFinder getResolver() {
        ComponentFinder resolver = new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(),
                new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()) {
            public MComponent getMComponentByComponent(Component obj) {
                return getMComponentByComponent(MComponentMock.this.getComponent(), null, null);
            }

            public MComponent getMComponentById(ComponentId id) {
                return getMComponentByComponent(MComponentMock.this.getComponent(), null, null);
            }

            public MComponent getMComponentByComponent(Component object, Point location) {
                return getMComponentByComponent(MComponentMock.this.getComponent(), null, null);
            }

            public Window getWindow() {
                return new JDialog();
            }
        };
        if (getComponent() == null) {
            throw new RuntimeException("Please call MComponentMock.setComponent(Component c) before this method.");
        }
        return resolver;
    }

    public MessageList getHistory() {
        return messages;
    }

    public void click(int numberOfClicks, boolean isPopupTrigger) {
        messages.add("click(" + numberOfClicks + ", " + isPopupTrigger + ")");
    }

    public void click(int numberOfClicks, int modifiers, Point position) {
        if (position == null) {
            click(numberOfClicks, (modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0);
            return;
        }
        modifiers &= ~InputEvent.BUTTON1_DOWN_MASK;
        messages.add("click(" + numberOfClicks + ", " + position.x + ", " + position.y + ", " + "\""
                + ClickAction.getModifiersText(modifiers) + getButtonText(modifiers) + "\")");
    }

    private String getButtonText(int modifiers) {
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0)
            return "+Button2";
        else if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0)
            return "+Button3";
        return "";
    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        messages.add("setText(" + text + ")");
        label.setText(text);
    }

    public int clickNeeded(MouseEvent e) {
        return ClickAction.RECORD_CLICK;
    }
}
