package net.sourceforge.marathon;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateTestSuite {

    static String pwd = "/Users/dakshinamurthykarra/Projects/workspaces/marathon-workspace/marathon/marathon-unit-tests/test";
    static String code_prefix = "package net.sourceforge.marathon;\n" + "\n" + "import org.junit.runner.RunWith;\n"
            + "import org.junit.runners.Suite;\n" + "\n" + "@RunWith(Suite.class)\n" + "\n" + "@Suite.SuiteClasses({\n";

    static String code_suffix = "})\n\npublic class AllTests {}\n";

    /**
     * @param args
     */
    public static void main(String[] args) {
        ArrayList<String> testClassNames = new ArrayList<String>();
        pwd = new File(".").getAbsolutePath();
        File currentDirectory = new File(pwd);
        collectFiles(testClassNames, currentDirectory);
        try {
            PrintWriter writer = new PrintWriter(new File("net/sourceforge/marathon/AllTests.java"));
            writer.println(code_prefix);
            for (int i = 0; i < testClassNames.size(); i++) {
                String className = testClassNames.get(i);
                writer.print("        " + className + ".class");
                if (i < testClassNames.size() - 1)
                    writer.println(",");
                else
                    writer.println();
            }
            writer.println(code_suffix);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(testClassNames);
    }

    private static void collectFiles(ArrayList<String> testFileNames, File currentDirectory) {
        File[] listFiles = currentDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().startsWith("Test") && pathname.getName().endsWith(".java");
            }
        });
        List<File> asList = Arrays.asList(listFiles);
        for (File file : asList) {
            testFileNames.add(getRelative(file.getAbsolutePath()));
        }
        File[] listDirs = currentDirectory.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File file : listDirs) {
            collectFiles(testFileNames, file);
        }
    }

    private static String getRelative(String absolutePath) {
        if (absolutePath.startsWith(pwd))
            return absolutePath.substring(pwd.length() + 1, absolutePath.length() - 5).replaceAll("\\/", ".");
        return null;
    }
}
