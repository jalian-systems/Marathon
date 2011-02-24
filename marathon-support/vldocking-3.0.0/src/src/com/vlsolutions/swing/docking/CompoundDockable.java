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

import java.awt.Component;

/** Describes a Dockable that can contain sub-dockables.
 * <p>
 * This class allows dockable nesting, with full support for workspace save and restore.
 * <p> 
 * Currently this class is associated to a CompoundDockingPanel component : you 
 * insert dockables into it with an initial addDockable(CompoundDockable) invocation from DockingDesktop, 
 * and further dockables with split/createTab calls.
 * <p>
 * Example :
 * <pre>
 *   DockingDesktop desk = ...
 *   desk.addDockable(dockable1);
 *   desk.split(dockable1, dockable2, DockingConstants.SPLIT_RIGHT);
 *   // now let's add a compound dockable
 *   CompoundDockable compound = new CompoundDockable(new DockKey("Compound!"));
 *   desk.split(dockable1, compound);
 *   // and add a tab into it
 *   desk.addDockable(compound, dockable3); // initial nesting : new API call
 *   desk.createTab(dockable3, dockable4, 1); // a tab, using standard API
 * </pre>
 * <p>
 * CompoundDockables are displayed as SingleDockableContainers (with a title bar). Don't 
 * forget to properly initialize the DockKey of this dockable to have it well presented on 
 * screen (name, tooltip, icon).
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1
 */
public class CompoundDockable implements Dockable {

  private CompoundDockingPanel panel = new CompoundDockingPanel(this);

  private DockKey key;
  
  /** Constructs a new CompoundDockable with a given key */
  public CompoundDockable(DockKey key){
    this.key = key;
  }
  
  /** Returns the key used to describe this dockable */
  public DockKey getDockKey() {
    return key;
  }

  /** Returns the component used by this dockable.
   *
   * <p> 
   * Implementation note : this method always returns a CompoundDockingPanel.
   * 
   */
  public final Component getComponent() {
    return panel;
  }
  
}
