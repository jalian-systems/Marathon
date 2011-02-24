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

import javax.swing.*;
import java.awt.*;

/** A basic implementation of Dockable.
 * <p>
 * It's a JPanel with a BorderLayout, and it can display a single Dockable.
 * <p>
 * This class is provided as an intermediary between a User Component and the docking
 * desktop (for example, for application that don't want to be too tied with the
 * DockingFramework).
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class DockablePanel extends JPanel implements Dockable{

  private DockKey key;

  public DockablePanel() {
    setLayout(new BorderLayout());
  }

  public DockablePanel(Component contained, DockKey key) {
    setLayout(new BorderLayout());
    add(contained, BorderLayout.CENTER);
    this.key = key;
  }

  public DockKey getDockKey() {
    return key;
  }

  public void setDockKey(DockKey key){
    this.key = key;
  }

  public Component getComponent() {
    return this;
  }

  /** Removes all contained components (normally no more than one), and
   * put this <code>comp</code> on the center area of the BorderLayout.
   *  */
  public void setComponent(Component comp) {
    removeAll();
    add(comp, BorderLayout.CENTER);
  }

}
