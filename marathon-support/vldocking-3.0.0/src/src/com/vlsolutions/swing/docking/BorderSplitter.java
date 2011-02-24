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

import com.vlsolutions.swing.docking.event.DockDropEvent;
import com.vlsolutions.swing.docking.event.DockDragEvent;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import com.vlsolutions.swing.docking.event.*;

/** An utility class used to delegate the border drag/drop scanning for docking (
 * since the same code is used in many DockDropReceivers).
 * <p>
 * This class is only meant for API Extenders.
 * <p>
 * Taking a DockDragEvent or DockDropEvent, check if mouse is near of one border.
 * If so, accepts the drag or drop operation.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * @update 2005/10/21 Lilian Chamontin : updated the shape to show real size of drop (and not 20 pixeles width/height)
 * @update 2005/11/14 Lilian Chamontin : added support for drag and drop of a whole tabbed container
 * @update 2005/12/08 Lilian Chamontin : fixed a bug when dropping into itself
 */

public class BorderSplitter {
  Component delegator;
  
  public BorderSplitter(Component delegator) {
    this.delegator = delegator;
  }
  
  
  public void processDockableDrag(DockDragEvent e){
    scanDrop(e, false);
  }
  
  public void processDockableDrop(DockDropEvent e){
    scanDrop(e, true);
  }
  
  /** This method should be overriden if the delegator doesn't belong to the
   * docking containment hierarchy after drop acceptance.
   * <p> Here is a case :
   * <UL>
   *   <LI>if tabpane is a TabbedDockableContainer with 2 tabs,
   *   <LI>if drag starts on tabpane
   *   <LI>if drop occurs on a border of the same tabpane
   * </UL>
   *
   * <p>
   * Then, tabpane will be replaced by a simple DockableContainer on drop acceptance
   * (because only one tab left).
   * <p>
   * And DockingDesktop.splitContainer(Component base, Split position) cannot be
   * called with tabpane as "base" (instead, it should be called with the replacer of
   * tabpane, or use the other split method which takes a dockable as base.
   *
   *
   * */
  public void split(DockDropEvent event, Container draggedContainer, DockingConstants.Split position){
    /* standard splitting */
//    event.getDesktop().splitComponent(delegator, event.getDragSource().getDockable(),
//        position);
    event.getDesktop().splitComponent(delegator, draggedContainer, position); //2005/11/14
    
  }
  
  private void acceptSplit(DockDropEvent event, DockingConstants.Split position){
    Container container = event.getDragSource().getDockableContainer();
    Dockable dockable = event.getDragSource().getDockable();//2005/12/08
    TabbedDockableContainer tabAncestor = DockingUtilities.findTabbedDockableContainer(dockable);
    Dockable remainingDockable = null;
    if (tabAncestor != null && tabAncestor.getTabCount() == 2 && tabAncestor == delegator){//2007//11/15
      // drop onto itself : one of the two remanining dockable is dropped
      // find which dockable is dropped, and which remains
        remainingDockable = tabAncestor.getDockableAt(0); 
        if (remainingDockable == dockable){
          //oops !
          remainingDockable = tabAncestor.getDockableAt(1);
        }
    }
    ( (DockDropEvent) event).acceptDrop(); 
    if (container instanceof TabbedDockableContainer){
      split(event, container, position);
    } else {
      //if (tabAncestor != null && tabAncestor.getTabCount() == 1 && tabAncestor == delegator){//2007//11/15
      if (tabAncestor != null && tabAncestor.getTabCount() == 0 && tabAncestor == delegator){//2007//11/15
        // we have detected a dropping onto itself :
        // the drop gesture comes from the tabAncestor (as it's given by the drag source)
        // and translates into a border split
        // if only one tab remains, that means  the tabAncestor is no longer useful and
        // so we now need to remove that tab and replace it by a standard singledockable
        // which is done by the standard desktop "split" public method.
        //Dockable remainingDockable = tabAncestor.getDockableAt(0);
        event.getDesktop().split(
            remainingDockable, // the only remaining dockable
            dockable,
            position);
      } else {
        // standard dropping, just use the internal API to relayout the desktop.
        event.getDesktop().splitComponent(delegator, dockable, position);
      }
    }
  }
  
