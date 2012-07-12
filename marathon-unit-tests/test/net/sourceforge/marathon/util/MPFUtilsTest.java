package net.sourceforge.marathon.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import net.sourceforge.marathon.Constants;

import org.junit.Test;

public class MPFUtilsTest {

    @Test public void testEncodeProjectDir() {
        Properties props = new Properties();
        props.setProperty(Constants.PROP_PROJECT_DIR, new File(".").getAbsolutePath());
        String encodeProjectDir = MPFUtils.encodeProjectDir(new File("."), props);
        assertEquals("%marathon.project.dir%/", encodeProjectDir);
    }


    @Test public void testEncodeProjectDirReturnsSameFile() {
        Properties props = new Properties();
        File cwd = new File(".");
        props.setProperty(Constants.PROP_PROJECT_DIR, cwd.getAbsolutePath());
        File[] listFiles = cwd.listFiles();
        File selectedFile = null ;
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isFile()) {
                selectedFile = listFiles[i];
                break;
            }
        }
        assertNotNull(selectedFile);
        String encodeProjectDir = MPFUtils.encodeProjectDir(selectedFile, props);
        assertEquals("%marathon.project.dir%/" + selectedFile.getName(), encodeProjectDir);
    }

    @Test public void testEncodeProjectDirReturnsSameDir() {
        Properties props = new Properties();
        File cwd = new File(".");
        props.setProperty(Constants.PROP_PROJECT_DIR, cwd.getAbsolutePath());
        File[] listFiles = cwd.listFiles();
        File selectedFile = null ;
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isDirectory()) {
                selectedFile = listFiles[i];
                break;
            }
        }
        assertNotNull(selectedFile);
        String encodeProjectDir = MPFUtils.encodeProjectDir(selectedFile, props);
        assertEquals("%marathon.project.dir%/" + selectedFile.getName(), encodeProjectDir);
    }

}
