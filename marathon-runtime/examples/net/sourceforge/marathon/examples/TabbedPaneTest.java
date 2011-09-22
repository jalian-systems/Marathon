package net.sourceforge.marathon.examples;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.DefaultFormBuilder;
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
        DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow"));
        if (i == 1) {
            b.append("Name " + i, new JTextField(40));
            b.append("Password " + i, new JTextField(30));
        } else {
            b.append("Name " + i, new JComboBox(new String[] {  "Name 1", "Name 2", "Name 3"}));
            b.append("Password " + i, new JComboBox(new String[] { "Password 1", "Password 2"}));
        }
        return b.getPanel();
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
