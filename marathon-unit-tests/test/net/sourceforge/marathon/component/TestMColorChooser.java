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

import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;

import javax.swing.JColorChooser;

public class TestMColorChooser {

    private MColorChooser colorChooser;
    private JColorChooser chooser;
    private Color choosenColor;

    @Before
    public void setUp() throws Exception {
        chooser = new JColorChooser();
        choosenColor = Color.RED;
        colorChooser = new MColorChooser(chooser, "ColorChooser", null, WindowMonitor.getInstance());
        chooser.setColor(choosenColor);
    }

    @After
    public void tearDown() throws Exception {
        colorChooser = null;
    }

    @Test
    public void testGetTextReturnsColorCodeForSelectedChooserColor() {
        String colorText = encode(chooser.getColor());
        assertEquals(colorText, colorChooser.getText());
    }

    private String encode(Color color) {
        return "#" + Integer.toHexString((color.getRGB() & 0x00FFFFFF) | 0x1000000).substring(1);
    }

    @Test
    public void testSetTextForColorChooser() {
        chooser.setColor(Color.WHITE);
        colorChooser.setText(encode(choosenColor));
        assertEquals(choosenColor, chooser.getColor());
    }

}
