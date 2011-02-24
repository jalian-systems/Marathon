/*
    VLDocking Framework 3.0
    Copyright VLSOLUTIONS, 2004-2009
    
    email : info at vlsolutions.com
------------------------------------------------------------------------
This software is distributed under the LGPL license

The fact that you are presently reading this and using this class means that you have had
knowledge of the LGPL license and that you accept its terms.

You can read the complete license here :

    http://www.gnu.org/licenses/lgpl.html

*/

package com.vlsolutions.swing.docking;

import java.beans.*;

import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import com.vlsolutions.swing.docking.animation.ComponentAnimator;
import com.vlsolutions.swing.docking.animation.*;
import java.lang.reflect.Method;
import javax.swing.border.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** A component used to show the currently expanded view.
 * <p>
 * Auto-Hide / Auto-Expand feature is a means to save space of screen replacing
 * a Dockable by a Button on one of the Desktop borders.
 * <p> When the user clicks on the button or his mouse rolls over it, the component
 * is shown (with an expansion animation) as if it was coming from behind the button's
 * border.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 2.0
 * @update 2005/10/06 Lilian Chamontin : protected access to the exansion timer that might be
 * null when AutoHidePolicy is EXPAND_ON_CLICK
 * @update 2005/11/01 Lilian Chamontin : enhanced timer management to auto-collaspe the panel
 * when mouse out of bounds
 * @update 2005/12/08 Lilian Chamontin : updated the order of component insertion for JDIC support.
 * @update 2006/12/19 Lilian Chamontin : fixed a memory leak issue.
 * @update 2007/01/08 Lilian Chamontin : updated to use the new titlebar factory method
 *
 * */
public class AutoHideExpandPanel extends JPanel implements SingleDockableContainer {
    public static final String uiClassID = "AutoHideExpandPanelUI";

    /** Constant for the Expanded property (a bound property) */
    public static final String PROPERTY_EXPANDED = "AutoHideExpanded";

    private Timer expansionTimer;

    private boolean isRolloverTimer = false;

    private boolean isExpanding = false;
    // true during animation phase, will be used to avoid concurrent animations (@todo)

    private HashMap <Component, Dimension> savedDimensions = new HashMap(); // Component/Dimension

    ExpandControler controler = new ExpandControler(); // hide event listener

    private AutoHideButton selectedButton;

    private Component expandedComponent; // current component

    DockingDesktop desk;

    private JPanel topDragger = getTopDragger(); // indirection because those components are used by the ui delegate

    private JPanel leftDragger = getLeftDragger();

    private JPanel bottomDragger = getBottomDragger();

    private JPanel rightDragger = getRightDragger();

    private JPanel lastDragger; // last used dragger

    private JPanel content = new JPanel(new BorderLayout()); // content displayed (user component)

    private Panel heavyPanel; // used only when mixing lightweight and heavyweight components

    private DockViewTitleBar titleBar = createTitleBar();

    private AnimationControler animationControler = new AnimationControler();

    private Border expandFromTopBorder,  expandFromLeftBorder,  expandFromBottomBorder,  expandFromRightBorder;

    /** this boolean is used to disable auto-hiding temporarily, especially during
     * drag operation (where mouse can leave the component) */
    private boolean shouldCollapse = true;

    /** flag used when heavywieght usage + single heavyweight component */
    private boolean isHeavyPanelInstalled = false;

    /** Used by the collapse timer*/
    private long lastTimeMouseWasIn = 0;

    private boolean canUseMouseInfo = DockingUtilities.canUseMouseInfo();

    private FocusCollapser collapser = new FocusCollapser(); // 2006/12/19

