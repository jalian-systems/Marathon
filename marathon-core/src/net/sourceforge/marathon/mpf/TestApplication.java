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
package net.sourceforge.marathon.mpf;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Main;
import net.sourceforge.marathon.util.StreamPumper;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TestApplication extends JDialog {
    private static final long serialVersionUID = 1L;

    private final static class TextAreaWriter extends Writer {
        private JTextArea textArea;

        public TextAreaWriter(JTextArea area) {
            textArea = area;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            String newText = new String(cbuf, off, len);
            textArea.setText(textArea.getText() + newText);
        }

        public void close() throws IOException {
        }

        public void flush() throws IOException {
        }
    }

    private String launchCommand;
    private JTextArea commandField = new JTextArea(3, 30);
    private JTextArea outputArea = new JTextArea(4, 50);
    private JTextArea errorArea = new JTextArea(4, 50);
    private JButton closeButton = new JButton("Close");
    private Process process = null;
    private String workingDir = null;

    public TestApplication(JDialog parent, Properties props) {
        super(parent);
        setLocationRelativeTo(parent);
        Main.convertPathChar(props);
        Main.replaceEnviron(props);
        createLaunchCommand(props);
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

    private void createLaunchCommand(Properties props) {
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
        workingDir = props.getProperty(Constants.PROP_APPLICATION_WORKING_DIR, ".");
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
        launchCommand = command.toString();
    }

    public void launch() throws IOException, InterruptedException {
        pack();
        commandField.setText(launchCommand);
        commandField.setCaretPosition(0);
        String[] cmdElements = getCommandArray(launchCommand);
        File cwd;
        if (workingDir != null && !workingDir.equals(""))
            cwd = new File(workingDir);
        else
            cwd = null;
        process = Runtime.getRuntime().exec(cmdElements, null, cwd);
        new StreamPumper(process.getInputStream(), new TextAreaWriter(outputArea)).start();
        new StreamPumper(process.getErrorStream(), new TextAreaWriter(errorArea)).start();
        setVisible(true);
    }

    private String[] getCommandArray(String command) {
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
}
