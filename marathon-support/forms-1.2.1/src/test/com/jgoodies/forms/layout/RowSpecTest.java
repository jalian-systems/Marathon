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
 * A test case for class {@link RowSpec}.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.26 $
 */
public final class RowSpecTest extends TestCase {


    /**
     * Checks that the constructor rejects negative resize weights.
     */
    public void testRejectNegativeResizeWeight() {
        try {
            new RowSpec(RowSpec.DEFAULT, Sizes.DEFAULT, -1);
            fail("The RowSpec constructor should reject negative resize weights.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The RowSpec constructor has thrown an unexpected exception.");
        }
    }


    /**
     * Checks that the constructor rejects negative resize weights.
     */
    public void testRejectParsedNegativeResizeWeight() {
        try {
            RowSpec.decode("right:default:-1");
            fail("The RowSpec parser constructor should reject negative resize weights.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The RowSpec constructor has thrown an unexpected exception.");
        }
    }


    /**
     * Tests the RowSpec parser on valid encodings with different locales.
     */
    public void testValidRowSpecEncodings() {
        testValidRowSpecEncodings(Locale.ENGLISH);
        testValidRowSpecEncodings(AllFormsTests.TURKISH);
    }


    /**
     * Tests that the RowSpec parser rejects invalid encodings for a given Locale.
     */
    public void testRejectInvalidRowSpecEncodings() {
        testRejectInvalidRowSpecEncodings(Locale.ENGLISH);
        testRejectInvalidRowSpecEncodings(AllFormsTests.TURKISH);
    }


    public void testDefaultVariables() {
        assertEquals(
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("${related-gap}"));
        assertEquals(
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("${RELATED-GAP}"));
        assertEquals(
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("$rgap"));
        assertEquals(
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("$rg"));

        assertEquals(
                FormFactory.UNRELATED_GAP_ROWSPEC,
                RowSpec.decode("${unrelated-gap}"));
        assertEquals(
                FormFactory.UNRELATED_GAP_ROWSPEC,
                RowSpec.decode("$ugap"));
        assertEquals(
                FormFactory.UNRELATED_GAP_ROWSPEC,
                RowSpec.decode("$ug"));

        assertEquals(
                FormFactory.NARROW_LINE_GAP_ROWSPEC,
                RowSpec.decode("${narrow-line-gap}"));
        assertEquals(
                FormFactory.NARROW_LINE_GAP_ROWSPEC,
                RowSpec.decode("$nlgap"));
        assertEquals(
                FormFactory.NARROW_LINE_GAP_ROWSPEC,
                RowSpec.decode("$nlg"));

        assertEquals(
                FormFactory.LINE_GAP_ROWSPEC,
                RowSpec.decode("${line-gap}"));
        assertEquals(
                FormFactory.LINE_GAP_ROWSPEC,
                RowSpec.decode("$lgap"));
        assertEquals(
                FormFactory.LINE_GAP_ROWSPEC,
                RowSpec.decode("$lg"));

        assertEquals(
                FormFactory.PARAGRAPH_GAP_ROWSPEC,
                RowSpec.decode("${paragraph-gap}"));
        assertEquals(
                FormFactory.PARAGRAPH_GAP_ROWSPEC,
                RowSpec.decode("$pgap"));
    }


    public void testCustomVariable() {
        ConstantSize gapHeight = Sizes.DLUY21;
        RowSpec largeGap = RowSpec.createGap(gapHeight);
        LayoutMap layoutMap = new LayoutMap(null);
        layoutMap.rowPut("large", largeGap);
        assertEquals(
                largeGap,
                RowSpec.decode("$large", layoutMap));
    }


    public void testOverrideDefaultVariableWithDefaultName() {
        ConstantSize gapHeight = Sizes.DLUY1;
        RowSpec lineSpec = RowSpec.createGap(gapHeight);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.rowPut("line-gap", lineSpec);
        assertEquals(
                lineSpec,
                RowSpec.decode("${line-gap}", layoutMap));
        assertEquals(
                lineSpec,
                RowSpec.decode("$lgap", layoutMap));
    }