    /** Timer used to collapse the expand panel (when mouse is out of bounds).
     * (only for java > 1.5)
     */
    private javax.swing.Timer collapseTimer // 2005/11/01
            = new javax.swing.Timer(250, new ActionListener() {
        // timer used to hide the expanded panel when mouse is out too long
        public void actionPerformed(ActionEvent actionEvent) {
            // all this mess to allow compilation from java 1.4
            Point mouseLocation = DockingUtilities.getMouseLocation();
            if (mouseLocation == null) {
                return;
            }
            Point p = new Point();
            SwingUtilities.convertPointToScreen(p, AutoHideExpandPanel.this);
            Rectangle expandPanelBounds = new Rectangle(p.x, p.y, getWidth(), getHeight());
            if (expandPanelBounds.contains(mouseLocation)) {
                // we're inside the expand panel
                lastTimeMouseWasIn = System.currentTimeMillis();
            } else {
                // not inside the component : check also into its associated button
                Point btnPoint = new Point();//selectedButton.getX(),selectedButton.getY());
                SwingUtilities.convertPointToScreen(btnPoint, selectedButton);
                Rectangle btnRect = new Rectangle(btnPoint.x, btnPoint.y, selectedButton.getWidth(), selectedButton.getHeight());
                if (btnRect.contains(mouseLocation)) {
                    lastTimeMouseWasIn = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - lastTimeMouseWasIn > 1000) {
                        // it's time to collapse
                        if (!isActive() && shouldCollapse()) {
                            // do not hide it if it has got the focus
                            // or if a non-collapsible operation is occuring
                            if (selectedButton != null) {
                                collapse();
                            }
                        }
                    }
                }
            }

        }
    });

    public AutoHideExpandPanel() {
        super(new BorderLayout());

        if (!DockingPreferences.isLightWeightUsageEnabled()) {
            this.heavyPanel = new Panel(new BorderLayout());
        }


        if (AutoHidePolicy.getPolicy().getExpandMode() ==
                AutoHidePolicy.ExpandMode.EXPAND_ON_ROLLOVER) {
            expansionTimer = new Timer(AutoHidePolicy.getPolicy().
                    getRolloverTriggerDelay(), controler);
            expansionTimer.setRepeats(false);
            isRolloverTimer = true;
        }


        initBorders();

        setFocusCycleRoot(true); // keep keyboard focus

        //requiered to trap mouse events under the panel (so they don't go to the mousegrabber)
        addMouseListener(new MouseAdapter() {
        });

        if (DockingPreferences.isLightWeightUsageEnabled()) {
            // for Swing only : direct usage of the content jpanel
            add(content, BorderLayout.CENTER);
        } else {
            if (!DockingPreferences.isSingleHeavyWeightComponent()) {
                // when mixing heavyweight and lightweight component, we have
                // to ensure our content will not be covered by an underlying heavy component.
                // to do this, we add an intermediary heavyweight panel.
                // note : if there is only one heavyweight dockable,
                //        we delay and delegate this operation to installHeavyWeightIfNeeded
                heavyPanel.add(content, BorderLayout.CENTER);

                // jdk1.5 only, but we compile with 1.4 source level
                try {
                    // this.setComponentZOrder(heavyPanel, 0); // top most
                    Method m = Container.class.getMethod("setComponentZOrder", new Class[]{
                                Component.class, int.class});
                    m.invoke(this, new Object[]{heavyPanel, new Integer(0)});
                } catch (Exception ignore) {
                }

                add(heavyPanel, BorderLayout.CENTER);
            } else {
                // (heavy + single) will be dynamically changed depending on the expanded dockable
                add(content, BorderLayout.CENTER);
                add(topDragger, BorderLayout.NORTH);
                add(bottomDragger, BorderLayout.SOUTH);
                add(leftDragger, BorderLayout.WEST);
                add(rightDragger, BorderLayout.EAST);
            }
        }

        addAncestorListener(new AncestorListener() { //2006/12/19 : reworked to avoid GC leak
            public void ancestorAdded(AncestorEvent event) {
                AutoHidePolicy.getPolicy().addPropertyChangeListener(controler);
                if (DockingPreferences.isLightWeightUsageEnabled()) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                            "focusOwner", collapser);
                }
            }

            public void ancestorMoved(AncestorEvent event) {
            }

            public void ancestorRemoved(AncestorEvent event) {
                AutoHidePolicy.getPolicy().removePropertyChangeListener(controler);
                if (DockingPreferences.isLightWeightUsageEnabled()) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(
                            "focusOwner", collapser);
                }
            }
        });



        content.add(titleBar, BorderLayout.NORTH);
