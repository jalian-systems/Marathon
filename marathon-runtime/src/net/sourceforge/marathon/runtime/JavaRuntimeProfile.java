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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Constants.MarathonMode;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IRuntimeProfile;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.util.ClassPathHelper;
import net.sourceforge.rmilite.Server;

import org.yaml.snakeyaml.Yaml;

import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.Provider;

public class JavaRuntimeProfile implements IRuntimeProfile {
    private static final long serialVersionUID = 1L;
    private String vmParams = System.getProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS, "");
    private String vmCommand = System.getProperty(Constants.PROP_APPLICATION_VM_COMMAND, "");
    private final static String DEFAULT_JAVA_COMMAND = "java";
    private String appArgs = "";
    private int port = 0;
    private final MarathonMode mode;

    public JavaRuntimeProfile(MarathonMode mode) {
        this.mode = mode;
    }

    public String getClasspath() {
        StringBuffer path = new StringBuffer();
        String app = System.getProperty(Constants.PROP_APPLICATION_PATH, "");
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
        String scriptPath = ScriptModelClientPart.getModel().getClasspath();
        if (scriptPath != null)
            paths.add(scriptPath);
        StringBuffer classPath = new StringBuffer();
        for (String string : paths) {
            classPath.append(string).append(File.pathSeparator);
        }
        return classPath.toString();
    }

    public String getVMArgs() {
        StringBuffer vmArgs = new StringBuffer();
        Properties props = System.getProperties();
        String workingDir = props.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, "");
        if (!workingDir.equals(""))
            vmArgs.append("\"-Duser.dir=").append(escape(workingDir)).append("\" ");
        if (mode == MarathonMode.RECORDING)
            vmArgs.append("\"-Dmarathon.mode=recording").append("\" ");
        else
            vmArgs.append("\"-Dmarathon.mode=other").append("\" ");
        vmArgs.append(vmParams);
        return vmArgs.toString();
    }

    public String getVMCommand() {
        if (vmCommand.equals(""))
            return DEFAULT_JAVA_COMMAND;
        else
            return "\"" + vmCommand + "\"";
    }

    private String escape(String property) {
        return property.replaceAll("\"", "\\\\\"");
    }

    public void setAppArgs(String appArgs) {
        this.appArgs = appArgs;
    }

    public String getAppArgs() {
        return appArgs;
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
            return "recording" ;
        return "other";
    }
}
