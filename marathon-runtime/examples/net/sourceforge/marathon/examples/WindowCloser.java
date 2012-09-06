package net.sourceforge.marathon.examples;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class WindowCloser implements AWTEventListener {

    private static WindowCloser instance;
    private final String title;

    public WindowCloser(String title) {
        this.title = title;
    }

    public static void init(String title) {
        if (instance != null)
            return;
        instance = new WindowCloser(title);
        Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.WINDOW_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof WindowEvent) {
            switch (event.getID()) {
            case WindowEvent.WINDOW_OPENED:
                String wtitle = getTitle((Window) event.getSource());
                if(title.equals(wtitle))
                    ((Window)event.getSource()).dispose();
                break;
            }
        }
    }

    private String getTitle(Window source) {
        if (source instanceof JFrame)
            return ((JFrame) source).getTitle();
        else if (source instanceof JDialog)
            return ((JDialog) source).getTitle();
        return null;
    }

}
