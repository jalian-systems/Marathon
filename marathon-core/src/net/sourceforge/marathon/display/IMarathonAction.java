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

import net.sourceforge.marathon.api.IScriptModelClientPart;

public interface IMarathonAction {

    Icon getDisabledIcon();

    String getName();

    String getDescription();

    Icon getEnabledIcon();

    char getMneumonic();

    void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script, int beginCaretPostion,
            int endCaretPosition, int startLine) throws Exception;

    boolean isToolBarAction();

    boolean isMenuBarAction();

    String getMenuName();

    String getAccelKey();

    boolean isSeperator();

    char getMenuMnemonic();

    ButtonGroup getButtonGroup();
    
    boolean isSelected();

    boolean isPopupMenu();

    JMenu getPopupMenu();
}
