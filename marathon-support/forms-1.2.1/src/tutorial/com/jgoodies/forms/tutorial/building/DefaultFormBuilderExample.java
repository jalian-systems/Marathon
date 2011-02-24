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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Uses the FormLayout and the <code>DefaultFormBuilder</code>.
 * Columns are specified before the panel is filled with components,
 * rows are added dynamically. The builder is used to hold a cursor,
 * to add rows dynamically, and to fill components.
 * The builder's convenience methods are used to add labels and separators.<p>
 *
 * This panel building style is recommended unless you have a more
 * powerful builder or don't want to add rows dynamically.
 * See the {@link DynamicRowsExample} for an implementation that specifies
 * rows before the panel is filled with components.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.21 $
 *
 * @see     DefaultFormBuilder
 * @see     PlainExample
 * @see     RowCounterExample
 * @see     DynamicRowsExample
 */

public final class DefaultFormBuilderExample extends TutorialApplication {

    private JTextField identifierField;
    private JTextField ptiField;
    private JTextField powerField;
    private JTextField sField;
    private JTextField daField;
    private JTextField diField;
    private JTextField da2Field;
    private JTextField di2Field;
    private JTextField rField;
    private JTextField dField;
    private JComboBox  locationCombo;
    private JTextField kFactorField;


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(DefaultFormBuilderExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Default Form");
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
        sField          = new JTextField();
        daField         = new JTextField();
        diField         = new JTextField();
        da2Field        = new JTextField();
        di2Field        = new JTextField();
        rField          = new JTextField();
        dField          = new JTextField();
        locationCombo   = createLocationComboBox();
        kFactorField    = new JTextField();
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


    // Building ***************************************************************

    /**
     * Builds the flange editor panel.
     * Columns are specified before components are added to the form,
     * rows are added dynamically using the {@link DefaultFormBuilder}.<p>
     *
     * The builder combines a step that is done again and again:
     * add a label, proceed to the next data column and add a component.
     *
     * @return the built panel
     */
    public JComponent buildPanel() {
        initComponents();

        // Column specs only, rows will be added dynamically.
        FormLayout layout = new FormLayout(
                "right:[40dlu,pref], 3dlu, 70dlu, 7dlu, "
              + "right:[40dlu,pref], 3dlu, 70dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();


        builder.appendSeparator("Flange");

        builder.append("&Identifier:", identifierField);
        builder.nextLine();

        builder.append("PTI/kW:",   ptiField);
        builder.append("Power/kW:", powerField);

        builder.append("s/mm:",     sField);
        builder.nextLine();


        builder.appendSeparator("Diameters");

        builder.append("&da/mm:",   daField);
        builder.append("di/mm:",    diField);

        builder.append("da2/mm:",   da2Field);
        builder.append("di2/mm:",   di2Field);

        builder.append("R/mm:",     rField);
        builder.append("D/mm:",     dField);


        builder.appendSeparator("Criteria");

        builder.append("&Location:",  locationCombo);
        builder.append("k-factor:",   kFactorField);

        return builder.getPanel();
    }

}
