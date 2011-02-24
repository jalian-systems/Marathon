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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A test suite for all tests related to the JGoodies Forms framework.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.19 $
 */
public final class AllFormsTests {

    /** A constant for the Turkish locale. */
    public static final Locale TURKISH = new Locale("tr");

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllFormsTests.class);
    }

    public static Test suite() {
        return new TestSuite(new Class[]{
                BordersTest.class,
                CellConstraintsTest.class,
                ClassLoaderTest.class,
                ColumnSpecTest.class,
                DefaultComponentFactoryTest.class,
                FormLayoutTest.class,
                FormLayoutGroupsTest.class,
                RowSpecTest.class,
                SerializationTest.class,
                UnitConversionTest.class
            },
            "Tests for com.jgoodies.forms.layout");
    }

}
