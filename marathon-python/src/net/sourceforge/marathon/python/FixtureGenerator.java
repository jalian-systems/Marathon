/*******************************************************************************
 *  
 *  $Id: FixtureGenerator.java 175 2008-12-22 10:07:39Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
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
package net.sourceforge.marathon.python;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import net.sourceforge.marathon.util.Indent;

public class FixtureGenerator {
    // @formatter:off
    private static final String comment_start_application =
        "Starts the application. The arguments can be changed by modifying the args array";

    private static final String comment_teardown =
        "Marathon executes this method at the end of test script.";

    private static final String comment_setup =
        "Marathon executes this method before the test script. The application needs to be\n" +
        "started here. You can add other tasks before start_application.";

    private static final String comment_test_setup =
        "Marathon executes this method after the first window of the application is displayed.\n" +
        "You can add any Marathon script elements here.";

    // @formatter:on

    public void printFixture(String mainClass, String args, String description, PrintStream ps) {
        String sClass;
        int index = mainClass.lastIndexOf('.');
        if (index == -1) {
            ps.println("import " + mainClass);
            sClass = mainClass;
        } else {
            sClass = mainClass.substring(index + 1);
            ps.println("from " + mainClass.substring(0, index) + " import " + sClass);
        }
        ps.println("from marathon.playback import *");
        ps.println();

        ps.println("class Fixture:");
        String d = description.trim();
        if (!"".equals(d)) {
            printComments(ps, d, Indent.getDefaultIndent());
        }
        ps.print(Indent.getDefaultIndent());
        ps.println("def start_application(self):");
        printComments(ps, comment_start_application, Indent.getDefaultIndent() + Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("args = [" + getArgs(args) + "]");
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println(sClass + ".main(args)");
        ps.println();
        ps.print(Indent.getDefaultIndent());
        ps.println("def teardown(self):");
        printComments(ps, comment_teardown, Indent.getDefaultIndent() + Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("pass");
        ps.println();
        ps.print(Indent.getDefaultIndent());
        ps.println("def setup(self):");
        printComments(ps, comment_setup, Indent.getDefaultIndent() + Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("self.start_application()");
        ps.println();
        ps.print(Indent.getDefaultIndent());
        ps.println("def test_setup(self):");
        printComments(ps, comment_test_setup, Indent.getDefaultIndent() + Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("pass");
        ps.println();
        ps.println("fixture = Fixture()");
        ps.close();
    }

    /**
     * Writes the comments into the stream with proper comment begin and end
     * symbol.
     * 
     * @param ps
     * @param d
     * @param indent
     */
    private void printComments(PrintStream ps, String d, String indent) {
        ps.print(indent);
        ps.print("'''");
        BufferedReader reader = new BufferedReader(new StringReader(d));
        try {
            String line = reader.readLine();
            while (line != null) {
                ps.print(line);
                if ((line = reader.readLine()) != null) {
                    ps.println();
                    ps.print(indent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps.println("'''");
    }

    private String getArgs(String appArgs) {
        if (appArgs == null || appArgs.trim().equals(""))
            return "";
        String[] arguments = appArgs.split(" (?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
        StringBuffer retValue = new StringBuffer();
        retValue.append(escape(arguments[0]));
        if (arguments.length == 1)
            return retValue.toString();
        for (int i = 1; i < arguments.length; i++) {
            retValue.append(",").append(escape(arguments[i]));
        }
        return retValue.toString();
    }

    private String escape(String string) {
        if (string.startsWith("\""))
            string = string.substring(1);
        if (string.endsWith("\""))
            string = string.substring(0, string.length() - 1);
        string = string.replaceAll("\\\\", "\\\\\\\\");
        string = string.replaceAll("\"", "\\\\\"");
        return "\"" + string + "\"";
    }
}
