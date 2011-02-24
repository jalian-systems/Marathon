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
import com.vlsolutions.swing.docking.event.*;

/** An interface implemented by components that can accept docking action with mouse drag and drop.
 * <p>
 * This interface is used by API Extenders to create new kind of drop receivers.
 * <p>
 * The DockDropReceiver is responsible for the Drag and Drop gesture recognition of
 * a docking movement. When a user starts a drag gesture over a {@link DockableDragSource},
 * the DockingDesktop scans its containment hierarchy (under the mouse pointer), looking
 * for DockDropReceivers. If one is found, it is asked for drag or drop processing.
 * <p>
 * The receiver can reject a drag, or display a shape explaining to the user how a
 * drop of the component would affect the global layout.
 *
 *
 * @see DockableDragSource
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public interface DockDropReceiver {


  /** The component is notified of a drag operation.
   *
   * <p>
   * This method is used to give a visual clue of dropping capability
   * (for example, mouse cursor change, painting a shadow of future component position)
   *
   *
   * <p>
   * These clues are transmitted via the event's methods.
   *
   * @see DockDragEvent
   * @see DockDropEvent
   *
   * */
  public void processDockableDrag(DockDragEvent event);

  /** The component is requested to perform a docking drop action.
   * <p>
   *  This method is called only after a successful {@link #processDockableDrag(DockDragEvent)}
   * <p>
   * The parameters provided for the drop are the same than those of the last drag
   * (in order to avoid an allowed last drag followed by a rejected drop).
   * <p>
   * On event acceptation ( {@link DockDropEvent#acceptDrop() } , the source component
   * will be removed from its container, so the DropReceiver <b>must</b> add the
   * component to its own hierarchy otherwise
   * the component would be lost (from a user's point of view).
   * */
  public void processDockableDrop(DockDropEvent event) ;

}
