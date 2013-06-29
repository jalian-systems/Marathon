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
package net.sourceforge.marathon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import junit.framework.TestResult;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.display.DisplayWindow;
import net.sourceforge.marathon.display.SplashScreen;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.junit.textui.TestRunner;
import net.sourceforge.marathon.mpf.MPFSelection;
import net.sourceforge.marathon.runtime.NullLogger;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.MPFUtils;
import net.sourceforge.marathon.util.OSUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Main entry point into Marathon application.
 */
public class Main {
    private static ArgumentProcessor argProcessor = new ArgumentProcessor();

    /**
     * Entry point into Marathon application. Invoke main on the command line
     * using the <code>net.sourceforge.marathon.Main</code> class.
     * 
     * @param args
     *            - Arguments passed on the command line. Invoke with
     *            <code>-help</code> to see available options.
     */
    public static void main(String[] args) {
        String vers = System.getProperty("mrj.version");
        if (vers == null)
            System.setProperty("mrj.version", "1070.1.6.0_26-383");
        OSUtils.setLookAndFeel();
        argProcessor.process(args);
        if (!argProcessor.isBatchMode())
            runGUIMode();
        else {
            runBatchMode();
        }
    }

    private static void setDefaultIndent() {
        String guiceModuleName = System.getProperty("marathon.guice.module", MarathonGuiceModule.class.getName());
        Module guiceModule;
        try {
            Class<?> klass = Class.forName(guiceModuleName);
            guiceModule = (Module) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            guiceModule = new MarathonGuiceModule();
        }
        Injector injector = Guice.createInjector(guiceModule);
        IEditorProvider provider = injector.getInstance(IEditorProvider.class);
        Indent.setDefaultIndent(provider.getTabConversion(), provider.getTabSize());
    }

    /**
     * Run Marathon in batch mode.
     */
    private static void runBatchMode() {
        String projectDir = argProcessor.getProjectDirectory();
        if (projectDir == null) {
            argProcessor.help("No project directory");
            return;
        }
        if (projectDir.endsWith(".mpf") && new File(projectDir).isFile()) {
            argProcessor.help("A marathon project file is given.\nUse project directory instead");
            return;
        }
        processMPF(projectDir);
        setDefaultIndent();
        setLogConfiguration(projectDir);
        RuntimeLogger.setRuntimeLogger(new NullLogger());
        TestRunner aTestRunner = new TestRunner();
        try {
            TestResult r = aTestRunner.runTests(argProcessor);
            if (!r.wasSuccessful())
                System.exit(junit.textui.TestRunner.FAILURE_EXIT);
            System.exit(junit.textui.TestRunner.SUCCESS_EXIT);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(junit.textui.TestRunner.EXCEPTION_EXIT);
        }
    }

