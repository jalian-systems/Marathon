/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.editor.rsta;

import java.awt.event.ActionEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.util.PropertyEditor;

public class RSTAEditorProvider implements IEditorProvider, PreferenceChangeListener {

    private boolean tabConversion;
    private int tabSize;

    public RSTAEditorProvider() {
        readPreferences();
        installPreferenceListener();

    }

    private void installPreferenceListener() {
        Preferences preferences = Preferences.userNodeForPackage(IEditor.class);
        preferences.addPreferenceChangeListener(this);
    }

    private void readPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(IEditorProvider.class);
        String prop = preferences.get("editor.tabconversion", "true");
        tabConversion = Boolean.parseBoolean(prop);
        prop = preferences.get("editor.tabsize", "4");
        tabSize = Integer.parseInt(prop);
    }

    public boolean getTabConversion() {
        return tabConversion;
    }

    public int getTabSize() {
        return tabSize;
    }

    public void changeEditorSettings(JFrame parent) {
        PropertyEditor ped = new PropertyEditor(parent, RSTAEditorProvider.class,
                RSTAEditorProvider.class.getResource("rsyntaxtextarea.props"));
        ped.setVisible(true);
    }

    public void changeShortcuts(JFrame parent) {
        JOptionPane.showMessageDialog(parent, "Not yet implemented...");
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        String key = evt.getKey();
        String value = evt.getNewValue();
        if ("editor.tabconversion".equals(key))
            tabConversion = Boolean.parseBoolean(value);
        else if ("editor.tabsize".equals(key))
            tabSize = Integer.parseInt(value);
    }

    public boolean isEditorSettingsAvailable() {
        return true;
    }

    public boolean isEditorShortcutKeysAvailable() {
        return true;
    }

    public IEditor get(boolean linenumbers, int startLineNumber, EditorType type) {
        RSTAEditor rstaEditor = new RSTAEditor(linenumbers, startLineNumber);
        rstaEditor.setTabsEmulated(getTabConversion());
        rstaEditor.setTabSize(getTabSize());
        return rstaEditor;
    }

    public JMenuItem getEditorSettingsMenuItem(final JFrame parent) {
        return new JMenuItem(new AbstractAction("Marathon Editor Settings...", RSTAEditor.EMPTY_ICON) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                changeEditorSettings(parent);
            }
        });
    }

    public JMenuItem getEditorShortcutMenuItem(final JFrame parent) {
        return new JMenuItem(new AbstractAction("Marathon Editor Shorcuts...", RSTAEditor.EMPTY_ICON) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                changeShortcuts(parent);
            }
        });
    }

    public boolean supports(EditorType type) {
        if (type == EditorType.CSV)
            return false;
        return true;
    }

}
