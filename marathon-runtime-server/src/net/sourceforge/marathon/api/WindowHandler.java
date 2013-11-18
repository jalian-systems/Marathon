package net.sourceforge.marathon.api;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.sourceforge.marathon.player.MarathonJava;

public class WindowHandler implements AWTEventListener {

    public static abstract interface WindowPredicate {
        public abstract boolean shouldHandle(Window w);
    }

    private static class Handler {

        public enum Action {
            DISPOSE, IGNORE, FAIL
        }

        private WindowPredicate predicate;
        private Action action;

        public Handler(WindowPredicate predicate, Action action) {
            this.predicate = predicate;
            this.action = action;
        }

        public boolean handled(Window window) {
            if(predicate.shouldHandle(window)) {
                if(action == Action.DISPOSE)
                    window.dispose();
                else if(action == Action.FAIL)
                    MarathonJava.failTest("Window matched " + predicate + ". Failing the test");
                return true ;
            }
            return false;
        }

    }

    private static WindowHandler instance;
    private final List<Handler> handlers;

    public WindowHandler() {
        handlers = new ArrayList<Handler>();
    }

    public static void add(WindowPredicate predicate, String action) {
        if (instance == null) {
            instance = new WindowHandler();
            Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.WINDOW_EVENT_MASK);
        }
        instance.addHandler(predicate, action);
    }

    private void addHandler(WindowPredicate predicate, String saction) {
        Handler.Action action = Handler.Action.IGNORE;
        if(saction.equals("dispose")) {
            action = Handler.Action.DISPOSE;
        } else if(saction.equals("fail"))
            action = Handler.Action.FAIL;
        handlers.add(new Handler(predicate, action));
    }

    public static void add(String title, String className, String action) {
        if (instance == null) {
            instance = new WindowHandler();
            Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.WINDOW_EVENT_MASK);
        }
        instance.addHandler(title, className, action);
    }

    private void addHandler(final String title, final String className, String saction) {
        Handler.Action action = Handler.Action.IGNORE;
        if(saction.equals("dispose")) {
            action = Handler.Action.DISPOSE;
        } else if(saction.equals("fail"))
            action = Handler.Action.FAIL;
        WindowPredicate titlePredicate = new WindowPredicate() {
            @Override public boolean shouldHandle(Window w) {
                boolean titleMatches ;
                String wtitle = getTitle(w);
                if (wtitle == null)
                    titleMatches = false;
                else if (title.startsWith("/")) {
                    titleMatches = wtitle.matches(title.substring(1));
                } else {
                    titleMatches = title.equals(wtitle);
                }
                if(className == null)
                    return titleMatches ;
                return titleMatches && className.equals(w.getClass().getName());
            }

            private String getTitle(Window w) {
                if (w instanceof JFrame)
                    return ((JFrame) w).getTitle();
                else if (w instanceof JDialog)
                    return ((JDialog) w).getTitle();
                return null;
            }

            @Override public String toString() {
                return "(Title = '" + title + "', className = '" + className + "')";
            }
        };
        handlers.add(new Handler(titlePredicate, action));
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof WindowEvent) {
            switch (event.getID()) {
            case WindowEvent.WINDOW_OPENED:
                handleWindow((Window) event.getSource());
                break;
            }
        }
    }

    private void handleWindow(Window window) {
        for (Handler handler : handlers) {
            if (handler.handled(window))
                return;
        }
    }

}
