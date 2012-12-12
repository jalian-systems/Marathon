package net.sourceforge.marathon.display;

import java.util.Date;

import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.LogRecord;
import net.sourceforge.marathon.api.LogRecord.LogType;
import net.sourceforge.marathon.display.DisplayWindow.DisplayView;

public class LogViewLogger implements ILogger {
    /**
     * 
     */
    private final DisplayView displayView;

    /**
     * @param displayWindow
     */
    LogViewLogger(DisplayView displayWindow) {
        displayView = displayWindow;
    }

    private void log(LogRecord r) {
        displayView.getLogView().addLog(r);
    }

    public void info(String module, String message) {
        log(new LogRecord(LogType.LOG_INFO, message, null, module, new Date()));
    }

    public void info(String module, String message, String description) {
        log(new LogRecord(LogType.LOG_INFO, message, description, module, new Date()));
    }

    public void warning(String module, String message) {
        log(new LogRecord(LogType.LOG_WARNING, message, null, module, new Date()));
    }

    public void warning(String module, String message, String description) {
        log(new LogRecord(LogType.LOG_WARNING, message, description, module, new Date()));
    }

    public void error(String module, String message) {
        log(new LogRecord(LogType.LOG_ERROR, message, null, module, new Date()));
    }

    public void error(String module, String message, String description) {
        log(new LogRecord(LogType.LOG_ERROR, message, description, module, new Date()));
    }

}