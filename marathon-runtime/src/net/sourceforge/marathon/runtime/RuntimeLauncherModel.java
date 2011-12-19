package net.sourceforge.marathon.runtime;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JDialog;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.IRuntimeLauncherModel;
import net.sourceforge.marathon.mpf.ClassPathPanel;
import net.sourceforge.marathon.mpf.IPropertiesPanel;
import net.sourceforge.marathon.mpf.MainPanel;

public class RuntimeLauncherModel implements IRuntimeLauncherModel {
    public IPropertiesPanel[] getSubPanels(JDialog parent) {
        return new IPropertiesPanel[] { new MainPanel(parent), new ClassPathPanel(parent) };
    }

    public List<String> getPropertyKeys() {
        return Arrays.asList(Constants.PROP_APPLICATION_MAINCLASS, Constants.PROP_APPLICATION_ARGUMENTS,
                Constants.PROP_APPLICATION_VM_ARGUMENTS, Constants.PROP_APPLICATION_VM_COMMAND,
                Constants.PROP_APPLICATION_WORKING_DIR, Constants.PROP_APPLICATION_TOOLKIT_MENUMASK,
                Constants.PROP_APPLICATION_PATH);
    }

    public IRuntimeFactory getRuntimeFactory() {
        return new JavaRuntimeFactory();
    }

    public String createLaunchCommand(Properties props) {
        StringBuffer command = new StringBuffer();
        String vmCommand = props.getProperty(Constants.PROP_APPLICATION_VM_COMMAND, "java");
        if (vmCommand.equals(""))
            vmCommand = "java";
        command.append("\"" + vmCommand + "\" ");
        String vmArgs = props.getProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS, "");
        if (!vmArgs.equals(""))
            command.append(vmArgs).append(" ");
        String classPath = props.getProperty(Constants.PROP_APPLICATION_PATH, "");
        if (classPath.equals(""))
            classPath = System.getProperty("java.class.path", "");
        else
            classPath += File.pathSeparator + System.getProperty("java.class.path", "");
        if (!classPath.equals(""))
            command.append("-classpath ").append("\"").append(classPath).append("\" ");
        String workingDir = props.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, ".");
        if (!workingDir.equals(""))
            command.append("\"-Duser.dir=").append(workingDir).append("\" ");
        Set<Object> keys = props.keySet();
        for (Iterator<Object> iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if (key.startsWith(Constants.PROP_PROPPREFIX)) {
                String value = escape(props.getProperty(key));
                command.append("\"-D").append(key.substring(Constants.PROP_PROPPREFIX.length())).append("=").append(value)
                        .append("\" ");
            }
        }
        String mainClass = props.getProperty(Constants.PROP_APPLICATION_MAINCLASS);
        if (mainClass == null || mainClass.equals(""))
            throw new RuntimeException("Main Class Not Given");
        command.append(mainClass);
        String args = props.getProperty(Constants.PROP_APPLICATION_ARGUMENTS, "");
        if (!args.equals(""))
            command.append(" ").append(args);
        return command.toString();
    }

    private String escape(String string) {
        if (string.startsWith("\""))
            string = string.substring(1);
        if (string.endsWith("\""))
            string = string.substring(0, string.length() - 1);
        return string;
    }
}
