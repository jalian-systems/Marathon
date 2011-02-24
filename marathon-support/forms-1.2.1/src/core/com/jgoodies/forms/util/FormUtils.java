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

package com.jgoodies.forms.util;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Consists only of static utility methods.
 *
 * This class may be merged with the FormLayoutUtils extra - or not.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.9 $
 *
 * @since 1.2
 */
public final class FormUtils {

    // Instance *************************************************************

    private FormUtils() {
        // Suppresses default constructor, prevents instantiation.
    }


    // API ********************************************************************

    /**
     * Throws an exception if the specified text is blank using the given
     * text description.
     *
     * @param text          the text to check
     * @param description   describes the text, used in the exception message
     *
     * @throws NullPointerException if {@code text} is {@code null}
     * @throws IllegalArgumentException if {@code text} is empty, or blank
     */
    public static void assertNotBlank(String text, String description) {
        if (text == null)
            throw new NullPointerException("The " + description + " must not be null.");
        if (FormUtils.isBlank(text)) {
            throw new IllegalArgumentException(
                    "The " + description + " must not be empty, or whitespace. " +
                    "See FormUtils.isBlank(String)");
        }
    }


    /**
     * Throws an NPE if the given object is {@code null} that uses
     * the specified text to describe the object.
     *
     * @param object        the text to check
     * @param description   describes the object, used in the exception message
     *
     * @throws NullPointerException if {@code object} is {@code null}
     */
    public static void assertNotNull(Object object, String description) {
        if (object == null)
            throw new NullPointerException("The " + description + " must not be null.");
    }


    /**
     * Checks and answers if the two objects are
     * both {@code null} or equal.
     *
     * <pre>
     * #equals(null, null)  == true
     * #equals("Hi", "Hi")  == true
     * #equals("Hi", null)  == false
     * #equals(null, "Hi")  == false
     * #equals("Hi", "Ho")  == false
     * </pre>
     *
     * @param o1        the first object to compare
     * @param o2        the second object to compare
     * @return boolean  {@code true} if and only if
     *    both objects are {@code null} or equal
     */
    public static boolean equals(Object o1, Object o2) {
        return    ((o1 != null) && (o2 != null) && (o1.equals(o2)))
               || ((o1 == null) && (o2 == null));
    }


    /**
     * Checks and answers if the given string is whitespace, empty (""),
     * or {@code null}.
     *
     * <pre>
     * FormUtils.isBlank(null)    == true
     * FormUtils.isBlank("")      == true
     * FormUtils.isBlank(" ")     == true
     * FormUtils.isBlank(" abc")  == false
     * FormUtils.isBlank("abc ")  == false
     * FormUtils.isBlank(" abc ") == false
     * </pre>
     *
     * @param str   the string to check, may be{@code null}
     * @return {@code true} if the string is whitespace, empty, or {@code null}
     */
    public static boolean isBlank(String str) {
        int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }
        for (int i = length - 1; i >= 0; i--) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks and answers if the given string is not empty (""),
     * not {@code null} and not whitespace only.
     *
     * <pre>
     * FormUtils.isNotBlank(null)    == false
     * FormUtils.isNotBlank("")      == false
     * FormUtils.isNotBlank(" ")     == false
     * FormUtils.isNotBlank(" abc")  == true
     * FormUtils.isNotBlank("abc ")  == true
     * FormUtils.isNotBlank(" abc ") == true
     * </pre>
     *
     * @param str   the string to check, may be {@code null}
     * @return {@code true} if the string is not empty
     *    and not {@code null} and not whitespace only
     */
    public static boolean isNotBlank(String str) {
        int length;
        if ((str == null) || ((length = str.length()) == 0))
            return false;
        for (int i = length-1; i >= 0; i--) {
            if (!Character.isWhitespace(str.charAt(i)))
                return true;
        }
        return false;
    }


    /**
     * Lazily checks and answers whether the Aqua look&amp;feel is active.
     *
     * @return {@code true} if the current look&amp;feel is Aqua
     */
    public static boolean isLafAqua() {
        ensureValidCache();
        if (cachedIsLafAqua == null) {
            cachedIsLafAqua = Boolean.valueOf(computeIsLafAqua());
        }
        return cachedIsLafAqua.booleanValue();
    }


    // Caching and Lazily Computing the Laf State *****************************

    /**
     * Clears cached internal Forms state that is based
     * on the Look&amp;Feel, for example dialog base units.<p>
     *
     * There's typically no need to call this method directly.
     * It'll be invoked automatically, if the L&amp;F has been changed
     * via {@link UIManager#setLookAndFeel} and cached data is requested.
     * It's been made public to allow cache invalidation for cases
     * where the L&amp;F is changed temporarily by replacing the UIDefaults,
     * for example in a visual editor.
     *
     * @since 1.2.1
     */
    public static void clearLookAndFeelBasedCaches() {
        cachedIsLafAqua = null;
        DefaultUnitConverter.getInstance().clearCache();
    }


    /**
     * Holds the LookAndFeel that has been used to computed cached values.
     * If the current L&amp;F differs from this cached value,
     * the caches must be cleared.
     */
    private static LookAndFeel cachedLookAndFeel;


    /**
     * Holds the cached result of the Aqua l&amp;f check.
     * Is invalidated if a look&amp;feel change has been detected
     * in <code>#ensureValidCache</code>.
     */
    private static Boolean cachedIsLafAqua;

    /**
     * Computes and answers whether an Aqua look&amp;feel is active.
     * This may be Apple's Aqua L&amp;f, or a sub-L&amp;f that
     * uses the same ID, because it doesn't substantially change the look.
     *
     * @return true if the current look&amp;feel is Aqua
     */
    private static boolean computeIsLafAqua() {
        return UIManager.getLookAndFeel().getID().equals("Aqua");
    }


    static void ensureValidCache() {
        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        if (currentLookAndFeel != cachedLookAndFeel) {
            clearLookAndFeelBasedCaches();
            cachedLookAndFeel = currentLookAndFeel;
        }
    }


}
