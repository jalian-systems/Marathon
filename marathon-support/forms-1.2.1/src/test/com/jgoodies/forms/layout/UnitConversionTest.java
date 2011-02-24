/*
 * Copyright (c) 2002-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.forms.layout;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;

import junit.framework.TestCase;

import com.jgoodies.forms.util.DefaultUnitConverter;

/**
 * Tests the unit conversion used by the Forms layout system.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.10 $
 */
public final class UnitConversionTest extends TestCase {

    /**
     * Checks that users can set a custom font for use in
     * the DefaultUnitConverter.
     */
    public void testSetDefaultDialogFont() {
        DefaultUnitConverter duc = DefaultUnitConverter.getInstance();
        Font customFont = new Font("Serif", Font.PLAIN, 16);
        duc.setDefaultDialogFont(customFont);
    }


    /**
     * Checks that users can set a custom string for testing
     * the average character width in the DefaultUnitConverter.
     */
    public void testSetAverageCharacterWidthTestString() {
        DefaultUnitConverter duc = DefaultUnitConverter.getInstance();
        String customString = "Einen Vorsprung im Leben hat, " +
                              "wer da anpackt, " +
                              "wo die anderen erst einmal reden.";
        duc.setAverageCharacterWidthTestString(customString);
    }


    /**
     * Checks that a size with a value of 0 map to 0 px regardless
     * which unit has been specified.
     */
    public void testZeroSizesMapToZeroPixels() {
        Component component = new JLabel();
        assertEquals("Centimeter to pixel", 0, Sizes.centimeterAsPixel(0.0d, component));
        assertEquals("DluX to pixel",       0, Sizes.dialogUnitXAsPixel(0, component));
        assertEquals("DluY to pixel",       0, Sizes.dialogUnitYAsPixel(0, component));
        assertEquals("Inch to pixel",       0, Sizes.inchAsPixel      (0.0d, component));
        assertEquals("Millimeter to pixel", 0, Sizes.millimeterAsPixel(0.0d, component));
        assertEquals("Point to pixel",      0, Sizes.pointAsPixel     (0,    component));
    }


}
