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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponentMock;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAssertText {
    private static final ComponentId ID = new ComponentId("text.name");

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
        System.setProperty(Constants.PROP_IMAGE_CAPTURE_DIR, "./testDir");
        createDir("./testDir");
    }

    @After
    public void tearDown() throws Exception {
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_IMAGE_CAPTURE_DIR);
        System.setProperties(properties);
        deleteRecursive(new File("./testDir"));
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleteRecursive(list[i]);
            }
        }
        file.delete();
    }

    private File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @Test
    public void testAssertingText() throws Exception {
        MComponentMock component = new MComponentMock();
        ComponentFinder resolver = component.getDummyResolver();
        component.setText("abc");
        ActionTestCase.assertPasses(
                new AssertText(ID, "abc", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertFails(
                new AssertText(ID, "xyz", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testCheckingText() {
        MComponentMock component = new MComponentMock();
        ComponentFinder resolver = component.getDummyResolver();
        component.setText(" stop it you fool ");
        ActionTestCase.assertPasses(new AssertText(ID, " stop it you fool ", ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertPasses(new AssertText(ID, "stop it you fool", ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertPasses(new AssertText(ID, "stop it you fool  \n   \t", ScriptModelServerPart.getModelServerPart(),
                WindowMonitor.getInstance()), resolver);
        ActionTestCase.assertFails(
                new AssertText(ID, "stop it", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
    }

    @Test
    public void testCheckingTextWhenLineBreaksAreDifferent() {
        MComponentMock component = new MComponentMock();
        ComponentFinder resolver = component.getDummyResolver();
        component.setText("this\nis\n\rit");
        ActionTestCase.assertPasses(
                new AssertText(ID, "this\n\ris\nit", ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()),
                resolver);
    }

    @Test
    public void testNullTextDoesntThrowNullPointerException() {
        MComponentMock component = new MComponentMock();
        ComponentFinder resolver = component.getDummyResolver();
        component.setText(null);
        ActionTestCase.assertPasses(
                new AssertText(ID, null, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()), resolver);
    }

    /**
     * Make sure that blocks of text in between occurances of [RE] elementsare
     * treated as regular expressions. for example [RE]some regexp[RE].This is
     * not meant to be a comprehensive test of the regular expression engine. We
     * assume that it will work. The main thing we want to know, is whether the
     * text is correctly passed to said engine
     */
    @Test
    public void testAssertWithRegularExpressions() throws Exception {
        String expected = "/dilzy?\\s?\\w*nack";
        assertExpectationPasses(expected, "dilznack", true);
        assertExpectationPasses(expected, "dilzy nilznack", true);
        assertExpectationPasses(expected, "not even close", false);
        assertExpectationPasses(expected, "dilz//nack", false);
    }

    /**
     * Only the part of the string that appears in between [RE] elements should
     * actually be interpreted as a regular expression. Everything else should
     * be literal text.
     */
    @Test
    public void testAssertWithMultipleRegularExpressions() throws Exception {
        String expected = "/dilz.{2}nack fo{0,2}l";
        assertExpectationPasses(expected, "dilz  nack fool", true);
        assertExpectationPasses(expected, "dilzionack fol", true);
        assertExpectationPasses(expected, "dilz$$nack fl", true);
        assertExpectationPasses(expected, "shizzle", false);
    }

    /**
     * In the offhand chance that the text we want to assert against has the
     * literal text '[RE]' which is used to denote that we are using regular
     * expressions, we have to provide an escaping mechanism which is to prefix
     * it with a '_' character.
     */
    @Test
    public void testAssertWithEscapedRegularExpressions() throws Exception {
        String expected = "dilz_[RE]";
        assertExpectationPasses(expected, "dilz_[RE]", true);
        assertExpectationPasses(expected, "dilz_", false);
    }

    /**
     * Text that appears outside of the bounds of a regular expression should be
     * treated as literal, so we want to make sure that any regular expression
     * meta characters appearing in this area, only match their literal value,
     * and are not interpreted
     */
    @Test
    public void testEscapingOutsideOfRegularExpressions() throws Exception {
        String expected = "\\dilzi\\s";
        assertExpectationPasses(expected, expected, true);
        assertExpectationPasses(expected, "9ilzi\t", false);
    }

    /**
     * Quite often, we have the case, where the text that we're asserting
     * against will span multiple lines. Make sure that it is possible to embed
     * regular expressions inside strings such as these.
     */
    @Test
    public void testRegularExpressionsInsideMultiLineStrings() throws Exception {
        String expected = "/first\n" + "\\w{2}\n" + "third\n";
        assertExpectationPasses(expected, "first\nxx\nthird\n", true);
        assertExpectationPasses(expected, "first\nx\nthird\n", false);
    }

    /**
     * Let's also check the case where, not only does the string itself span
     * multiple lines, but the regular expression itself spans multiple lines.
     */
    @Test
    public void testRegularExpressionsSpanningMultipleLines() throws Exception {
        String expected = "/first spl\n" + "it{0,2}\n" + "line\n" + "last";
        assertExpectationPasses(expected, "first spl\nitt\nline\nlast", true);
        assertExpectationPasses(expected, "first spl\ni\nline\nlast", true);
        assertExpectationPasses(expected, "first spl\nittt\nline\nlast", false);
    }

    /**
     * What happens when we start the regular expression at the beginning of the
     * very beginning of the string, such that there is no character preceding
     * the first regular expression identifier?
     */
    @Test
    public void testRegularExpressionRightFromTheGetGo() throws Exception {
        String expected = "/t{3}";
        assertExpectationPasses(expected, "ttt", true);
        assertExpectationPasses(expected, "tt", false);
    }

    @Test
    public void testToPython() {
        assertEquals("assert_p('text.name', 'Text', 'foo')\n", new AssertText(ID, "foo",
                ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).toScriptCode());
    }

    private void assertExpectationPasses(String expected, String actual, boolean shouldPass) {
        boolean passed = true;
        MComponentMock component = new MComponentMock();
        component.setText(actual);
        try {
            new AssertText(ID, expected, ScriptModelServerPart.getModelServerPart(), WindowMonitor.getInstance()).play(component
                    .getDummyResolver());
        } catch (TestException e) {
            passed = false;
        }
        assertEquals("check that <" + expected + "> matches <" + actual + ">", shouldPass, passed);
    }
}
