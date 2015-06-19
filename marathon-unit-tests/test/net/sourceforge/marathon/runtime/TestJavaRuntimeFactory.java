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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Constants.MarathonMode;
import net.sourceforge.marathon.api.IMarathonRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJavaRuntimeFactory {
    private JavaRuntimeProfile profile;
    private IMarathonRuntime runtime;

    @Before
    public void setUp() throws Exception {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
        System.setProperty(Constants.PROP_PROFILE_MAIN_CLASS, MainClassProbeForTesting.class.getName());
        System.setProperty("marathon.s.club.seven", "would_not_bone");
        System.setProperty("marathon.suga.babes", "would_bone");
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (runtime != null)
                runtime.destroy();
        } finally {
        }
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    /**
     * we run this main method in which we print out all this great stuff to the
     * console, and make sure that it gets set up right on this end
     */
    @Test
    public void testCreatesCorrectlyConfiguredJavaRuntime() throws Exception {
        JavaRuntimeFactory factory = new JavaRuntimeFactory() {
            @Override protected JavaRuntimeProfile createProfile(MarathonMode mode, String script) {
                JavaRuntimeProfile profile = new JavaRuntimeProfile(MarathonMode.PLAYING, "");
                profile.setAppArgs(Arrays.asList("dude whereis mycar".split(" ")));
                return profile;
            }
        };
        StringConsole console = new StringConsole();
        runtime = factory.createRuntime(MarathonMode.PLAYING, "", console);

        StringReader stringReader = new StringReader(console.stdOutBuffer.toString());
        BufferedReader reader = new BufferedReader(stringReader);
        String s = reader.readLine();
        profile = factory.getProfile(); 
        String classpath = profile.getClasspath();
        /*
         * The reason for assertContains instead of assertEquals is that Mac OSX
         * appends a compatibility Jar to the Java classpath.
         */
        assertContains(s, classpath.substring(1, classpath.length() - 1));
        assertEquals("marathon.suga.babes", "would_bone", reader.readLine());
        assertEquals("marathon.s.club.seven", "would_not_bone", reader.readLine());
        assertEquals("dude", reader.readLine());
        assertEquals("whereis", reader.readLine());
        assertEquals("mycar", reader.readLine());
        // now test the standard err is attached
        reader = new BufferedReader(new StringReader(console.stdErrBuffer.toString()));
        String out = "";
        while ((s = reader.readLine()) != null) {
            out = s;
        }
        assertTrue(out.matches(".*this is std err.*"));
        runtime.destroy();
        runtime = null;
    }

    private void assertContains(String expected, String actual) {
        if (expected.toString().indexOf(actual) == -1) {
            assertEquals("Could not find path", expected, actual);
        }
    }

}
