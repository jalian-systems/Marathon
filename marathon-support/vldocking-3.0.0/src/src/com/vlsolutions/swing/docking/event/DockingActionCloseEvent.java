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

/** A DockingActionEvent describing the closing of a dockable
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1 
 */
public class DockingActionCloseEvent extends DockingActionDockableEvent {
  
  public DockingActionCloseEvent(DockingDesktop desktop, Dockable dockable, DockableState.Location initialLocation) {
    super(desktop, dockable, initialLocation, DockableState.Location.CLOSED, ACTION_CLOSE);
  }
  
  public String toString(){
    return "DockingActionCloseEvent";
  }

}
