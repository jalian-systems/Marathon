package net.sourceforge.marathon.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.editor.IEditorProvider.EditorType;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder2;

public class MessageDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private final String message;
    private JButton closeButton;
    private IEditorProvider editorProvider;

    public MessageDialog(String message, String title, IEditorProvider editorProvider) {
        super((JFrame) null, title, true);
        this.message = message;
        this.editorProvider = editorProvider;
        initialize();
    }

    private void initialize() {
        getContentPane().setLayout(new BorderLayout());
        closeButton = UIUtils.createCloseButton();
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.addButton(closeButton);
        getContentPane().add(builder.getPanel(), BorderLayout.SOUTH);
        IEditor editor = editorProvider.get(true, 1, EditorType.OTHER);
        editor.setText(message);
        editor.setEditable(false);
        getContentPane().add(new JScrollPane(editor.getComponent()));
        getContentPane().setPreferredSize(new Dimension(640, 480));
        pack();
        setLocationRelativeTo(null);
    }

    @Override public JButton getOKButton() {
        return closeButton;
    }

    @Override public JButton getCloseButton() {
        return closeButton;
    }
}