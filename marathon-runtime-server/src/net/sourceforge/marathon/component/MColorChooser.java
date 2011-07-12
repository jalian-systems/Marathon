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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JColorChooser;

import net.sourceforge.marathon.recorder.WindowMonitor;

public class MColorChooser extends MComponent {

    private JColorChooser chooser;

    public MColorChooser(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
        chooser = (JColorChooser) component;
    }

    public String getText() {
        return getColorCode((Color) eventQueueRunner.invoke(chooser, "getColor"));
    }

    private String getColorCode(Color color) {
        return "#" + Integer.toHexString((color.getRGB() & 0x00FFFFFF) | 0x1000000).substring(1);
    }

    public void setText(String text) {
        if (!text.equals("")) {
            try {
                Color color = Color.decode(text);
                eventQueueRunner.invoke(chooser, "setColor", new Object[] { color }, new Class[] { Color.class });
            } catch (Throwable t) {
                // Might throw a number format exception
                return;
            }
        }
    }

    public boolean recordAlways() {
        return true;
    }

}
