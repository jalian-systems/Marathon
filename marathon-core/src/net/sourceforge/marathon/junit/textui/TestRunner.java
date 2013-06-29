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

import java.io.PrintStream;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.textui.ResultPrinter;
import net.sourceforge.marathon.ArgumentProcessor;
import net.sourceforge.marathon.junit.MarathonResultReporter;
import net.sourceforge.marathon.junit.MarathonTestCase;
import net.sourceforge.marathon.junit.StdOutConsole;
import net.sourceforge.marathon.junit.TestCreator;

public class TestRunner extends junit.textui.TestRunner {
    ArgumentProcessor argProcessor;
    private Test currentTest;
    private TestCreator creator;
    private MarathonResultReporter resultReporter;

    public TestRunner() {
    }

    public TestResult doRun(Test suite, boolean wait) {
        TestResult result = new TestResult();
        TestResultPrinter printer = new TestResultPrinter(System.out);
        resultReporter = new MarathonResultReporter(currentTest);
        result.addListener(resultReporter);
        result.addListener(printer);
        result.addListener(TestRunner.this);
        long startTime = System.currentTimeMillis();
        suite.run(result);
        MarathonTestCase.reset();
        long endTime = System.currentTimeMillis();
        long runTime = (endTime - startTime);
        String xmlFileName = argProcessor.getXmlFileName();
        if (xmlFileName != null) {
            resultReporter.generateReport(new XMLOutputter(), xmlFileName);
        }
        String textFileName = argProcessor.getTextFileName();
        ;
        if (textFileName != null) {
            resultReporter.generateReport(new TextOutputter(), textFileName);
        }
        String htmlFileName = argProcessor.getHtmlFileName();
        if (htmlFileName != null) {
            resultReporter.generateReport(new HTMLOutputter(), htmlFileName);
        }
        String testLinkXmlFileName = argProcessor.getTestLinkXmlFileName();
        if (testLinkXmlFileName != null) {
            resultReporter.generateReport(new TestLinkXMLOutputter(), testLinkXmlFileName);
        }
        printer.printDetails(result, runTime);
        pause(wait);
        return result;
    }

    public TestResult runTests(ArgumentProcessor argProcessor) throws Exception {
        this.argProcessor = argProcessor;
        List<String> tests = this.argProcessor.getTests();
        try {
            creator = new TestCreator(this.argProcessor.getAcceptChecklists(), new StdOutConsole());
            currentTest = creator.getTest(tests);
            return doRun(currentTest, false);
        } catch (Exception e) {
            throw new Exception("Could not create test suite for argument: " + tests);
        }
    }

    private static final class TestResultPrinter extends ResultPrinter {
        public TestResultPrinter(PrintStream writer) {
            super(writer);
        }

        public void printDetails(TestResult result, long runTime) {
            printHeader(runTime);
            printErrors(result);
            printFailures(result);
            printFooter(result);
        }
    }
}
