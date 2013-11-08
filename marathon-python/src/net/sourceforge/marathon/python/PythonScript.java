/*******************************************************************************
 *  
 *  $Id: PythonScript.java 278 2009-01-20 12:37:13Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
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
package net.sourceforge.marathon.python;

import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ApplicationLaunchException;
import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptException;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.player.Marathon;
import net.sourceforge.marathon.player.MarathonPlayer;
import net.sourceforge.marathon.recorder.ITopLevelWindowListener;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.runtime.JavaRuntime;
import net.sourceforge.marathon.runtime.SetupState;
import net.sourceforge.marathon.util.ClassPathHelper;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyMethod;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySyntaxError;
import org.python.core.PySystemState;
import org.python.core.PyTableCode;
import org.python.util.PythonInterpreter;

public class PythonScript implements IScript, ITopLevelWindowListener {

    public static final String PROP_APPLICATION_PYTHONPATH = "marathon.application.pythonpath";
    public static final String PROP_APPLICATION_PYTHONHOME = "marathon.application.pythonhome";

    private final class FixtureRunner implements Runnable {
        private final boolean fixture;
        private Thread thread;
        private boolean setupFailed = false;

        private FixtureRunner(boolean fixture, Thread playbackThread) {
            this.fixture = fixture;
            this.thread = playbackThread;
        }

        public void run() {
            if (fixture) {
                invokeAndWaitForWindow(new Runnable() {
                    public void run() {
                        try {
                            try {
                                debugger.run("marathon.execFixtureSetup(fixture)");
                                runMain();
                            } catch (Throwable t) {
                                debugger.run("marathon.execFixtureTeardown(fixture)");
                                setupFailed = true;
                                thread.interrupt();
                            }
                        } catch (Throwable t) {
                            thread.interrupt();
                        }
                    }

                });
                if (setupFailed) {
                    return;
                }
                try {
                    debugger.run("marathon.execTestSetup(fixture)");
                    debugger.run("marathon.execTest(test)");
                } finally {
                    debugger.run("marathon.execFixtureTeardown(fixture)");
                }
            } else {
                debugger.run("marathon.execTestSetup(fixture)");
                debugger.run("marathon.execTest(test)");
            }
        }
    }

    private String script;
    private String filename;
    private ComponentFinder finder;
    // TODO: Make the interpreter static to make this work for WebTesting
    private PythonInterpreter interpreter;
    private PyObject testFunction;
    private Marathon runtime;
    private MarathonPlayer player;
    private ModuleList moduleList;
    private IDebugger debugger;

    private static boolean init = false;
    private static Object initLock = new Object();
    private final WindowMonitor windowMonitor;

    private Throwable runMainFailure = null;
    private MarathonAppType type;

    private static void initializePythonRuntime() {
        String pythonPath = computePythonPath();
        Properties props = System.getProperties();
        props.setProperty("python.path", pythonPath);
        PythonInterpreter.initialize(System.getProperties(), props, new String[] { "" });
    }

    public static String computePythonPath() {
        String pythonPath = null;
        ArrayList<String> pathSegments = new ArrayList<String>();
        setFixture(pathSegments);
        setProjectHome(pathSegments);

        String systemPythonPath = System.getProperty("python.path", null);
        if (systemPythonPath != null)
            addToPath(pathSegments, systemPythonPath);
        String appPythonPath = System.getProperty(PROP_APPLICATION_PYTHONPATH);
        if (appPythonPath != null) {
            StringTokenizer tok = new StringTokenizer(appPythonPath, ";");
            while (tok.hasMoreTokens())
                addToPath(pathSegments, tok.nextToken().replace('/', File.separatorChar));
        }
        String path = ClassPathHelper.getClassPath(PythonInterpreter.class);
        path = new File(path).getParentFile().getAbsolutePath();
        String pyHome = new File(path, "Lib").getAbsolutePath();
        String property = System.getProperty("python.home");
        if (property == null)
            System.setProperty("python.home", pyHome);
        try {
            addToPath(pathSegments, getPathForPython("/libpy/marathon"));
            addToPath(pathSegments, pyHome);
            pythonPath = getPathFromSegments(pathSegments);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ScriptException("unable to intialize marathon runtime."
                    + " Check to make sure that marathon modules are in the python path:\n" + pythonPath);
        }
        return pythonPath;
    }

    private static void addToPath(List<String> pathSegments, String path) {
        if (!pathSegments.contains(path))
            pathSegments.add(path);
    }

    private static String getPathFromSegments(ArrayList<String> pathSegments) {
        StringBuffer path = new StringBuffer();
        if (pathSegments.size() == 0)
            return "";
        for (int i = 0; i < pathSegments.size() - 1; i++)
            path.append(pathSegments.get(i)).append(File.pathSeparator);
        path.append(pathSegments.get(pathSegments.size() - 1));
        return path.toString();
    }

    private static String getPathForPython(String resource) {
        URL r = PythonScript.class.getResource(resource);
        if (r == null)
            return null;
        String path = r.toString();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (path.startsWith("jar:")) {
            path = path.substring("jar:".length());
            int i = path.lastIndexOf('!');
            path = path.substring(0, i) + path.substring(i + 1);
        }
        if (path.startsWith("file:/")) {
            path = path.substring("file:".length());
        } else {
            throw new RuntimeException("path should start w/ file:/");
        }
        path = new File(path).toString();
        path = path.substring(0, path.lastIndexOf(File.separator));
        return path;
    }

    public PythonScript(Writer out, Writer err, String script, String filename, ComponentFinder resolver,
            WindowMonitor windowMonitor, MarathonAppType type) {
        synchronized (initLock) {
            if (!init) {
                initializePythonRuntime();
                init = true;
            }
        }
        this.windowMonitor = windowMonitor;
        this.script = script;
        this.filename = filename;
        this.type = type;
        finder = resolver;
        loadScript(out, err);
        readGlobals();
        if (windowMonitor != null)
            windowMonitor.addTopLevelWindowListener(this);
    }

    public IPlayer getPlayer(IPlaybackListener playbackListener, PlaybackResult result) {
        readTestFunction();
        runtime.result = result;
        runtime.finder = finder;
        player = new MarathonPlayer(this, playbackListener, result);
        return player;
    }

    private static class WriterOutputStream extends OutputStream {
        private final Writer out;

        public WriterOutputStream(Writer out) {
            this.out = out;
        }

        public void write(int b) throws IOException {
            out.write(b);
        }
    }

    private void loadScript(Writer out, Writer err) {
        interpreter = new PythonInterpreter();
        interpreter.setOut(new WriterOutputStream(out));
        interpreter.setErr(new WriterOutputStream(err));
        try {
            clearModuleDefinitions();
            interpreterExec("import __builtin__");
            interpreterExec("from marathon.playback import *");
            defineVariable("__test_file__", filename);
            defineVariable("__test_name__", getTestName());
            defineVariable("__project_dir__", System.getProperty("marathon.project.dir"));
            defineVariable("__marathon_home__", System.getProperty("marathon.home"));
            defineVariable("__marathon_project_name__", System.getProperty("marathon.project.name"));
            defineVariable("__marathon_project_dir__", System.getProperty("marathon.project.dir"));
            defineVariable("__marathon_fixture_dir__", System.getProperty("marathon.fixture.dir"));
            defineVariable("__marathon_test_dir__", System.getProperty("marathon.test.dir"));

            moduleList = new ModuleList(interpreter, Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS));
            moduleList.evaluateModulesFromRoot();

        } catch (PySyntaxError e) {
            raiseSyntaxError(e);
        } catch (PyException e) {
            e.printStackTrace();
            raisePythonError(e);
        }
        try {
            interpreter.execfile(new ByteArrayInputStream(script.getBytes()), filename);
        } catch (PySyntaxError e) {
            raiseSyntaxError(e);
        } catch (PyException e) {
            e.printStackTrace();
            raisePythonError(e);
        }
    }

    private String getTestName() {
        String name = new File(filename).getName().toUpperCase();
        if (name.endsWith(".PY"))
            return name.substring(0, name.length() - 3);
        return name;
    }

    private void defineVariable(String varname, String value) {
        PyObject builtin = interpreterEval("__builtin__");
        PyObject pyString;
        if (value == null)
            pyString = Py.None;
        else
            pyString = new PyString(value);
        builtin.__setattr__(varname, pyString);
    }

    public void attachPlaybackListener(IPlaybackListener listener) {
        debugger.setListener(listener);
    }

    public Runnable playbackBody(final boolean shouldRunFixture, Thread playbackThread) {
        return new FixtureRunner(shouldRunFixture, playbackThread);
    }

    private void readGlobals() {
        readRuntime();
        readTestExecutor();
        debugger = new PythonDebugger(this);
    }

    private PyObject getFixture() {
        PyObject fixture = interpreter.get("fixture");
        if (fixture == null) {
            throw new ScriptException("no fixture found for test " + filename);
        }
        return fixture;
    }

    private void readRuntime() {
        runtime = (Marathon) interpreter.get("marathon", Marathon.class);
    }

    private void readTestExecutor() {
        PyObject marathon = interpreter.get("marathon");
        PyMethod testExecutor = (PyMethod) marathon.__getattr__("execTest");
        if (testExecutor == null) {
            throw new ScriptException("no python test executor, something is wrong with the marathon runtime!");
        }
    }

    private void readTestFunction() {
        testFunction = interpreter.get("test");
        if (testFunction == null) {
            throw new ScriptException("there is no function test() defined in " + filename);
        }
    }

    private void raisePythonError(PyException e) {
        StringTokenizer toker = new StringTokenizer(e.toString(), "\n");
        Stack<String> tokens = new Stack<String>();
        while (toker.hasMoreTokens())
            tokens.push(toker.nextToken());
        throw new ScriptException(tokens.pop().toString() + tokens.pop());
    }

    private void raiseSyntaxError(PySyntaxError e) {
        String error = e.toString();
        throw new ScriptException(error.substring(error.lastIndexOf("Syntax")));
    }

    /**
     * this is called because by default, jython will not reload an external
     * module even if the file which defines it has changed.
     */
    private void clearModuleDefinitions() {
        interpreterExec("import sys");
        interpreterExec("from java.awt import Color");
        PySystemState system = (PySystemState) interpreter.get("sys");
        PyStringMap modules = (PyStringMap) system.__getattr__("modules");
        PyObject builtin = modules.get(new PyString("__builtin__"));
        modules.clear();
        modules.__setitem__("__builtin__", builtin);
    }

    public void runFixtureSetup() {
        invokeAndWaitForWindow(new Runnable() {
            public void run() {
                PyObject fixture = getFixture();
                try {
                    fixture.invoke("setup");
                    runMain();
                } catch (Throwable t) {
                    t.printStackTrace();
                    synchronized (PythonScript.this) {
                        PythonScript.this.notifyAll();
                    }
                }
            }
        });
        PyObject fixture = getFixture();
        if (fixture.__findattr__("test_setup") != null) {
            try {
                fixture.invoke("test_setup");
            } catch (Throwable t) {
                if (t instanceof PyException)
                    raisePythonError((PyException) t);
                throw new ScriptException("Could not invoke test_setup: " + t.getMessage());
            }
        }
    }

    public void runFixtureTeardown() {
        getFixture().invoke("teardown");
    }

    public Module getModuleFuctions() {
        return moduleList.getRoot();
    }

    public String[][] getArgumentsFor(DefaultMutableTreeNode node) {
        String fName = (String) node.getUserObject();
        PyFunction function = (PyFunction) interpreterEval(fName);
        int argcount = ((PyTableCode) function.func_code).co_argcount;
        String[] arguments = new String[argcount];
        String[] varNames = ((PyTableCode) function.func_code).co_varnames;
        for (int i = 0; i < argcount; i++) {
            arguments[i] = varNames[i];
        }
        String[] defaults = new String[0];
        if (function.func_defaults != null && function.func_defaults.length > 0) {
            defaults = new String[function.func_defaults.length];
            for (int i = 0; i < function.func_defaults.length; i++) {
                defaults[i] = function.func_defaults[i].toString();
            }
        }
        return new String[][] { arguments, defaults };
    }

    public void exec(String function) {
        try {
            interpreterExec(function);
        } catch (RuntimeException t) {
            t.printStackTrace();
        }
    }

    private void runMain() {
        SetupState.setupDone = true;
        if (type != MarathonAppType.JAVA)
            return;
        String[] args = JavaRuntime.getInstance().getArgs();
        if (args.length == 0)
            return;
        String mainClass = args[0];
        args = dropFirstArg(args);
        try {
            Class<?> klass = Class.forName(mainClass);
            Method method = klass.getMethod("main", String[].class);
            method.invoke(null, (Object) args);
        } catch (Exception e) {
            runMainFailure = e;
        }
    }

    private String[] dropFirstArg(String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    public void topLevelWindowCreated(Window arg0) {
        synchronized (PythonScript.this) {
            notifyAll();
        }
    }

    public void topLevelWindowDestroyed(Window arg0) {
    }

    private void invokeAndWaitForWindow(Runnable runnable) {
        runMainFailure = null;
        synchronized (PythonScript.this) {
            new Thread(runnable, "Marathon Player").start();
        }
        int applicationWaitTime = Integer.parseInt(System.getProperty(Constants.PROP_APPLICATION_LAUNCHTIME, "60000"));
        if (windowMonitor == null || applicationWaitTime == 0 || windowMonitor.getAllWindows().size() > 0)
            return;
        synchronized (PythonScript.this) {
            int ntries = 10;
            while (ntries-- > 0 && windowMonitor.getAllWindows().size() <= 0) {
                try {
                    wait(applicationWaitTime / 10);
                } catch (InterruptedException e) {
                }
                if (windowMonitor.getAllWindows().size() > 0 || runMainFailure != null)
                    break;
            }
            if (runMainFailure != null) {
                runMainFailure.printStackTrace();
                throw new ApplicationLaunchException("Could not execute main class: " + runMainFailure.getClass().getName() + " ("
                        + runMainFailure.getMessage() + ")");
            }
            if (windowMonitor.getAllWindows().size() <= 0)
                throw new ApplicationLaunchException("AUT Mainwindow not opened\n"
                        + "You can increase the timeout by setting marathon.application.launchtime property in project file");
        }
        return;
    }

    private static void setProjectHome(ArrayList<String> pathSegments) {
        try {
            addToPath(pathSegments, new File(System.getProperty(Constants.PROP_PROJECT_DIR)).getCanonicalFile().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setFixture(List<String> pathSegments) {
        try {
            addToPath(pathSegments, new File(System.getProperty(Constants.PROP_FIXTURE_DIR)).getCanonicalFile().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PyObject interpreterEval(String fName) {
        return interpreter.eval(fName);
    }

    public void interpreterExec(String fName) {
        interpreter.exec(fName);
    }

    public IDebugger getDebugger() {
        return debugger;
    }

    public String evaluate(String code) {
        try {
            try {
                return interpreterEval(code).toString();
            } catch (PySyntaxError e) {
                interpreterExec(code);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

    public boolean isCustomAssertionsAvailable() {
        return false;
    }

    public String[][] getCustomAssertions(Object component) {
        return null;
    }

    public void setDataVariables(Properties dataVariables) {
        Set<Entry<Object, Object>> set = dataVariables.entrySet();
        PyObject builtin = interpreterEval("__builtin__");
        for (Entry<Object, Object> entry : set) {
            try {
                String key = (String) entry.getKey();
                String value = entry.getValue().toString();
                PyObject pyValue;
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") || value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                    pyValue = new PyString(value);
                } else {
                    try {
                        int v = Integer.parseInt(value);
                        pyValue = new PyInteger(v);
                    } catch (NumberFormatException e) {
                        try {
                            double v = Double.parseDouble(value);
                            pyValue = new PyFloat(v);
                        } catch (NumberFormatException e1) {
                            pyValue = new PyString(value);
                        }
                    }
                }
                builtin.__setattr__(key, pyValue);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ScriptException(t.getMessage());
            }
        }
    }
}
