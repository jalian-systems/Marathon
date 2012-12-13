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
package net.sourceforge.rmilite.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Set;

import net.sourceforge.rmilite.RemoteInvocationException;

public class LocalInvocationHandlerImpl implements InvocationHandler {
    private IRemoteInvocationHandler handler;
    private Set<Class<?>> exportedInterfaces;

    public LocalInvocationHandlerImpl(IRemoteInvocationHandler handler, Set<Class<?>> exportedInterfaces) {
        this.handler = handler;
        this.exportedInterfaces = exportedInterfaces;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (args == null) {
            args = new Object[0];
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (exportedInterfaces.contains(type) && args[i] != null) {
                RemoteInvocationHandlerImpl obj = new RemoteInvocationHandlerImpl(args[i], exportedInterfaces);
                args[i] = RemoteObject.toStub(obj);
            }
        }
        Object returnValue = invokeRemote(method, parameterTypes, args);
        if (returnValue instanceof IRemoteInvocationHandler) {
            returnValue = LocalInvocationHandlerImpl.create(method.getReturnType(), (IRemoteInvocationHandler) returnValue,
                    exportedInterfaces);
        }
        return returnValue;
    }

    private Object invokeRemote(Method method, Class<?>[] parameterTypes, Object[] args) throws Throwable {
        try {
            return handler.invoke(method.getName(), parameterTypes, args);
        } catch (RemoteInvocationException e) {
            rethrow(method, e);
        } catch (RemoteException e) {
            throw new RemoteInvocationException(method.getName(), e);
        }
        return null;
    }

    public void rethrow(Method method, Exception e) throws Throwable {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            if (exceptionTypes[i].isAssignableFrom(cause.getClass())) {
                throw cause;
            }
        }
        throw e;
    }

    public static Object create(Class<?> iface, IRemoteInvocationHandler remote, Set<Class<?>> exportedInterfaces) {
        return Proxy.newProxyInstance(LocalInvocationHandlerImpl.class.getClassLoader(), new Class[] { iface },
                new LocalInvocationHandlerImpl(remote, exportedInterfaces));
    }

}
