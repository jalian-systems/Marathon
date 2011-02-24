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

/** This interface describes a listener for dockable selection changes.
 *
 *<p> The notification is currently based on keyboard focus policy (the event is 
 * triggered when a new dockable grabs the keyboard focus).
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.0
 * @see DockableSelectionEvent
 * @see com.vlsolutions.swing.docking.DockingDesktop#addDockableSelectionListener(DockableSelectionListener)
 */
public interface DockableSelectionListener {
  
  /** This method is invoked when a new dockable is selected. */
  public void selectionChanged(DockableSelectionEvent e) ;
  
}
