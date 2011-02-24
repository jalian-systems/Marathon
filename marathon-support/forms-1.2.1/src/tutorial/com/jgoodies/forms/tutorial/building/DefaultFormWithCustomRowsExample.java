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

package com.jgoodies.forms.tutorial.building;

import javax.swing.*;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Shows three approaches how to add custom rows to a form that is built
 * using a DefaultFormBuilder.<ol>
 * <li> single custom row,
 * <li> standard + custom row,
 * <li> multiple standard rows.
 * </ol>
 * These differ in the position of the 'Comment' label, the alignment
 * of font baselines and the height of the custom row.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.18 $
 *
 * @see    DefaultFormBuilder
 * @see    DefaultFormWithCustomAreasExample
 */
public final class DefaultFormWithCustomRowsExample extends TutorialApplication {

    private JTextField name1Field;
    private JTextArea  comment1Area;
    private JTextField name2Field;
    private JTextArea  comment2Area;
    private JTextField name3Field;
    private JTextArea  comment3Area;


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(DefaultFormWithCustomRowsExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Custom Rows");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Component Creation and Initialization **********************************

    /**
     *  Creates and initializes the UI components.
     */
    private void initComponents() {
        name1Field     = new JTextField("Name - font baselines shall be aligned");
        name2Field     = new JTextField("Name - font baselines shall be aligned");
        name3Field     = new JTextField("Name - font baselines shall be aligned");
        comment1Area   = new JTextArea(2, 20);
        comment2Area   = new JTextArea(2, 20);
        comment3Area   = new JTextArea(2, 20);
        comment1Area.setText("Comment - likely baselines are unaligned");
        comment2Area.setText("Comment - baselines shall be aligned");
        comment3Area.setText("Comment - baselines shall be aligned");
    }


    // Building ***************************************************************

    /**
     * Demonstrates three different ways to add custom rows to a form
     * that is build with a {@link DefaultFormBuilder}.
     *
     * @return the built panel
     */
    public JComponent buildPanel() {
        initComponents();

        // Column specs only, rows will be added dynamically.
        FormLayout layout = new FormLayout("right:pref, 3dlu, min:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setRowGroupingEnabled(true);

        CellConstraints cc = new CellConstraints();

        // In this approach, we add a gap and a custom row.
        // The advantage of this approach is, that we can express
        // the row spec and comment area cell constraints freely.
        // The disadvantage is the misalignment of the leading label.
        // Also the row's height may be inconsistent with other rows.
        builder.appendSeparator("Single Custom Row");
        builder.append("Name:", name1Field);
        builder.appendRow(builder.getLineGapSpec());
        builder.appendRow("top:31dlu"); // Assumes line is 14, gap is 3
        builder.nextLine(2);
        builder.append("Comment:");
        builder.add(new JScrollPane(comment1Area),
                    cc.xy(builder.getColumn(), builder.getRow(), "fill, fill"));
        builder.nextLine();

        // In this approach, we append a standard row with gap before it.
        // The advantage is, that the leading label is aligned well.
        // The disadvantage is that the comment area now spans
        // multiple cells and is slightly less flexible.
        // Also the row's height may be inconsistent with other rows.
        builder.appendSeparator("Standard + Custom Row");
        builder.append("Name:", name2Field);
        builder.append("Comment:");
        builder.appendRow("17dlu"); // Assumes line is 14, gap is 3
        builder.add(new JScrollPane(comment2Area),
                    cc.xywh(builder.getColumn(), builder.getRow(), 1, 2));
        builder.nextLine(2);

        // In this approach, we append two standard rows with associated gaps.
        // The advantage is, that the leading label is aligned well,
        // and the height is consistent with other rows.
        // The disadvantage is that the comment area now spans
        // multiple cells and is slightly less flexible.
        builder.appendSeparator("Two Standard Rows");
        builder.append("Name:", name3Field);
        builder.append("Comment:");
        builder.nextLine();
        builder.append("");
        builder.nextRow(-2);
        builder.add(new JScrollPane(comment3Area),
                    cc.xywh(builder.getColumn(), builder.getRow(), 1, 3));

        return builder.getPanel();
    }


}
