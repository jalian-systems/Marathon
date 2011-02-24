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
package net.sourceforge.rmilite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class TestClientServer {

    private static final String EXCEPTION_MESSAGE = "this is the exception";
    private static Server server;
    private Client client;
    private InterfaceImplForTesting serverTester;
    private InterfaceForTesting clientTester;

    @Before
    public void setUp() throws Exception {
        if (server == null) {
            server = new Server();
        }
        serverTester = new InterfaceImplForTesting();
        server.publish(InterfaceForTesting.class, serverTester, new Class[] { CallbackInterfaceForTesting.class });

        client = new Client("localhost");
        client.exportInterface(CallbackInterfaceForTesting.class);
        clientTester = (InterfaceForTesting) client.lookup(InterfaceForTesting.class);
        assertNotNull("could not find interface", clientTester);
    }

    @Test
    public void testBasicMethodInvocation() throws Exception {
        clientTester.call();
        assertEquals(true, serverTester.called);
    }

    @Test
    public void testInvokesOverloadedMethods() throws Exception {
        Integer num = new Integer(10);
        clientTester.overload(num);
        String name = "Rupert";
        clientTester.overload(name);
        assertEquals(num, serverTester.integer);
        assertEquals(name, serverTester.name);
    }

    @Test
    public void testCallback() throws Exception {
        CallbackImplForTesting testCallback = new CallbackImplForTesting();
        ValueObjectForTesting TestValueObject = new ValueObjectForTesting();
        clientTester.testCallback(testCallback, TestValueObject);

        assertEquals(true, testCallback.called);
        assertEquals(false, TestValueObject.called);
    }

    @Test
    public void testCallbackOnReturnValue() throws Exception {
        CallbackInterfaceForTesting callback = clientTester.testReturnCallback();
        ValueObjectForTesting stupid = clientTester.returnStupid();
        callback.call();
        stupid.call();
        assertEquals("callback not invoked on server", true, serverTester.returnCallback.called);
        assertEquals("value object invoked on server", false, serverTester.returnStupid.called);
    }

    @Test
    public void testCallbackOnCallback() throws Exception {
        clientTester.testDoubleCallback(new CallbackImplForTesting());
        assertEquals(true, serverTester.doubleCallback.called);
    }

    @Test
    public void testCheckedExceptionThrownOnRemoteHost() throws Exception {
        ExceptionForTesting prototype = new ExceptionForTesting();
        try {
            clientTester.throwTestException(prototype);
            fail("no exception");
        } catch (ExceptionForTesting e) {
            assertNotSame("hey, this wasn't a remote call", prototype, e);
        }
    }

    @Test
    public void testCheckedExceptionSubclassThrownOnRemoteHost() throws Exception {
        ExceptionForTesting prototype = new ExceptionForTesting.SubClass();
        try {
            clientTester.throwTestException(prototype);
            fail("no exception");
        } catch (ExceptionForTesting.SubClass e) {
            assertNotSame("hey, this wasn't a remote call", prototype, e);
        }
    }

    @Test
    public void testRuntimeExceptionThrownOnRemoteHost() throws Exception {
        server.publish(InterfaceForTesting.class, new Barfer(new TestRuntimeException()), new Class[0]);
        clientTester = (InterfaceForTesting) client.lookup(InterfaceForTesting.class);
        try {
            clientTester.call();
            fail("did not throw exception");
        } catch (TestRuntimeException e) {
            assertEquals(EXCEPTION_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void testErrorThrownOnRemoteHost() throws Exception {
        server.publish(InterfaceForTesting.class, new Barfer(new TestError()), new Class[0]);
        clientTester = (InterfaceForTesting) client.lookup(InterfaceForTesting.class);
        try {
            clientTester.call();
            fail("did not throw exception");
        } catch (TestError e) {
            assertEquals(EXCEPTION_MESSAGE, e.getMessage());
        }
    }

    public static class Barfer extends InterfaceImplForTesting {
        private TestRuntimeException rte;
        private Error err;

        public Barfer(TestRuntimeException rte) {
            this.rte = rte;
        }

        public Barfer(Error err) {
            this.err = err;
        }

        public void call() {
            if (rte != null)
                throw rte;
            throw err;
        }
    }

    public static class TestRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TestRuntimeException() {
            super(EXCEPTION_MESSAGE);
        }
    }

    public static class TestError extends Error {
        private static final long serialVersionUID = 1L;

        public TestError() {
            super(EXCEPTION_MESSAGE);
        }
    }
}
