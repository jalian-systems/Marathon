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
 * Compares approaches how to append a custom area at the end of
 * a panel built with the DefaultFormBuilder:<ol>
 * <li> using two custom rows to align the leading label,
 * <li> using a single custom row with label on top,
 * <li> using a separator.
 * </ol>
 * These differ in the position of the leading 'Feedback" label,
 * and in turn in the alignment of font baselines between label
 * and the text area.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.19 $
 *
 * @see     DefaultFormBuilder
 * @see     DefaultFormWithCustomRowsExample
 */
public final class DefaultFormWithCustomAreasExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(DefaultFormWithCustomAreasExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Custom Areas");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add(buildCustomAreaWithAlignedLabelPanel(), "Aligned label");
        tabbedPane.add(buildCustomAreaWithTopLabelPanel(),     "Top label");
        tabbedPane.add(buildCustomAreaWithSeparatorPanel(),    "Separator");
        return tabbedPane;
    }


    private DefaultFormBuilder buildPanelHeader() {
        // Column specs only, rows will be added dynamically.
        FormLayout layout = new FormLayout("right:pref, 3dlu, min:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setRowGroupingEnabled(true);

        builder.appendSeparator("Customer Data");
        builder.append("Last Name:",  new JTextField());
        builder.append("First Name:", new JTextField());
        builder.append("Street:",     new JTextField());
        builder.append("Email:",      new JTextField());

        return builder;
    }


    /**
     * Demonstrates how to append a larger custom area at the end of
     * a panel that is build with a {@link DefaultFormBuilder}.<p>
     *
     * We add a gap and a single custom row that grows and that
     * is filled vertically (where the default is center vertically).
     * The area uses a standard leading label.
     *
     * @return the custom area panel with aligned labels
     */
    private JComponent buildCustomAreaWithAlignedLabelPanel() {
        DefaultFormBuilder builder = buildPanelHeader();

        CellConstraints cc = new CellConstraints();
        builder.append("Feedback:");
        builder.appendRow("0:grow");
        builder.add(new JScrollPane(new JTextArea("Feedback - font baselines shall be aligned")),
                    cc.xywh(builder.getColumn(), builder.getRow(), 1, 2, "fill, fill"));

        return builder.getPanel();
    }


    /**
     * Demonstrates how to append two custom areas at the end of
     * a panel that is build with a DefaultFormBuilder.
     *
     * @return the custom area panel with label in the top
     */
    private JComponent buildCustomAreaWithTopLabelPanel() {
        DefaultFormBuilder builder = buildPanelHeader();

        CellConstraints cc = new CellConstraints();
        builder.appendRow(builder.getLineGapSpec());
        builder.appendRow("top:28dlu:grow");
        builder.nextLine(2);
        builder.append("Feedback:");
        builder.add(new JScrollPane(new JTextArea("Feedback - likely the baselines are not aligned")),
                    cc.xy(builder.getColumn(), builder.getRow(), "fill, fill"));

        return builder.getPanel();
    }


    /**
     * Demonstrates how to append a larger custom area at the end of
     * a panel that is build with a DefaultFormBuilder.<p>
     *
     * We add a gap and a single custom row that grows and that
     * is filled vertically (where the default is center vertically).
     * The area is separated by a titled separator and it is indented
     * using an empty leading label.
     *
     * @return the custom area panel with separators
     */
    private JComponent buildCustomAreaWithSeparatorPanel() {
        DefaultFormBuilder builder = buildPanelHeader();

        builder.appendSeparator("Customer Feedback");
        builder.appendRow(builder.getLineGapSpec());
        builder.appendRow("fill:28dlu:grow");
        builder.nextLine(2);
        builder.append("", new JScrollPane(new JTextArea()));

        return builder.getPanel();
    }


 }
