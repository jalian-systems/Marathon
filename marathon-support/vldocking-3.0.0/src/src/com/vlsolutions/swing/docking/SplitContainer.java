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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import com.vlsolutions.swing.docking.event.*;

/** A Specialized JSplitPane which accepts drag and drop of DockableContainer.
 *<p>
 * The split container should be used only by the DockingDesktop.
 *<p>
 * It is a SplitPane with bug workarounds and a custom UI (in fact : no UI painting at all,
 * by default).
 *
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class SplitContainer extends JSplitPane implements DockDropReceiver {
  
  // this is for debugging purpose and will be removed.
  private static Color [] colors = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
  Color.MAGENTA, Color.ORANGE, Color.PINK, Color.WHITE, Color.YELLOW};
  private static int colorindex = -1;
  
  private static final boolean DEBUG = false;
  private static final String uiClassID = "DockingSplitPaneUI";
  
  
  /** Constructs a vertically splitted SplitContainer */
  public SplitContainer() {
    this(VERTICAL_SPLIT);
  }
  
  /** Constructs a SplitContainer with the given JSplitPane orientation. */
  public SplitContainer(int orientation) {
    super(orientation);
    setBorder(null);
    if (DEBUG){
      Color c = colors[ (++colorindex % colors.length)];
      setBorder(BorderFactory.createLineBorder(c, 2));
    }    
  }
  
  
  /** Overriden for custom UI delegation */
  public String getUIClassID() {
    return uiClassID;
  }
  
  /** Returns a readable String representing this SplitContainer */
  public String toString() {
    if (getOrientation() == HORIZONTAL_SPLIT){
      return "SplitContainer[HORIZONTAL, " + getTopComponent() + " / " + getBottomComponent() +
          ']';
    } else {
      return "SplitContainer[VERTICAL, " + getTopComponent() + " / " + getBottomComponent() +
          ']';
    }
  }
  
  /** Returns a suitable name for when this container is the main child of a Tab (TabbedDockableContainer) */
  public String getTabName(){
    Component left = getLeftComponent();
    if (left instanceof SingleDockableContainer){
      return ((SingleDockableContainer)left).getDockable().getDockKey().getTabName();
    } else if (left instanceof SplitContainer){
      return ((SplitContainer)left).getTabName();
    } else {
      return null;
    }
  }
  
  
  /** {@inheritDoc} */
  public void processDockableDrag(DockDragEvent event) {
    scanContainer(event, false);
  }
  
  /** {@inheritDoc} */
  public void processDockableDrop(DockDropEvent event) {
    scanContainer(event, true);
  }
  
  private void scanContainer(DockEvent event, boolean drop) {
    // reject operation if the source is an ancestor of this view.
    if (event.getDragSource().getDockableContainer().isAncestorOf(this)){
      // this is possible for compound containers (as they contain sub-dockables)
      // in that case, you cannnot drop a compound into one of its children  // 2007/01/08
      if (drop){
        ((DockDropEvent) event).rejectDrop();
      } else {
        ((DockDragEvent) event).delegateDrag();
      }
      return;
    }
    
    
    /*   Dock is available :
     *    - on borders of the two components
     *    - on the center of the 2 components (meaning : tab'em)
     */
    Point p = event.getMouseEvent().getPoint();
    Rectangle compBounds = getLeftComponent().getBounds();
    if (compBounds.contains(p)){
      scanComponent(getLeftComponent(), event, drop, p, compBounds);
    }else {
      getRightComponent().getBounds(compBounds);
      if (compBounds.contains(p)){
        scanComponent(getRightComponent(), event, drop, p, compBounds);
      } else {
        // we are elsewhere : reject
        if (drop) {
          ( (DockDropEvent) event).rejectDrop();
        } else {
          ( (DockDragEvent) event).rejectDrag();
        }
      }
    }
    
  }
  
  // workaround bug #4276222
  private boolean isPainted;
  private boolean hasProportionalLocation;
  private double proportionalLocation;
  /** Overriden for a bug workaround*/
  public void setDividerLocation(double proportionalLocation){
    if (! isPainted){
      hasProportionalLocation = true;
      this.proportionalLocation = proportionalLocation;
    } else {
      if (proportionalLocation < 0 ){ // safety checks to avoid exceptions
        proportionalLocation = 0;
      } else if (proportionalLocation > 1){
        proportionalLocation = 1;
      } else {
        super.setDividerLocation(proportionalLocation);
      }
    }
  }
  
  public void setDividerLocation(int location) {
    super.setDividerLocation(location);
    if (!DockingPreferences.isLightWeightUsageEnabled()){
      // ugly repaint trick due to incorrect repaint on linux/mac systems
      final Window w = SwingUtilities.getWindowAncestor(this);
      if (w != null){
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            w.repaint();
          }
        });
      }
    }
  }
  
  
  
  /** overriden for a bug workaround reason */
  public void paint(Graphics g){
    if (! isPainted){
      isPainted = true;
      if (hasProportionalLocation){
        setDividerLocation(proportionalLocation);
        doLayout();
      }
    }
    super.paint(g);
  }
  
  /** Resest recursively the left and right components to their preferred size
   * if possible. (same contract as JSplitPane's).
   * <p>
   * If contained components are SplitContainers, their are also reset to their
   * preferredSize.
   *  */
  public void resetToPreferredSizes(){
    super.resetToPreferredSizes();
    if (getLeftComponent() instanceof SplitContainer){
      ((SplitContainer)getLeftComponent()).resetToPreferredSizes();
    }
    if (getRightComponent() instanceof SplitContainer){
      ((SplitContainer)getRightComponent()).resetToPreferredSizes();
    }
    
  }
  
  /** Replaces the child component by a sub-split pane.
   * @param child one or the two components of this split pane
   * @param newComp the new component to add
   * @param position where to put comp / child (i.e if SPLIT_TOP,
   * a vertical splitpane will be created and comp will be on top of it
   * (and child at bottom).
   */
  public void split(Component newComp, Component child, DockingConstants.Split position){
    
    SplitContainer split;
    if (position == DockingConstants.SPLIT_TOP
        || position == DockingConstants.SPLIT_BOTTOM){
      split = new SplitContainer(JSplitPane.VERTICAL_SPLIT);
    } else /*if (position == DockingConstants.SPLIT_LEFT
        || position == DockingConstants.SPLIT_RIGHT)*/ {
      split = new SplitContainer(JSplitPane.HORIZONTAL_SPLIT);
    }
    
    if (getLeftComponent() == child){
      remove(child);
      setLeftComponent(split);
      if (position == DockingConstants.SPLIT_TOP || position == DockingConstants.SPLIT_LEFT){
        split.setLeftComponent(newComp); // for splitpane, left == top
        split.setRightComponent(child);
      } else {
        split.setLeftComponent(child); // for splitpane, left == top
        split.setRightComponent(newComp);
      }
    } else {
      remove(child);
      setRightComponent(split);
      if (position == DockingConstants.SPLIT_TOP || position == DockingConstants.SPLIT_LEFT){
        split.setLeftComponent(newComp); // for splitpane, left == top
        split.setRightComponent(child);
      } else {
        split.setLeftComponent(child); // for splitpane, left == top
        split.setRightComponent(newComp);
      }
    }
  }
  
  private void scanComponent(Component comp, DockEvent event, boolean drop,
      Point p, Rectangle compBounds) {
    int distTop = p.y - compBounds.y;
    int distLeft = p.x - compBounds.x;
    int min = Math.min(distTop, distLeft);
    int distRight = compBounds.x + compBounds.width - p.x;
    int distBottom = compBounds.y + compBounds.height - p.y;
    int min2 = Math.min(distBottom, distRight);
    min = Math.min(min, min2);
    Dockable dockable = event.getDragSource().getDockable();
    DockableState.Location splitState = DockingUtilities.getDockableLocationFromHierarchy(this);
    DockableState.Location dockableState = dockable.getDockKey().getLocation();
    
    if (min == distTop) {
      // dock on top
      if (drop) {
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_TOP, 0.5f));
        
        ( (DockDropEvent) event).acceptDrop();
        
        event.getDesktop().splitComponent(comp, event.getDragSource().getDockable(),
            DockingConstants.SPLIT_TOP);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(compBounds.x,
            compBounds.y,
            compBounds.width,
            20);
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_TOP, 0.5f));
        
        ( (DockDragEvent) event).acceptDrag(r2d);
      }
    } else if (min == distLeft) {
      if (drop) {
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_LEFT, 0.5f));
        
        ( (DockDropEvent) event).acceptDrop();
        event.getDesktop().splitComponent(comp, event.getDragSource().getDockable(),
            DockingConstants.SPLIT_LEFT);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(compBounds.x,
            compBounds.y,
            20,
            compBounds.height);

        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_LEFT, 0.5f));

        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockable.getDockKey().getLocation(), splitState,
            comp, DockingConstants.SPLIT_TOP, 0.5f));
        
        ( (DockDragEvent) event).acceptDrag(r2d);
      }
    } else if (min == distBottom) {
      if (drop) {
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_BOTTOM, 0.5f));

        ( (DockDropEvent) event).acceptDrop();
        event.getDesktop().splitComponent(comp, event.getDragSource().getDockable(),
            DockingConstants.SPLIT_BOTTOM);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(compBounds.x,
            compBounds.y + compBounds.height - 20,
            compBounds.width,
            20);
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_BOTTOM, 0.5f));

        ( (DockDragEvent) event).acceptDrag(r2d);
      }
    } else { // right
      if (drop) {
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_RIGHT, 0.5f));

        ( (DockDropEvent) event).acceptDrop();
        event.getDesktop().splitComponent(comp, event.getDragSource().getDockable(),
            DockingConstants.SPLIT_RIGHT);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(
            compBounds.x + compBounds.width - 20,
            compBounds.y,
            20,
            compBounds.height);
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
            dockable, dockableState, splitState, comp, DockingConstants.SPLIT_RIGHT, 0.5f));

        ( (DockDragEvent) event).acceptDrag(r2d);
      }
    }
  }
  
}

