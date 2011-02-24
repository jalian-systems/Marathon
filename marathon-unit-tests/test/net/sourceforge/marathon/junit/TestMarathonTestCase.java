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
package net.sourceforge.marathon.junit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.runtime.RuntimeStub;

public class TestMarathonTestCase {
    @BeforeClass
    public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    @AfterClass
    public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Before
    public void setUp() throws Exception {
        createSuccessFile();
        createFailureFile();
    }

    @After
    public void tearDown() {
        new File("success.py").delete();
        new File("failure.py").delete();
    }

    private File createSuccessFile() throws IOException {
        File file = createFile("success.py");
        FileWriter writer = new FileWriter(file);
        writer.write("def test():\n\tpass\n");
        writer.close();
        return file;
    }

    private File createFailureFile() throws IOException {
        File file = createFile("failure.py");
        FileWriter writer = new FileWriter(file);
        writer.write("def test():\n\traise java.lang.RuntimeException()\n");
        writer.close();
        return file;
    }

    private File createFile(String name) throws IOException {
        File file = new File(name);
        file.createNewFile();
        return file;
    }

    @Test
    public void testMarathonTestCaseName() throws Exception {
        IMarathonRuntime runtime = new RuntimeStub();
        MarathonTestCase t = new MarathonTestCase(new File("/path/to/scriptboy.py"), runtime);
        assertEquals("scriptboy", t.getName());
    }

    /**
     * this testcase uses the file success.py contained in the same directory as
     * this testcase
     */
    @Test
    public void testRunSuccessfulTestCase() throws Throwable {
        RuntimeStub runtime = new RuntimeStub();
        MarathonTestCase t = new MarathonTestCase(new File("./success.py"), runtime);
        TestResult result = t.run();
        if (result.errorCount() > 0) {
            TestFailure failure = (TestFailure) result.errors().nextElement();
            throw failure.thrownException();
        }
        assertEquals("failed", true, result.wasSuccessful());
    }

    /**
     * this testcase uses the file failure.py contained in the same directory as
     * this testcase
     */
    @Test
    public void testRunFailingTestCase() throws Exception {
        RuntimeStub runtime = new RuntimeStub();
        runtime.scriptsFail = true;
        MarathonTestCase t = new MarathonTestCase(new File("./failure.py"), runtime);
        TestResult result = t.run();
        assertEquals("test case should have failed", false, result.wasSuccessful());
        assertEquals("each failing testcase should have one failure, and no errors", 1, result.failureCount());
        assertEquals("each failing testcase should have one failure, and no errors", 0, result.errorCount());
    }
}
