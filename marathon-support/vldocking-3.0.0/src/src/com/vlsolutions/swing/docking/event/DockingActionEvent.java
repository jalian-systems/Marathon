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

import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;

/** An abstract event class describing a docking action.
 *<p> 
 * This event is used to explain a dockable action : it doesn't contain any information 
 * about the current state of a dockable, but describes precisely what we want to do 
 * with it (for example, detach it, or left-split another dockable).
 *<p>
 * The DockingActionDockableEvent abstract subclass is used as a superclass for all events 
 * associated to a single dockable.
 *<p>
 * Concrete sub classes are used for every type of docking action (split, tab...)
 * 
 * @since 2.1 
 *
 * @author Lilian Chamontin, VLSolutions
 */
public abstract class DockingActionEvent implements Cloneable {
  
  /* action descriptors : allow for fast switch/case tests */
  /** Field associated to a closing action */
  public static final int ACTION_CLOSE = 0;
  /** Field associated to an addDockable action */
  public static final int ACTION_ADD_DOCKABLE = 1;
  
  /** Field associated to a split dockable action */
  public static final int ACTION_SPLIT_DOCKABLE = 2;
  
  /** Field associated to a split component action */
  public static final int ACTION_SPLIT_COMPONENT = 3;
  
  /** Field associated to a multipurpose (API internals) splut container action */
  public static final int ACTION_SPLIT_DOCKABLE_CONTAINER = 4;

  /** Field associated to a createTab action */
  public static final int ACTION_CREATE_TAB = 5;
  
  /** A multipurpose action used to describe maximize, hide and detach actions */
  public static final int ACTION_STATE_CHANGE = 6;
  
  
  private DockableState.Location initialLocation;
  private DockableState.Location nextLocation;
  private int actionType;
  private DockingDesktop desktop;
  
  /** Constructs a new DockingActionEvent 
   * 
   * @param targetDesktop the desktop that will receive the action .
   * @param initialState  the initial (before action) DockableState (DockableState.STATE_FLOATING...)
   * @param nextState     the next (after action) DockableState (DockableState.STATE_DOCKED...)
   * @param actionType    a field defining the type of action used (ACTION_CLOSE...)
   */
  public DockingActionEvent(DockingDesktop targetDesktop, DockableState.Location initialLocation, DockableState.Location nextLocation, int actionType) {
    this.desktop = targetDesktop;
    this.initialLocation = initialLocation;
    this.nextLocation = nextLocation;
    this.actionType = actionType;
  }

  /** Returns one of the DockableState states reflecting the state of the dockable before the action */
  public DockableState.Location getInitialLocation() {
    return initialLocation;
  }

  /** Returns one of the DockableState states reflecting the state of the dockable after the action */
  public DockableState.Location getNextLocation() {
    return nextLocation;
  }

  /** Returns a field describing the action (ACTION_CLOSE...).
   */
  public int getActionType() {
    return actionType;
  }

  /** Returns the desktop used as a receiver (target) of the docking action.
   *<p>
   * The desktop used as a source (on multi-desktop applications) can be retrieved by asking for 
   * the current dockable state (to the DockingContext).
   */
  public DockingDesktop getDesktop(){
    return desktop;
  }
  
  /** Overriden clone method */
  public Object clone(){
    try {
      return super.clone();
    } catch (CloneNotSupportedException e){
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  
}
