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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.SwingConstants;


/** The layout of a VLToolBarPanel
 *
 * @author Lilian Chamontin, VLSolutions
 * @update 2006/09/09 Support for LTR and RTL component orientation
 */
public class ToolBarPanelLayout implements LayoutManager2{
  
  private ToolBarPanel panel;
  
  private ArrayList componentInfos = new ArrayList();
  
  private int gap = 3; // gap in pixels between components
  
  private int align = FlowLayout.LEADING ;
  
  public ToolBarPanelLayout(ToolBarPanel panel, int align) {
    this.panel = panel ;
    this.align = align ;
  }
  
  public ToolBarPanelLayout(ToolBarPanel panel) {
    this(panel, FlowLayout.LEADING);
  }
  
  /** Returns the alignment of this layout (<code>FlowLayout.LEADING, TRAILING</code>...)
   */
  public int getAlign() {
    return align;
  }
  
  public void setAlign(int align) {
    this.align = align;
  }
  
  
  private int getAlignedX(Container target) {
    int left = 0 ;
    boolean ltr = target.getComponentOrientation().isLeftToRight() ;
    int width = preferredLayoutSize(target).width ;
    switch (align) {
      case FlowLayout.LEFT:
        left += ltr ? 0 : target.getSize().width - width ;
        break ;
      case FlowLayout.CENTER:
        left += (target.getSize().width - width)/2 ;
        break ;
      case FlowLayout.RIGHT:
        left += ltr ? target.getSize().width - width : 0 ;
        break ;
      case FlowLayout.LEADING:
        break ;
      case FlowLayout.TRAILING:
        left += target.getSize().width - width ;
        break ;
    }
    return left ;
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
      throw new NullPointerException();
    } else {
      ToolBarConstraints tc = (ToolBarConstraints) constraints;
      componentInfos.add(new ComponentInfo(comp, tc));
      reorderComponents();
    }
  }
  
  public ToolBarConstraints getConstraints(Component comp){
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo) componentInfos.get(i);
      if (ci.comp == comp){
        return ci.constraints;
      }
    }
    return null;
  }
  
  public void removeLayoutComponent(Component comp) {
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo) componentInfos.get(i);
      if (ci.comp == comp){
        componentInfos.remove(i);
        return ;
      }
    }
  }
  
  public void addLayoutComponent(String name, Component comp) {
  }
  
  public Dimension minimumLayoutSize(Container parent) {
    ToolBarPanel panel = (ToolBarPanel) parent;
    boolean isHorizontal = panel.getOrientation() == SwingConstants.HORIZONTAL;
    synchronized (parent.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      int majorCount = getMajorCount();
      for (int i=0; i < majorCount; i++){
        Dimension minorDim = new Dimension(0,0);
        ArrayList minorList = getMinorComponents(i);
        for (int j=0; j < minorList.size(); j++){
          ComponentInfo ci = (ComponentInfo) minorList.get(j);
          Dimension d = ci.comp.getPreferredSize();
          if (ci.comp.isVisible()){
            if (isHorizontal) {
              minorDim.width += d.width;
              minorDim.height = Math.max(minorDim.height, d.height);
              if (j > 0){
                minorDim.width += gap;
              }
            } else {
              minorDim.width = Math.max(minorDim.width, d.width);
              minorDim.height += d.height;
              if (j > 0){
                minorDim.height += gap;
              }
            }
          }
        }
        if (isHorizontal){
          dim.width = Math.max(dim.width, minorDim.width);
          dim.height += minorDim.height;
        } else {
          dim.width += minorDim.width;
          dim.height = Math.max(dim.height, minorDim.height);
        }
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
  
  /** Returns and ordered array of the contained components */
  public Component [] getComponents(){
    Component [] comps = new Component[componentInfos.size()];
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      comps[i] = ci.comp;
    }
    return comps;
  }
  
  
  public void layoutContainer(Container target) {
    ToolBarPanel panel = (ToolBarPanel) target;
    boolean isHorizontal = panel.getOrientation() == SwingConstants.HORIZONTAL;
    boolean ltr = target.getComponentOrientation().isLeftToRight();
    synchronized (target.getTreeLock()) {
      Collections.sort(componentInfos);
      
      Insets insets = target.getInsets();
      if (isHorizontal){
        int top = insets.top;
        int majorCount = getMajorCount();
        for (int i=0; i < majorCount; i++){
          int left = insets.left + getAlignedX(target);
          int maxHeight = 0;
          ArrayList minorList = getMinorComponents(i);
          
          /*for (int j=0; j < minorList.size(); j++){
            ComponentInfo ci = (ComponentInfo) minorList.get(j);
            Component comp = ci.comp;
            if (comp.isVisible()){
              Dimension d = comp.getPreferredSize();
              comp.setBounds(left, top, d.width, d.height);
              left += d.width + gap;
              maxHeight = Math.max(maxHeight, d.height);
            }
          }*/
          if (ltr) {
            for (int j=0; j < minorList.size(); j++){
              ComponentInfo ci = (ComponentInfo) minorList.get(j);
              Dimension d = layoutHorizMinor(ci, left, top);
              if (d != null) {
                left += d.width + gap ;
                maxHeight = Math.max(maxHeight, d.height);
              }
            }
          } else {
            for (int j=minorList.size()-1; j >= 0; j--){
              ComponentInfo ci = (ComponentInfo) minorList.get(j);
              Dimension d = layoutHorizMinor(ci, left, top);
              if (d != null) {
                left += d.width + gap ;
                maxHeight = Math.max(maxHeight, d.height);
              }
            }
          }
          top += maxHeight;
        }
      } else {
        int majorCount = getMajorCount();
        /*int left = insets.left;
        for (int i=0; i < majorCount; i++){
          int top = insets.top;
          int maxWidth = 0;
          ArrayList minorList = getMinorComponents(i);
          for (int j=0; j < minorList.size(); j++){
            ComponentInfo ci = (ComponentInfo) minorList.get(j);
            Component comp = ci.comp;
            if (comp.isVisible()){
              Dimension d = comp.getPreferredSize();
              comp.setBounds(left, top, d.width, d.height);
              top += d.height + gap;
              maxWidth = Math.max(maxWidth, d.width);
            }
          }
          left += maxWidth;*/
        int left = insets.left;
        if (ltr) {
          for (int i=0; i < majorCount; i++){
            int maxWidth = layoutVertMajor(insets, left, i);
            left += maxWidth;
          }
        } else {
          for (int i=majorCount-1; i >= 0; i--){
            int maxWidth = layoutVertMajor(insets, left, i);
            left += maxWidth;
          }
        }
      }
    }
  }
  
  
  private Dimension layoutHorizMinor(ComponentInfo ci, int left, int top) {
    Component comp = ci.comp ;
    if (comp.isVisible()) {
      Dimension d = comp.getPreferredSize();
      comp.setBounds(left, top, d.width, d.height);
      return d ;
    }
    return null ;
  }
  
  private int layoutVertMajor(Insets insets, int left, int i) {
    int top = insets.top;
    int maxWidth = 0;
    ArrayList minorList = getMinorComponents(i);
    for (int j=0; j < minorList.size(); j++){
      ComponentInfo ci = (ComponentInfo) minorList.get(j);
      Component comp = ci.comp;
      if (comp.isVisible()){
        Dimension d = comp.getPreferredSize();
        comp.setBounds(left, top, d.width, d.height);
        top += d.height + gap;
        maxWidth = Math.max(maxWidth, d.width);
      }
    }
    return maxWidth;
  }
  
  
  private void reorderComponents(){
    Collections.sort(componentInfos);
    /* Usefull state dumps : I'll let them here for some time.
    System.out.println("---------------------------------------------------");
    System.out.println("List of components : ----BEFORE ------");
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      System.out.println(ci.constraints + " = " + ci.comp);
    }
    //*/
    // first, insert new major orders
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      if (ci.constraints.majorOffset < 0){
        ci.constraints.majorOffset = 0;
        for (int j=i+1; j < componentInfos.size(); j++){
          ComponentInfo next = (ComponentInfo)componentInfos.get(j);
          next.constraints.majorOrder ++;
        }
      } else if (ci.constraints.majorOffset > 0){
        ci.constraints.majorOffset = 0;
        for (int j=i; j < componentInfos.size(); j++){ // also increase the current one
          ComponentInfo next = (ComponentInfo)componentInfos.get(j);
          next.constraints.majorOrder ++;
        }
      }
    }
    
    // next, insert new minor orders
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      if (ci.constraints.minorOffset < 0){
        ci.constraints.minorOffset = 0;
        for (int j=i+1; j < componentInfos.size(); j++){
          ComponentInfo next = (ComponentInfo)componentInfos.get(j);
          if (next.constraints.majorOrder == ci.constraints.majorOrder){
            next.constraints.minorOrder ++;
          }
        }
      } else if (ci.constraints.minorOffset > 0){
        ci.constraints.minorOffset = 0;
        for (int j=i; j < componentInfos.size(); j++){ // also increase the current one
          ComponentInfo next = (ComponentInfo)componentInfos.get(j);
          if (next.constraints.majorOrder == ci.constraints.majorOrder){
            next.constraints.minorOrder ++;
          }
        }
      }
    }
    
/*    System.out.println("List of components : after offsets");
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      System.out.println(ci.constraints + " = " + ci.comp);
    }*/
    
    // next, let all major and minor order be consecutive
    int order = 0;
    int minorOrder = 0;
    int lastMajorOrder = -1;
    int lastMinorOrder = -1;
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      if (lastMajorOrder == -1){
        lastMajorOrder = ci.constraints.majorOrder;
        lastMinorOrder = ci.constraints.minorOrder;
      }
      if (ci.constraints.majorOrder == lastMajorOrder){
        ci.constraints.majorOrder = order;
        if (ci.constraints.minorOrder == lastMinorOrder){
          ci.constraints.minorOrder = minorOrder;
        } else {
          minorOrder ++;
          lastMinorOrder = ci.constraints.minorOrder;
          ci.constraints.minorOrder = minorOrder;
        }
      } else { // beginning of a new major
        order ++;
        minorOrder = 0;
        lastMajorOrder = ci.constraints.majorOrder;
        lastMinorOrder = ci.constraints.minorOrder;
        ci.constraints.majorOrder = order;
        ci.constraints.minorOrder = minorOrder;
      }
    }
    
