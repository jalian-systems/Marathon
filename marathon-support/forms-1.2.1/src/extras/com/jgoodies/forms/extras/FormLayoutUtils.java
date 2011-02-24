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

package com.jgoodies.forms.extras;

import java.awt.Component;
import java.awt.Container;
import java.util.NoSuchElementException;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * Consists only of static methods that provide convenience behavior
 * for working with the <code>FormLayout</code>.<p>
 *
 * <strong>Note:</strong> This class is not part of the binary Form library.
 * It comes with the Forms distributions as an extra.
 * <strong>The API is work in progress and may change without notice;
 * this class may even be completely removed from future distributions.</strong>
 * If you want to use this class, you may consider copying it into
 * your codebase.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.8 $
 */
public final class FormLayoutUtils {

    private FormLayoutUtils() {
        // Override default constructor; prevents instantiation.
    }


    // Tests ******************************************************************

    /**
     * Checks and answers whether the given FormLayout container
     * contains a component in the specified column.<p>
     *
     * For every container child component, we look up the associated
     * <code>CellConstraints</code> object from the layout and
     * compare its horizontal grid origin with the specified column index.
     *
     * @param container     the layout container
     * @param columnIndex   the index of the column to test
     * @return true if the column contains a component, false otherwise
     * @throws IllegalArgumentException if the container's layout is
     *     not a <code>FormLayout</code>
     */
    public static boolean columnContainsComponent(
            Container container,
            int columnIndex) {
        for (ConstraintIterator iterator = new ConstraintIterator(container); iterator.hasNext(); ) {
            if (columnIndex == iterator.nextConstraints().gridX) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks and answers whether the given FormLayout container
     * contains a component in the specified row.<p>
     *
     * For every container child component, we look up the associated
     * <code>CellConstraints</code> object from the layout and
     * compare its vertical grid origin with the specified row index.
     *
     * @param container     the layout container
     * @param rowIndex      the index of the row to test
     * @return true if the row contains a component, false otherwise
     * @throws IllegalArgumentException if the container's layout is
     *     not a <code>FormLayout</code>
     */
    public static boolean rowContainsComponent(
            Container container,
            int rowIndex) {
        for (ConstraintIterator iterator = new ConstraintIterator(container); iterator.hasNext(); ) {
            if (rowIndex == iterator.nextConstraints().gridY) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks and answers whether the specified column is grouped
     * in the given FormLayout. A column <code>col</code> is grouped,
     * if and only if there's a column group <em>group</em>
     * that includes <code>col</code>'s index.
     *
     * @param layout      the layout to be inspected
     * @param columnIndex the index of the column to be checked
     * @return true if the column is grouped, false if not
     */
    public static boolean isGroupedColumn(FormLayout layout, int columnIndex) {
        return isGrouped(layout.getColumnGroups(), columnIndex);
    }


    /**
     * Checks and answers whether the specified row is grouped
     * in the given FormLayout. A row <code>row</code> is grouped,
     * if and only if there's a row group <em>group</em>
     * that includes <code>row</code>'s index.
     *
     * @param layout      the layout to be inspected
     * @param rowIndex    the index of the row to be checked
     * @return true if the column is grouped, false if not
     */
    public static boolean isGroupedRow(FormLayout layout, int rowIndex) {
        return isGrouped(layout.getRowGroups(), rowIndex);
    }


    // Helper Code ***********************************************************

    /**
     * Checks and answers whether the specified index is contained
     * in one of the given group indices.
     *
     * @param allGroupIndices  an array of arrays of group indices
     * @param index            the index to be tested
     * @return true if index is contained in one of the groups
     */
    private static boolean isGrouped(int[][] allGroupIndices, int index) {
        for (int group = 0; group < allGroupIndices.length; group++) {
            int[] groupIndices = allGroupIndices[group];
            for (int i = 0; i < groupIndices.length; i++) {
                int aGroupIndex = groupIndices[i];
                if (index == aGroupIndex) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Iterates over a FormLayout container's <code>CellConstraints</code>.
     * The container's child component collection and the layout's
     * constraints collection must not be changed during the iteration;
     * otherwise the behavior of this iterator is unspecified and unsafe.
     *
     * @see FormLayout
     * @see CellConstraints
     */
    public static final class ConstraintIterator {

        /**
         * Refers to the FormLayout instance used to look up constraints.
         */
        private final FormLayout  layout;

        /**
         * Holds a copy of the container's components.
         */
        private final Component[] components;

        /**
         * The current index in the component array.
         * Used to determine whether there are more elements
         * and to look up the next constraints.
         */
        private int index;


        // Instance Creation *************************************************

        /**
         * Constructs a ConstraintIterator for the given FormLayout container.
         * Useful to iterate over the container's <code>CellConstraints</code>.
         *
         * @param container   the layout container
         * @throws IllegalArgumentException if the container's layout is
         *     not a <code>FormLayout</code>
         */
        public ConstraintIterator(Container container) {
            if (!(container.getLayout() instanceof FormLayout)) {
                throw new IllegalArgumentException("The container must use an instance of FormLayout.");
            }
            layout = (FormLayout) container.getLayout();
            components = container.getComponents();
            index = 0;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return true if the iterator has more elements.
         */
        public boolean hasNext() {
            return index < components.length;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public CellConstraints nextConstraints() {
            if (!hasNext())
                throw new NoSuchElementException(
                        "The constraint iterator has no more elements.");

            return layout.getConstraints(components[index++]);
        }

    }


}
