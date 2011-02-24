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

/** Constants used by the DockingDesktop to position Dockables.
 * <p>
 * API Users will use these constants to interact with the DockingDesktop.
 * <p>
 * These constants are defined as enumerations of Hide and Split classes, in order
 * to avoid runtime errors.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class  DockingConstants {

  public static final int INT_SPLIT_TOP = 0;
  public static final int INT_SPLIT_LEFT = 1;
  public static final int INT_SPLIT_BOTTOM = 2;
  public static final int INT_SPLIT_RIGHT = 3;

  /** A constant describing a VERTICAL splitting operation where the new component
   * goes on the TOP part */
  public static final Split SPLIT_TOP = new Split(INT_SPLIT_TOP);

  /** A constant describing an HORIZONTAL splitting operation where the new component
   * goes on the LEFT part */
  public static final Split SPLIT_LEFT = new Split(INT_SPLIT_LEFT);

  /** A constant describing a VERTICAL splitting operation where the new component
   * goes on the BOTTOM part */
  public static final Split SPLIT_BOTTOM = new Split(INT_SPLIT_BOTTOM);

  /** A constant describing an HORIZONTAL splitting operation where the new component
   * goes on the RIGHT part */
  public static final Split SPLIT_RIGHT = new Split(INT_SPLIT_RIGHT);

  public static final int INT_HIDE_TOP = 0;
  public static final int INT_HIDE_LEFT = 1;
  public static final int INT_HIDE_BOTTOM = 2;
  public static final int INT_HIDE_RIGHT = 3;

  /** A constant describing an auto-hide operation where the component
   * goes on the TOP border */
  public static final Hide HIDE_TOP = new Hide(INT_HIDE_TOP);

  /** A constant describing an auto-hide operation where the component
   * goes on the LEFT border */
  public static final Hide HIDE_LEFT = new Hide(INT_HIDE_LEFT);
  /** A constant describing an auto-hide operation where the component
   * goes on the BOTTOM border */
  public static final Hide HIDE_BOTTOM = new Hide(INT_HIDE_BOTTOM);
  /** A constant describing an auto-hide operation where the component
   * goes on the RIGHT border */
  public static final Hide HIDE_RIGHT = new Hide(INT_HIDE_RIGHT);


  /** Typesafe enumeration describing a split operation on the DockingDesktop.*/
 public static class Split {
   private int value;
   private Split(int value){
     this.value = value;
   }
   public int value(){
     return value;
   }
 }

 /** Typesafe enumeration describing an auto-hide  operation on the DockingDesktop.*/
 public static class Hide {
   private int value;
   private Hide(int value){
     this.value = value;
   }
   public int value(){
     return value;
   }
 }


}
