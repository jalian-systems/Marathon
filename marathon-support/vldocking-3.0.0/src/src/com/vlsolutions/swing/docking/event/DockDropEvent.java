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

import com.vlsolutions.swing.docking.ui.DockingUISettings;
import java.awt.event.*;

import com.vlsolutions.swing.docking.*;
import javax.swing.*;

/** An event send to a dockDropReceiver to complete a drop operation
 * <p>
 * This event is used by API extenders to manage drag and drop action related to
 * docking. This is not meant to be used by User Applications.
 *
 * <p>
 * The dockDropReceiver can accept or reject the drop.
 * <p>
 * If the drop is accepted, the receiver must process the docking operation,
 * (usually by invoking a docking method on the desktop).
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class DockDropEvent extends DockEvent {
  
  private DockingDesktop desk;
  private boolean accepted;
  
  public DockDropEvent(DockingDesktop desk, DockableDragSource source, MouseEvent event) {
    super(desk, source, event);
    this.desk = desk;
  }
  
  
  /** shortcut for acceptDrop(true).
   * <p>
   * If the drop is accepted, the receiver must process the docking operation,
   * (usually by invoking a docking method on the desktop).
   * <p>
   * As of version 2.1, a DockingActionEvent has been added to the DockEvent class, adding
   * a "semantic" description of the docking operation. The DropReceiver must call
   * setDockingAction() before acceptDrop().
   * <p>
   * It is still the responsability of the drop receiver to process the docking operation.
   * */
  public void acceptDrop(){
    acceptDrop(true);
  }
  
  /** notifies the event manager that docking is accepted.
   * <p>
   * If the drop is accepted, the receiver must process the docking operation,
   * (usually by invoking a docking method on the desktop).
   *
   * @param remove  if true, the docking desktop will remove the dragged dockable
   * from its containment hierarchy. If false, it is the responsibility of the
   * DockDropReceiver to remove (or simply move) the dragged dockable.
   * <p> for example, accepting drop without removing the component is saving
   * a lot of processing for TabbedDockableContainers when the dragged component
   * already belongs to their tabs.
   * */
  public void acceptDrop(boolean remove){
    this.accepted = true;
    if (remove){
      DockableState state = desk.getContext().getDockableState(source.getDockable());
      DockingDesktop sourceDesktop;
      if (state == null){
        // this should happen only with whole tabs drags as they don't have any state
        // associated with them (bug ?)
        if (source instanceof TabbedDockableContainer){
          Dockable d1 = ((TabbedDockableContainer)source).getDockableAt(0);
          state = desk.getContext().getDockableState(d1);
          sourceDesktop = state.getDesktop();
        } else {
          throw new RuntimeException("unmanaged dockable drag source : " + source);
        }
      } else {
        sourceDesktop = state.getDesktop();
      }
      if (sourceDesktop != null){
        sourceDesktop.dropRemove(source);
      }
    }
  }
  
  /** It is still time to reject a drop, although the standard way is by denying the
   * previous drag operation. */
  public void rejectDrop(){
    UIManager.getLookAndFeel().provideErrorFeedback(desk);
  }
  
  /** Indicates if the drop operation is accepted.
   * <p>
   * Once rejected (with <code>rejectDrop</code>, it is not possible to re-accept it
   * */
  public boolean isDropAccepted() {
    return accepted;
  }
  
}
