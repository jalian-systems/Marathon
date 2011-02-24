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


package com.vlsolutions.swing.docking.ws;

import com.vlsolutions.swing.docking.DockingConstants;

/** Defines a key associated to a dockable in a workspace.
 *
 * The internal "key" will have to match the one of a DockKey when the 
 * worspace is applied to a DockingContext.
 *
 * <p> example :
 * if your application uses a dockable with DockKey k = new DockKey("testDockable"); <br>
 * than your corresponding WSDockKey will be wsdk = new WSDockKey("testDockable");
 * <p>
 *  Design note : We use this class insted of DockKey because sometimes dockkeys are lazily
 *  created (at dockable creation) and thus aren't already available when specifying a 
 *  workspace.
 *  
 * @author Lilian Chamontin, VLSolutions
 */
public class WSDockKey {
  
  private String key; // must match DockKey#key

  private DockingConstants.Hide autoHideBorder;
  
  public WSDockKey(String key) {
    this.key = key;
  }
  
  public String toString(){
    return key;
  }
  
  /** returns this object's hashcode */
  public int hashCode(){
    return key.hashCode();
  }
  
  public boolean equals(Object o){
    if (o instanceof WSDockKey){
      return key.equals(((WSDockKey)o).key);
    }
    return false;
  }

  /** returns the inner key of this object (which must match a DockKey's 'key' instance variable) */
  public String getKey() {
    return key;
  }
  
  /** Returns the autohide border of this dockable, or null if not set*/
  public DockingConstants.Hide getAutoHideBorder(){
    return autoHideBorder;
  }
  
  /** Updates the autohide border property */
  public void setAutoHideBorder(DockingConstants.Hide border){
    this.autoHideBorder = border;
  }
  
}
