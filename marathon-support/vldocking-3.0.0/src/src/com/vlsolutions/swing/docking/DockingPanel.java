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

import com.vlsolutions.swing.docking.event.DockingActionSplitComponentEvent;
import com.vlsolutions.swing.docking.event.DockingActionSplitDockableContainerEvent;
import java.awt.Shape;
import javax.swing.*;
import com.vlsolutions.swing.docking.event.DockDragEvent;
import com.vlsolutions.swing.docking.event.DockEvent;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import com.vlsolutions.swing.docking.event.DockDropEvent;
import java.awt.Point;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

/** This class is responsible for the containment of a Dockable component.
 * <p>
 * Users of the VLDocking Framework should not call this class which is
 * a utility component of DockingDesktop.
 *
 * <p>
 * Please rely on DockingDesktop capabilities.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * @update 2005/11/08 Lilian Chamontin : added support for global width/height drop
 */
public class DockingPanel extends JPanel implements DockDropReceiver{

    /**  @todo  see if we still need the DockDropReceiver */

    /** Javabeans constructor  */
    public DockingPanel(){
      super(new BorderLayout());
    }

    /** act the same as a splitContainer : allow drop on the borders*/
    public void processDockableDrag(DockDragEvent event) {
      scanContainer(event, false);
    }

    public void processDockableDrop(DockDropEvent event) {
      scanContainer(event, true);
    }

    private void acceptDrop(DockEvent event, DockingConstants.Split position){ 
      Container dragContainer = event.getDragSource().getDockableContainer();
      ( (DockDropEvent) event).acceptDrop();        
      if (dragContainer instanceof TabbedDockableContainer){
         event.getDesktop().splitComponent(this, dragContainer,  position);
      } else {
        event.getDesktop().splitComponent(this, event.getDragSource().getDockable(),  position);
      }
    }
    
    private void acceptDrag(DockEvent event, DockingConstants.Split position, Shape shape){ 
      Container dragContainer = event.getDragSource().getDockableContainer();
      Dockable dockable = event.getDragSource().getDockable();
      DockableState.Location initialState = dockable.getDockKey().getLocation();
      DockableState.Location nextState = DockableState.Location.DOCKED;
      
      if (dragContainer instanceof TabbedDockableContainer){
         event.setDockingAction(new DockingActionSplitDockableContainerEvent(
             event.getDesktop(), initialState, nextState, 
             this, dragContainer, position, 0.5f));
      } else {
         event.setDockingAction(new DockingActionSplitComponentEvent(
             event.getDesktop(), dockable, initialState, nextState, 
             this, position, 0.5f));
      }
      ( (DockDragEvent) event).acceptDrag(shape);        
    }

    private void scanContainer(DockEvent event, boolean drop) {
      Point p = event.getMouseEvent().getPoint();
      Rectangle compBounds = getBounds();
      int distTop = p.y;
      int distLeft = p.x;
      int min = Math.min(distTop, distLeft);
      int distRight = compBounds.width - p.x;
      int distBottom = compBounds.height - p.y;
      int min2 = Math.min(distBottom, distRight);
      min = Math.min(min, min2);

      Dimension size = getSize();
      Dockable dragged = event.getDragSource().getDockable();
      // the drag size is the one of the parent dockable container
      Dimension draggedSize = dragged.getComponent().getParent().getSize();
      int bestHeight = (int)Math.min(draggedSize.height , size.height * 0.5);
      int bestWidth = (int)Math.min(draggedSize.width , size.width * 0.5);
      
      
      if (min == distTop) {
        // dock on top
        if (drop) {
          acceptDrop(event, DockingConstants.SPLIT_TOP);
        } else {
          Rectangle2D r2d = new Rectangle2D.Float(0,
              0,
              compBounds.width,
              bestHeight);
          acceptDrag(event, DockingConstants.SPLIT_TOP, r2d);
        }
      } else if (min == distLeft) {
        if (drop) {
          acceptDrop(event, DockingConstants.SPLIT_LEFT);
        } else {
          Rectangle2D r2d = new Rectangle2D.Float(0,
              0,
              bestWidth,
              compBounds.height);
          acceptDrag(event, DockingConstants.SPLIT_LEFT, r2d);
        }
      } else if (min == distBottom) {
        if (drop) {
          acceptDrop(event, DockingConstants.SPLIT_BOTTOM);
        } else {
          Rectangle2D r2d = new Rectangle2D.Float(0,
              compBounds.height - bestHeight,
              compBounds.width,
              bestHeight);
          acceptDrag(event, DockingConstants.SPLIT_BOTTOM, r2d);
        }
        } else { // right
          if (drop) {
            acceptDrop(event, DockingConstants.SPLIT_RIGHT);
          } else {
            Rectangle2D r2d = new Rectangle2D.Float(
                compBounds.width - bestWidth,
                0,
                bestWidth,
                compBounds.height);
          acceptDrag(event, DockingConstants.SPLIT_RIGHT, r2d);
          }
        }
      }

    public void resetToPreferredSize() {
      Component mainComp = getComponent(0);
      if (mainComp instanceof SplitContainer){
        ((SplitContainer)mainComp).resetToPreferredSizes();
      }
    }
    
    public String toString(){
      return "DockingPanel["+hashCode()+"]"; // 2007/01/24
    }


  }
