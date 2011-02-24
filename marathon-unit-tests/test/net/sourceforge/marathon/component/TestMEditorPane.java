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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.swing.JEditorPane;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.action.ActionTestCase;
import net.sourceforge.marathon.action.AssertText;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMEditorPane {
    private DialogForTesting dialog;
    private JEditorPane plainEditor;
    private JEditorPane htmlEditor;
    private static String S1 = "developer developer developer developer";
    private static String S2 = "I....Love...This...COMPANY!!";

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
        dialog = new DialogForTesting(getName());
        dialog.setBounds(300, 200, 100, 100);
        plainEditor = new JEditorPane("text/plain", S1);
        htmlEditor = new JEditorPane("text/html", getHTML(S1, false));
        dialog.getContentPane().add(plainEditor);
        dialog.pack();
        dialog.show();
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        dialog.dispose();
        dialog = null;
    }

    // /////////////// Test for both type //////////////////
    @Test
    public void testTypeCheck() throws Exception {
        MEditorPane mEditorPane = new MEditorPane(plainEditor, "plainSong", null, null, WindowMonitor.getInstance());
        assertFalse("Should be a plain document", mEditorPane.isHtmlDocument());
        mEditorPane = new MEditorPane(htmlEditor, "htmlSong", null, null, WindowMonitor.getInstance());
        assertTrue("Should be a html document", mEditorPane.isHtmlDocument());
    }

    // /////////////// test for plain text //////////////////
    // Just to make sure everything is delegated to MTextField correctly
    @Test
    public void testGetSetText() {
        MEditorPane mEditorPane = new MEditorPane(plainEditor, "plainSong", null, null, WindowMonitor.getInstance());
        assertEquals(S1, mEditorPane.getText());
        mEditorPane.setText(S2);
        assertEquals(S2, mEditorPane.getText());
    }

    @Test
    public void testSetTextToEmptyString() throws Exception {
        MEditorPane mEditorPane = new MEditorPane(plainEditor, "plainSong", null, null, WindowMonitor.getInstance());
        mEditorPane.setText("");
        assertEquals("set text to empty string", "", mEditorPane.getText());
    }

    // /////////// test for HTML content ///////////////////////
    @Test
    public void testGetSetHTML() {
        MComponentMock mock = new MComponentMock(htmlEditor, "htmlSong");
        ComponentFinder resolver = mock.getResolver();
        ActionTestCase.assertPasses(new AssertText(null, getHTML(S1, false), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
        htmlEditor.setText(getHTML(S2, false));
        ActionTestCase.assertPasses(new AssertText(null, getHTML(S2, false), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testSetTextToHTMLString() throws Exception {
        MComponentMock mock = new MComponentMock(htmlEditor, "htmlSong");
        ComponentFinder resolver = mock.getResolver();
        htmlEditor.setText("");
        ActionTestCase.assertPasses(
                new AssertText(null, "<html><head></head><body></body></html>", ScriptModelServerPart.getModelServerPart(),
                        WindowMonitor.getInstance()), resolver);
        htmlEditor.setText("hello");
        ActionTestCase.assertPasses(
                new AssertText(null, "<html><head></head><body>hello</body></html>", ScriptModelServerPart.getModelServerPart(),
                        WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testStringCompareInHTMLContext() throws Exception {
        MComponentMock mock = new MComponentMock(htmlEditor, "htmlSong");
        ComponentFinder resolver = mock.getResolver();
        ActionTestCase.assertPasses(new AssertText(null, getHTML(S1, false), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertPasses(new AssertText(null, getHTML(S1, true), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertFails(new AssertText(null, getHTML(S2, true), ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
    }

    private String getHTML(String text, boolean switchAttribute) {
        String attributes = (switchAttribute) ? "color='red' size='24'" : " size='24' color='red'";
        String html = "<html><head></head><body><font " + attributes + ">" + text + "</font></body></html>";
        JEditorPane pane = new JEditorPane("text/html", html);
        return pane.getText();
    }
}
