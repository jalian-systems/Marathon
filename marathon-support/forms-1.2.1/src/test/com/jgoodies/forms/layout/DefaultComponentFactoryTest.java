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

import javax.swing.JLabel;

import junit.framework.TestCase;

import com.jgoodies.forms.factories.DefaultComponentFactory;

/**
 * A test case for class {@link DefaultComponentFactory}.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.5 $
 */
public final class DefaultComponentFactoryTest extends TestCase {

    public void testLabelWithoutMnemonic() {
        testMnemonic("",      "",      0, -1);
        testMnemonic(" ",     " ",     0, -1);
        testMnemonic("  ",    "  ",    0, -1);
        testMnemonic("   ",   "   ",   0, -1);
        testMnemonic("a",     "a",     0, -1);
        testMnemonic("a ",    "a ",    0, -1);
        testMnemonic(" a",    " a",    0, -1);
        testMnemonic(" a ",   " a ",   0, -1);
        testMnemonic("abcd",  "abcd",  0, -1);
        testMnemonic("abcd ", "abcd ", 0, -1);
    }

    public void testLabelWithSingleMnemonicMarker() {
        testMnemonic("&x",      "x",      'X', 0);
        testMnemonic("a&x",     "ax",     'X', 1);
        testMnemonic("ab&x",    "abx",    'X', 2);
        testMnemonic("abc&x",   "abcx",   'X', 3);
        testMnemonic("abc&x ",  "abcx ",  'X', 3);
        testMnemonic("x&x",     "xx",     'X', 1);
        testMnemonic("xa&x",    "xax",    'X', 2);
        testMnemonic("xab&x",   "xabx",   'X', 3);
        testMnemonic("xabc&x",  "xabcx",  'X', 4);
        testMnemonic("xabc&x ", "xabcx ", 'X', 4);
        testMnemonic("X&x",     "Xx",     'X', 1);
        testMnemonic("Xa&x",    "Xax",    'X', 2);
        testMnemonic("Xab&x",   "Xabx",   'X', 3);
        testMnemonic("Xabc&x",  "Xabcx",  'X', 4);
        testMnemonic("Xabc&x ", "Xabcx ", 'X', 4);
    }

    public void testLabelWithSingleQuotedMnemonicMarker() {
        testMnemonic("&&",       "&",       0, -1);
        testMnemonic("a&&",      "a&",      0, -1);
        testMnemonic("ab&&",     "ab&",     0, -1);
        testMnemonic("abc&&",    "abc&",    0, -1);
        testMnemonic("abc&& ",   "abc& ",   0, -1);
        testMnemonic("abc&&a",   "abc&a",   0, -1);
        testMnemonic("abc&&ab",  "abc&ab",  0, -1);
        testMnemonic("abc&&ab ", "abc&ab ", 0, -1);
    }

    public void testLabelWithMultipleMnemonicMarkers() {
        testMnemonic("&x&x",      "xx",      'X', 0);
        testMnemonic("a&x&x",     "axx",     'X', 1);
        testMnemonic("ab&x&x",    "abxx",    'X', 2);
        testMnemonic("abc&x&x",   "abcxx",   'X', 3);
        testMnemonic("abc&x &x",  "abcx x",  'X', 3);
        testMnemonic("x&x&x",     "xxx",     'X', 1);
        testMnemonic("xa&x&x",    "xaxx",    'X', 2);
        testMnemonic("xab&x&x",   "xabxx",   'X', 3);
        testMnemonic("xabc&x&x",  "xabcxx",  'X', 4);
        testMnemonic("xabc&x &x", "xabcx x", 'X', 4);
        testMnemonic("X&x&x",     "Xxx",     'X', 1);
        testMnemonic("Xa&x&x",    "Xaxx",    'X', 2);
        testMnemonic("Xab&x&x",   "Xabxx",   'X', 3);
        testMnemonic("Xabc&x&x",  "Xabcxx",  'X', 4);
        testMnemonic("Xabc&x &x", "Xabcx x", 'X', 4);
    }

    public void testLabelWithQuotedMarkerBeforeMnemonic() {
        testMnemonic("L&&F &choice", "L&F choice", 'C', 4);
        testMnemonic("&&&x",      "&x",      'X', 1);
        testMnemonic("&&a&x",     "&ax",     'X', 2);
        testMnemonic("&&ab&x",    "&abx",    'X', 3);
        testMnemonic("&&abc&x",   "&abcx",   'X', 4);
        testMnemonic("&&abc&x ",  "&abcx ",  'X', 4);
        testMnemonic("&&x&x",     "&xx",     'X', 2);
        testMnemonic("&&xa&x",    "&xax",    'X', 3);
        testMnemonic("&&xab&x",   "&xabx",   'X', 4);
        testMnemonic("&&xabc&x",  "&xabcx",  'X', 5);
        testMnemonic("&&xabc&x ", "&xabcx ", 'X', 5);
        testMnemonic("&&X&x",     "&Xx",     'X', 2);
        testMnemonic("X&&a&x",    "X&ax",    'X', 3);
        testMnemonic("X&&ab&x",   "X&abx",   'X', 4);
        testMnemonic("X&&abc&x",  "X&abcx",  'X', 5);
        testMnemonic("X&&abc&x ", "X&abcx ", 'X', 5);
    }

    public void testLabelWithQuotedMarkerAfterMnemonic() {
        testMnemonic("&Look&&Feel choice", "Look&Feel choice", 'L', 0);
        testMnemonic("&x&&",      "x&",      'X', 0);
        testMnemonic("a&x&&",     "ax&",     'X', 1);
        testMnemonic("ab&x&&",    "abx&",    'X', 2);
        testMnemonic("abc&x&&",   "abcx&",   'X', 3);
        testMnemonic("abc&x &&",  "abcx &",  'X', 3);
        testMnemonic("a&x&&b",    "ax&b",    'X', 1);
        testMnemonic("a&xb&&c",   "axb&c",   'X', 1);
        testMnemonic("a&xbc&&",   "axbc&",   'X', 1);
        testMnemonic("a&xb&&c",   "axb&c",   'X', 1);
    }

    public void testLabelWithQuotedMarkersBeforeAndAfterMnemonic() {
        testMnemonic("a&&b&&c&&d&x&&e&&f&&",  "a&b&c&dx&e&f&",  'X', 7);
        testMnemonic("a&&b&&c&&d&x&&e&&f&&g", "a&b&c&dx&e&f&g", 'X', 7);
    }


    // Test Helper Code *******************************************************

    private void testMnemonic(String textWithMnemonic,
            String expectedText,
            int expectedMnemonic,
            int expectedMnemonicIndex) {
        JLabel label = DefaultComponentFactory.getInstance().createLabel(textWithMnemonic);
        assertEquals("Text",
                expectedText,
                label.getText());
        assertEquals("Mnemonic",
                expectedMnemonic,
                label.getDisplayedMnemonic());
        assertEquals("Mnemonic index",
                expectedMnemonicIndex,
                label.getDisplayedMnemonicIndex());
    }


}
