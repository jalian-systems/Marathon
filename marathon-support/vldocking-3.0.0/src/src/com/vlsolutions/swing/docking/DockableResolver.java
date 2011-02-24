package com.vlsolutions.swing.docking;

/** An interface used to find a dockable (when read from an input stream like XML)
 * when the only information given is its DockKey id.
 * 
 * <p>This is mainly to allow auto-registration of new dockables when loading a new workspace.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1.2
 */
public interface DockableResolver {
  
  /** Returns the dockable which should be associated to this DockKey identifier, or null if 
   * not found.
   */
  public Dockable resolveDockable(String keyName);
  
}
