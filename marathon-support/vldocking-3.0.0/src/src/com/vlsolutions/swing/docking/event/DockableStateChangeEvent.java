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

import com.vlsolutions.swing.docking.*;

/** An event triggered after a change of dockable state.
 *<P> Events are triggered when a component is :
 * <UL>
 * <LI> Docked (added to the desktop)
 * <LI> set in Auto-hide mode (collapsed as a border button)
 * <LI> closed (removed from the desktop)
 * <LI> maximized (the only one visible)
 * <LI> floating (detached from the desktop)
 * </UL>
 *
 * <P> It is also possible to track changes before they occurs with
 * the DockableStateWillChangeEvent, which is triggered before the change is
 * processed (this event is vetoable).
 *
 * @see DockableStateWillChangeEvent
 * @see DockableStateChangeListener
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class DockableStateChangeEvent {
  private DockableState previousState, newState;
  public DockableStateChangeEvent(DockableState previousState, DockableState newState) {
    this.previousState = previousState;
    this.newState = newState;
  }

  /** returns the previous state of the dockable, or null if the component
   * wasn't known to the docking desktop before. */
  public DockableState getPreviousState(){
    return previousState;
  }


  /** returns the new state of the dockable */
  public DockableState getNewState(){
    return newState;
  }

}
