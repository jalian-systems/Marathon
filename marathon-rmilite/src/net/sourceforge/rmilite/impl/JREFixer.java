package net.sourceforge.rmilite.impl;

import java.lang.reflect.Field;
import java.util.Map;

import sun.awt.AppContext;

public class JREFixer {

    private static AppContext mainThreadContext;

    public static void fixThreadAppContext() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (AppContext.getAppContext() != null || mainThreadContext == null) {
            return;
        }
        final Field field = AppContext.class.getDeclaredField("threadGroup2appContext");
        field.setAccessible(true);
        @SuppressWarnings("unchecked") Map<ThreadGroup, AppContext> threadGroup2appContext = (Map<ThreadGroup, AppContext>) field.get(null);
        final ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
        threadGroup2appContext.put(currentThreadGroup, mainThreadContext);
    }

    public static void init() {
        JREFixer.mainThreadContext = AppContext.getAppContext();
    }

}
