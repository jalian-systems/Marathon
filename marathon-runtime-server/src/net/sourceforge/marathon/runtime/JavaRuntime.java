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

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.action.ScreenCaptureAction;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.MarathonException;
import net.sourceforge.marathon.api.MarathonRuntimeException;
import net.sourceforge.marathon.api.ScriptException;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.DelegatingNamingStrategy;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.RecordingEventListener;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.ConsoleWriter;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.rmilite.RemoteInvocationException;

/**
 * This is a proxy for JavaRuntime This also invoke the main method to jump
 * start the application
 */
public class JavaRuntime implements IMarathonRuntime {
    private static class JavaVersionScriptElement implements IScriptElement {
        private static final long serialVersionUID = 1L;
        private final String javaRecordedVersionTag;

        public JavaVersionScriptElement(String javaRecordedVersionTag) {
            this.javaRecordedVersionTag = javaRecordedVersionTag;
        }

        public String toScriptCode() {
            return Indent.getIndent() + javaRecordedVersionTag;
        }

        public ComponentId getComponentId() {
            return null;
        }

        public WindowId getWindowId() {
            return null;
        }

        public boolean isUndo() {
            return false;
        }

        public IScriptElement getUndoElement() {
            return null;
        }

    }

    JavaVersionScriptElement javaVersionScriptElement;

    private static class ScriptOutput extends ConsoleWriter {
        public ScriptOutput(final IConsole console) {
            super(new Writer() {
                public void write(char cbuf[], int off, int len) throws IOException {
                    console.writeScriptOut(cbuf, off, len);
                }

                public void flush() throws IOException {
                }

                public void close() throws IOException {
                }
            });
        }
    }

    private static class ScriptError extends ConsoleWriter {
        public ScriptError(final IConsole console) {
            super(new Writer() {
                public void write(char cbuf[], int off, int len) throws IOException {
                    console.writeScriptErr(cbuf, off, len);
                }

                public void flush() throws IOException {
                }

                public void close() throws IOException {
                }
            });
        }
    }

    private ComponentFinder finder;
    private RecordingEventListener eventListener;
    private IScript script;
    private ScriptOutput consoleOut;
    private ScriptError consoleErr;
    private IRecorder currentRecorder = null;
    private IScriptModelServerPart scriptModel;
    private WindowMonitor windowMonitor;
    private INamingStrategy<Component, Component> namingStrategy;
    private String[] args;
    private static Logger logger = Logger.getLogger(JavaRuntime.class.getName());
    private static JavaRuntime instance;

    public JavaRuntime(IConsole console, String[] args) {
        logger.info("Creating a JavaRuntime");
        setInstance(this);
        windowMonitor = WindowMonitor.getInstance();
        scriptModel = ScriptModelServerPart.getModelServerPart();
        javaVersionScriptElement = new JavaVersionScriptElement(scriptModel.getJavaRecordedVersionTag());
        namingStrategy = windowMonitor.getNamingStrategy();
        consoleOut = new ScriptOutput(console);
        consoleErr = new ScriptError(console);
        addShutdownHook();
        this.args = args;
        runMain(args);
    }

    public String[] getArgs() {
        return args;
    }

