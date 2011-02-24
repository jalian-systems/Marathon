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

package com.jgoodies.forms.builder;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The abstract superclass for {@link ButtonBarBuilder2}.
 * Provides a cell cursor for traversing
 * the button bar/stack while components are added. It also offers
 * convenience methods to append logical columns and rows.<p>
 *
 * TODO: Mention the ButtonStackBuilder2 subclass as soon as it is available.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.3 $
 *
 * @since 1.2
 */
public abstract class AbstractButtonPanelBuilder {

    /**
     * Holds the layout container that we are building.
     */
    private final JPanel container;

    /**
     * Holds the instance of <code>FormLayout</code> that is used
     * to specify, fill and layout this form.
     */
    private final FormLayout layout;

    /**
     * Holds an instance of <code>CellConstraints</code> that will be used to
     * specify the location, extent and alignments of the component to be
     * added next.
     */
    private final CellConstraints currentCellConstraints;

    /**
     * Specifies if we fill the grid from left to right or right to left.
     * This value is initialized during the construction from the layout
     * container's component orientation.
     *
     * @see #isLeftToRight()
     * @see #setLeftToRight(boolean)
     * @see ComponentOrientation
     */
    private boolean leftToRight;



    // Instance Creation ****************************************************

    /**
     * Constructs a <code>AbstractFormBuilder</code>
     * for the given FormLayout and layout container.
     *
     * @param layout     the {@link FormLayout} to use
     * @param container  the layout container
     *
     * @throws NullPointerException if the layout or container is null
     */
    protected AbstractButtonPanelBuilder(FormLayout layout, JPanel container) {
        if (layout == null)
            throw new NullPointerException("The layout must not be null.");

        if (container == null)
            throw new NullPointerException("The layout container must not be null.");

        this.container = container;
        this.layout    = layout;

        container.setLayout(layout);
        currentCellConstraints = new CellConstraints();
        ComponentOrientation orientation = container.getComponentOrientation();
        leftToRight = orientation.isLeftToRight()
                  || !orientation.isHorizontal();
    }


    // Accessors ************************************************************

    /**
     * Returns the container used to build the form.
     *
     * @return the layout container, a {code JPanel}.
     */
    public final JPanel getContainer() {
        return container;
    }


    /**
     * Returns the panel used to build the form.
     *
     * @return the panel used by this builder to build the form
     */
    public final JPanel getPanel() {
        return getContainer();
    }


    /**
     * Returns the instance of {@link FormLayout} used to build this form.
     *
     * @return the FormLayout
     */
    public final FormLayout getLayout() {
        return layout;
    }


    // Frequently Used Panel Properties ***************************************

    /**
     * Sets the panel's background color.
     *
     * @param background  the color to set as new background
     *
     * @see JComponent#setBackground(Color)
     */
    public final void setBackground(Color background) {
        getPanel().setBackground(background);
    }


    /**
     * Sets the panel's border.
     *
     * @param border    the border to set
     *
     * @see JComponent#setBorder(Border)
     */
    public final void setBorder(Border border) {
        getPanel().setBorder(border);
    }


    /**
     * Sets the panel's opaque state.
     *
     * @param b   true for opaque, false for non-opaque
     *
     * @see JComponent#setOpaque(boolean)
     *
     * @since 1.1
     */
    public final void setOpaque(boolean b) {
        getPanel().setOpaque(b);
    }


    // Accessing the Cursor Direction ***************************************

    /**
     * Returns whether this builder fills the form left-to-right
     * or right-to-left. The initial value of this property is set
     * during the builder construction from the layout container's
     * <code>componentOrientation</code> property.
     *
     * @return true indicates left-to-right, false indicates right-to-left
     *
     * @see #setLeftToRight(boolean)
     * @see ComponentOrientation
     */
    public final boolean isLeftToRight() {
        return leftToRight;
    }


