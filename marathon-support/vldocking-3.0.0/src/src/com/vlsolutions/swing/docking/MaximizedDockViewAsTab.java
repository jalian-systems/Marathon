package com.vlsolutions.swing.docking;

import com.vlsolutions.swing.docking.event.DockDragEvent;
import com.vlsolutions.swing.docking.event.DockDropEvent;
import java.awt.Point;
import javax.swing.AbstractAction;
import javax.swing.UIManager;

/** A maximized view to use with the TabFactory.
 *
 * @author Lilian Chamontin, VLSolutions
 * @see TabFactory
 * @since 2.1.3
 */
public class MaximizedDockViewAsTab extends DockViewAsTab{
  
  public MaximizedDockViewAsTab(Dockable dockable) {
    super(dockable);
  }
  
  protected void configureMaximizeButton() {
    if (isRestoreButtonDisplayed){
      maximizeSmartIcon.setIcon(UIManager.getIcon("DockViewTitleBar.restore"));
      maximizeSmartIcon.setPressedIcon(UIManager.getIcon("DockViewTitleBar.restore.pressed"));
      maximizeSmartIcon.setRolloverIcon(UIManager.getIcon("DockViewTitleBar.restore.rollover"));
      // add a tooltip
      maximizeAction.putValue(AbstractAction.SHORT_DESCRIPTION,
          UIManager.get("DockViewTitleBar.restoreButtonText"));
    }
  }
  
  protected void configureFloatButton() {
    isFloatButtonDisplayed = false;
  }
  
  protected void configureHideButton() {
    isHideButtonDisplayed = false;
  }
  
  protected void configureCloseButton() {
    isCloseButtonDisplayed = false;
  }
  
  protected TabHeader createTabHeader(){
    return new MaximizedTabHeader();
  }

  public void processDockableDrag(DockDragEvent event) {
    event.rejectDrag();
  }

  public void processDockableDrop(DockDropEvent event) {
    event.rejectDrop();
  }
  
  
  
  
  protected class MaximizedTabHeader extends TabHeader {
    /** {@inheritDoc} */
    public boolean startDragComponent(Point p) {
      // disable DnD for some cases :
      // - child of a compound dockable, in hidden state
      // - child of a maximized compund dockable
      // - maximized dockable
      Dockable target = getDockable();
      DockableState.Location targetState = target.getDockKey().getLocation();
      if (targetState == DockableState.Location.HIDDEN){
        if (DockingUtilities.isChildOfCompoundDockable(target)){
          // nested hidden dockables cannot be drag and dropped
          return false;
        }
      } else if (targetState == DockableState.Location.DOCKED){
        boolean isChildOfMaximizedContainer = false;
        if (desktop != null){
          Dockable max = desktop.getMaximizedDockable();
          if (max != null && max.getComponent().getParent().isAncestorOf(this)){
            isChildOfMaximizedContainer = true;
          }
        }
        if (isChildOfMaximizedContainer){
          return false;
        }
      } else if (targetState == DockableState.Location.MAXIMIZED){
        return false;
      }
      
      return true;
      
    }
  }
  
}
