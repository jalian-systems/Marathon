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

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.util.LayoutStyle;

/**
 * A non-visual builder for building consistent button bars that comply
 * with popular style guides. Utilizes the JGoodies {@link FormLayout}
 * and honors the platform's {@link LayoutStyle} regarding button sizes,
 * gap widths, and the default button order.<p>
 *
 * This is an improved version of the older {@link ButtonBarBuilder}.
 * The ButtonBarBuilder2 has a simpler, safer, and more convenient API,
 * see below for a comparison.<p>
 *
 * <strong>ButtonBarBuilder2 vs. ButtonBarBuilder:</strong><br>
 * ButtonBarBuilder2 uses only 3 component types that can be added:
 * <em>button</em>, <em>standard</em>, and <em>growing button</em>, where
 * ButtonBarBuilder has <em>button</em>, <em>fixed</em>, and <em>growing</em>.
 * Also, the ButtonBarBuilder2 doesn't group buttons.
 * The layout of the ButtonBarBuilder and ButtonBarBuilder2 is the same
 * if all buttons are smaller than {@link LayoutStyle#getDefaultButtonWidth()}.
 * If some buttons are wider, ButtonBarBuilder2 will make only these buttons
 * wider, where the old ButtonBarBuilder makes all (gridded) buttons wider.
 *
 * <p>
 * <strong>Examples:</strong><pre>
 * // Build a right-aligned bar for: OK, Cancel, Apply
 * ButtonBarBuilder2 builder = new ButtonBarBuilder2();
 * builder.addGlue();
 * builder.addButton(okButton);
 * builder.addRelatedGap();
 * builder.addButton(cancelButton);
 * builder.addRelatedGap();
 * builder.addButton(applyButton);
 * return builder.getPanel();
 *
 * // Add a sequence of related buttons
 * ButtonBarBuilder2 builder = new ButtonBarBuilder2();
 * builder.addGlue();
 * builder.addButton(okButton, cancelButton, applyButton);
 * return builder.getPanel();
 *
 * // Add a sequence of related buttons for given Actions
 * ButtonBarBuilder2 builder = new ButtonBarBuilder2();
 * builder.addGlue();
 * builder.addButton(okAction, cancelAction, applyAction);
 * return builder.getPanel();
 * </pre>
 *
 * Buttons are added to a builder individually or as a sequence.
 *
 * To honor the platform's button order (left-to-right vs. right-to-left)
 * this builder uses the <em>leftToRightButtonOrder</em> property.
 * It is initialized with the current LayoutStyle's button order,
 * which in turn is left-to-right on most platforms and right-to-left
 * on the Mac OS X. Builder methods that create sequences of buttons
 * (e.g. {@link #addButton(JComponent[])} honor the button order.
 * If you want to ignore the default button order, you can either
 * add individual buttons, or create a ButtonBarBuilder2 instance
 * with the order set to left-to-right. For the latter see
 * {@link #createLeftToRightBuilder()}. Also see the button order
 * example below.<p>
 *
 * <strong>Example:</strong><br>
 * The following example builds a button bar with <i>Help</i> button on the
 * left-hand side and <i>OK, Cancel, Apply</i> buttons on the right-hand side.
 * <pre>
 * private JPanel createHelpOKCancelApplyBar(
 *         JButton help, JButton ok, JButton cancel, JButton apply) {
 *     ButtonBarBuilder2 builder = new ButtonBarBuilder2();
 *     builder.addButton(help);
 *     builder.addUnrelatedGap();
 *     builder.addGlue();
 *     builder.addButton(new JButton[]{ok, cancel, apply});
 *     return builder.getPanel();
 * }
 * </pre><p>
 *
 * <strong>Button Order Example:</strong><br>
 * The following example builds three button bars where one honors
 * the platform's button order and the other two ignore it.
 * <pre>
 * public JComponent buildPanel() {
 *     FormLayout layout = new FormLayout("pref");
 *     DefaultFormBuilder rowBuilder = new DefaultFormBuilder(layout);
 *     rowBuilder.setDefaultDialogBorder();
 *
 *     rowBuilder.append(buildButtonSequence(new ButtonBarBuilder2()));
 *     rowBuilder.append(buildButtonSequence(ButtonBarBuilder2.createLeftToRightBuilder()));
 *     rowBuilder.append(buildIndividualButtons(new ButtonBarBuilder2()));
 *
 *     return rowBuilder.getPanel();
 * }
 *
 * private Component buildButtonSequence(ButtonBarBuilder2 builder) {
 *     builder.addButton(new JButton[] {
 *             new JButton("One"),
 *             new JButton("Two"),
 *             new JButton("Three")
 *     });
 *     return builder.getPanel();
 * }
 *
 * private Component buildIndividualButtons(ButtonBarBuilder2 builder) {
 *     builder.addButton(new JButton("One"));
 *     builder.addRelatedGap();
 *     builder.addButton(new JButton("Two"));
 *     builder.addRelatedGap();
 *     builder.addButton(new JButton("Three"));
 *     return builder.getPanel();
 * }
 * </pre>
 *
 * @author	Karsten Lentzsch
 * @version $Revision: 1.9 $
 *
 * @see ButtonStackBuilder
 * @see com.jgoodies.forms.factories.ButtonBarFactory
 * @see com.jgoodies.forms.util.LayoutStyle
 */
