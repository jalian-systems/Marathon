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

import javax.swing.JTabbedPane;
import junit.framework.Test;
import junit.framework.TestResult;

/**
 * A TestRunView is shown as a page in a tabbed folder. It contributes the page
 * contents and can return the currently selected tests. A TestRunView is
 * notified about the start and finish of a run.
 */
interface TestRunView {
    /**
     * Returns the currently selected Test in the View
     */
    public Test getSelectedTest();

    /**
     * Activates the TestRunView
     */
    public void activate();

    /**
     * Reveals the given failure
     */
    public void revealFailure(Test failure);

    /**
     * Adds the TestRunView to the test run views tab
     */
    public void addTab(JTabbedPane pane);

    /**
     * Informs that the suite is about to start
     */
    public void aboutToStart(Test suite, TestResult result);

    /**
     * Informs that the run of the test suite has finished
     */
    public void runFinished(Test suite, TestResult result);

    public void reset(Test test);
}
