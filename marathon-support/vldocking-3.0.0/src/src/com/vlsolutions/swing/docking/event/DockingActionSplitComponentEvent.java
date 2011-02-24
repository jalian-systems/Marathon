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
import java.awt.Component;

/** A DockingActionEvent describing a split action (from a base component (splitcontainer, tab...)).
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1 
 */
public class DockingActionSplitComponentEvent extends DockingActionDockableEvent {
  private Component base;
  private float dividorLocation;
  private float parentDividorLocation;

  private DockingConstants.Split splitPosition;
  
  /** Constructs a new event */
  public DockingActionSplitComponentEvent(DockingDesktop desktop, Dockable dockable, 
      DockableState.Location initialLocation, DockableState.Location nextLocation,
      Component base, DockingConstants.Split splitPosition, float dividorLocation) {
    super(desktop, dockable, initialLocation, nextLocation, ACTION_SPLIT_COMPONENT);
    this.base = base;
    this.dividorLocation = dividorLocation;
    this.parentDividorLocation = -1;
    this.splitPosition = splitPosition;
  }

  /** Constructs a new event.
   *<p>
   * This version of the constructor also contains resizing information for the parent of 
   * thhe splitted component.
   */
  public DockingActionSplitComponentEvent(DockingDesktop desktop, Dockable dockable, 
      DockableState.Location initialLocation, DockableState.Location nextLocation, Component base, DockingConstants.Split splitPosition,
      float dividorLocation, float parentDividorLocation) {
    super(desktop, dockable, initialLocation, nextLocation, ACTION_SPLIT_COMPONENT);
    this.base = base;
    this.dividorLocation = dividorLocation;
    this.parentDividorLocation = parentDividorLocation;
    this.splitPosition = splitPosition;
  }

  /** Returns the dockable which will be used as a base for the splitting */
  public Component getBase() {
    return base;
  }

  public float getDividorLocation() {
    return dividorLocation;
  }

  /** returns a dividor location value for the parent split container, or -1 if not needed 
   *<p>
   * This value is used to express inserting a component with same orientation of the 
   * parent split container (like : transform A|B into [A|child]|B : we need to adjust 
   * A|child dividor, and also [] | B dividor).
   */
  public float getParentDividorLocation() {
    return parentDividorLocation;
  }

  public DockingConstants.Split getSplitPosition() {
    return splitPosition;
  }
  public String toString(){
    return "DockingActionSplitComponentEvent";
  }
}

  
