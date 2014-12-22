package net.sourceforge.marathon.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sourceforge.marathon.Constants;

public abstract class AbstractFileConsole implements IConsole {

    public AbstractFileConsole() {
    }

    private static FileWriter consoleLogWriter;

    static {
        try {
            renameFile();
            consoleLogWriter = new FileWriter(new File(Constants.getMarathonProjectDirectory(), "console.log"));
        } catch (Exception e) {
        }
    }

    public void writeToFile(String text) {
        try {
            consoleLogWriter.append(text);
            consoleLogWriter.flush();
        } catch (IOException e) {
        }
    }

    private static void renameFile() {
        File mpdDir = Constants.getMarathonProjectDirectory();
        File file = new File(Constants.getMarathonProjectDirectory(), "console5.log");
        if (file.exists())
            file.delete();
        for (int i = 4; i >= 0; i--) {
            if (i == 0)
                file = new File(mpdDir, "console.log");
            else
                file = new File(mpdDir, createLogFileName(i));
            if (file.exists())
                file.renameTo(new File(mpdDir, createLogFileName(i + 1)));
        }

    }

    private static String createLogFileName(int index) {
        return "console" + ".log." + index;
    }

}
