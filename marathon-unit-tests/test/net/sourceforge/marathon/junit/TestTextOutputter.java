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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.junit.textui.MarathonTestResult;
import net.sourceforge.marathon.junit.textui.StdOutLogger;
import net.sourceforge.marathon.junit.textui.TextOutputter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class TestTextOutputter {
    private RuntimeException exception;

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
        System.setProperty(Constants.PROP_TEST_DIR, "./testDir");
        exception = new RuntimeException("Runtime Exception");
        createDir("./testDir");
        createDir("./testDir/subdir");
        createTestFile("./testDir/testcase1.py");
        createTestFile("./testDir/testcase2.py");
        createTestFile("./testDir/subdir/subtest1.py");
        createTestFile("./testDir/subdir/subtest2.py");
    }

    private File createTestFile(String name) throws IOException {
        File file = createFile(name);
        FileWriter writer = new FileWriter(file);
        writer.write("def test():\n\tpass\n");
        writer.close();
        return file;
    }

    private File createFile(String name) throws IOException {
        File file = new File(name);
        file.createNewFile();
        return file;
    }

    private File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @After
    public void tearDown() throws Exception {
        deleteRecursive(new File("./testDir"));
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteRecursive(list[i]);
            }
        }
        file.delete();
    }

    @org.junit.Test
    public void testOutput() throws IOException {
        Map<Test, MarathonTestResult> testOutputMap = new HashMap<Test, MarathonTestResult>();
        TestSuite suite = (TestSuite) new TestCreator(false, new StdOutConsole(), new StdOutLogger()).getTest("AllTests");
        createResult(suite, testOutputMap);
        StringWriter writer = new StringWriter();
        TextOutputter outputter = new TextOutputter();
        outputter.output(writer, suite, testOutputMap);
        String actual = writer.toString();
        String expected = "TestSuite: AllTests\n" + "Tests run: 4,  Failures: 1,  Errors: 1,  Time elapsed: 141.602 seconds\n\n"
                + "Testcase: subtest1   Passed\n" + " took 0 seconds\n\n" + "Testcase: subtest2   FAILED\n" + "Runtime Exception\n"
                + BaseTestRunner.getFilteredTrace(exception) + " took 0.102 seconds\n\n"
                + "Testcase: testcase1   Caused an ERROR\n" + "Runtime Exception\n" + BaseTestRunner.getFilteredTrace(exception)
                + " took 141.5 seconds\n\n" + "Testcase: testcase2   Passed\n" + " took 0 seconds\n\n";
        assertEquals("Failed ", expected, actual);
    }

    @SuppressWarnings("rawtypes")
    private void createResult(TestSuite suite, Map<Test, MarathonTestResult> testOutputMap) {
        Enumeration allTests = suite.tests();
        while (allTests.hasMoreElements()) {
            Test test = (Test) allTests.nextElement();
            if (test instanceof TestSuite)
                createResult((TestSuite) test, testOutputMap);
            else {
                MarathonTestCase testCase = (MarathonTestCase) test;
                MarathonTestResult result = new MarathonTestResult((junit.framework.Test) test);
                if (testCase.getName().equals("testcase1")) {
                    result.setStatus(MarathonTestResult.STATUS_ERROR);
                    result.setThrowable(exception);
                    result.setDuration(141.5);
                }
                if (testCase.getName().equals("subtest2")) {
                    result.setStatus(MarathonTestResult.STATUS_FAILURE);
                    result.setThrowable(exception);
                    result.setDuration(0.102);
                }
                testOutputMap.put(test, result);
            }
        }
    }
}
