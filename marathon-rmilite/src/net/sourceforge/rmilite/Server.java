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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashSet;

import net.sourceforge.rmilite.impl.RemoteInvocationHandlerImpl;

public class Server {
    public static final int DEFAULT_PORT = Registry.REGISTRY_PORT;

    private Registry registry;

    public Server() throws RemoteException {
        this(DEFAULT_PORT);
    }

    public Server(int port) throws RemoteException {
        registry = LocateRegistry.createRegistry(port);
    }

    public void publish(Class<?> iface, Object impl, Class<?>[] exportedInterfaces) throws RemoteException {
        RemoteInvocationHandlerImpl handler = new RemoteInvocationHandlerImpl(impl, new HashSet<Class<?>>(
                Arrays.asList(exportedInterfaces)));
        registry.rebind(iface.getName(), handler);
    }
}
