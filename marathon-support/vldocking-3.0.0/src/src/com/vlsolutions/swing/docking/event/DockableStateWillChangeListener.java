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

/** Interface implemented by classes that need to be notified of
 * a state change of a dockable before it is processed.
 * <p>
 * Events are triggered before a Dockable component is :
 * <ul>
 * <li> docked (added to the desktop)
 * <li> set in Auto-hide mode (collapsed as a border button)
 * <li> closed (removed from the desktop)
 * <li> maximized
 * <li> floating (detached)
 * </ul>
 * <p> Those event are vetoable : invoking their <code>cancel</code> method
 * will stop the state change processing.
 *
 *
 * @see com.vlsolutions.swing.docking.DockingDesktop#addDockableStateWillChangeListener(DockableStateWillChangeListener)
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public interface DockableStateWillChangeListener {


  /** invoked before a dockable state change.
   * @param event  a vetoable state change event.
   *
   */
  public void dockableStateWillChange(DockableStateWillChangeEvent event);

}
