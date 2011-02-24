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

package com.jgoodies.forms.tutorial.basics;

import javax.swing.*;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates how FormLayout applies the default column and row
 * alignments to cells, and how to override the defaults.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.19 $
 */
public final class CellAlignmentExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(CellAlignmentExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Cell Alignments");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add(buildHorizontalPanel(), "Horizontal");
        tabbedPane.add(buildVerticalPanel(),   "Vertical");
        return tabbedPane;
    }


    private JComponent buildHorizontalPanel() {
        FormLayout layout = new FormLayout(
                        "r:p, 4dlu, left:pref:g, center:pref:g, right:pref:g, pref:g",
                        "pref, 8dlu, pref, pref, pref, pref, pref");
        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);

        panel.add(new JLabel("Column Spec: "),          "1, 1, r, c");
        panel.add(new JLabel(" \"left:pref:grow\" "),   "3, 1, c, c");
        panel.add(new JLabel(" \"center:pref:grow\" "), "4, 1, c, c");
        panel.add(new JLabel(" \"right:pref:grow\" "),  "5, 1, c, c");
        panel.add(new JLabel(" \"pref:grow\" "),        "6, 1, c, c");

        int row = 3;
        addHorizontalButton(panel, 3, row, CellConstraints.DEFAULT);
        addHorizontalButton(panel, 4, row, CellConstraints.DEFAULT);
        addHorizontalButton(panel, 5, row, CellConstraints.DEFAULT);
        addHorizontalButton(panel, 6, row, CellConstraints.DEFAULT);

        row++;
        addHorizontalButton(panel, 3, row, CellConstraints.FILL);
        addHorizontalButton(panel, 4, row, CellConstraints.FILL);
        addHorizontalButton(panel, 5, row, CellConstraints.FILL);
        addHorizontalButton(panel, 6, row, CellConstraints.FILL);

        row++;
        addHorizontalButton(panel, 3, row, CellConstraints.LEFT);
        addHorizontalButton(panel, 4, row, CellConstraints.LEFT);
        addHorizontalButton(panel, 5, row, CellConstraints.LEFT);
        addHorizontalButton(panel, 6, row, CellConstraints.LEFT);

        row++;
        addHorizontalButton(panel, 3, row, CellConstraints.CENTER);
        addHorizontalButton(panel, 4, row, CellConstraints.CENTER);
        addHorizontalButton(panel, 5, row, CellConstraints.CENTER);
        addHorizontalButton(panel, 6, row, CellConstraints.CENTER);

        row++;
        addHorizontalButton(panel, 3, row, CellConstraints.RIGHT);
        addHorizontalButton(panel, 4, row, CellConstraints.RIGHT);
        addHorizontalButton(panel, 5, row, CellConstraints.RIGHT);
        addHorizontalButton(panel, 6, row, CellConstraints.RIGHT);

        return panel;
    }


    private JComponent buildVerticalPanel() {
        FormLayout layout = new FormLayout(
                        "left:pref, 8dlu, p, c:p, p, p, p",
                        "p, 4dlu, top:pref:g, center:pref:g, bottom:pref:g, pref:g");
        layout.setColumnGroups(new int[][] {{3, 5, 6, 7}});
        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);

        panel.add(new JLabel("Row Spec:"),             "1, 1, r, c");
        panel.add(new JLabel("\"top:pref:grow\""),     "1, 3, r, c");
        panel.add(new JLabel("\"center:pref:grow\""), "1, 4, r, c");
        panel.add(new JLabel("\"bottom:pref:grow\""),  "1, 5, r, c");
        panel.add(new JLabel("\"pref:grow\""),         "1, 6, r, c");

        int col = 3;
        addVerticalButton(panel, col, 3, CellConstraints.DEFAULT);
        addVerticalButton(panel, col, 4, CellConstraints.DEFAULT);
        addVerticalButton(panel, col, 5, CellConstraints.DEFAULT);
        addVerticalButton(panel, col, 6, CellConstraints.DEFAULT);

        col++;
        addVerticalButton(panel, col, 3, CellConstraints.FILL);
        addVerticalButton(panel, col, 4, CellConstraints.FILL);
        addVerticalButton(panel, col, 5, CellConstraints.FILL);
        addVerticalButton(panel, col, 6, CellConstraints.FILL);

        col++;
        addVerticalButton(panel, col, 3, CellConstraints.TOP);
        addVerticalButton(panel, col, 4, CellConstraints.TOP);
        addVerticalButton(panel, col, 5, CellConstraints.TOP);
        addVerticalButton(panel, col, 6, CellConstraints.TOP);

        col++;
        addVerticalButton(panel, col, 3, CellConstraints.CENTER);
        addVerticalButton(panel, col, 4, CellConstraints.CENTER);
        addVerticalButton(panel, col, 5, CellConstraints.CENTER);
        addVerticalButton(panel, col, 6, CellConstraints.CENTER);

        col++;
        addVerticalButton(panel, col, 3, CellConstraints.BOTTOM);
        addVerticalButton(panel, col, 4, CellConstraints.BOTTOM);
        addVerticalButton(panel, col, 5, CellConstraints.BOTTOM);
        addVerticalButton(panel, col, 6, CellConstraints.BOTTOM);

        return panel;
    }


    private void addHorizontalButton(JPanel panel, int col, int row,
                                    CellConstraints.Alignment hAlignment) {
        JButton button = new JButton(hAlignment.toString());
        panel.add(button, new CellConstraints(col, row,
                                              hAlignment,
                                              CellConstraints.DEFAULT));
    }


    private void addVerticalButton(JPanel panel, int col, int row,
                                    CellConstraints.Alignment vAlignment) {
        JButton button = new JButton(vAlignment.toString());
        panel.add(button, new CellConstraints(col, row,
                                              CellConstraints.DEFAULT,
                                              vAlignment));
    }


}
