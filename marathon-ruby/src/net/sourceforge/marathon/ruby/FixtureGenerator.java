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
package net.sourceforge.marathon.ruby;

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
            sClass = mainClass;
        } else {
            sClass = mainClass.substring(index + 1);
        }
        ps.println("include_class '" + mainClass + "'");
        String d = description.trim();
        if (!"".equals(d)) {
            printComments(ps, d, "");
        }
        ps.println();
        ps.println("class Fixture");
        printComments(ps, comment_start_application, Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("def start_application");
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("args = [" + getArgs(args) + "]");
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println(sClass + ".main(args.to_java:String)");
        ps.print(Indent.getDefaultIndent());
        ps.println("end");
        ps.println();
        printComments(ps, comment_teardown, Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("def teardown");
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println();
        ps.print(Indent.getDefaultIndent());
        ps.println("end");
        ps.println();
        printComments(ps, comment_setup, Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("def setup");
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("start_application");
        ps.print(Indent.getDefaultIndent());
        ps.println("end");
        ps.println();
        printComments(ps, comment_test_setup, Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println("def test_setup");
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.println();
        ps.print(Indent.getDefaultIndent());
        ps.println("end");
        ps.println();
        ps.println("end");
        ps.println();
        ps.println("$fixture = Fixture.new");
        ps.close();
    }

    private void printComments(PrintStream ps, String d, String indent) {
        ps.println("=begin");
        BufferedReader reader = new BufferedReader(new StringReader(d));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                ps.print(indent);
                ps.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps.println("=end");
        ps.println();
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
