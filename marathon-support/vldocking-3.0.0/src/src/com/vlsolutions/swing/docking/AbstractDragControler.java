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

import com.vlsolutions.swing.docking.event.DockDragEvent;
import com.vlsolutions.swing.docking.event.DockDropEvent;
import com.vlsolutions.swing.docking.event.DockableStateChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockingActionDockableEvent;
import com.vlsolutions.swing.docking.event.DockingActionEvent;
import com.vlsolutions.swing.docking.event.DockingActionSimpleStateChangeEvent;
import com.vlsolutions.swing.docking.event.DockingActionSplitDockableContainerEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Abstract drag controler : provides common behaviour for LW and HW controlers
 * <p>(should only be used by VLDocking core framework and extensions)
 * 
 * @author Lilian Chamontin, VLSolutions
 * @since 3.0
 */
abstract class AbstractDragControler implements DragControler {
    /* This class is not public : it relies so heavily on vldocking internals that I'm reluctant
     * to give extenders an access to it.
     */
    protected Shape dropShape;

    protected boolean isFloatingShape = false; // true during floating Dnd

    protected boolean startDrag;

    protected Point dragPoint; // !=null means that drag is occuring

    protected Point startDragPoint; // for offset drawing

    protected boolean ignoreDrag; // flag to ignore a drag  operation

    protected DockableDragSource dockableDragSource;

    protected DockDropReceiver dropReceiver; // the component receiving the drop

    protected boolean paintFloatingDragShape = UIManager.getBoolean("FloatingContainer.paintDragShape");

    protected DockingDesktop desktop;

    protected DockingContext dockingContext;

    protected ShapePainterStrategy currentShapePainterStrategy;

    private HashMap <Window, ShapePainterStrategy> shapePainters = new HashMap();

    public AbstractDragControler(DockingDesktop desktop) {
        this.desktop = desktop;
        this.dockingContext = desktop.getContext();
    }

    /** process mouse clicks on DockableDragSource */
    public void mouseClicked(MouseEvent e) {
    }

    /** process mouse entered on DockableDragSource (nothing yet) */
    public void mouseEntered(MouseEvent e) {
    }