public class ButtonBarBuilder2 extends AbstractButtonPanelBuilder {

    /**
     * Specifies the columns of the initial FormLayout used in constructors.
     */
    private static final ColumnSpec[] COL_SPECS  =
        new ColumnSpec[]{};

    /**
     * Specifies the FormLayout's the single button bar row.
     */
    private static final RowSpec[] ROW_SPECS  =
        new RowSpec[]{ RowSpec.decode("center:pref") };


    /**
     * The client property key used to indicate that a button shall
     * get narrow margins on the left and right hand side.<p>
     *
     * This optional setting will be honored by all JGoodies Look&amp;Feel
     * implementations. The Mac Aqua l&amp;f uses narrow margins only.
     * Other look&amp;feel implementations will likely ignore this key
     * and so may render a wider button margin.
     */
    private static final String NARROW_KEY = "jgoodies.isNarrow";


    /**
     * Describes how sequences of buttons are added to the button bar:
     * left-to-right or right-to-left. This setting is initialized using
     * the current {@link LayoutStyle}'s button order. It is honored
     * only by builder methods that build sequences of button, for example
     * {@link #addButton(JComponent[])}, and ignored if you add
     * individual button, for example using {@link #addButton(JComponent)}.
     *
     * @see #isLeftToRight()
     * @see #setLeftToRight(boolean)
     * @see #addGrowing(JComponent[])
     */
    private boolean leftToRight;


    // Instance Creation ****************************************************

    /**
     * Constructs an empty ButtonBarBuilder2 on a JPanel.
     */
    public ButtonBarBuilder2() {
        this(new JPanel(null));
    }


    /**
     * Constructs an empty ButtonBarBuilder2 on the given panel.
     *
     * @param panel  the layout container
     */
    public ButtonBarBuilder2(JPanel panel) {
        super(new FormLayout(COL_SPECS, ROW_SPECS), panel);
        leftToRight = LayoutStyle.getCurrent().isLeftToRightButtonOrder();
        setOpaque(false);
    }


    /**
     * Creates and returns a ButtonBarBuilder2 with
     * a left to right button order.
     *
     * @return a button bar builder with button order set to left-to-right
     */
    public static ButtonBarBuilder2 createLeftToRightBuilder() {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.setLeftToRightButtonOrder(true);
        return builder;
    }


    // Accessing Properties *************************************************

    /**
     * Returns whether button sequences will be ordered from
     * left to right or from right to left.
     *
     * @return true if button sequences are ordered from left to right
     *
     * @see LayoutStyle#isLeftToRightButtonOrder()
     */
    public boolean isLeftToRightButtonOrder() {
        return leftToRight;
    }


    /**
     * Sets the order for button sequences to either left to right,
     * or right to left.
     *
     * @param newButtonOrder  true if button sequences shall be ordered
     *     from left to right
     *
     * @see LayoutStyle#isLeftToRightButtonOrder()
     */
    public void setLeftToRightButtonOrder(boolean newButtonOrder) {
        leftToRight = newButtonOrder;
    }


    // Default Borders ******************************************************

    /**
     * Sets a default border that has a gap in the bar's north.
     */
    public void setDefaultButtonBarGapBorder() {
        setBorder(Borders.BUTTON_BAR_GAP_BORDER);
    }


    // Spacing ****************************************************************

    /**
     * Adds a glue that will be given the extra space,
     * if this button bar is larger than its preferred size.
     */
    public void addGlue() {
        appendGlueColumn();
        nextColumn();
    }


    /**
     * Adds the standard horizontal gap for related components.
     *
     * @see LayoutStyle#getRelatedComponentsPadX()
     */
    public void addRelatedGap() {
        appendRelatedComponentsGapColumn();
        nextColumn();
    }


    /**
     * Adds the standard horizontal gap for unrelated components.
     *
     * @see LayoutStyle#getUnrelatedComponentsPadX()
     */
    public void addUnrelatedGap() {
        appendUnrelatedComponentsGapColumn();
        nextColumn();
    }


