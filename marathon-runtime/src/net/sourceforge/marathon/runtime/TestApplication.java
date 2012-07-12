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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IRuntimeLauncherModel;
import net.sourceforge.marathon.api.ITestApplication;
import net.sourceforge.marathon.util.ArgumentProcessor;
import net.sourceforge.marathon.util.LauncherModelHelper;
import net.sourceforge.marathon.util.MPFUtils;
import net.sourceforge.marathon.util.StreamPumper;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TestApplication extends JDialog implements ITestApplication {
    private static final long serialVersionUID = 1L;

    private final static class TextAreaWriter extends Writer {
        private JTextArea textArea;

        public TextAreaWriter(JTextArea area) {
            textArea = area;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            final String newText = new String(cbuf, off, len);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textArea.setText(textArea.getText() + newText);
                }
            });
        }

        public void close() throws IOException {
        }

        public void flush() throws IOException {
        }
    }

    private String[] launchCommand;
    private JTextArea commandField = new JTextArea(3, 30);
    private JTextArea outputArea = new JTextArea(4, 50);
    private JTextArea errorArea = new JTextArea(4, 50);
    private JButton closeButton = UIUtils.createCloseButton();
    private Process process = null;
    private String workingDir = null;

    public TestApplication(JDialog parent, Properties props) {
        super(parent);
        setLocationRelativeTo(parent);
        MPFUtils.convertPathChar(props);
        MPFUtils.replaceEnviron(props);
        workingDir = props.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, ".");
        String model = props.getProperty(Constants.PROP_PROJECT_LAUNCHER_MODEL);
        if (model == null || model.equals(""))
            launchCommand = createLaunchCommand(props);
        else {
            IRuntimeLauncherModel launcherModel = LauncherModelHelper.getLauncherModel(model);
            launchCommand = launcherModel.createLaunchCommand(props);
        }
        setModal(true);
        PanelBuilder builder = new PanelBuilder(new FormLayout("pref:grow, 3dlu, pref",
                "pref, 3dlu, fill:p:grow, 3dlu, pref, 3dlu, fill:p:grow, 3dlu, pref, 3dlu, fill:p:grow, 3dlu, pref"));
        CellConstraints cellconstraints = new CellConstraints();
        builder.addSeparator("Command", cellconstraints.xyw(1, 1, 3));
        commandField.setEditable(false);
        commandField.setLineWrap(true);
        commandField.setWrapStyleWord(true);
        builder.add(new JScrollPane(commandField), cellconstraints.xyw(1, 3, 3));
        builder.addSeparator("Standard Output", cellconstraints.xyw(1, 5, 3));
        outputArea.setEditable(false);
        builder.add(new JScrollPane(outputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), cellconstraints.xyw(1, 7, 3));
        builder.addSeparator("Standard Error", cellconstraints.xyw(1, 9, 3));
        errorArea.setEditable(false);
        builder.add(new JScrollPane(errorArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), cellconstraints.xyw(1, 11, 3));
        errorArea.setForeground(new Color(0xFF0000));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (process != null)
                    process.destroy();
                dispose();
            }
        });
        builder.add(closeButton, cellconstraints.xy(3, 13));
        builder.setDefaultDialogBorder();
        getContentPane().add(builder.getPanel());
    }

    protected String[] createLaunchCommand(Properties props) {
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
            command.add("\"" + classPath + "\" ");
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

    public void launch() throws IOException, InterruptedException {
        pack();
        commandField.setText(create_command(launchCommand));
        commandField.setCaretPosition(0);
        String[] cmdElements = launchCommand;
        File cwd;
        if (workingDir != null && !workingDir.equals(""))
            cwd = new File(workingDir);
        else
            cwd = null;
        ProcessBuilder pb = new ProcessBuilder(cmdElements);
        if (cwd != null)
            pb = pb.directory(cwd);
        process = pb.start();
        new StreamPumper(process.getInputStream(), new TextAreaWriter(outputArea)).start();
        new StreamPumper(process.getErrorStream(), new TextAreaWriter(errorArea)).start();
        setVisible(true);
    }

    private String create_command(String[] cmdElements) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cmdElements.length; i++) {
            sb.append(cmdElements[i]);
            if (i != cmdElements.length - 1)
                sb.append(' ');
        }
        return sb.toString();
    }
}
