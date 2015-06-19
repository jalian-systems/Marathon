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
package net.sourceforge.marathon.display;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;

import net.sourceforge.marathon.editor.IEditorProvider;

public abstract class MarathonAction implements IMarathonAction {

    private Icon enabledIcon;
    private Icon disabledIcon;
    private String name;
    private String description;
    private char mneumonic;
    private final IEditorProvider editorProvider;
    private final boolean toolbar;
    private final boolean menubar;
    private String menuName;
    private String accelKey;
    private char menuMnemonic;

    public MarathonAction(String name, String description, char mneumonic, Icon enabledIcon, Icon disabledIcon,
            IEditorProvider editorProvider, boolean toolbar, boolean menubar) {
        this.name = name;
        this.description = description;
        this.mneumonic = mneumonic;
        this.enabledIcon = enabledIcon;
        this.disabledIcon = disabledIcon;
        this.editorProvider = editorProvider;
        this.toolbar = toolbar;
        this.menubar = menubar;
    }

    public Icon getDisabledIcon() {
        return disabledIcon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Icon getEnabledIcon() {
        return enabledIcon;
    }

    public char getMneumonic() {
        return mneumonic;
    }

    public IEditorProvider getEditorProvider() {
        return editorProvider;
    }

    public boolean isToolBarAction() {
        return toolbar;
    }

    public boolean isMenuBarAction() {
        return menubar;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setAccelKey(String accelKey) {
        this.accelKey = accelKey;
    }

    public String getAccelKey() {
        return accelKey;
    }

    public boolean isSeperator() {
        return false;
    }

    public void setMenuMnemonic(char mnemonicChar) {
        this.menuMnemonic = mnemonicChar;
    }

    public char getMenuMnemonic() {
        return menuMnemonic;
    }

    public ButtonGroup getButtonGroup() {
        return null;
    }
    
    public boolean isSelected() {
        return false;
    }
    
    @Override public boolean isPopupMenu() {
        return false;
    }
    
    @Override public JMenu getPopupMenu() {
        return null;
    }
}
