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

package com.jgoodies.forms.tutorial.factories;

import java.awt.Component;

import javax.swing.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates the use of Factories as provided by the Forms framework.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.18 $
 *
 * @see	ButtonBarFactory
 */
public final class ButtonBarFactoryExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(ButtonBarFactoryExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: ButtonBarFactory");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    public JComponent buildPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);

        tabbedPane.add(buildButtonBar1Panel(),          "Dialog 1");
        tabbedPane.add(buildButtonBar2Panel(),          "Dialog 2");
        tabbedPane.add(buildButtonBar3Panel(),          "Dialog 3");
        tabbedPane.add(buildAddRemovePropertiesPanel(), "List 1");
        tabbedPane.add(buildAddRemovePanel(),           "List 2");
        return tabbedPane;
    }

    private Component buildButtonBar1Panel() {
        FormLayout layout = new FormLayout(
                        "default:grow",
                        "0:grow, p, 4dlu, p, 4dlu, p, 4dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.nextRow();
        builder.add(ButtonBarFactory.buildCloseBar(
            new JButton("Close")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildOKBar(
            new JButton("OK")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildOKCancelBar(
            new JButton("OK"), new JButton("Cancel")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildOKCancelApplyBar(
            new JButton("OK"), new JButton("Cancel"), new JButton("Apply")
        ));

        return builder.getContainer();
    }


    private Component buildButtonBar2Panel() {
        FormLayout layout = new FormLayout(
                        "default:grow",
                        "0:grow, p, 4dlu, p, 4dlu, p, 4dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.nextRow();
        builder.add(ButtonBarFactory.buildCloseHelpBar(
            new JButton("Close"), new JButton("Help")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildOKHelpBar(
            new JButton("OK"), new JButton("Help")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildOKCancelHelpBar(
            new JButton("OK"), new JButton("Cancel"), new JButton("Help")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildOKCancelApplyHelpBar(
            new JButton("OK"), new JButton("Cancel"), new JButton("Apply"), new JButton("Help")
        ));

        return builder.getContainer();
    }


    private Component buildButtonBar3Panel() {
        FormLayout layout = new FormLayout(
                        "default:grow",
                        "0:grow, p, 4dlu, p, 4dlu, p, 4dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.nextRow();
        builder.add(ButtonBarFactory.buildHelpCloseBar(
            new JButton("Help"), new JButton("Close")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildHelpOKBar(
            new JButton("Help"), new JButton("OK")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildHelpOKCancelBar(
            new JButton("Help"), new JButton("OK"), new JButton("Cancel")
        ));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildHelpOKCancelApplyBar(
            new JButton("Help"), new JButton("OK"), new JButton("Cancel"), new JButton("Apply")
        ));

        return builder.getContainer();
    }


    private Component buildAddRemovePropertiesPanel() {
        FormLayout layout = new FormLayout(
                        "fill:default:grow",
                        "fill:p:grow, 4dlu, p, 14dlu, " +
                        "fill:p:grow, 4dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.add(new JScrollPane(new JTextArea()));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildAddRemovePropertiesLeftBar(
            new JButton("Add\u2026"), new JButton("Remove"), new JButton("Properties\u2026")
        ));
        builder.nextRow(2);

        builder.add(new JScrollPane(new JTextArea()));
        builder.nextRow(2);
        builder.add(ButtonBarFactory.buildAddRemovePropertiesRightBar(
            new JButton("Add\u2026"), new JButton("Remove"), new JButton("Properties\u2026")
        ));
        return builder.getContainer();
    }

    private Component buildAddRemovePanel() {
        FormLayout layout = new FormLayout(
                        "fill:default:grow, 9dlu, fill:default:grow",
                        "fill:p:grow, 4dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        builder.add(new JScrollPane(new JTextArea()),      cc.xy(1, 1));
        builder.add(ButtonBarFactory.buildAddRemoveLeftBar(
            new JButton("Add\u2026"), new JButton("Remove")), cc.xy(1, 3));

        builder.add(new JScrollPane(new JTextArea()),      cc.xy(3, 1));
        builder.add(ButtonBarFactory.buildAddRemoveRightBar(
            new JButton("Add\u2026"), new JButton("Remove")), cc.xy(3, 3));
        return builder.getContainer();
    }


}

