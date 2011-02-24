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

import java.util.Locale;

import junit.framework.TestCase;

import com.jgoodies.forms.factories.FormFactory;

/**
 * A test case for class {@link ColumnSpec}.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.32 $
 */
public final class ColumnSpecTest extends TestCase {


    /**
     * Checks that the constructor rejects negative resize weights.
     */
    public void testRejectNegativeResizeWeight() {
        try {
            new ColumnSpec(ColumnSpec.DEFAULT, Sizes.DEFAULT, -1);
            fail("The ColumnSpec constructor should reject negative resize weights.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The ColumnSpec constructor has thrown an unexpected exception.");
        }
    }


    /**
     * Checks that the constructor rejects negative resize weights.
     */
    public void testRejectParsedNegativeResizeWeight() {
        try {
            ColumnSpec.decode("right:default:-1");
            fail("The ColumnSpec parser constructor should reject negative resize weights.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The ColumnSpec constructor has thrown an unexpected exception.");
        }
    }


    /**
     * Tests the ColumnSpec parser on valid encodings with different Locales.
     */
    public void testValidColumnSpecEncodings() {
        testValidColumnSpecEncodings(Locale.ENGLISH);
        testValidColumnSpecEncodings(AllFormsTests.TURKISH);
    }


    /**
     * Tests with different Locales that the ColumnSpec parser
     * rejects invalid encodings.
     */
    public void testRejectInvalidColumnSpecEncodings() {
        testRejectInvalidColumnSpecEncodings(Locale.ENGLISH);
        testRejectInvalidColumnSpecEncodings(AllFormsTests.TURKISH);
    }


    public void testDefaultVariables() {
        assertEquals(
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("${label-component-gap}"));
        assertEquals(
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("${LABEL-COMPONENT-GAP}"));
        assertEquals(
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("$lcgap"));
        assertEquals(
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("$lcg"));

        assertEquals(
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("${related-gap}"));
        assertEquals(
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("$rgap"));
        assertEquals(
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("$rg"));

        assertEquals(
                FormFactory.UNRELATED_GAP_COLSPEC,
                ColumnSpec.decode("${unrelated-gap}"));
        assertEquals(
                FormFactory.UNRELATED_GAP_COLSPEC,
                ColumnSpec.decode("$ugap"));
        assertEquals(
                FormFactory.UNRELATED_GAP_COLSPEC,
                ColumnSpec.decode("$ug"));
    }


    public void testCustomVariable() {
        ColumnSpec labelColumnSpec = ColumnSpec.decode("left:[80dlu,pref]");
        LayoutMap layoutMap = new LayoutMap(null);
        layoutMap.columnPut("label", labelColumnSpec);
        assertEquals(
                labelColumnSpec,
                ColumnSpec.decode("$label", layoutMap));
    }


    public void testOverrideDefaultVariableWithDefaultName() {
        ConstantSize gapWidth = Sizes.DLUX1;
        ColumnSpec labelComponentColumnSpec = ColumnSpec.createGap(gapWidth);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.columnPut("label-component-gap", labelComponentColumnSpec);
        assertEquals(
                labelComponentColumnSpec,
                ColumnSpec.decode("${label-component-gap}", layoutMap));
        assertEquals(
                labelComponentColumnSpec,
                ColumnSpec.decode("$lcgap", layoutMap));
    }


    public void testOverrideDefaultVariableWithAlias() {
        ConstantSize gapWidth = Sizes.DLUX1;
        ColumnSpec labelComponentColumnSpec = ColumnSpec.createGap(gapWidth);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.columnPut("lcgap", labelComponentColumnSpec);
        assertEquals(
                labelComponentColumnSpec,
                ColumnSpec.decode("${label-component-gap}", layoutMap));
        assertEquals(
                labelComponentColumnSpec,
                ColumnSpec.decode("$lcgap", layoutMap));
    }


