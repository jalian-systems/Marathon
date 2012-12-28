package net.sourceforge.marathon.api;

import net.sourceforge.marathon.runtime.NullLogger;

public class RuntimeLogger {

    private static ILogger runtimeLogger;

    public static void setRuntimeLogger(ILogger logViewLogger) {
        runtimeLogger = logViewLogger;
    }

    public static ILogger getRuntimeLogger() {
        if(runtimeLogger == null)
            return new NullLogger();
        return runtimeLogger;
    }
}
