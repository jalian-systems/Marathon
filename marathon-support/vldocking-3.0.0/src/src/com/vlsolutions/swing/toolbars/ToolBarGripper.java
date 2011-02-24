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


package com.vlsolutions.swing.toolbars;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


/** The component used to have a "grip" action on a toolbar (Drag and Drop support)
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.0
 */
public class ToolBarGripper extends JComponent {
  private static final String uiClassID = "ToolBarGripperUI";
  
  /** Id for the orientation property */
  public static final String PROPERTY_ORIENTATION = "ORIENTATION";
  
  private int orientation = SwingConstants.HORIZONTAL;
  
  private boolean collapsible = true;
  private boolean collapsed = false;
  
  public ToolBarGripper() {
    this(SwingConstants.HORIZONTAL);
  }
  
  public ToolBarGripper(int orientation) {
    setOrientation(orientation);
    updateUI();
  }

  public void setOrientation(int orientation){
    int old = this.orientation;
    this.orientation = orientation;
    firePropertyChange(PROPERTY_ORIENTATION, old, orientation); // not used by the framwork, but might be usefull for someone
  }
  
  /** Returns the orientation of this gripper (SwingConstants.HORIZONTAL/VERTICAL) */
  public int getOrientation(){
    return this.orientation;
  }
  
  /**
   * Resets the UI property to a value from the current look and feel.
   *
   * @see JComponent#updateUI
   */
  public void updateUI() {
      setUI(UIManager.getUI(this));
  }
  
  /** {@inheritDoc}
   * @since 2.0
   */
  public String getUIClassID() {
    return uiClassID;
  }

  /** Return true is this toolbar can be collapsed (default is true)*/
  public boolean isCollapsible() {
    return collapsible;
  }

  public void setCollapsible(boolean collapsible) {
    this.collapsible = collapsible;
  }

  /** Return true is this toolbar is currently collapsed */
  public boolean isCollapsed() {
    return collapsed;
  }

  public void setCollapsed(boolean collapsed) {
    this.collapsed = collapsed;
  }
}
