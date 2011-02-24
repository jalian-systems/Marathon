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
package net.sourceforge.marathon.junit.swingui;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {
    private static String pathToIcons = "net/sourceforge/marathon/junit/swingui/icons/";
    public static final Icon STOP = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "enabled/stop.gif"));
    public static final Icon STOP_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "disabled/stop.gif"));
    public static final Icon RELAUNCH = new ImageIcon(Icons.class.getClassLoader()
            .getResource(pathToIcons + "enabled/relaunch.gif"));
    public static final Icon RELAUNCH_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "disabled/relaunch.gif"));
    public static final Icon RUN = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "enabled/run.gif"));
    public static final Icon RUN_DISABLED = new ImageIcon(Icons.class.getClassLoader()
            .getResource(pathToIcons + "disabled/run.gif"));
    public static final Icon SELECT_NEXT = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "enabled/select_next.gif"));
    public static final Icon SELECT_NEXT_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "disabled/select_next.gif"));
    public static final Icon SELECT_PREV = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "enabled/select_prev.gif"));
    public static final Icon SELECT_PREV_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "disabled/select_prev.gif"));
    public static final Icon JUNIT = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "junit.gif"));
    public static final Icon ERROR = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "error.gif"));
    public static final Icon FAILURE = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "failure.gif"));
    public static final Icon FAILURES = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "failures.gif"));
    public static final Icon HIERARCHY = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "hierarchy.gif"));
    public static final Icon TRACE = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "trace.gif"));
    public static final Icon T_TEST = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "tree/test.gif"));
    public static final Icon T_TESTERROR = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "tree/testerror.gif"));
    public static final Icon T_TESTFAIL = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "tree/testfail.gif"));
    public static final Icon REPORT = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "enabled/report.gif"));
    public static final Icon REPORT_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "disabled/report.gif"));
    public static final Icon T_TESTOK = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "tree/testok.gif"));
    public static final Icon T_TSUITE = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "tree/tsuite.gif"));
    public static final Icon T_TSUITEERROR = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "tree/tsuiteerror.gif"));
    public static final Icon T_TSUITEFAIL = new ImageIcon(Icons.class.getClassLoader().getResource(
            pathToIcons + "tree/tsuitefail.gif"));
    public static final Icon T_TSUITEOK = new ImageIcon(Icons.class.getClassLoader().getResource(pathToIcons + "tree/tsuiteok.gif"));
}