    public void testVariableExpression() {
        ColumnSpec spec0 = new ColumnSpec(ColumnSpec.LEFT_ALIGN, Sizes.PREFERRED, ColumnSpec.NO_GROW);
        ColumnSpec spec1 = ColumnSpec.createGap(Sizes.DLUX3);
        ColumnSpec spec2 = new ColumnSpec(Sizes.PREFERRED);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.columnPut("var1", "left:p, 3dlu, p");
        layoutMap.columnPut("var2", "$var1, 3dlu, $var1");
        ColumnSpec[] specs = ColumnSpec.decodeSpecs("$var1, 3dlu, $var1", layoutMap);
        ColumnSpec[] expected = new ColumnSpec[]{spec0, spec1, spec2, spec1, spec0, spec1, spec2};
        assertEquals(expected, specs);
        specs = ColumnSpec.decodeSpecs("$var2", layoutMap);
        assertEquals(expected, specs);
    }


    public void testPrototypeVariable() {
        Size size = new PrototypeSize("123-456-789");
        ColumnSpec spec = new ColumnSpec(size);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.columnPut("prototype", "'123-456-789'");
        assertEquals(spec, ColumnSpec.decode("$prototype", layoutMap));
    }


    public void testMissingVariable() {
        String variable = "$rumpelstilzchen";
        try {
            ColumnSpec.decode(variable);
            fail("The parser should reject the missing variable:" + variable);
        } catch (Exception e) {
            // The expected behavior
        }
    }


    public void testMultiplier()  {
        ColumnSpec prefSpec = ColumnSpec.decode("pref");
        ColumnSpec[] specs = ColumnSpec.decodeSpecs("2*(pref)");
        assertEquals(prefSpec, 2, specs);
    }


    public void testMultiplierWithBlanks() {
        ColumnSpec prefSpec = ColumnSpec.decode("pref");
        ColumnSpec[] specs = ColumnSpec.decodeSpecs("2* (pref)");
        assertEquals(prefSpec, 2, specs);

        specs = ColumnSpec.decodeSpecs("2 *(pref)");
        assertEquals(prefSpec, 2, specs);

        specs = ColumnSpec.decodeSpecs("2 * (pref)");
        assertEquals(prefSpec, 2, specs);

        specs = ColumnSpec.decodeSpecs(" 2 * (pref) ");
        assertEquals(prefSpec, 2, specs);

        specs = ColumnSpec.decodeSpecs(" 2 * ( pref ) ");
        assertEquals(prefSpec, 2, specs);
    }


    /**
     * Tests the ColumnSpec parser on valid encodings for a given Locale.
     *
     * @param locale    the Locale used while parsing the strings
     */
    private void testValidColumnSpecEncodings(Locale locale) {
        Locale oldDefault = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            ColumnSpec spec;
            spec = new ColumnSpec(ColumnSpec.LEFT, Sizes.PREFERRED, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("l:p"));
            assertEquals(spec, ColumnSpec.decode("L:P"));
            assertEquals(spec, ColumnSpec.decode("left:p"));
            assertEquals(spec, ColumnSpec.decode("LEFT:P"));
            assertEquals(spec, ColumnSpec.decode("l:pref"));
            assertEquals(spec, ColumnSpec.decode("L:PREF"));
            assertEquals(spec, ColumnSpec.decode("left:pref"));
            assertEquals(spec, ColumnSpec.decode("LEFT:PREF"));

            spec = new ColumnSpec(ColumnSpec.DEFAULT, Sizes.MINIMUM, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("min"));
            assertEquals(spec, ColumnSpec.decode("MIN"));
            assertEquals(spec, ColumnSpec.decode("f:min"));
            assertEquals(spec, ColumnSpec.decode("fill:min"));
            assertEquals(spec, ColumnSpec.decode("FILL:MIN"));
            assertEquals(spec, ColumnSpec.decode("f:min:nogrow"));
            assertEquals(spec, ColumnSpec.decode("F:MIN:NOGROW"));
            assertEquals(spec, ColumnSpec.decode("fill:min:grow(0)"));

            spec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("d"));
            assertEquals(spec, ColumnSpec.decode("default"));
            assertEquals(spec, ColumnSpec.decode("DEFAULT"));
            assertEquals(spec, ColumnSpec.decode("f:default"));
            assertEquals(spec, ColumnSpec.decode("fill:default"));
            assertEquals(spec, ColumnSpec.decode("f:default:nogrow"));
            assertEquals(spec, ColumnSpec.decode("fill:default:grow(0)"));
            assertEquals(spec, ColumnSpec.decode("FILL:DEFAULT:GROW(0)"));

