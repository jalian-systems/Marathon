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

import java.awt.Dimension;

import javax.swing.JPanel;

import junit.framework.TestCase;

/**
 * Tests the FormLayout's layout algorithm.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.14 $
 */
public final class FormLayoutTest extends TestCase {

    private final CellConstraints cc = new CellConstraints();


    /**
     * Checks basic layout functions.
     */
    public void testBasic() {
        FormLayout layout = new FormLayout(
            "1px, 2px, 3px, 5px, 7px",
            "1px, 2px, 3px");

        JPanel panel = new JPanel(layout);
        panel.doLayout();
        FormLayout.LayoutInfo info = layout.getLayoutInfo(panel);
        assertEquals("Columns",   6, info.columnOrigins.length);
        assertEquals("Rows",      4, info.rowOrigins.length);
        assertEquals("Column 0",  0, info.columnOrigins[0]);
        assertEquals("Column 1",  1, info.columnOrigins[1]);
        assertEquals("Column 2",  3, info.columnOrigins[2]);
        assertEquals("Column 3",  6, info.columnOrigins[3]);
        assertEquals("Column 4", 11, info.columnOrigins[4]);
        assertEquals("Column 5", 18, info.columnOrigins[5]);
    }


    /**
     * Checks whether components are aligned according to the column specs.
     */
    public void testHorizontalAlignments() {
        TestComponent left   = new TestComponent(2, 7, 4, 9);
        TestComponent center = new TestComponent(2, 7, 4, 9);
        TestComponent right  = new TestComponent(2, 7, 4, 9);
        TestComponent fill   = new TestComponent(2, 7, 4, 9);
        TestComponent def    = new TestComponent(2, 7, 4, 9);
        FormLayout layout = new FormLayout(
            "left:10px, center:10px, right:10px, fill:10px, 10px",
            "pref");

        JPanel panel = new JPanel(layout);
        panel.add(left,   cc.xy(1, 1));
        panel.add(center, cc.xy(2, 1));
        panel.add(right,  cc.xy(3, 1));
        panel.add(fill,   cc.xy(4, 1));
        panel.add(def,    cc.xy(5, 1));

        panel.doLayout();

        assertEquals("Left.x",         0, left.getX());
        assertEquals("Left.width",     4, left.getWidth());
        assertEquals("Center.x",      13, center.getX());
        assertEquals("Center.width",   4, center.getWidth());
        assertEquals("Right.x",       26, right.getX());
        assertEquals("Right.width",    4, right.getWidth());
        assertEquals("Fill.x",        30, fill.getX());
        assertEquals("Fill.width",    10, fill.getWidth());
        assertEquals("Default.x",     40, def.getX());
        assertEquals("Default.width", 10, def.getWidth());
    }


    /**
     * Checks whether components are aligned according to the row specs.
     */
    public void testVerticalAlignments() {
        TestComponent top     = new TestComponent(7, 2, 9, 4);
        TestComponent center  = new TestComponent(7, 2, 9, 4);
        TestComponent bottom  = new TestComponent(7, 2, 9, 4);
        TestComponent fill    = new TestComponent(7, 2, 9, 4);
        TestComponent def     = new TestComponent(7, 2, 9, 4);
        FormLayout layout = new FormLayout(
            "pref",
            "top:10px, center:10px, bottom:10px, fill:10px, 10px");

        JPanel panel = new JPanel(layout);
        panel.add(top,     cc.xy(1, 1));
        panel.add(center,  cc.xy(1, 2));
        panel.add(bottom,  cc.xy(1, 3));
        panel.add(fill,    cc.xy(1, 4));
        panel.add(def,     cc.xy(1, 5));

        panel.doLayout();

        assertEquals("Top.y",           0, top.getY());
        assertEquals("Top.height",      4, top.getHeight());
        assertEquals("Center.y",       13, center.getY());
        assertEquals("Center.height",   4, center.getHeight());
        assertEquals("Bottom.y",       26, bottom.getY());
        assertEquals("Bottom.height",   4, bottom.getHeight());
        assertEquals("Fill.y",         30, fill.getY());
        assertEquals("Fill.height",    10, fill.getHeight());
        assertEquals("Default.y",      43, def.getY());
        assertEquals("Default.height",  4, def.getHeight());
    }


