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

/** A DockableContainer is the base interface of GUI components used to
 * display <code>Dockable</code> components.
 * <p>
 * The components are usually displayed with decorations
 * (a title bar, docking state management buttons, a drop shadow, ...)
 * <p>
 * This interface is meant for API extenders that would like to create new kind of
 * containers (via the sub-interfaces {@link SingleDockableContainer}
 * and {@link TabbedDockableContainer} and the {@link DockableContainerFactory}).
 * <p>
 * Usually, a DockableContainer also contains (or implements) one ore more
 * <code>DockableDragSource</code>s
 * in order to achieve drag and drop operations.
 * <p>
 *
 * @see DockableDragSource
 * @see DockableContainerFactory
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */

public interface DockableContainer {


  /** Invoked once after creation, in order to let this component register
   * its DockableDragSources to the DockingDesktop, and have a reference of this
   * Desktop.
   *
   * <P> basic implementation use a single DragSource, and thus calls
   * desk.installDockableDragSource(dragSource).
   *
   *
   * */
  public void installDocking(DockingDesktop desktop);

  /** Called once, when the dockablecontainer is no longer used by the desktop,
   * in order to releases resources and listeners taken on <code>installDocking()<code>.
   *  */
  public void uninstallDocking(DockingDesktop desktop);
  
  

}