            spec = new ColumnSpec(ColumnSpec.RIGHT, Sizes.pixel(10), FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("r:10px"));
            assertEquals(spec, ColumnSpec.decode("right:10px"));
            assertEquals(spec, ColumnSpec.decode("right:10px:nogrow"));
            assertEquals(spec, ColumnSpec.decode("RIGHT:10PX:NOGROW"));
            assertEquals(spec, ColumnSpec.decode("right:10px:grow(0)"));
            assertEquals(spec, ColumnSpec.decode("right:10px:g(0)"));

            Size size = Sizes.bounded(Sizes.PREFERRED, Sizes.pixel(10), null);
            spec = new ColumnSpec(ColumnSpec.RIGHT, size, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("right:max(10px;pref)"));
            assertEquals(spec, ColumnSpec.decode("right:max(pref;10px)"));
            assertEquals(spec, ColumnSpec.decode("right:[10px,pref]"));
            assertEquals(spec, ColumnSpec.decode("right:[10px, pref]"));
            assertEquals(spec, ColumnSpec.decode("right:[10px ,pref]"));
            assertEquals(spec, ColumnSpec.decode("right:[ 10px , pref ]"));

            size = Sizes.bounded(Sizes.PREFERRED, null, Sizes.pixel(10));
            spec = new ColumnSpec(ColumnSpec.RIGHT, size, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("right:min(10px;pref)"));
            assertEquals(spec, ColumnSpec.decode("right:min(pref;10px)"));
            assertEquals(spec, ColumnSpec.decode("right:[pref,10px]"));

            size = Sizes.bounded(Sizes.DEFAULT, null, Sizes.pixel(10));
            spec = new ColumnSpec(ColumnSpec.DEFAULT, size, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("min(10px;default)"));
            assertEquals(spec, ColumnSpec.decode("MIN(10PX;DEFAULT)"));
            assertEquals(spec, ColumnSpec.decode("min(10px;d)"));
            assertEquals(spec, ColumnSpec.decode("min(default;10px)"));
            assertEquals(spec, ColumnSpec.decode("min(d;10px)"));
            assertEquals(spec, ColumnSpec.decode("[d,10px]"));

            size = Sizes.bounded(Sizes.PREFERRED, Sizes.pixel(50), Sizes.pixel(200));
            spec = new ColumnSpec(ColumnSpec.DEFAULT, size, FormSpec.NO_GROW);
            assertEquals(spec, ColumnSpec.decode("[50px,pref,200px]"));

