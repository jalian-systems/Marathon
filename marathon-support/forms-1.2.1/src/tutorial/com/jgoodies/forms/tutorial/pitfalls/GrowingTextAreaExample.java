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

package com.jgoodies.forms.tutorial.pitfalls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates how a JTextArea's preferred size grows with the container
 * if no columns and rows are set.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.14 $
 */
public final class GrowingTextAreaExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(GrowingTextAreaExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Growing Text Area");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        String example1Text =
            "Here the layout uses a fixed initial size of 200 dlu. "
          + "The area's minimum and preferred sizes will be ignored. "
          + "And so, the area will grow and shrink.";

        tabbedPane.add("1",
              buildTab("Fixed Size (Good)",
                       "fill:200dlu:grow",
                       createArea(example1Text, true, 0, null)));

        String example2Text =
            "This text area has line wrapping disabled\n"
          + "and uses a hand-wrapped text (using '\\n').\n\n"
          + "Its minimum and preferred sizes are constant and so,\n"
          + "the area will grow but shrink down to its minimum size.";

        tabbedPane.add("2",
              buildTab("Pref Size, Line Wrap Disabled (Good)",
                       "fill:pref:grow",
                       createArea(example2Text, false, 0, null)));

        String example3Text =
                  "This text area grows horizontally and will never shrink again. "
                + "Since line wrapping is enabled, "
                + "the area's preferred size is defined by its size. "
                + "(See BasicTextAreaUI#getPreferredSize(Component).\n\n"
                + "If the layout container grows, the layout manager "
                + "sets a larger size, and hence, the preferred size grows."
                + "The FormLayout honors the area's preferred size to compute "
                + "the new column width, and so the area won't shrink again.\n\n"
                + "Even if you use a 'default' width the column won't shrink.";

        tabbedPane.add("3",
                buildTab("Pref Size, Line Wrap Enabled (Never Shrinks)",
                         "fill:pref:grow",
                         createArea(example3Text, true, 0, null)));

        String example4Text =
            "This text area grows but never shrinks. "
            + "Since line wrapping is enabled, the area's "
            + "minimum and preferred sizes are defined by its size.\n\n"
            + "If the layout container grows, the layout manager "
            + "sets a larger size, and hence, the minimum and preferred sizes grow. "
            + "If the layout container shrinks, the layout manager can shrink "
            + "the column width down to the area's minimum width. "
            + "But the minimum size is like the preferred size determined "
            + "by the size previously set, and so, the column won't shrink.\n\n"
            + "A solution to this problem is to set a custom minimum size.";

        tabbedPane.add("4",
                buildTab("Default Size, Line Wrap Enabled (Never Shrinks)",
                         "fill:default:grow",
                         createArea(example4Text, true, 0, null)));

        String example5Text =
            "This text area has uses a column width of 30 characters. "
            + "But that just affects the initial preferred and minimum size."
            + "The area grows and won't shrink again - just as in tabs 3 and 4.";

        tabbedPane.add("5",
                buildTab("Default Size, Line Wrap Enabled, Columns Set (Never Shrinks)",
                         "fill:default:grow",
                         createArea(example5Text, true, 30, null)));

        String example6Text =
            "This text area grows and shrinks. "
            + "Since line wrapping is enabled, "
            + "the area's preferred size is defined by its size. "
            + "Here a custom minimum size (100, 32) has been set.\n\n"
            + "If the layout container grows, the layout manager "
            + "sets a larger size, and hence, the preferred size grows. "
            + "However, if the layout container shrinks, the layout can "
            + "shrink the column down to the area's minimum width, which is 100; "
            + "the minimum size is independent from the size previously set.";

        tabbedPane.add("6",
                buildTab("Default Size, Line Wrap Enabled, Min Size Set (Good)",
                         "fill:default:grow",
                         createArea(example6Text, true, 0, new Dimension(100, 32))));

        return tabbedPane;
    }


    private JTextArea createArea(
            String text,
            boolean lineWrap,
            int columns,
            Dimension minimumSize) {
        JTextArea area  = new JTextArea(text);
        area.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1)));
        area.setLineWrap(lineWrap);
        area.setWrapStyleWord(true);
        area.setColumns(columns);
        if (minimumSize != null) {
            area.setMinimumSize(new Dimension(100, 32));
        }
        return area;
    }


    private JComponent buildTab(String title, String columnSpec, JTextArea area) {
        JLabel columnSpecLabel = new JLabel(columnSpec);
        columnSpecLabel.setHorizontalAlignment(JLabel.CENTER);

        FormLayout layout = new FormLayout(
                columnSpec,
                "pref, 9dlu, pref, 3dlu, fill:default:grow, 9dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        builder.addTitle(title,           cc.xy(1, 1));
        builder.add(columnSpecLabel,      cc.xy(1, 3));
        builder.add(area,                 cc.xy(1, 5));
        builder.add(buildInfoPanel(area), cc.xy(1, 7));

        return builder.getPanel();
    }


    private JComponent buildInfoPanel(JTextArea area) {
        JLabel sizeLabel      = new JLabel();
        JLabel minSizeLabel   = new JLabel();
        JLabel prefSizeLabel  = new JLabel();
        JLabel lineWrapLabel  = new JLabel(area.getLineWrap() ? "enabled" : "disabled");
        JLabel customMinLabel = new JLabel(area.isMinimumSizeSet() ? "set" : "computed");
        JLabel columnsLabel   = new JLabel(area.getColumns() == 0
                                            ? "not specified"
                                            : String.valueOf(area.getColumns()));

        area.addComponentListener(new SizeChangeHandler(
                area, sizeLabel, minSizeLabel, prefSizeLabel));

        FormLayout layout = new FormLayout("pref, 4dlu, pref, 21dlu, pref, 4dlu, pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append("Size:",      sizeLabel);
        builder.append("Line wrap:", lineWrapLabel);

        builder.append("Min size:",  minSizeLabel);
        builder.append("Min size:", customMinLabel);

        builder.append("Pref size:", prefSizeLabel);
        builder.append("Columns:",   columnsLabel);
        return builder.getPanel();
    }


    /**
     * Listens to area size changes and writes the formatted sizes to the given labels.
     */
    private static final class SizeChangeHandler extends ComponentAdapter {

        private final JTextArea area;
        private final JLabel    sizeLabel;
        private final JLabel    minSizeLabel;
        private final JLabel    prefSizeLabel;

        private SizeChangeHandler(
                JTextArea area,
                JLabel sizeLabel,
                JLabel minSizeLabel,
                JLabel prefSizeLabel) {
            this.area = area;
            this.sizeLabel = sizeLabel;
            this.minSizeLabel = minSizeLabel;
            this.prefSizeLabel = prefSizeLabel;
        }

        public void componentResized(ComponentEvent evt) {
            sizeLabel.setText(format(area.getSize()));
            minSizeLabel.setText(format(area.getMinimumSize()));
            prefSizeLabel.setText(format(area.getPreferredSize()));
        }

        private String format(Dimension d) {
            return String.valueOf(d.width) + ", " + d.height;
        }

    }



}
