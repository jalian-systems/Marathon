package net.sourceforge.marathon.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.factories.ButtonBarFactory;

public abstract class Blurb {
    private URL url;
    private String title;
    private int selection;
    private boolean cancel;

    public Blurb(String marker, String title, boolean cancel) {
        this.url = getClass().getResource(marker + ".html");
        this.title = title;
        this.cancel = cancel;
        selection = showMessage();
    }

    public Blurb(String marker, String title) {
        this(marker, title, false);
    }

    protected int showDialog() {
        try {
            BlurbDialog dialog = new BlurbDialog(url, title, cancel);
            dialog.setPreferredSize(new Dimension(640, 480));
            dialog.setLocationRelativeTo(null);
            dialog.pack();
            dialog.centerScreen();
            dialog.setVisible(true);
            return dialog.getSelection();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getSelection() {
        return selection;
    }

    public int showMessage() {
        if (SwingUtilities.isEventDispatchThread()) {
            return showDialog();
        } else {
            final Integer[] ret = new Integer[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override public void run() {
                        ret[0] = showDialog();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return ret[0];
        }
    }

    public static class BlurbDialog extends EscapeDialog {
        private static final long serialVersionUID = 1L;
        private URL url;
        private JButton ok;
        private JButton cancel;
        private int selection = JOptionPane.OK_OPTION;
        private boolean cancelNeeded;

        public BlurbDialog(URL url, String title, boolean cancelNeeded) throws IOException {
            super((Frame) null, title, true);
            this.url = url;
            this.cancelNeeded = cancelNeeded;
            initComponents();
        }

        public int getSelection() {
            return selection;
        }

        private void initComponents() throws IOException {
            JEditorPane pane = new JEditorPane(url);
            pane.setEditable(false);
            pane.addHyperlinkListener(new HyperlinkListener() {
                @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(e.getURL().toURI());
                            } catch (IOException e1) {
                            } catch (URISyntaxException e1) {
                            }
                        }
                    }
                }
            });
            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(new JScrollPane(pane), BorderLayout.CENTER);
            contentPane.add(getButtonBar(), BorderLayout.SOUTH);
        }

        private Component getButtonBar() {
            ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            if (cancelNeeded) {
                cancel = new JButton("Cancel");
                cancel.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        selection = JOptionPane.CANCEL_OPTION;
                        dispose();
                    }
                });
            } else
                cancel = ok;
            if(cancelNeeded)
                return ButtonBarFactory.buildOKCancelBar(ok, cancel);
            return ButtonBarFactory.buildOKBar(ok);
        }

        @Override public JButton getOKButton() {
            return ok;
        }

        @Override public JButton getCloseButton() {
            return cancel;
        }

        public void centerScreen() {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
        }
    }
}