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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IJavaRuntimeInstantiator;
import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.MarathonRuntimeException;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.util.ConsoleWriter;
import net.sourceforge.marathon.util.Retry;
import net.sourceforge.marathon.util.StreamPumper;
import net.sourceforge.rmilite.Client;
import net.sourceforge.rmilite.RemoteInvocationException;

/**
 * JavaRuntimeLeash is just a wrapper around a remote java runtime running
 * inside another process. It handles the interface to the remote process,
 * redirecting the output of its std in and std out, as well as arbitrating
 * communication with this process.
 * 
 * This decorate JavaRuntime which is a proxy for JavaRuntimeLauncher
 */
public class JavaRuntimeLeash implements IMarathonRuntime {

    private static Logger logger = Logger.getLogger(JavaRuntimeLeash.class.getName());

    private static class StdOut extends ConsoleWriter {
        public StdOut(final IConsole console) {
            super(new Writer() {
                public void write(char cbuf[], int off, int len) throws IOException {
                    console.writeStdOut(cbuf, off, len);
                }

                public void flush() throws IOException {
                }

                public void close() throws IOException {
                }
            });
        }
    }

    private static class StdErr extends ConsoleWriter {
        public StdErr(final IConsole console) {
            super(new Writer() {
                public void write(char cbuf[], int off, int len) throws IOException {
                    console.writeStdErr(cbuf, off, len);
                }

                public void flush() throws IOException {
                }

                public void close() throws IOException {
                }
            });
        }
    }

    private IMarathonRuntime impl;
    private StdOut stdout;
    private StdErr stderr;
    private IJavaRuntimeInstantiator instantiator;
    private Thread shThread;

    public JavaRuntimeLeash(Client client, Process process, IConsole console, ILogger logViewLogger) {
        stdout = new StdOut(console);
        stderr = new StdErr(console);
        redirectOutput(process.getInputStream(), stdout);
        redirectOutput(process.getErrorStream(), stderr);
        addShutdownHook();
        getInstantiator(client);
        try {
            impl = instantiator.createRuntime(console, logViewLogger);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public IScript createScript(String content, String filename, boolean isDebugging) {
        return createScript(content, filename, false, isDebugging);
    }

    public IScript createScript(String content, String filename, boolean isRecording, boolean isDebugging) {
        return impl.createScript(content, filename, isRecording, isDebugging);
    }

    public void startRecording(IRecorder recorder) {
        impl.startRecording(recorder);
    }

    public void stopRecording() {
        try {
            impl.stopRecording();
        } catch (RemoteInvocationException e) {
            throw new MarathonRuntimeException();
        }
    }

    public void destroy() {
        flush();
        logger.info("Destroying the VM");
        try {
            impl.aboutToDestroy();
        } catch (Throwable t) {
        }
        try {
            impl.destroy();
        } catch (Throwable t) {
        }
        if (shThread != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shThread);
            } catch (IllegalStateException e) {
            }
        }
    }

    private void flush() {
        try {
            stdout.flush();
            stderr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redirectOutput(InputStream inputStream, Writer writer) {
        new StreamPumper(inputStream, writer).start();
    }

    /**
     * don't let these bad boys hang around after this vm shuts down.
     */
    private void addShutdownHook() {
        shThread = new Thread() {
            public void run() {
                logger.info("Destroying the VM");
                JavaRuntimeLeash.this.destroy();
            }
        };
        Runtime.getRuntime().addShutdownHook(shThread);
    }

    private void getInstantiator(final Client client) {
        final Exception[] passback = new Exception[1];
        try {
            new Retry("looking up runtime instantiator", 600, 100, new Retry.Attempt() {
                public void perform() {
                    try {
                        instantiator = (IJavaRuntimeInstantiator) client.lookup(IJavaRuntimeInstantiator.class);
                        instantiator.setProperties(getProperties());
                        passback[0] = null;// clear the exception
                    } catch (Exception e) {
                        passback[0] = e;
                        retry();
                    }
                }

                private Properties getProperties() {
                    Properties props = System.getProperties();
                    Properties vmProps = new Properties();
                    Set<Object> keys = props.keySet();
                    for (Iterator<Object> iter = keys.iterator(); iter.hasNext();) {
                        String key = (String) iter.next();
                        if (key.startsWith(Constants.PROP_PROPPREFIX)) {
                            vmProps.put(key.substring(Constants.PROP_PROPPREFIX.length()), props.getProperty(key));
                        } else if (key.startsWith("marathon")) {
                            vmProps.put(key, props.getProperty(key));
                        }
                    }
                    return vmProps;
                }
            });
        } finally {
            // ensure the last exception gets printed if we failed
            if (passback[0] != null) {
                System.err.println("Error in JavaRuntimeLeash");
                passback[0].printStackTrace();
            }
        }
    }

    public void startApplication() {
        impl.startApplication();
    }

    public void stopApplication() {
        impl.stopApplication();
    }

    public Module getModuleFunctions() {
        return impl.getModuleFunctions();
    }

    public void exec(String function) {
        impl.exec(function);
    }

    public void setRawRecording(boolean selected) {
        impl.setRawRecording(selected);
    }

    public String evaluate(String code) {
        return impl.evaluate(code);
    }

    public WindowId getTopWindowId() {
        return impl.getTopWindowId();
    }

    public File getScreenCapture() {
        return impl.getScreenCapture();
    }

    public boolean isCustomAssertionsAvailable() {
        return impl.isCustomAssertionsAvailable();
    }

    public String[][] getCustomAssertions(Object mcomponent) {
        return impl.getCustomAssertions(mcomponent);
    }

    public void insertScript(String function) {
        impl.insertScript(function);
    }

    public void aboutToDestroy() {
        // TODO Auto-generated method stub

    }
}
