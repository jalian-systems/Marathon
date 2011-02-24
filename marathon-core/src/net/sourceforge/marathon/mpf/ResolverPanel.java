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

import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import net.sourceforge.marathon.Constants;

public class ResolverPanel extends ListPanel {
    public ResolverPanel(JDialog parent) {
        super(parent);
    }

    public static final Icon _icon = new ImageIcon(ResolverPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/cp_obj.gif"));

    public boolean isAddArchivesNeeded() {
        return false;
    }

    public boolean isAddClassesNeeded() {
        return true;
    }

    public boolean isAddFoldersNeeded() {
        return false;
    }

    public String getPropertyKey() {
        return Constants.PROP_COMPONENT_RESOLVERS;
    }

    public String getName() {
        return "Resolvers";
    }

    public Icon getIcon() {
        return _icon;
    }

    public boolean isValidInput() {
        return true;
    }

    protected String getClassPath() {
        StringBuffer cp = new StringBuffer("");
        int size = classpathListModel.getSize();
        if (size == 0)
            return cp.toString();
        for (int i = 0; i < size - 1; i++) {
            cp.append(classpathListModel.getElementAt(i));
            cp.append(";");
        }
        cp.append(classpathListModel.getElementAt(size - 1));
        return cp.toString();
    }

    public void setProperties(Properties props) {
        String cp = props.getProperty(getPropertyKey(), "");
        if (cp.length() == 0)
            return;
        String[] elements = cp.split(";");
        for (int i = 0; i < elements.length; i++) {
            classpathListModel.add(elements[i]);
        }
    }
}
