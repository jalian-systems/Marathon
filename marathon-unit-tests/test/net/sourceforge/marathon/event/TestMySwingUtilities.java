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
package net.sourceforge.marathon.event;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Window;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.recorder.ITopLevelWindowListener;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class TestMySwingUtilities {
    @BeforeClass
    public static void setupClass() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
    }

    @AfterClass
    public static void teardownClass() {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWaitForSwing() throws Throwable {
        DialogForTesting dialog1 = new DialogForTesting(this.getClass().getName());
        dialog1.addButton("foo", "bar");
        DialogForTesting dialog2 = new DialogForTesting(this.getClass().getName() + ".2");
        final HashSet<Window> hashWindows = new HashSet<Window>();
        ITopLevelWindowListener listener = new ITopLevelWindowListener() {
            public void topLevelWindowCreated(Window window) {
                hashWindows.add(window);
            }

            public void topLevelWindowDestroyed(Window window) {
                hashWindows.remove(window);
            }
        };
        try {
            WindowMonitor.getInstance().addTopLevelWindowListener(listener);
            assertEquals("should be nothing up", toSet(new Object[] {}), hashWindows);
            dialog1.show();
            assertEquals("dialog1 should have been shown", toSet(new Object[] { dialog1 }), hashWindows);
            dialog2.show();
            // new Snooze(500);
            assertEquals("dialog2 should have been shown", toSet(new Object[] { dialog1, dialog2 }), hashWindows);
            dialog1.dispose();
            dialog2.dispose();
            AWTSync.sync();
            assertNull("windows 1 should be closed", WindowMonitor.getInstance().getWindow(dialog1.getTitle()));
            assertNull("windows 2 should be closed", WindowMonitor.getInstance().getWindow(dialog2.getTitle()));
            if (hashWindows.size() != 0) {
                Thread.sleep(100); // doesn't matter, we know this is a race
                                   // condition, but who cares
                assertEquals("should have disposed both", toSet(new Object[] {}), hashWindows);
            }
        } finally {
            dialog1.dispose();
            dialog2.dispose();
            WindowMonitor.getInstance().removeTopLevelWindowListener(listener);
        }
    }

    private HashSet<Object> toSet(Object[] a) {
        return new HashSet<Object>(Arrays.asList(a));
    }
}
