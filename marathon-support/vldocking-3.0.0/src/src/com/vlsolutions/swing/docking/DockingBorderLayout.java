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
import java.util.Hashtable;
import java.awt.*;
import javax.swing.*;

/** A layout adapted to autohide panels.
 * <p> 
 * This layout is like a BorderLayout but corners are always empty
 * (for example, the TOP component starts with an x coordinate equal to the
 * width of the LEFT component).
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class DockingBorderLayout implements LayoutManager2{
  Component topComp, leftComp, bottomComp, rightComp, centerComp;

  public DockingBorderLayout() {
  }


  public float getLayoutAlignmentX(Container target) {
    return 0.5F;
  }

  public float getLayoutAlignmentY(Container target) {
    return 0.5F;
  }

  public void invalidateLayout(Container target) {
  }

  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public void addLayoutComponent(Component comp, Object constraints) {
    addLayoutComponent((String)constraints, comp);
  }

  public void removeLayoutComponent(Component comp) {
  }

  public void addLayoutComponent(String name, Component comp) {
    if (name.equals(BorderLayout.NORTH)){
      topComp = comp;
    } else if (name.equals(BorderLayout.EAST)){
      rightComp = comp;
    } else if (name.equals(BorderLayout.SOUTH)){
      bottomComp = comp;
    } else if (name.equals(BorderLayout.WEST)){
      leftComp = comp;
    } else if (name.equals(BorderLayout.CENTER)){
      centerComp = comp;
    } else {
      throw new IllegalArgumentException("wrong constraint");
    }
  }

  public Dimension minimumLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);

      if (leftComp != null && leftComp.isVisible()) {
        Dimension d = leftComp.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if (rightComp != null && rightComp.isVisible()) {
        Dimension d = rightComp.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if (centerComp != null && centerComp.isVisible()) {
        Dimension d = centerComp.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if (topComp != null && topComp.isVisible()) {
        Dimension d = topComp.getMinimumSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height;
      }
      if (bottomComp != null && bottomComp.isVisible()) {
        Dimension d = bottomComp.getMinimumSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height;
      }

      Insets insets = parent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

  public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);

      if (leftComp != null && leftComp.isVisible()) {
        Dimension d = leftComp.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if (rightComp != null && rightComp.isVisible()) {
        Dimension d = rightComp.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if (centerComp != null && centerComp.isVisible()) {
        Dimension d = centerComp.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if (topComp != null && topComp.isVisible()) {
        Dimension d = topComp.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height;
      }
      if (bottomComp != null && bottomComp.isVisible()) {
        Dimension d = bottomComp.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height;
      }

      Insets insets = parent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }

  }

  private int getSideWidth(Component comp){
    if (comp == null) return 0;
    if (comp.isVisible()) return comp.getPreferredSize().width;
    return 0;
  }
  private int getSideHeight(Component comp){
    if (comp == null) return 0;
    if (comp.isVisible()) return comp.getPreferredSize().height;
    return 0;
  }

  /** overridden to adjust position (x(top) = width(left), y(left) = height(top)
   */
  public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      int top = insets.top;
      int bottom = target.getHeight() - insets.bottom;
      int left = insets.left;
      int right = target.getWidth() - insets.right;
      int lw = getSideWidth(leftComp);
      int rw = getSideWidth(rightComp);
      int th = getSideHeight(topComp);
      int bh = getSideHeight(bottomComp);

      if (topComp != null && topComp.isVisible()) {
        topComp.setSize(right - left - lw - rw, topComp.getHeight());
        Dimension d = topComp.getPreferredSize();
        topComp.setBounds(left + lw, top,
            right - left - lw - rw, d.height);
      }

      if (bottomComp != null && bottomComp.isVisible()) {
        bottomComp.setSize(right - left - lw - rw, bottomComp.getHeight());
        Dimension d = bottomComp.getPreferredSize();
        bottomComp.setBounds(left + lw, bottom - d.height,
            right - left - lw - rw , d.height);
      }

      if (rightComp != null && rightComp.isVisible()) {
        rightComp.setSize(rightComp.getWidth(), bottom - top - th - bh);
        Dimension d = rightComp.getPreferredSize();
        rightComp.setBounds(right - d.width,
            top + th, d.width, bottom - top - th - bh);
      }

      if (leftComp != null && leftComp.isVisible()) {
        leftComp.setSize(leftComp.getWidth(), bottom - top);
        Dimension d = leftComp.getPreferredSize();
        leftComp.setBounds(left,
            top + th, d.width, bottom - top - th - bh);
      }
      if (centerComp != null && centerComp.isVisible()) {
        centerComp.setBounds(left + lw, top + th,
            right - left-lw - rw,
            bottom - top - th - bh);
      }
    }
  }


}
