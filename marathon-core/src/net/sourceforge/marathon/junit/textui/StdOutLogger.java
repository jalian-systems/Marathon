package net.sourceforge.marathon.junit.textui;

import net.sourceforge.marathon.api.ILogger;

public class StdOutLogger implements ILogger {
    private int level;

    public void info(String module, String message) {
        info(module, message, null);
    }

    public void info(String module, String message, String description) {
        if (level <= ILogger.INFO)
            p("INFO", module, message, description);
    }

    public void warning(String module, String message) {
        warning(module, message, null);
    }

    public void warning(String module, String message, String description) {
        if (level <= ILogger.WARN)
            p("WARNING", module, message, description);
    }

    public void error(String module, String message) {
        error(module, message, null);
    }

    public void error(String module, String message, String description) {
        if (level <= ILogger.ERROR)
            p("ERROR", module, message, description);
    }

    private void p(String type, String module, String message, String description) {
        System.err.println(type + "<" + module + ">: " + message);
        if (description != null)
            System.err.println(description);
    }

    public void setLogLevel(int level) {
        this.level = level;
    }

    public int getLogLevel() {
        return level;
    }

    public void msg(String module, String message) {
        msg(module, message, null);
    }

    public void msg(String module, String message, String description) {
        p("MESSAGE", module, message, description);
    }

}
