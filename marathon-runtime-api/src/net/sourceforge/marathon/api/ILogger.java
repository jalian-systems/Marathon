package net.sourceforge.marathon.api;

public interface ILogger {
    public static final int MESSAGE = 0 ;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    public void msg(String module, String message);

    public void msg(String module, String message, String description);

    public void info(String module, String message);

    public void info(String module, String message, String description);

    public void warning(String module, String message);

    public void warning(String module, String message, String description);

    public void error(String module, String message);

    public void error(String module, String message, String description);

    public void setLogLevel(int level);

    public int getLogLevel();

}
