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

package com.jgoodies.forms.extras;

import java.awt.Component;
import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Adds a feature to its superclass PanelBuilder that labels with a mnemonic
 * are associated with the next added focusable component.
 * This feature shall be moved to the PanelBuilder.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.1 $
 */
public class PanelBuilder2 extends PanelBuilder {

    /**
     * Refers to the most recently label that has a mnemonic set - if any.
     * Used to invoke {@link JLabel#setLabelFor(java.awt.Component)}
     * for the next component added to the panel. After the association
     * has been set, the reference will be cleared.
     */
    private WeakReference mostRecentlyAddedMnemonicLabelReference = null;


    // Instance Creation ****************************************************

    /**
     * Constructs a <code>PanelBuilder2</code> for the given
     * layout. Uses an instance of <code>JPanel</code> as layout container
     * with the given layout as layout manager.
     *
     * @param layout  the FormLayout to use
     */
    public PanelBuilder2(FormLayout layout){
        this(layout, new JPanel(null));
    }

    /**
     * Constructs a <code>PanelBuilder2</code> for the given
     * FormLayout and layout container.
     *
     * @param layout  the FormLayout to use
     * @param panel   the layout container to build on
     */
    public PanelBuilder2(FormLayout layout, JPanel panel){
        super(layout, panel);
    }


    // Adding Components ****************************************************

    /**
     * Adds a component to the panel using the given cell constraints.
     * In addition to the superclass behavior, this implementation
     * tracks the most recently label that has mnemonic, and associates
     * it with the next added focusable component.
     *
     * @param component        the component to add
     * @param cellConstraints  the component's cell constraints
     * @return the added component
     *
     * @see #isLabelForFeatureActive()
     * @see #isLabelForApplicable(Component)
     */
    public Component add(Component component, CellConstraints cellConstraints) {
        Component result = super.add(component, cellConstraints);
        if (!isLabelForFeatureActive()) {
            return result;
        }
        JLabel mostRecentlyAddedMnemonicLabel = getMostRecentlyAddedMnemonicLabel();
        if (   (mostRecentlyAddedMnemonicLabel != null)
            && isLabelForApplicable(component)) {
            mostRecentlyAddedMnemonicLabel.setLabelFor(component);
            clearMostRecentlyAddedMnemonicLabel();
        }
        setMostRecentlyAddedMnemonicLabel(component);
        return result;
    }


    // Default Behavior *******************************************************

    /**
     * Checks and answers whether the labelFor feature is active
     * for this builder.
     *
     * @return true for active, false for inactive
     */
    protected boolean isLabelForFeatureActive() {
        return true;
    }


    /**
     * Checks and answers whether the given component shall be set
     * as component for a previously added label with mnemonic using
     * {@link JLabel#setLabelFor(Component)}.
     * The current implementation just checks whether the component is
     * focusable.
     *
     * @param component    the component to be checked
     * @return true if focusable, false otherwise
     */
    protected boolean isLabelForApplicable(Component component) {
        return component.isFocusable();
    }


    // Helper Code ************************************************************

    private JLabel getMostRecentlyAddedMnemonicLabel() {
        if (mostRecentlyAddedMnemonicLabelReference == null) {
            return null;
        }
        JLabel label = (JLabel) mostRecentlyAddedMnemonicLabelReference.get();
        if (label == null) {
            return null;
        }
        return label;
    }


    private void setMostRecentlyAddedMnemonicLabel(Component component) {
        if (!(component instanceof JLabel)) {
            return;
        }
        JLabel label = (JLabel) component;
        if (label.getDisplayedMnemonic() != -1) {
            mostRecentlyAddedMnemonicLabelReference =
                new WeakReference(label);
        }
    }


    private void clearMostRecentlyAddedMnemonicLabel() {
        mostRecentlyAddedMnemonicLabelReference = null;
    }

}
