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
package net.sourceforge.marathon.action;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Captures a window state (size, maximising). Allows it to be restored later.
 */
public class WindowState implements Serializable {
    private static final Pattern pattern = Pattern.compile("(\\d+):(\\d+):(\\d+):(\\d+)");

    private static final long serialVersionUID = 1L;

    public Rectangle bounds = null;

    public WindowState(Window window) {

        // Capture the size, but only if the window is resizable.
        if (window instanceof Frame) {
            Frame w = (Frame) window;

            if (w.isUndecorated()) {
                // take this to mean: resizing and maximising isn't allowed
                // - this is to avoid a restore or resize operation on the
                // splash screen, for example
            } else {
                if (w.isResizable()) {
                    bounds = w.getBounds();
                }
            }
        } else if (window instanceof Dialog) {
            Dialog w = (Dialog) window;
            if (w.isUndecorated()) {
                // take this to mean: resizing and maximising isn't allowed
            } else {
                if (w.isResizable()) {
                    bounds = w.getBounds();
                }
                // No state to capture for dialogs - cannot maximise etc.
            }
        }
    }

    public WindowState(String state) {
        if (state.equals(""))
            return;
        Matcher matcher = pattern.matcher(state);
        if (!matcher.matches())
            return;
        bounds = new Rectangle(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher
                .group(3)), Integer.parseInt(matcher.group(4)));
    }

    /** Returns true if the script will record nothing - nothing to restore */
    public boolean isEmpty() {
        return bounds == null;
    }

    public String toString() {
        if (bounds != null)
            return bounds.x + ":" + bounds.y + ":" + bounds.width + ":" + bounds.height;
        return "";
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
