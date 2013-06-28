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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.MPFUtils;

public class FixtureGenerator {
    // @formatter:off
    private static final String comment_fixture_properties =
        "Launcher uses the properties specified here to launch the application";

    private static final String comment_teardown =
        "Marathon executes this method at the end of test script.";

    private static final String comment_setup =
        "Marathon executes this method before the test script.";

    private static final String comment_test_setup =
        "Marathon executes this method after the first window of the application is displayed.\n" +
        "You can add any Marathon script elements here.";

    // @formatter:on

    public void printFixture(Properties props, PrintStream ps, String launcher, List<String> keys) {
        printComments(ps, comment_fixture_properties, "");
        ps.println("#{{{ Fixture Properties");
        ps.println("fixture_properties = {");

        printKeyValue(Constants.PROP_PROJECT_LAUNCHER_MODEL, launcher, ps, false);
        keys = new ArrayList<String>(keys);
        List<String> nsKeys = MPFUtils.getNSKeys(props.getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY));
        if(nsKeys != null) {
            keys.addAll(nsKeys);
        }
        int size = keys.size();
        for (int i = 0; i < size; i++)
            printProperty(props, keys.get(i), ps, false);

        printProperty(props, Constants.FIXTURE_REUSE, ps, true);
        ps.print(Indent.getDefaultIndent());
        ps.println("}");
        ps.println("#}}} Fixture Properties");

        ps.println();
        String d = props.getProperty(Constants.FIXTURE_DESCRIPTION);
        if (!"".equals(d)) {
            printComments(ps, d, "");
        }
        ps.println("class Fixture");

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

    private void printProperty(Properties props, String key, PrintStream ps, boolean last) {
        printKeyValue(key, props.getProperty(key), ps, last);
    }

    private void printKeyValue(String key, String value, PrintStream ps, boolean last) {
        ps.print(Indent.getDefaultIndent());
        ps.print(Indent.getDefaultIndent());
        ps.print("'" + key + "' => ");
        ps.print(RubyScriptModel.encode(value));
        if (last)
            ps.println();
        else
            ps.println(",");
    }

    private void printComments(PrintStream ps, String d, String indent) {
        ps.println("=begin");
        BufferedReader reader = new BufferedReader(new StringReader(d));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if ("=end".equals(line))
                    line = "\\=end";
                ps.print(indent);
                ps.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps.println("=end");
        ps.println();
    }

}
