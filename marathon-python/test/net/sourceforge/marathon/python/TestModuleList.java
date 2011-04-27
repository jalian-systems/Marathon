package net.sourceforge.marathon.python;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.sourceforge.marathon.api.module.Argument;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.python.util.PythonInterpreter;

public class TestModuleList {
    // @formatter:off

    private String testDirectory = "unittests";
    
    private String[] singleEmptyModuleDir = new String[] { 
            testDirectory + "/emptyModuleDir"
            };
    
    private String[] multipleEmptyModuleDirs = new String[] {
            testDirectory + "/emptyModuleDir1",
            testDirectory + "/emptyModuleDir2"
            };
    
    private String[] emptySubDirModuleDirs = new String[] {
            testDirectory + "/emptyModuleDir1/emptySubDir1",
            testDirectory + "/emptyModuleDir2/emptySubDir2"
            };
    
    private String[] nonExistentModuleDir = new String[] {
            testDirectory + "/nonExistentModuleDir"
            };

    private String[] emptyFileModuleDir = new String[] {
            testDirectory + "/emptyFileModuleDir"
            };
    
    private String[] moduleDir = new String[] {
            testDirectory + "/moduleDir"
            };
    
    private String emptyFile = "emptyFile.py";
    
    private String singleMethodFile = "singleMethodFile.py";
    
    private String methodWithWindowFile = "methodWithWindow.py";

    private String firstMethodDefWithNoArgs = 
        "\ndef firstMethod():\n"+
        "   method1Call()\n"+
        "   method2Call(\"In method1\")\n";

    private String methodDefWithOneArg = 
        "\ndef methodWithOneArg(arg1):\n"+
        "   method1Call()\n"+
        "   method2Call(\"In method1\")\n";

    private String methodDefWithArgsDefaultAssignment = 
        "\ndef methodWithArgs(argNoDefault, argString='string', argInt=10, argFloat=10.5, argTuple=(1,2), argList=['entry1', 'entry2'], argBool=true):\n"+
        "   method1Call(arg1)\n"+
        "   method2Call(\"In method1\")\n";

    private String defWithDoc = 
        "\ndef methodWithDoc():\n" +
        "   '''This is the documentation'''\n"+
        "   method1Call()\n"+
        "   method2Call(\"In method1\")\n";

    private String multipleMethodFile = "multipleMethodFile.py";

    private String multipleMethodDefns = 
        "\ndef firstMethod():\n"+
        "   method1Call()\n"+
        "   method2Call(\"In method1\")\n"+
        "\ndef secondMethod():\n"+
        "   method3Call()\n"+
        "   method4Call(\"In second method\")\n";

    private String methodWithWindowDefn = 
        "\ndef methodWithWindow():\n"+
        "   if window('Window Name'):\n"+
        "       method1Call()\n"+
        "       method2Call(\"In method1\")\n";

    private String methodWithWindowInBetweenDefn = 
        "\ndef methodWithWindow():\n"+
        "   a=10\n"+
        "   if window('Window Name'):\n"+
        "       method2Call(\"In method1\")\n";

    private String methodWithWindowDefnWithComment =
        "\ndef methodWithWindow():\n" +
        "   '''This is a comment'''\n"+
        "   if window('Window Name'):\n"+
        "       method1Call()\n"+
        "       method2Call(\"In method1\")\n";

    // @formatter:on

    @Before public void setup() {

    }

    @After public void teardown() {
        File dir = new File(testDirectory);
        deleteDir(dir);
    }

    /**
     * Creates the directories with given names under the directory specified by
     * testDirecory.
     * 
     * @param directories
     * @throws IOException
     */
    private void createDirectories(String[] directories) throws IOException {
        for (int i = 0; i < directories.length; i++) {
            createDirectory(directories[i]);
        }
    }

