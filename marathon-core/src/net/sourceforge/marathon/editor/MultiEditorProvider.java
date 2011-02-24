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
package net.sourceforge.marathon.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MultiEditorProvider implements IEditorProvider {
    public static final ImageIcon EMPTY_ICON = new ImageIcon(MultiEditorProvider.class.getResource("empty.gif"));

    private List<IEditorProvider> providers = new ArrayList<IEditorProvider>();
    private IEditorProvider defaultProvider;

    public IEditor get(boolean linenumbers, int startLineNumber, EditorType type) {
        if (type == IEditorProvider.EditorType.OTHER) {
            return defaultProvider.get(linenumbers, startLineNumber, type);
        }
        return findProvider(type).get(linenumbers, startLineNumber, type);
    }

    private IEditorProvider findProvider(EditorType type) {
        for (IEditorProvider provider : providers) {
            if (provider.supports(type))
                return provider;
        }
        return defaultProvider;
    }

    public boolean getTabConversion() {
        return defaultProvider.getTabConversion();
    }

    public int getTabSize() {
        return defaultProvider.getTabSize();
    }

    public boolean isEditorSettingsAvailable() {
        for (IEditorProvider provider : providers) {
            if (provider.isEditorSettingsAvailable())
                return true;
        }
        return false;
    }

    public boolean isEditorShortcutKeysAvailable() {
        for (IEditorProvider provider : providers) {
            if (provider.isEditorShortcutKeysAvailable())
                return true;
        }
        return false;
    }

    public JMenuItem getEditorSettingsMenuItem(JFrame parent) {
        List<JMenuItem> items = new ArrayList<JMenuItem>();
        for (IEditorProvider provider : providers) {
            if (provider.isEditorSettingsAvailable())
                items.add(provider.getEditorSettingsMenuItem(parent));
        }
        if (items.size() == 0)
            return null;
        if (items.size() == 1)
            return items.get(0);
        JMenu menu = new JMenu("Editor Settings");
        menu.setIcon(EMPTY_ICON);
        for (JMenuItem item : items) {
            menu.add(item);
        }
        return menu;
    }

    public JMenuItem getEditorShortcutMenuItem(JFrame parent) {
        List<JMenuItem> items = new ArrayList<JMenuItem>();
        for (IEditorProvider provider : providers) {
            if (provider.isEditorShortcutKeysAvailable())
                items.add(provider.getEditorShortcutMenuItem(parent));
        }
        if (items.size() == 0)
            return null;
        if (items.size() == 1)
            return items.get(0);
        JMenu menu = new JMenu("Editor Shortcuts");
        menu.setIcon(EMPTY_ICON);
        for (JMenuItem item : items) {
            menu.add(item);
        }
        return menu;
    }

    public boolean supports(EditorType type) {
        throw new UnsupportedOperationException("Multi editor provider can't support supports");
    }

    public void add(IEditorProvider provider, boolean b) {
        providers.add(provider);
        if (b)
            defaultProvider = provider;
    }

}
