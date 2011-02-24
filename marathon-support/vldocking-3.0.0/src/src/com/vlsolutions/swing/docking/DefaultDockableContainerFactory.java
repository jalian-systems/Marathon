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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

/** Default implementation of the DockableContainerFactory.
 * <p>
 * Uses <code>DockView</code> as a <code>SingleDockableContainer</code>,
 * and <code>DockTabbedPane</code> as a <code>TabbedDockableContainer</code>
 * <p>
 * Note : the API has changed between VLDocking 1.1 and 2.0 : the createDockableContainer
 * now uses a second parameter to specify the context of the container creation.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 2.0
 */

public class DefaultDockableContainerFactory extends DockableContainerFactory {
  
  
  public DefaultDockableContainerFactory() {
  }
  
  /** Returns the component used to modify the expand panel size when expanded from the top.
   * This implementation uses the following components :
   *<ul>
   * <li> DockView for standard dockables
   * <li> TabbedDockView for dockable contained in a tabContainer
   * <li> MaximizedDockView for maximized dockables
   * <li> DetachedDockView for floating dockables.
   *</ul>
   *
   *@see DockView
   *@see TabbedDockView
   *@see MaximizedDockView
   *@see DetachedDockView
   */
  public SingleDockableContainer createDockableContainer(Dockable dockable, ParentType parentType) {
    switch (parentType){
      case PARENT_TABBED_CONTAINER:
        return new TabbedDockView(dockable);
      case PARENT_DESKTOP:
        return new MaximizedDockView(dockable);
      case PARENT_SPLIT_CONTAINER:
        return new DockView(dockable);
      case PARENT_DETACHED_WINDOW:
        return new DetachedDockView(dockable);
      default :
        throw new RuntimeException("Wrong dockable container type");
    }    
  }
  
  /** Returns the container used for tabbed docking.
   *<p>
   * Current implementation uses the DockTabbedPane class.
   *
   * @see DockTabbedPane
   */
  public TabbedDockableContainer createTabbedDockableContainer() {
    return new DockTabbedPane();
  }
  
  
  /** This method is called when a dockable is detached from the DockingDesktop and put
   * in the FLOATING state.
   *<p>
   * The floating container must be an instanceof Dialog or Window.
   */
  public FloatingDockableContainer createFloatingDockableContainer(Window owner){
    if (owner instanceof Dialog){
      return new FloatingDialog((Dialog)owner);
    } else {
      return new FloatingDialog((Frame)owner);
    }
  }
  
  /** This implementation of the factory method return a default DockViewTitleBar 
   *  object.
   *@since 2.1.3
   */
  public DockViewTitleBar createTitleBar() {
    return new DockViewTitleBar();
  }
  
  
  
}
