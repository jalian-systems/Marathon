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

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates how to build button bars with a fixed button order
 * or with a button order that honors the platform's style.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.13 $
 *
 * @see     ButtonBarBuilder
 * @see     com.jgoodies.forms.factories.ButtonBarFactory
 */
public final class ButtonOrderExample extends TutorialApplication {


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(ButtonOrderExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Button Order");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Building ***************************************************************

    /**
     * Builds and returns a panel that consists of three paragraphs
     * that demonstrate different button orders. Each paragraph contains
     * a button bar that is built from a button sequence, and another
     * bar that is built from individual buttons.
     *
     * @return a panel that demonstrates button order
     */
    public JComponent buildPanel() {
        FormLayout layout = new FormLayout("right:pref:grow, 4dlu, pref");
        DefaultFormBuilder rowBuilder = new DefaultFormBuilder(layout);
        rowBuilder.setDefaultDialogBorder();

        rowBuilder.appendSeparator("Left to Right");
        rowBuilder.append("Ordered:", buildButtonSequence(ButtonBarBuilder.createLeftToRightBuilder()));
        rowBuilder.append("Fixed:",   buildIndividualButtons(ButtonBarBuilder.createLeftToRightBuilder()));

        rowBuilder.appendSeparator("Right to Left");
        rowBuilder.append("Ordered:", buildButtonSequence(createRightToLeftBuilder()));
        rowBuilder.append("Fixed:",   buildIndividualButtons(createRightToLeftBuilder()));

        rowBuilder.appendSeparator("Platform Default Order");
        rowBuilder.append("Ordered:", buildButtonSequence(new ButtonBarBuilder()));
        rowBuilder.append("Fixed:",   buildIndividualButtons(new ButtonBarBuilder()));

        return rowBuilder.getPanel();
    }


    /**
     * Builds and returns a button bar honoring the builder's button order.
     *
     * @param builder   the builder used to build the bar
     * @return a button bar that honors the builder's button order
     */
    private Component buildButtonSequence(ButtonBarBuilder builder) {
        builder.addGriddedButtons(new JButton[] {
                new JButton("One"),
                new JButton("Two"),
                new JButton("Three")
        });
        return builder.getPanel();
    }


    /**
     * Builds and returns a button bar ignoring the builder's button order.
     * Instead a fixed left to right order is used.
     *
     * @param builder   the builder used to build the bar
     * @return a button bar with a fixed left to right button order
     */
    private Component buildIndividualButtons(ButtonBarBuilder builder) {
        builder.addGridded(new JButton("One"));
        builder.addRelatedGap();
        builder.addGridded(new JButton("Two"));
        builder.addRelatedGap();
        builder.addGridded(new JButton("Three"));
        return builder.getPanel();
    }


    /**
     * Creates and returns a button bar builder with a fixed
     * right-to-left button order. Unlike the factory method
     * {@link ButtonBarBuilder#createLeftToRightBuilder()}
     * this method is useful for demonstration purposes only.
     *
     * @return a ButtonBarBuilder with right-to-left button order
     */
    private static ButtonBarBuilder createRightToLeftBuilder() {
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.setLeftToRightButtonOrder(false);
        return builder;
    }

}

