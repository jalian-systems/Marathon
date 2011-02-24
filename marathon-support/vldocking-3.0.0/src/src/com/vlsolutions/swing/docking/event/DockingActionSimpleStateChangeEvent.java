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
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;

/** An event describing a basic state change.
 *<p> 
 * Baic state changes include :
 *<ul>
 * <li> Maximize/Restore
 * <li> Float (just detach : attach is expressed with split/addDockable/createTab events)
 * <li> Hide (just hide : show is expressed with split/addDockable/createTab events)
 *</ul>
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class DockingActionSimpleStateChangeEvent extends DockingActionDockableEvent {
    /** Constructs a new event  */
    public DockingActionSimpleStateChangeEvent(DockingDesktop desktop,
            Dockable dockable, DockableState.Location initiallocation, DockableState.Location nextLocation) {
        super(desktop, dockable, initiallocation, nextLocation, ACTION_STATE_CHANGE);
    }

    public String toString() {
        return "DockingActionSimpleStateChangeEvent (" + getInitialLocation().name() + " -> " + getNextLocation().name();
    }
}
