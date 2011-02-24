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

public class InterfaceImplForTesting implements InterfaceForTesting {
    public boolean called = false;
    public Integer integer;
    public String name;
    public CallbackImplForTesting returnCallback = new CallbackImplForTesting();
    public ValueObjectForTesting returnStupid = new ValueObjectForTesting();
    public CallbackImplForTesting doubleCallback;

    public void call() {
        called = true;
    }

    public void throwTestException(ExceptionForTesting e) throws ExceptionForTesting {
        throw e;
    }

    public void overload(Integer integer) {
        this.integer = integer;
    }

    public void overload(String name) {
        this.name = name;
    }

    public void testCallback(CallbackInterfaceForTesting callback, ValueObjectForTesting stupid) {
        callback.call();
        stupid.call();
    }

    public void testDoubleCallback(CallbackInterfaceForTesting callback) {
        doubleCallback = new CallbackImplForTesting();
        callback.call(doubleCallback);
    }

    public CallbackInterfaceForTesting testReturnCallback() {
        return returnCallback;
    }

    public ValueObjectForTesting returnStupid() {
        return returnStupid;
    }

}
