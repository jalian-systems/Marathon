package net.sourceforge.marathon.junit.textui;

import net.sourceforge.marathon.api.ILogger;

public class StdOutLogger implements ILogger {
    public void info(String module, String message) {
        info(module, message, null);
    }

    public void info(String module, String message, String description) {
        p("INFO", module, message, description);
    }

    public void warning(String module, String message) {
        warning(module, message, null);
    }

    public void warning(String module, String message, String description) {
        p("WARNING", module, message, description);
    }

    public void error(String module, String message) {
        error(module, message, null);
    }

    public void error(String module, String message, String description) {
        p("ERROR", module, message, description);
    }

    private void p(String type, String module, String message, String description) {
        System.err.println(type + "<" + module + ">: " + message);
        if(description != null)
            System.err.println(description);
    }

}
