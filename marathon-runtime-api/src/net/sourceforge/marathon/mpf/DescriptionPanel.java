package net.sourceforge.marathon.mpf;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
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
        textArea.setEditable(false);
        textArea.setBackground(Color.LIGHT_GRAY);
        add(textArea, BorderLayout.CENTER);
    }
}
