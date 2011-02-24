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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import net.sourceforge.marathon.MessageList;

public class MockComponentListener implements FocusListener, MouseListener {
    private MessageList events;
    private String prefix;

    public MockComponentListener(MessageList events, String prefix) {
        this.events = events;
        this.prefix = prefix;
    }

    private void log(String message) {
        events.add(prefix + ":" + message);
    }

    // focusListener methods
    public void focusGained(FocusEvent e) {
        log("focusGained");
    }

    public void focusLost(FocusEvent e) {
        log("focusLost");
    }

    // mouseListener methods
    public void mouseClicked(MouseEvent e) {
        log("mouseClicked");
    }

    public void mousePressed(MouseEvent e) {
        log("mousePressed");
    }

    public void mouseReleased(MouseEvent e) {
        log("mouseReleased");
    }

    public void mouseEntered(MouseEvent e) {
        log("mouseEntered");
    }

    public void mouseExited(MouseEvent e) {
        log("mouseExited");
    }
}
