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
import java.awt.Component;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** A DockView that can be nested into a TabbedDockableContainer
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class TabbedDockView extends DockView {
  
  public TabbedDockView(Dockable dockable) {
    super(dockable, false);
  }
  
  protected void scanDrop(DockEvent event, boolean drop){
    DockableState.Location location = dockable.getDockKey().getLocation();
    if (location == DockableState.Location.DOCKED) {
      super.scanDrop(event, drop);
    } else if (location == DockableState.Location.FLOATING){
      // don't allow drop for floating tabs : only when child of a compound dockable
      if (DockingUtilities.isChildOfCompoundDockable(dockable)){
        super.scanDrop(event, drop);
      } else {
        if (drop){
          ((DockDropEvent) event).rejectDrop();
        } else {
          ((DockDragEvent) event).delegateDrag();
        }
      }
    }
  }
  
  public void setVisible(boolean visible){
    super.setVisible(visible);
    if (visible){
      if (UIManager.getBoolean("TabbedContainer.requestFocusOnTabSelection")){
        // this is a workaround to get focus on a tab when it's selected
        // obviously, this relies in the fact that the parent of this dockView is
        // the tab container.
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            if (getDockable() != null){
              Component comp = getDockable().getComponent();
              if (comp != null){
                comp.requestFocus();
              }
            }
          }
        });
      }
    }
  }
  
  
}
