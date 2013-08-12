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
package net.sourceforge.marathon.navigator;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {
    private static String ICON_PATH = "net/sourceforge/marathon/navigator/icons/";
    public static final Icon NAVIGATOR = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "navigator.gif"));
    public static final Icon COPY_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "enabled/copy.gif"));
    public static final Icon DELETE_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "enabled/delete.gif"));
    public static final Icon NEWFILE_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "enabled/newfile.gif"));
    public static final Icon NEWFOLDER_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "enabled/newfolder.gif"));
    public static final Icon PASTE_ENABLED = new ImageIcon(Icons.class.getClassLoader()
            .getResource(ICON_PATH + "enabled/paste.gif"));
    public static final Icon REFRESH_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "enabled/refresh.gif"));
    public static final Icon GOUP_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "enabled/goup.gif"));
    public static final Icon GOINTO_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "enabled/gointo.gif"));
    public static final Icon HOME_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(ICON_PATH + "enabled/home.gif"));
    public static final Icon COLLAPSEALL_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "enabled/collapseall.gif"));
    public static final Icon COPY_DISABLED = new ImageIcon(Icons.class.getClassLoader()
            .getResource(ICON_PATH + "disabled/copy.gif"));
    public static final Icon DELETE_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/delete.gif"));
    public static final Icon NEWFILE_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/newfile.gif"));
    public static final Icon NEWFOLDER_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/newfolder.gif"));
    public static final Icon PASTE_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/paste.gif"));
    public static final Icon REFRESH_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/refresh.gif"));
    public static final Icon GOUP_DISABLED = new ImageIcon(Icons.class.getClassLoader()
            .getResource(ICON_PATH + "disabled/goup.gif"));
    public static final Icon GOINTO_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/gointo.gif"));
    public static final Icon HOME_DISABLED = new ImageIcon(Icons.class.getClassLoader()
            .getResource(ICON_PATH + "disabled/home.gif"));
    public static final Icon COLLAPSEALL_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "disabled/collapseall.gif"));
    public static final Icon PROPERTIES_ENABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "properties.gif"));;
    public static final Icon PROPERIES_DISABLED = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "properties.gif"));
    public static final Icon INFO = new ImageIcon(Icons.class.getClassLoader().getResource(
            ICON_PATH + "info.gif"));
}
