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
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.sourceforge.marathon.util.EscapeDialog;

public abstract class Blurb {
    private URL url;
    private String title;

    public Blurb(String marker, String title) {
        this.url = getClass().getResource(marker + ".html");
        this.title = title;
        showMessage();
    }

    public void showMessage() {
        try {
            final BlurbDialog dialog;
            dialog = new BlurbDialog(url, title);
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    dialog.setPreferredSize(new Dimension(640, 480));
                    dialog.setLocationRelativeTo(null);
                    dialog.pack();
                    dialog.centerScreen();
                    dialog.setVisible(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class BlurbDialog extends EscapeDialog {
        private static final long serialVersionUID = 1L;
        private URL url;
        private JButton ok;

        public BlurbDialog(URL url, String title) throws IOException {
            super((Frame) null, title, true);
            this.url = url;
            initComponents();
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
            JPanel panel = new JPanel(new BorderLayout());
            ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            panel.add(ok, BorderLayout.EAST);
            return panel;
        }

        @Override public JButton getOKButton() {
            return ok;
        }

        @Override public JButton getCloseButton() {
            return ok;
        }

        public void centerScreen() {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
        }
    }
}