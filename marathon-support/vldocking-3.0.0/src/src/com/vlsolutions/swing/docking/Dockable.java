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

import java.awt.*;

/** Interface describing a component (AWT/Swing) that can be managed by the
 * DockingDesktop.
 *
 *<p>
 * Objects implementing this interface are usually wrappers around user components,
 * or user components themselves.
 * <ul>
 * <li> a wrapper example : the {@link DockablePanel} is a Dockable, and is used to display a
 * single user Component.
 * <li> a direct implementation example :
 * <pre>
 * class MyComponent extends  JPanel implements Dockable {
 *     // unique key for the docking desktop
 *     DockKey key = new DockKey("MyComponent");
 *
 *     public MyComponent(){
 *         // build your user component here
 *         add(new JButton("Button"));
 *         add(new JTextField("Field"));
 *
 *         // initialize docking properties
 *         key.setName("My Component");
 *         key.setIcon(...);
 *         key.setCloseEnabled(false);
 *     }
 *     public DockKey getDockKey(){
 *         return key;
 *     }
 *     public Component getComponent(){
 *         // this component is the dockable
 *         return this;
 *     }
 * }
 * </pre>
 * </ul>
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public interface Dockable {

  /** returns the unique key identifying the docked element */
  public DockKey getDockKey();

  /** returns the component wrapped.
   *
   *  */
  public Component getComponent();

}
