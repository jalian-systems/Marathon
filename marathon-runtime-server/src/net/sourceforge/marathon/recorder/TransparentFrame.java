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
package net.sourceforge.marathon.recorder;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.component.MComponent;

public class TransparentFrame implements AWTEventListener {

    private MComponent component;
    private boolean disposed = false;
    private Graphics graphics;
    private static final Color BG = new Color(1.0f, 0.0f, 0.0f, 0.6f);

    public TransparentFrame(MComponent mcomponent) {
        this.component = mcomponent;
        graphics = component.getComponent().getGraphics().create();
    }

    public void setVisible(boolean b) {
        if (b) {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.PAINT_EVENT_MASK);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    paintTransparentFrame();
                }
            });
        } else {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        }
    }

    public void dispose() {
        disposed = true;
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                paintTransparentFrame();
            }
        });
    }

    public void eventDispatched(AWTEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                paintTransparentFrame();
            }
        });
    }

    protected void paintTransparentFrame() {
        Dimension size = component.getComponent().getSize();
        if (component.getComponent() instanceof JComponent) {
            JComponent jc = (JComponent) component.getComponent();
            jc.paintImmediately(0, 0, size.width, size.height);
        }
        if (disposed)
            return;
        size = component.getSize();
        Point location = component.getLocation();
        graphics.setColor(BG);
        graphics.fillRect(0 + location.x, 0 + location.y, size.width, size.height);
        String name = component.getMComponentName();
        graphics.setColor(Color.WHITE);
        Font font = new Font(graphics.getFont().getName(), Font.ITALIC | Font.BOLD, 12);
        FontMetrics metrics = graphics.getFontMetrics(font);
        int stringWidth = metrics.stringWidth(name);
        int stringHeight = metrics.getHeight() / 2;
        if (stringWidth < size.width && stringHeight < size.height) {
            graphics.setFont(font);
            graphics.drawString(name, (size.width - stringWidth) / 2 + location.x, (size.height + stringHeight) / 2 + location.y);
        } else if (stringWidth >= size.width || stringHeight >= size.height) {
            graphics.setFont(new Font(graphics.getFont().getName(), Font.ITALIC, 9));
            graphics.drawString(name, 0 + location.x, (size.height + stringHeight) / 2 + location.y);
        } else
            System.err.println("Not drawing");
    }
}