    /**
     * Sets the form fill direction to left-to-right or right-to-left.
     * The initial value of this property is set during the builder construction
     * from the layout container's <code>componentOrientation</code> property.
     *
     * @param b   true indicates left-to-right, false right-to-left
     *
     * @see #isLeftToRight()
     * @see ComponentOrientation
     */
    public final void setLeftToRight(boolean b) {
        leftToRight = b;
    }


    // Accessing the Cursor Location and Extent *****************************

    /**
     * Moves to the next column, does the same as #nextColumn(1).
     */
    protected final void nextColumn() {
        nextColumn(1);
    }


    /**
     * Moves to the next column.
     *
     * @param columns	 number of columns to move
     */
    private void nextColumn(int columns) {
        currentCellConstraints.gridX += columns * getColumnIncrementSign();
    }


    /**
     * Increases the row by one; does the same as #nextRow(1).
     */
    protected final void nextRow() {
        nextRow(1);
    }


    /**
     * Increases the row by the specified rows.
     *
     * @param rows	 number of rows to move
     */
    private void nextRow(int rows) {
        currentCellConstraints.gridY += rows;
    }


    // Appending Columns ******************************************************

    /**
     * Appends the given column specification to the builder's layout.
     *
     * @param columnSpec  the column specification object to append
     */
    protected final void appendColumn(ColumnSpec columnSpec) {
        getLayout().appendColumn(columnSpec);
    }


    /**
     * Appends a glue column.
     *
     * @see #appendRelatedComponentsGapColumn()
     * @see #appendUnrelatedComponentsGapColumn()
     */
    protected final void appendGlueColumn() {
        appendColumn(FormFactory.GLUE_COLSPEC);
    }


    /**
     * Appends a column that is the default gap for related components.
     *
     * @see #appendGlueColumn()
     * @see #appendUnrelatedComponentsGapColumn()
     */
    protected final void appendRelatedComponentsGapColumn() {
        appendColumn(FormFactory.RELATED_GAP_COLSPEC);
    }


    /**
     * Appends a column that is the default gap for unrelated components.
     *
     * @see #appendGlueColumn()
     * @see #appendRelatedComponentsGapColumn()
     */
    protected final void appendUnrelatedComponentsGapColumn() {
        appendColumn(FormFactory.UNRELATED_GAP_COLSPEC);
    }


    // Appending Rows ********************************************************

    /**
     * Appends the given row specification to the builder's layout.
     *
     * @param rowSpec  the row specification object to append
     */
    protected final void appendRow(RowSpec rowSpec) {
        getLayout().appendRow(rowSpec);
    }


    /**
     * Appends a glue row.
     *
     * @see #appendRelatedComponentsGapRow()
     * @see #appendUnrelatedComponentsGapRow()
     */
    protected final void appendGlueRow() {
        appendRow(FormFactory.GLUE_ROWSPEC);
    }


    /**
     * Appends a row that is the default gap for related components.
     *
     * @see #appendGlueRow()
     * @see #appendUnrelatedComponentsGapRow()
     */
    protected final void appendRelatedComponentsGapRow() {
        appendRow(FormFactory.RELATED_GAP_ROWSPEC);
    }


    /**
     * Appends a row that is the default gap for unrelated components.
     *
     * @see #appendGlueRow()
     * @see #appendRelatedComponentsGapRow()
     */
    protected final void appendUnrelatedComponentsGapRow() {
        appendRow(FormFactory.UNRELATED_GAP_ROWSPEC);
    }


    // Adding Components ****************************************************

    /**
     * Adds a component to the container using the default cell constraints.
     * Note that when building from left to right, this method won't adjust
     * the cell constraints if the column span is larger than 1.
     *
     * @param component	the component to add
     * @return the added component
     */
    protected final Component add(Component component) {
        container.add(component, currentCellConstraints);
        return component;
    }


    // Misc *****************************************************************

    /**
     * Returns the sign (-1 or 1) used to increment the cursor's column
     * when moving to the next column.
     *
     * @return -1 for right-to-left, 1 for left-to-right
     */
    private int getColumnIncrementSign() {
        return isLeftToRight() ? 1 : -1;
    }


}
