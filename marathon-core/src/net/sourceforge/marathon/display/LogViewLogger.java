package net.sourceforge.marathon.display;

import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.LogRecord;

public class LogViewLogger implements ILogger {
    private LogView logView;
    private int level;

    /**
     * @param displayWindow
     */
    public LogViewLogger(LogView logView) {
        this.logView = logView;
        setLogLevel();
        logView.setLogger(this);
    }

    private void setLogLevel() {
        Preferences p = Preferences.userNodeForPackage(LogViewLogger.class);
        setLogLevel(p.getInt("loglevel", WARN));
    }

    private void log(LogRecord r) {
        logView.addLog(r);
    }

    public void info(String module, String message) {
        log(new LogRecord(ILogger.INFO, message, null, module, new Date()));
    }

    public void info(String module, String message, String description) {
        log(new LogRecord(ILogger.INFO, message, description, module, new Date()));
    }

    public void warning(String module, String message) {
        log(new LogRecord(ILogger.WARN, message, null, module, new Date()));
    }

    public void warning(String module, String message, String description) {
        log(new LogRecord(ILogger.WARN, message, description, module, new Date()));
    }

    public void error(String module, String message) {
        log(new LogRecord(ILogger.ERROR, message, null, module, new Date()));
    }

    public void error(String module, String message, String description) {
        log(new LogRecord(ILogger.ERROR, message, description, module, new Date()));
    }

    public void setLogLevel(int level) {
        Preferences p = Preferences.userNodeForPackage(LogViewLogger.class);
        p.putInt("loglevel", level);
        try {
            p.flush();
        } catch (BackingStoreException e) {
        }
        this.level = level;
    }

    public int getLogLevel() {
        return level;
    }

    public void msg(String module, String message) {
        log(new LogRecord(ILogger.MESSAGE, message, null, module, new Date()));
    }

    public void msg(String module, String message, String description) {
        log(new LogRecord(ILogger.MESSAGE, message, description, module, new Date()));
    }

}