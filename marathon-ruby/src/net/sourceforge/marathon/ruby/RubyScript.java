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
package net.sourceforge.marathon.ruby;

import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ApplicationLaunchException;
import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.api.ScriptException;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.player.Marathon;
import net.sourceforge.marathon.player.MarathonPlayer;
import net.sourceforge.marathon.recorder.ITopLevelWindowListener;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.runtime.JavaRuntime;
import net.sourceforge.marathon.runtime.SetupState;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.RubyProc;
import org.jruby.embed.io.WriterOutputStream;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.GlobalVariable;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyScript implements IScript, ITopLevelWindowListener {

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("(.*)/(.*\\(.*)", Pattern.DOTALL | Pattern.MULTILINE);
    public static final String PROP_APPLICATION_RUBYPATH = "marathon.application.rubypath";
    public static final String PROP_APPLICATION_RUBYHOME = "marathon.application.rubyhome";

    private final class FixtureRunner implements Runnable {
        protected static final String MODULE = "Ruby Script";
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
                                RuntimeLogger.getRuntimeLogger().info(MODULE, "Running fixture setup...");
                                debugger.run("$marathon.execFixtureSetup");
                                RuntimeLogger.getRuntimeLogger().info(MODULE, "Running fixture setup... Done");
                                runMain();
                            } catch (Throwable t) {
                                isTeardownCalled = true;
                                debugger.run("$marathon.execFixtureTeardown");
                                setupFailed = true;
                                thread.interrupt();
                            }
                        } catch (Throwable t) {
                            thread.interrupt();
                        }
                    }

                });
                if (setupFailed)
                    return;
                try {
                    debugger.run("$marathon.execTestSetup");
                    debugger.run("$marathon.execTest($test)");
                } finally {
                    isTeardownCalled = true;
                    debugger.run("$marathon.execFixtureTeardown");
                }
            } else {
                debugger.run("$marathon.execTestSetup");
                debugger.run("$marathon.execTest($test)");
            }
        }
    }

    private String script;
    private String filename;
    private ComponentFinder finder;
    private static Ruby interpreter;
    private Marathon runtime;
    private RubyDebugger debugger;
    private ModuleList moduleList;
    private boolean isTeardownCalled = false;
    private ArrayList<String> assertionProviderList;
    private final WindowMonitor windowMonitor;
    private Throwable runMainFailure;
    private MarathonAppType type;

    public RubyScript(Writer out, Writer err, String script, String filename, ComponentFinder resolver, boolean isDebugging,
            WindowMonitor windowMonitor, MarathonAppType type) {
        this.script = script;
        this.filename = filename;
        finder = resolver;
        this.windowMonitor = windowMonitor;
        this.type = type;
        loadScript(out, err, isDebugging);
        readGlobals();
        debugger = new RubyDebugger(interpreter);
        if (windowMonitor != null)
            windowMonitor.addTopLevelWindowListener(this);
    }

    private void readGlobals() {
        IRubyObject marathon = interpreter.evalScriptlet("$marathon");
        interpreter.evalScriptlet("$test = proc { test }");
        runtime = (Marathon) JavaEmbedUtils.rubyToJava(interpreter, marathon, Marathon.class);
    }

    private void loadScript(Writer out, Writer err, boolean isDebugging) {
        try {
            if (interpreter == null) {
                RubyInstanceConfig config = new RubyInstanceConfig();
                if (isDebugging)
                    config.setCompileMode(CompileMode.OFF);
                List<String> loadPaths = new ArrayList<String>();
                setModule(loadPaths);
                String appRubyPath = System.getProperty(PROP_APPLICATION_RUBYPATH);
                if (appRubyPath != null) {
                    StringTokenizer tok = new StringTokenizer(appRubyPath, ";");
                    while (tok.hasMoreTokens())
                        loadPaths.add(tok.nextToken().replace('/', File.separatorChar));
                }
                config.setOutput(new PrintStream(new WriterOutputStream(out)));
                config.setError(new PrintStream(new WriterOutputStream(err)));
                interpreter = JavaEmbedUtils.initialize(loadPaths, config);
                interpreter.evalScriptlet("require 'marathon/results'");
            }
            if (type == MarathonAppType.JAVA) {
                interpreter.evalScriptlet("require 'marathon/playback'");
            } else if (type == MarathonAppType.WEB) {
                interpreter.evalScriptlet("require 'marathon/playbackweb'");
            }
            moduleList = new ModuleList(interpreter, Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS));
            loadAssertionProviders();
            defineVariable("test_file", filename);
            defineVariable("test_name", getTestName());
            defineVariable("project_dir", System.getProperty("marathon.project.dir"));
            defineVariable("marathon_home", System.getProperty("marathon.home"));
            defineVariable("marathon_project_name", System.getProperty("marathon.project.name"));
            defineVariable("marathon_project_dir", System.getProperty("marathon.project.dir"));
            defineVariable("marathon_fixture_dir", System.getProperty("marathon.fixture.dir"));
            defineVariable("marathon_test_dir", System.getProperty("marathon.test.dir"));
            interpreter.executeScript(script, filename);
        } catch (RaiseException e) {
            e.printStackTrace();
            // e.getCause().printStackTrace();
            throw new ScriptException(e.getException().toString());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ScriptException(t.getMessage());
        }
    }

    private void defineVariable(String variable, String value) {
        try {
            GlobalVariable v = new GlobalVariable(interpreter, "$" + variable, interpreter.newString(value));
            interpreter.defineVariable(v);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ScriptException(t.getMessage());
        }
    }

    private void defineVariable(String variable, int value) {
        try {
            GlobalVariable v = new GlobalVariable(interpreter, "$" + variable, interpreter.newFixnum(value));
            interpreter.defineVariable(v);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ScriptException(t.getMessage());
        }
    }

    private void defineVariable(String variable, double value) {
        try {
            GlobalVariable v = new GlobalVariable(interpreter, "$" + variable, interpreter.newFloat(value));
            interpreter.defineVariable(v);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ScriptException(t.getMessage());
        }
    }

    private String getTestName() {
        String name = new File(filename).getName().toUpperCase();
        if (name.endsWith(".RB"))
            return name.substring(0, name.length() - 3);
        return name;
    }

    private void setModule(List<String> segments) {
        try {
            String[] ModuleDirs = Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS);
            for (int i = 0; i < ModuleDirs.length; i++) {
                segments.add(new File(ModuleDirs[i]).getCanonicalFile().getCanonicalPath());
            }
            // segments.add(new
            // File(System.getProperty(Constants.PROP_FIXTURE_DIR)).getCanonicalFile().getCanonicalPath());
            File assertionDir = new File(System.getProperty(Constants.PROP_PROJECT_DIR), "Assertions");
            if (assertionDir.exists() && assertionDir.isDirectory())
                segments.add(assertionDir.getCanonicalFile().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exec(String function) {
        try {
            Matcher matcher = FUNCTION_PATTERN.matcher(function);
            if (matcher.matches()) {
                interpreter.evalScriptlet("require '" + matcher.group(1) + "'");
                interpreter.evalScriptlet(matcher.group(2));
            } else {
                interpreter.evalScriptlet(function);
            }
        } catch (Throwable t) {
            if (t instanceof ScriptException)
                throw (ScriptException) t;
            throw new ScriptException(t.getMessage());
        }
    }

    public IDebugger getDebugger() {
        return debugger;
    }

    public Module getModuleFuctions() {
        return moduleList.getTop();
    }

    public IPlayer getPlayer(IPlaybackListener playbackListener, PlaybackResult result) {
        runtime.finder = finder;
        runtime.result = result;
        return new MarathonPlayer(this, playbackListener, result);
    }

    public void runFixtureSetup() {
        final IRubyObject fixture = getFixture();
        invokeAndWaitForWindow(new Runnable() {
            public void run() {
                try {
                    fixture.callMethod(interpreter.getCurrentContext(), "setup");
                    runMain();
                } catch (Throwable t) {
                    t.printStackTrace();
                    synchronized (RubyScript.this) {
                        RubyScript.this.notifyAll();
                    }
                }
            }
        });
        if (fixture.respondsTo("test_setup")) {
            try {
                fixture.callMethod(interpreter.getCurrentContext(), "test_setup");
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ScriptException(t.getMessage());
            }
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

    private void invokeAndWaitForWindow(Runnable runnable) {
        runMainFailure = null;
        Thread thread;
        synchronized (RubyScript.this) {
            thread = new Thread(runnable, "Marathon Player");
            thread.start();
        }
        int applicationWaitTime = Integer.parseInt(System.getProperty(Constants.PROP_APPLICATION_LAUNCHTIME, "60000"));
        if (windowMonitor == null || applicationWaitTime == 0 || windowMonitor.getAllWindows().size() > 0) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            return;
        }
        synchronized (RubyScript.this) {
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
    }

    protected IRubyObject getFixture() {
        return interpreter.evalScriptlet("$fixture");
    }

    public void runFixtureTeardown() {
        if (!isTeardownCalled) {
            isTeardownCalled = true;
            getFixture().callMethod(interpreter.getCurrentContext(), "teardown");
        }
    }

    public void topLevelWindowCreated(Window arg0) {
        synchronized (RubyScript.this) {
            notifyAll();
        }
    }

    public void topLevelWindowDestroyed(Window arg0) {
    }

    public Ruby getInterpreter() {
        return interpreter;
    }

    public void attachPlaybackListener(IPlaybackListener listener) {
        debugger.setListener(listener);
    }

    public Runnable playbackBody(boolean shouldRunFixture, Thread playbackThread) {
        return new FixtureRunner(shouldRunFixture, playbackThread);
    }

    public String evaluate(String code) {
        try {
            return interpreter.evalScriptlet(code).inspect().toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

    void loadAssertionProviders() {
        File defaultDir = new File(System.getProperty(Constants.PROP_PROJECT_DIR), "Assertions");
        if (defaultDir.exists() && defaultDir.isDirectory())
            loadAssertionProvidersFromDir(defaultDir);
    }

    private void findAssertionProviderMethods() {
        IRubyObject ro = interpreter.evalScriptlet("Object.private_instance_methods");
        Object[] methods = ((RubyArray) JavaEmbedUtils.rubyToJava(interpreter, ro, String[].class)).toArray();
        assertionProviderList = new ArrayList<String>();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].toString().startsWith("marathon_assert_"))
                assertionProviderList.add(methods[i].toString());
        }
    }

    private void loadAssertionProvidersFromDir(final File dirFile) {
        File[] listFiles = dirFile.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return dir.equals(dirFile) && name.endsWith(".rb");
            }
        });
        for (int i = 0; i < listFiles.length; i++) {
            try {
                String fileName = listFiles[i].getName();
                interpreter.executeScript("require '" + fileName.substring(0, fileName.length() - 3) + "'", "<internal>");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        findAssertionProviderMethods();
    }

    public boolean isCustomAssertionsAvailable() {
        return true;
    }

    public String[][] getCustomAssertions(Object o) {
        if (!(o instanceof MComponent))
            return new String[0][0];
        MComponent component = (MComponent) o;
        ArrayList<String[]> assertions = new ArrayList<String[]>();

        if (assertionProviderList == null)
            return null;
        for (String method : assertionProviderList) {
            RubyProc proc = (RubyProc) interpreter.evalScriptlet("proc { |x| " + method + " x }");
            RubyArray ret = (RubyArray) proc.call(interpreter.getCurrentContext(),
                    new IRubyObject[] { JavaEmbedUtils.javaToRuby(interpreter, component) });
            for (int i = 0; i < ret.size(); i++) {
                RubyArray object = (RubyArray) ret.get(i);
                String[] assertion = new String[3];
                assertion[0] = object.get(0).toString();
                assertion[1] = object.get(1).toString();
                assertion[2] = object.get(2).toString();
                assertions.add(assertion);
            }
        }
        return assertions.toArray(new String[assertions.size()][]);
    }

    public void setDataVariables(Properties dataVariables) {
        Set<Entry<Object, Object>> set = dataVariables.entrySet();
        for (Entry<Object, Object> entry : set) {
            try {
                String key = (String) entry.getKey();
                String value = entry.getValue().toString();
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") || value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                    defineVariable(key, value);
                } else {
                    try {
                        int v = Integer.parseInt(value);
                        defineVariable(key, v);
                    } catch (NumberFormatException e) {
                        try {
                            double v = Double.parseDouble(value);
                            defineVariable(key, v);
                        } catch (NumberFormatException e1) {
                            defineVariable(key, value);
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ScriptException(t.getMessage());
            }
        }
    }

}