    private static void setInstance(JavaRuntime inst) {
        instance = inst;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (currentRecorder != null)
                    currentRecorder.abortRecording();
                aboutToDestroy();
            }
        });
    }

    public void destroy() {
        logger.info("Destroying the runtime");
        Timer t = new Timer();
        int delay = 1;
        if(shutdownInProgress())
        	delay = 20000;
		t.schedule(new TimerTask() {
            @Override public void run() {
                logger.info("Halting the VM in timer task");
                Runtime.getRuntime().halt(0);
            }
        }, delay);
    }

    private boolean shutdownInProgress() {
    	try {
    		Thread t = new Thread();
    		Runtime.getRuntime().addShutdownHook(t);
    		Runtime.getRuntime().removeShutdownHook(t);
    	} catch(IllegalStateException e) {
    		return true ;
    	}
		return false;
	}

	public IScript createScript(String content, String filename, boolean isRecording, boolean isDebugging) {
        script = scriptModel.getScript(consoleOut, consoleErr, content, filename, getComponentResolver(isRecording), isDebugging,
                windowMonitor, MarathonAppType.JAVA);
        return script;
    }

    public void startRecording(IRecorder recorder) {
        if (javaVersionScriptElement != null) {
            recorder.record(javaVersionScriptElement);
            javaVersionScriptElement = null;
        }
        currentRecorder = recorder;
        eventListener = new RecordingEventListener(recorder, this, scriptModel, windowMonitor);
        eventListener.startListening(getComponentResolver(true));
    }

    public void stopRecording() {
        try {
            eventListener.stopListening();
        } catch (RemoteInvocationException e) {
            throw new MarathonRuntimeException();
        }
    }

    public ComponentFinder getComponentResolver(boolean isRecording) {
        if (finder == null) {
            finder = createResolver(isRecording);
        }
        finder.setRecording(isRecording);
        return finder;
    }

    private ComponentFinder createResolver(boolean isRecording) {
        try {
            return new ComponentFinder(isRecording, windowMonitor.getNamingStrategy(), new ResolversProvider(), scriptModel,
                    windowMonitor);
        } catch (Exception e) {
            throw new MarathonException("instantiating component resolver: " + e.getMessage(), e);
        }
    }

    private void runMain(final String[] args) {
        // Check to see if you need to jump start the main class
        String mainClassName = getMainClass();
        if (mainClassName == null || "".equals(mainClassName))
            return;
        // if yes, invoke it
        Class<?> mainClass = null;
        try {
            mainClass = getClass().getClassLoader().loadClass(mainClassName);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            throw new MarathonException(e1.getMessage() + " in " + getMainClass() + ".main()", e1);
        }
        Method main = null;
        try {
            main = mainClass.getMethod("main", new Class[] { String[].class });
        } catch (SecurityException e1) {
            e1.printStackTrace();
            throw new MarathonException(e1.getMessage() + " in " + getMainClass() + ".main()", e1);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
            throw new MarathonException(e1.getMessage() + " in " + getMainClass() + ".main()", e1);
        }
        try {
            main.invoke(null, new Object[] { args });
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            throw new MarathonException(cause.getMessage() + " in " + getMainClass() + ".main()", cause);
        } catch (Exception e) {
            throw new MarathonException(e.getMessage() + " in " + getMainClass() + ".main()", e);
        }
    }

    private String getMainClass() {
        return System.getProperty(Constants.PROP_PROFILE_MAIN_CLASS, "");
    }

    public void startApplication() {
        try {
            script.runFixtureSetup();
        } catch (Throwable t) {
            throw new ScriptException(t.getMessage());
        }
    }

    public void stopApplication() {
        script.runFixtureTeardown();
    }

    public Module getModuleFunctions() {
        return script.getModuleFuctions();
    }

    public void exec(String function) {
        try {
            eventListener.stopListening();
            finder.setRecording(false);
            script.exec(function);
        } finally {
            finder.setRecording(true);
            eventListener.startListening(finder);
        }
    }

    public void insertScript(String function) {
        Window window = windowMonitor.getWindow(windowMonitor.getWindowId().getTitle());
        finder.push(window);
        exec(function);
        finder.pop();
    }

    public void setRawRecording(boolean selected) {
        finder.setRawRecording(selected);
    }

    public String evaluate(String code) {
        return script.evaluate(code);
    }

    public WindowId getTopWindowId() {
        return windowMonitor.getWindowId();
    }

    public File getScreenCapture() {
        try {
            String imgDir = System.getProperty(Constants.PROP_REPORT_DIR);
            if (imgDir != null) {
                File tempFile = File.createTempFile("screencap", ".png", new File(imgDir));
                new ScreenCaptureAction(tempFile.getAbsolutePath(), getTopWindowId().getTitle(), scriptModel, windowMonitor)
                        .play(finder);
                return tempFile;
            } else
                System.err.println("getScreenCapture(): Image directory is not set");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isCustomAssertionsAvailable() {
        return script.isCustomAssertionsAvailable();
    }

    public String[][] getCustomAssertions(Object mcomponent) {
        return scriptModel.getCustomAssertions(script, (MComponent) mcomponent);
    }

    public ComponentFinder getFinder() {
        return finder;
    }

    public RecordingEventListener getEventListener() {
        return eventListener;
    }

    public IScript getScript() {
        return script;
    }

    public IRecorder getCurrentRecorder() {
        return currentRecorder;
    }

    public IScriptModelServerPart getScriptModel() {
        return scriptModel;
    }

    public WindowMonitor getWindowMonitor() {
        return windowMonitor;
    }

    public INamingStrategy<Component, Component> getNamingStrategy() {
        return namingStrategy;
    }

    public static JavaRuntime getInstance() {
        if (instance == null) {
            instance = createAJavaRuntime();
        }
        return instance;
    }

    private static JavaRuntime createAJavaRuntime() {
        return new JavaRuntime(new IConsole() {
            public void writeScriptOut(char[] cbuf, int off, int len) {
                try {
                    System.out.write(new String(cbuf, off, len).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void writeScriptErr(char[] cbuf, int off, int len) {
                try {
                    System.err.write(new String(cbuf, off, len).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void writeStdOut(char[] cbuf, int off, int len) {
                try {
                    System.out.write(new String(cbuf, off, len).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void writeStdErr(char[] cbuf, int off, int len) {
                try {
                    System.err.write(new String(cbuf, off, len).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void clear() {
            }

        }, new String[] {});
    }

    public void aboutToDestroy() {
        new DelegatingNamingStrategy<Component>().saveIfNeeded();
    }

}
