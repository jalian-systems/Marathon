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

package com.jgoodies.forms.layout;

import java.awt.Component;
import java.awt.Dimension;

/**
 * A component that is used in the layout tests. It is constructed
 * with fixed minimum and preferred sizes.
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.10 $
 */
public final class TestComponent extends Component {

    /**
     * Holds the component's minimum size that can be requested
     * using <code>#getMinimumSize</code>.
     */
    private final Dimension minimumSize;

    /**
     * Holds the component's preferred size that can be requested
     * using <code>#getPreferredSize</code>.
     */
    private final Dimension preferredSize;


    // Instance Creation ******************************************************

    /**
     * Constructs a TestComponent with the given minimum and preferred sizes.
     *
     * @param minimumSize      the component's minimum size
     * @param preferredSize    the component's preferred size
     */
    public TestComponent(Dimension minimumSize, Dimension preferredSize) {
        this.minimumSize   = minimumSize;
        this.preferredSize = preferredSize;
    }

    /**
     * Constructs a TestComponent with the given minimum and preferred
     * widths and heights.
     *
     * @param minWidth      the component's minimum width
     * @param minHeight     the component's minimum height
     * @param prefWidth     the component's preferred width
     * @param prefHeight    the component's preferred height
     */
    public TestComponent(int minWidth, int minHeight, int prefWidth, int prefHeight) {
        this(new Dimension(minWidth, minHeight),
             new Dimension(prefWidth, prefHeight));
    }


    // Accessing Properties ***************************************************

    /**
     * Returns the minimum size of this component.
     *
     * @return a dimension object indicating this component's minimum size
     * @see #getPreferredSize()
     */
    public Dimension getMinimumSize() {
        return minimumSize;
    }


    /**
     * Returns the preferred size of this component.
     *
     * @return a dimension object indicating this component's preferred size
     * @see #getMinimumSize()
     */
    public Dimension getPreferredSize() {
        return preferredSize;
    }


}