    public void testOverrideDefaultVariableWithAlias() {
        ConstantSize gapHeight = Sizes.DLUY1;
        RowSpec lineSpec = RowSpec.createGap(gapHeight);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.rowPut("lgap", lineSpec);
        assertEquals(
                lineSpec,
                RowSpec.decode("${line-gap}", layoutMap));
        assertEquals(
                lineSpec,
                RowSpec.decode("$lgap", layoutMap));
    }


    public void testVariableExpression() {
        RowSpec spec0 = new RowSpec(RowSpec.TOP_ALIGN, Sizes.PREFERRED, RowSpec.NO_GROW);
        RowSpec spec1 = RowSpec.createGap(Sizes.DLUY3);
        RowSpec spec2 = new RowSpec(Sizes.PREFERRED);
        LayoutMap layoutMap = new LayoutMap();
        layoutMap.rowPut("var1", "top:p, 3dlu, p");
        layoutMap.rowPut("var2", "$var1, 3dlu, $var1");
        RowSpec[] specs = RowSpec.decodeSpecs("$var1, 3dlu, $var1", layoutMap);
        RowSpec[] expected = new RowSpec[]{spec0, spec1, spec2, spec1, spec0, spec1, spec2};
        assertEquals(expected, specs);
        specs = RowSpec.decodeSpecs("$var2", layoutMap);
        assertEquals(expected, specs);
    }


    public void testMissingColumnSpecVariable() {
        String variable = "$rumpelstilzchen";
        try {
            RowSpec.decode(variable);
            fail("The parser should reject the missing variable:" + variable);
        } catch (Exception e) {
            // The expected behavior
        }
    }


    /**
     * Tests the RowSpec parser on valid encodings for a given locale.
     *
     * @param locale    the Locale used while parsing the strings
     */
    private void testValidRowSpecEncodings(Locale locale) {
        Locale oldDefault = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            RowSpec spec;
            spec = new RowSpec(RowSpec.TOP, Sizes.PREFERRED, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("t:p"));
            assertEquals(spec, RowSpec.decode("top:p"));
            assertEquals(spec, RowSpec.decode("t:pref"));
            assertEquals(spec, RowSpec.decode("top:pref"));

            spec = new RowSpec(RowSpec.DEFAULT, Sizes.MINIMUM, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("min"));
            assertEquals(spec, RowSpec.decode("c:min"));
            assertEquals(spec, RowSpec.decode("center:min"));
            assertEquals(spec, RowSpec.decode("c:min:none"));
            assertEquals(spec, RowSpec.decode("center:min:grow(0)"));

            spec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("f:default"));
            assertEquals(spec, RowSpec.decode("fill:default"));
            assertEquals(spec, RowSpec.decode("FILL:DEFAULT"));
            assertEquals(spec, RowSpec.decode("f:default:none"));
            assertEquals(spec, RowSpec.decode("F:DEFAULT:NONE"));
            assertEquals(spec, RowSpec.decode("fill:default:grow(0)"));
            assertEquals(spec, RowSpec.decode("FILL:DEFAULT:GROW(0)"));

            spec = new RowSpec(RowSpec.BOTTOM, Sizes.pixel(10), FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("b:10px"));
            assertEquals(spec, RowSpec.decode("bottom:10px"));
            assertEquals(spec, RowSpec.decode("BOTTOM:10PX"));
            assertEquals(spec, RowSpec.decode("bottom:10px:none"));
            assertEquals(spec, RowSpec.decode("bottom:10px:grow(0)"));
            assertEquals(spec, RowSpec.decode("bottom:10px:g(0)"));

            Size size = Sizes.bounded(Sizes.PREFERRED, Sizes.pixel(10), null);
            spec = new RowSpec(RowSpec.BOTTOM, size, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("bottom:max(10px;pref)"));
            assertEquals(spec, RowSpec.decode("bottom:max(pref;10px)"));
            assertEquals(spec, RowSpec.decode("bottom:[10px,pref]"));

            size = Sizes.bounded(Sizes.PREFERRED, null, Sizes.pixel(10));
            spec = new RowSpec(RowSpec.BOTTOM, size, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("bottom:min(10px;pref)"));
            assertEquals(spec, RowSpec.decode("BOTTOM:MIN(10PX;PREF)"));
            assertEquals(spec, RowSpec.decode("bottom:min(pref;10px)"));
            assertEquals(spec, RowSpec.decode("bottom:[pref,10px]"));

