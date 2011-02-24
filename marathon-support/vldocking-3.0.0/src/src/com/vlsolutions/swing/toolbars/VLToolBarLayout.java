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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;

/** The layout for VLToolBar (supports horizontal and vertical orientation).
 *<p>
 * When used in horizontal orientation, this layout respects the preferred width of
 * the contained components, and forces the height to the maximum of all the preferred heights.
 * (so components are vertically streched to have the same height).
 *<p>
 * When used in vertical orientation, this layout respects the preferred height of
 * the contained components, and forces the width to the maximum of all the preferred widths.
 * (so components are horizontally streched to have the same width).
 *
 * @author Lilian Chamontin, VLSolutions
 * @update 2006/09/09 Support for LTR and RTL component orientation
 */
public class VLToolBarLayout implements LayoutManager2{
  
  private ArrayList components = new ArrayList();
  private boolean isHorizontal;
  private int gap; // gap in pixels between components
  
  public VLToolBarLayout() {
    this(true, 0);
  }
  
  
  /** Constructs a new Layout.
   *
   * @param isHorizontal  indicates if the layout will be horizontal or vertical.
   *
   * */
  public VLToolBarLayout(boolean isHorizontal) {
    this(isHorizontal, 0);
  }
  
  /** Constructs a new Layout.
   * @param isHorizontal  indicates if the layout will be horizontal or vertical.
   * @param gap gap in pixels between components
   * */
  public VLToolBarLayout(boolean isHorizontal, int gap) {
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
      Dimension toolbarDim = preferredLayoutSize(target);
      Insets insets = target.getInsets();
      if (isHorizontal){
        int left = insets.left;
        /*for (int i = 0; i < components.size(); i++) {
          Component comp = (Component) components.get(i);
          if (comp.isVisible()){
            Dimension d = comp.getPreferredSize();
            comp.setBounds(left, insets.top, d.width, toolbarDim.height - insets.top - insets.bottom);
            left += d.width + gap;
          }
        }*/
        if (target.getComponentOrientation().isLeftToRight()) {
          for (int i = 0; i < components.size(); i++) {
            left = layoutHorizComponent((Component)components.get(i), toolbarDim, insets, left);
          }
        } else {
          // Right to left
          Component firstComp = components.size() == 0 ? null : (Component)components.get(0);
          int endWith = 0 ;
          if (firstComp != null && firstComp instanceof ToolBarGripper) {
            left = layoutHorizComponent(firstComp, toolbarDim, insets, left);
            endWith = 1;
          }
          for (int i = components.size() - 1; i >= endWith; i--) {
            left = layoutHorizComponent((Component)components.get(i), toolbarDim, insets, left);
          }
        }
      } else { //vertical
        int centerX = insets.left + (toolbarDim.width - insets.left - insets.right)/2;
        int top = insets.top;
        for (int i = 0; i < components.size(); i++) {
          Component comp = (Component) components.get(i);
          if (comp.isVisible()){
            Dimension d = comp.getPreferredSize();
            comp.setBounds(insets.left, top, toolbarDim.width - insets.left - insets.right, d.height);
            top += d.height + gap;
          }
        }
      }
    }
  }
  
  
  /* 2006/09/09 */
  private int layoutHorizComponent(Component comp, Dimension toolbarDim, Insets insets, int left) {
    if (comp.isVisible()){
      Dimension d = comp.getPreferredSize();
      comp.setBounds(left, insets.top, d.width, toolbarDim.height - insets.top - insets.bottom);
      left += d.width + gap;
    }
    return left;
  }
  
  
}


