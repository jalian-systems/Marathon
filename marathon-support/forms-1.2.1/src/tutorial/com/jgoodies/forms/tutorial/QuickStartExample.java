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

package com.jgoodies.forms.tutorial;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Quickly introduces the most important features of the FormLayout:
 * create and configure a layout, create a builder, add components.<p>
 *
 * Note that this class is not a JPanel subclass; it just <em>uses</em>
 * a JPanel as layout container that will be returned by
 * <code>#buildPanel()</code>.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.17 $
 */
public final class QuickStartExample extends TutorialApplication {

    private JTextField companyField;
    private JTextField contactField;
    private JTextField ptiField;
    private JTextField powerField;
    private JTextField radiusField;
    private JTextField diameterField;


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(QuickStartExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Quick Start");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Component Creation and Initialization **********************************

    /**
     * Creates, intializes and configures the UI components.
     * Real applications may further bind the components to underlying models.
     */
    private void initComponents() {
        companyField  = new JTextField();
        contactField  = new JTextField();
        ptiField      = new JTextField(6);
        powerField    = new JTextField(10);
        radiusField   = new JTextField(8);
        diameterField = new JTextField(8);
    }

    // Building *************************************************************

    /**
     * Builds the panel. Initializes and configures components first,
     * then creates a FormLayout, configures the layout, creates a builder,
     * sets a border, and finally adds the components.
     *
     * @return the built panel
     */
    public JComponent buildPanel() {
        // Separating the component initialization and configuration
        // from the layout code makes both parts easier to read.
        initComponents();

        // Create a FormLayout instance on the given column and row specs.
        // For almost all forms you specify the columns; sometimes rows are
        // created dynamically. In this case the labels are right aligned.
        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, pref, 7dlu, right:pref, 3dlu, pref", // cols
                "p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, p");      // rows

        // Specify that columns 1 & 5 as well as 3 & 7 have equal widths.
        layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

        // Create a builder that assists in adding components to the container.
        // Wrap the panel with a standardized border.
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        // Obtain a reusable constraints object to place components in the grid.
        CellConstraints cc = new CellConstraints();

        // Fill the grid with components; the builder offers to create
        // frequently used components, e.g. separators and labels.

        // Add a titled separator to cell (1, 1) that spans 7 columns.
        builder.addSeparator("General",   cc.xyw(1,  1, 7));
        builder.addLabel("Company",       cc.xy (1,  3));
        builder.add(companyField,         cc.xyw(3,  3, 5));
        builder.addLabel("Contact",       cc.xy (1,  5));
        builder.add(contactField,         cc.xyw(3,  5, 5));

        builder.addSeparator("Propeller", cc.xyw(1,  7, 7));
        builder.addLabel("PTI/kW",        cc.xy (1,  9));
        builder.add(ptiField,             cc.xy (3,  9));
        builder.addLabel("Power/kW",      cc.xy (5,  9));
        builder.add(powerField,           cc.xy (7,  9));
        builder.addLabel("R/mm",          cc.xy (1, 11));
        builder.add(radiusField,          cc.xy (3, 11));
        builder.addLabel("D/mm",          cc.xy (5, 11));
        builder.add(diameterField,        cc.xy (7, 11));

        // The builder holds the layout container that we now return.
        return builder.getPanel();
    }

}
