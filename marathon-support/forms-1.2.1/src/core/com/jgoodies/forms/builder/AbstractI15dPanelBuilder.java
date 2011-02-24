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

package com.jgoodies.forms.builder;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An abstract panel builder class that uses the {@link FormLayout}
 * to lay out <code>JPanel</code>s. In addition to its superclass
 * {@link PanelBuilder} this class provides convenience behavior to map
 * resource keys to their associated internationalized (i15d) strings
 * when adding labels, titles and titled separators.<p>
 *
 * The localized texts used in methods <code>#addI15dLabel</code>
 * and <code>#addI15dTitle</code> can contain an optional mnemonic marker.
 * The mnemonic and mnemonic index are indicated by a single ampersand
 * (<tt>&amp;</tt>). For example <tt>&quot;&amp;Save&quot</tt>, or
 * <tt>&quot;Save&nbsp;&amp;as&quot</tt>. To use the ampersand itself,
 * duplicate it, for example <tt>&quot;Look&amp;&amp;Feel&quot</tt>.<p>
 *
 * For debugging purposes you can automatically set a tooltip for the
 * created labels that show its resource key. In case of an inproper
 * resource localization, the label will show the wrong text, and the tooltip
 * will help you identify the resource key with the broken localization.
 * This feature can be enabled by calling <code>setDebugToolTipsEnabled</code>.
 * If you want to enable it in a deployed application, you can set the system
 * parameter <code>I15dPanelBuilder.debugToolTipsEnabled</code> to "true".<p>
 *
 * Subclasses must implement the conversion from resource key
 * to the localized string in <code>#getI15dString(String)</code>.
 * For example class I15dPanelBuilder gets a ResourceBundle on
 * construction, and requests strings from that bundle.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.4 $
 *
 * @since 1.1
 */
public abstract class AbstractI15dPanelBuilder extends PanelBuilder {

    private static final String DEBUG_TOOL_TIPS_ENABLED_KEY =
        "I15dPanelBuilder.debugToolTipsEnabled";

    private static boolean debugToolTipsEnabled =
        getDebugToolTipSystemProperty();


    // Instance Creation ****************************************************

    /**
     * Constructs an <code>AbstractI15dPanelBuilder</code> for the given
     * layout. Uses an instance of <code>JPanel</code> as layout container.
     *
     * @param layout        the <code>FormLayout</code> used to layout the container
     */
    protected AbstractI15dPanelBuilder(FormLayout layout){
        super(layout);
    }


    /**
     * Constructs an <code>AbstractI15dPanelBuilder</code>
     * for the given FormLayout and layout container.
     *
     * @param layout  the <code>FormLayout</code> used to layout the container
     * @param panel   the layout container
     */
    protected AbstractI15dPanelBuilder(FormLayout layout, JPanel panel){
        super(layout, panel);
    }


    // Debug ToolTip Settings *************************************************

    private static boolean getDebugToolTipSystemProperty() {
        try {
            String value = System.getProperty(DEBUG_TOOL_TIPS_ENABLED_KEY);
            return "true".equalsIgnoreCase(value);
        } catch (SecurityException e) {
            return false;
        }
    }


    /**
     * Returns whether the debug tool tips are enabled or not.
     *
     * @return true if debug tool tips are enabled, false if disabled
     */
    public static boolean isDebugToolTipsEnabled() {
        return debugToolTipsEnabled;
    }


    /**
     * Enables or disables the debug tool tips.
     *
     * @param b true to enable, false to disable
     */
    public static void setDebugToolTipsEnabled(boolean b) {
        debugToolTipsEnabled = b;
    }


    // Adding Labels and Separators *****************************************

    /**
     * Adds an internationalized (i15d) textual label to the form using the
     * specified constraints.
     *
     * @param resourceKey	the resource key for the label's text
     * @param constraints	the label's cell constraints
     * @return the added label
     */
    public final JLabel addI15dLabel(String resourceKey, CellConstraints constraints) {
        JLabel label = addLabel(getI15dString(resourceKey), constraints);
        if (isDebugToolTipsEnabled()) {
            label.setToolTipText(resourceKey);
        }
        return label;
    }

    /**
     * Adds an internationalized (i15d) textual label to the form using the
     * specified constraints.
     *
     * @param resourceKey         the resource key for the label's text
     * @param encodedConstraints  a string representation for the constraints
     * @return the added label
     */
    public final JLabel addI15dLabel(String resourceKey, String encodedConstraints) {
        return addI15dLabel(resourceKey, new CellConstraints(encodedConstraints));
    }

