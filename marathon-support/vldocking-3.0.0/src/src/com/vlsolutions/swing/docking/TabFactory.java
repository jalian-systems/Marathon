package com.vlsolutions.swing.docking;


/** A factory that replaces DockViews (with title headers) by tabbed panes with a single tab.
 *<p>
 * This factory provides a unified GUI for single AND tabbed components (with the exception of 
 * auto-hide and single-floating that still use the standard title bars).
 *<p> 
 * It is still in an early stage and will be improved in future releases, depending on its 
 * adoption.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1.3
 *
 */
public class TabFactory  extends DefaultDockableContainerFactory {
  public SingleDockableContainer createDockableContainer(Dockable dockable, ParentType parentType) {
    switch (parentType){
      case PARENT_TABBED_CONTAINER:
        return new TabbedDockView(dockable);
      case PARENT_DESKTOP:
        return new MaximizedDockViewAsTab(dockable);//MaximizedDockView(dockable);
      case PARENT_SPLIT_CONTAINER:
        return new DockViewAsTab(dockable);
      case PARENT_DETACHED_WINDOW:
        return new DetachedDockView(dockable);
      default :
        throw new RuntimeException("Wrong dockable container type");
    }
  }

  
}