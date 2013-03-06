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
package net.sourceforge.marathon.junit.textui;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import net.sourceforge.marathon.Constants;

public class TestLinkXMLOutputter implements IOutputter {
    public TestLinkXMLOutputter() {
        super();
    }

    public void output(Writer writer, Test testSuite, Map<Test, MarathonTestResult> testOutputMap) {
        try {
            writer.write("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>\n");
            writer.write("<results>\n");
            String reportDir = new File(System.getProperty(Constants.PROP_REPORT_DIR)).getName();
            writer.write("<!-- Project name: '" + System.getProperty(Constants.PROP_PROJECT_NAME, "") + "' - " + "Report dir: '" + reportDir + "' - START -->\n");
            writeTestsuite("", writer, testSuite, testOutputMap);
            writer.write("<!-- Project name: '" + System.getProperty(Constants.PROP_PROJECT_NAME, "") + "' - " + "Report dir: '" + reportDir + "' - END -->\n");
            writer.write("</results>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTestsuite(String indent, Writer writer, Test test, Map<Test, MarathonTestResult> testOutputMap) throws IOException {
        if (test instanceof TestSuite) {
            TestSuite suite = (TestSuite) test;
            writer.write(indent + "<!-- Testsuite name: '" + suite.getName() + "' - START -->\n");
            Enumeration<Test> testsEnum = suite.tests();
            while (testsEnum.hasMoreElements()) {
            	writeTestsuite(indent + "  ", writer, (Test) testsEnum.nextElement(), testOutputMap);
            }
            writer.write(indent + "<!-- Testsuite name: '" + suite.getName() + "' - END -->\n");
        } else {
            MarathonTestResult result = (MarathonTestResult) testOutputMap.get(test);
            writeTestCase(indent, writer, result, test);
        }
    }

    private void writeTestCase(String indent, Writer writer, MarathonTestResult result, Test test) throws IOException {
        writer.write(indent + "<testcase external_id=\"" + result.getTestName() + "\" >\n");
        writeTestCaseResult(indent, writer, result, test);
        writer.write(indent + "</testcase>\n");
    }


    private void writeTestCaseResult(String indent, Writer writer, MarathonTestResult result, Test test) throws IOException {
        String testLinkResult = "n";
        String testLinkNotes = "";
        int status = MarathonTestResult.STATUS_NONE;
        if (result != null)
        {
	        status = result.getStatus();
	        if (status == MarathonTestResult.STATUS_PASS)
	        	testLinkResult = "p";
	        else
	        {
	        	if (status == MarathonTestResult.STATUS_FAILURE)
	        		testLinkResult = "f";
		        else if (status == MarathonTestResult.STATUS_ERROR)
		        	testLinkResult = "b";

		        if (status != MarathonTestResult.STATUS_PASS) {
		            String stackTrace = " ";
		            Throwable throwable = result.getThrowable();
		            if (throwable != null)
		                stackTrace = BaseTestRunner.getFilteredTrace(throwable);
		            testLinkNotes = "<![CDATA[" + stackTrace + "]]>";
		        }
	        }
        }
        writer.write(indent + "<result>" + testLinkResult + "</result>\n");
        writer.write(indent + "<notes>" + testLinkNotes + "</notes>\n");
    }
}
