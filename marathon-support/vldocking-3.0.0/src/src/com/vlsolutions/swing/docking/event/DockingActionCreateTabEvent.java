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


package com.vlsolutions.swing.docking.event;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;

/** A DockingActionEvent describing a tab insertion (or movement).
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1 
 */
public class DockingActionCreateTabEvent extends DockingActionDockableEvent {

  private Dockable base;

  private int order;
  
  public DockingActionCreateTabEvent(DockingDesktop desktop, Dockable dockable, 
      DockableState.Location initialLocation, DockableState.Location nextLocation, Dockable base, int order) {
    super(desktop, dockable, initialLocation, nextLocation, ACTION_CREATE_TAB);
    this.base = base;
    this.order = order;
  }

  /** Returns the dockable used as a reference to create a tab (may already belong to a tab)*/
  public Dockable getBase() {
    return base;
  }

  /** Returns the order of insertion in the tabbed container */
  public int getOrder() {
    return order;
  }
  
  public String toString(){
    return "DockingActionCreateTabEvent";
  }

  
}
