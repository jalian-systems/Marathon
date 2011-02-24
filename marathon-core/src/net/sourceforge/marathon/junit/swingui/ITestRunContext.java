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

import javax.swing.ListModel;
import junit.framework.Test;

/**
 * The interface for accessing the Test run context. Test run views should use
 * this interface rather than accessing the TestRunner directly.
 */
public interface ITestRunContext {
    /**
     * Handles the selection of a Test.
     */
    public void handleTestSelected(Test test);

    /**
     * Returns the failure model
     */
    public ListModel getFailures();

    /**
     * Handles the opening of a Test.
     */
    public void handleTestOpened(Test test);
}
