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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.checklist.CheckList;
import net.sourceforge.marathon.junit.MarathonTestCase;

public class XMLOutputter implements IOutputter {
    public XMLOutputter() {
        super();
    }

    public void output(Writer writer, Test testSuite, Map<Test, MarathonTestResult> testOutputMap) {
        try {
            writer.write("<?xml version=\"1.0\" ?>\n");
            String reportDir = new File(System.getProperty(Constants.PROP_REPORT_DIR)).getName();
            writer.write("<test projectname='" + System.getProperty(Constants.PROP_PROJECT_NAME, "") + "' " + "reportdir='"
                    + reportDir + "' " + ">\n");
            printResult("", writer, testSuite, testOutputMap);
            writer.write("</test>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printResult(String indent, Writer writer, Test test, Map<Test, MarathonTestResult> testOutputMap) throws IOException {
        if (test instanceof TestSuite) {
            TestSuite suite = (TestSuite) test;
            writer.write(indent + "<testsuite name=\"" + suite.getName() + "\" >\n");
            Enumeration<Test> testsEnum = suite.tests();
            while (testsEnum.hasMoreElements()) {
                printResult(indent + "  ", writer, (Test) testsEnum.nextElement(), testOutputMap);
            }
            writer.write(indent + "</testsuite>\n");
        } else {
            MarathonTestResult result = (MarathonTestResult) testOutputMap.get(test);
            writeResultXML(indent, writer, result, test);
        }
    }

    private void writeResultXML(String indent, Writer writer, MarathonTestResult result, Test test) throws IOException {
        if (result == null)
            return;
        String durationStr = NumberFormat.getInstance().format(result.getDuration());
        int status = result.getStatus();
        StringBuilder xml = new StringBuilder();
        xml.append(indent);
        xml.append("<testcase name=\"");
        xml.append(result.getTestName());
        xml.append("\" status=\"");
        xml.append(status);
        xml.append("\" time=\"");
        xml.append(durationStr);
        xml.append("\" >\n");
        if (test instanceof MarathonTestCase) {
            MarathonTestCase mtestcase = (MarathonTestCase) test;
            ArrayList<CheckList> checklists = mtestcase.getChecklists();
            if (checklists.size() > 0) {
                int index = 1;
                for (CheckList checkList : checklists) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    checkList.saveXML(indent, baos, index++);
                    xml.append(new String(baos.toByteArray()));
                }
            }
        }
        if (status == MarathonTestResult.STATUS_PASS) {
            xml.append(indent).append("</testcase>\n");
        } else {
            String stackTrace = " ";
            Throwable throwable = result.getThrowable();
            if (throwable != null)
                stackTrace = BaseTestRunner.getFilteredTrace(throwable);
            String captureDir = System.getProperty(Constants.PROP_IMAGE_CAPTURE_DIR);
            if (captureDir != null && test instanceof MarathonTestCase) {
                File[] files = ((MarathonTestCase) test).getScreenCaptures();
                List<File> fileList = new ArrayList<File>();
                for (int i = 0; i < files.length; i++) {
                    fileList.add(files[i]);
                }
                /**
                 * We have to sort them, because they are not guaranteed to be
                 * in any particular order, and tend to be in reverse order
                 * (ordered by newest to oldest file)
                 */
                Collections.sort(fileList);
                if (fileList.size() > 0) {
                    xml.append("<screen_captures>");
                    Iterator<File> it = fileList.iterator();
                    while (it.hasNext()) {
                        File file = (File) it.next();
                        xml.append("<screen_capture file=\"").append(file.getName()).append("\"/>");
                    }
                    xml.append("</screen_captures>");
                }
            }
            xml.append("<![CDATA[").append(stackTrace);
            xml.append(indent).append("]]></testcase>\n");
        }
        writer.write(xml.toString());
    }
}
