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

import net.sourceforge.rmilite.impl.LocalInvocationHandlerImpl;
import net.sourceforge.rmilite.impl.IRemoteInvocationHandler;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.HashSet;
import java.util.Set;

public class Client {
    private Set<Class<?>> exportedInterfaces = new HashSet<Class<?>>();
    private String serverHost;
    private int serverPort;

    public Client(String serverHost) {
        this(serverHost, Server.DEFAULT_PORT);
    }

    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void exportInterface(Class<?> iface) {
        exportedInterfaces.add(iface);
    }

    public Object lookup(Class<?> iface) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
        IRemoteInvocationHandler remote = (IRemoteInvocationHandler) registry.lookup(iface.getName());
        return LocalInvocationHandlerImpl.create(iface, remote, exportedInterfaces);
    }
}
