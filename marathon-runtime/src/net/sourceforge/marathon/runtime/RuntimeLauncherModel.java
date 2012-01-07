package net.sourceforge.marathon.runtime;

import java.util.ArrayList;
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
import net.sourceforge.marathon.util.ArgumentProcessor;

public class RuntimeLauncherModel implements IRuntimeLauncherModel {
    public IPropertiesPanel[] getSubPanels(JDialog parent) {
        return new IPropertiesPanel[] { new MainPanel(parent), new ClassPathPanel(parent) };
    }

    public List<String> getPropertyKeys() {
        return Arrays.asList(Constants.PROP_APPLICATION_MAINCLASS, Constants.PROP_APPLICATION_ARGUMENTS,
                Constants.PROP_APPLICATION_VM_ARGUMENTS, Constants.PROP_APPLICATION_VM_COMMAND,
                Constants.PROP_APPLICATION_WORKING_DIR, Constants.PROP_APPLICATION_PATH);
    }

    public IRuntimeFactory getRuntimeFactory() {
        return new JavaRuntimeFactory();
    }

    public String[] createLaunchCommand(Properties props) {
        List<String> command = new ArrayList<String>();
        String vmCommand = props.getProperty(Constants.PROP_APPLICATION_VM_COMMAND, "java");
        if (vmCommand.equals(""))
            vmCommand = "java";
        command.add(vmCommand);
        String vmArgs = props.getProperty(Constants.PROP_APPLICATION_VM_ARGUMENTS, "");
        if (!vmArgs.equals("")) {
            ArgumentProcessor p = new ArgumentProcessor(vmArgs);
            command.addAll(p.parseArguments());
        }
        String classPath = props.getProperty(Constants.PROP_APPLICATION_PATH, "");
        if (!classPath.equals("")) {
            command.add("-classpath");
            command.add(classPath);
        }
        Set<Object> keys = props.keySet();
        for (Iterator<Object> iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if (key.startsWith(Constants.PROP_PROPPREFIX)) {
                String value = props.getProperty(key);
                command.add("-D" + key.substring(Constants.PROP_PROPPREFIX.length()) + "=" + value);
            }
        }
        String mainClass = props.getProperty(Constants.PROP_APPLICATION_MAINCLASS);
        if (mainClass == null || mainClass.equals(""))
            throw new RuntimeException("Main Class Not Given");
        command.add(mainClass);
        String args = props.getProperty(Constants.PROP_APPLICATION_ARGUMENTS, "");
        if (!args.equals("")) {
            ArgumentProcessor p = new ArgumentProcessor(args);
            command.addAll(p.parseArguments());
        }
        return command.toArray(new String[command.size()]);
    }

}