    /**
     * Adds a horizontal strut of the specified width.
     * For related and unrelated components use {@link #addRelatedGap()}
     * and {@link #addUnrelatedGap()} respectively.
     *
     * @param width  describes the gap width
     *
     * @see ColumnSpec#createGap(ConstantSize)
     */
    public void addStrut(ConstantSize width) {
        getLayout().appendColumn(ColumnSpec.createGap(width));
        nextColumn();
    }


    /**
     * Adds a command button component that has a minimum width
     * specified by the {@link LayoutStyle#getDefaultButtonWidth()}.<p>
     *
     * Although a JButton is expected, any JComponent is accepted
     * to allow custom button component types.
     *
     * @param button  the component to add
     *
     * @throws NullPointerException if {@code button} is {@code null}
     */
    public void addButton(JComponent button) {
        button.putClientProperty(NARROW_KEY, Boolean.TRUE);
        getLayout().appendColumn(FormFactory.BUTTON_COLSPEC);
        add(button);
        nextColumn();
    }


    /**
     * Adds a sequence of related button components.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new JComponent[]{button1, button2});</code>
     *
     * @param button1    the first button to add
     * @param button2    the second button to add
     *
     * @throws NullPointerException if a button is {@code null}
     *
     * @see #addButton(JComponent[])
     */
    public void addButton(
            JComponent button1,
            JComponent button2) {
        addButton(new JComponent[]{button1, button2});
    }


    /**
     * Adds a sequence of related button components.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new JComponent[]{button1, button2, button3});</code>
     *
     * @param button1    the first button to add
     * @param button2    the second button to add
     * @param button3    the third button to add
     *
     * @throws NullPointerException if a button is {@code null}
     *
     * @see #addButton(JComponent[])
     */
    public void addButton(
            JComponent button1,
            JComponent button2,
            JComponent button3) {
        addButton(new JComponent[]{button1, button2, button3});
    }


    /**
     * Adds a sequence of related button components.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new JComponent[]{button1, button2, button3, button4});</code>
     *
     * @param button1    the first button to add
     * @param button2    the second button to add
     * @param button3    the third button to add
     * @param button4    the fourth button to add
     *
     * @throws NullPointerException if a button is {@code null}
     *
     * @see #addButton(JComponent[])
     */
    public void addButton(
            JComponent button1,
            JComponent button2,
            JComponent button3,
            JComponent button4) {
        addButton(new JComponent[]{button1, button2, button3, button4});
    }


    /**
     * Adds a sequence of related button components.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new JComponent[]{button1, button2, button3, button4, button5});</code>
     *
     * @param button1    the first button to add
     * @param button2    the second button to add
     * @param button3    the third button to add
     * @param button4    the fourth button to add
     * @param button5    the fifth button to add
     *
     * @throws NullPointerException if a button is {@code null}
     *
     * @see #addButton(JComponent[])
     */
    public void addButton(
            JComponent button1,
            JComponent button2,
            JComponent button3,
            JComponent button4,
            JComponent button5) {
        addButton(new JComponent[]{button1, button2, button3, button4, button5});
    }


    /**
     * Adds a sequence of related button components.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * Uses this builder's button order (left-to-right vs. right-to-left).
     * If you  want to use a fixed order, add individual buttons instead.<p>
     *
     * Although JButtons are expected, general JComponents are accepted
     * to allow custom button component types.
     *
     * @param buttons  an array of buttons to add
     *
     * @throws NullPointerException if the button array or a button is {@code null}
     * @throws IllegalArgumentException if the button array is empty
     *
     * @see #addButton(JComponent)
     */
    public void addButton(JComponent[] buttons) {
        if (buttons == null)
            throw new NullPointerException("The button array must not be null.");
        int length = buttons.length;
        if (length == 0)
            throw new IllegalArgumentException("The button array must not be empty.");
        for (int i = 0; i < length; i++) {
            int index = leftToRight ? i : length -1 - i;
            addButton(buttons[index]);
            if (i < buttons.length - 1)
                addRelatedGap();
        }
    }


    /**
     * Adds a JButton for the given Action that has a minimum width
     * specified by the {@link LayoutStyle#getDefaultButtonWidth()}.
     *
     * @param action  the action that describes the button to add
     *
     * @throws NullPointerException if {@code action} is {@code null}
     *
     * @see #addButton(JComponent)
     */
    public void addButton(Action action) {
        if (action == null)
            throw new NullPointerException("The button Action must not be null.");
        addButton(new JButton(action));
    }


    /**
     * Adds a sequence of related JButtons built from the given Actions.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new Action[]{action1, action2});</code>
     *
     * @param action1    describes the first button to add
     * @param action2    describes the second button to add
     *
     * @throws NullPointerException if an Action is {@code null}
     *
     * @see #addButton(Action[])
     * @see #addButton(JComponent[])
     */
    public void addButton(
            Action action1,
            Action action2) {
        addButton(new Action[]{action1, action2});
    }


