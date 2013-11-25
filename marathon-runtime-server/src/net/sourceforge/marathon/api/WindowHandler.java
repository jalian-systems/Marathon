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
            IGNORE, FAIL, ABORT, IGNORE_CLOSE, FAIL_CLOSE
        }

        private WindowPredicate predicate;
        private Action action;

        public Handler(WindowPredicate predicate, Action action) {
            this.predicate = predicate;
            this.action = action;
        }

        public boolean handled(Window window) {
            if(predicate.shouldHandle(window)) {
                if(action == Action.IGNORE_CLOSE)
                    window.dispose();
                else if(action == Action.FAIL)
                    MarathonJava.failTest("Window matched " + predicate + ". Failing the test");
                else if(action == Action.ABORT)
                    MarathonJava.abortTest("Window matched " + predicate + ". Aborting the test");
                else if(action == Action.FAIL_CLOSE) {
                    window.dispose();
                    MarathonJava.failTest("Window matched " + predicate + ". Failing the test");
                }
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

    public static void addPredicate(WindowPredicate predicate, String action) {
        if (instance == null) {
            instance = new WindowHandler();
            Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.WINDOW_EVENT_MASK);
        }
        instance.addHandler(predicate, action);
    }

    private void addHandler(WindowPredicate predicate, String saction) {
        handlers.add(new Handler(predicate, findAction(saction)));
    }

    private Handler.Action findAction(String saction) {
        Handler.Action action = Handler.Action.IGNORE;
        if(saction.equals("ignore-close"))
            action = Handler.Action.IGNORE_CLOSE;
        else if(saction.equals("fail"))
            action = Handler.Action.FAIL;
        else if(saction.equals("abort"))
            action = Handler.Action.ABORT;
        else if(saction.equals("fail-close"))
            action = Handler.Action.FAIL_CLOSE;
        return action;
    }

    public static void add(String title, String className, String action) {
        if (instance == null) {
            instance = new WindowHandler();
            Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.WINDOW_EVENT_MASK);
        }
        instance.addHandler(title, className, action);
    }

    private void addHandler(final String title, final String className, String saction) {
        Handler.Action action = findAction(saction);
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
