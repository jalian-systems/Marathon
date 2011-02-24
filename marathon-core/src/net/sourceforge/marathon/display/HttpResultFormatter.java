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
package net.sourceforge.marathon.display;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;

import net.sourceforge.marathon.api.Failure;
import net.sourceforge.marathon.api.PlaybackResult;

public class HttpResultFormatter {
    private StringBuffer result = new StringBuffer(1000);
    private String batchRunFile;
    private int runs, fails;
    public final static String EXTENSION = ".html";

    public HttpResultFormatter(String batchRunFile) {
        this.batchRunFile = batchRunFile;
    }

    public FileFilter getFileFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(EXTENSION);
            }
        };
    }

    public void addThrowable(String testScript, Throwable throwable) {
        runs++;
        fails++;
        String stackTrace = getStackTrace(throwable);
        result.append("<tr style='background-color:#ffcccc;'><td>");
        if (throwable.getMessage() != null)
            result.append(throwable.getMessage() + "<BR>");
        result.append(testScript + "</td>");
        result.append("<td><b>Failed</b><ul>");
        result.append("<li>" + stackTrace + "\n");
        result.append("</ul></td></tr>");
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter out = new StringWriter(1000);
        PrintWriter pw = new PrintWriter(out);
        throwable.printStackTrace(pw);
        return out.toString();
    }

    public void addPlaybackResult(String testScript, PlaybackResult pbResult) {
        if (pbResult == null)
            throw new RuntimeException("Playback Result cannot be null!!!");
        runs++;
        String color = (pbResult.hasFailure()) ? "#ffcccc" : "#99ff99";
        result.append("<tr style='background-color:" + color + ";'><td>" + testScript + "</td>");
        if (pbResult.hasFailure()) {
            fails++;
            result.append("<td><b>Failed</b><ol>");
            Failure[] fails = pbResult.failures();
            for (int i = 0; i < fails.length; i++) {
                result.append("<li>" + fails[i] + "<br>" + fails[i].getMessage() + "\n");
            }
            result.append("</ol></td></tr>");
        } else {
            result.append("<td>Passed</td></tr>\n");
        }
    }

    public String toString() {
        final String footer = "</table></p>" + "  </div></body>\n" + "</html>";
        return getHeader() + result.toString() + footer;
    }

    public String getExtension() {
        return EXTENSION;
    }

    private String getHeader() {
        String header = "<html>\n" + "  <head>\n" + "    <title>Batch Run Result - " + batchRunFile + "</title>\n" + "  </head>\n"
                + "  <body><div align='center'>\n" + "    <H3>Batch Run Result - " + batchRunFile + "</H3>" + "    <p><b>Runs: "
                + runs + "; Fails: " + fails + "</b></p>\n" + "    <p><table border=1 cellpadding=5 cellspacing=0>"
                + "      <tr><td align=center valigh=center><b>File</b></td>\n"
                + "      <td align=center valigh=center><b>Results</b></td></tr>\n";
        return header;
    }
}
