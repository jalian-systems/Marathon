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
import com.vlsolutions.swing.docking.*;

/** Abstract superclass of drag and drop docking events.
 * <P> This class holds a reference to the source of the event and its mouse event.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 *  */
public abstract class DockEvent {
  /** The drag source */
  protected DockableDragSource source;

  /** The docking desktop which is the destination of the drag and drop gesture */
  protected DockingDesktop desktop;

  /** The MouseEvent, in target's coordinates */
  protected MouseEvent event;
  
  /** The action corresponding to the drag and drop gesture 
   * (actions can be seen as allowed docking gestures)
   */
  protected DockingActionEvent action;

  /** Constructor for dock events.
   *
   * @param targetDesktop the desktop which will receive the drop 
   * @param source the source of drag operation
   * @param event the mouse event which is the cause of this DockEvent.
   */
  public DockEvent(DockingDesktop targetDesktop, DockableDragSource source, MouseEvent event) {
    this.desktop = targetDesktop;
    this.source = source;
    this.event = event;
  }

  /** Returns the MouseEvent which is the cause of this DockEvent.
   * <p>
   * <b> note that mouse coordinates are converted into the target's coordinates.</b>
   * @return a mouse event
   */
  public MouseEvent getMouseEvent(){
    return event;
  }

  /** Returns a reference to the drag source of this event.
   *
   * @return the drag source (the component actually dragged)
   */
  public DockableDragSource getDragSource(){
    return source;
  }

  /** Returns a reference of the desktop in which the docking event takes place.
   *<p> 
   * As of version 2.1, this method returns the "target" desktop (the one in which the drop will occur)
   *<p>
   * To get a reference of the "source" desktop, use the DockableState information provided by 
   * the DockingContext.
   *
   */
  public DockingDesktop getDesktop(){
    return desktop;
  }

  
  /** Returns the DockingActionEvent corresponding to the drag and drop gesture */
  public DockingActionEvent getDockingAction(){
    return action;
  }
  
  /** Set the docking action corresponding to this drag and drop gesture.
   *
   */
  public void setDockingAction(DockingActionEvent actionEvent){
    this.action = actionEvent;
  }


}
