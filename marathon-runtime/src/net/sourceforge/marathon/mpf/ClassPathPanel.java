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
package net.sourceforge.marathon.mpf;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import net.sourceforge.marathon.Constants;

public class ClassPathPanel extends ListPanel {
    public static final Icon ICON = new ImageIcon(ClassPathPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/cp_obj.gif"));;

    public ClassPathPanel(JDialog parent) {
        super(parent, true);
    }

    public Icon getIcon() {
        return ICON;
    }

    public String getName() {
        return "Class Path";
    }

    public String getPropertyKey() {
        return Constants.PROP_APPLICATION_PATH;
    }

    public boolean isAddArchivesNeeded() {
        return true;
    }

    public boolean isValidInput() {
        return true;
    }

    public boolean isAddFoldersNeeded() {
        return true;
    }

    public boolean isAddClassesNeeded() {
        return false;
    }
}
