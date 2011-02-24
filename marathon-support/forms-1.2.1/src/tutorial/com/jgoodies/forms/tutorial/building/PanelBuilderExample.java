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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates a typical use of the FormLayout.
 * Columns and rows are specified before the panel is filled with
 * components, and the panel is filled with a PanelBuilder.<p>
 *
 * Unlike the PlainExample, this implementation can delegate
 * the component creation for text labels and titled separators
 * to the builder.<p>
 *
 * This panel building style is recommended for panels with
 * a medium number of rows and components. If the panel has more rows,
 * you may consider using a row variable to address the current row.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.19 $
 *
 * @see     PanelBuilder
 * @see	RowCounterExample
 * @see	DynamicRowsExample
 * @see	DefaultFormBuilderExample
 */
public final class PanelBuilderExample extends TutorialApplication {

    private JTextField companyNameField;
    private JTextField contactPersonField;
    private JTextField orderNoField;
    private JTextField inspectorField;
    private JTextField referenceNoField;
    private JComboBox  approvalStatusComboBox;
    private JTextField shipYardField;
    private JTextField registerNoField;
    private JTextField hullNumbersField;
    private JComboBox  projectTypeComboBox;


    // Launching **************************************************************

    public static void main(String[] args) {
        TutorialApplication.launch(PanelBuilderExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: PanelBuilder");
        frame.getContentPane().add(buildPanel());
        packAndShowOnScreenCenter(frame);
    }


    // Component Creation and Initialization **********************************

    /**
     *  Creates and initializes the UI components.
     */
    private void initComponents() {
        companyNameField       = new JTextField();
        contactPersonField     = new JTextField();
        orderNoField           = new JTextField();
        inspectorField         = new JTextField();
        referenceNoField       = new JTextField();
        approvalStatusComboBox = createApprovalStatusComboBox();
        shipYardField          = new JTextField();
        registerNoField        = new JTextField();
        hullNumbersField       = new JTextField();
        projectTypeComboBox    = createProjectTypeComboBox();
    }


    /**
     * Creates and returns a combo box for the approval states.
     *
     * @return a combo box for the approval status
     */
    private JComboBox createApprovalStatusComboBox() {
        return new JComboBox(
            new String[] { "In Progress", "Finished", "Released" });
    }


    /**
     * Creates and returns a combo box for the project types.
     *
     * @return a combo box for the project type
     */
    private JComboBox createProjectTypeComboBox() {
        return new JComboBox(
            new String[] { "New Building", "Conversion", "Repair" });
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
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " +
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " +
                "p, 3dlu, p, 3dlu, p, 3dlu, p");

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        // Fill the table with labels and components.
        CellConstraints cc = new CellConstraints();
        builder.addSeparator("Manufacturer",  cc.xyw(1,  1, 7));
        builder.addLabel("Company:",          cc.xy (1,  3));
        builder.add(companyNameField,         cc.xyw(3,  3, 5));
        builder.addLabel("Contact:",          cc.xy (1,  5));
        builder.add(contactPersonField,       cc.xyw(3,  5, 5));
        builder.addLabel("Order No:",         cc.xy (1,  7));
        builder.add(orderNoField,             cc.xy (3,  7));

        builder.addSeparator("Inspector",     cc.xyw(1,  9, 7));
        builder.addLabel("Name:",             cc.xy (1, 11));
        builder.add(inspectorField,           cc.xyw(3, 11, 5));
        builder.addLabel("Reference No:",     cc.xy (1, 13));
        builder.add(referenceNoField,         cc.xy (3, 13));
        builder.addLabel("Status:",           cc.xy (1, 15));
        builder.add(approvalStatusComboBox,  cc.xy (3, 15));

        builder.addSeparator("Ship",          cc.xyw(1, 17, 7));
        builder.addLabel("Shipyard:",         cc.xy (1, 19));
        builder.add(shipYardField,            cc.xyw(3, 19, 5));
        builder.addLabel("Register No:",      cc.xy (1, 21));
        builder.add(registerNoField,          cc.xy (3, 21));
        builder.addLabel("Hull No:",          cc.xy (5, 21));
        builder.add(hullNumbersField,         cc.xy (7, 21));
        builder.addLabel("Project type:",     cc.xy (1, 23));
        builder.add(projectTypeComboBox,      cc.xy (3, 23));

        return builder.getPanel();
    }

}