    /**
     * Tests bounded min and pref widths.
     */
    public void testBoundedWidth() {
        TestComponent c1 = new TestComponent( 2, 7,  4, 9);
        TestComponent c2 = new TestComponent(20, 7, 40, 9);
        TestComponent c3 = new TestComponent( 2, 7,  4, 9);
        TestComponent c4 = new TestComponent(20, 7, 40, 9);
        TestComponent c5 = new TestComponent( 2, 7,  4, 9);
        TestComponent c6 = new TestComponent(20, 7, 40, 9);
        TestComponent c7 = new TestComponent( 2, 7,  4, 9);
        TestComponent c8 = new TestComponent(20, 7, 40, 9);
        FormLayout layout = new FormLayout(
            "[10px,min],  [10px,min],  " +
            "[10px,pref], [10px,pref], " +
            "[min,10px],  [min,10px],  " +
            "[pref,10px], [pref,10px]",
            "pref");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy(1, 1));
        panel.add(c2, cc.xy(2, 1));
        panel.add(c3, cc.xy(3, 1));
        panel.add(c4, cc.xy(4, 1));
        panel.add(c5, cc.xy(5, 1));
        panel.add(c6, cc.xy(6, 1));
        panel.add(c7, cc.xy(7, 1));
        panel.add(c8, cc.xy(8, 1));

        panel.doLayout();

