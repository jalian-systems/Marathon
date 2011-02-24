package com.vlsolutions.swing.docking;

/** The interface describing a Floating window used to display one or more dockables 
 * <p>
 * Objects implementing this interface must be instances of JDialog , as the 
 * pack(), setVisible(), setLocation(), dispose() etc. are assumed. 
 *
 * @author Lilian Chamontin, VLSolutions
 */
public interface FloatingDockableContainer {
  
  /** This install method is invoked before adding the first dockable */
  public void installDocking(DockingDesktop desktop);
  
  /** adds the initial dockable to this top-level container */
  public void setInitialDockable(Dockable dockable);
  
  /** adds the initial tabbdeddockablecontainer to this top-level container.
   * This will happen when drag-n-dropping a whole tab container from the desktop.
   */
  public void setInitialTabbedDockableContainer(TabbedDockableContainer tdc);
  
  
}
