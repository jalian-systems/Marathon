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
package net.sourceforge.marathon.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JList;
import javax.swing.JTextField;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.Test;

public class TestMComponent {
    public final class MyTextField extends JTextField {
        private static final long serialVersionUID = 1L;

        public Object getSystemProperties() {
            return System.getProperties();
        }

        public Object getSomeArray() {
            return new int[] { 1, 2, 3 };
        }
    }

    @Test
    public void testComponentId() {
        MComponent component = new MComponent(null, "octo", null, WindowMonitor.getInstance()) {
            public String getComponentInfo() {
                return "pus";
            }
        };
        assertEquals(new ComponentId("octo", "pus"), component.getComponentId());
    }

    @Test
    public void testSingleClickInterfaceIsBackwardCompatible() throws Exception {
        assertNotNull(MComponent.class.getMethod("click", new Class[] { int.class }));
    }

    @Test
    public void testGetProperty() throws Exception {
        JTextField f = new MyTextField();
        MTextComponent mtc = new MTextComponent(f, "some name", null, WindowMonitor.getInstance());
        assertTrue(mtc.getProperty("Text.Bytes.Class") != null);
        assertTrue(mtc.getProperty("Component.SystemProperties[os.arch]") != null);
        assertEquals(mtc.getProperty("Component.SomeArray[1]"), "2");
        assertEquals(mtc.getProperty("Component.SomeArray.size"), "3");
    }

    @Test
    public void testMethods() throws Exception {
        JList l = new JList();
        MList ml = new MList(l, "Some other name", null, WindowMonitor.getInstance());
        ml.getMethods();
    }
}