            size = Sizes.bounded(Sizes.DEFAULT, null, Sizes.pixel(10));
            spec = new RowSpec(RowSpec.DEFAULT, size, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("min(10px;default)"));
            assertEquals(spec, RowSpec.decode("min(10px;d)"));
            assertEquals(spec, RowSpec.decode("min(default;10px)"));
            assertEquals(spec, RowSpec.decode("min(d;10px)"));
            assertEquals(spec, RowSpec.decode("[d,10px]"));

            size = Sizes.bounded(Sizes.PREFERRED, Sizes.pixel(50), Sizes.pixel(200));
            spec = new RowSpec(RowSpec.DEFAULT, size, FormSpec.NO_GROW);
            assertEquals(spec, RowSpec.decode("[50px,pref,200px]"));

            spec = new RowSpec(RowSpec.DEFAULT, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
            assertEquals(spec, RowSpec.decode("d:grow"));
            assertEquals(spec, RowSpec.decode("default:grow(1)"));
            assertEquals(spec, RowSpec.decode("c:d:g"));
            assertEquals(spec, RowSpec.decode("c:d:grow(1.0)"));
            assertEquals(spec, RowSpec.decode("c:d:g(1.0)"));

            spec = new RowSpec(RowSpec.DEFAULT, Sizes.DEFAULT, 0.75);
            assertEquals(spec, RowSpec.decode("d:grow(0.75)"));
            assertEquals(spec, RowSpec.decode("default:grow(0.75)"));
            assertEquals(spec, RowSpec.decode("c:d:grow(0.75)"));
            assertEquals(spec, RowSpec.decode("center:default:grow(0.75)"));

            RowSpec spec1 = new RowSpec(RowSpec.TOP, Sizes.PREFERRED, FormSpec.NO_GROW);
            RowSpec spec2 = new RowSpec(RowSpec.BOTTOM, Sizes.DEFAULT, 1.0);
            RowSpec[] specs = RowSpec.decodeSpecs(
                    "top:pref:none , bottom:default:grow");
            assertEquals(2, specs.length);
            assertEquals(spec1, specs[0]);
            assertEquals(spec2, specs[1]);
        } finally {
            Locale.setDefault(oldDefault);
        }
    }


    /**
     * Tests that the RowSpec parser rejects invalid encodings for a given Locale.
     *
     * @param locale    the Locale used while parsing the strings
     */
    private void testRejectInvalidRowSpecEncodings(Locale locale) {
        Locale oldDefault = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            assertRejects("karsten");
            assertRejects("d:a:b:");
            assertRejects("right:default:grow"); // invalid alignment
            assertRejects("left:20dlu");
            assertRejects("max(10px;20px)");
            assertRejects("min(10px;20px)");
            assertRejects("[10px,20px]");
            assertRejects("max(pref;pref)");
            assertRejects("min(pref;pref)");
            assertRejects("[pref,pref]");
            assertRejects("[pref,pref,200px]");  // lower bound must be constant
            assertRejects("[10px,50px,200px]");  // basis must be logical
            assertRejects("[10px,pref,pref]");   // upper bound must be constant
        } finally {
            Locale.setDefault(oldDefault);
        }
    }


    // Helper Code ***********************************************************

    /**
     * Checks if the given RowSpec instances are equal and throws a failure
     * if not.
     *
     * @param expected  the expected row spec object to be compared
     * @param actual    the actual row spec object to be compared
     */
    private void assertEquals(RowSpec expected, RowSpec actual) {
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

    private void assertEquals(RowSpec[] specs1, RowSpec[] specs2) {
        if (specs1.length != specs2.length) {
            fail("Array size mismatch. specs1.length" + specs1.length + "; specs2.length=" + specs2.length);
        }
        for (int i = 0; i < specs1.length; i++) {
            assertEquals(specs1[i], specs2[i]);
        }
    }


    /**
     * Asserts that the specified row spec encoding is rejected.
     *
     * @param encodedRowSpec  an encoded row spec
     */
    private void assertRejects(String encodedRowSpec) {
        try {
            RowSpec.decode(encodedRowSpec);
            fail("The parser should reject encoding:" + encodedRowSpec);
        } catch (Exception e) {
            // The expected behavior
        }
    }

}
