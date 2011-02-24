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
package net.sourceforge.marathon.display;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.UndeclaredThrowableException;
import javax.swing.SwingUtilities;

public class TestDisplayEventQueue {
    private MockExceptionReporter reporter;
    private DisplayEventQueue queue;

    @Before
    public void setUp() throws Exception {
        reporter = new MockExceptionReporter();
        queue = new DisplayEventQueue(reporter);
        queue.attach();
    }

    @After
    public void tearDown() throws Exception {
        try {
            queue.detach();
        } finally {
        }
    }

    @Test
    public void testAttachTwice() throws Exception {
        try {
            queue.attach();
            fail("attaching the display event queue twice is illegal");
        } catch (UnsupportedOperationException e) {
            // this is expected
        }
    }

    @Test
    public void testUncaughtRuntimeExceptionInsideAWTDispatchThread() throws Exception {
        final RuntimeException exception = new RuntimeException("this is a runtimeException");
        throwInAWTThread(exception);
        assertEquals(1, reporter.exceptions);
        assertSame(exception, reporter.last);
    }

    @Test
    public void testUncaughtErrorInsideAWTDisptachThread() throws Exception {
        final Error err = new Error("error dude!");
        throwInAWTThread(err);
        assertEquals(1, reporter.exceptions);
        assertSame(err, reporter.last);
    }

    private void throwInAWTThread(final Throwable err) throws InterruptedException {
        synchronized (err) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            synchronized (err) {
                                err.notify();
                            }
                        }
                    });
                    if (err instanceof RuntimeException)
                        throw (RuntimeException) err;
                    if (err instanceof Error)
                        throw (Error) err;
                    throw new UndeclaredThrowableException(err);
                }
            });
            err.wait(10000);
        }
    }

    private class MockExceptionReporter implements IExceptionReporter {
        int exceptions = 0;
        private Throwable last;

        public void reportException(Throwable t) {
            exceptions++;
            last = t;
        }
    }
}
