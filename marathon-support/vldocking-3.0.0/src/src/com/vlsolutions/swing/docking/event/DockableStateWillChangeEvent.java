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
/** An event triggered before a change of dockable state.
 *<P> Events are triggered when a component is :
 * <UL>
 * <LI> Docked (added to the desktop)
 * <LI> set in Auto-hide mode (collapsed as a border button)
 * <LI> closed (removed from the desktop)
 * <LI> maximized
 * <LI> floating (detached from its window)
 * </UL>
 *
 * <P> Note that event are not triggered by drag-dock moves (they don't correspond
 * to a state change). If you need to track such changes, use a Swing AncestorListener,
 * or override the addNotify method of your dockable component.
 *
 * <P> It is also possible to track changes after they occur with
 * the DockableStateChangeEvent .
 *
 * @see javax.swing.event.AncestorListener
 * @see DockableStateWillChangeListener
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class DockableStateWillChangeEvent {
  private DockableState currentState, futureState;

  private boolean isAccepted;

  public DockableStateWillChangeEvent(DockableState currentState, DockableState futureState) {
    this.currentState = currentState;
    this.futureState = futureState;
    this.isAccepted = true;
  }

  /** this method will return null when the event is triggered for initial docking
   * */
  public DockableState getCurrentState(){
    return currentState;
  }

  /** Returns the future (proposed) state of the dockable. if cancel() is called on the event, 
   * this state change will be vetoed.
   *@see #cancel()
   */
  public DockableState getFutureState(){
    return futureState;
  }

  /** Refuse the change of dockable state.
   * <P> The docking action is cancelled.
   *  */
  public void cancel(){
    isAccepted = false;
  }

  public boolean isAccepted() {
    return isAccepted;
  }

}
