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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import net.sourceforge.marathon.Constants;

public class TestJavaRuntime {
    private JavaRuntime runtime;
    private StringConsole console;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        createDir("./testDir");
        System.setProperty(Constants.PROP_PROJECT_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_FIXTURE_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_TEST_DIR, new File("./testDir").getCanonicalPath());
        System.setProperty(Constants.PROP_MODULE_DIRS, new File(".").getCanonicalPath());
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    private static File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_DIR);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_TEST_DIR);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
        deleteRecursive(new File("./testDir"));
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteRecursive(list[i]);
            }
        }
        file.delete();
    }

    @Before
    public void setUp() throws Exception {
        console = new StringConsole();
        System.setProperty(Constants.PROP_PROFILE_MAIN_CLASS, getClass().getName());
        runtime = new JavaRuntime(console, new String[0]);
    }

    @After
    public void tearDown() throws Exception {
        new File("bogus.py").delete();
        new File("bogus$py.class").delete();
    }

    @Test
    public void testHasDefaultComponentResolver() throws Exception {
        assertNotNull("no default component resolver", runtime.getComponentResolver(false));
    }

    @Test
    public void testDoNotInvokeMainWhenNotSpecify() throws Exception {
        System.setProperty(Constants.PROP_PROFILE_MAIN_CLASS, "");
        StringConsole console = new StringConsole();
        runtime = new JavaRuntime(console, new String[0]);
        assertEquals(console.buffer.toString(), "");
    }

    // This is for testing the start main function in JavaRunTime
    public static void main(String[] args) {
    }
}
