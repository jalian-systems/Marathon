/*******************************************************************************
 *  
 *  $Id: CurrentFixture.java 175 2008-12-22 10:07:39Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
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
package net.sourceforge.marathon.python;

import javax.swing.JFrame;

public class CurrentFixture {
    private TestPythonScript current;
    public Runnable body;
    private JFrame frame;

    public CurrentFixture() {
        current = TestPythonScript.current;
        body = TestPythonScript.body;
    }

    public void setup() throws Exception {
        current.setup();
        frame = new JFrame("Trivial Fixture");
        frame.pack();
        frame.setVisible(true);
    }

    public void teardown() throws Exception {
        if (frame != null)
            frame.setVisible(false);
        current.teardown();
    }

    public void setPlaceHolder(Object o) {
        TestPythonScript.placeHolder = o;
    }
}
