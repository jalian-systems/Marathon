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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import net.sourceforge.marathon.objectmap.ObjectMapConfiguration;

public class Constants {
    
    private static URL OMAP_STREAM = ObjectMapConfiguration.class.getResource("default-omap-configuration.yaml");

    public static enum MarathonMode {
        RECORDING,
        OTHER
    }
    
    public static File[] getMarathonDirectories(String propKey) throws IOException {
        String testDir = System.getProperty(propKey);
        String[] values = testDir.split(String.valueOf(File.pathSeparatorChar));
        File[] MarathonDirs = new File[values.length];

        for (int i = 0; i < values.length; i++) {
            MarathonDirs[i] = new File(values[i]).getCanonicalFile();
        }

        return MarathonDirs;
    }

    public static String[] getMarathonDirectoriesAsStringArray(String propKey) {
        String testDir = System.getProperty(propKey);
        if (testDir == null)
            return new String[0];
        String[] values = testDir.split(String.valueOf(File.pathSeparatorChar));

        return values;
    }

    public static String[] getAllMarathonDirectoriesAsStringArray() {
        ArrayList<String> dirs = new ArrayList<String>();
        String[] propKeys = { PROP_TEST_DIR, PROP_FIXTURE_DIR, PROP_MODULE_DIRS, PROP_CHECKLIST_DIR, PROP_DATA_DIR, PROP_SUITE_DIR };
        for (int i = 0; i < propKeys.length; i++) {
            dirs.addAll(Arrays.asList(getMarathonDirectoriesAsStringArray(propKeys[i])));
        }
        return dirs.toArray(new String[dirs.size()]);
    }
    
    public static File getMarathonProjectDirectory() {
        String p = System.getProperty(PROP_PROJECT_DIR);
        if (p == null)
            return null;
        return new File(p);
    }

    static {
        PROJECT_FILE = System.getProperty("marathon.project.file", ".project");
    }
    public static final String PROP_PROJECT_DIR = "marathon.project.dir";
    public static final String PROP_PROJECT_SCRIPT_MODEL = "marathon.project.script.model";
    public static final String PROP_IMAGE_CAPTURE_DIR = "marathon.image.capture.dir";
    public static final String PROP_REPORT_DIR = "marathon.report.dir";
    public static final String PROP_CHECKLIST_DIR = "marathon.checklist.dir";
    public static final String PROP_RUNTIME_DEFAULT_DELAY = "marathon.runtime.default.delay";
    public static final String PROP_RUNTIME_DELAY = "marathon.runtime.delay";
    public static final String PROP_RECORDER_ASSERTIONS = "marathon.runtime.assertions";
    public static final String PROP_HOME = "marathon.home";
    public static final String PROP_PROJECT_NAME = "marathon.project.name";
    public static final String PROP_TEST_DIR = "marathon.test.dir";
    public static final String PROP_SUITE_DIR = "marathon.suite.dir";
    public static final String PROP_FIXTURE_DIR = "marathon.fixture.dir";
    public static final String PROP_MODULE_DIRS = "marathon.capture.dir";
    public static final String PROP_DATA_DIR = "marathon.data.dir";
    public static final String PROP_RECORDER_KEYTRIGGER = "marathon.recorder.keytrigger";
    public static final String PROP_RECORDER_MOUSETRIGGER = "marathon.recorder.mousetrigger";
    public static final String PROP_RECORDER_NAMINGSTRATEGY = "marathon.recorder.namingstrategy";
    public static final String PROP_COMPONENT_RESOLVERS = "marathon.component.resolvers";
    public static final String PROP_IGNORE_COMPONENTS = "marathon.component.ignore.components";
    public static final String PROP_APPLICATION_PATH = "marathon.application.classpath";
    public static final String PROP_APPLICATION_EXTRA_JAR = "marathon.application.extra.jar";
    public static final String ENV_APPLICATION_PATH = "MARATHON_APPLICATION_CLASSPATH";
    public static final String PROP_APPLICATION_ARGUMENTS = "marathon.application.arguments";
    public static final String PROP_APPLICATION_MAINCLASS = "marathon.application.mainclass";
    public static final String PROP_APPLICATION_VM_ARGUMENTS = "marathon.application.vm.arguments";
    public static final String PROP_APPLICATION_VM_COMMAND = "marathon.application.vm.command";
    public static final String PROP_APPLICATION_WORKING_DIR = "marathon.application.working.dir";
    public static final String PROP_APPLICATION_TOOLKIT_MENUMASK = "marathon.application.toolkit_menumask";
    public static final String PROP_APPLICATION_DEVMODE = "marathon.application.devmode";
    public static final String PROP_APPLICATION_START_WINDOW = "marathon.application.start.window";
    public static final String PROP_APPLICATION_START_WINDOW_REGEX = "marathon.application.start.window.regex";
    public static final String PROP_PROJECT_DESCRIPTION = "marathon.project.description";
    public static final String PROP_PROPPREFIX = "marathon.properties.";
    public static final String PROP_WINDOW_TIMEOUT = "marathon.WINDOW_TIMEOUT";
    public static final String PROP_USE_FIELD_NAMES = "marathon.USE_FIELD_NAMES";
    public static final String PROP_COMPONENT_SEARCH_RETRY_COUNT = "marathon.COMPONENT_SEARCH_RETRY_COUNT";
    public static final String PROP_RETRY_INTERVAL_MS = "marathon.RETRY_INTERVAL_MS";

