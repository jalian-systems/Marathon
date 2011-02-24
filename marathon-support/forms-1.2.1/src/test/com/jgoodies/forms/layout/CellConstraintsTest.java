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

/**
 * A test case for class {@link CellConstraints}.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.16 $
 */
public final class CellConstraintsTest extends TestCase {

    /**
     * Checks that the constructor rejects non-positive origin and extent.
     */
    public void testRejectNonPositiveOriginAndExtent() {
        assertRejects( 0,  1,  1,  1);
        assertRejects(-1,  1,  1,  1);
        assertRejects( 1,  0,  1,  1);
        assertRejects( 1, -1,  1,  1);
        assertRejects( 1,  1,  0,  1);
        assertRejects( 1,  1, -1,  1);
        assertRejects( 1,  1,  1,  0);
        assertRejects( 1,  1,  1, -1);
    }


    /**
     * Tests that the CellConstraints parser rejects invalid alignments.
     */
    public void testRejectInvalidCellConstraintsAlignments() {
        try {
            new CellConstraints(1, 1, CellConstraints.BOTTOM, CellConstraints.CENTER);
            fail("The CellConstraints constructor should reject invalid orientations.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The constructor has thrown an unexpected exception: " + e);
        }
        try {
            new CellConstraints(1, 1, CellConstraints.CENTER, CellConstraints.RIGHT);
            fail("The CellConstraints constructor should reject invalid orientations.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The constructor has thrown an unexpected exception: " + e);
        }
        CellConstraints cc = new CellConstraints();
        try {
            cc.xy(1, 1, CellConstraints.BOTTOM, CellConstraints.CENTER);
            fail("The CellConstraints setter should reject invalid orientations.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The setter has thrown an unexpected exception: " + e);
        }
        try {
            cc.xy(1, 1, CellConstraints.BOTTOM, CellConstraints.CENTER);
            fail("The CellConstraints setter should reject invalid orientations.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The setter has thrown an unexpected exception: " + e);
        }
    }


    /**
     * Tests the CellConstraints parser on valid encodings with different locales.
     */
    public void testValidEncodings() {
        testValidEncodings(Locale.ENGLISH);
        testValidEncodings(AllFormsTests.TURKISH);
    }


    /**
     * Tests with different locales that the CellConstraints parser
     * rejects invalid encodings.
     */
    public void testRejectInvalidCellConstraintsEncodings() {
        testRejectInvalidCellConstraintsEncodings(Locale.ENGLISH);
        testRejectInvalidCellConstraintsEncodings(AllFormsTests.TURKISH);
    }


    /**
     * Tests the CellConstraints parser on valid encodings with a given locale.
     *
     * @param locale    the Locale used while parsing the strings
     */
    private void testValidEncodings(Locale locale) {
        Locale oldDefault = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            CellConstraints cc;
            cc = new CellConstraints();
            assertEquals(cc, new CellConstraints("1, 1"));

            cc = new CellConstraints(2, 3);
            assertEquals(cc, new CellConstraints("2, 3"));

            cc = new CellConstraints(3, 4, 2, 5);
            assertEquals(cc, new CellConstraints("3, 4, 2, 5"));

            cc = new CellConstraints(5, 6, CellConstraints.LEFT, CellConstraints.BOTTOM);
            assertEquals(cc, new CellConstraints("5, 6, l, b"));
            assertEquals(cc, new CellConstraints("5, 6, L, B"));
            assertEquals(cc, new CellConstraints("5, 6, left, bottom"));
            assertEquals(cc, new CellConstraints("5, 6, LEFT, BOTTOM"));

            cc = new CellConstraints(7, 8, 3, 2, CellConstraints.FILL, CellConstraints.DEFAULT);
            assertEquals(cc, new CellConstraints("7, 8, 3, 2, f, d"));
            assertEquals(cc, new CellConstraints("7, 8, 3, 2, F, D"));
            assertEquals(cc, new CellConstraints("7, 8, 3, 2, fill, default"));
            assertEquals(cc, new CellConstraints("7, 8, 3, 2, FILL, DEFAULT"));
        } finally {
            Locale.setDefault(oldDefault);
        }
    }


    /**
     * Tests that the CellConstraints parser rejects invalid encodings
     * on a given locale.
     *
     * @param locale    the Locale used while parsing the strings
     */
    private void testRejectInvalidCellConstraintsEncodings(Locale locale) {
        Locale oldDefault = Locale.getDefault();
        Locale.setDefault(locale);
        try {
            assertRejects("0, 1, 1, 1");           // Illegal bounds
            assertRejects("0, 1, 1");              // Illegal number of arguments
            assertRejects("0, 1, 1, 1, 1");        // Illegal number of arguments
            assertRejects("1");                    // Syntax error
            assertRejects("1, 1, fill");           // Syntax error
            assertRejects("1, 1, 3, 4, f");        // Syntax error
            assertRejects("1, 1, top, center");    // Illegal column alignment
            assertRejects("1, 1, fill, left");     // Illegal row alignment
            assertRejects("1, 1, F\u0131LL, TOP"); // Illegal Turkish char
            assertRejects("1, 1, 2, 3, t, c");     // Illegal column alignment
            assertRejects("1, 1, 2, 3, f, l");     // Illegal row alignment
        } finally {
            Locale.setDefault(oldDefault);
        }
    }


    // Helper Code ***********************************************************

    /**
     * Checks if the CellConstraints constructor allows to construct
     * an instance for the specified cell bounds.
     *
     * @param invalidEncoding   the encoding that should be rejected
     */
    private void assertRejects(String invalidEncoding) {
        try {
            new CellConstraints(invalidEncoding);
            fail("The parser should reject the invalid encoding: " + invalidEncoding);
        } catch (IllegalArgumentException e) {
            // The expected behavior
        } catch (IndexOutOfBoundsException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The parser has thrown an unexpected exception for:"
                 + invalidEncoding
                 + "; exception=" + e);
        }
    }


    /**
     * Checks if the CellConstraints constructor allows to construct
     * an instance for the specified cell bounds.
     *
     * @param gridX   the first column in the grid
     * @param gridY   the first row in the grid
     * @param gridWidth the column span
     * @param gridHeight the row span
     */
    private void assertRejects(int gridX, int gridY,
                               int gridWidth, int gridHeight) {
        try {
            new CellConstraints(gridX, gridY, gridWidth, gridHeight);
            fail("The CellConstraints constructor should reject non-positive bounds values.");
        } catch (IndexOutOfBoundsException e) {
            // The expected behavior
        } catch (Exception e) {
            fail("The CellConstraints constructor has thrown an unexpected exception:" + e);
        }
    }


    /**
     * Checks if the given CellConstraints instances are equal
     * and throws a failure if not.
     *
     * @param expected the expected constraints object to be compared
     * @param actual   the actual constraints object to be compared
     */
    private void assertEquals(CellConstraints expected, CellConstraints actual) {
        if (   expected.gridX != actual.gridX
            || expected.gridY != actual.gridY
            || expected.gridWidth != actual.gridWidth
            || expected.gridHeight != actual.gridHeight) {
            fail("Bounds mismatch: expected=" + expected + "; actual=" + actual);
        }
        if (   expected.hAlign != actual.hAlign
            || expected.vAlign != actual.vAlign) {
            fail("Alignment mismatch: expected=" + expected + "; actual=" + actual);
        }
        if (!expected.insets.equals(actual.insets)) {
            fail("Insets mismatch: expected=" + expected + "; actual=" + actual);
        }
    }


}