/*    System.out.println("List of components : ------AFTER ----");
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo)componentInfos.get(i);
      System.out.println(ci.constraints + " = " + ci.comp);
    }
 */
    
  }
  
  private ToolBarConstraints getHorizontalInsertionContraintsAt(Component draggedComponent, Point p){
    ToolBarConstraints tc = new ToolBarConstraints();
    int majorCount = getMajorCount();
    for (int i=0; i < majorCount; i++){
      Rectangle bounds = getMinorBounds(i);
      if (p.y < bounds.y){ // insert before first
        tc.majorOrder = i;
        tc.minorOrder = 0;
        tc.majorOffset = -1;
        return tc;
      } else if (p.y < bounds.y + 5){ // insert before current row
        tc.majorOrder = i;
        tc.minorOrder = 0;
        tc.majorOffset = -1;
        return tc;
      } else if (p.y > bounds.y + bounds.height){ // insert after.. ignore (will be tested on next loop)
        
      } else if (p.y > bounds.y + bounds.height - 5){ // insert a new major as we are just below the lower part
        tc.majorOrder = i;
        tc.minorOrder = 0;
        tc.majorOffset = +1;
        return tc;
      } else if (p.x < bounds.x + bounds.width){ // insert inside
        ArrayList minorList = getMinorComponents(i);
        for (int j=0; j < minorList.size(); j++){
          ComponentInfo ci = (ComponentInfo) minorList.get(j);
          Component comp = ci.comp;
          if (comp.isVisible()){
            if (p.x < comp.getX() + comp.getWidth()){
              if (comp == draggedComponent){
                return null;
              } else if (p.x < comp.getX() + 10){ // at the beginning of the component
                tc.majorOrder = i;
                tc.minorOrder = j;
                tc.minorOffset = -1;
                return tc;
              } else { // on the component but not at the beginning
                if (j < minorList.size()-1){
                  ComponentInfo next = (ComponentInfo)minorList.get(j+1);
                  if (next.comp == draggedComponent){
                    return null;
                  }
                }
                tc.majorOrder = i;
                tc.minorOrder = j;
                tc.minorOffset = +1;
                return tc;
              }
            }
          }
        }
        // not possible to reach this point
      } else { // after last of the row
        ArrayList minorList = getMinorComponents(i);
        tc.majorOrder = i;
        tc.minorOrder = minorList.size();
        return tc;
      }
    }
    // not found... so it's after the last
    tc.majorOrder = majorCount;
    tc.minorOrder = 0;
    return tc;
  }
  
  private ToolBarConstraints getVerticalInsertionContraintsAt(Component draggedComponent, Point p){
    ToolBarConstraints tc = new ToolBarConstraints();
    int majorCount = getMajorCount();
    for (int i=0; i < majorCount; i++){
      Rectangle bounds = getMinorBounds(i);
      if (p.x < bounds.x){ // insert before first
        tc.majorOrder = i;
        tc.minorOrder = 0;
        tc.majorOffset = -1;
        return tc;
      } else if (p.x < bounds.x + 3){ // insert before current row
        tc.majorOrder = i;
        tc.minorOrder = 0;
        tc.majorOffset = -1;
        return tc;
      } else if (p.x > bounds.x + bounds.width){ // insert after.. ignore (will be tested on next loop)
        
      } else if (p.x > bounds.x + bounds.width - 3){ // insert a new major as we are just below the lower part
        tc.majorOrder = i;
        tc.minorOrder = 0;
        tc.majorOffset = +1;
        return tc;
      } else if (p.y < bounds.y + bounds.height){ // insert inside
        ArrayList minorList = getMinorComponents(i);
        for (int j=0; j < minorList.size(); j++){
          ComponentInfo ci = (ComponentInfo) minorList.get(j);
          Component comp = ci.comp;
          if (comp.isVisible()){
            if (p.y < comp.getY() + comp.getHeight()){
              if (comp == draggedComponent){
                return null;
              } else if (p.y < comp.getY() + 10){ // at the beginning of the component
                tc.majorOrder = i;
                tc.minorOrder = j;
                tc.minorOffset = -1;
                return tc;
              } else { // on the component but not at the beginning
                if (j < minorList.size()-1){
                  ComponentInfo next = (ComponentInfo)minorList.get(j+1);
                  if (next.comp == draggedComponent){
                    return null;
                  }
                }
                tc.majorOrder = i;
                tc.minorOrder = j;
                tc.minorOffset = +1;
                return tc;
              }
            }
          }
        }
        // not possible to reach this point
      } else { // after last of the row
        ArrayList minorList = getMinorComponents(i);
        tc.majorOrder = i;
        tc.minorOrder = minorList.size();
        return tc;
      }
    }
    // not found... so it's after the last
    tc.majorOrder = majorCount;
    tc.minorOrder = 0;
    return tc;
  }
  
  
  /** Returns a proposed TooBarContraints for a given insertion point */
  public ToolBarConstraints getInsertionContraintsAt(Component draggedComponent, Point p){
    boolean isHorizontal = panel.getOrientation() == SwingConstants.HORIZONTAL;
    if (isHorizontal){
      return getHorizontalInsertionContraintsAt(draggedComponent, p);
    } else { // vertical
      return getVerticalInsertionContraintsAt(draggedComponent, p);
    }
  }
  
  
  private int getMajorCount(){
    int max = 0;
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo) componentInfos.get(i);
      ToolBarConstraints tc = ci.constraints;
      if (tc.majorOrder > max){
        max = tc.majorOrder;
      }
    }
    return max+1;
  }
  
  private Rectangle getMinorBounds(int majorOrder){
    Rectangle  r = new Rectangle();
    Rectangle temp = null;
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo) componentInfos.get(i);
      if (ci.constraints.majorOrder == majorOrder){
        temp = ci.comp.getBounds(temp);
        r = r.union(temp);
      }
    }
    return r;
  }
  
  private ArrayList getMinorComponents(int majorOrder){
    ArrayList list = new ArrayList();
    for (int i=0; i < componentInfos.size(); i++){
      ComponentInfo ci = (ComponentInfo) componentInfos.get(i);
      if (ci.constraints.majorOrder == majorOrder){
        list.add(ci);
      }
    }
    return list;
  }
  
  private class ComponentInfo implements Comparable{
    
    Component comp;
    ToolBarConstraints constraints;
    ComponentInfo(Component comp, ToolBarConstraints constraints){
      this.comp = comp;
      this.constraints = constraints;
    }
    
    private int getCompareOrder(){
      int order = constraints.majorOrder * 100000 + constraints.majorOffset * 10000
          + constraints.minorOrder * 100 + constraints.minorOffset;
      return order;
    }
    
    public int compareTo(Object obj) {
      ComponentInfo ci = (ComponentInfo) obj;
      return getCompareOrder() - ci.getCompareOrder();
    }
  }
  
}
