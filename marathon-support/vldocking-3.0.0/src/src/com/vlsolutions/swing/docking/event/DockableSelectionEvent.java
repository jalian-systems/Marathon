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

/** An event for tracking selection changes of dockables. 
 * <p>
 *  Useful for example when  
 *  the developper wants to enable or disable actions depending on the dockable 
 *  which takes the keyboard focus.
 *
 * <p> implementation note : works with keyboard focus events.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.0
 * @see DockableSelectionListener
 */
public class DockableSelectionEvent {
  private Dockable selectedDockable;
  public DockableSelectionEvent(Dockable selectedDockable) {
    this.selectedDockable = selectedDockable;
  }
  
  /** Returns the currently selected Dockable */
  public Dockable getSelectedDockable(){
    return selectedDockable;
  }
  
  
}