  /** Accept the drag gesture and setup the docking action event associated to it */
  private void acceptDragSplit(DockDragEvent event, DockingConstants.Split position, Shape shape){
    
    Container container = event.getDragSource().getDockableContainer();
    Dockable dockable = event.getDragSource().getDockable();//2005/12/08
    TabbedDockableContainer tabAncestor = DockingUtilities.findTabbedDockableContainer(dockable);
    DockableState.Location initialLocation = dockable.getDockKey().getLocation();
    DockableState.Location borderSpliterLocation = DockingUtilities.getDockableLocationFromHierarchy(delegator);
    
    if (container instanceof TabbedDockableContainer){
      event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
          dockable, initialLocation, borderSpliterLocation, container, position, 0.5f));
    } else {
      if (tabAncestor != null && tabAncestor.getTabCount() == 1 && tabAncestor == delegator){
        // we have detected a dropping onto itself :
        // the drop gesture comes from the tabAncestor (as it's given by the drag source)
        // and translates into a border split
        // if only one tab remains, that means  the tabAncestor is no longer useful and
        // so we now need to remove that tab and replace it by a standard singledockable
        // which is done by the standard desktop "split" public method.
        Dockable remainingDockable = tabAncestor.getDockableAt(0);
        event.setDockingAction(new DockingActionSplitDockableEvent(event.getDesktop(), 
          dockable, initialLocation, borderSpliterLocation, remainingDockable, position, 0.5f));
      } else {
        // standard dropping, just use the internal API to relayout the desktop.
        event.setDockingAction(new DockingActionSplitComponentEvent(event.getDesktop(), 
          dockable, initialLocation, borderSpliterLocation, delegator, position, 0.5f));
      }
    }
    event.acceptDrag(shape);
  }
  
  private void scanDrop(DockEvent event, boolean drop){
    Point p = event.getMouseEvent().getPoint();
    Dimension size = delegator.getSize();
    int distTop = p.y;
    int distLeft = p.x;
    int min = Math.min(distTop, distLeft);
    int distRight = size.width - p.x;
    min = Math.min(min, distRight);
    int distBottom = size.height - p.y;
    min = Math.min(min, distBottom);
    
    Dimension draggedSize = null;
    DockableDragSource source = event.getDragSource();
    
    // the drag size is the one of the parent dockable container
    //draggedSize = dragged.getComponent().getParent().getSize();
    draggedSize = source.getDockableContainer().getSize();
    
    int bestHeight = (int)Math.min(draggedSize.height , size.height * 0.5);
    int bestWidth = (int)Math.min(draggedSize.width , size.width * 0.5);
    
    if (min == distTop && min < delegator.getHeight()/4) {
      // dock on top
      if (drop) {
        acceptSplit((DockDropEvent) event, DockingConstants.SPLIT_TOP);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(0,
            0,
            size.width,
            bestHeight);
        acceptDragSplit((DockDragEvent) event, DockingConstants.SPLIT_TOP, r2d);
      }
    } else if (min == distLeft && min < delegator.getWidth()/4) {
      if (drop) {
        acceptSplit((DockDropEvent) event, DockingConstants.SPLIT_LEFT);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(0,
            0,
            bestWidth,
            size.height);
        acceptDragSplit((DockDragEvent) event, DockingConstants.SPLIT_LEFT, r2d);
      }
    } else if (min == distBottom && min < delegator.getHeight()/4) {
      if (drop) {
        acceptSplit((DockDropEvent) event, DockingConstants.SPLIT_BOTTOM);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(0,
            size.height - bestHeight, //20,
            size.width,
            bestHeight);
        acceptDragSplit((DockDragEvent) event, DockingConstants.SPLIT_BOTTOM, r2d);
      }
    } else if (min == distRight && min < delegator.getWidth()/4){ // right
      if (drop) {
        acceptSplit((DockDropEvent) event, DockingConstants.SPLIT_RIGHT);
      } else {
        Rectangle2D r2d = new Rectangle2D.Float(
            size.width - bestWidth,
            0,
            bestWidth, 
            size.height);
        acceptDragSplit((DockDragEvent) event, DockingConstants.SPLIT_RIGHT, r2d);
        
      }
    }
  }
  
  
}
