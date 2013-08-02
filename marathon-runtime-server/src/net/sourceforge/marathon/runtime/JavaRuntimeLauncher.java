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
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
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
import net.sourceforge.rmilite.Server;

/**
 * This is the server for the separate VM. It utilies rmi-lite for communication
 */
public class JavaRuntimeLauncher {
    static final Class<?>[] EXPORTED_INTERFACES = { IMarathonRuntime.class, IScript.class, IPlayer.class, IDebugger.class,
            ILogger.class };
    public static Thread currentThread;
    private static File logFile;

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

    public static void premain(final String args) throws Exception {
        logmsg(null);
        quickAndDirtyFixForProblemWithWebStartInJava7u25();
        dumpLaunchInfo();
        if (System.getProperty("sun.java.command") == null) {
            logmsg("No mainclass in java command: ignore this launch");
            return;
        }
        if (isJreLocator()) {
            logmsg("JRELocator: ignore this launch");
            return;
        }
        if (isWebStart()) {
            if (isAppleJava() && !Boolean.getBoolean("jnlpx.relaunch")) {
                logmsg("Mac: Not a relaunch. ignore this launch.");
                return;
            }
        }
        logmsg("Listening to window open events to hook");
        final AWTEventListener l = new AWTEventListener() {
            private boolean notDone = true;
            private boolean webstart = false;

            public void eventDispatched(AWTEvent event) {
                if (event.getID() != WindowEvent.WINDOW_OPENED)
                    return;
                Window window = ((WindowEvent) event).getWindow();
                String cname = window.getClass().getName();
                logmsg("Window Opened: " + cname);
                String title = null;
                try {
                    Method method = window.getClass().getMethod("getTitle");
                    title = (String) method.invoke(window);
                    logmsg("Window Title: " + title);
                } catch (Exception e) {
                }
                if (cname.startsWith("com.sun.javaws")) {
                    webstart = true;
                    logmsg("JavaWS internal window: ignore this launch");
                    return;
                }
                if (webstart && title != null && title.startsWith("Starting application...")) {
                    logmsg("JavaWS start application window: ignore this launch");
                    return;
                }
                if (notDone) {
                    notDone = false;
                    logmsg("Hooking to Marathon client");
                    main(new String[] { args });
                    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(l, AWTEvent.WINDOW_EVENT_MASK);
    }

    private static void dumpLaunchInfo() {
        logmsg("Java command: " + System.getProperty("sun.java.command"));
        logmsg("Vendor: " + System.getProperty("java.vendor"));
        logmsg("Java Version: " + System.getProperty("java.version"));
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

    public static void logmsg(String s) {
        try {
            if (logFile == null)
                logFile = File.createTempFile("marathon", ".log");
            PrintStream ps = new PrintStream(new FileOutputStream(logFile, s != null));
            if (s != null)
                ps.println(System.currentTimeMillis() + ":" + s);
            else
                System.out.println("Log at: " + logFile.getAbsolutePath());
            ps.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
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
            Runnable doRun = new Runnable() {
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
            };
            if (SwingUtilities.isEventDispatchThread())
                doRun.run();
            else
                SwingUtilities.invokeAndWait(doRun);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.println("Unable to apply 'fix' for java 1.7u25");
        }
    }
}
