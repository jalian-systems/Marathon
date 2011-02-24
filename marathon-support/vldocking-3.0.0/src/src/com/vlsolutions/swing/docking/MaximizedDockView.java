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

import com.vlsolutions.swing.docking.event.DockDragEvent;
import com.vlsolutions.swing.docking.event.DockDropEvent;
import com.vlsolutions.swing.docking.event.DockEvent;

/** A dockView suitable for maximized dockables
 *
 * @author Lilian Chamontin, VLSolutions
 * @update 2007/01/24 Lilian Chamontin : added DnD blocking (maximized components should't support 
 * drag/drop from floating windows)
 */
public class MaximizedDockView extends DockView {
  
  public MaximizedDockView(Dockable dockable) {
    super(dockable, true);   
  }

  /** maximized dockable don't support drag and drop */
  public void processDockableDrag(DockDragEvent event) { //2007/01/24
    event.rejectDrag();
  }

  /** maximized dockable don't support drag and drop */
  public void processDockableDrop(DockDropEvent event) {
    event.rejectDrop();
  }

  
  
}
