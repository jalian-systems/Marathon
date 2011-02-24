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

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

public class DirectoryFileRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private ImageIcon dirIcon = new ImageIcon(getClass().getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/dir_obj.gif"));;
    private ImageIcon jarIcon = new ImageIcon(getClass().getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/jar_obj.gif"));;

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel comp = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!(value instanceof File))
            return comp;
        File file = (File) value;
        if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))
            comp.setIcon(jarIcon);
        else
            comp.setIcon(dirIcon);
        String fileName = file.getName();
        if (file.getParent() != null)
            fileName = fileName + " - " + file.getParent();
        comp.setText(fileName);
        return comp;
    }
}
