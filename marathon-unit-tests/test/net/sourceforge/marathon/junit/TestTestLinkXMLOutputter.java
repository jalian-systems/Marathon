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
import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.junit.textui.MarathonTestResult;
import net.sourceforge.marathon.junit.textui.TestLinkXMLOutputter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

public class TestTestLinkXMLOutputter {
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
        System.setProperty(Constants.PROP_REPORT_DIR, "./testDir");
        System.setProperty(Constants.PROP_IMAGE_CAPTURE_DIR, "./testDir");
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
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_IMAGE_CAPTURE_DIR);
        properties.remove(Constants.PROP_REPORT_DIR);
        System.setProperties(properties);
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
    @Ignore
    public void testOutput() throws IOException {
        // TODO:
    	Map<Test, MarathonTestResult> testOutputMap = new HashMap<Test, MarathonTestResult>();
    	TestSuite suite = (TestSuite) new TestCreator(false, new StdOutConsole()).getTest("AllTests");
        createResult(suite, testOutputMap);
        StringWriter writer = new StringWriter();
        TestLinkXMLOutputter outputter = new TestLinkXMLOutputter();
        outputter.output(writer, suite, testOutputMap);
        String actual = writer.toString();
        /*
    	String expected = "<?xml version=\"1.0\" ?>\n" + "<test projectname='' reportdir='testDir' >\n"
                + "<testsuite name=\"AllTests\" >\n" + "  <testsuite name=\"subdir.AllTests\" >\n"
                + "    <testcase name=\"subtest1\" status=\"0\" time=\"0\" >\n" + "    </testcase>\n"
                + "    <testcase name=\"subtest2\" status=\"2\" time=\"0.102\" >\n<![CDATA["
                + BaseTestRunner.getFilteredTrace(exception) + "    ]]></testcase>\n" + "  </testsuite>\n"
                + "  <testcase name=\"testcase1\" status=\"1\" time=\"141.5\" >\n<![CDATA["
                + BaseTestRunner.getFilteredTrace(exception) + "  ]]></testcase>\n"
                + "  <testcase name=\"testcase2\" status=\"0\" time=\"0\" >\n" + "  </testcase>\n" + "</testsuite>\n" + "</test>";
        */
    	String expected = "";
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
