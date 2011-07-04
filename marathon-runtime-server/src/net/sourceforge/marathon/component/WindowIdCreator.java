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
package net.sourceforge.marathon.component;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;

import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class WindowIdCreator {

    private static List<Integer> windowList = new ArrayList<Integer>();

    public static WindowId createWindowId(Component window, WindowMonitor windowMonitor) {
        return new WindowId(getId(window), windowMonitor.getNamingStrategy().getName(window), getParentTitleForWindow(
                window, windowMonitor), window instanceof JInternalFrame);
    }

    private static int getId(Component w) {
        if (!(w instanceof Window))
            return 0;
        Integer window = Integer.valueOf(w.hashCode());
        if (!windowList.contains(window)) {
            windowList.add(window);
        }
        return windowList.indexOf(window);
    }

    private static String getParentTitleForWindow(Component window, WindowMonitor windowMonitor) {
        String parentTitle = null;
        List<Window> windows = windowMonitor.getAllWindows();
        int i;
        if (window instanceof Window) {
            if (window instanceof JDialog && ((JDialog)window).isModal()) {
                int index = windows.indexOf(window);
                if (index != -1 && index > 0) {
                    parentTitle = windowMonitor.getNamingStrategy().getName((Window) windows.get(index - 1));
                    return parentTitle ;
                }
            }
            Window owner = ((Window) window).getOwner();
            if (owner != null) {
                i = windows.indexOf(owner);
                if (i != -1)
                    parentTitle = windowMonitor.getNamingStrategy().getName((Window) windows.get(i));
                return parentTitle;
            }
            i = windows.indexOf(window);
            if (i == -1) {
                throw new Error("Window not found in openWindow list?");
            }
            if (i == 0) {
                return parentTitle;
            }
            parentTitle = windowMonitor.getNamingStrategy().getName((Window) windows.get(i - 1));
            return parentTitle;
        } else if (window instanceof JInternalFrame) {
            window = getIFParent(window);
            i = windows.indexOf(window);
            if (i == -1) {
                throw new Error("Window not found in openWindow list?");
            }
            parentTitle = windowMonitor.getNamingStrategy().getName((Window) windows.get(i));
            return parentTitle;
        }
        return parentTitle;
    }

    private static Component getIFParent(Component window) {
        Component parent = window;
        while (parent != null) {
            if (parent instanceof Window)
                return parent;
            parent = parent.getParent();
        }
        return null;
    }
}