/* Utility class, resizes a splitcontainer after its size is known (needs an invokelater after
 * having added it).
 * allows us to avoid the nasty resizing of a splitpane when a component
 * is added on the right/bottom (it will then take most of the split surface,
 * which is not good when we add a small dockable to the right of a big dockable
 */
class SplitResizer implements Runnable {
  SplitContainer split;
  
  int location;
  double doubleloc;
  boolean  isDouble;
  
  SplitResizer(SplitContainer split, int location) {
    this.split = split;
    this.location = location;
    isDouble = false;
  }
  
  SplitResizer(SplitContainer split, double location) {
    this.split = split;
    this.doubleloc = location;
    isDouble = true;
  }
  
  public void run() {
    // used as invokeLater, as the size of the splitpane is not known
    // when split is first inserted.
    if (isDouble){
      split.setDividerLocation(doubleloc);
    } else {
      
      // clamp resizing to the half width/height of the splitpane
      int maxWidth = split.getWidth() / 2;
      int maxHeight = split.getHeight() / 2;
      if (location < 0) { //meaning it's a resize of the opposite component
        if (split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
          location = Math.max(maxWidth, split.getWidth() + location);
        } else {
          location = Math.max(maxHeight, split.getHeight() + location);
        }
      } else {
        if (split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
          location = Math.min(maxWidth, location);
        } else {
          location = Math.min(maxHeight, location);
        }
      }
      
      split.setDividerLocation(location);
    }
  }
  
}
