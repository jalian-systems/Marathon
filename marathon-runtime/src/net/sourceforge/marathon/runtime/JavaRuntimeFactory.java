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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Constants.MarathonMode;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.MarathonException;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.util.Path;
import net.sourceforge.rmilite.Client;

/**
 * This start the client server run time. In this case, it's implemented in
 * java. JavaRuntimeLauncher is the server JavaRuntmieLeash is the client
 */
public class JavaRuntimeFactory implements IRuntimeFactory {
    private Process process;
    private JavaRuntimeProfile profile;
    private static Logger logger = Logger.getLogger(JavaRuntimeFactory.class.getName());;

    public synchronized IMarathonRuntime createRuntime(MarathonMode mode, String script, IConsole console) {
        ILogger logViewLogger = RuntimeLogger.getRuntimeLogger();
        profile = createProfile(mode, script);
        Client client = new Client("localhost", profile.getPort());
        client.exportInterface(IConsole.class);
        client.exportInterface(IRecorder.class);
        client.exportInterface(IPlaybackListener.class);
        client.exportInterface(ILogger.class);
        try {
            this.process = launchVM(profile, logViewLogger);
        } catch (Throwable t) {
            if (process != null)
                process.destroy();
            t.printStackTrace();
            throw new MarathonException("error creating Java Runtime: " + t.getMessage(), t);
        }
        return new JavaRuntimeLeash(client, process, console, logViewLogger);
    }

    protected JavaRuntimeProfile createProfile(MarathonMode mode, String script) {
        return new JavaRuntimeProfile(mode, script);
    }

    public JavaRuntimeProfile getProfile() {
        return profile;
    }

    protected Process launchVM(JavaRuntimeProfile jprofile, ILogger logViewLogger) throws IOException {
        String[] cmdElements = createCommand(jprofile);
        logger.info("Command: " + Arrays.asList(cmdElements));
        Path extendedClasspath = getExtendedClassPath(jprofile);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdElements);
        Map<String, String> environ = processBuilder.environment();
        logger.info("Classpath: " + extendedClasspath);
        environ.put("CLASSPATH", extendedClasspath.toString());
        StringBuilder msg = new StringBuilder();
        msg.append("Command:\n").append(processBuilder.command().toString()).append("\n\n");
        msg.append("CLASSPATH set to:    \n").append(extendedClasspath.toString()).append("\n");
        logViewLogger.info("Launcher", "Launching Application", msg.toString());
        return processBuilder.directory(jprofile.getWorkingDirectory()).start();
    }

    protected Path getExtendedClassPath(JavaRuntimeProfile jprofile) {
        Path extendedClasspath = new Path(jprofile.getClasspath());
        return extendedClasspath;
    }

    protected String[] createCommand(JavaRuntimeProfile profile) {
        List<String> l = new ArrayList<String>();
        l.add(profile.getVMCommand());
        l.addAll(profile.getVMArgs());
        l.add(Constants.LAUNCHER_MAIN_CLASS);
        l.add(profile.getPort() + "");
        if (profile.getMainClass() != null) {
            l.add(profile.getMainClass());
        }
        l.addAll(profile.getAppArgs());
        return l.toArray(new String[l.size()]);
    }
}
