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
 * Demonstrates the FormLayout growing options: none, default, weighted.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.18 $
 */
public final class GrowingExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(GrowingExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Growing");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add("All",        buildHorizontalAllExtraSpacePanel());
        tabbedPane.add("Half",       buildHorizontalHalfAndHalfPanel());
        tabbedPane.add("Percent",    buildHorizontalPercentMixedPanel());
        tabbedPane.add("Percent 2",  buildHorizontalPercentPanel());
        tabbedPane.add("Vertical 1", buildVerticalGrowing1Panel());
        tabbedPane.add("Vertical 2", buildVerticalGrowing2Panel());
        return tabbedPane;
    }


    private JComponent buildHorizontalAllExtraSpacePanel() {
        FormLayout layout = new FormLayout(
            "pref, 6px, pref:grow",
            "pref, 12px, pref");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("Fixed"),  cc.xy(1, 1));
        panel.add(new JLabel("Gets all extra space"),  cc.xy(3, 1));

        panel.add(new JTextField(5),   cc.xy(1, 3));
        panel.add(new JTextField(5),   cc.xy(3, 3));

        return panel;
    }


    private JComponent buildHorizontalHalfAndHalfPanel() {
        FormLayout layout = new FormLayout(
            "pref, 6px, 0:grow, 6px, 0:grow",
            "pref, 12px, pref");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("Fixed"),  cc.xy(1, 1));
        panel.add(new JLabel("Gets half of extra space"),  cc.xy(3, 1));
        panel.add(new JLabel("gets half of extra space"),  cc.xy(5, 1));

        panel.add(new JTextField(5),   cc.xy(1, 3));
        panel.add(new JTextField(5),   cc.xy(3, 3));
        panel.add(new JTextField(5),   cc.xy(5, 3));

        return panel;
    }


    private JComponent buildHorizontalPercentMixedPanel() {
        FormLayout layout = new FormLayout(
            "pref, 6px, 0:grow(0.25), 6px, 0:grow(0.75)",
            "pref, 12px, pref");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("Fixed"),       cc.xy(1, 1));
        panel.add(new JLabel("Gets 25% of extra space"),  cc.xy(3, 1));
        panel.add(new JLabel("Gets 75% of extra space"),  cc.xy(5, 1));

        panel.add(new JTextField(5),        cc.xy(1, 3));
        panel.add(new JTextField(5),        cc.xy(3, 3));
        panel.add(new JTextField(5),        cc.xy(5, 3));

        return panel;
    }


    private JComponent buildHorizontalPercentPanel() {
        FormLayout layout = new FormLayout(
            "pref:grow(0.33), 6px, pref:grow(0.67)",
            "pref, 12px, pref");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("Gets 33% of the space"),    cc.xy(1, 1));
        panel.add(new JLabel("Gets 67% of the space"),    cc.xy(3, 1));

        panel.add(new JTextField(5),   cc.xy(1, 3));
        panel.add(new JTextField(5),   cc.xy(3, 3));

        return panel;
    }

    private JComponent buildVerticalGrowing1Panel() {
        FormLayout layout = new FormLayout(
            "pref, 12px, pref",
            "pref, 6px, fill:0:grow(0.25), 6px, fill:0:grow(0.75)");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("Fixed"),                   cc.xy(1, 1));
        panel.add(new JLabel("Gets 25% of extra space"), cc.xy(1, 3));
        panel.add(new JLabel("Gets 75% of extra space"), cc.xy(1, 5));

        panel.add(createTextArea(4, 30), cc.xy(3, 1));
        panel.add(createTextArea(4, 30), cc.xy(3, 3));
        panel.add(createTextArea(4, 30), cc.xy(3, 5));

        return panel;
    }

    private JComponent buildVerticalGrowing2Panel() {
        FormLayout layout = new FormLayout(
            "pref, 12px, pref",
            "fill:0:grow(0.25), 6px, fill:0:grow(0.75)");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("Gets 25% of extra space"), cc.xy(1, 1));
        panel.add(new JLabel("Gets 75% of extra space"), cc.xy(1, 3));

        panel.add(createTextArea(4, 30), cc.xy(3, 1));
        panel.add(createTextArea(4, 30), cc.xy(3, 3));

        return panel;
    }


    // Component Creation *****************************************************

    private JComponent createTextArea(int rows, int cols) {
        return new JScrollPane(new JTextArea(rows, cols),
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }


}