    /** process mouse exit on DockableDragSource (nothing yet) */
    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    /** process mouse pressed on DockableDragSource : prepares a drag operation */
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            startDrag = true;
            ignoreDrag = false;
        } else {
            if (dragPoint != null) { // we were dragging
                cancelDrag();
            } else {
                ignoreDrag = true; // ignore drag gesture with other than left button 2008/04/15
            }
        }
    }

    private boolean isDragAndDropEnabled() {
        return UIManager.getBoolean("DragControler.isDragAndDropEnabled");
    }

    public Dockable getDockable() {
        if (dockableDragSource != null) {
            return dockableDragSource.getDockable();
        } else {
            return null;
        }
    }

    public DockDropReceiver getDropReceiver() {
        return dropReceiver;
    }

    public Shape getDropShape() {
        return dropShape;
    }

    public boolean isFloatingShape() {
        return isFloatingShape;
    }

    protected abstract ShapePainterStrategy createShapePainterStrategy(Window w);

    /** searches the best shapePainter */
    protected ShapePainterStrategy getShapePainter(Component comp, DockableDragSource source) {
        if (comp == null) {
            comp = desktop;
        }
        Window w = SwingUtilities.getWindowAncestor(comp);
        ShapePainterStrategy newStrategy = shapePainters.get(w);
        if (newStrategy == null) {
            newStrategy = createShapePainterStrategy(w);
            shapePainters.put(w, newStrategy);
            if (currentShapePainterStrategy != null) {
                currentShapePainterStrategy.endDrag();
            }
            currentShapePainterStrategy = newStrategy;
            newStrategy.startDrag(source);
            return newStrategy;
        } else if (newStrategy == currentShapePainterStrategy) {
            return newStrategy;
        } else { // newstrategy not null and != currentStategy
            currentShapePainterStrategy.endDrag();
            currentShapePainterStrategy = newStrategy;
            newStrategy.startDrag(source);
            return newStrategy;
        }
    }

    /** This method cancels the current drag gesture.
     *<p>
     * It can be used for example by a key listener reacting to the escape key
     *
     *@since 2.0.1
     */
    /**
     * This method cancels the current drag gesture.
     * <p>
     * It can be used for example by a key listener reacting to the escape key
     *
     * @since 2.0.1
     */
    public void cancelDrag() {
        this.ignoreDrag = true;
        this.dropShape = null;
        this.dockableDragSource = null;
        this.startDrag = true;
        this.dragPoint = null;
        if (currentShapePainterStrategy != null) {
            currentShapePainterStrategy.endDrag();
        }
        clearStrategies();
        desktop.repaint();
    }

    /**
     * clears the shape painters cache to avoid keeping references of old windows
     */
    private void clearStrategies() {
        Window w = SwingUtilities.getWindowAncestor(desktop);
        Window[] owned = w.getOwnedWindows();
        for (int i = 0; i < owned.length; i++) {
            shapePainters.remove(w);
        }
    }

    /**
     * process mouse released on DockableDragSource
     */
    public void mouseReleased(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            if (dragPoint != null) {
                notifyDragSourceDragComplete(false);
                cancelDrag();
            }
        } else {
            // left button
            // are we above an insertion point ?
            if (ignoreDrag) {
                return;
            }
            try {
                boolean dropped = false;
                if (dragPoint != null && dockableDragSource != null) {
                    dropped = processMouseReleased(e);
                }
                notifyDragSourceDragComplete(dropped);
            } finally {
                // clean up state variables
                notifyDragSourceDragComplete(false);
                cancelDrag();
            }
        }
    }

    private void notifyDragSourceDragComplete(boolean isDropped) {
        if (dockableDragSource != null) {
            dockableDragSource.endDragComponent(isDropped);
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (!isDragAndDropEnabled()) {
            // disable dnd right now
            ignoreDrag = true;
        }
        if (ignoreDrag) {
            return;
        } else if (startDrag) {
            startDrag = false;
            processDragGestureStart(e);
        } else {
            processMouseDragged(e);
        }
    }

    /** invoked only when a drag gesture has beed done
     * @return  true if the dockable has been dropped.
     */
    private boolean processMouseReleased(MouseEvent e) {
        // 2007/01/08 updated to return a boolean

        //Component underMouse = findComponentAt(desktop, dragPoint.x, dragPoint.y);
        DockableDragSource dragSource = ((DockableDragSource) e.getSource());
        this.dragPoint = SwingUtilities.convertPoint(
                (Component) e.getSource(), e.getPoint(), desktop);

        UnderMouseInfo umInfo = findComponentUnderMouse(e);
        Component underMouse = umInfo.underMouse;

        Component underMouseNullDesktop = null;
        DockingDesktop targetDesktop = umInfo.desktop; //findNearestNestedDesktop(underMouse);//umInfo.desktop
        if (targetDesktop == null) {
            underMouseNullDesktop = findDropReceiverOutsideDesktop(underMouseNullDesktop, e);
        }


        ShapePainterStrategy shapePainterStrategy = getShapePainter(underMouse, dragSource);

        int dx = dragPoint.x - startDragPoint.x;
        int dy = dragPoint.y - startDragPoint.y;
        if (Math.abs(dx) < 20 && Math.abs(dy) < 10) {
            // deny drag gesture when too near from start point 2005/11/15
            return false;
        }

        if (targetDesktop == null) {
            // deny drag gesture when desktops aren't compatible
            if (underMouseNullDesktop != null) { // we've found a dock drop receiver
                // we send a really simple event
                DockDropEvent event = new DockDropEvent(desktop, dragSource, e);
                ((DockDropReceiver) underMouseNullDesktop).processDockableDrop(event);
            // this event will be ignored
            }
            return false;
        } else if (targetDesktop.getContext() != desktop.getContext()) { //2006/09/11
            // contexts not compatible, stop here
            return false;
        }

        // 2005/11/08 support for full height/width docking

        DockingPanel dp = targetDesktop.getDockingPanel();

        Insets i = targetDesktop.getDockingPanelInsets();
        Rectangle deskBounds = new Rectangle(i.left, i.top,
                targetDesktop.getWidth() - i.left - i.right, targetDesktop.getHeight() - i.top - i.bottom);
        Rectangle innerBounds = new Rectangle(deskBounds);
        innerBounds.x += 5;
        innerBounds.y += 5;
        innerBounds.width -= 10;
        innerBounds.height -= 10;

        if (deskBounds.contains(dragPoint) && !innerBounds.contains(dragPoint)) {
            // at less than 5 pixels from a order, promote DockingPanel as the receiver
            underMouse = dp;
        }


        // move up hierarchy till we find a drop receiver
        while (underMouse != null && underMouse != targetDesktop &&
                !(underMouse instanceof DockDropReceiver)) {
            underMouse = underMouse.getParent();
        }

        umInfo.underMouse = underMouse; // resync

        if (underMouse == null) {
            // || isAncestorOf(dockableDragSource.getDockableContainer(), underMouse)){ 2007/01/08 moved to implementations
            // it's not possible to drop a parent on one of its children
            if (underMouse instanceof DockDropReceiver) {
                // but we still have to use the reference on drop receiver to disaplay a floating drag shape
                this.dropReceiver = (DockDropReceiver) underMouse;
            }
            DropProcess dropProcess = new DropProcess(e, dockableDragSource, umInfo);
            if (dropProcess.canDockableBeDetached() && dropProcess.checkDockableWillBeDetached()) {
                Point location = new Point(e.getPoint());
                SwingUtilities.convertPointToScreen(location, e.getComponent());
                dropProcess.setFloating(location);
                return true;
            } else {
                // refused (vetoed)
                return false;
            }
        } else if (underMouse instanceof DockDropReceiver && e.isControlDown()) { //2005/11/08 HOT SWAP FUNCTION
            processHotSwap(underMouse, e.getComponent(), null, true);
            return true;
        } else if (underMouse instanceof DockDropReceiver) {
//      MouseEvent convertMouse;
            DropProcess process = new DropProcess(e, dockableDragSource, umInfo);
            DockDragEvent event = process.findAcceptableEvent(e);


            if (event.isDragAccepted() && process.isDockingActionAccepted()) {// triggers vetoable listeners
                return process.dropIfPossible();
            } else if (process.canDockableBeDetached() // Not accepted on the desktop //2005/10/07
                    && process.checkDockableWillBeDetached()) {
                Point location = new Point(e.getPoint());
                SwingUtilities.convertPointToScreen(location, e.getComponent());
                process.setFloating(location);
                return true;
            } else {
                // vetoed, nothing can be be done
                return false;
            }
        } else { /*if (underMouse == null){ */ // not under a droppable zone
            DropProcess process = new DropProcess(e, dockableDragSource, umInfo);
            if (process.canDockableBeDetached() && process.checkDockableWillBeDetached()) {
                Point location = new Point(e.getPoint());
                SwingUtilities.convertPointToScreen(location, e.getComponent());
                process.setFloating(location);
                return true;
            } else {
                return false;
            }
        }

    }

    /** Allow hot swappping of two top level dockable containers (during drag) */
    protected void processHotSwap(Component underMouse, Component dragged,
            ShapePainterStrategy shapePainterStrategy, boolean drop) { //2005/11/08
    /* This whole method should be reworked to allow hooking a DockingActionEvent subclass
         * to process hotswapping.
         * This also means we have to get rid of these "Component" and rely on DockableContainers
         * instead (which will allow us to properly track dockable state changes)
         */


        // ---------------------
        // shortcut : if the underMouse component doesn't belong to a DockingPanel, we
        // just cancel the hot swap operation
        // explanation : hotswap between DOCKED and FLOATING whould otherwise mean
        // swapping dockable states, and this will not be allowed until a further release
        Component topLevel = underMouse;
        while (topLevel != null && !(topLevel instanceof DockingPanel)) {
            topLevel = topLevel.getParent();
        }
        if (topLevel == null) {
            // doesn't belong to a docking panel == not DOCKED
            if (!drop) {
                shapePainterStrategy.showStopDragCursor();
                setDropShape(null, shapePainterStrategy);
            }
            return;
        }
        // ---------------------

        // to implement the "hot swap" we need to find a top level dockable container
        // (whose parent should be SplitContainer).

        while (underMouse != null && !(underMouse.getParent() instanceof SplitContainer)) {
            underMouse = underMouse.getParent();
        }
        if (underMouse != null) {
            Component splitChild = dragged;
            while (splitChild != null && !(splitChild.getParent() instanceof SplitContainer)) {
                splitChild = splitChild.getParent();
            }
            if (splitChild != null && splitChild != underMouse && underMouse instanceof DockDropReceiver) {// this one should always be true (although as it depends on implementation details, I prefer to keep it safe)
                // ok we've found a suitable swap pattern
                if (drop) {
                    DockingUtilities.updateResizeWeights(desktop.getDockingPanel());
                    DockingUtilities.swapComponents(splitChild, underMouse);
                    DockingUtilities.updateResizeWeights(desktop.getDockingPanel());
                } else {
                    shapePainterStrategy.showSwapDragCursor();
                    Rectangle bounds = underMouse.getBounds();
                    Rectangle2D shape = new Rectangle2D.Float(0, 0, bounds.width, bounds.height);
                    this.dropReceiver = (DockDropReceiver) underMouse;
                    setDropShape(shape, shapePainterStrategy);
                }
            } else {
                // couldn't find a suitable component
                if (!drop) {
                    shapePainterStrategy.showStopDragCursor();
                    setDropShape(null, shapePainterStrategy);
                }
            }
        } else {
            // couldn't find a suitable component
            if (!drop) {
                shapePainterStrategy.showStopDragCursor();
                setDropShape(null, shapePainterStrategy);
            }

        }
    }

    private void processDragGestureStart(MouseEvent e) {
        DockableDragSource dragSource = ((DockableDragSource) e.getSource());

        this.dragPoint = SwingUtilities.convertPoint(
                (Component) e.getSource(), e.getPoint(), desktop);
        this.dropShape = null;
        this.dockableDragSource = null;

        this.startDragPoint = e.getPoint();

        if (dragSource.startDragComponent(startDragPoint)) {
            this.dockableDragSource = dragSource;
            getShapePainter(e.getComponent(), dragSource).startDrag(dragSource);
            this.startDragPoint = new Point(dragPoint); // same coordinate system for future use
            processMouseDragged(e); // forward the event to the actual processing
        } else { // drag rejected
            this.ignoreDrag = true;
        }

    }

    /** Returns information about the component right under the mouse (including other owned windows)*/
    protected UnderMouseInfo findComponentUnderMouse(MouseEvent e) {

        // are we above an insertion point ?
        Point p = new Point(e.getPoint());
        SwingUtilities.convertPointToScreen(p, e.getComponent());

        UnderMouseInfo umInfo = new UnderMouseInfo();
        //Component underMouse = null;
        // iterate through the owned windows (might be floatables)
        Window w = SwingUtilities.getWindowAncestor(desktop);

        Window[] children = w.getOwnedWindows();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof FloatingDockableContainer && children[i].isVisible()) {
                Window wChild = (Window) children[i]; // assumed by the FloatingDockableContainer interface
                Rectangle bounds = wChild.getBounds();
                if (bounds.contains(p)) {
                    // an owned window is on top of the desktop, at current mouse position
                    // we have to try and find a dockable into this window
                    Point p2 = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), wChild);
                    Container contentPane = (wChild instanceof JDialog)
                            ? ((JDialog) wChild).getContentPane() : ((JFrame) wChild).getContentPane();
                    umInfo.underMouse = findComponentAt(contentPane, p2.x, p2.y); // bypass the glasspane
                    umInfo.desktop = desktop;
                }
            }
        }


        if (umInfo.underMouse == null && desktop.getContext().getDesktopList().size() > 1) {
            // now look for other desktops sharing the same context
            // and select the top most window at current mouse location

            DockingContext ctx = desktop.getContext();
            ArrayList desktops = ctx.getDesktopList();

            // create a list of unique windows
            ArrayList windows = new ArrayList();
            for (int i = 0; i < desktops.size(); i++) {
                DockingDesktop desk = (DockingDesktop) desktops.get(i);
                Window deskWin = SwingUtilities.getWindowAncestor(desk);
                if (deskWin != null && !windows.contains(deskWin)) {
                    // intersection with mouse ?
                    if (deskWin.getBounds().contains(p)) {
                        windows.add(deskWin);
                    }
                }
            }
            // now we have an unordered list of windows all intersecting our point : find which one
            // is above
            Window topWindow = null;
            Iterator it = ctx.getOwnedWindowActionOrder().iterator();
            while (it.hasNext()) {
                Window win = (Window) it.next();
                if (windows.contains(win)) {
                    topWindow = win;
                    break;
                }
            }

            if (topWindow != null) {
                Point p2 = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), topWindow);
                Container contentPane = (topWindow instanceof JDialog)
                        ? ((JDialog) topWindow).getContentPane() : ((JFrame) topWindow).getContentPane();
                umInfo.underMouse = findComponentAt(contentPane, p2.x, p2.y); // bypass the glasspane
                // now find the desktop associated to the component (there might be more than 1 desktop / window)
                // we have to walk up the hierarchy
                Component c = umInfo.underMouse;
                while (c != null) {
                    if (c instanceof DockingDesktop) {
                        umInfo.desktop = (DockingDesktop) c;
                        break;
                    } else {
                        c = c.getParent();
                    }
                }
            }

        // @todo : also check for floating windows belonging to these other desktops
        }

        if (umInfo.underMouse == null) {
            umInfo.underMouse = findComponentAt(desktop, dragPoint.x, dragPoint.y);
            umInfo.desktop = desktop;
        }
        return umInfo;
    }

    /** find the top most component of this container */
    protected abstract Component findComponentAt(Container container, int x, int y);

    /** searches for a dockDropReceiver when mouse is outside a docking desktop */
    protected Component findDropReceiverOutsideDesktop(Component underMouse, MouseEvent e) {
        // there's a side case we have to manage : when a DockDropReceiver has been
        // installed *outside* a desktop (this is desired when you want to
        // listen to drag and drop events (e.g. for a workspace switcher)
        // note : the event will be sent to the DockDropReceiver, but just for Drag
        // (ignored as a drop)
        // go up the hierarchy until we find a component that can receive a drop
        // currently this works only for the desktop's own window
        Container base = null;
        Window w = SwingUtilities.getWindowAncestor(desktop);
        if (w instanceof JFrame) {
            base = ((JFrame) w).getContentPane();
        } else if (w instanceof JDialog) {
            base = ((JDialog) w).getContentPane();
        }
        if (base != null) {
            Point pw = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), base);
            underMouse = findComponentAt(base, pw.x, pw.y);
            while (underMouse != null &&
                    !(underMouse instanceof DockDropReceiver)) {
                underMouse = underMouse.getParent();
            }
        }
        return underMouse;
    }

    protected void processMouseDragged(MouseEvent e) {
        Component dragged = (Component) e.getSource();

        DockableDragSource dragSource = ((DockableDragSource) e.getSource());

        this.dragPoint = SwingUtilities.convertPoint(
                (Component) e.getSource(), e.getPoint(), desktop);

        // continue drag
        UnderMouseInfo umInfo = findComponentUnderMouse(e);

        Component underMouse = umInfo.underMouse;
        Component underMouseNullDesktop = null;
        DockingDesktop targetDesktop = umInfo.desktop;//findNearestNestedDesktop(umInfo.underMouse);
        if (targetDesktop == null) {
            underMouseNullDesktop = findDropReceiverOutsideDesktop(underMouseNullDesktop, e);
        }

        ShapePainterStrategy shapePainterStrategy = getShapePainter(underMouse, dragSource);

        if (dragPoint == null || startDragPoint == null) {
            // 2006/03/16
            // safety net again : a bug has been submitted reagrding a NPE and I can't reproduce it
            ignoreDrag = true;
            return;
        }

        if (targetDesktop == null) {   //2006/09/11
            // deny drag gesture when desktops aren't compatible
            if (underMouseNullDesktop != null) { // we've found a dock drop receiver
                // we send a really simple event
                DockDragEvent event = new DockDragEvent(desktop, dragSource, e);
                ((DockDropReceiver) underMouseNullDesktop).processDockableDrag(event);
            }
            // finished
            shapePainterStrategy.showStopDragCursor();
            setDropShape(null, shapePainterStrategy);
            return;
        } else if (targetDesktop.getContext() != desktop.getContext()) {   //2006/09/11
            // deny drag gesture when desktops aren't compatible
            shapePainterStrategy.showStopDragCursor();
            setDropShape(null, shapePainterStrategy);
            return;
        }



        int dx = dragPoint.x - startDragPoint.x;
        int dy = dragPoint.y - startDragPoint.y;
        if (Math.abs(dx) < 20 && Math.abs(dy) < 10) {
            // deny drag gesture when too near from start point 2005/11/15
            shapePainterStrategy.showStopDragCursor();
            setDropShape(null, shapePainterStrategy);
            return;
        }


        // 2005/11/08 support for full height/width docking

        DockingPanel dp = targetDesktop.getDockingPanel();

        Insets i = targetDesktop.getDockingPanelInsets();
        Rectangle deskBounds = new Rectangle(i.left, i.top,
                targetDesktop.getWidth() - i.left - i.right, targetDesktop.getHeight() - i.top - i.bottom);
        Rectangle innerBounds = new Rectangle(deskBounds);
        innerBounds.x += 5;
        innerBounds.y += 5;
        innerBounds.width -= 10;
        innerBounds.height -= 10;

        if (deskBounds.contains(dragPoint) && !innerBounds.contains(dragPoint) && underMouse != null && targetDesktop.isAncestorOf(underMouse)) {
            // at less than 5 pixels from a border, promote DockingPanel as the receiver
            underMouse = dp;
        }

        // go up the hierarchy until we find a component that can receive a drop
        while (underMouse != null && underMouse != targetDesktop &&
                !(underMouse instanceof DockDropReceiver)) {
            underMouse = underMouse.getParent();
        }

        umInfo.underMouse = underMouse; // resync


        if (underMouse == null) {// || isAncestorOf(dockableDragSource.getDockableContainer(), underMouse)){
            // it's not possible to drop a parent on one of its children
            // so we're goind to try and detach it
            if (underMouse instanceof DockDropReceiver) {
                // but we still have to use the reference on drop receiver to display a floating drag shape
                this.dropReceiver = (DockDropReceiver) underMouse;
            }
            DragProcess process = new DragProcess(dockableDragSource, umInfo);
            if (process.canDockableBeDetached() && process.checkAndDetachDockable(shapePainterStrategy)) {
                // this method manages the detachement
            } else {
                // refused (vetoed)
                shapePainterStrategy.showStopDragCursor();
                setDropShape(null, shapePainterStrategy);
            }
        } else if (underMouse instanceof DockDropReceiver && e.isControlDown()) { //2005/11/08 HOT SWAP FUNCTION
            processHotSwap(underMouse, dragged, shapePainterStrategy, false);
        } else if (underMouse instanceof DockDropReceiver) {
            // loop if it returns null
            DragProcess process = new DragProcess(dockableDragSource, umInfo);
            DockDragEvent event = process.findAcceptableEvent(e);
            // will it cause a state change ?
            // a state change occur when switching between CLOSED, HIDDEN, and DOCKED
            // in this context, the drag event can only be generated by a hidden dockable
            // (if there is a way to drag its button) or an already docked dockable (no state change)

            if (event.isDragAccepted() && process.isDockingActionAccepted()) {
                shapePainterStrategy.showDragCursor();
                setDropShape(event.getDropShape(), shapePainterStrategy);
            } else if (process.canDockableBeDetached() && process.checkAndDetachDockable(shapePainterStrategy)) {
                // detach done by the "if"
            } else {
                event.rejectDrag(); // vetoed by listeners
                shapePainterStrategy.showStopDragCursor();
                setDropShape(null, shapePainterStrategy);
            }
        } else {// not above a drop receiver, might be detachable
            DragProcess process = new DragProcess(dockableDragSource, umInfo);

            if (process.canDockableBeDetached() && process.checkAndDetachDockable(shapePainterStrategy)) {
                // this method manages the detachement
            } else {
                // refused (vetoed)
                shapePainterStrategy.showStopDragCursor();
                setDropShape(null, shapePainterStrategy);
            }
        }
    }

    protected void setDropShape(Shape shape, ShapePainterStrategy shapePainterStrategy) {
        setDropShape(shape, shapePainterStrategy, false);
    }

    protected void setDropShape(Shape shape, ShapePainterStrategy shapePainterStrategy, boolean floating) {
        if (dropShape == null) {
            if (shape != null) {
                this.dropShape = shape;
                this.isFloatingShape = floating;
                shapePainterStrategy.repaint();
            }
        } else {
            if (shape != null) {
                if (!dropShape.equals(shape)) {
                    this.dropShape = shape;
                    this.isFloatingShape = floating;
                    shapePainterStrategy.repaint();
                }
            } else {
                this.dropShape = shape;
                this.isFloatingShape = floating;
                shapePainterStrategy.repaint();
            }
        }
    }

    /** A component that encapsulates the drag process (manages vetoable events, floatability..) */
    class DragProcess {
        DockableDragSource source;

        UnderMouseInfo umInfo;

        boolean stateChange = false;

        DockableState.Location futureLocation;

        DockingActionEvent dockingActionEvent;

        DragProcess(DockableDragSource source, UnderMouseInfo umInfo) {
            this.source = source;
            this.umInfo = umInfo;
            this.futureLocation = DockingUtilities.getDockableLocationFromHierarchy(umInfo.underMouse);
        }

        /** search until a drag event is accepted or rejected (not delegated) */
        public DockDragEvent findAcceptableEvent(MouseEvent e) {
            DockDragEvent event;
            boolean loop = false;
            Component underMouse = umInfo.underMouse;
            do {
                dropReceiver = (DockDropReceiver) underMouse;
                MouseEvent convertMouse = SwingUtilities.convertMouseEvent(
                        (Component) e.getSource(),
                        e, underMouse);
                DockableDragSource dragSource = ((DockableDragSource) e.getSource());

                event = new DockDragEvent(umInfo.desktop, dragSource, convertMouse);
                dropReceiver.processDockableDrag(event);
                if (event.isDragAccepted()) {
                } else if (event.isDragDelegated()) {
                    // find another dropper
                    underMouse = underMouse.getParent();
                    while (underMouse != null && underMouse != umInfo.desktop &&
                            !(underMouse instanceof DockDropReceiver)) {
                        underMouse = underMouse.getParent();
                    }
                }

                if (event.isDragAccepted()) {
                    loop = false;
                } else if (event.isDragDelegated() &&
                        underMouse instanceof DockDropReceiver) {
                    loop = true;
                } else {
                    loop = false;
                }
            } while (loop);
            if (event != null) {
                this.dockingActionEvent = event.getDockingAction();
                umInfo.underMouse = underMouse; // 2007/01/06
            }
            return event;
        }

        /** verifies if the dockable(s) movement will not be vetoed by listeners
         * */
        public boolean isDockingActionAccepted() {
            if (source.getDockableContainer() instanceof TabbedDockableContainer) {
                // here we're dragging a whole tabbed pane
                TabbedDockableContainer tdc = (TabbedDockableContainer) source.getDockableContainer();
                for (int i = 0; i < tdc.getTabCount(); i++) {
                    Dockable d = tdc.getDockableAt(i);
                    if (!isSingleDockingActionAccepted(d)) {
                        return false;
                    }
                }
                return true;
            } else {
                return isSingleDockingActionAccepted(source.getDockable());
            }
        }

        /** internal method for a single dockable  */
        private boolean isSingleDockingActionAccepted(Dockable dockable) {
            DockableState currentState = umInfo.desktop.getDockableState(dockable);


            // will it cause a state change ?
            // a state change occur when switching between CLOSED, HIDDEN, and DOCKED
            // in this context, the drag event can only be generated by a hidden dockable
            // (if there is a way to drag its button) or an already docked dockable (no state change)
            if (currentState.getLocation() != futureLocation) { // state cannot be null
                this.stateChange = true;
                DockableState newState = new DockableState(umInfo.desktop, dockable, futureLocation);
                DockableStateWillChangeEvent dscwEvent =
                        new DockableStateWillChangeEvent(currentState, newState);
                if (!desktop.getContext().fireDockableStateWillChange(dscwEvent)) {
                    return false;
                }
            }

            if (dockingActionEvent instanceof DockingActionDockableEvent) {
                DockingActionDockableEvent dde = (DockingActionDockableEvent) dockingActionEvent;
                // also check for DockingActionEvents
                if (dde.getDockable() == dockable) {
                    return desktop.getContext().fireAcceptDockingAction(dockingActionEvent);
                } else {
                    // multiple dockable moved at the same time (tabs) : we create copies
                    // of the original DockingActionEvent
                    // @todo : see if we couldn't create a new type of action instead
                    DockingActionDockableEvent dae = (DockingActionDockableEvent) dde.clone();
                    dae.setDockable(dockable);
                    return desktop.getContext().fireAcceptDockingAction(dae);
                }
            } else if (dockingActionEvent instanceof DockingActionSplitDockableContainerEvent) {
                // we're dragging a full tab dockable around : accept it always @todo check this
                return true;
            } else {
                throw new RuntimeException("unmanaged docking action " + dockingActionEvent);
            }

        }

        /** check if a dockable can be detached from the desktop */
        public boolean canDockableBeDetached() {
            if (source.getDockableContainer() instanceof TabbedDockableContainer) {
                // here we're dragging a whole tabbed pane
                TabbedDockableContainer tdc = (TabbedDockableContainer) source.getDockableContainer();
                for (int i = 0; i < tdc.getTabCount(); i++) {
                    Dockable d = tdc.getDockableAt(i);
                    if (!canSingleDockableBeDetached(d)) {
                        return false;
                    }
                }
                // last check : the tab container must not be itself already detached
                Dockable any = tdc.getDockableAt(0);
                if (any.getDockKey().getLocation() == DockableState.Location.FLOATING) {
                    //already detached, refuse another detaching
                    return false;
                } else {
                    return true;
                }
            } else {
                return canSingleDockableBeDetached(source.getDockable());
            }
        }

        private boolean canSingleDockableBeDetached(Dockable dockable) {
            /* must not be already detached + floatingenabled + not maximized */
            DockKey key = dockable.getDockKey();
            if (key.getLocation() == DockableState.Location.FLOATING) {
                if (DockingUtilities.findTabbedDockableContainer(dockable) != null) {
                    return true;
                } else {
                    return false;// already detached and single
                }
            }
            if (key.isFloatEnabled()) {
                if (key.getLocation() != DockableState.Location.MAXIMIZED) {
                    /* int dx = dragPoint.x - startDragPoint.x;
                    int dy = dragPoint.y - startDragPoint.y;
                    if (Math.abs(dx) < 20 && Math.abs(dy) < 10){
                    // deny detach when too near from start point 2005/11/01
                    return false;
                    } else {
                    return true;
                    }*/
                    return true; // the test above has been moved up to filter more drag events
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        /** trigers dockableStageWillChangeEvent to allow vetoing the floating */
        public boolean checkDockableWillBeDetached() {
            if (source.getDockableContainer() instanceof TabbedDockableContainer) {
                // here we're dragging a whole tabbed pane
                TabbedDockableContainer tdc = (TabbedDockableContainer) source.getDockableContainer();
                for (int i = 0; i < tdc.getTabCount(); i++) {
                    Dockable d = tdc.getDockableAt(i);
                    if (!checkSingleDockableWillBeDetached(d)) {
                        return false;
                    }
                }
                return true;
            } else {
                return checkSingleDockableWillBeDetached(source.getDockable());
            }
        }

        private boolean checkSingleDockableWillBeDetached(Dockable dockable) {
            DockableState newState = new DockableState(desktop, dockable, DockableState.Location.FLOATING);
            DockableState currentState = desktop.getDockableState(dockable);

            if (currentState == null) {
                return false; // safety check as is shouldn't be the case
            }

            DockableStateWillChangeEvent dscwEvent = new DockableStateWillChangeEvent(currentState, newState);
            //
            if (!desktop.getContext().fireDockableStateWillChange(dscwEvent)) {
                return false;
            } else {
                // also trigger DockingAction
                DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(desktop, dockable,
                        currentState.getLocation(), DockableState.Location.FLOATING);
                return desktop.getContext().fireAcceptDockingAction(dae);
            }
        }

        public boolean checkAndDetachDockable(ShapePainterStrategy shapePainterStrategy) {
            if (checkDockableWillBeDetached()) {
                if (DockingPreferences.isLightWeightUsageEnabled() && paintFloatingDragShape) {
                    Point shapePoint = new Point(dragPoint);
                    SwingUtilities.convertPointToScreen(shapePoint, desktop);
                    if (dropReceiver != null) { // we are above a drop receiver and we can show something
                        SwingUtilities.convertPointFromScreen(shapePoint, (Component) dropReceiver);
                        Dimension dragSize = dockableDragSource.getDockableContainer().getSize();
                        setDropShape(new Rectangle2D.Float(shapePoint.x, shapePoint.y,
                                dragSize.width, dragSize.height), shapePainterStrategy, true);
                    } else {
                        setDropShape(null, shapePainterStrategy);
                    }
                } else {
                    setDropShape(null, shapePainterStrategy);
                }
                shapePainterStrategy.showFloatCursor();
                return true;
            } else {
                return false;
            }
        }
    }

    class DropProcess extends DragProcess {
        MouseEvent event;

        DropProcess(MouseEvent event, DockableDragSource source, UnderMouseInfo umInfo) {
            super(source, umInfo);
            this.event = event;
        }

        public boolean dropIfPossible() {

            MouseEvent convertMouse = SwingUtilities.convertMouseEvent(
                    (Component) event.getSource(), event, umInfo.underMouse);

            DockDropEvent dropEvent = new DockDropEvent(
                    umInfo.desktop,
                    source,
                    convertMouse);
            dropReceiver.processDockableDrop(dropEvent);
            Component underMouse = umInfo.underMouse;
            if (dropEvent.isDropAccepted()) {
                if (underMouse instanceof JComponent) {
                    ((JComponent) underMouse).revalidate();
                } else if (underMouse instanceof Component) {
                    underMouse.validate();
                    underMouse.repaint();
                }

                fireDockingActionEvent();
                return true;
            } else {
                return false;
            }
        }

        public void fireDockingActionEvent() {
            if (source.getDockableContainer() instanceof TabbedDockableContainer) {
                TabbedDockableContainer tdc = (TabbedDockableContainer) source.getDockableContainer();
                for (int i = 0; i < tdc.getTabCount(); i++) {
                    Dockable d = tdc.getDockableAt(i);
                    fireSingleDockingActionEvent(tdc.getDockableAt(i));
                }
            } else {
                fireSingleDockingActionEvent(source.getDockable());
            }
        }

        private void fireSingleDockingActionEvent(Dockable dockable) {
            if (stateChange) {
                DockableState currentState = desktop.getDockableState(dockable);
                dockable.getDockKey().setLocation(futureLocation);

                dockingContext.fireDockableStateChange(new DockableStateChangeEvent(currentState,
                        new DockableState(desktop, dockable, futureLocation)));
            }
            if (dockingActionEvent instanceof DockingActionDockableEvent) {
                DockingActionDockableEvent dde = (DockingActionDockableEvent) dockingActionEvent;
                if (dde.getDockable() == dockable) {
                    dockingContext.fireDockingActionPerformed(dockingActionEvent);
                } else {
                    DockingActionDockableEvent dae = (DockingActionDockableEvent) dde.clone();
                    dae.setDockable(dockable);
                    dockingContext.fireDockingActionPerformed(dae);
                }
            } else if (dockingActionEvent instanceof DockingActionSplitDockableContainerEvent) {
                // we're dropping a whole container (currently it is only possible with
                // a tabbeddockablecontainer
                // @todo : missing event to fire here
            } else {
                throw new RuntimeException("Unmanaged docking action");
            }
        }

        public void setFloating(Point location) {
            if (source.getDockableContainer() instanceof TabbedDockableContainer) {
                desktop.setFloating((TabbedDockableContainer) source.getDockableContainer(), location);
            } else {
                Dockable dockable = source.getDockable();
                desktop.setFloating(source.getDockable(), true, location);
            }
        }
    }

    class UnderMouseInfo {
        DockingDesktop desktop;

        Component underMouse;
    }
}
