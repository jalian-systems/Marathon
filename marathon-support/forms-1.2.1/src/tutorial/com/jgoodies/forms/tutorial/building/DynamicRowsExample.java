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

import javax.swing.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Combines the FormLayout with the PanelBuilder.
 * Columns and rows are specified before the panel is filled
 * with components. The builder's cursor is used to determine the location
 * of the next component. And the builder's convenience methods are used
 * to add labels and separators.<p>
 *
 * This panel building style is intended for learning purposes only.
 * The recommended style is demonstrated in the {@link DefaultFormBuilderExample}.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.20 $
 *
 * @see	PlainExample
 * @see	RowCounterExample
 * @see	DefaultFormBuilderExample
 */
public final class DynamicRowsExample extends TutorialApplication {

    private JTextField identifierField;
    private JTextField ptiField;
    private JTextField powerField;
    private JTextField lenField;
    private JTextField daField;
    private JTextField diField;
    private JTextField da2Field;
    private JTextField di2Field;
    private JTextField rField;
    private JTextField dField;
    private JComboBox  locationCombo;
    private JTextField kFactorField;
    private JCheckBox  holesCheckBox;
    private JCheckBox  slotsCheckBox;


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(DynamicRowsExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Dynamic Rows");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Component Creation and Initialization **********************************

    /**
     *  Creates and initializes the UI components.
     */
    private void initComponents() {
        identifierField = new JTextField();
        ptiField        = new JTextField();
        powerField      = new JTextField();
        lenField        = new JTextField();
        daField         = new JTextField();
        diField         = new JTextField();
        da2Field        = new JTextField();
        di2Field        = new JTextField();
        rField          = new JTextField();
        dField          = new JTextField();
        locationCombo   = createLocationComboBox();
        kFactorField    = new JTextField();
        holesCheckBox   = new JCheckBox("Has radial holes", true);
        slotsCheckBox   = new JCheckBox("Has longitudinal slots");
    }

    /**
     * Creates and returns a combo box for the locations.
     *
     * @return a combo box for three locations
     */
    private JComboBox createLocationComboBox() {
        return new JComboBox(
            new String[] {
                "Propeller nut thread",
                "Stern tube front area",
                "Shaft taper" });
    }


    // Building *************************************************************

    /**
     * Builds the pane.
     *
     * @return the built panel
     */
    public JComponent buildPanel() {
        initComponents();

        FormLayout layout = new FormLayout(
                "right:[40dlu,pref], 3dlu, 70dlu, 7dlu, "
              + "right:[40dlu,pref], 3dlu, 70dlu",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, "
              + "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, "
              + "p, 3dlu, p, 3dlu, p, 3dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.addSeparator("Segment");
        builder.nextLine(2);

        builder.addLabel("Identifier:");       builder.nextColumn(2);
        builder.add(identifierField);
        builder.nextLine(2);

        builder.addLabel("PTI/kW:");           builder.nextColumn(2);
        builder.add(ptiField);                 builder.nextColumn(2);
        builder.addLabel("Power/kW:");         builder.nextColumn(2);
        builder.add(powerField);
        builder.nextLine(2);

        builder.addLabel("len/mm:");           builder.nextColumn(2);
        builder.add(lenField);
        builder.nextLine(2);

        builder.addSeparator("Diameters");
        builder.nextLine(2);

        builder.addLabel("da/mm:");            builder.nextColumn(2);
        builder.add(daField);                  builder.nextColumn(2);
        builder.addLabel("di/mm:");            builder.nextColumn(2);
        builder.add(diField);
        builder.nextLine(2);

        builder.addLabel("da2/mm:");           builder.nextColumn(2);
        builder.add(da2Field);                 builder.nextColumn(2);
        builder.addLabel("di2/mm:");           builder.nextColumn(2);
        builder.add(di2Field);

        builder.nextLine(2);
        builder.addLabel("R/mm:");             builder.nextColumn(2);
        builder.add(rField);                   builder.nextColumn(2);
        builder.addLabel("D/mm:");             builder.nextColumn(2);
        builder.add(dField);
        builder.nextLine(2);

        builder.addSeparator("Criteria");
        builder.nextLine(2);

        builder.addLabel("Location:");         builder.nextColumn(2);
        builder.add(locationCombo);            builder.nextColumn(2);
        builder.addLabel("k-factor:");         builder.nextColumn(2);
        builder.add(kFactorField);
        builder.nextLine(2);

        builder.addLabel("Holes:");            builder.nextColumn(2);
        builder.setColumnSpan(5);
        builder.add(holesCheckBox);
        builder.setColumnSpan(1);
        builder.nextLine(2);

        builder.addLabel("Slots");            builder.nextColumn(2);
        builder.setColumnSpan(5);
        builder.add(slotsCheckBox);
        builder.setColumnSpan(1);

        return builder.getPanel();
    }


}
