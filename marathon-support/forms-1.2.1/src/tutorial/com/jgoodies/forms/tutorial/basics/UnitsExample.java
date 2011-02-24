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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates the different sizing units provided by the FormLayout:
 * Pixel, Dialog Units (dlu), Point, Millimeter, Centimeter and Inches.
 * Pt, mm, cm, in honor the screen resolution; dlus honor the font size too.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.22 $
 */
public final class UnitsExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(UnitsExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Units");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add("Horizontal",     buildHorizontalUnitsPanel());
        tabbedPane.add("Horizontal Dlu", buildHorizontalDluPanel());
        tabbedPane.add("Vertical",       buildVerticalUnitsPanel());
        tabbedPane.add("Vertical Dlu",   buildVerticalDluPanel());
        return tabbedPane;
    }


    private JComponent buildHorizontalUnitsPanel() {
        FormLayout layout = new FormLayout(
            "right:pref, 1dlu, left:pref, 4dlu, left:pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.append("72",   new JLabel("pt"), buildHorizontalPanel("72pt"));
        builder.append("25.4", new JLabel("mm"), buildHorizontalPanel("25.4mm"));
        builder.append("2.54", new JLabel("cm"), buildHorizontalPanel("2.54cm"));
        builder.append("1",    new JLabel("in"), buildHorizontalPanel("1in"));
        builder.append("72",   new JLabel("px"), buildHorizontalPanel("72px"));
        builder.append("96",   new JLabel("px"), buildHorizontalPanel("96px"));
        builder.append("120",  new JLabel("px"), buildHorizontalPanel("120px"));

        return builder.getPanel();
    }


    private JComponent buildHorizontalDluPanel() {
        FormLayout layout = new FormLayout(
            "right:pref, 1dlu, left:pref, 4dlu, left:pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.append("9",  new JLabel("cols"), buildHorizontalPanel(9));
        builder.append("50", new JLabel("dlu"),  buildHorizontalPanel("50dlu"));
        builder.append("75", new JLabel("px"),   buildHorizontalPanel("75px"));
        builder.append("88", new JLabel("px"),   buildHorizontalPanel("88px"));
        builder.append("100",new JLabel("px"),   buildHorizontalPanel("100px"));

        return builder.getPanel();
    }


    private JComponent buildVerticalUnitsPanel() {
        FormLayout layout = new FormLayout(
            "6*(c:p, 4dlu), c:p",
            "pref, 6dlu, top:pref");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("72 pt"),            cc.xy(1, 1));
        panel.add(buildVerticalPanel("72pt"),     cc.xy(1, 3));

        panel.add(new JLabel("25.4 mm"),          cc.xy(3, 1));
        panel.add(buildVerticalPanel("25.4mm"),   cc.xy(3, 3));

        panel.add(new JLabel("2.54 cm"),          cc.xy(5, 1));
        panel.add(buildVerticalPanel("2.54cm"),   cc.xy(5, 3));

        panel.add(new JLabel("1 in"),             cc.xy(7, 1));
        panel.add(buildVerticalPanel("1in"),      cc.xy(7, 3));

        panel.add(new JLabel("72 px"),            cc.xy(9, 1));
        panel.add(buildVerticalPanel("72px"),     cc.xy(9, 3));

        panel.add(new JLabel("96 px"),           cc.xy(11, 1));
        panel.add(buildVerticalPanel("96px"),    cc.xy(11, 3));

        panel.add(new JLabel("120 px"),           cc.xy(13, 1));
        panel.add(buildVerticalPanel("120px"),    cc.xy(13, 3));

        return panel;
    }

    private JComponent buildVerticalDluPanel() {
        FormLayout layout = new FormLayout(
            "6*(c:p, 4dlu), c:p",
            "pref, 6dlu, top:pref, 25dlu, pref, 6dlu, top:pref");

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("4 rows"),           cc.xy(1, 1));
        panel.add(buildVerticalPanel("pref", 4),  cc.xy(1, 3));

        panel.add(new JLabel("42 dlu"),           cc.xy(3, 1));
        panel.add(buildVerticalPanel("42dlu", 4), cc.xy(3, 3));

        panel.add(new JLabel("57 px"),            cc.xy(5, 1));
        panel.add(buildVerticalPanel("57px", 4),  cc.xy(5, 3));

        panel.add(new JLabel("63 px"),            cc.xy(7, 1));
        panel.add(buildVerticalPanel("63px", 4),  cc.xy(7, 3));

        panel.add(new JLabel("68 px"),            cc.xy(9, 1));
        panel.add(buildVerticalPanel("68px", 4),  cc.xy(9, 3));


        panel.add(new JLabel("field"),            cc.xy(1, 5));
        panel.add(new JTextField(4),              cc.xy(1, 7));

        panel.add(new JLabel("14 dlu"),           cc.xy(3, 5));
        panel.add(buildVerticalPanel("14dlu"),    cc.xy(3, 7));

        panel.add(new JLabel("21 px"),            cc.xy(5, 5));
        panel.add(buildVerticalPanel("21px"),     cc.xy(5, 7));

        panel.add(new JLabel("23 px"),            cc.xy(7, 5));
        panel.add(buildVerticalPanel("23px"),     cc.xy(7, 7));

        panel.add(new JLabel("24 px"),            cc.xy(9, 5));
        panel.add(buildVerticalPanel("24px"),     cc.xy(9, 7));

        panel.add(new JLabel("button"),           cc.xy(11, 5));
        panel.add(new JButton("..."),             cc.xy(11, 7));

        return panel;
    }


    // Component Creation *****************************************************

    private JComponent createTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        //area.setText(text);
        return new JScrollPane(area,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }


    // Helper Code ************************************************************

    private JComponent buildHorizontalPanel(String width) {
        FormLayout layout = new FormLayout(width, "pref");
        JPanel panel = new JPanel(layout);
        panel.add(new JTextField(), new CellConstraints(1, 1));
        return panel;
    }

    private JComponent buildHorizontalPanel(int columns) {
        FormLayout layout = new FormLayout("pref, 4dlu, pref", "pref");
        JPanel panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        panel.add(new JTextField(columns),
                  cc.xy(1, 1));
        panel.add(new JLabel("Width of new JTextField(" + columns + ")"),
                  cc.xy(3, 1));
        return panel;
    }

    private JComponent buildVerticalPanel(String height) {
        return buildVerticalPanel(height, 10);
    }

    private JComponent buildVerticalPanel(String height, int rows) {
        FormLayout layout = new FormLayout("pref", "fill:"+ height);
        JPanel panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        panel.add(createTextArea(rows, 5), cc.xy(1, 1));
        return panel;
    }


}
