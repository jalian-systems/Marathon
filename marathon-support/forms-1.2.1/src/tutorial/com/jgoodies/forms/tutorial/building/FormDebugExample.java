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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates how to find bugs in the layout using
 * the {@link FormDebugPanel} and the {@link FormDebugUtils}.<p>
 *
 * The example also demonstrates efficient panel building with
 * the DefaultFormBuilder. The builder has been configured
 * to use a leading indent column.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.22 $
 */
public final class FormDebugExample extends TutorialApplication {

    private JTextField fileNumberField;
    private JTextField rfqNumberField;
    private JTextField blNumberField;
    private JTextField mblNumberField;

    private JTextField customerKeyField;
    private JTextField customerAddressField;
    private JTextField shipperKeyField;
    private JTextField shipperAddressField;
    private JTextField consigneeKeyField;
    private JTextField consigneeAddressField;

    private JTextField departureCodeField;
    private JTextField departurePortField;
    private JTextField destinationCodeField;
    private JTextField destinationPortField;
    private JTextField deliveryDateField;


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(FormDebugExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Debug a Form");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Component Creation and Initialization **********************************

    /**
     *  Creates and initializes the UI components.
     */
    private void initComponents() {
        fileNumberField       = new JTextField();
        rfqNumberField        = new JTextField();
        blNumberField         = new JTextField();
        mblNumberField        = new JTextField();
        customerKeyField      = new JTextField();
        customerAddressField  = new JTextField();
        customerAddressField.setEditable(false);
        shipperKeyField       = new JTextField();
        shipperAddressField   = new JTextField();
        shipperAddressField.setEditable(false);
        consigneeKeyField     = new JTextField();
        consigneeAddressField = new JTextField();
        consigneeAddressField.setEditable(false);
        departureCodeField    = new JTextField();
        departurePortField    = new JTextField();
        departurePortField.setEditable(false);
        destinationCodeField  = new JTextField();
        destinationPortField  = new JTextField();
        destinationPortField.setEditable(false);
        deliveryDateField     = new JTextField();
    }


    // Building *************************************************************

    /**
     * Builds the panel.
     *
     * @return the built panel
     */
    public JComponent buildPanel() {
        initComponents();

        FormLayout layout = new FormLayout(
                "12dlu, pref, 3dlu, [45dlu,min], 2dlu, min, 2dlu, min, 2dlu, min");
        layout.setColumnGroups(new int[][] { { 4, 6, 8, 10 } });

        DefaultFormBuilder builder =
            new DefaultFormBuilder(layout, new FormDebugPanel());

        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);

        builder.appendSeparator("General");
        builder.append("File Number:",    fileNumberField, 7);
        builder.append("RFQ Number:",     rfqNumberField,  7);
        builder.append("BL/MBL:",         blNumberField, mblNumberField); builder.nextLine();

        builder.appendSeparator("Addresses");
        builder.append("Customer:",       customerKeyField,  customerAddressField,  5);
        builder.append("Shipper:",        shipperKeyField,   shipperAddressField,   5);
        builder.append("Consignee:",      consigneeKeyField, consigneeAddressField, 5);

        builder.appendSeparator("Transport");
        builder.append("Departure:",      departureCodeField,   departurePortField,   5);
        builder.append("Destination:",    destinationCodeField, destinationPortField, 5);
        builder.append("Delivery date:",  deliveryDateField); builder.nextLine();

        FormDebugUtils.dumpAll(builder.getPanel());

        return builder.getPanel();
    }


}