    /**
     * Adds an internationalized (i15d) label and component to the panel using
     * the given cell constraints. Sets the label as <i>the</i> component label
     * using {@link JLabel#setLabelFor(java.awt.Component)}.<p>
     *
     * <strong>Note:</strong> The {@link CellConstraints} objects for the label
     * and the component must be different. Cell constraints are implicitly
     * cloned by the <code>FormLayout</code> when added to the container.
     * However, in this case you may be tempted to reuse a
     * <code>CellConstraints</code> object in the same way as with many other
     * builder methods that require a single <code>CellConstraints</code>
     * parameter.
     * The pitfall is that the methods <code>CellConstraints.xy**(...)</code>
     * just set the coordinates but do <em>not</em> create a new instance.
     * And so the second invocation of <code>xy***(...)</code> overrides
     * the settings performed in the first invocation before the object
     * is cloned by the <code>FormLayout</code>.<p>
     *
     * <strong>Wrong:</strong><pre>
     * builder.add("name.key",
     *             cc.xy(1, 7),         // will be modified by the code below
     *             nameField,
     *             cc.xy(3, 7)          // sets the single instance to (3, 7)
     *            );
     * </pre>
     * <strong>Correct:</strong><pre>
     * builder.add("name.key",
     *             cc.xy(1, 7).clone(), // cloned before the next modification
     *             nameField,
     *             cc.xy(3, 7)          // sets this instance to (3, 7)
     *            );
     * </pre>
     *
     * @param resourceKey           the resource key for the label
     * @param labelConstraints      the label's cell constraints
     * @param component             the component to add
     * @param componentConstraints  the component's cell constraints
     * @return the added label
     * @throws IllegalArgumentException if the same cell constraints instance
     *     is used for the label and the component
     * @see JLabel#setLabelFor(java.awt.Component)
     */
    public final JLabel addI15dLabel(
            String resourceKey,   CellConstraints labelConstraints,
			Component component,  CellConstraints componentConstraints) {

        JLabel label = addLabel(getI15dString(resourceKey), labelConstraints,
                        component, componentConstraints);
        if (isDebugToolTipsEnabled()) {
            label.setToolTipText(resourceKey);
        }
        return label;
    }


    /**
     * Adds an internationalized (i15d) titled separator to the form using the
     * specified constraints.
     *
     * @param resourceKey  the resource key for the separator title
     * @param constraints  the separator's cell constraints
     * @return the added titled separator
     */
    public final JComponent addI15dSeparator(String resourceKey, CellConstraints constraints) {
        JComponent component = addSeparator(getI15dString(resourceKey), constraints);
        if (isDebugToolTipsEnabled()) {
            component.setToolTipText(resourceKey);
        }
        return component;
    }

    /**
     * Adds an internationalized (i15d)  titled separator to the form using
     * the specified constraints.
     *
     * @param resourceKey         the resource key for the separator title
     * @param encodedConstraints  a string representation for the constraints
     * @return the added titled separator
     */
    public final JComponent addI15dSeparator(String resourceKey, String encodedConstraints) {
        return addI15dSeparator(resourceKey, new CellConstraints(encodedConstraints));
    }

    /**
     * Adds a title to the form using the specified constraints.
     *
     * @param resourceKey  the resource key for  the separator title
     * @param constraints  the separator's cell constraints
     * @return the added title label
     */
    public final JLabel addI15dTitle(String resourceKey, CellConstraints constraints) {
        JLabel label = addTitle(getI15dString(resourceKey), constraints);
        if (isDebugToolTipsEnabled()) {
            label.setToolTipText(resourceKey);
        }
        return label;
    }

    /**
     * Adds a title to the form using the specified constraints.
     *
     * @param resourceKey         the resource key for the separator title
     * @param encodedConstraints  a string representation for the constraints
     * @return the added title label
     */
    public final JLabel addI15dTitle(String resourceKey, String encodedConstraints) {
        return addI15dTitle(resourceKey, new CellConstraints(encodedConstraints));
    }


    // Abstract Behavior ******************************************************

    /**
     * Looks up and returns the internationalized (i15d) string for the given
     * resource key from the <code>ResourceMap</code>.
     *
     * @param resourceKey  the key to look for in the resource map
     * @return the associated internationalized string, or the resource key
     *     itself in case of a missing resource
     * @throws IllegalStateException  if no <code>ResourceBundle</code>
     *     has been set
     */
    protected abstract String getI15dString(String resourceKey);


}