    /**
     * Adds a sequence of related JButtons built from the given Actions.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new Action[]{action1, action2, action3});</code>
     *
     * @param action1    describes the first button to add
     * @param action2    describes the second button to add
     * @param action3    describes the third button to add
     *
     * @throws NullPointerException if an Action is {@code null}
     *
     * @see #addButton(Action[])
     * @see #addButton(JComponent[])
     */
    public void addButton(
            Action action1,
            Action action2,
            Action action3) {
        addButton(new Action[]{action1, action2, action3});
    }


    /**
     * Adds a sequence of related JButtons built from the given Actions.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new Action[]{action1, action2, action3, action4});</code>
     *
     * @param action1    describes the first button to add
     * @param action2    describes the second button to add
     * @param action3    describes the third button to add
     * @param action4    describes the fourth button to add
     *
     * @throws NullPointerException if an Action is {@code null}
     *
     * @see #addButton(Action[])
     * @see #addButton(JComponent[])
     */
    public void addButton(
            Action action1,
            Action action2,
            Action action3,
            Action action4) {
        addButton(new Action[]{action1, action2, action3, action4});
    }


    /**
     * Adds a sequence of related JButtons built from the given Actions.
     * Each button has the minimum width as specified by
     * {@link LayoutStyle#getDefaultButtonWidth()}. The gap width between
     * the buttons is {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * This method is equivalent to
     * <code>addButton(new Action[]{action1, action2, action3, action4, action5});</code>
     *
     * @param action1    describes the first button to add
     * @param action2    describes the second button to add
     * @param action3    describes the third button to add
     * @param action4    describes the fourth button to add
     * @param action5    describes the fifth button to add
     *
     * @throws NullPointerException if an Action is {@code null}
     *
     * @see #addButton(Action[])
     * @see #addButton(JComponent[])
     */
    public void addButton(
            Action action1,
            Action action2,
            Action action3,
            Action action4,
            Action action5) {
        addButton(new Action[]{action1, action2, action3, action4, action5});
    }


    /**
     * Adds a sequence of related JButtons built from the given Actions
     * that are separated by the default gap as specified by
     * {@link LayoutStyle#getRelatedComponentsPadX()}.<p>
     *
     * Uses this builder's button order (left-to-right vs. right-to-left).
     * If you  want to use a fixed order, add individual Actions instead.
     *
     * @param actions   the Actions that describe the buttons to add
     *
     * @throws NullPointerException if the Action array or an Action is {@code null}
     * @throws IllegalArgumentException if the Action array is empty
     *
     * @see #addButton(JComponent[])
     */
    public void addButton(Action[] actions) {
        if (actions == null)
            throw new NullPointerException("The Action array must not be null.");
        int length = actions.length;
        if (length == 0)
            throw new IllegalArgumentException("The Action array must not be empty.");
        JButton[] buttons = new JButton[length];
        for (int i = 0; i < length; i++) {
            buttons[i] = new JButton(actions[i]);
        }
        addButton(buttons);
    }


    // Other ******************************************************************

    /**
     * Adds a button or other component that grows if the container grows.
     * The component's initial size (before it grows) is specified
     * by the {@link LayoutStyle#getDefaultButtonWidth()}.
     *
     * @param component  the component to add
     */
    public void addGrowing(JComponent component) {
        getLayout().appendColumn(FormFactory.GROWING_BUTTON_COLSPEC);
        component.putClientProperty(NARROW_KEY, Boolean.TRUE);
        add(component);
        nextColumn();
    }


    /**
     * Adds a sequence of related growing buttons
     * where each is separated by a default gap.
     * Honors this builder's button order. If you
     * want to use a fixed left to right order,
     * add individual buttons.
     *
     * @param buttons  an array of buttons to add
     *
     * @see LayoutStyle
     */
    public void addGrowing(JComponent[] buttons) {
        int length = buttons.length;
        for (int i = 0; i < length; i++) {
            int index = leftToRight ? i : length -1 - i;
            addGrowing(buttons[index]);
            if (i < buttons.length - 1)
                addRelatedGap();
        }
    }


    /**
     * Adds a fixed size component with narrow margin. Unlike the gridded
     * components, this component keeps its individual preferred dimension.
     *
     * @param component  the component to add
     */
    public void addFixed(JComponent component) {
        component.putClientProperty(NARROW_KEY, Boolean.TRUE);
        getLayout().appendColumn(FormFactory.PREF_COLSPEC);
        add(component);
        nextColumn();
    }


}
