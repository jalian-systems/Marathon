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

/** A DockingActionEvent involving a single dockable as source of the action.
 *
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1 
 */
public abstract class DockingActionDockableEvent extends DockingActionEvent {
  private Dockable dockable;
  
  public DockingActionDockableEvent(DockingDesktop desktop, Dockable dockable, 
      DockableState.Location initialLocation, DockableState.Location nextLocation, int actionType) {
    super(desktop, initialLocation, nextLocation, actionType);
    this.dockable = dockable;
  }
  
  public Dockable getDockable() {
    return dockable;
  }
  
  public void setDockable(Dockable dockable){
    this.dockable = dockable;
  }
  

  
}
