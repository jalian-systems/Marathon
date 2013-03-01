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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.runner.TestRunListener;

public class MarathonTestResult {
    public static final int STATUS_NONE = -1;
    public static final int STATUS_PASS = 0;
    public static final int STATUS_ERROR = TestRunListener.STATUS_ERROR;
    public static final int STATUS_FAILURE = TestRunListener.STATUS_FAILURE;
    private Test test;
    private Throwable throwable;
    private double duration;
    private int status;

    public MarathonTestResult(Test test) {
        this.test = test;
        status = STATUS_PASS;
    }

    public final double getDuration() {
        return duration;
    }

    public final int getStatus() {
        return status;
    }

    public final Test getTest() {
        return test;
    }

    public final String getTestName() {
        return ((TestCase) test).getName();
    }

    public final Throwable getThrowable() {
        return throwable;
    }

    public final void setDuration(double duration) {
        this.duration = duration;
    }

    public final void setStatus(int status) {
        this.status = status;
    }

    public final void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public final String getStatusDescription() {
        String desc = (status == STATUS_PASS) ? "Passed" : ((status == STATUS_ERROR) ? "Caused an ERROR" : "FAILED");
        return desc;
    }
}
