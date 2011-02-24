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
 * Demonstrates the basic FormLayout sizes: constant, minimum, preferred.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.18 $
 */
public final class BasicSizesExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(BasicSizesExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Basic Sizes");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add("Horizontal", buildHorizontalSizesPanel());
        tabbedPane.add("Vertical",   buildVerticalSizesPanel());
        return tabbedPane;
    }


    private JComponent buildHorizontalSizesPanel() {
        FormLayout layout = new FormLayout(
            "pref, 12px, " + "75px, 25px, min, 25px, pref",
            "pref, 12px, pref");

        // Create a panel that uses the layout.
        JPanel panel = new JPanel(layout);

        // Set a default border.
        panel.setBorder(Borders.DIALOG_BORDER);

        // Create a reusable CellConstraints instance.
        CellConstraints cc = new CellConstraints();

        // Add components to the panel.
        panel.add(new JLabel("75px"),  cc.xy(3, 1));
        panel.add(new JLabel("Min"),   cc.xy(5, 1));
        panel.add(new JLabel("Pref"),  cc.xy(7, 1));

        panel.add(new JLabel("new JTextField(15)"),  cc.xy(1, 3));

        panel.add(new JTextField(15),  cc.xy(3, 3));
        panel.add(new JTextField(15),  cc.xy(5, 3));
        panel.add(new JTextField(15),  cc.xy(7, 3));

        return panel;
    }


    private JComponent buildVerticalSizesPanel() {
        FormLayout layout = new FormLayout(
            "pref, 12px, pref",
            "pref, 12px, 45px, 12px, min, 12px, pref");

        // Create a panel that uses the layout.
        JPanel panel = new JPanel(layout);

        // Set a default border.
        panel.setBorder(Borders.DIALOG_BORDER);

        // Create a reusable CellConstraints instance.
        CellConstraints cc = new CellConstraints();

        // Add components to the panel.
        panel.add(new JLabel("new JTextArea(10, 40)"), cc.xy(3, 1));

        panel.add(new JLabel("45px"),     cc.xy(1, 3));
        panel.add(new JLabel("Min"),      cc.xy(1, 5));
        panel.add(new JLabel("Pref"),     cc.xy(1, 7));

        panel.add(createTextArea(10, 40), cc.xy(3, 3));
        panel.add(createTextArea(10, 40), cc.xy(3, 5));
        panel.add(createTextArea(10, 40), cc.xy(3, 7));

        return panel;
    }

    private JComponent createTextArea(int rows, int cols) {
        return new JScrollPane(new JTextArea(rows, cols),
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }


    // Helper Classes *********************************************************

    /**
     * Creates and returns a button that can have predefined minimum
     * and preferred sizes. In the constructor you can specify or omit
     * the minimum width, height and preferred width/height.
     */
//    private static class SpecialSizeButton extends JButton {
//
//        private final Dimension fixedMinimumSize;
//        private final Dimension fixedPreferredSize;
//
//        private SpecialSizeButton(
//            String text,
//            Dimension fixedMinimumSize,
//            Dimension fixedPreferredSize) {
//            super(text);
//            this.fixedMinimumSize   = fixedMinimumSize;
//            this.fixedPreferredSize = fixedPreferredSize;
//            //putClientProperty("jgoodies.isNarrow", Boolean.TRUE);
//        }
//
//        public Dimension getMinimumSize() {
//            Dimension d = super.getMinimumSize();
//            return new Dimension(
//                fixedMinimumSize.width  == -1
//                    ? d.width
//                    : fixedMinimumSize.width,
//                fixedMinimumSize.height == -1
//                    ? d.height
//                    : fixedMinimumSize.height);
//        }
//
//        public Dimension getPreferredSize() {
//            Dimension d = super.getPreferredSize();
//            return new Dimension(
//                fixedPreferredSize.width  == -1
//                    ? d.width
//                    : fixedPreferredSize.width,
//                fixedPreferredSize.height == -1
//                    ? d.height
//                    : fixedPreferredSize.height);
//        }
//    }


}