            spec = new ColumnSpec(ColumnSpec.DEFAULT, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
            assertEquals(spec, ColumnSpec.decode("d:grow"));
            assertEquals(spec, ColumnSpec.decode("default:grow(1)"));
            assertEquals(spec, ColumnSpec.decode("f:d:g"));
            assertEquals(spec, ColumnSpec.decode("f:d:grow(1.0)"));
            assertEquals(spec, ColumnSpec.decode("f:d:g(1.0)"));

            spec = new ColumnSpec(ColumnSpec.DEFAULT, Sizes.DEFAULT, 0.75);
            assertEquals(spec, ColumnSpec.decode("d:grow(0.75)"));
            assertEquals(spec, ColumnSpec.decode("default:grow(0.75)"));
            assertEquals(spec, ColumnSpec.decode("f:d:grow(0.75)"));
            assertEquals(spec, ColumnSpec.decode("fill:default:grow(0.75)"));
            assertEquals(spec, ColumnSpec.decode("FILL:DEFAULT:GROW(0.75)"));

            spec = ColumnSpec.decode("fill:10in");
            assertEquals(spec, ColumnSpec.decode("FILL:10IN"));

            spec = new ColumnSpec(new PrototypeSize("prototype"));
            assertEquals(spec, ColumnSpec.decode("'prototype'"));

            spec = new ColumnSpec(new PrototypeSize("prototype string"));
            assertEquals(spec, ColumnSpec.decode("'prototype string'"));

            ColumnSpec spec1 = new ColumnSpec(ColumnSpec.LEFT, Sizes.PREFERRED, FormSpec.NO_GROW);
            ColumnSpec spec2 = new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, 1.0);
            ColumnSpec[] specs = ColumnSpec.decodeSpecs(
                    "left:pref:none , right:default:grow");
            assertEquals(2, specs.length);
            assertEquals(spec1, specs[0]);
            assertEquals(spec2, specs[1]);
        } finally {
            Locale.setDefault(oldDefault);
        }
    }


    /**
     * Tests that the ColumnSpec parser rejects invalid encodings for a given Locale.
     *
     * @param locale    the Locale used while parsing the strings
     */
    private void testRejectInvalidColumnSpecEncodings(Locale locale) {
        Locale oldDefault = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            assertRejects("karsten");
            assertRejects("d:a:b:");
            assertRejects("top:default:grow");
            assertRejects("bottom:10px");
            assertRejects("max(10px;20px)");
            assertRejects("min(10px;20px)");
            assertRejects("[10px,20px]");
            assertRejects("max(pref;pref)");
            assertRejects("min(pref;pref)");
            assertRejects("[pref,pref]");
            assertRejects("[pref,pref,200px]");  // lower bound must be constant
            assertRejects("[10px,50px,200px]");  // basis must be logical
            assertRejects("[10px,pref,pref]");   // upper bound must be constant

            assertRejectsSpecs("7dlu l:p:g 7dlu");    // Missing commas
        } finally {
            Locale.setDefault(oldDefault);
        }
    }


    // Helper Code ***********************************************************

    /**
     * Checks if the given ColumnSpec instances are equal and throws a failure
     * if not.
     *
     * @param expected  the expected spec object to be compared
     * @param actual    the actual spec object to be compared
     */
    private void assertEquals(ColumnSpec expected, ColumnSpec actual) {
        if (!expected.getDefaultAlignment().equals(actual.getDefaultAlignment())) {
            fail("Alignment mismatch: expected=" + expected + "; actual=" + actual);
        }
        if (!expected.getSize().equals(actual.getSize())) {
            fail("Size mismatch: expected=" + expected + "; actual=" + actual);
        }
        if (!(expected.getResizeWeight() == actual.getResizeWeight())) {
            fail("Resize weight mismatch: expected=" + expected + "; actual=" + actual);
        }
    }


    private void assertEquals(ColumnSpec[] specs1, ColumnSpec[] specs2) {
        if (specs1.length != specs2.length) {
            fail("Array size mismatch. specs1.length" + specs1.length + "; specs2.length=" + specs2.length);
        }
        for (int i = 0; i < specs1.length; i++) {
            assertEquals(specs1[i], specs2[i]);
        }
    }


    private void assertEquals(ColumnSpec expectedSpec, int expectedLength, ColumnSpec[] specs) {
        assertEquals("Multiplier", expectedLength, specs.length);
        for (int i = 0; i < specs.length; i++) {
            assertEquals("ColumnSpec[" + i + "]",
                    expectedSpec,
                    specs[i]);
        }
    }


    /**
     * Asserts that the specified column spec encoding is rejected.
     *
     * @param invalidEncoding  the invalid encoded column spec
     */
    private void assertRejects(String invalidEncoding) {
        try {
            ColumnSpec.decode(invalidEncoding);
            fail("The parser should reject the invalid encoding:" + invalidEncoding);
        } catch (Exception e) {
            // The expected behavior
        }
    }


    /**
     * Asserts that the specified column spec encodings are rejected.
     *
     * @param invalidEncodings  the invalid encoded column specs
     */
    private void assertRejectsSpecs(String invalidEncodings) {
        try {
            ColumnSpec.decodeSpecs(invalidEncodings);
            fail("The parser should reject the invalid encodings:" + invalidEncodings);
        } catch (Exception e) {
            // The expected behavior
        }
    }


}
