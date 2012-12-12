package net.sourceforge.marathon.runtime;

import net.sourceforge.marathon.api.ILogger;

public class NullLogger implements ILogger {

    public void info(String module, String message) {
    }

    public void info(String module, String message, String description) {
    }

    public void warning(String module, String message) {
    }

    public void warning(String module, String message, String description) {
    }

    public void error(String module, String message) {
    }

    public void error(String module, String message, String description) {
    }

}
