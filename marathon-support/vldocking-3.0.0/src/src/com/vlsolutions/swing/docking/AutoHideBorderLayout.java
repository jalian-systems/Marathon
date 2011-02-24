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
import java.util.*;
import java.awt.*;

/** A Layout Manager for auto-hide borders.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class AutoHideBorderLayout implements LayoutManager2{
  private ArrayList components = new ArrayList();
  private boolean isHorizontal;
  private int gap; // gap in pixels between components

  /** Constructs a new Layout.
   *
   * @param isHorizontal  indicates if the layout will be horizontal or vertical.
   *
   * */
  public AutoHideBorderLayout(boolean isHorizontal) {
    this(isHorizontal, AutoHidePolicy.getPolicy().getDefaultGap());
    /** @todo we do not yet listen to gap property change */
  }

  /** Constructs a new Layout.
   * @param isHorizontal  indicates if the layout will be horizontal or vertical.
   * @param gap gap in pixels between components
   * */
  public AutoHideBorderLayout(boolean isHorizontal, int gap) {
    this.isHorizontal = isHorizontal;
    this.gap = gap;
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
    if (constraints == null){
      components.add(comp);
    } else {
      Integer i = (Integer) constraints;
      components.add(i.intValue(), comp);
    }
  }

  public void removeLayoutComponent(Component comp) {
    components.remove(comp);
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public Dimension minimumLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      int visible = 0;
      for (int i = 0; i < components.size(); i++) {
        Component comp = (Component) components.get(i);
        Dimension d = comp.getPreferredSize();
        if (comp.isVisible()){
          visible ++;
          if (isHorizontal) {
            dim.width += d.width;
            dim.height = Math.max(dim.height, d.height);
          } else {
            dim.width = Math.max(dim.width, d.width);
            dim.height += d.height;
          }
        }
      }
      // add the gap between components
      if (isHorizontal) {
        dim.width += (visible - 1) * gap;
      } else {
        dim.height += (visible - 1) * gap;
      }

      Insets insets = parent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
      return dim;
    }
  }

  public Dimension preferredLayoutSize(Container parent) {
    return minimumLayoutSize(parent);
  }

  /** overridden to adjust position (x(top) = width(left), y(left) = height(top)
   */
  public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      int top = insets.top;
      int left = insets.left;
      if (isHorizontal){
        for (int i = 0; i < components.size(); i++) {
          Component comp = (Component) components.get(i);
          if (comp.isVisible()){
            Dimension d = comp.getPreferredSize();
            comp.setBounds(left, top, d.width, d.height);
            left += d.width + gap;
          }
        }
      } else {
        for (int i = 0; i < components.size(); i++) {
          Component comp = (Component) components.get(i);
          if (comp.isVisible()){
            Dimension d = comp.getPreferredSize();
            comp.setBounds(left, top, d.width, d.height);
            top += d.height + gap;
          }
        }
      }
    }
  }
}


