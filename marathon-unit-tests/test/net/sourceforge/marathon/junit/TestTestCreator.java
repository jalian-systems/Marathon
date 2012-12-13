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
package net.sourceforge.marathon.junit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestSuite;
import net.sourceforge.marathon.Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTestCreator {
    private TestCreator testCreator;

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
        System.setProperty(Constants.PROP_TEST_DIR, "./testDir");
        createDir("./testDir");
        createDir("./testDir/subdir");
        createDir("./testDir/subdir1");
        createDir("./testDir/emptyDir");
        createTestFile("./testDir/testcase1.py");
        createTestFile("./testDir/testcase2.py");
        createTestFile("./testDir/subdir/subtest1.py");
        createTestFile("./testDir/subdir/subtest2.py");
        createFile("./testDir/subdir/notatest.py");
        createTestFile("./testDir/subdir1/subtest1.py");
        createTestFile("./testDir/subdir1/subtest2.py");
        testCreator = new TestCreator(false, new StdOutConsole());
    }

    private File createTestFile(String name) throws IOException {
        File file = createFile(name);
        FileWriter writer = new FileWriter(file);
        writer.write("def test():\n\tpass\n");
        writer.close();
        return file;
    }

    private File createFile(String name) throws IOException {
        File file = new File(name);
        file.createNewFile();
        return file;
    }

    private File createDir(String name) {
        File file = new File(name);
        file.mkdir();
        return file;
    }

    @After
    public void tearDown() throws Exception {
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

    @Test
    public void testGetFile() throws IOException {
        File file = testCreator.getFile("AllTests");
        assertEquals(new File("./testDir").getCanonicalPath(), file.getCanonicalPath());
        file = testCreator.getFile("subdir.AllTests");
        assertEquals(new File("./testDir/subdir").getCanonicalPath(), file.getCanonicalPath());
        file = testCreator.getFile("testcase1");
        assertEquals(new File("./testDir/testcase1.py").getCanonicalPath(), file.getCanonicalPath());
    }

    @Test
    public void testGetTestEmptySuite() throws Exception {
        TestSuite suite = (TestSuite) testCreator.getTest("emptyDir.AllTests");
        assertEquals("Test count in Empty suite", null, suite);
    }

    @Test
    public void testGetTest() throws Exception {
        TestSuite suite = (TestSuite) testCreator.getTest("subdir.AllTests");
        assertEquals("Test count in suite", 2, suite.testCount());
    }

    @Test
    public void testGetTestMultiple() throws Exception {
        List<String> testCaseList = new ArrayList<String>();
        testCaseList.add("subdir.AllTests");
        testCaseList.add("subdir1.AllTests");
        TestSuite suite = (TestSuite) testCreator.getTest(testCaseList);
        assertEquals("Test count in suite", 4, suite.countTestCases());
    }
}