//    initDockingFunctions();
    }

    private void installHeavyWeightParentIfNeeded(Dockable target) {
        if (DockingPreferences.isLightWeightUsageEnabled()) {
            return; // not needed as we are in pure lightweight
        }
        if (!DockingPreferences.isSingleHeavyWeightComponent()) {
            return; // not possible as there are multiple heavyweight
        }

        if (DockingUtilities.isHeavyWeightComponent(target.getComponent())) {
            // no need to install the heavypanel
            if (isHeavyPanelInstalled) {
                // we even have to remove it
                this.removeAll();
                this.add(content, BorderLayout.CENTER);
                add(topDragger, BorderLayout.NORTH);
                add(bottomDragger, BorderLayout.SOUTH);
                add(leftDragger, BorderLayout.WEST);
                add(rightDragger, BorderLayout.EAST);

                revalidate();
                isHeavyPanelInstalled = false;
            }
        } else {
            if (isHeavyPanelInstalled) {
                // nothing more to do, content is already inside heavypanel
            } else {
                this.removeAll();
                this.add(heavyPanel, BorderLayout.CENTER);
                heavyPanel.add(content, BorderLayout.CENTER);
                heavyPanel.add(topDragger, BorderLayout.NORTH);
                heavyPanel.add(bottomDragger, BorderLayout.SOUTH);
                heavyPanel.add(leftDragger, BorderLayout.WEST);
                heavyPanel.add(rightDragger, BorderLayout.EAST);

                isHeavyPanelInstalled = true;
                revalidate();
                // jdk1.5 only, but we compile with 1.4 source level
                try {
                    // this.setComponentZOrder(heavyPanel, 0); // top most
                    Method m = Container.class.getMethod("setComponentZOrder", new Class[]{
                                Component.class, int.class});
                    m.invoke(this, new Object[]{heavyPanel, new Integer(0)});
                } catch (Exception ignore) {
                }

            }
        }
    }

    public boolean isOptimizedDrawingEnabled() {
        return DockingPreferences.isLightWeightUsageEnabled();
    // only when lightweight components (to ensure correct zorder for AWT)
    }

    /** Returns true if this panel is the ancestor of the focused component */
    public boolean isActive() {
        if (titleBar.isActive()) {
            return true;
        } else {
            // since 2.1 : the autohide component can contain a nested set of dockables, so the
            // titlebar's activity isn't always usable : we have to verify if the focus is still
            // inside
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner == null) {
                return false;
            } else {
                return isAncestorOf(focusOwner);
            }
        }
    //return titleBar.isActive();
    }

    /** Returns true if this panel agrees to beeing hidden.
     * <p>
     * During drag operations (resizing), some mouseEnter/mouseExit events
     * can be lost. In that case, the desktop relies on this method to
     * request collaping or not.
     *  */
    public boolean shouldCollapse() {
        return shouldCollapse;
    }

    /** Returns the component used to modify the expand panel size when expanded from the bottom */
    public JPanel getTopDragger() {
        /* This method is used by the UI to install proper borders */
        if (topDragger == null) {
            topDragger = new JPanel();
        }
        return topDragger;
    }

    /** Returns the component used to modify the expand panel size when expanded from the right */
    public JPanel getLeftDragger() {
        /* This method is used by the UI to install proper borders */
        if (leftDragger == null) {
            leftDragger = new JPanel();
        }
        return leftDragger;
    }

    /** Returns the component used to modify the expand panel size when expanded from the top */
    public JPanel getBottomDragger() {
        /* This method is used by the UI to install proper borders */
        if (bottomDragger == null) {
            bottomDragger = new JPanel();
        }
        return bottomDragger;
    }

    /** Returns the component used to modify the expand panel size when expanded from the right */
    public JPanel getRightDragger() {
        /* This method is used by the UI to install proper borders */
        if (rightDragger == null) {
            rightDragger = new JPanel();
        }
        return rightDragger;
    }

    /** creates the shared title bar for all expanded panels */
    protected DockViewTitleBar createTitleBar() {
        return DockableContainerFactory.getFactory().createTitleBar(); //2007/01/08
    //return new DockViewTitleBar();
    }

    public DockViewTitleBar getTitleBar() {
        return this.titleBar;
    }

    /** Creates the default borders for the expand panel */
    public void resetBorders() {
        Color shadow = UIManager.getColor("VLDocking.shadow");
        Color highlight = UIManager.getColor("VLDocking.highlight");
        if (highlight == null){
            highlight = shadow.brighter();
        }
        getTopDragger().setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, shadow));
        getLeftDragger().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, shadow));
        getBottomDragger().setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, shadow));
        getRightDragger().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, shadow));

        expandFromTopBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 0, 1, shadow),
                BorderFactory.createMatteBorder(1, 1, 1, 0, highlight)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
        expandFromLeftBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 0, shadow),
                BorderFactory.createMatteBorder(1, 1, 1, 1, highlight)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
        expandFromBottomBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, shadow),
                BorderFactory.createMatteBorder(1, 1, 1, 1, highlight)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
        expandFromRightBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 1, shadow),
                BorderFactory.createMatteBorder(1, 1, 1, 1, highlight)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));


    }

    /** Installs borders used to drag the expand panel around */
    protected void initBorders() {
        Dimension min = new Dimension(4, 4);

        topDragger.setVisible(false);
        topDragger.setPreferredSize(min);
        topDragger.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        DragListener topListener = new DragListener(DockingConstants.INT_HIDE_BOTTOM);
        topDragger.addMouseMotionListener(topListener);
        topDragger.addMouseListener(topListener);

        leftDragger.setVisible(false);
        leftDragger.setPreferredSize(min);
        leftDragger.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        DragListener leftListener = new DragListener(DockingConstants.INT_HIDE_RIGHT);
        leftDragger.addMouseMotionListener(leftListener);
        leftDragger.addMouseListener(leftListener);

        bottomDragger.setVisible(false);
        bottomDragger.setPreferredSize(min);
        bottomDragger.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        DragListener bottomListener = new DragListener(DockingConstants.INT_HIDE_TOP);
        bottomDragger.addMouseMotionListener(bottomListener);
        bottomDragger.addMouseListener(bottomListener);

        rightDragger.setVisible(false);
        rightDragger.setPreferredSize(min);
        rightDragger.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        DragListener rightListener = new DragListener(DockingConstants.INT_HIDE_LEFT);
        rightDragger.addMouseMotionListener(rightListener);
        rightDragger.addMouseListener(rightListener);

        if (DockingPreferences.isLightWeightUsageEnabled()) {
            add(topDragger, BorderLayout.NORTH);
            add(bottomDragger, BorderLayout.SOUTH);
            add(leftDragger, BorderLayout.WEST);
            add(rightDragger, BorderLayout.EAST);
        } else {
            if (!DockingPreferences.isSingleHeavyWeightComponent()) {
                heavyPanel.add(topDragger, BorderLayout.NORTH);
                heavyPanel.add(bottomDragger, BorderLayout.SOUTH);
                heavyPanel.add(leftDragger, BorderLayout.WEST);
                heavyPanel.add(rightDragger, BorderLayout.EAST);
            }
        }


    }

    private void initDockingFunctions() {
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(DockViewTitleBar.PROPERTY_AUTOHIDE)) {
                    // from autohide to dock
                    // remember values after autohide
                    Dockable d = selectedButton.getDockable();
                    desk.setAutoHide(d, false);
                } else if (e.getPropertyName().equals(DockViewTitleBar.PROPERTY_CLOSED)) {
                    Dockable d = selectedButton.getDockable();
                    collapse();
                    desk.close(d);
                } else if (e.getPropertyName().equals(DockViewTitleBar.PROPERTY_DRAGGED)) {
                    // the user is starting a drag gesture : we must collapse !
                    collapse();
                } else if (e.getPropertyName().equals(DockViewTitleBar.PROPERTY_FLOAT)) {
                    Dockable d = selectedButton.getDockable();
                    collapse();
                    desk.setFloating(d, true);
                }
            }
        };

        titleBar.addPropertyChangeListener(DockViewTitleBar.PROPERTY_AUTOHIDE, listener);
        titleBar.addPropertyChangeListener(DockViewTitleBar.PROPERTY_CLOSED, listener);
        titleBar.addPropertyChangeListener(DockViewTitleBar.PROPERTY_DRAGGED, listener);
        titleBar.addPropertyChangeListener(DockViewTitleBar.PROPERTY_FLOAT, listener);
        titleBar.installDocking(desk);

        desk.installDockableDragSource(titleBar);

    }

    /** Returns the component responsible for managing auto-expansion.
     * */
    /* package protected */ ExpandControler getControler() {
        return controler;
    }

    /** Selects a button (may collapse a prevoiusly selected button) */
    public void select(AutoHideButton btn) {
        if (selectedButton != null && selectedButton != btn) {
            collapse();
        }
        selectedButton = btn;
        btn.setSelected(true);
    }

    private void restartCollapseTimer() {
//    if (!DockingPreferences.isLightWeightUsageEnabled()){
        if (canUseMouseInfo) { // not for 1.4
            this.lastTimeMouseWasIn = System.currentTimeMillis();
            collapseTimer.restart();
        }
    }

    private void stopCollapseTimer() {
//    if (!DockingPreferences.isLightWeightUsageEnabled()){
        if (canUseMouseInfo) { // not for 1.4
            collapseTimer.stop();
        }
    }

    /**  Expands the currently selected button */
    public void expand() {
        if (selectedButton == null) {
            return;//2007/01/10
        //throw new IllegalStateException("No button selected for expansion");
        }


        installComponent();
        restartCollapseTimer();

        setVisible(true);

        Rectangle bounds = desk.getBounds();

        AutoHidePolicy policy = AutoHidePolicy.getPolicy();
        int model = policy.getInitialExpansionModel();
        Dimension bestDimension = null;
        if (model == AutoHidePolicy.INITIAL_EXPAND_COMPONENT_SIZE) {
            bestDimension = savedDimensions.get(selectedButton);
            if (bestDimension == null) {
                bestDimension = selectedButton.getDockable().getComponent().getPreferredSize();
                switch (selectedButton.getZone()) {
                    case DockingConstants.INT_HIDE_TOP:
                    case DockingConstants.INT_HIDE_BOTTOM:
                        if (bestDimension.height > bounds.height / 2) {
                            bestDimension.height = bounds.height / 2;
                        }
                        break;
                    case DockingConstants.INT_HIDE_LEFT:
                    case DockingConstants.INT_HIDE_RIGHT:
                        if (bestDimension.width > bounds.width / 2) {
                            bestDimension.width = bounds.width / 2;
                        }
                        break;
                }
            }
        } else if (model == AutoHidePolicy.INITIAL_EXPAND_CUSTOM_SIZE) {
            bestDimension = new Dimension();
            switch (selectedButton.getZone()) {
                case DockingConstants.INT_HIDE_TOP:
                case DockingConstants.INT_HIDE_BOTTOM:
                    bestDimension.height = policy.getInitialExpansionHeight();
                    if (bestDimension.height > bounds.height / 2) {
                        bestDimension.height = bounds.height / 2;
                    }
                    break;
                case DockingConstants.INT_HIDE_LEFT:
                case DockingConstants.INT_HIDE_RIGHT:
                    bestDimension.width = policy.getInitialExpansionWidth();
                    if (bestDimension.width > bounds.width / 2) {
                        bestDimension.width = bounds.width / 2;
                    }
                    break;
            }
        } else {
            throw new RuntimeException("invalid Initial Expansion model : " + model);
        }

        // outer insets (including autohide borders)
        Insets i = desk.getDockingPanelInsets();

        // inner insets (of expandPanel : mouse grabbers etc..)
        Insets i2 = getComponentInsets();


        switch (selectedButton.getZone()) {
            case DockingConstants.INT_HIDE_TOP:
                new ComponentAnimator(this,
                        new Rectangle(i.left,
                        i.top,
                        bounds.width - i.left - i.right, 0),
                        new Rectangle(i.left,
                        i.top,
                        bounds.width - i.left - i.right,
                        bestDimension.height + i2.top + i2.bottom),
                        AutoHidePolicy.getPolicy().getExpansionDuration() / 1000f,
                        animationControler);
                break;
            case DockingConstants.INT_HIDE_BOTTOM:
                new ComponentAnimator(this,
                        new Rectangle(i.left,
                        bounds.height - i.bottom,
                        bounds.width - i.left - i.right, 0),
                        new Rectangle(i.left,
                        bounds.height - bestDimension.height - i.bottom - i2.top - i2.bottom,
                        bounds.width - i.left - i.right,
                        bestDimension.height + i2.top + i2.bottom),
                        AutoHidePolicy.getPolicy().getExpansionDuration() / 1000f,
                        animationControler);
                break;
            case DockingConstants.INT_HIDE_LEFT:
                new ComponentAnimator(this,
                        new Rectangle(i.left,
                        i.top,
                        0, bounds.height - i.top - i.bottom),
                        new Rectangle(i.left,
                        i.top,
                        bestDimension.width + i2.left + i2.right,
                        bounds.height - i.top - i.bottom),
                        AutoHidePolicy.getPolicy().getExpansionDuration() / 1000f,
                        animationControler);
                break;
            case DockingConstants.INT_HIDE_RIGHT:
                new ComponentAnimator(this,
                        new Rectangle(bounds.width - i.right,
                        i.top,
                        0, bounds.height - i.top - i.bottom),
                        new Rectangle(bounds.width - bestDimension.width - i.right - i2.left - i2.right,
                        i.top,
                        bestDimension.width + i2.left + i2.right,
                        bounds.height - i.top - i.bottom),
                        AutoHidePolicy.getPolicy().getExpansionDuration() / 1000f,
                        animationControler);
                break;
            default:
                assert false;
        }

        firePropertyChange(PROPERTY_EXPANDED, false, true);

    }

    /** Removes a dockable (if it was the currently expanded one) */
    public void remove(Dockable dockable) {
        Component comp = dockable.getComponent();
        if (expandedComponent != null && expandedComponent == comp) {
            content.remove(expandedComponent);
            expandedComponent = null;
        }
    }

    private void installComponent() {
        final Component comp = selectedButton.getDockable().getComponent();
        if (expandedComponent != null) {
            if (expandedComponent != comp) {
                content.remove(expandedComponent);
                content.add(comp, BorderLayout.CENTER); //2005/12/08 back again in this order (JDIC workaround)
                installHeavyWeightParentIfNeeded(selectedButton.getDockable());
                expandedComponent = comp;
            }
        } else {
            content.add(comp, BorderLayout.CENTER);
            installHeavyWeightParentIfNeeded(selectedButton.getDockable());
            expandedComponent = comp;
        }

        /* // the workaround isn't needed anymore : we use the heavyweightTimer instead

        if (!DockingPreferences.isLightWeightUsageEnabled()){
        // this is a workaround : mouse listener doesn't work properly
        // on AWT components (they grab the event even if they are 'under'
        // our mouse grabber.
        SwingUtilities.invokeLater(new Runnable(){
        public void run(){
        //                heavyPanel.requestFocus();
        comp.requestFocus();
        }
        // by requesting focus, we can rely on the safer focusOwner property
        // and use it to collapse thi panel (see FocusCollapser code below)
        });
        }*/


        titleBar.setDockable(selectedButton.getDockable());
        desk.installDockableDragSource(titleBar);
        switch (selectedButton.getZone()) {
            case DockingConstants.INT_HIDE_TOP:
                content.setBorder(expandFromTopBorder);
                bottomDragger.setVisible(true);
                // remove previous border
                if (lastDragger != null && lastDragger != bottomDragger) {
                    lastDragger.setVisible(false);
                }
                lastDragger = bottomDragger;
                break;
            case DockingConstants.INT_HIDE_BOTTOM:
                content.setBorder(expandFromBottomBorder);
                topDragger.setVisible(true);
                if (lastDragger != null && lastDragger != topDragger) {
                    lastDragger.setVisible(false);
                }
                lastDragger = topDragger;
                break;
            case DockingConstants.INT_HIDE_LEFT:
                content.setBorder(expandFromLeftBorder);
                rightDragger.setVisible(true);
                if (lastDragger != null && lastDragger != rightDragger) {
                    lastDragger.setVisible(false);
                }
                lastDragger = rightDragger;
                break;
            case DockingConstants.INT_HIDE_RIGHT:
                content.setBorder(expandFromRightBorder);
                leftDragger.setVisible(true);
                if (lastDragger != null && lastDragger != leftDragger) {
                    lastDragger.setVisible(false);
                }
                lastDragger = leftDragger;
                break;
            default:
                assert false;
        }
        revalidate();
        repaint();
    }

    /** Calculates the insets needed around the center component.
     * This is the sum of border sizes and bordercomponents sizes.
     * */
    private Insets getComponentInsets() {
        Insets i = getInsets(); // borders
        Insets i2 = content.getInsets();
        i.top += i2.top;
        i.left += i2.left;
        i.bottom += i2.bottom;
        i.right += i2.right;

        if (topDragger.isVisible()) {
            i.top += topDragger.getHeight();
        }
        i.top += titleBar.getHeight();
        if (leftDragger.isVisible()) {
            i.left += leftDragger.getWidth();
        }
        if (bottomDragger.isVisible()) {
            i.bottom += bottomDragger.getHeight();
        }
        if (rightDragger.isVisible()) {
            i.right += rightDragger.getWidth();
        }

        return i;
    }

    /** Collapse the expand panel (making it unvisible) */
    public void collapse() {
        if (selectedButton != null) {
            savedDimensions.put(selectedButton, selectedButton.getDockable().getComponent().getSize());
            selectedButton.setSelected(false);
            firePropertyChange(PROPERTY_EXPANDED, true, false);
        }
        selectedButton = null;

        setVisible(false);

        titleBar.closePopUp();

        stopCollapseTimer();

        if (!DockingPreferences.isLightWeightUsageEnabled()) {
            // we'd better repaint (ugly traces on linux)
            desk.repaint();
        }

    }

    public void uninstallDocking(DockingDesktop desktop) {
        titleBar.uninstallDocking(desk);
    }

    public void installDocking(DockingDesktop desktop) {
        this.desk = desktop;
        initDockingFunctions();
    }

    public Dockable getDockable() {
        if (selectedButton != null) {
            return selectedButton.getDockable();
        } else {
            return null;
        }
    }

    public String getUIClassID() {
        return uiClassID;
    }

    /** Clears the state of this expand panel.
     *<p>
     * This is useful when re-installing a desktop from readXml (for example,
     * it resets dockable expand sizes)
     */
    public void clear() {
        // clear
        savedDimensions.clear();
    }

    /** inner class that follows animations of expansion */
    private class AnimationControler implements AnimationListener {
        public void animation(AnimationEvent e) {
            isExpanding = e.getState() != AnimationEvent.ANIMATION_END;

            if (e.getState() == AnimationEvent.ANIMATION_END) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        content.repaint();
                    }
                });

            }

        }
    }

    /** This class hides expansion implementation from API */
    private class ExpandControler implements MouseListener, ActionListener, PropertyChangeListener {
        /* temporary button : the next to be selected after a mouseEntered event and
         * triggered timer delay. */
        private AutoHideButton mouseEnteredButton;

        /** used to avoid bad interactions between focus listeners and expand controler */
        private boolean isUnderControl = false;

        public void mouseClicked(MouseEvent e) {
            // don't do anything during animation phase
            if (!isExpanding) {
                if (expansionTimer != null) { //2005/10/06
                    expansionTimer.stop();
                }

                AutoHideButton clicked = (AutoHideButton) e.getSource();
                if (selectedButton != clicked) {
                    // we need to tell the focus listener not to manage focus while
                    // this operation is occuring
                    isUnderControl = true;
                    select(clicked); // will trigger a focus change is DockingPreferences.isHeavuWeight
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            expand();
                            isUnderControl = false;
                        }
                    });
                } else {
                    collapse();
                }

            }
        }

        public void mouseEntered(MouseEvent e) {
            if (!isRolloverTimer) {
                return;
            }
            AutoHideButton btn = (AutoHideButton) e.getSource();
            if (!btn.isSelected()) { // nothing more to do if selected
                if (expansionTimer != null) {
                    expansionTimer.restart(); // 2005/10/06
                }
                mouseEnteredButton = btn;
            }
        }

        public void mouseExited(MouseEvent e) {
            if (!isRolloverTimer) {
                return;
            }

            AutoHideButton btn = (AutoHideButton) e.getSource();
            if (!btn.isSelected()) { // nothing more to do if selected
                expansionTimer.stop();
                mouseEnteredButton = null;
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void propertyChange(PropertyChangeEvent e) {
            /* triggered by autohide policy change*/
            if (e.getPropertyName().equals(AutoHidePolicy.PROPERTY_EXPAND_MODE)) {
                if (AutoHidePolicy.getPolicy().getExpandMode() ==
                        AutoHidePolicy.ExpandMode.EXPAND_ON_ROLLOVER) {
                    expansionTimer = new Timer(AutoHidePolicy.getPolicy().
                            getRolloverTriggerDelay(), this);
                    isRolloverTimer = true;
                } else {
                    expansionTimer.stop();
                    expansionTimer = null;
                    isRolloverTimer = false;
                }
            } else if (e.getPropertyName().equals(AutoHidePolicy.PROPERTY_ROLLOVER_TRIGGER_DELAY)) {
                if (AutoHidePolicy.getPolicy().getExpandMode() ==
                        AutoHidePolicy.ExpandMode.EXPAND_ON_ROLLOVER) {
                    expansionTimer.setDelay(
                            AutoHidePolicy.getPolicy().getRolloverTriggerDelay());
                }
            }

        }

        public void actionPerformed(ActionEvent e) {
            // timer event : there is a button to expand
            assert mouseEnteredButton != null;
            isUnderControl = true;
            select(mouseEnteredButton); // will collapse the previous button
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    expand();
                    isUnderControl = false;
                }
            });


        }
    }

    private class DragListener implements MouseListener, MouseMotionListener {
        /* inner class of expand panel*/
        int zone;

        DragListener(int zone) {
            this.zone = zone;
        }

        public void mouseDragged(MouseEvent e) {
            /* implement the drag effect on expand panel : a single border
             * can be dragged (the one not overlapping the borders of the panel
             */

            // new height/width (including borders)
            int newHeight, newWidth;

            // where are we on the desk ?
            Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                    e.getPoint(), desk);

            Insets insets = getInsets();

            Insets idesk = desk.getDockingPanelInsets();

            switch (zone) {
                case DockingConstants.INT_HIDE_TOP:
                    // drag from top to bottom : change the height of the panel
                    newHeight = p.y - idesk.top + bottomDragger.getHeight() / 2 +
                            insets.bottom + insets.top;
                    newHeight = Math.max(10, Math.min(newHeight, desk.getHeight())); // clip

                    setSize(getWidth(), newHeight);
                    invalidate();
                    validate();
                    repaint();
                    break;
                case DockingConstants.INT_HIDE_BOTTOM:
                    newHeight = desk.getHeight() - idesk.bottom - p.y + topDragger.getHeight() / 2 + insets.top + insets.bottom;
                    int maxHeight = desk.getHeight() -
                            topDragger.getHeight();
                    if (newHeight > maxHeight) {
                        newHeight = maxHeight;
                    } else if (newHeight < 10) {
                        newHeight = 10;
                    }
                    Rectangle bounds = getBounds();
                    bounds.y = desk.getHeight() - idesk.bottom - newHeight;
                    bounds.height = newHeight;
                    setBounds(bounds);
                    invalidate();
                    validate();
                    repaint();
                    break;
                case DockingConstants.INT_HIDE_LEFT:
                    newWidth = p.x + rightDragger.getWidth() / 2 - idesk.left +
                            insets.right + insets.left;
                    int maxWidth = desk.getWidth() - rightDragger.getWidth();
                    if (newWidth > maxWidth) {
                        newWidth = maxWidth;
                    } else if (newWidth < 10) {
                        newWidth = 10;
                    }
                    setSize(newWidth, getHeight());
                    invalidate();
                    validate();
                    repaint();
                    break;
                case DockingConstants.INT_HIDE_RIGHT:
                    newWidth = desk.getWidth() - idesk.right - p.x + leftDragger.getWidth() / 2 + insets.left + insets.right;
                    maxWidth = desk.getWidth() - leftDragger.getWidth();
                    if (newWidth > maxWidth) {
                        newWidth = maxWidth;
                    } else if (newWidth < 10) {
                        newWidth = 10;
                    }
                    bounds = getBounds();
                    bounds.x = desk.getWidth() - idesk.right - newWidth;
                    bounds.width = newWidth;
                    setBounds(bounds);
                    invalidate();
                    validate();
                    repaint();
                    break;
            }
            desk.repaint();
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            shouldCollapse = false; // begining or drag
        }

        public void mouseReleased(MouseEvent e) {
            shouldCollapse = true; // end of drag
        }
    }

    private class FocusCollapser implements PropertyChangeListener {
        // focusOwner
        public void propertyChange(PropertyChangeEvent e) {
            if (!controler.isUnderControl) {
                // this is not a focus lost due to the expand controler
                Component c = (Component) e.getNewValue();
                if (c != null && !AutoHideExpandPanel.this.isAncestorOf(c)) {

                    // avoid collapsing when focus is given to a component which isn't a dockable
                    if (DockingUtilities.findSingleDockableContainerAncestor(c) != null) {
                        collapse();
                    }
                }
            }
        }
    }
}

