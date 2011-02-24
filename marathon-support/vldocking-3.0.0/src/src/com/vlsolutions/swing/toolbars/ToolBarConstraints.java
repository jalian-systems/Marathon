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

/** Constraints to be used with the ToolBarLayout.
 *<p>
 * Depending on the orientation of the layout, the two integers majorOrder and minorOrder 
 * express the opposite measures :
 *<ul>
 * <li> in an horizontal layout, majorOrder is the ROW, and minorOrder is the COLUMN
 * <li>in a vertical layout, majorOrder is the COLUMN, and minorOrder is the ROW
 *</ul>
 *<p>
 * Example : To layout two toolbars horizontally on an horizontal layout, use the following constraints 
 * (0,0) and (0,1) (the column is the minor order on an horizontal layout).
 *<p>
 * to layout them in a row (one above the other), use the constraints (0,0) and (1,0) (the row
 * is the major order on an horizontal layout).
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class ToolBarConstraints {
  /** The major order is the row index for horizontal toolbars, and the column index for 
   * vertical toolbars.
   */
  public int majorOrder;
  
  /** The minor order is the column index for horizontal toolbars, and the row index for 
   * vertical toolbars.
   */
  public int minorOrder;

  
  /** Set to -1/0/+1 during drag/drop phase to allow insertion between rows/columns.
   * Do not use it outside drag and drop operations.
   */
  /* package protected */ int majorOffset = 0;

  /** Set to -1/0-31 during drag/drop phase to allow insertion between rows/columns
   * Do not use it outside drag and drop operations.
   */
  /* package protected */ int minorOffset = 0;
      
  /** Constructs a new ToolBarConstraints at (0,0) */
  public ToolBarConstraints() {
  }

  /** Constructs a new ToolBarConstraints at (majorOrder,minorOrder).
   *<p>
   * the majorOrder is the ROW for an horizontal layout, and the columns for a vertical layout.
   */
  public ToolBarConstraints(int majorOrder, int minorOrder) {
    this.majorOrder = majorOrder;
    this.minorOrder = minorOrder;
  }
  
  /** Returns a description of these constraints. */
  public String toString(){
    return "ToolBarConstraints[" + majorOrder + ", " + minorOrder + "]";
  }
  
}
