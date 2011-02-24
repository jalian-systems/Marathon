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

import javax.swing.JPopupMenu;

/** An objet that can provide actions relative to a dockable.
 *<p> 
 * It can be used, for example, to add a set of menu items to the contextual menu 
 * associated with any dockable.
 *<p> Example :
 * <pre>
 *   DockableActionCustomizer customizer = new DockableActionCustomizer(){
 *     public void visitSingleDockableTitleBarPopUp(JPopupMenu popUpMenu, Dockable dockable){
 *        popUpMenu.add(new JMenuItem("test")); // a menu for SingleDockableContainers
 *     }
 *     public void visitTabSelectorPopUp(JPopupMenu popUpMenu, Dockable dockable){
 *          popUpMenu.add(new JMenuItem(closeAllInTab)); // two menus when contained in a tabbed container
 *          popUpMenu.add(new JMenuItem(closeAllOtherInTab));
 *     }
 *   };
 *   customizer.setSingleDockableTitleBarPopUpCustomizer(true); // enable single
 *   customizer.setTabSelectorPopUpCustomizer(true); // enable tabbed
 *   DockKey key = ...
 *   key.setActioncustomizer(customizer); // assoiate it with one or more DockKeys
 * </pre>
 * <p>
 * Implementation Note : the customizer is invoked every time a pop-up is triggered 
 * from the associated dockable container. It is a good practice to keep object creation
 * and listener attachments outside the visit..() methods.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class DockableActionCustomizer {
  
  private boolean isSingleDockableTitleBarPopUpCustomizer = false;
  private boolean isTabSelectorPopUpCustomizer = false;
  
  /** Default constructor */
  public DockableActionCustomizer() {
  }
  
  /** Override this method to add contextual items at the end of the pop-up menu
   * which can be triggered on a tab selector.
   *<p> 
   * It is recommended to add menu items at the end to the pop-up menu as other 
   * items may have been installed by other visitors (DockGroup for example)
   *<p> 
   * implementation note : This method can be called multiple times as pop-up menus are built during 
   * the contextual event dispatch (not stored for the life of the dockable).
   *
   */
  public void visitTabSelectorPopUp(JPopupMenu popUpMenu, Dockable tabbedDockable){    
  }
  
  /** Override this method to add contextual items at the end of the pop-up menu
   * which can be triggered on the title bar of a single dockable.
   *<p> 
   * It is recommended to add menu items at the end to the pop-up menu as other 
   * items may have been installed by other visitors (DockGroup for example)
   *<p> 
   * implementation note : This method can be called multiple times as pop-up menus are built during 
   * the contextual event dispatch (not stored for the life of the dockable).
   *
   */
  public void visitSingleDockableTitleBarPopUp(JPopupMenu popUpMenu, Dockable dockable){
    
  }
  
  /** Returns wether this customizer is used in the context of a SingleDockableContainer */
  public boolean isSingleDockableTitleBarPopUpCustomizer(){
    return this.isSingleDockableTitleBarPopUpCustomizer;    
  }
  
  /** Updates the singleDockableTitleBar property.
   * @see #isSingleDockableTitleBarPopUpCustomizer()
   */
  public void setSingleDockableTitleBarPopUpCustomizer(boolean isCustomizer){
    this.isSingleDockableTitleBarPopUpCustomizer = isCustomizer;
  }
  
  /** Returns wether this customizer is used in the context of a TabbedDockableContainer */
  public boolean isTabSelectorPopUpCustomizer(){
    return this.isTabSelectorPopUpCustomizer;
  }
  
  
  /** Updates the tabSelectorDockableTitleBar property 
   * @see #isTabSelectorPopUpCustomizer()
   */
  public void setTabSelectorPopUpCustomizer(boolean isCustomizer){
    this.isTabSelectorPopUpCustomizer = isCustomizer;
  }
  
  
}
