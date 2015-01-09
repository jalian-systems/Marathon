package net.sourceforge.marathon.display;

import java.io.File;
import java.util.Properties;

import net.sourceforge.marathon.Constants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestConsole {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		createDir("./testDir");
		System.setProperty(Constants.PROP_PROJECT_DIR,
				new File("./testDir").getCanonicalPath());
		System.setProperty(Constants.PROP_FIXTURE_DIR,
				new File("./testDir").getCanonicalPath());
		System.setProperty(Constants.PROP_TEST_DIR,
				new File("./testDir").getCanonicalPath());
		System.setProperty(Constants.PROP_MODULE_DIRS,
				new File("./testDir").getCanonicalPath());
		System.setProperty(Constants.PROP_HOME,
				new File("./testDir").getCanonicalPath());
		new File("./testDir/readme.txt").createNewFile();
		System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL,
				"net.sourceforge.marathon.mocks.MockScriptModel");
	}

	private static File createDir(String name) {
		File file = new File(name);
		file.mkdir();
		return file;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Properties properties = System.getProperties();
		properties.remove(Constants.PROP_PROJECT_DIR);
		properties.remove(Constants.PROP_FIXTURE_DIR);
		properties.remove(Constants.PROP_TEST_DIR);
		properties.remove(Constants.PROP_MODULE_DIRS);
		properties.remove(Constants.PROP_HOME);
		properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
		System.setProperties(properties);
		deleteRecursive(new File("./testDir"));
	}

	private static void deleteRecursive(File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				deleteRecursive(list[i]);
			}
		}
		file.delete();
	}

	@Test
	public void consoleTest() throws Throwable {
		MockDisplayView displayView = new MockDisplayView();
		EditorConsole console = (EditorConsole) displayView.getConsole();
		console.writeToFile("Hello World");
	}
	
}
