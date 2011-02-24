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


package com.vlsolutions.swing.docking.ui;

import com.vlsolutions.swing.docking.DockView;
import com.vlsolutions.swing.docking.ShadowBorder;
import com.vlsolutions.swing.docking.SplitContainer;
import com.vlsolutions.swing.docking.TabbedDockableContainer;
import java.awt.Color;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;

/** This is the UI delegate for the DockView class.
 *
 * <p> Default behaviour in to install a shadow border on the dockview, 
 * but this can be replaced by overriding the 3 install<i>XXX</i>DockableBorder methods.
 *
 * @author Lilian Chamontin, VLSolutions
 *
 * @since 2.0
 */
public class DockViewUI extends PanelUI {
  
  /** Ancestor listener used to install different borders depending on the usage 
   * of the dock view (docked, maximized, tabbed).
   */
  protected ViewAncestorListener ancestorListener = new ViewAncestorListener();
  
  private static DockViewUI instance = new DockViewUI();

  
  public DockViewUI() {    
  }
  
  
  public static ComponentUI createUI(JComponent c) {
      return instance;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    c.addAncestorListener(ancestorListener);
  }
  
  public void uninstallUI(JComponent c){
    super.uninstallUI(c);
    c.removeAncestorListener(ancestorListener);
    
  }


  /** Installs a border when the DockView target is a SingleDockableContainer 
   * (not nested in a tabbed container )
   *
   *<p> default is a shadowed border.
   */
  protected void installSingleDockableBorder(DockView v){
    Border b = UIManager.getBorder("DockView.singleDockableBorder");
    v.setBorder(b);
    
  }

  /** Installs a border when the DockView target is included in a TabbedDockableContainer.
   * 
   *<p> Default is a shadow border without top and left shadows
   *
   */
  protected void installTabbedDockableBorder(DockView v){
    Border b = UIManager.getBorder("DockView.tabbedDockableBorder");
    v.setBorder(b);

  }

  /** Installs a border when the DockView target is unique on the desktop (mamimized, or alone)
   * 
   *<p> Default is a shadow border without top and left shadows
   *
   */
  protected void installMaximizedDockableBorder(DockView v){
    Border b = UIManager.getBorder("DockView.maximizedDockableBorder");
    v.setBorder(b); 
  }

  
  class ViewAncestorListener implements AncestorListener {
      public void ancestorAdded(AncestorEvent ancestorEvent) {
        DockView v = (DockView)ancestorEvent.getComponent();
        Container parent = v.getParent();
        if (parent instanceof TabbedDockableContainer){
          installTabbedDockableBorder(v);
        } else if (parent instanceof SplitContainer){
          installSingleDockableBorder(v);
        } else {
          installMaximizedDockableBorder(v);
        }
      }
      public void ancestorMoved(AncestorEvent ancestorEvent) {  }
      public void ancestorRemoved(AncestorEvent ancestorEvent) { }
  }


  
}
