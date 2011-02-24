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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.applet.AppletViewer;

public class AppletTester {

    private class Parameter {
        String name;
        String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;

        }

        public String toString() {
            return name + ":" + value;
        }
    }

    private class Parameters extends ArrayList<Parameter> {
        private static final long serialVersionUID = 1L;

        public void setParameter(String name, String value) {
            add(new Parameter(name, value));
        }

    }

    /**
     * Invoke as: net.sourceforge.marathon.Applet main-class [-width 640]
     * [-height 480] [<param>=<name>]*
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        AppletTester applet = new AppletTester();
        applet.processArguments(args);
        applet.invoke();
    }

    private Parameters params = new Parameters();
    private String mainClass;
    private int width = 640;
    private int height = 480;
    private Pattern PARAM_PATTERN = Pattern.compile("([^=][^=]*)=(.*)");

    @SuppressWarnings("deprecation")
    /** FIXME: Check how we can avoid the deprecation warning **/
    private void invoke() throws IOException {
        File tempFile = new File(new File(System.getProperty(Constants.PROP_PROJECT_DIR)), "appletviewer.html");
        // tempFile.deleteOnExit();

        PrintWriter writer = new PrintWriter(tempFile);
        writer.println("<applet code=" + mainClass + " width=" + width + " height=" + height + ">");
        for (int i = 0; i < params.size(); i++) {
            Parameter param = (Parameter) params.get(i);
            writer.println("<param name=" + param.name + " value=" + param.value + ">");
        }
        writer.println("</applet>");
        writer.close();

        AppletViewer.main(new String[] { "-Xnosecurity", tempFile.getAbsolutePath() });
    }

    private void processArguments(String[] args) {
        if (args.length == 0)
            help();
        mainClass = args[0];
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-width")) {
                width = readint(args, ++i);
            } else if (args[i].equals("-height")) {
                height = readint(args, ++i);
            } else {
                Matcher matcher = PARAM_PATTERN.matcher(args[i].trim());
                if (!matcher.matches())
                    help();
                params.setParameter(matcher.group(1), matcher.group(2));
            }
        }
    }

    private int readint(String[] args, int i) {
        if (i == args.length)
            help();
        try {
            return Integer.parseInt(args[i]);
        } catch (NumberFormatException e) {
            help();
        }
        return -1;
    }

    private void help() {
        System.err.println("net.sourceforge.marathon.Applet main-class [-width width] [-height height] [<param>=<name>]*");
        System.exit(0);
    }
}
