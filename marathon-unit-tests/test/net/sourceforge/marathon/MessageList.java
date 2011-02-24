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
package net.sourceforge.marathon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.AbstractButton;
import junit.framework.Assert;

/**
 * maintains a queue of messages - used for testing, you can assert that a given
 * message is the next one
 */
public class MessageList {
    private LinkedList<String> list = new LinkedList<String>();

    public void add(String message) {
        list.add(message);
    }

    public void clear() {
        list.clear();
    }

    public void addActionListener(AbstractButton button, final String message) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add(message);
            }
        });
    }

    public String nextMessage() {
        Assert.assertTrue("there were no more messages", list.size() > 0);
        return (String) list.removeFirst();
    }

    /**
     * assert that the given message is at the head of the queue and then pop it
     * off
     */
    public void assertNextMessage(String action) {
        String availMessages = "Messages: ";
        String next = nextMessage();
        if (!action.equals(next)) {
            while (list.size() > 0)
                availMessages += "[" + list.removeFirst() + "] ";
        }
        Assert.assertEquals(availMessages, action, next);
    }

    /**
     * assert that the given message is in the queue and then pop off messages
     * till there
     */
    public void assertNextMessageInList(String action) {
        String availMessages = "Messages: ";
        String next = nextMessage();
        while (!action.equals(next) && list.size() > 0) {
            availMessages += "[" + next + "] ";
            next = nextMessage();
        }
        Assert.assertEquals(availMessages, action, next);
    }

    public void assertEmpty() {
        String availMessages = "Messages: ";
        boolean empty = list.size() == 0;
        while (list.size() > 0)
            availMessages += "[" + list.removeFirst() + "] ";
        Assert.assertTrue(availMessages, empty);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<String> i = list.iterator(); i.hasNext();) {
            buffer.append(i.next() + "\n");
        }
        return buffer.toString();
    }
}
