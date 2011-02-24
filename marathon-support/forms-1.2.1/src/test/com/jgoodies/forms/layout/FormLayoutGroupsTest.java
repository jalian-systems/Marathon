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

import junit.framework.TestCase;

/**
 * Tests column and row groups of the FormLayout.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.13 $
 */
public final class FormLayoutGroupsTest extends TestCase {

    private FormLayout layout;

    /**
     * @throws Exception   in case of an unexpected problem
     */
    protected void setUp() throws Exception {
        super.setUp();
        layout = new FormLayout(
            "pref, pref, pref, pref",
            "pref, pref, pref, pref");
    }

    /**
     * @throws Exception   in case of an unexpected problem
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        layout = null;
    }

    /**
     * Checks that column groups use a deep copy mechanism,
     * not a shallow copy.
     */
    public void testDeepCopyColumnGroups() {
        int[][] columnGroups = createAllGroups();
        layout.setColumnGroups(columnGroups);

        // Modify the column group set (first level).
        columnGroups[1] = new int[]{1, 4};
        if (equals(columnGroups, layout.getColumnGroups()))
            fail("Column group sets should be immutable.");

        // Modify a column group (second level)
        columnGroups[0][0] = 5;
        if (equals(columnGroups, layout.getColumnGroups()))
            fail("Column groups should be immutable.");
    }


    /**
     * Checks that row groups use a deep copy mechanism,
     * not a shallow copy.
     */
    public void testDeepCopyRowGroups() {
        int[][] rowGroups = createAllGroups();
        layout.setRowGroups(rowGroups);

        // Modify the row group set (first level).
        rowGroups[1] = new int[]{1, 4};
        if (equals(rowGroups, layout.getRowGroups()))
            fail("The row group sets should be immutable.");

        // Modify a row group (second level)
        rowGroups[0][0] = 5;
        if (equals(rowGroups, layout.getRowGroups()))
            fail("Row groups should be immutable.");
    }

    /**
     * Tests if invalid column indices are rejected.
     */
    public void testRejectInvalidColumnIndex() {
        try {
            layout.setColumnGroups(new int[][]{{1, 5}});
            fail("An invalid column index should be rejected.");
        } catch (IndexOutOfBoundsException e) {
            // The expected behavior
        }
    }

    /**
     * Tests if invalid row indices are rejected.
     */
    public void testRejectInvalidRowIndex() {
        try {
            layout.setRowGroups(new int[][]{{1, 5}});
            fail("An invalid row index should be rejected.");
        } catch (IndexOutOfBoundsException e) {
            // The expected behavior
        }
    }

    /**
     * Tests if duplicate column indices are rejected.
     */
    public void testRejectDuplicateColumnIndex() {
        try {
            layout.setColumnGroups(new int[][]{{1, 2}, {2, 3}});
            fail("A duplicate column index should be rejected.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        }
    }

    /**
     * Tests if duplicate row indices are rejected.
     */
    public void testRejectDuplicateRowIndex() {
        try {
            layout.setRowGroups(new int[][]{{1, 2}, {2, 3}});
            fail("A duplicate row index should be rejected.");
        } catch (IllegalArgumentException e) {
            // The expected behavior
        }
    }


    // Helper Code *************************************************

    private int[][] createAllGroups() {
        int[] group1 = new int[]{1, 2};
        int[] group2 = new int[]{3, 4};
        return new int[][] {group1, group2};
    }

    /**
     * Checks and returns if the two-dimensional arrays are equal.
     * @param array1	a two-dimensional array
     * @param array2	a second two-dimensional array
     * @return true if both arrays are equal, false otherwise
     */
    private boolean equals(int[][] array1, int[][] array2) {
        if (array1.length != array2.length)
            return false;
        for (int i = 0; i < array1.length; i++) {
            int[] subarray1 = array1[i];
            int[] subarray2 = array2[i];
            if (subarray1.length != subarray2.length)
                return false;
            for (int j = 0; j < subarray1.length; j++) {
                if (subarray1[j] != subarray2[j])
                    return false;
            }
        }
        return true;
    }

}
