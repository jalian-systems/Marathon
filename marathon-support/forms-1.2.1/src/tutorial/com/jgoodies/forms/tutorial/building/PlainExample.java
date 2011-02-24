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

import javax.swing.*;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.tutorial.util.TutorialApplication;

/**
 * Demonstrates a <em>pure</em> use of the FormLayout.
 * Columns and rows are specified before the panel is filled with
 * components. And the panel is filled without a builder.<p>
 *
 * This panel building style is simple but not recommended. Other panel
 * building styles use a builder to fill the panel and/or create
 * form rows dynamically. See the {@link PanelBuilderExample} for
 * a slightly better panel building style that can use the builder
 * to create text labels and separators.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.19 $
 *
 * @see     PanelBuilderExample
 * @see	RowCounterExample
 * @see	DynamicRowsExample
 * @see	DefaultFormBuilderExample
 */
public final class PlainExample extends TutorialApplication {

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
        TutorialApplication.launch(PlainExample.class, args);
    }


    protected void startup(String[] args) {
        JFrame frame = createFrame("Forms Tutorial :: Plain Building");
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

        JPanel panel = new JPanel(layout);
        panel.setBorder(Borders.DIALOG_BORDER);

        // Fill the table with labels and components.
        CellConstraints cc = new CellConstraints();
        panel.add(createSeparator("Manufacturer"),   cc.xyw(1,  1, 7));
        panel.add(new JLabel("Company:"),            cc.xy (1,  3));
        panel.add(companyNameField,                  cc.xyw(3,  3, 5));
        panel.add(new JLabel("Contact:"),            cc.xy (1,  5));
        panel.add(contactPersonField,                cc.xyw(3,  5, 5));
        panel.add(new JLabel("Order No:"),           cc.xy (1, 7));
        panel.add(orderNoField,                      cc.xy (3, 7));

        panel.add(createSeparator("Inspector"),      cc.xyw(1, 9, 7));
        panel.add(new JLabel("Name:"),               cc.xy (1, 11));
        panel.add(inspectorField,                    cc.xyw(3, 11, 5));
        panel.add(new JLabel("Reference No:"),       cc.xy (1, 13));
        panel.add(referenceNoField,                  cc.xy (3, 13));
        panel.add(new JLabel("Status:"),             cc.xy (1, 15));
        panel.add(approvalStatusComboBox,            cc.xy (3, 15));

        panel.add(createSeparator("Ship"),           cc.xyw(1, 17, 7));
        panel.add(new JLabel("Shipyard:"),           cc.xy (1, 19));
        panel.add(shipYardField,                     cc.xyw(3, 19, 5));
        panel.add(new JLabel("Register No:"),        cc.xy (1, 21));
        panel.add(registerNoField,                   cc.xy (3, 21));
        panel.add(new JLabel("Hull No:"),            cc.xy (5, 21));
        panel.add(hullNumbersField,                  cc.xy (7, 21));
        panel.add(new JLabel("Project type:"),       cc.xy (1, 23));
        panel.add(projectTypeComboBox,               cc.xy (3, 23));

        return panel;
    }


    /**
     * Creates and returns a separator with a label in the left hand side.<p>
     *
     * <pre>
     * createSeparator("Name");       // No mnemonic
     * createSeparator("N&ame");      // Mnemonic is 'a'
     * createSeparator("Save &as");   // Mnemonic is the second 'a'
     * createSeparator("Look&&Feel"); // No mnemonic, text is Look&Feel
     * </pre>
     *
     * @param textWithMnemonic  the label's text -
     *     may contain an ampersand (<tt>&amp;</tt>) to mark a mnemonic
     * @return a separator with label in the left hand side
     */
    private Component createSeparator(String textWithMnemonic) {
        return DefaultComponentFactory.getInstance().createSeparator(
                textWithMnemonic);
    }

}
