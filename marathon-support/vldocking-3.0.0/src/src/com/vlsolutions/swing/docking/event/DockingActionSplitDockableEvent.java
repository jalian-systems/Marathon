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
import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.DockingDesktop;

/** A DockingActionEvent describing a split action (from a base dockable).
 *
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1 
 */
public class DockingActionSplitDockableEvent extends DockingActionDockableEvent {
  private Dockable base;
  private float dividorLocation;
  
  private DockingConstants.Split splitPosition;
  
  /** Constructs a new event  */
  public DockingActionSplitDockableEvent(DockingDesktop desktop, Dockable dockable,
      DockableState.Location initialLocation, DockableState.Location nextLocation,
      Dockable base, DockingConstants.Split splitPosition,
      float dividorLocation) {
    super(desktop, dockable, initialLocation, nextLocation, ACTION_SPLIT_DOCKABLE);
    this.base = base;
    this.dividorLocation = dividorLocation;
    this.splitPosition = splitPosition;
  }
  
  /** Returns the dockable which will be used as a base for the splitting */
  public Dockable getBase() {
    return base;
  }
  
  public float getDividorLocation() {
    return dividorLocation;
  }
  
  public DockingConstants.Split getSplitPosition() {
    return splitPosition;
  }
  
  public String toString(){
    return "DockingActionSplitDockableEvent [base:" + base.getDockKey()
    + ", dockable:" + getDockable().getDockKey()+"]";
  }
  
}
