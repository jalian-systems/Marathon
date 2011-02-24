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
 * Demonstrates how columns and rows can be grouped in FormLayout.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.22 $
 */
public final class GroupingExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(GroupingExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Grouping");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add("Ungrouped Bar",   buildWizardBar(false));
        tabbedPane.add("Grouped Bar",     buildWizardBar(true));
        tabbedPane.add("Ungrouped Rows",  buildEditorPanel(false));
        tabbedPane.add("Grouped Rows",    buildEditorPanel(true));
        return tabbedPane;
    }


    private JComponent buildWizardBar(boolean grouped) {
        FormLayout layout = new FormLayout(
            "pref, 6px:grow, pref, pref, 12px, pref, 6px, pref",
            "pref");
        if (grouped) {
            layout.setColumnGroups(new int[][]{{1, 3, 4, 6, 8}});
        }
        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JButton("Hilfe"),         cc.xy(1, 1));
        panel.add(new JButton("< Zur\u00FCck"), cc.xy(3, 1));
        panel.add(new JButton("Vor >"),         cc.xy(4, 1));
        panel.add(new JButton("Beenden"),       cc.xy(6, 1));
        panel.add(new JButton("Abbrechen"),     cc.xy(8, 1));

        return panel;
    }


    private JComponent buildEditorPanel(boolean grouped) {
        FormLayout layout = new FormLayout(
                "pref, 3dlu, 35dlu, 2dlu, 35dlu, 2dlu, 35dlu, 2dlu, 35dlu",
                "8*(p, 2dlu), p");
        if (grouped) {
            layout.setRowGroups(new int[][] { { 1, 3, 5, 7, 9, 11, 13, 15, 17 } });
        }

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel("File number:"),       cc.xy (1,  1));
        panel.add(new JTextField(),                 cc.xyw(3,  1, 7));
        panel.add(new JLabel("BL/MBL number:"),     cc.xy (1,  3));
        panel.add(new JTextField(),                 cc.xy (3,  3));
        panel.add(new JTextField(),                 cc.xy (5,  3));
        panel.add(new JLabel("Entry date:"),        cc.xy (1,  5));
        panel.add(new JTextField(),                 cc.xy (3,  5));
        panel.add(new JLabel("RFQ number:"),        cc.xy (1,  7));
        panel.add(new JTextField(),                 cc.xyw(3,  7, 7));
        panel.add(new JLabel("Goods:"),             cc.xy (1,  9));
        panel.add(new JCheckBox("Dangerous"),       cc.xyw(3,  9, 7));
        panel.add(new JLabel("Shipper:"),           cc.xy (1, 11));
        panel.add(new JTextField(),                 cc.xyw(3, 11, 7));
        panel.add(new JLabel("Customer:"),          cc.xy (1, 13));
        panel.add(new JTextField(),                 cc.xyw(3, 13, 5));
        panel.add(new JButton("\u2026"),            cc.xy (9, 13));
        panel.add(new JLabel("Port of loading:"),   cc.xy (1, 15));
        panel.add(new JTextField(),                 cc.xyw(3, 15, 7));
        panel.add(new JLabel("Destination:"),       cc.xy (1, 17));
        panel.add(new JTextField(),                 cc.xyw(3, 17, 7));

        return panel;
    }


}
