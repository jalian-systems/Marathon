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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

public class TestOutputPane {
    private TextAreaOutput outputPane;

    @Before
    public void setUp() throws Exception {
        outputPane = new TextAreaOutput();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOutputPane() throws InterruptedException, InvocationTargetException {
        assertEquals("", outputPane.getText());
        outputPane.append("foo", IStdOut.STD_OUT);
        outputPane.append("foo", IStdOut.STD_OUT);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        assertEquals("foofoo", outputPane.getText());
        outputPane.clear();
        assertEquals("", outputPane.getText());
    }
}
