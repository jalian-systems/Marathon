package net.sourceforge.marathon.suite.editor;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.IEditorProvider;

public class SuiteEditorProvider implements IEditorProvider {

    public boolean getTabConversion() {
        return false;
    }

    public int getTabSize() {
        return 0;
    }

    public boolean isEditorSettingsAvailable() {
        return false;
    }

    public boolean isEditorShortcutKeysAvailable() {
        return false;
    }

    public JMenuItem getEditorSettingsMenuItem(JFrame parent) {
        // TODO Auto-generated method stub
        return null;
    }

    public JMenuItem getEditorShortcutMenuItem(JFrame parent) {
        // TODO Auto-generated method stub
        return null;
    }

    public IEditor get(boolean linenumbers, int startLineNumber, EditorType type) {
        return new SuiteEditor();
    }

    public boolean supports(EditorType type) {
        return type == EditorType.SUITE;
    }

}
