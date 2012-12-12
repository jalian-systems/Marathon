package net.sourceforge.marathon.mpf;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DescriptionPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextArea textArea;

    public DescriptionPanel(String description) {
        initComponents(description);
    }

    private void initComponents(String description) {
        setLayout(new BorderLayout());
        textArea = new JTextArea(description);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }
}
