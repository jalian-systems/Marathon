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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Set;

import net.sourceforge.rmilite.RemoteInvocationException;

public class RemoteInvocationHandlerImpl extends UnicastRemoteObject implements IRemoteInvocationHandler {

    private static final long serialVersionUID = 1L;
    private Object impl;
    private Set<Class<?>> exportedInterfaces;

    // Looks like quite a few objects are not being kept in the heap while using
    // RMI atleast in Marathon. That
    // is causing the problem of NoSuchObject exceptions at different places. To
    // solve the issue we added this
    // array and keep references to the remote objects. Not a clean solution,
    // but WTH - a solution nevertheless.
    private static ArrayList<RemoteInvocationHandlerImpl> keepAround = new ArrayList<RemoteInvocationHandlerImpl>();

    public RemoteInvocationHandlerImpl(Object impl, Set<Class<?>> exportedInterfaces) throws RemoteException {
        this.impl = impl;
        this.exportedInterfaces = exportedInterfaces;
        keepAround.add(this);
        if (impl == null)
            throw new RemoteException("Impl is NULL!");
    }

    public Object invoke(String methodName, Class<?>[] paramTypes, Object[] args) throws RemoteException {
        try {
            if (args == null) {
                args = new Object[0];
            }
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof IRemoteInvocationHandler) {
                    IRemoteInvocationHandler handler = (IRemoteInvocationHandler) args[i];
                    args[i] = LocalInvocationHandlerImpl.create(paramTypes[i], handler, exportedInterfaces);
                }
            }
            Method method = impl.getClass().getMethod(methodName, paramTypes);
            JREFixer.fixThreadAppContext();
            Object returnValue = method.invoke(impl, args);

            if (returnValue != null && exportedInterfaces.contains(method.getReturnType())) {
                IRemoteInvocationHandler remoteHandler = new RemoteInvocationHandlerImpl(returnValue, exportedInterfaces);
                returnValue = RemoteObject.toStub(remoteHandler);
            }

            return returnValue;
        } catch (InvocationTargetException e) {
            throw new RemoteInvocationException(methodName, e.getTargetException());
        } catch (Exception e) {
            throw new RemoteInvocationException(methodName, e);
        }
    }

}
