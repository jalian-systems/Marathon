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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;

import org.junit.Test;

public class TestStreamPumper {
    @Test
    public void testPumpsCharacters() throws Exception {
        String pumpMe = "pump it up!\n";
        SynchronizedStringWriter pumpTo = new SynchronizedStringWriter('\n');
        StreamPumper pumper = new StreamPumper(new ByteArrayInputStream(pumpMe.getBytes()), pumpTo);
        synchronized (pumpTo) {
            pumper.start();
            pumpTo.wait(3000);
        }
        assertEquals(pumpMe, pumpTo.getBuffer().toString());
    }

    @Test
    public void testSwitchesWritersToPumpToInMidStream() throws Exception {
        String pumpMe = "pump it up!\nthat is me in the corner\n";
        SynchronizedStringWriter pumpTo = new SynchronizedStringWriter('\n');
        SynchronizedStringWriter pumpToNext = new SynchronizedStringWriter('\n');
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        StreamPumper pumper = new StreamPumper(pis, pumpTo);
        pos.write("pump it up!\n".getBytes());
        synchronized (pumpTo) {
            pumper.start();
            pumpTo.wait(3000);
            pumper.setWriter(pumpToNext);
            pumpTo.notify();
        }
        pos.write("that is me in the corner\n".getBytes());
        synchronized (pumpToNext) {
            pumpToNext.wait(3000);
            pumpToNext.notify();
        }
        assertEquals("pump it up!\n", pumpTo.getBuffer().toString());
        assertEquals("that is me in the corner\n", pumpToNext.getBuffer().toString());
    }

    /**
     * notify when a certain string has been written
     */
    private static class SynchronizedStringWriter extends StringWriter {
        private char notifyOn;

        public SynchronizedStringWriter(char c) {
            this.notifyOn = c;
        }

        public synchronized void write(int c) {
            super.write(c);
            if (c == notifyOn) {
                notify();
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }
}
