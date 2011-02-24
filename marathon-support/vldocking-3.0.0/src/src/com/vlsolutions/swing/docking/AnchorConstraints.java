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

/** A constraint used to specify how a dockable is tied (anchored) to its containing parent .
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1
 */
public class AnchorConstraints {
  /** Field describing a dockable anchored to the TOP border of its ancestor container */
    
  public static final int ANCHOR_TOP = 1;

  /** Field describing a dockable anchored to the LEFT border of its ancestor container */
  public static final int ANCHOR_LEFT = 2;
  
  /** Field describing a dockable anchored to the BOTTOM border of its ancestor container */
  public static final int ANCHOR_BOTTOM = 4;
  
  /** Field describing a dockable anchored to the RIGHT border of its ancestor container */
  public static final int ANCHOR_RIGHT = 8;
  
  
  private int anchor;
      
  /** Constructor for an immutable contraints 
   *
   *@param anchor    value expressed as a bitwise OR between ANCHOR_ fields (for example ANCHOR_TOP|ANCHOR_LEFT) 
   *
   */
  public AnchorConstraints(int anchor) {
    this.anchor = anchor;
  }

  /** Returns the anchor value of this contraints object.
   * <p>
   *  Anchor is expressed as a bitwise OR between ANCHOR_ fields (for example ANCHOR_TOP|ANCHOR_LEFT) 
   */
  public int getAnchor() {
    return anchor;
  }

}
