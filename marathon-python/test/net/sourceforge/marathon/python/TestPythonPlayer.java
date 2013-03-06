/*******************************************************************************
 *  
 *  $Id: TestPythonPlayer.java 176 2008-12-22 11:04:49Z kd $
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPythonPlayer implements IPlaybackListener {
    private static final Boolean TRUE = Boolean.TRUE;
    private static final Boolean FALSE = Boolean.FALSE;
    public static Boolean ran;
    private IScript script;
    private IPlayer player;
    private PlaybackResult expectedResult;
    private PlaybackResult actualResult;
    private String testRoot = "./testDir";

    @Before public void setUp() throws Exception {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.python.PythonScriptModel");
        System.setProperty(Constants.PROP_MODULE_DIRS, testRoot);
        System.setProperty(Constants.PROP_FIXTURE_DIR, testRoot);
        System.setProperty(Constants.PROP_PROJECT_DIR, testRoot);
        createDir(testRoot);
        createInitFileInDir(testRoot);
        ran = Boolean.FALSE;
        String[] codeTest = { "def test():", "    from java.lang import Boolean",
                "    from net.sourceforge.marathon.python import TestPythonPlayer", "", "    TestPythonPlayer.ran = Boolean.TRUE", };
        script = getScriptInstance(new StringWriter(), new StringWriter(), TrivialFixture.convertCode(TrivialFixture.codeFixture,
                codeTest), "bloob.py", new ComponentFinder(Boolean.FALSE, WindowMonitor.getInstance().getNamingStrategy(),
                new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()));
        expectedResult = new PlaybackResult();
        player = script.getPlayer(this, expectedResult);
    }

    private void createInitFileInDir(String dirName) throws IOException {
        File dir = new File(dirName);
        if (dir.exists()) {
            File initFile = new File(testRoot, "__init__.py");
            initFile.createNewFile();
            String parent = dir.getParent();
            if (parent != null && !parent.equals(testRoot))
                createInitFileInDir(parent);
        }
    }

    private void createDir(String dir) {
        File directory = new File(dir);
        if (!directory.exists())
            directory.mkdirs();
    }

    public static IScript getScriptInstance(Writer out, Writer err, String script, String filename, ComponentFinder resolver)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return ScriptModelServerPart.getModelServerPart().getScript(out, err, script, filename, resolver, true,
                WindowMonitor.getInstance(), MarathonAppType.JAVA);
    }

    @After public void tearDown() throws Exception {
        player.halt();
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        properties.remove(Constants.PROP_MODULE_DIRS);
        properties.remove(Constants.PROP_FIXTURE_DIR);
        properties.remove(Constants.PROP_PROJECT_DIR);
        System.setProperties(properties);
        deleteDir(testRoot);
    }

    private void deleteDir(String dirName) {
        File dir = new File(dirName);
        deleteDir(dir);
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory())
                    deleteDir(files[i]);
                files[i].delete();
            }
            dir.delete();
        }
    }

    @Test public synchronized void testPlay() throws Exception {
        assertEquals("script played", FALSE, ran);
        player.play(true);
        this.wait();
        assertEquals("script played", TRUE, ran);
        assertSame("expectedResult", expectedResult, actualResult);
    }

    @Test public synchronized void testPlayWithAppOpen() throws Exception {
        assertEquals("script played", FALSE, ran);
        player.play(false);
        this.wait();
        assertEquals("script played", TRUE, ran);
        assertSame("expectedResult", expectedResult, actualResult);
    }

    public synchronized void xtestHaltDuringPlayback() throws Exception {
        assertEquals("script played", FALSE, ran);
        player.play(true);
        player.halt();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
        assertEquals("script played", FALSE, ran);
    }

    public synchronized void playbackFinished(PlaybackResult result, boolean shutdown) {
        actualResult = result;
        notify();
    }

    public synchronized int lineReached(SourceLine line) {
        return CONTINUE;
    }

    public int methodReturned(SourceLine line) {
        return CONTINUE;
    }

    public int methodCalled(SourceLine line) {
        return CONTINUE;
    }

    public int acceptChecklist(String fileName) {
        return 0;
    }

    public int showChecklist(String filename) {
        return 0;
    }

}
