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


/** This class is an immutable object used to remember the relative positionning
 * of docked components.
 * <p>
 * <code>x, y, w, h</code> fields are expressed with <code>double</code> values
 * varying from 0 to 1.
 * <p>
 * <ul>
 *  <li> x : 0 = left, 1 = right
 *  <li> y : 0 = top, 1 = bottom
 *  <li> w : 0 = no width, 1-x = full remaining width
 *  <li> h : 0 = no height, 1-y = full remaining height
 * </ul>
 *
 * <p>
 * Objects of this class are used to reposition a Dockable on the desktop, based
 * on lazy constraints : As the desktop is a mix of horizontal and vertical
 * split panes ({@link SplitContainer}) it is not always possible to put a dockable
 * exactly where you want given a set of x,y, w, and h constraints.
 *
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class RelativeDockablePosition {

  /** convenience field referencing the TOP border of the DockingDesktop */
  public static final RelativeDockablePosition TOP = new RelativeDockablePosition(0,0,1, 0.2);

  /** convenience field referencing the CENTER part of TOP border of the DockingDesktop */
  public static final RelativeDockablePosition TOP_CENTER = new RelativeDockablePosition(0.4,0,0.2, 0.2);

  /** convenience field referencing the TOP-RIGHT corner of the DockingDesktop */
  public static final RelativeDockablePosition TOP_RIGHT= new RelativeDockablePosition(0,0,0.2, 0.2);

  /** convenience field referencing the TOP-LEFT corner of the DockingDesktop */
  public static final RelativeDockablePosition TOP_LEFT = new RelativeDockablePosition(0.8,0,0.2, 0.2);

  /** convenience field referencing the RIGHT border of the DockingDesktop */
  public static final RelativeDockablePosition RIGHT = new RelativeDockablePosition(0.8,0,0.2, 1);

  /** convenience field referencing the CENTER part of the RIGHT border of the DockingDesktop */
  public static final RelativeDockablePosition RIGHT_CENTER = new RelativeDockablePosition(0.8,0.4,0.2, 0.2);

  /** convenience field referencing the TOP-RIGHT corner of the DockingDesktop */
  public static final RelativeDockablePosition BOTTOM_RIGHT = new RelativeDockablePosition(0.8,0.8,0.2, 0.2);

  /** convenience field referencing the BOTTOM border of the DockingDesktop */
  public static final RelativeDockablePosition BOTTOM = new RelativeDockablePosition(0,0.8,1, 0.2);

  /** convenience field referencing the CENTER part of the BOTTOM border of the DockingDesktop */
  public static final RelativeDockablePosition BOTTOM_CENTER = new RelativeDockablePosition(0.4,0.8,0.2, 0.2);

  /** convenience field referencing the TOP-RIGHT corner of the DockingDesktop */
  public static final RelativeDockablePosition BOTTOM_LEFT = new RelativeDockablePosition(0,0.8,0.2, 0.2);

  /** convenience field referencing the LEFT border of the DockingDesktop */
  public static final RelativeDockablePosition LEFT = new RelativeDockablePosition(0,0,0.2, 1);

  /** convenience field referencing the CENTER part of the LEFT border of the DockingDesktop */
  public static final RelativeDockablePosition LEFT_CENTER = new RelativeDockablePosition(0,0.4,0.2, 0.2);

  private double x, y, w; // relative positionning (0-1) of the component in the container

  /** The container used for relative positioning (can be a top level DockingPanel 
   * or an intermediate CompoundDockingPanel) */
  private Container relativeAncestorContainer;

  private double h;
  
  /** @see AnchorConstraints.ANCHOR_TOP (this field is an ORing of possible anchors) and contains
   * 4 boolean values (TOP, LEFT, BOTTOM, RIGHT)
   */
  private int anchors;
  

  /** Construct a RelativeDockablePosition, assigning x, y, w, h values from the
   * current position of a dockable in its desktop container.
   * <p>
   * This constructor is a convenience for the framework, and should not be
   * used by API users (thus is package protected visibility).
   * */
  RelativeDockablePosition(Container relativeAncestorContainer, Dockable dockable) {
    /* (package protected) */
    
    resetRelativePosition(relativeAncestorContainer, dockable);
    
  }

  /** Constructs an empty relative position (all fields are initialized with 0.0 values) */
  public RelativeDockablePosition() {

  }

  /** Constructs a relative position with given location and size.
   *<p>
   * Please remember that the values must be between 0 and 1, as they designate
   * relative coordinates position/size.
   *
   * @throws IllegalArgumentException if values are not in the given bounds
   *  */
  public RelativeDockablePosition(double x, double y, double w, double h) {
    this (null, x, y, w, h);    
  }
  
  /** Constructs a relative position with a given location and size, relative to a container */
  public RelativeDockablePosition(Container relativeAncestorContainer, double x, double y, double w, double h) {
    this.relativeAncestorContainer = relativeAncestorContainer;
    if (x < 0 || x > 1){
      throw new IllegalArgumentException("x is out of bounds [0.0 , 1.0] " + x);
    }
    if (y < 0 || y > 1){
      throw new IllegalArgumentException("y is out of bounds [0.0 , 1.0] " + y);
    }
    if (w < 0 || w > 1){
      throw new IllegalArgumentException("w is out of bounds [0.0 , 1.0] " + w);
    }
    if (h < 0 || h > 1){
      throw new IllegalArgumentException("h is out of bounds [0.0 , 1.0] " + h);
    }
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }


  /** Returns the relative x position of the dockable */
  public double getX(){
    return x;
  }

  /** Returns the relative y position of the dockable */
  public double getY(){
    return y;
  }

  /** Returns the relative width of the dockable */
  public double getWidth(){
    return w;
  }

  /** Returns the relative height of the dockable */
  public double getHeight(){
    return h;
  }

  /** Returns a meaningfull string representation of this RelativeDockablePosition  */
  public String toString(){
    return "RelativePosition [" + x+", " + y +", " + w + ", " + h + ", anchors=" + anchors+"]";
  }
  
  /** Returns the ancestor container used as a reference for the relative potionning 
   *<p> This can be a DockingPanel (top level ancestor) or CompoundDockingPanel (intermediate ancestor)
   */
  public Container getRelativeAncestorContainer(){
    return relativeAncestorContainer;
  }
  
  /** Returns an integer value representing the anchoring of the dockable relatively to 
   * its ancestor container.
   * <p>
   * For example a dockable at the top left angle of an ancestor container will have an 
   * "anchors" value or : AnchorConstraints.ANCHOR_TOP|AnchorConstraints.ANCHOR_LEFT
   * @see AnchorConstraints
   */
  public int getAnchors(){
    return anchors;
  }

  /** Updates the current relative ancestor container and anchor positionning */
  public void resetRelativePosition(Container relativeAncestorContainer, Dockable dockable) {
    this.relativeAncestorContainer = relativeAncestorContainer;

    DockableContainer dockableContainer = DockingUtilities.findSingleDockableContainer(dockable);
    if (dockableContainer != null && relativeAncestorContainer != null){
      Component c = (Component) dockableContainer;
      Rectangle converted = SwingUtilities.convertRectangle(c,
          new Rectangle(0,0, c.getWidth(), c.getHeight()), relativeAncestorContainer);
      x = converted.x / (float)relativeAncestorContainer.getWidth();
      y = converted.y / (float)relativeAncestorContainer.getHeight();
      w = converted.width / (float)relativeAncestorContainer.getWidth();
      h = converted.height / (float)relativeAncestorContainer.getHeight();      
  
      // also set boolean anchors
      this.anchors = RelativeDockingUtilities.findAnchors(dockable.getComponent(), relativeAncestorContainer);
    }    
    
  }

}
