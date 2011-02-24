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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.jgoodies.forms.layout.FormLayout;

/**
 * A general purpose panel builder that uses the {@link FormLayout}
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
 * @author	Karsten Lentzsch
 * @version $Revision: 1.7 $
 * @since 1.0.3
 *
 * @see	ResourceBundle
 */
public class I15dPanelBuilder extends AbstractI15dPanelBuilder {

    /**
     * Holds the <code>ResourceBundle</code> used to lookup internationalized
     * (i15d) String resources.
     */
    private final ResourceBundle bundle;


    // Instance Creation ****************************************************

    /**
     * Constructs an <code>I15dPanelBuilder</code> for the given
     * layout and resource bundle. Uses an instance of <code>JPanel</code>
     * as layout container.
     *
     * @param layout    the <code>FormLayout</code> used to layout the container
     * @param bundle    the <code>ResourceBundle</code> used to lookup i15d strings
     */
    public I15dPanelBuilder(FormLayout layout, ResourceBundle bundle){
        this(layout, bundle, new JPanel(null));
    }


    /**
     * Constructs an <code>I15dPanelBuilder</code>
     * for the given FormLayout, resource bundle, and layout container.
     *
     * @param layout  the <code>FormLayout</code> used to layout the container
     * @param bundle  the <code>ResourceBundle</code> used to lookup i15d strings
     * @param panel   the layout container
     */
    public I15dPanelBuilder(FormLayout layout, ResourceBundle bundle, JPanel panel){
        super(layout, panel);
        this.bundle = bundle;
    }


    // Implementing Abstract Behavior *****************************************

    /**
     * Looks up and returns the internationalized (i15d) string for the given
     * resource key from the <code>ResourceBundle</code>.
     *
     * @param resourceKey  the key to look for in the resource bundle
     * @return the associated internationalized string, or the resource key
     *     itself in case of a missing resource
     * @throws IllegalStateException  if no <code>ResourceBundle</code>
     *     has been set
     */
    protected String getI15dString(String resourceKey) {
        if (bundle == null)
            throw new IllegalStateException("You must specify a ResourceBundle" +                " before using the internationalization support.");
        try {
            return bundle.getString(resourceKey);
        } catch (MissingResourceException mre) {
            return resourceKey;
        }
    }



}
