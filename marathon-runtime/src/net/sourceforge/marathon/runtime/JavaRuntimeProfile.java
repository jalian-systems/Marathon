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
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Constants.MarathonMode;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IRuntimeProfile;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.util.ArgumentProcessor;
import net.sourceforge.marathon.util.ClassPathHelper;
import net.sourceforge.marathon.util.MPFUtils;
import net.sourceforge.rmilite.Server;

import org.yaml.snakeyaml.Yaml;

import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.Provider;

public class JavaRuntimeProfile implements IRuntimeProfile {
    private static final long serialVersionUID = 1L;
    private final static String DEFAULT_JAVA_COMMAND = "java";
    private List<String> appArgs;
    private int port = 0;
    private final MarathonMode mode;
    private Map<String, Object> fixtureProperties;

    public JavaRuntimeProfile(MarathonMode mode, String scriptText) {
        this.mode = mode;
        IScriptModelClientPart model = ScriptModelClientPart.getModel();
        fixtureProperties = model.getFixtureProperties(scriptText);
        replaceEnviron(fixtureProperties);
    }

    public String getClasspath() {
        StringBuffer path = new StringBuffer();
        String app;
        if (fixtureProperties.size() == 0)
            app = System.getProperty(Constants.PROP_APPLICATION_PATH, "");
        else {
            app = getFixtureProperty(Constants.PROP_APPLICATION_PATH);
            if (app != null && app.trim().equals(""))
                app = MPFUtils.convertPathChar(app);
        }
        String classpath = getMarathonClasspath();
        String envAppPath = System.getenv(Constants.ENV_APPLICATION_PATH);
        if (envAppPath != null)
            path.append(envAppPath).append(File.pathSeparator);
        path.append(app).append(File.pathSeparator).append(classpath);
        return path.toString();
    }

    public String getMarathonClasspath() {
        Set<String> paths = new HashSet<String>();
        paths.add(ClassPathHelper.getClassPath(JavaRuntimeProfile.class));
        paths.add(ClassPathHelper.getClassPath(Server.class));
        paths.add(ClassPathHelper.getClassPath(IPlayer.class));
        paths.add(ClassPathHelper.getClassPath(Provider.class));
        paths.add(ClassPathHelper.getClassPath(Constants.LAUNCHER_MAIN_CLASS));
        paths.add(ClassPathHelper.getClassPath(CSVReader.class));
        paths.add(ClassPathHelper.getClassPath(Yaml.class));
        paths.add(ClassPathHelper.getClassPath(Inject.class));
        paths.add(ClassPathHelper.getClassPath(Constants.getNSClassName()));
        String contextMenus = System.getProperty(Constants.PROP_CUSTOM_CONTEXT_MENUS);
        if (contextMenus != null) {
            String[] menus = contextMenus.split(";");
            for (String menuClass : menus) {
                try {
                    Class<?> klass = Class.forName(menuClass);
                    paths.add(ClassPathHelper.getClassPath(klass));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        String scriptPath = ScriptModelClientPart.getModel().getClasspath();
        if (scriptPath != null)
            paths.add(scriptPath);
        StringBuffer classPath = new StringBuffer();
        for (String string : paths) {
            classPath.append(string).append(File.pathSeparator);
        }
        return classPath.toString();
    }

    public List<String> getVMArgs() {
        List<String> vmArgs = new ArrayList<String>();

        vmArgs.add("-Dmarathon.mode=" + (mode == MarathonMode.RECORDING ? "recording" : "other"));
        String vmParams;
        if (fixtureProperties.size() == 0)
            vmParams = System.getProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS, "");
        else
            vmParams = getFixtureProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS);
        if (vmParams != null)
            vmArgs.addAll(tokenize(vmParams));
        return vmArgs;
    }

    public String getVMCommand() {
        String vmCommand;
        if (fixtureProperties.size() == 0)
            vmCommand = System.getProperty(Constants.PROP_APPLICATION_VM_COMMAND, "");
        else
            vmCommand = getFixtureProperty(Constants.PROP_APPLICATION_VM_COMMAND);
        if (vmCommand == null || vmCommand.equals(""))
            return DEFAULT_JAVA_COMMAND;
        else
            return vmCommand;
    }

    public void setAppArgs(List<String> appArgs) {
        this.appArgs = appArgs;
    }

    public List<String> getAppArgs() {
        if (appArgs != null) {
            // For UT
            return appArgs;
        }
        if (fixtureProperties.size() == 0)
            return new ArrayList<String>();
        String args = getFixtureProperty(Constants.PROP_APPLICATION_ARGUMENTS);
        return tokenize(args);
    }

    private static List<String> tokenize(String args) {
        ArgumentProcessor p = new ArgumentProcessor(args);
        return p.parseArguments();
    }

    public int getPort() {
        if (port == 0) {
            try {
                ServerSocket socket = new ServerSocket(0);
                port = socket.getLocalPort();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return port;
    }

    public String getMode() {
        if (mode == MarathonMode.RECORDING)
            return "recording";
        return "other";
    }

    @SuppressWarnings("unchecked") public <T> T getFixtureProperty(String name) {
        return (T) fixtureProperties.get(name);
    }

    public File getWorkingDirectory() {
        String cwd = null;
        if (fixtureProperties.size() == 0)
            cwd = System.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, ".");
        else
            cwd = getFixtureProperty(Constants.PROP_APPLICATION_WORKING_DIR);
        if (cwd == null || cwd.equals(""))
            cwd = ".";
        File cwdFile = new File(cwd);
        if (cwdFile.exists() && cwdFile.isDirectory())
            return cwdFile;
        try {
            cwdFile = new File(".").getCanonicalFile();
        } catch (IOException e) {
            cwdFile = new File(".");
        }
        RuntimeLogger.getRuntimeLogger().warning("Runtime",
                "Given working directory '" + cwd + "' is not valid. Defaulting to " + cwdFile.getAbsolutePath());
        return cwdFile;
    }

    public String getMainClass() {
        if (fixtureProperties.size() == 0)
            return null;
        return getFixtureProperty(Constants.PROP_APPLICATION_MAINCLASS);
    }

    private void replaceEnviron(Map<String, Object> props) {
        Iterator<Entry<String, Object>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof String) {
                props.put(entry.getKey(), MPFUtils.getUpdatedValue((String) entry.getValue()));
            }
        }
    }

    public Properties getFixtureProperties(List<String> list) {
        Properties properties = new Properties();
        for (String key : list) {
            Object v = getFixtureProperty(key);
            if (v != null)
                properties.put(key, v);
        }
        return properties;
    }

}
