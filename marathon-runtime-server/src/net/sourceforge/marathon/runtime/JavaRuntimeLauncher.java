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

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IJavaRuntimeInstantiator;
import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.player.MarathonJava;
import net.sourceforge.marathon.util.Retry;
import net.sourceforge.rmilite.Server;

/**
 * This is the server for the separate VM. It utilies rmi-lite for communication
 */
public class JavaRuntimeLauncher {
    static final Class<?>[] EXPORTED_INTERFACES = { IMarathonRuntime.class, IScript.class, IPlayer.class, IDebugger.class,
            ILogger.class };
    public static Thread currentThread;

    public static void main(String[] args) {
        quickAndDirtyFixForProblemWithWebStartInJava7u25();
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
        logmsg("JavaRuntimeLauncher.launch(): start");
        new Retry("Attempting to restart server", 600, 100, new Retry.Attempt() {
            public void perform() {
                try {
                    int port = Integer.parseInt(args[0]);
                    Server server = new Server(port);
                    server.publish(IJavaRuntimeInstantiator.class, new JavaRuntimeInstantiatorImpl(dropFirstArg(args)),
                            EXPORTED_INTERFACES);
                } catch (Exception e) {
                    e.printStackTrace();
                    retry();
                }
            }
        });
        logmsg("JavaRuntimeLauncher.launch(): end");
    }

    private String[] dropFirstArg(String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    public static void premain(final String args) throws Exception {
        logmsg(null);
        dumpInfo();
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                if (event instanceof WindowEvent && event.getID() == WindowEvent.WINDOW_OPENED) {
                    Window window = ((WindowEvent) event).getWindow();
                    logmsg("Window Opened: " + window.getClass().getName());
                    try {
                        Method method = window.getClass().getMethod("getTitle");
                        try {
                            logmsg("Window Title: " + method.invoke(window));
                        } catch (IllegalArgumentException e) {
                        } catch (IllegalAccessException e) {
                        } catch (InvocationTargetException e) {
                        }
                    } catch (SecurityException e) {
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
        if (isJreLocator())
            return;
        if (isWebStart()) {
            if (isAppleJava() && !Boolean.getBoolean("jnlpx.relaunch")) {
                logmsg("Mac: Not a relaunch. Returning.");
                return;
            }
            logmsg("Mac: Hooking on to the application.");
        }
        main(new String[] { args });
    }

    private static boolean isJreLocator() {
        return System.getProperty("sun.java.command").startsWith("com.sun.deploy.panel.JreLocator");
    }

    public static boolean isWebStart() {
        return System.getProperty("sun.java.command").startsWith("com.sun.javaws.Main");
    }

    public static boolean isAppleJava() {
        return System.getProperty("java.vendor").startsWith("Apple");
    }

    private static void dumpInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        logmsg("Args: " + runtimeMXBean.getInputArguments());
        logmsg("Main Class: " + System.getProperty("sun.java.command"));
    }

    public static void logmsg(String s) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(new File("/tmp", "marathon.log"), s != null));
            if (s != null)
                ps.println(s);
            ps.close();
        } catch (FileNotFoundException e) {
        }
    }

    public static void agentmain(String args) throws Exception {
        System.err.println("agentMain method invoked with args: {} and inst: {}" + args);
    }

    private static void quickAndDirtyFixForProblemWithWebStartInJava7u25() {
        if (!"1.7.0_25".equals(System.getProperty("java.version"))) {
            return;
        }
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        // Change context in all future threads
                        final Field field = EventQueue.class.getDeclaredField("classLoader");
                        field.setAccessible(true);
                        final EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
                        field.set(eq, cl);
                        // Change context in this thread
                        Thread.currentThread().setContextClassLoader(cl);
                    } catch (Exception ex) {
                        // Call to java logging causes NPE :-( ...
                        ex.printStackTrace(System.err);
                        System.err.println("Unable to apply 'fix' for java 1.7u25");
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.println("Unable to apply 'fix' for java 1.7u25");
        }
    }
}
