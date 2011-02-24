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

import java.awt.*;

/** General information about the current state of a dockable component.
 * <p>
 * This class is a simple record of a dockable and its current display state.
 * <p> It is used together with DockableStateChangeEvents (and listeners) to
 * track changes of Dockable visibility.
 *
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 *
 * @update 2005/10/07 Lilian Chamontin : added getStateName(int state) method
 */
public class DockableState implements Comparable {

  private Dockable dockable;
  
  //private static final String [] stateNames = {"CLOSED", "DOCKED", "HIDDEN", "MAXIMIZED", "FLOATING"};

  /** Possible locations of a dockable */
  public enum Location {
      /** A constant describing a Dockable that is not visible.
       *
       */
      CLOSED,

      /** A constant describing a Dockable that is visible (displayed in a DockableContainer) */
      DOCKED,

      /** A constant describing a Dockable that is in auto-hide mode (reduced to a button) */
      HIDDEN,

      /** A constant describing a Dockable that is currently maximized */
      MAXIMIZED,

      /** A constant describing a Dockable that is currently floating (detached from the desktop) */
      FLOATING
    }
  
  private Location location ;



  /** Relative position for hidden/closed dockable show-again */
  private RelativeDockablePosition position;

  /** Desktop currently using this dockable */
  private DockingDesktop desktop;


  public DockableState() {
    position = new RelativeDockablePosition();
  }

  public DockableState(DockingDesktop desktop, Dockable dockable, Location location) {
    this.desktop = desktop;
    this.dockable = dockable;
    this.location = location;
    position = new RelativeDockablePosition();
  }

  public DockableState(DockingDesktop desktop, Dockable dockable, Location location, RelativeDockablePosition position) {
    this.desktop = desktop;
    this.dockable = dockable;
    this.location = location;
    this.position = position;
  }
  
  /** Creates a dockable state based on an existing state, with another relative positionning */
  public DockableState(DockableState copy, RelativeDockablePosition position) {
    this.desktop = copy.desktop;
    this.dockable = copy.dockable;
    this.location = copy.location;
    this.position = position;
  }

  /** Comparable interface, used to sort components by name order.
   * */
  public int compareTo(Object object) {
    if (object instanceof DockableState){
      return dockable.getDockKey().getName().compareTo( ( (DockableState)
          object).dockable.getDockKey().getName());
    } else {
      return -1;
    }
  }

  /** Returns the dockable this state is for */
  public Dockable getDockable() {
    return dockable;
  }

  /** Convenience method returning wether the dockable is in the DOCKED state */
  public boolean isDocked(){
    return location == Location.DOCKED;
  }

  /** Convenience method returning wether the dockable is in the HIDDEN state */
  public boolean isHidden(){
    return location == Location.HIDDEN;
  }
  
  /** Convenience method returning wether the dockable is in the CLOSED state */
  public boolean isClosed(){
    return location == Location.CLOSED;
  }
  
  /** Convenience method returning wether the dockable is in the FLOATING state */
  public boolean isFloating(){
    return location == Location.FLOATING;
  }
  
  /** Convenience method returning wether the dockable is in the MAXIMIZED state */
  public boolean isMaximized(){
    return location == Location.MAXIMIZED;
  }

  /** Returns the current location of the dockable (CLOSED, DOCKED, HIDDEN...).
   */
  public Location getLocation() {
    return location;
  }

  /** Returns the relative restore position of the dockable when it is not shown (auto-hide
   * or closed).
   * <P>
   * Please note that this position is meaningless for visible components
   * (as it is not automatically calculated at every move / resize of the dockable ).
   *
   * */
  public RelativeDockablePosition getPosition(){
    return position;
  }

  public String toString(){
    return "DockableState [" + dockable.getDockKey() + ", state=" + location.name() + ", position="
        + position;
  }
  
  
  /** Returns the desktop currently using this dockable, or null if none 
   *
   * @since 2.1
   */
  public DockingDesktop getDesktop() {
    return desktop;
  }

  /** Updates the desktop field (desktop using this dockable)
   * @since 2.1
   */
  public void setDesktop(DockingDesktop desktop) {
    this.desktop = desktop;
  }
  
  /** Utility (compatibility) method used to associate an int to the Location enumeration
   * (opposite to location.ordinal()
   */
  public static Location getLocationFromInt(int state) {
      switch (state){
          case 0 : return Location.CLOSED;
          case 1 : return Location.DOCKED;
          case 2 : return Location.HIDDEN;
          case 3 : return Location.MAXIMIZED;
          case 4 : return Location.FLOATING;
          default: return null;
      }
  }


}