    /**
     * Run Marathon in GUI mode with all bells and whistles.
     */
    private static void runGUIMode() {
        showSplash();
        String projectDir = getProjectDirectory(argProcessor.getProjectDirectory());
        if (projectDir == null)
            System.exit(0);
        processMPF(projectDir);
        setDefaultIndent();
        setLogConfiguration(projectDir);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Injector injector = getInjector();
                final DisplayWindow display = injector.getInstance(DisplayWindow.class);
                display.setVisible(true);
            }
        });
    }

    private static void setLogConfiguration(String projectDir) {
        File logconfig = new File(projectDir, "logging.properties");
        if(logconfig.exists())
            try {
                System.setProperty("java.util.logging.config.file", logconfig.getAbsolutePath());
                LogManager.getLogManager().readConfiguration();
            } catch (SecurityException e) {
            } catch (IOException e) {
            }
    }

    public static Injector getInjector() {
        String guiceModuleName = System.getProperty("marathon.guice.module", MarathonGuiceModule.class.getName());
        Module guiceModule;
        try {
            Class<?> klass = Class.forName(guiceModuleName);
            guiceModule = (Module) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            guiceModule = new MarathonGuiceModule();
        }
        Injector injector = Guice.createInjector(guiceModule);
        return injector;
    }

    /**
     * Show the splash screen. The splash can be suppressed by giving
     * <code>-nosplash</code> on command line.
     */
    private static void showSplash() {
        if (argProcessor.showSplash())
            new SplashScreen();
    }

    /**
     * Called when no MPF is given on command line while running Marathon in GUI
     * mode. Pops up a dialog for selecting a MPF.
     * 
     * @param arg
     *            , the MPF given on command line, null if none given
     * @return MPF selected by the user. Can be null.
     */
    private static String getProjectDirectory(final String arg) {
        final String[] ret = new String[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    MPFSelection selection = new MPFSelection();
                    if (arg != null && arg.endsWith(".mpf") && new File(arg).isFile()) {
                        argProcessor.help("A marathon project file is given.\nUse project directory instead");
                    }
                    ret[0] = selection.getProjectDirectory(arg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret[0];
    }

    /**
     * Process the given MPF.
     * 
     * @param mpf
     *            , Marathon project file. a suffix '.mpf' is added if the given
     *            name does not end with it.
     */
    public static void processMPF(String projectDir) {
        FileInputStream input = null;
        try {
            File file = new File(projectDir);
            projectDir = file.getCanonicalPath();
            System.setProperty(Constants.PROP_PROJECT_DIR, projectDir);
            input = new FileInputStream(new File(projectDir, Constants.PROJECT_FILE));
            Properties mpfProps = new Properties();
            mpfProps.load(input);
            MPFUtils.convertPathChar(mpfProps);
            MPFUtils.replaceEnviron(mpfProps);
            Properties props = System.getProperties();
            props.putAll(mpfProps);
            System.setProperties(props);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Unable to open Marathon Project File " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to read Marathon Project File " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String userDir = System.getProperty(Constants.PROP_PROJECT_DIR);
        if (userDir != null && !userDir.equals("") && System.getProperty("user.dir") == null)
            System.setProperty("user.dir", userDir);
        checkForProperties();
        if (!dirExists(Constants.PROP_MODULE_DIRS) || !dirExists(Constants.PROP_TEST_DIR) || !dirExists(Constants.PROP_FIXTURE_DIR)
                || !dirExists(Constants.PROP_CHECKLIST_DIR))
            System.exit(1);
    }

    

    /**
     * The user selected properties are set with 'marathon.properties' prefix in
     * the MPF files. This function removes this prefix (if exist).
     * 
     * @param mpfProps
     *            , properties for which the substitution need to be performed.
     * @return new property list.
     */
    public static Properties removePrefixes(Properties mpfProps) {
        Enumeration<Object> enumeration = mpfProps.keys();
        Properties props = new Properties();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = mpfProps.getProperty(key);
            if (key.startsWith(Constants.PROP_PROPPREFIX)) {
                key = key.substring(Constants.PROP_PROPPREFIX.length());
                props.setProperty(key, value);
            } else if (!props.containsKey(key)) {
                props.setProperty(key, value);
            }
        }
        return props;
    }

    

    

    

    /**
     * Given a directory key like marathon.test.dir check whether given
     * directory exists.
     * 
     * @param dirKey
     *            , a property key
     * @return true, if the directory exists
     */
    private static boolean dirExists(String dirKey) {
        String dirName = System.getProperty(dirKey);
        if (dirKey != null) {
            dirName = dirName.replace(';', File.pathSeparatorChar);
            dirName = dirName.replace('/', File.separatorChar);
            System.setProperty(dirKey, dirName);
        }
        dirName = System.getProperty(dirKey);
        String[] values = dirName.split(String.valueOf(File.pathSeparatorChar));
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && !values[i].trim().isEmpty()) {
                File dir = new File(values[i]);
                if (!dir.exists() || !dir.isDirectory()) {
                    JOptionPane.showMessageDialog(null, "Invalid directory specified for " + dirKey + " - " + dirName, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check whether the mandatory properties are given.
     */
    private static void checkForProperties() {
        List<String> missingProperties = new ArrayList<String>();
        missingProperties.add("The following properties are not given.");
        String[] reqdProperties = { Constants.PROP_FIXTURE_DIR, Constants.PROP_TEST_DIR, Constants.PROP_MODULE_DIRS,
                Constants.PROP_CHECKLIST_DIR };
        for (int i = 0; i < reqdProperties.length; i++) {
            if (System.getProperty(reqdProperties[i]) == null)
                missingProperties.add(reqdProperties[i]);
        }
        if (missingProperties.size() > 1) {
            JOptionPane.showMessageDialog(null, missingProperties.toArray(), "Missing Properties", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
