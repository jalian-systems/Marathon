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
package net.sourceforge.marathon.action;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class TestScreenCapture {

    private File file;
    private JFrame frame;

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
        file = File.createTempFile("screenCaptureTest", ".png");
    }

    @After
    public void tearDown() throws Exception {
        file.delete();
        if (frame != null)
            frame.dispose();
    }

    @Test
    public void testCaptureScreen() throws Exception {
        ScreenCaptureAction capture = new ScreenCaptureAction(file.getAbsolutePath(), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance());
        capture.play(null);
        assertTrue("Capture did not succeed", file.exists() && file.isFile() && file.length() > 0);
    }

    @Test
    public void testCaptureWindow() throws Exception {
        frame = new JFrame("ScreenCaptureTest");
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("deprecation")
            public void run() {
                frame.show();
            }
        });
        WindowMonitor.getInstance().waitForWindowToOpen(60000, "ScreenCaptureTest", ScriptModelServerPart.getModelServerPart());
        ScreenCaptureAction capture = new ScreenCaptureAction(file.getAbsolutePath(), "ScreenCaptureTest",
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance());
        capture.play(null);
        assertTrue("Capture did not succeed", file.exists() && file.isFile() && file.length() > 0);
    }
}
