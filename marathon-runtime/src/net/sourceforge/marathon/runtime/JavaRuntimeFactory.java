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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.IRuntimeProfile;
import net.sourceforge.marathon.api.MarathonException;
import net.sourceforge.marathon.util.Path;
import net.sourceforge.rmilite.Client;

/**
 * This start the client server run time. In this case, it's implemented in
 * java. JavaRuntimeLauncher is the server JavaRuntmieLeash is the client
 */
public class JavaRuntimeFactory implements IRuntimeFactory {
    private Process process;

    public synchronized IMarathonRuntime createRuntime(IRuntimeProfile profile, IConsole console) {
        JavaRuntimeProfile jprofile = (JavaRuntimeProfile) profile;
        Client client = new Client("localhost", jprofile.getPort());
        client.exportInterface(IConsole.class);
        client.exportInterface(IRecorder.class);
        client.exportInterface(IPlaybackListener.class);
        try {
            this.process = launchVM(jprofile);
        } catch (Throwable t) {
            if (process != null)
                process.destroy();
            t.printStackTrace();
            throw new MarathonException("error creating Java Runtime: " + t.getMessage(), t);
        }
        return new JavaRuntimeLeash(client, process, console);
    }

    protected Process launchVM(JavaRuntimeProfile jprofile) throws IOException {
        String command = createCommand(jprofile);
        String[] cmdElements = getCommandArray(command);
        String dirName = System.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, ".");
        if (dirName.equals(""))
            dirName = ".";
        File workingDir = new File(dirName);
        Path extendedClasspath = new Path(jprofile.getClasspath());
        Process process = Runtime.getRuntime().exec(cmdElements, getExtendedEnviron(extendedClasspath), workingDir);
        return process;
    }

    private String[] getExtendedEnviron(Path extendedClasspath) {
        Map<String, String> env = new HashMap<String, String>(System.getenv());
        env.put("CLASSPATH", extendedClasspath.toString());
        Set<String> keySet = env.keySet();
        String[] r = new String[keySet.size()];
        int i = 0;
        for (String string : keySet) {
            r[i++] = string + "=" + env.get(string);
        }
        return r;
    }

    private String[] getCommandArray(String command) {
        command = command.replaceAll("  ", " ");
        String[] arguments = command.split(" (?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = escape(arguments[i]);
        }
        return arguments;
    }

    private String escape(String string) {
        if (string.startsWith("\""))
            string = string.substring(1);
        if (string.endsWith("\""))
            string = string.substring(0, string.length() - 1);
        return string;
    }

    String createCommand(JavaRuntimeProfile profile) {
        MessageFormat launch_command = new MessageFormat("{3} {0} " + Constants.LAUNCHER_MAIN_CLASS + " {1,number,#} {2}");
        return launch_command.format(new Object[] { profile.getVMArgs(), Integer.valueOf(profile.getPort()), profile.getAppArgs(),
                profile.getVMCommand() });
    }
}
