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

import java.awt.Insets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates the three FormLayout component sizes: minimum, default and
 * preferred.
 * Min and Pref measure the components minimum and preferred size, where the
 * Default size behaves like Pref but shrinks if the container space is scarce.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.19 $
 */
public final class ComponentSizesExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(ComponentSizesExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Component Sizes");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            true,
            buildCombinedPanel(),
            buildTextPanel());
        return splitPane;
    }


    /**
     * Builds and returns the panel that combines the three sizing panels.
     *
     * @return the combined panel
     */
    private JComponent buildCombinedPanel() {
        FormLayout layout = new FormLayout(
            "30dlu:grow",
            "pref, 3dlu, pref, 3dlu, pref");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.add(buildMinimumSizePanel(),   cc.xy(1, 1));
        builder.add(buildDefaultSizePanel(),   cc.xy(1, 3));
        builder.add(buildPreferredSizePanel(), cc.xy(1, 5));

        return builder.getPanel();
    }


    private JComponent buildMinimumSizePanel() {
        FormLayout layout = new FormLayout(
                "[25dlu,pref], 3dlu, min",
                "pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append("Min", new JTextField(15));
        return builder.getPanel();
    }

    private JComponent buildDefaultSizePanel() {
        FormLayout layout = new FormLayout(
                "[25dlu,pref], 3dlu, default",
                "pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append("Default", new JTextField(15));
        return builder.getPanel();
    }

    private JComponent buildPreferredSizePanel() {
        FormLayout layout = new FormLayout(
                "[25dlu,pref], 3dlu, pref",
                "pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append("Pref", new JTextField(15));
        return builder.getPanel();
    }

    private JComponent buildTextPanel() {
        JTextArea textArea = new JTextArea(5, 20);
        textArea.setMargin(new Insets(6, 10, 4, 6));
        // Non-editable but shall use the editable background.
        // textArea.setEditable(false);
        textArea.putClientProperty("JTextArea.infoBackground", Boolean.TRUE);
        textArea.setText("The text field used in the example on the left\n" +
        "has a narrow minimum width and a wider preferred width.\n\n" +
        "If you move the split divider to the left and right\n" +
        "you can see how 'Default' shrinks the field if space is scarce.\n\n" +
        "If there's not enough space for the preferred width\n" +
        "the bottom field will be 'cut' on the right-hand side.");
        JScrollPane scrollpane = new JScrollPane(textArea);
        scrollpane.setBorder(new EmptyBorder(0, 0, 0, 0));
        return scrollpane;
    }


}
