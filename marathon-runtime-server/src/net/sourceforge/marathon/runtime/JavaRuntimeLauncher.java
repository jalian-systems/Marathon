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
package net.sourceforge.marathon.runtime;

import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IJavaRuntimeInstantiator;
import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.player.MarathonJava;
import net.sourceforge.rmilite.Server;

/**
 * This is the server for the separate VM. It utilies rmi-lite for communication
 */
public class JavaRuntimeLauncher {
    static final Class<?>[] EXPORTED_INTERFACES = { IMarathonRuntime.class, IScript.class, IPlayer.class, IDebugger.class,
            ILogger.class };
    public static Thread currentThread;

    public static void main(String[] args) {
        MarathonJava.class.getName();
        MComponent.init();
        JavaRuntimeLauncher launcher = new JavaRuntimeLauncher();
        try {
            launcher.launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentThread = Thread.currentThread();
    }

    private void launch(final String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        Server server;
        server = new Server(port);
        server.publish(IJavaRuntimeInstantiator.class, new JavaRuntimeInstantiatorImpl(dropFirstArg(args)), EXPORTED_INTERFACES);
    }

    private String[] dropFirstArg(String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

}