    private File createDirectory(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
            File initFile = new File(dir, "__init__.py");
            initFile.createNewFile();
        }
        return dir;
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory())
                    deleteDir(files[i]);
                else
                    files[i].delete();
            }
            dir.delete();
        }
    }

    private PythonInterpreter createPythonInterpreter() throws IOException {
        Properties props = System.getProperties();
        // String moduleDirs = getConcatenatedStringFromArray(moduleDir, ';');
        props.setProperty("python.path", (new File(testDirectory)).getCanonicalPath());
        PythonInterpreter.initialize(System.getProperties(), props, new String[] { "" });
        return new PythonInterpreter();
    }

    /**
     * Test to check when the module directory specified does not exists.
     */
    @Test public void testNonExistentDir() {
        deleteDir(new File(nonExistentModuleDir[0]));
        ModuleList moduleList = new ModuleList(null, nonExistentModuleDir);
        Module root = moduleList.getRoot();
        Assert.assertNotNull(root);
        Assert.assertTrue(root.getName().equals("Root"));
        Assert.assertNull(root.getParent());
        Assert.assertEquals(0, root.getChildren().size());
    }

    /**
     * Test to check where a single Module Directory is specified which is
     * empty.
     * 
     * @throws IOException
     */
    @Test public void testSingleEmptyModuleDir() throws IOException {
        createDirectories(singleEmptyModuleDir);
        ModuleList moduleList = new ModuleList(null, singleEmptyModuleDir);
        Module root = moduleList.getRoot();
        Assert.assertNotNull(root);
        Assert.assertTrue(root.getName().equals("Root"));
        Assert.assertNull(root.getParent());
        Assert.assertEquals(0, root.getChildren().size());
    }

    /**
     * Test to check where a multiple Module Directories are specified which are
     * empty.
     * 
     * @throws IOException
     */
    @Test public void testMultipleEmptyModuleDir() throws IOException {
        createDirectories(multipleEmptyModuleDirs);
        ModuleList moduleList = new ModuleList(null, singleEmptyModuleDir);
        Module root = moduleList.getRoot();
        Assert.assertNotNull(root);
        Assert.assertTrue(root.getName().equals("Root"));
        Assert.assertNull(root.getParent());
        Assert.assertEquals(0, root.getChildren().size());
    }

    /**
     * Test to check when the module directory and its sub directories are
     * empty.
     * 
     * @throws IOException
     */
    @Test public void testEmptySubDir() throws IOException {
        createDirectories(emptySubDirModuleDirs);
        ModuleList moduleList = new ModuleList(null, singleEmptyModuleDir);
        Module root = moduleList.getRoot();
        Assert.assertNotNull(root);
        Assert.assertTrue(root.getName().equals("Root"));
        Assert.assertNull(root.getParent());
        Assert.assertEquals(0, root.getChildren().size());
    }

    /**
     * Test to check when the file in the module directory is empty.
     * 
     * @throws IOException
     */
    @Test public void testEmptyFileInModDir() throws IOException {
        createDirectories(emptyFileModuleDir);
        createFile(emptyFileModuleDir[0], emptyFile);
        ModuleList moduleList = new ModuleList(null, emptyFileModuleDir);
        Module root = moduleList.getRoot();
        Assert.assertEquals(0, root.getChildren().size());
        Assert.assertTrue(verifyModuleStructure(root, testDirectory));
    }

    /**
     * Test to check when the module directory has a file which has single
     * method defined in it.
     * 
     * @throws IOException
     */
    @Test public void testFileWithSingleMethodInModDir() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], singleMethodFile);
        writeIntoFile(firstMethodDefWithNoArgs, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertTrue(function.getName().equals("firstMethod"));
    }

    @Test public void testMultipleMethodsInFile() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], multipleMethodFile);
        writeIntoFile(multipleMethodDefns, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(2, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertTrue(function.getName().equals("firstMethod"));
        function = functionsInFile.get(1);
        Assert.assertTrue(function.getName().equals("secondMethod"));
    }

    /**
     * Test to check the arguments of the function definition in the file.
     * 
     * @throws IOException
     */
    @Test public void testMethodWithOneArg() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], singleMethodFile);
        writeIntoFile(methodDefWithOneArg, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertEquals(function.getName(), "methodWithOneArg");

        List<Argument> arguments = function.getArguments();
        Assert.assertEquals(1, arguments.size());

    }

    /**
     * Test to check the arguments with default assignments of the function
     * definition in the file.
     * 
     * @throws IOException
     */
    @Test public void testMethodWithArgs() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], singleMethodFile);
        writeIntoFile(methodDefWithArgsDefaultAssignment, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertEquals(function.getName(), "methodWithArgs");

        List<Argument> arguments = function.getArguments();
        Assert.assertEquals(7, arguments.size());
        Assert.assertEquals("", function.getDocumentation());

        int constant = 0;
        Argument argument;

        argument = arguments.get(constant++);
        Assert.assertEquals("argNoDefault", argument.getName());

        argument = arguments.get(constant++);
        Assert.assertEquals("argString", argument.getName());
        Assert.assertEquals("string", argument.getDefault());
        // Assert.assertEquals(Type.STRING, argument.ge)

        argument = arguments.get(constant++);
        Assert.assertEquals("argInt", argument.getName());
        Assert.assertEquals("10", argument.getDefault());

        argument = arguments.get(constant++);
        Assert.assertEquals("argFloat", argument.getName());
        Assert.assertEquals("10.5", argument.getDefault());

        argument = arguments.get(constant++);
        Assert.assertEquals("argTuple", argument.getName());
        List<String> defaultList = argument.getDefaultList();
        Assert.assertNotNull(defaultList);
        Assert.assertEquals(2, defaultList.size());

        Assert.assertEquals("1", defaultList.get(0));
        Assert.assertEquals("2", defaultList.get(1));

        argument = arguments.get(constant++);
        Assert.assertEquals("argList", argument.getName());
        defaultList = argument.getDefaultList();
        Assert.assertNotNull(defaultList);
        Assert.assertEquals(2, defaultList.size());

        Assert.assertEquals("entry1", defaultList.get(0));
        Assert.assertEquals("entry2", defaultList.get(1));

        argument = arguments.get(constant++);
        Assert.assertEquals("argBool", argument.getName());
        Assert.assertNull(argument.getDefault());
    }

    /**
     * Test to check the documentation of the method.
     * 
     * @throws IOException
     */
    @Test public void testMethodWithDoc() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], singleMethodFile);
        writeIntoFile(defWithDoc, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertEquals("methodWithDoc", function.getName());

        Assert.assertEquals("This is the documentation", function.getDocumentation());
    }

    /**
     * Test to check if the window name is identified for the given method defn.
     * 
     * @throws IOException
     */
    @Test public void testMethodWithWindow() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], methodWithWindowFile);
        writeIntoFile(methodWithWindowDefn, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertEquals(function.getName(), "methodWithWindow");

        Assert.assertEquals("Window Name", function.getWindow());

    }

    @Test public void testMethodWithWindowInBetween() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], methodWithWindowFile);
        writeIntoFile(methodWithWindowInBetweenDefn, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertEquals(function.getName(), "methodWithWindow");

        Assert.assertNull(function.getWindow());

    }

    /**
     * Test to check if the window name is identified for the given method defn.
     * 
     * @throws IOException
     */
    @Test public void testMethodWithWindowWithComment() throws IOException {
        createDirectories(moduleDir);
        File modFile = createFile(moduleDir[0], methodWithWindowFile);
        writeIntoFile(methodWithWindowDefnWithComment, modFile);

        ModuleList moduleList = new ModuleList(createPythonInterpreter(), moduleDir);
        Module root = moduleList.getRoot();
        Assert.assertTrue("Module structure did not match directory structure.", verifyModuleStructure(root, testDirectory));

        List<Module> moduleForModuleDirs = root.getChildren();
        Module modForFirstDir = moduleForModuleDirs.get(0);
        List<Module> modForFilesAndDirsInModDir = modForFirstDir.getChildren();
        Module modForSingleMethodFile = modForFilesAndDirsInModDir.get(0);
        Assert.assertTrue(modForSingleMethodFile.isFile());
        List<Function> functionsInFile = modForSingleMethodFile.getFunctions();
        Assert.assertEquals(1, functionsInFile.size());

        Function function = functionsInFile.get(0);
        Assert.assertEquals(function.getName(), "methodWithWindow");

        Assert.assertEquals("Window Name", function.getWindow());

    }

    private boolean verifyModuleStructure(Module root, String superDir) {
        List<Module> children = root.getChildren();
        for (Module childModule : children) {
            String name = childModule.getName();
            String path = superDir + "/" + name;
            if (!childModule.isFile()) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    if (!verifyModuleStructure(childModule, path))
                        return false;
                } else
                    return false;
            } else {
                path = path + ".py";
                File file = new File(path);
                if (file.exists() && !file.isDirectory())
                    return true;
                else
                    return false;
            }
        }
        return true;
    }

    /**
     * Writes the given code into the given file.
     * 
     * @param code
     * @param file
     * @throws IOException
     */
    private void writeIntoFile(String code, File file) throws IOException {
        FileWriter writer = new FileWriter(file, true);
        writer.write(code);
        writer.flush();
        writer.close();
    }

    /**
     * Creates a file with the given name under the given directory.
     * 
     * @param dirName
     * @param fileName
     * @return
     * @throws IOException
     */
    private File createFile(String dirName, String fileName) throws IOException {
        File dir = createDirectory(dirName);
        File file = new File(dir, fileName);
        if (!file.exists())
            file.createNewFile();
        return file;
    }

}
