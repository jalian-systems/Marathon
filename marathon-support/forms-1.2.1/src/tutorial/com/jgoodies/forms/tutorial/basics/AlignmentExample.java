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
 * Demonstrates the different FormLayout alignments.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.19 $
 */
public final class AlignmentExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(AlignmentExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Alignments");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add("Horizontal", buildHorizontalButtons());
        tabbedPane.add("Vertical",   buildVerticalButtons());
        return tabbedPane;
    }


    private JComponent buildHorizontalButtons() {
        FormLayout layout = new FormLayout(
            "left:pref, 15px, center:pref, 15px, right:pref, 15px, fill:pref, 15px, pref",
            "pref, 12px, pref, 4px, pref, 4px, pref, 4px, pref, 4px, pref");

        // Create a panel that uses the layout.
        JPanel panel = new JPanel(layout);

        // Set a default border.
        panel.setBorder(Borders.DIALOG_BORDER);

        // Create a reusable CellConstraints instance.
        CellConstraints cc = new CellConstraints();

        // Add components to the panel.
        panel.add(new JLabel("Left"),     cc.xy(1,  1));
        panel.add(new JButton("Name"),    cc.xy(1,  3));
        panel.add(new JButton("Phone"),   cc.xy(1,  5));
        panel.add(new JButton("Fax"),     cc.xy(1,  7));
        panel.add(new JButton("Email"),   cc.xy(1,  9));
        panel.add(new JButton("Address"), cc.xy(1, 11));

        panel.add(new JLabel("Center"),   cc.xy(3,  1));
        panel.add(new JButton("Name"),    cc.xy(3,  3));
        panel.add(new JButton("Phone"),   cc.xy(3,  5));
        panel.add(new JButton("Fax"),     cc.xy(3,  7));
        panel.add(new JButton("Email"),   cc.xy(3,  9));
        panel.add(new JButton("Address"), cc.xy(3, 11));

        panel.add(new JLabel("Right"),    cc.xy(5,  1));
        panel.add(new JButton("Name"),    cc.xy(5,  3));
        panel.add(new JButton("Phone"),   cc.xy(5,  5));
        panel.add(new JButton("Fax"),     cc.xy(5,  7));
        panel.add(new JButton("Email"),   cc.xy(5,  9));
        panel.add(new JButton("Address"), cc.xy(5, 11));

        panel.add(new JLabel("Fill"),     cc.xy(7,  1, "center, center"));
        panel.add(new JButton("Name"),    cc.xy(7,  3));
        panel.add(new JButton("Phone"),   cc.xy(7,  5));
        panel.add(new JButton("Fax"),     cc.xy(7,  7));
        panel.add(new JButton("Email"),   cc.xy(7,  9));
        panel.add(new JButton("Address"), cc.xy(7, 11));

        panel.add(new JLabel("Default"),  cc.xy(9,  1, "center, center"));
        panel.add(new JButton("Name"),    cc.xy(9,  3));
        panel.add(new JButton("Phone"),   cc.xy(9,  5));
        panel.add(new JButton("Fax"),     cc.xy(9,  7));
        panel.add(new JButton("Email"),   cc.xy(9,  9));
        panel.add(new JButton("Address"), cc.xy(9, 11));

        return panel;
    }


    private JComponent buildVerticalButtons() {
        FormLayout layout = new FormLayout(
            "pref, 8dlu, pref, 4dlu, pref",
            "top:pref, 9dlu, center:pref, 9dlu, bottom:pref, 9dlu, fill:pref, 9dlu, pref");

        // Create a panel that uses the layout.
        JPanel panel = new JPanel(layout);

        // Set a default border.
        panel.setBorder(Borders.DIALOG_BORDER);

        // Create a reusable CellConstraints instance.
        CellConstraints cc = new CellConstraints();

        // Add components to the panel.
        panel.add(new JLabel("Top"),      cc.xy(1,  1));
        panel.add(createSmallButton(),    cc.xy(3,  1));
        panel.add(createMediumButton(),   cc.xy(5,  1));

        panel.add(new JLabel("Center"),   cc.xy(1,  3));
        panel.add(createSmallButton(),    cc.xy(3,  3));
        panel.add(createMediumButton(),   cc.xy(5,  3));

        panel.add(new JLabel("Bottom"),   cc.xy(1,  5));
        panel.add(createSmallButton(),    cc.xy(3,  5));
        panel.add(createMediumButton(),   cc.xy(5,  5));

        panel.add(new JLabel("Fill"),     cc.xy(1,  7));
        panel.add(createSmallButton(),    cc.xy(3,  7));
        panel.add(createMediumButton(),   cc.xy(5,  7));

        panel.add(new JLabel("Default"),  cc.xy(1,  9));
        panel.add(createSmallButton(),    cc.xy(3,  9));
        panel.add(createMediumButton(),   cc.xy(5,  9));

        return panel;
    }

    private JButton createSmallButton() {
        return new JButton("<html>One</html>");
    }

    private JButton createMediumButton() {
        return new JButton("<html>One<br>Two</html>");
    }


}
