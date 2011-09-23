package net.sourceforge.marathon.examples;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TabbedPaneTest extends JFrame {

    private static final long serialVersionUID = 1L;

    public TabbedPaneTest() {
        setTitle(this.getClass().getSimpleName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        init();
    }

    private void init() {
        JTabbedPane pane = new JTabbedPane();
        pane.addTab("Tab1", createTab(1));
        pane.addTab("Tab2", createTab(2));
        add(pane);
        pack();
    }

    private Component createTab(int i) {
        JPanel panel = new JPanel();
        FormLayout formLayout = new FormLayout("pref, 3dlu, pref:grow", "pref, 3dlu, pref, 3dlu");
        panel.setLayout(formLayout);
        CellConstraints cc = new CellConstraints();
        JTextField tf = new JTextField(40);
        panel.add(tf, cc.xy(3, 1));
        if (i == 1)
            tf.setName("TF 1");
        panel.add(new JTextField(40), cc.xy(3, 3));
        
        return panel;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TabbedPaneTest f = new TabbedPaneTest();
                f.setVisible(true);
            }
        });
    }

}