    public static final String PREF_RECORDER_MOUSE_TRIGGER = "recorder.mouse.trigger";
    public static final String PREF_RECORDER_KEYBOARD_TRIGGER = "recorder.keyboard.trigger";
    public static final String PREF_NAVIGATOR_HIDEFILES = "navigator.hidefiles";
    public static final String PREF_JUNIT_HIDEFILES = "junit.hidefiles";

    public static final String DIR_TESTCASES = "TestCases";
    public static final String DIR_FIXTURES = "Fixtures";
    public static final String DIR_MODULE = "Modules";
    public static final String DIR_CHECKLIST = "Checklists";
    public static final String DIR_DATA = "TestData";
    public static final String DIR_TESTSUITES = "TestSuites";
    public static final String PROP_APPLICATION_LAUNCHTIME = "marathon.application.launchtime";
    public static final String PROJECT_FILE;
    public static final String PROP_CUSTOM_CONTEXT_MENUS = "marathon.custom.context.menus";
    public static final String PROP_PROFILE_MAIN_CLASS = "marathon.runtime.profile.mainclass";
    public static final String LAUNCHER_MAIN_CLASS = "net.sourceforge.marathon.runtime.JavaRuntimeLauncher";
    public static final String DEFAULT_NAMING_STRATEGY = "net.sourceforge.marathon.objectmap.ObjectMapNamingStrategy";
    public static final String FILE_OMAP_CONFIGURATION = "omap-configuration.yaml";
    public static final String PROP_OMAP_CONFIGURATION_FILE = "net.sourceforge.marathon.objectmap.configuration.file";
    public static final String FILE_OMAP = "omap.yaml";
    public static final String PROP_OMAP_FILE = "net.sourceforge.marathon.objectmap.file";
    public static final String DIR_OMAP = "omap";
    public static final String PROP_OMAP_DIR = "net.sourceforge.marathon.objectmap.directory";
    public static final String FILE_TESTPROPERTIES = "testproperties.yaml";
    public static final String PROP_TEXT_AREA_OUTPUT_SIZE = "net.sourceforge.marathon.textareaoutput.size";
    public static final String PROP_OMAP_RESOLVE_MODE = "net.sourceforge.marathon.objectmap.resolve.mode";
    public static final String PROP_PROJECT_LAUNCHER_MODEL = "marathon.project.launcher.model";
    public static final String FIXTURE_DESCRIPTION = "marathon.fixture.description";
    public static final String FIXTURE_REUSE = "marathon.fixture.reuse";
    public static String getNSClassName() {
        String s = System.getProperty(PROP_RECORDER_NAMINGSTRATEGY + ".fixture",
                System.getProperty(PROP_RECORDER_NAMINGSTRATEGY, DEFAULT_NAMING_STRATEGY));
        if (s.equals(""))
            s = DEFAULT_NAMING_STRATEGY;
        return s;
    }

    public static InputStream getOMapConfigurationStream() {
        try {
            return OMAP_STREAM.openStream();
        } catch (IOException e) {
            return null ;
        }
    }

    public static void setOMapConfigurationStream(URL url) {
        OMAP_STREAM = url;
    }
}
