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

/** A SingleDockableContainer is a DockableContainer that can display
 * a single <code>Dockable</code> component.
 * <p>
 * The components is usually displayed with decorations
 * (a title bar, docking state management buttons, a drop shadow, ...).
 * <p>
 * This interface is meant for API Extenders in order to provide new kinds of
 * SingleDockableContainers (if the default implementation, <code>DockView</code> hasn't
 * got enough features or to provide a different look and feel).
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public interface SingleDockableContainer extends DockableContainer {

  /** Returns the dockable this container is displaying */
  public Dockable getDockable();
}