        assertEquals("max(10px;c1_min).width",   10, c1.getWidth());
        assertEquals("max(10px;c2_min).width",   20, c2.getWidth());
        assertEquals("max(10px;c3_pref).width",  10, c3.getWidth());
        assertEquals("max(10px;c4_pref).width",  40, c4.getWidth());
        assertEquals("min(10px;c5_min).width",    2, c5.getWidth());
        assertEquals("min(10px;c6_min).width",   10, c6.getWidth());
        assertEquals("min(10px;c7_pref).width",   4, c7.getWidth());
        assertEquals("min(10px;c8_pref).width",  10, c8.getWidth());
    }

    /**
     * Tests bounded min and pref widths.
     */
    public void testBoundedHeight() {
        TestComponent c1 = new TestComponent(7,  2, 9,  4);
        TestComponent c2 = new TestComponent(7, 20, 9, 40);
        TestComponent c3 = new TestComponent(7,  2, 9,  4);
        TestComponent c4 = new TestComponent(7, 20, 9, 40);
        TestComponent c5 = new TestComponent(7,  2, 9,  4);
        TestComponent c6 = new TestComponent(7, 20, 9, 40);
        TestComponent c7 = new TestComponent(7,  2, 9,  4);
        TestComponent c8 = new TestComponent(7, 20, 9, 40);
        FormLayout layout = new FormLayout(
            "pref",
            "f:[10px,min],  f:[10px,min],  " +
            "f:[10px,pref], f:[10px,pref], " +
            "f:[min,10px],  f:[min,10px],  " +
            "f:[pref,10px], f:[pref,10px]");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy(1, 1));
        panel.add(c2, cc.xy(1, 2));
        panel.add(c3, cc.xy(1, 3));
        panel.add(c4, cc.xy(1, 4));
        panel.add(c5, cc.xy(1, 5));
        panel.add(c6, cc.xy(1, 6));
        panel.add(c7, cc.xy(1, 7));
        panel.add(c8, cc.xy(1, 8));

        panel.doLayout();

        assertEquals("[10px, c1_min].height",  10, c1.getHeight());
        assertEquals("[10px, c2_min].height",  20, c2.getHeight());
        assertEquals("[10px,c3_pref].height",  10, c3.getHeight());
        assertEquals("[10px,c4_pref].height",  40, c4.getHeight());
        assertEquals("[c5_min, 10px].height",   2, c5.getHeight());
        assertEquals("[c6_min, 10px].height",  10, c6.getHeight());
        assertEquals("[c7_pref,10px].height",   4, c7.getHeight());
        assertEquals("[c8_pref,10px].height",  10, c8.getHeight());
    }


    // Testing components that span multiple columns/rows *********************

    /**
     * Checks and verifies that components that span multiple columns
     * do not expand the container of no column grows.
     */
    public void testNoExtraExpansionIfAllColumnsAreFixed() {
        TestComponent c1 = new TestComponent(10, 1, 50, 1);
        TestComponent c2 = new TestComponent(10, 1, 50, 1);
        TestComponent c3 = new TestComponent(10, 1, 50, 1);
        TestComponent c4 = new TestComponent(10, 1, 50, 1);
        FormLayout layout = new FormLayout(
            "10px, 15px, 20px",
            "pref, pref");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy (1, 1));
        panel.add(c2, cc.xy (2, 1));
        panel.add(c3, cc.xy (3, 1));
        panel.add(c4, cc.xyw(1, 2, 2));

        Dimension preferredLayoutSize = layout.preferredLayoutSize(panel);
        panel.setSize(preferredLayoutSize);
        panel.doLayout();
        int col1And2Width = c2.getX() + c2.getWidth();
        int gridWidth     = c3.getX() + c3.getWidth();
        int totalWidth    = preferredLayoutSize.width;

        assertEquals("Col1+2 width", 25, col1And2Width);
        assertEquals("Grid width",   45, gridWidth);
        assertEquals("Total width",  45, totalWidth);
    }


    /**
     * Checks and verifies that components that span multiple columns
     * do not expand the container of no column grows.
     */
    public void testNoExtraExpansionIfSpannedColumnsAreFixed() {
        TestComponent c1 = new TestComponent(10, 1, 50, 1);
        TestComponent c2 = new TestComponent(10, 1, 50, 1);
        TestComponent c3 = new TestComponent(10, 1, 50, 1);
        TestComponent c4 = new TestComponent(10, 1, 50, 1);
        FormLayout layout = new FormLayout(
            "10px, 15px, 20px:grow",
            "pref, pref");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy (1, 1));
        panel.add(c2, cc.xy (2, 1));
        panel.add(c3, cc.xy (3, 1));
        panel.add(c4, cc.xyw(1, 2, 2));

        Dimension preferredLayoutSize = layout.preferredLayoutSize(panel);
        panel.setSize(preferredLayoutSize);
        panel.doLayout();
        int col1And2Width = c2.getX() + c2.getWidth();
        int gridWidth     = c3.getX() + c3.getWidth();
        int totalWidth    = preferredLayoutSize.width;

        assertEquals("Col1+2 width",  25, col1And2Width);
        assertEquals("Grid width",    45, gridWidth);
        assertEquals("Total width",   45, totalWidth); // 70 is wrong
    }


    /**
     * Checks and verifies that components that span multiple columns
     * do not expand the container of no column grows.
     */
    public void testExtraExpansionIfSpannedColumnsGrow() {
        TestComponent c1 = new TestComponent(10, 1, 50, 1);
        TestComponent c2 = new TestComponent(10, 1, 50, 1);
        TestComponent c3 = new TestComponent(10, 1, 50, 1);
        TestComponent c4 = new TestComponent(10, 1, 50, 1);
        FormLayout layout = new FormLayout(
            "10px, 15px:grow, 20px",
            "pref, pref");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy (1, 1));
        panel.add(c2, cc.xy (2, 1));
        panel.add(c3, cc.xy (3, 1));
        panel.add(c4, cc.xyw(1, 2, 2));

        Dimension preferredLayoutSize = layout.preferredLayoutSize(panel);
        panel.setSize(preferredLayoutSize);
        panel.doLayout();
        int col1And2Width = c2.getX() + c2.getWidth();
        int gridWidth     = c3.getX() + c3.getWidth();
        int totalWidth    = preferredLayoutSize.width;

        assertEquals("Col1+2 width",  50, col1And2Width);
        assertEquals("Grid width",    70, gridWidth);
        assertEquals("Total width",   70, totalWidth);
    }


    /**
     * Checks and verifies that components that span multiple columns
     * and that expand the container are measured using the correct measure.
     */
    public void testExtraExpansionHonorsCurrentMeasure() {
        TestComponent c1 = new TestComponent(10, 1, 50, 1);
        TestComponent c2 = new TestComponent(10, 1, 50, 1);
        TestComponent c3 = new TestComponent(10, 1, 50, 1);
        TestComponent c4 = new TestComponent(10, 1, 50, 1);
        FormLayout layout = new FormLayout(
            "10px, 15px:grow, 20px",
            "pref, pref");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy (1, 1));
        panel.add(c2, cc.xy (2, 1));
        panel.add(c3, cc.xy (3, 1));
        panel.add(c4, cc.xyw(1, 2, 2));

        int minimumLayoutWidth   = layout.minimumLayoutSize(panel).width;
        int preferredLayoutWidth = layout.preferredLayoutSize(panel).width;

        assertEquals("Minimum layout width",   45, minimumLayoutWidth);
        assertEquals("Preferred layout width", 70, preferredLayoutWidth);
    }


    /**
     * Tests the layout size, column and row sizes for a default specs.
     */
    public void testDefaultSize() {
        TestComponent c1 = new TestComponent(10, 10, 50, 50);
        FormLayout layout = new FormLayout(
                "default",
                "default");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy(1, 1));

        Dimension minimumLayoutSize    = layout.minimumLayoutSize(panel);
        Dimension preferredLayoutSize  = layout.preferredLayoutSize(panel);
        assertEquals("Minimum layout width", 10, minimumLayoutSize.width);
        assertEquals("Minimum layout height", 10, minimumLayoutSize.height);
        assertEquals("Preferred layout width",  50, preferredLayoutSize.width);
        assertEquals("Preferred layout height", 50, preferredLayoutSize.height);

        panel.setSize(minimumLayoutSize);
        panel.doLayout();
        int columnWidth = c1.getWidth();
        int rowHeight = c1.getHeight();
        assertEquals("Column width (container min)", 10, columnWidth);
        assertEquals("Row height (container min)", 10, rowHeight);

        panel.setSize(preferredLayoutSize);
        panel.doLayout();
        columnWidth = c1.getWidth();
        rowHeight = c1.getHeight();
        assertEquals("Column width (container pref)", 50, columnWidth);
        assertEquals("Row height (container pref)", 50, rowHeight);
    }


    /**
     * Tests the combination of a default size spec with a lower bound
     * that shall ensure a minimum size.
     */
    public void testDefaultWithLowerBound() {
        TestComponent c1 = new TestComponent(10, 10, 50, 50);
        FormLayout layout = new FormLayout(
                "[20px,default]",
                "[20px,default]");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy(1, 1));

        Dimension minimumLayoutSize    = layout.minimumLayoutSize(panel);
        Dimension preferredLayoutSize  = layout.preferredLayoutSize(panel);

        assertEquals("Minimum layout width", 20, minimumLayoutSize.width);
        assertEquals("Minimum layout height", 20, minimumLayoutSize.height);
        assertEquals("Preferred layout width",  50, preferredLayoutSize.width);
        assertEquals("Preferred layout height", 50, preferredLayoutSize.height);

        panel.setSize(minimumLayoutSize);
        panel.doLayout();
        int columnWidth = c1.getWidth();
        int rowHeight = c1.getHeight();
        assertEquals("Column width (container min)", 20, columnWidth);
        assertEquals("Row height (container min)", 20, rowHeight);

        panel.setSize(preferredLayoutSize);
        panel.doLayout();
        columnWidth = c1.getWidth();
        rowHeight = c1.getHeight();
        assertEquals("Column width (container pref)", 50, columnWidth);
        assertEquals("Row height (container pref)", 50, rowHeight);
    }


    /**
     * Tests the combination of a default size spec with an upper bound
     * that shall ensure a maximum size.
     */
    public void testDefaultWithUpperBound() {
        TestComponent c1 = new TestComponent(10, 10, 50, 50);
        FormLayout layout = new FormLayout(
                "[default,20px]",
                "[default,20px]");

        JPanel panel = new JPanel(layout);
        panel.add(c1, cc.xy(1, 1));

        Dimension minimumLayoutSize    = layout.minimumLayoutSize(panel);
        Dimension preferredLayoutSize  = layout.preferredLayoutSize(panel);

        assertEquals("Minimum layout width", 10, minimumLayoutSize.width);
        assertEquals("Minimum layout height", 10, minimumLayoutSize.height);
        assertEquals("Preferred layout width",  20, preferredLayoutSize.width);
        assertEquals("Preferred layout height", 20, preferredLayoutSize.height);

        panel.setSize(minimumLayoutSize);
        panel.doLayout();
        int columnWidth = c1.getWidth();
        int rowHeight = c1.getHeight();
        assertEquals("Column width (container min)", 10, columnWidth);
        assertEquals("Row height (container min)", 10, rowHeight);

        panel.setSize(preferredLayoutSize);
        panel.doLayout();
        columnWidth = c1.getWidth();
        rowHeight = c1.getHeight();
        assertEquals("Column width (container pref)", 20, columnWidth);
        assertEquals("Row height (container pref)", 20, rowHeight);
    }


    // Visibility *************************************************************

    /**
     * Checks whether components are taken into account according
     * to their visibility, the container wide FormLayout setting,
     * and the cell setting.
     */
    public void testVisibility() {
        testVisibility(true);
        testVisibility(false);
    }


    public void testVisibility(boolean containerHonorsVisibility) {
        TestComponent visible   = new TestComponent(10, 10, 10, 10);
        TestComponent invisible = new TestComponent(10, 10, 10, 10);
        invisible.setVisible(false);
        TestComponent invisibleHonorsVisibility = new TestComponent(10, 10, 10, 10);
        invisibleHonorsVisibility.setVisible(false);
        TestComponent invisibleIgnoresVisibility = new TestComponent(10, 10, 10, 10);
        invisibleIgnoresVisibility.setVisible(false);
        FormLayout layout = new FormLayout(
            "pref, pref, pref, pref",
            "pref, pref, pref, pref");
        layout.setHonorsVisibility(containerHonorsVisibility);

        JPanel panel = new JPanel(layout);
        panel.add(visible,                    cc.xy(1, 1));
        panel.add(invisible,                  cc.xy(2, 2));
        panel.add(invisibleHonorsVisibility,  cc.xy(3, 3));
        panel.add(invisibleIgnoresVisibility, cc.xy(4, 4));
        layout.setHonorsVisibility(invisibleHonorsVisibility, Boolean.TRUE);
        layout.setHonorsVisibility(invisibleIgnoresVisibility, Boolean.FALSE);

        panel.doLayout();
        FormLayout.LayoutInfo info = layout.getLayoutInfo(panel);
        int size1 = 10;
        int size2 = containerHonorsVisibility ? 0 : 10;
        int size3 = 0;
        int size4 = 10;
        int origin1 = size1;
        int origin2 = origin1 + size2;
        int origin3 = origin2 + size3;
        int origin4 = origin3 + size4;
        assertEquals("Column 0",  0,       info.columnOrigins[0]);
        assertEquals("Column 1",  origin1, info.columnOrigins[1]);
        assertEquals("Column 2",  origin2, info.columnOrigins[2]);
        assertEquals("Column 3",  origin3, info.columnOrigins[3]);
        assertEquals("Column 4",  origin4, info.columnOrigins[4]);
        assertEquals("Row 0",     0,       info.rowOrigins[0]);
        assertEquals("Row 1",     origin1, info.rowOrigins[1]);
        assertEquals("Row 2",     origin2, info.rowOrigins[2]);
        assertEquals("Row 3",     origin3, info.rowOrigins[3]);
        assertEquals("Row 4",     origin4, info.rowOrigins[4]);
    }
}
