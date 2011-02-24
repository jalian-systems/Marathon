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

import java.awt.event.*;
import java.awt.*;

import com.vlsolutions.swing.docking.*;

/** An event describing a drag operation occuring for docking purpose.
 * <p>
 * This event is used by API extenders to manage drag and drop action related to
 * docking. This is not meant to be used by User Applications.
 * <p>
 * Standard processing of dragging is (ignoring DockableStateChange events) :
 * <ul>
 *  <li>  the users starts a drag gesture from a DockableDragSource
 *  <li>  the mouse reaches a component implementing DockDropReceiver
 *  <li>  a DockDragEvent is created and passed to the DockDropReceiver
 *  <li>  the receiver accepts that drag with {@link #acceptDrag(Shape)}
 *  <li>  the shape is used to give a feeback to the user
 * </ul>
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class DockDragEvent extends DockEvent {

  private boolean acceptDrag = false;
// when accept drag is false, indicates if the docking should check ancestors
  private boolean delegateDrag = false;
  private Shape dropShape;



  /** Creates an event based on a drag source and a mouse event.
   * <P> The mouse coordinates are converted into the receiver's coordinates.*/
  public DockDragEvent(DockingDesktop desktop, DockableDragSource source, MouseEvent event) {
    super(desktop, source, event);
  }

  /** returns true if drag is accepted.
   *
   * @return true if drag is accepted
   */
  public boolean isDragAccepted(){
     return acceptDrag;
  }

  /** If drag is not accepted, this method will be invoked to evaluate
   * a delegating event processing.
   * <p>
   * This is useful for nested DropReceivers (like TabbedDockableContainers) */
  public boolean isDragDelegated(){
     return !acceptDrag && delegateDrag;
  }

  /** Returns the shape to display if drag is accepted.
   * <p>
   *  This method will be invoked only if <code>isDragAccepted</code> returns true
   * <p>
   * As this method can be called on every mouse drag over a component, it is
   * a good practice to reuse the same shape as lond a possible (do not create a
   * new Shape at every invocation).
   * */
  public Shape getDropShape(){
     return dropShape;
  }

  /** Used by a DockDropReceiver to indicate that the drag operation is accepted
   * (mouse is over a droppable zone).
   * <p>
   * As of version 2.1, please note that you also have to invoke #setDockingAction()
   * to specify the action associated with the drag shape.
   *
   * @param dropShape the shape (in drop component coordinates) showing the drop zone,
   * which will be displayed on the glasspane of the DockingPanel.
   * */
  public void acceptDrag(Shape dropShape){
      this.acceptDrag = true;
      this.dropShape = dropShape;
  }

  /** Used to reject the drag (e.g. when trying to drop a component onto itself) */
  public void rejectDrag(){
     this.acceptDrag = false;
     this.delegateDrag = false;
  }

  /** Rejects the drag, but allows the docking system to search for othet drop receiver
   * in ancestors.
   * <p>
   *  for example, a TabbedDockableContainer that doesn't allow drops on its
   * borders can delegates this management to its container.
   */
  public void delegateDrag(){
    this.acceptDrag = false;
    this.delegateDrag = true;
  }



}
