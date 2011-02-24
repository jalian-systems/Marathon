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
import java.awt.Container;

/** A DockingActionEvent describing the first insertion of a dockable either inside a desktop
 * docking panel (main panel) or into a compoundDockable (nested) container.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1 
 */
public class DockingActionAddDockableEvent extends DockingActionDockableEvent {
  
  private Container parentContainer;
  
  /** Constructor for a DockingActionAddDockableEvent 
   * @param parentContainer the container (DockingPanel or CompoundDockingPanel) into which this
   *                        dockable is goint to be added. 
   */
  public DockingActionAddDockableEvent(DockingDesktop desktop, Dockable dockable, 
      DockableState.Location initialLocation, DockableState.Location nextLocation, Container parentContainer) {
    super(desktop, dockable, initialLocation, nextLocation, ACTION_ADD_DOCKABLE);
    this.parentContainer = parentContainer;
  }

  /** Returns the parent container into which this dockable will be added  */
  public Container getParentContainer() {
    return parentContainer;
  }

  public String toString(){
    return "DockingActionAddDockableEvent ";
  }

  
}
