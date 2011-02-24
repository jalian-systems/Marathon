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
import com.vlsolutions.swing.docking.event.DockEvent;
import com.vlsolutions.swing.docking.event.DockingActionAddDockableEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** A specialized container used to nest dockables inside that sub-part of the
 * desktop.
 * <p>
 * This component is used by the API (as the associate component of a CompoundDockable) 
 * and shouldn't be used outside this context.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1
 * @see CompoundDockable
 */
public class CompoundDockingPanel extends JPanel implements DockDropReceiver {
  
  private CompoundDockable dockable;
  
  /** Constructs a new CompoundDockingPanel, for a given dockable */
  public CompoundDockingPanel(CompoundDockable dockable) {
    setLayout(new BorderLayout());
    this.dockable = dockable;
    
  }
  
  
  /** act the same as a splitContainer : allow drop on the borders*/
  public void processDockableDrag(DockDragEvent event) {
    scanContainer(event, false);
  }
  
  public void processDockableDrop(DockDropEvent event) {
    scanContainer(event, true);
  }
  
  private void acceptDrop(DockEvent event){
    ( (DockDropEvent) event).acceptDrop();
    
    Dockable d = event.getDragSource().getDockable();
    DockableContainer dockableContainer = DockableContainerFactory.getFactory().
        createDockableContainer(d, DockableContainerFactory.ParentType.PARENT_DESKTOP);
    dockableContainer.installDocking(event.getDesktop());
    add((JComponent)dockableContainer, BorderLayout.CENTER);
  }
  
  private void scanContainer(DockEvent event, boolean drop) {
    // reject operation if the source is an ancestor of this component
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
    
    Point p = event.getMouseEvent().getPoint();
    Rectangle compBounds = getBounds();
    Dockable dragged = event.getDragSource().getDockable();
    DockableState.Location initialLocation = dragged.getDockKey().getLocation();
    DockableState.Location nextLocation = dockable.getDockKey().getLocation();
    
    event.setDockingAction(new DockingActionAddDockableEvent(event.getDesktop(), 
        dragged, initialLocation, nextLocation, this));
    if (drop) {
      acceptDrop(event);
    } else {
      Rectangle2D r2d = new Rectangle2D.Float(0,
          0,
          compBounds.width,
          compBounds.height);
      ( (DockDragEvent) event).acceptDrag(r2d);
    }
  }

  /** Returns the compound dockable this container is for */
  public CompoundDockable getDockable() {
    return dockable;
  }
  
  
  
  
}
