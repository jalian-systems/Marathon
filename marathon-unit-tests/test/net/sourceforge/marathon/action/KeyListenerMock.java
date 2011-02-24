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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import net.sourceforge.marathon.util.OSUtils;

public class KeyListenerMock extends Assert implements KeyListener {
    private List<KeyEvent> events = new ArrayList<KeyEvent>();
    private Iterator<KeyEvent> eventIter;

    public void keyTyped(KeyEvent e) {
        events.add(e);
    }

    public void keyPressed(KeyEvent e) {
        events.add(e);
    }

    public void keyReleased(KeyEvent e) {
        events.add(e);
    }

    public void assertPressed(int keycode) {
        assertNext(keycode, KeyEvent.KEY_PRESSED);
    }

    private void assertNext(int keycode, int type) {
        if (eventIter == null)
            eventIter = events.iterator();
        assertTrue("no more events", eventIter.hasNext());
        KeyEvent event = (KeyEvent) eventIter.next();
        assertEquals(type, event.getID());
        assertEquals(OSUtils.KeyEventGetKeyText(keycode), OSUtils.KeyEventGetKeyText(event.getKeyCode()));
    }

    public void assertReleased(int keycode) {
        assertNext(keycode, KeyEvent.KEY_RELEASED);
    }

    public void assertTyped(int keycode) {
        assertNext(KeyEvent.VK_UNDEFINED, KeyEvent.KEY_TYPED);
    }

    public void assertEmpty() {
        assertTrue(!eventIter.hasNext());
    }
}
