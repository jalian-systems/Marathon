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

import com.vlsolutions.swing.docking.event.DockingActionAddDockableEvent;
import com.vlsolutions.swing.docking.event.DockingActionEvent;
import com.vlsolutions.swing.docking.event.DockingActionSplitComponentEvent;
import java.awt.*;
import javax.swing.*;

/** This class provides an algorithm for finding (and then inserting) the most suitable place to
 * show a dockable, based on a relative positionning.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * @update 2005/10/10 Lilian Chamontin : improved heuristics of resizing  (best matching of the appropriate SplitContainer)
 * @update 2005/10/10 Lilian Chamontin : improved again heuristics of resizing
 */
class RelativeDockingUtilities {
  /* package class */
  
 /* This class is an extraction of DockingDesktop - we will enhance (and complexify)
  * the algorithm later,
  * and should not mess up with DockingDesktop source code.
  *
  *
  */
  
  private RelativeDockingUtilities() {
  }
  
  /** try to find the best suitable SplitContainer from where insertion will
   * be done.
   * */
  private static SplitContainer findBestContainer(Container ancestorContainer, RelativeDockablePosition position){
    
    // convert relative positionning to current positionning (depending
    // on the current size of dockingPanel)
    int x = (int) (position.getX() * ancestorContainer.getWidth());
    int y = (int) (position.getY() * ancestorContainer.getHeight());
    int w = (int) (position.getWidth() * ancestorContainer.getWidth());
    int h = (int) (position.getHeight() * ancestorContainer.getHeight());
    
    int centerX = x + w / 2;
    int centerY = y + h / 2;
    
    Component centerComp = ancestorContainer.findComponentAt(centerX, centerY);
    // find the splitcontainer containing centerComp,
    // and ensure this split is linked to ancestorContainer by a hierarchy of split containers
    // (to avoid selecting a split inside a sub-container)
    Component splitComp = centerComp;
    boolean found = false;
    while (splitComp != null && splitComp != ancestorContainer && ! found){
      splitComp = splitComp.getParent();
      if (splitComp instanceof SplitContainer){
        // ok we've found a split : now let's check if there's a direct split hierarchy
        // between this one and the ancestor container
        Component up = splitComp.getParent();
        while (up instanceof SplitContainer){
          up = up.getParent();
        }
        if (up == ancestorContainer){
          found = true;
        }
      }
    }
    
    if (splitComp instanceof SplitContainer) { //2006/09/12
      /* Try to find a larger splitcontainer that still fits well
       * with the position.
       * for example :
       *
       * [1|___2_____]
       * [1|[3|[4|5]]]
       *
       * if we remove "2", what remains is [1|[3|[4|5]]]
       * and on restoring, the selected split is [4|5], although [3|[4|5]] would be
       * a much better choice.
       *
       * to do that, we try to find the parent with the best "width" and "height"
       *
       */
      float EPSILON = 0.05f;
      found = true;
      while (found && splitComp.getParent() instanceof SplitContainer){
        found = false;
        float widthRatio =  splitComp.getWidth() / (float) w;
        float heightRatio = splitComp.getHeight() / (float) h;
        
        SplitContainer parentSplit = (SplitContainer) splitComp.getParent();
        float pWidthRatio = parentSplit.getWidth() / (float) w;
        float pHeightRatio = parentSplit.getHeight() / (float) h;
        
        boolean sameOrientation = ((SplitContainer)splitComp).getOrientation() == parentSplit.getOrientation();
        if (sameOrientation){
          if (parentSplit.getOrientation() == SplitContainer.HORIZONTAL_SPLIT){
            // two horizontal, which one has the best width ?
            if (Math.abs(pWidthRatio-1) < Math.abs(widthRatio-1)){
              //parents width is better : we upgrade splitcomp
              splitComp = splitComp.getParent();
              found = true; // loop again
            }
          } else {
            // vertical == same width, is parent height better ?
            if (Math.abs(pHeightRatio-1) < Math.abs(heightRatio-1)){
              //height is better : we upgrade splitcomp
              splitComp = splitComp.getParent();
              found = true; // loop again
            }
          }
        } else { // opposite orientation
          if (parentSplit.getOrientation() == SplitContainer.HORIZONTAL_SPLIT){
            // so split is V and parent is H
            // has parent a better width ratio ?
            if (Math.abs(pWidthRatio-1) < Math.abs(widthRatio-1)){
              //parents width is better : we upgrade splitcomp
              splitComp = splitComp.getParent();
              found = true; // loop again
            }
          } else {
            if (Math.abs(pHeightRatio-1) < Math.abs(heightRatio-1)){
              //height is better : we upgrade splitcomp
              splitComp = splitComp.getParent();
              found = true; // loop again
            }
          }
        }      
      }
      
      
    }
    
    
    
    /*while (splitComp != null && splitComp != ancestorContainer
        && ! (splitComp instanceof SplitContainer)) {
      splitComp = splitComp.getParent();
    }*/
    
    if (splitComp instanceof SplitContainer) {
      // try to enhance the selection when there are global anchors //2005/10/10
      // we're now looking for a split container whose anchors would better match
      // the ones of the dockable position
      
//          boolean anchoredTop = Math.abs( y - dockingPanel.getY()) < 5;
//          boolean anchoredLeft =Math.abs(x - dockingPanel.getX()) < 5;
//          boolean anchoredBottom = Math.abs(y + h - dockingPanel.getY() - dockingPanel.getHeight()) < 5;
//          boolean anchoredRight = Math.abs(x + w - dockingPanel.getX() - dockingPanel.getWidth()) < 5;
      /*boolean anchoredTop = y < 5; //2005/11/08 enhanced !
      boolean anchoredLeft = x < 5;
      boolean anchoredBottom = Math.abs(y + h - ancestorContainer.getHeight()) < 5;
      boolean anchoredRight = Math.abs(x + w - ancestorContainer.getWidth()) < 5;*/
      
      int anchors = position.getAnchors();
      boolean anchoredTop = (anchors & AnchorConstraints.ANCHOR_TOP)>0;
      boolean anchoredLeft = (anchors & AnchorConstraints.ANCHOR_LEFT)>0;
      boolean anchoredBottom = (anchors & AnchorConstraints.ANCHOR_BOTTOM)>0;
      boolean anchoredRight = (anchors & AnchorConstraints.ANCHOR_RIGHT)>0;
      
      
      return findBetterContainer((SplitContainer)splitComp, ancestorContainer, anchoredTop, anchoredLeft, anchoredBottom, anchoredRight);
      
    } else {
      return null;
    }
  }
  
  /** try to find a SplitContainer up in the hierarchy, satisfying the global anchors */
  private static SplitContainer findBetterContainer(SplitContainer split,
      Container dockingPanel, boolean anchoredTop,
      boolean anchoredLeft, boolean anchoredBottom, boolean anchoredRight){
    // we already have found a splitContainer, but it can be subOptimal in some cases
    // for instance when we want to anchor a component at the bottom of the container (full width),
    // and there are two horizontal splits there, we might return the internal split (not full width),
    // and not the external (full width) one.
    // this method will try to find a splitcontainer upper in the hierarchy, with better anchors
      /*System.out.println("global anchors " + anchoredTop + " "
             + anchoredLeft + " " + anchoredBottom + " " + anchoredRight);*/
    boolean [] globalAnchors = {anchoredTop, anchoredLeft, anchoredBottom, anchoredRight};
    int globalCount = 0; // number of anchors to be found (at least)
    for (int i=0; i < 4; i++){
      if (globalAnchors[i]){
        globalCount++;
      }
    }
    
    SplitContainer betterSplit = split;
    boolean [] splitAnchors = new boolean[4];
    while (true){
      int contacts = findAnchors(split, dockingPanel);
      splitAnchors[0] = (contacts & AnchorConstraints.ANCHOR_TOP) > 0;
      splitAnchors[1] = (contacts & AnchorConstraints.ANCHOR_LEFT) > 0;
      splitAnchors[2] = (contacts & AnchorConstraints.ANCHOR_BOTTOM) > 0;
      splitAnchors[3] = (contacts & AnchorConstraints.ANCHOR_RIGHT) > 0;
      int count = 0;
      for (int i=0; i < 4; i++){
        if (globalAnchors[i] && splitAnchors[i]){
          count ++;
        }
      }
      if (count >= globalCount){
        return split;
      } else {
        if (split.getParent() instanceof SplitContainer){
          split = (SplitContainer) split.getParent();
        } else {
          return split; // last split container
        }
      }
    }
  }
  
  /** builds an array used to find anchors of a component relative to its ancestor container */
/*  private static boolean [] findAnchors(Component comp, Container container){
    Rectangle r = SwingUtilities.convertRectangle(comp,
        new Rectangle(0, 0, comp.getWidth(), comp.getHeight()),
        container);
    int x = container.getX();
    int y = container.getY();
    int w = container.getWidth();
    int h = container.getHeight();
    boolean [] anchors = new boolean [4];
    anchors[0] = Math.abs( y - r.y) < 5;
    anchors[1] = Math.abs(x - r.x) < 5;
    anchors[2] = Math.abs(y + h - r.y - r.height) < 30;
    anchors[3] = Math.abs(x + w - r.x - r.width) < 5;
    return anchors;
 
  }
 */
  
  /** Horizontally divide split and resize the new split
   *
   * @since 2004/04/24
   * */
  private static void hSplitAndResize(Component base,
      Component left, Component right, double proportion){
    Container parent = base.getParent();
    SplitContainer newSplit = new SplitContainer(JSplitPane.
        HORIZONTAL_SPLIT);
    DockingUtilities.replaceChild(parent, base, newSplit);
    newSplit.setLeftComponent( left);
    newSplit.setRightComponent(right);
    SwingUtilities.invokeLater(new SplitResizer(newSplit, proportion));
  }
  
  /** Vertically divide split and resize the new split
   *
   * @since 2004/04/24
   * */
  private static void vSplitAndResize(Component base,
      Component top, Component bottom, double proportion){
    Container parent = base.getParent();
    SplitContainer newSplit = new SplitContainer(JSplitPane.
        VERTICAL_SPLIT);
    DockingUtilities.replaceChild(parent, base, newSplit);
    newSplit.setTopComponent( top);
    newSplit.setBottomComponent(bottom);
    SwingUtilities.invokeLater(new SplitResizer(newSplit, proportion));
  }
  
  /** look up the split hierarchy to find which borders a component is touching
   *
   * @param base                the component to find anchors for
   * @param ancestorContainer   the top level ancestor used to stop searching
   */
  public static int findAnchors(Component base, Container ancestorContainer){
    int contact = AnchorConstraints.ANCHOR_TOP|AnchorConstraints.ANCHOR_LEFT
        |AnchorConstraints.ANCHOR_BOTTOM|AnchorConstraints.ANCHOR_RIGHT;
    
    Component comp = base.getParent();
    Component child = base;
    while (comp != null && comp != ancestorContainer && contact != 0){
      if (comp instanceof SplitContainer){
        SplitContainer sc = (SplitContainer) comp;
        if (sc.getOrientation() == JSplitPane.VERTICAL_SPLIT){
          if (sc.getTopComponent() == child){
            contact = contact & (~AnchorConstraints.ANCHOR_BOTTOM); // not at bottom
          } else {
            contact = contact & (~AnchorConstraints.ANCHOR_TOP); // not on top
          }
        } else {
          if (sc.getLeftComponent() == child){
            contact = contact & (~AnchorConstraints.ANCHOR_RIGHT); // not right
          } else {
            contact = contact & (~AnchorConstraints.ANCHOR_LEFT); // not left
          }
        }
      }
      child = comp;
      comp = comp.getParent();
    }
    return contact;
  }
  
  /** Tries to find the best position to insert an hidden dockable, and insert it
   * @deprecated  use getInsertionDockingAction / applyDockingAction instead
   */
  public static SingleDockableContainer insertDockable(Container relativeAncestorContainer,
      Dockable dockable, RelativeDockablePosition position) {
    
    if (position == null){
      // for safety
      position = new RelativeDockablePosition(0,0.8,0.5,0.2);
    }
    SingleDockableContainer dockableContainer = null;
    if (relativeAncestorContainer.getComponentCount() == 0){ // empty docking panel      
      dockableContainer = DockableContainerFactory.getFactory().
          createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_DESKTOP);
      
      // default central insertion
      relativeAncestorContainer.add((Component)dockableContainer, BorderLayout.CENTER);
      relativeAncestorContainer.invalidate(); // 2005/05/04
      relativeAncestorContainer.validate();
      relativeAncestorContainer.repaint();
    } else {
      dockableContainer = DockableContainerFactory.getFactory().
          createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
      
      // convert relative positionning to current positionning (depending
      // on the current size of dockingPanel)
      int x = (int) (position.getX() * relativeAncestorContainer.getWidth());
      int y = (int) (position.getY() * relativeAncestorContainer.getHeight());
      int w = (int) (position.getWidth() * relativeAncestorContainer.getWidth());
      int h = (int) (position.getHeight() * relativeAncestorContainer.getHeight());
      int centerX = x + w / 2;
      int centerY = y + h / 2;
      
      SplitContainer split = findBestContainer(relativeAncestorContainer, position);
      if (split != null){
        // ok we've found one
        /*Rectangle splitRect = SwingUtilities.convertRectangle(split,
            new Rectangle(0, 0, split.getWidth(), split.getHeight()),
            relativeAncestorContainer);*/
        //int contacts = findAnchors(split, relativeAncestorContainer);
        
        int contacts = position.getAnchors(); // we use the anchors that
        // were computed on closing
        
        
        // heuristics : try to find an anchor, and which component(left/right)
        // is nearer of center
        /*boolean anchoredTop = Math.abs( y - splitRect.y) < 5;
        boolean anchoredLeft = Math.abs(x - splitRect.x) < 5;
        boolean anchoredBottom = Math.abs(y + h - splitRect.y - splitRect.height) < 30;
        boolean anchoredRight = Math.abs(x + w - splitRect.x - splitRect.width) < 5;*/
        
        boolean anchoredTop = (contacts & AnchorConstraints.ANCHOR_TOP)>0;
        boolean anchoredLeft =(contacts & AnchorConstraints.ANCHOR_LEFT)>0;
        boolean anchoredBottom = (contacts & AnchorConstraints.ANCHOR_BOTTOM)>0;
        boolean anchoredRight = (contacts & AnchorConstraints.ANCHOR_RIGHT)>0;
        
//          System.out.println("anchors : " + anchoredTop + ", "+ anchoredLeft + ", "
//              + anchoredBottom + ", " + anchoredRight);
        
        Component left = split.getLeftComponent();
        Component right = split.getRightComponent();
        Point leftCenter = SwingUtilities.convertPoint(left,
            left.getWidth() / 2,
            left.getHeight() / 2, relativeAncestorContainer);
        Point rightCenter = SwingUtilities.convertPoint(right,
            right.getWidth() / 2,
            right.getHeight() / 2, relativeAncestorContainer);
        int leftDist = (leftCenter.x - x) * (leftCenter.x - x)
        + (leftCenter.y - y) * (leftCenter.y);
        int rightDist = (rightCenter.x - x) * (rightCenter.x - x)
        + (rightCenter.y - y) * (rightCenter.y);
        
        Point bestCenter;
        Component bestComp;
        if (leftDist < rightDist) {
          bestCenter = leftCenter;
          bestComp = left;
        } else {
          bestCenter = rightCenter;
          bestComp = right;
        }
        
        
        //2006/09/12
        /* If the split matches the size of the component, then
         * we'll have to split it (and not split one of its children)
         */
        float splitWidthRatio = Math.abs(1 - split.getWidth() / (float)w);
        float splitHeightRatio = Math.abs(1 - split.getHeight() / (float)h);
        
        /** this will happen only when anchors are not too strict (like top+right)
         * for wider anchors (left-top-right) this ration is not used as
         * superflous
         */
        
        
        
        
        
        if (split.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
          
          
          Component top = split.getTopComponent();
          Component bottom = split.getBottomComponent();
          int topH = top.getHeight();
          int bottomH = bottom.getHeight();
          // proportions used when splitting a vertical split vertically
          float proportionTopH = h / (float)topH;
          float proportionBottomH = h / (float)bottomH;
          if (proportionTopH >= 0.6f){ // not too big
            proportionTopH = 0.6f;
          }
          if (proportionBottomH >= 0.6f){ // not too big
            proportionBottomH = 0.6f;
          }
          // proportions used when splitting a vertical split horizontally
          int splitW = split.getWidth();
          float proportionW = w / (float) splitW;
          if (proportionW >= 0.8f){ // not too big
            proportionW = 0.8f;
          }
          
          if (anchoredTop) {
            if (anchoredLeft) {
              if (anchoredBottom) { // TLB = full left
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    HORIZONTAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setLeftComponent( (Component) dockableContainer);
                newSplit.setRightComponent(split);
                
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionW));
              } else {
                // Top + left
                if (splitWidthRatio < 0.1f && split.getHeight() > h){//2006/09/12
                  // almost the same width, but the split is taller : we split it
                  // vertically
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split.getParent(), split,
                      newSplit);
                  newSplit.setTopComponent( (Component) dockableContainer);
                  newSplit.setBottomComponent(split);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
                } else {
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      HORIZONTAL_SPLIT);
                  DockingUtilities.replaceChild(split, top, newSplit);
                  newSplit.setLeftComponent( (Component) dockableContainer);
                  newSplit.setRightComponent(top);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionW));
                }
              }
            } else if (anchoredRight) { // TR
              if (anchoredBottom) { // TRB = full right
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    HORIZONTAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setRightComponent( (Component) dockableContainer);
                newSplit.setLeftComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionW));
              } else {
                if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                  // almost the same width, but the split is taller : we split it
                  // vertically
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split.getParent(), split,
                      newSplit);
                  newSplit.setTopComponent( (Component) dockableContainer);
                  newSplit.setBottomComponent(split);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
                } else {
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      HORIZONTAL_SPLIT);
                  DockingUtilities.replaceChild(split, top, newSplit);
                  newSplit.setLeftComponent(top);
                  newSplit.setRightComponent( (Component) dockableContainer);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionW));
                }
              }
            } else {
              // just anchored top
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split, top, newSplit);
              newSplit.setTopComponent( (Component) dockableContainer);
              newSplit.setBottomComponent(top);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionTopH));
            }
          } else if (anchoredBottom) { // but not anchoredTop
            if (anchoredLeft) { //BL
              if (anchoredRight) { //BLR = full bottom
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split, bottom, newSplit);
                newSplit.setTopComponent(bottom);
                newSplit.setBottomComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionBottomH));
              } else {
                if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                  // almost the same width, but the split is taller : we split it
                  // vertically
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split.getParent(), split,
                      newSplit);
                  newSplit.setBottomComponent( (Component) dockableContainer);
                  newSplit.setTopComponent(split);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, 1- h / (float)split.getHeight()));
                } else {
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      HORIZONTAL_SPLIT);
                  DockingUtilities.replaceChild(split, bottom, newSplit);
                  newSplit.setLeftComponent( (Component) dockableContainer);
                  newSplit.setRightComponent(bottom);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionW));
                }
              }
            } else if (anchoredRight) { // BR
              if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                // almost the same width, but the split is taller : we split it
                // vertically
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setBottomComponent( (Component) dockableContainer);
                newSplit.setTopComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1 - h / (float)split.getHeight()));
              } else {
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    HORIZONTAL_SPLIT);
                DockingUtilities.replaceChild(split, bottom, newSplit);
                newSplit.setLeftComponent(bottom);
                newSplit.setRightComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionW));
              }
            } else {
              // just anchored bottom
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split, bottom, newSplit);
              newSplit.setTopComponent(bottom);
              newSplit.setBottomComponent( (Component) dockableContainer);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionBottomH));
            }
          } else if (anchoredLeft) { // not anchored on top / bottom
            if (anchoredRight){ //2005/10/10
              // left + right on a vertical split, means we have to insert a new component
              // in between
              int yTop = y;
              int yBottom = split.getHeight() - y - h;
              int splitTop = top.getHeight();
              int splitBottom = bottom.getHeight();
              
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);
              
              if (bestComp == top){
                newSplit.setTopComponent(bestComp);
                newSplit.setBottomComponent((Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(split,
                    (split.getHeight() - yBottom)/(float) split.getHeight()));
                SwingUtilities.invokeLater(new SplitResizer(newSplit,
                    yTop/(float) split.getHeight()));
              } else {
                newSplit.setBottomComponent(bestComp);
                newSplit.setTopComponent((Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(split, yTop/(float) split.getHeight()));
                SwingUtilities.invokeLater(new SplitResizer(newSplit, h/(float) splitBottom));
              }
              
            } else {// only left
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  HORIZONTAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);
              newSplit.setRightComponent(bestComp);
              newSplit.setLeftComponent( (Component) dockableContainer);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionW));
            }
          } else if (anchoredRight) {
            SplitContainer newSplit = new SplitContainer(JSplitPane.
                HORIZONTAL_SPLIT);
            DockingUtilities.replaceChild(split, bestComp, newSplit);
            newSplit.setLeftComponent(bestComp);
            newSplit.setRightComponent( (Component) dockableContainer);
            SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionW));
          } else { // not anchored at all, split verticaly and add
            // todo : check how it's done now for the split-h part and do the same here
            SplitContainer newSplit = new SplitContainer(JSplitPane.
                VERTICAL_SPLIT);
            DockingUtilities.replaceChild(split, bestComp, newSplit);
            if (bestCenter.y < centerY) {
              newSplit.setTopComponent(bestComp);
              newSplit.setBottomComponent( (Component) dockableContainer);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionBottomH));
            } else {
              newSplit.setBottomComponent(bestComp);
              newSplit.setTopComponent( (Component) dockableContainer);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionTopH));
            }
          }
        } else {
          
          
          // horizontal split
          // proportions used when splitting a horizontal split horizontally
          float proportionLeftW = w / (float)left.getWidth();
          float proportionRightW = w / (float)right.getWidth();
          if (proportionLeftW >= 0.8f){ // not too big
            proportionLeftW = 0.8f;
          }
          if (proportionRightW >= 0.8f){ // not too big
            proportionRightW = 0.8f;
          }
          // proportions used when splitting a horizontal split vertically
          int splitH = split.getHeight();
          float proportionH = h / (float) splitH;
          if (proportionH >= 0.6f){ // not too big
            proportionH = 0.6f;
          }
          
          
          
          if (anchoredTop) {
            if (anchoredLeft) {
              if (anchoredBottom) { // TLB = full left
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    HORIZONTAL_SPLIT);
                DockingUtilities.replaceChild(split, left,
                    newSplit);
                newSplit.setLeftComponent( (Component) dockableContainer);
                newSplit.setRightComponent(left);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionLeftW));
              } else if (anchoredRight){ // TLR = full top
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split, newSplit);
                newSplit.setTopComponent( (Component) dockableContainer);
                newSplit.setBottomComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionH));
              } else { // just anchored top/left
                if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                  // almost the same width, but the split is taller : we split it
                  // vertically
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split.getParent(), split,
                      newSplit);
                  newSplit.setTopComponent( (Component) dockableContainer);
                  newSplit.setBottomComponent(split);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
                } else {
                  // otherwise we just split vertically on the left side
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split, left,
                      newSplit);
                  newSplit.setTopComponent( (Component) dockableContainer);
                  newSplit.setBottomComponent(left);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionH));
                }
              }
            } else if (anchoredRight) { // Top but not left
              if (anchoredBottom) { // top + right + bottom == full right
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    HORIZONTAL_SPLIT);
                DockingUtilities.replaceChild(split, right,
                    newSplit);
                newSplit.setRightComponent( (Component) dockableContainer);
                newSplit.setLeftComponent(right);
                
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionRightW));
                
              } else { // top + right
                if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                  // almost the same width, but the split is taller : we split it
                  // vertically
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split.getParent(), split,
                      newSplit);
                  newSplit.setTopComponent( (Component) dockableContainer);
                  newSplit.setBottomComponent(split);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
                } else {
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split, right, newSplit);
                  newSplit.setBottomComponent(right);
                  newSplit.setTopComponent( (Component) dockableContainer);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionH));
                }
              }
            } else if (anchoredBottom){ //2005/10/10
              // top + bottom : create a horizontal split
              // as we are inserting a vertical element into a horizontal split
              // we have to adjust the width on both sides
              int xLeft = x;
              int xRight = split.getWidth() - x - w;
              int splitLeft = left.getWidth();
              int splitRight = right.getWidth();
              
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  HORIZONTAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);
              
              if (bestComp == left){
                newSplit.setLeftComponent(bestComp);
                newSplit.setRightComponent((Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(split, (split.getWidth() - xRight)/(float) split.getWidth()));
                SwingUtilities.invokeLater(new SplitResizer(newSplit, xLeft/(float) split.getWidth()));
              } else {
                newSplit.setRightComponent(bestComp);
                newSplit.setLeftComponent((Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(split, xLeft/(float) split.getWidth()));
                SwingUtilities.invokeLater(new SplitResizer(newSplit, w/(float) splitRight));
              }
            } else {
              // just anchored top
              if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                // almost the same width, but the split is taller : we split it
                // vertically
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setTopComponent( (Component) dockableContainer);
                newSplit.setBottomComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
              } else {
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split, bestComp, newSplit);
                newSplit.setTopComponent( (Component) dockableContainer);
                newSplit.setBottomComponent(bestComp);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionH));
              }
            }
          } else if (anchoredBottom) { // but not anchoredTop
            if (anchoredLeft) {
              if (anchoredRight) { // BLR == full bottom
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split, newSplit);
                newSplit.setTopComponent(split);
                newSplit.setBottomComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionH));
              } else { // bottom + left
                if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                  // almost the same width, but the split is taller : we split it
                  // vertically
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split.getParent(), split,
                      newSplit);
                  newSplit.setBottomComponent( (Component) dockableContainer);
                  newSplit.setTopComponent(split);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, 1 - h / (float)split.getHeight()));
                } else {
                  SplitContainer newSplit = new SplitContainer(JSplitPane.
                      VERTICAL_SPLIT);
                  DockingUtilities.replaceChild(split, left, newSplit);
                  newSplit.setBottomComponent( (Component) dockableContainer);
                  newSplit.setTopComponent(left);
                  SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionH));
                }
              }
            } else if (anchoredRight) { // bottom + right
              if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                // almost the same width, but the split is taller : we split it
                // vertically
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setBottomComponent( (Component) dockableContainer);
                newSplit.setTopComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1- h / (float)split.getHeight()));
              } else {
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split, right, newSplit);
                newSplit.setTopComponent(right);
                newSplit.setBottomComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionH));
              }
            } else {
              // just anchored bottom
              if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                // almost the same width, but the split is taller : we split it
                // vertically
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setBottomComponent( (Component) dockableContainer);
                newSplit.setTopComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1- h / (float)split.getHeight()));
              } else {
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split, bestComp, newSplit);
                newSplit.setTopComponent(bestComp);
                newSplit.setBottomComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionH));
              }
            }
          } else if (anchoredLeft) { // not anchored on top / bottom
            if (anchoredRight){
              // left + right... we have to split vertically
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split.getParent(), split,
                  newSplit);
              newSplit.setTopComponent( (Component) dockableContainer);
              newSplit.setBottomComponent(split); // todo : check this case :  why is the new component on top
              SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionH));
            } else {
              // just left
              if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
                // almost the same width, but the split is taller : we split it
                // vertically
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    VERTICAL_SPLIT);
                DockingUtilities.replaceChild(split.getParent(), split,
                    newSplit);
                newSplit.setTopComponent( (Component) dockableContainer);
                newSplit.setBottomComponent(split);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
              } else {
                SplitContainer newSplit = new SplitContainer(JSplitPane.
                    HORIZONTAL_SPLIT);
                DockingUtilities.replaceChild(split, left, newSplit);
                newSplit.setRightComponent(left);
                newSplit.setLeftComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionLeftW));
              }
            }
          } else if (anchoredRight) {
            if (splitWidthRatio < 0.1f && split.getHeight() > h){ // 2006/09/12
              // almost the same width, but the split is taller : we split it
              // vertically
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split.getParent(), split,
                  newSplit);
              newSplit.setTopComponent( (Component) dockableContainer);
              newSplit.setBottomComponent(split);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, h / (float)split.getHeight()));
            } else {              
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  HORIZONTAL_SPLIT);
              DockingUtilities.replaceChild(split, right, newSplit);
              newSplit.setLeftComponent(right);
              newSplit.setRightComponent( (Component) dockableContainer);
              SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionRightW));
            }
          } else { // not anchored at all
            // this part can be improved :
            //  - we know we're into an horizontal split
            //  - we can guess if the component was horizontaly splitted or vertically,
            //    and on which side of the split if was (bestComp)
            
            
            float widthIfSplitH = bestComp.getWidth()/2;
            float heightIfSplitH = bestComp.getHeight();
            float widthIfSplitV = bestComp.getWidth();
            float heightIfSplitV = bestComp.getHeight()/2;
            // now which case keeps the best proportions ?
            float whH = widthIfSplitH / heightIfSplitH;
            float whV = widthIfSplitV / heightIfSplitV;
            float whBefore = w / (float)h;
            if (Math.abs(whBefore - whV) < Math.abs(whBefore - whH)){
              // nearer of V
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);
              if (bestCenter.y < centerY) {
                newSplit.setTopComponent(bestComp);
                newSplit.setBottomComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, 1-proportionH));
              } else {
                newSplit.setBottomComponent(bestComp);
                newSplit.setTopComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionH));
              }
            } else {
              SplitContainer newSplit = new SplitContainer(JSplitPane.
                  HORIZONTAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);
              if (bestCenter.x < centerX) {
                newSplit.setLeftComponent(bestComp);
                newSplit.setRightComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionRightW));
              } else {
                newSplit.setRightComponent(bestComp);
                newSplit.setLeftComponent( (Component) dockableContainer);
                SwingUtilities.invokeLater(new SplitResizer(newSplit, proportionLeftW));
              }
            }
            
          }
        }
      } else { // 2004/04/01 (this part was missing) ------------------------------
        // one component, but not a split container
        // heuristics : try to find an anchor
        boolean anchoredTop = y < 5;
        boolean anchoredLeft = x < 5;
        boolean anchoredBottom = Math.abs(y + h - relativeAncestorContainer.getHeight()) < 5;
        boolean anchoredRight = Math.abs(x + w - relativeAncestorContainer.getWidth()) < 5;
        Component singleComp = relativeAncestorContainer.getComponent(0);
        
        float proportionW = w / (float) relativeAncestorContainer.getWidth();
        if (proportionW >= 0.8f){ // not too big
          proportionW = 0.8f;
        }
        float proportionH = h / (float) relativeAncestorContainer.getHeight();
        if (proportionH >= 0.6f){ // not too big
          proportionH = 0.6f;
        }
        
        
        if (anchoredTop){
          if (anchoredLeft){
            if (anchoredBottom){
              hSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionW);
            } else if (anchoredRight){ // top + left + right == TOP
              vSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionH);
            } else { // top + left... what should we do ?
              if (w > h){
                vSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionH);
              } else {
                hSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionW);
              }
            }
          } else if (anchoredRight){ // top + right (not left)
            if (anchoredBottom){ // top + right + bottom == on the right
              hSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionW);
            } else { // top + right
              if (w > h){ // on top
                vSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionH);
              } else { // on the right
                hSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionW);
              }
            }
          } else { // top only
            vSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionH);
          }
        } else if (anchoredLeft){ //left (but not top)
          if (anchoredBottom){ // left + bot
            if (anchoredRight){ // == bottom
              vSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionH);
            } else { // left + bottom
              if (w > h){ // bottom
                vSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionH);
              } else { // on the left
                hSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionW);
              }
            }
          } else if (anchoredRight){ // left + right, but not top/bottom...
            if (centerY < relativeAncestorContainer.getHeight()/2){ // center is upper part
              vSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionH);
            } else { // lower part
              vSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionH);
            }
          } else { // left only
            hSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionW);
          }
        } else if (anchoredBottom){ // bottom, but not top/left
          if (anchoredRight){
            if (w > h){ // bottom
              vSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionH);
            } else { // on the right
              hSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionW);
            }
          } else { // just bottom
            vSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionH);
          }
        } else if (anchoredRight){ // just right
          hSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionW);
        } else { // no anchors
          if (centerY < relativeAncestorContainer.getHeight()/2){ // center is upper part
            vSplitAndResize(singleComp, (Component) dockableContainer, singleComp, proportionH);
          } else {
            vSplitAndResize(singleComp, singleComp, (Component) dockableContainer, 1-proportionH);
          }
          
        }
      } // 2004/04/01 (end of missing part) ------------------------------
    }
    return dockableContainer;
    
    
    //DockingActionEvent action = getInsertionDockingAction(relativeAncestorContainer, dockable, position);
    //return applyDockingAction(dockable, action);
    
    
    
  }
  
  /** returns a docking action event corresponding to the insertion point and method
   * of the dockable.
   */
  public static DockingActionEvent getInsertionDockingAction(Container relativeAncestorContainer,
      Dockable dockable, DockableState dockableState, DockableState newState){
    RelativeDockablePosition position = dockableState.getPosition();
    DockingDesktop desktop = dockableState.getDesktop();
    DockableState.Location initialState = dockableState.getLocation();
    DockableState.Location nextState = newState.getLocation();
    
    if (position == null){
      // for safety
      position = new RelativeDockablePosition(0,0.8,0.5,0.2);
    }
    
    SingleDockableContainer dockableContainer = null;
    if (relativeAncestorContainer.getComponentCount() == 0){ // empty docking panel
      return new DockingActionAddDockableEvent(desktop, dockable,
          initialState, nextState, relativeAncestorContainer);
    } else {
      // convert relative positionning to current positionning (depending
      // on the current size of dockingPanel)
      int x = (int) (position.getX() * relativeAncestorContainer.getWidth());
      int y = (int) (position.getY() * relativeAncestorContainer.getHeight());
      int w = (int) (position.getWidth() * relativeAncestorContainer.getWidth());
      int h = (int) (position.getHeight() * relativeAncestorContainer.getHeight());
      int centerX = x + w / 2;
      int centerY = y + h / 2;
      
      SplitContainer split = findBestContainer(relativeAncestorContainer, position);
      if (split != null){
        // ok we've found one
        Rectangle splitRect = SwingUtilities.convertRectangle(split,
            new Rectangle(0, 0, split.getWidth(), split.getHeight()),
            relativeAncestorContainer);
        
        // heuristics : try to find an anchor, and which component(left/right)
        // is nearer of center
        boolean anchoredTop = Math.abs( y - splitRect.y) < 5;
        boolean anchoredLeft = Math.abs(x - splitRect.x) < 5;
        boolean anchoredBottom = Math.abs(y + h - splitRect.y - splitRect.height) < 30/*5*/;
        boolean anchoredRight = Math.abs(x + w - splitRect.x - splitRect.width) < 5;
        
        
        Component left = split.getLeftComponent();
        Component right = split.getRightComponent();
        Point leftCenter = SwingUtilities.convertPoint(left,
            left.getWidth() / 2,
            left.getHeight() / 2, relativeAncestorContainer);
        Point rightCenter = SwingUtilities.convertPoint(right,
            right.getWidth() / 2,
            right.getHeight() / 2, relativeAncestorContainer);
        int leftDist = (leftCenter.x - x) * (leftCenter.x - x)
        + (leftCenter.y - y) * (leftCenter.y);
        int rightDist = (rightCenter.x - x) * (rightCenter.x - x)
        + (rightCenter.y - y) * (rightCenter.y);
        
        Point bestCenter;
        Component bestComp;
        if (leftDist < rightDist) {
          bestCenter = leftCenter;
          bestComp = left;
        } else {
          bestCenter = rightCenter;
          bestComp = right;
        }
        
        
        if (split.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
          
          Component top = split.getTopComponent();
          Component bottom = split.getBottomComponent();
          int topH = top.getHeight();
          int bottomH = bottom.getHeight();
          // proportions used when splitting a vertical split vertically
          float proportionTopH = h / (float)topH;
          float proportionBottomH = h / (float)bottomH;
          if (proportionTopH >= 0.6f){ // not too big
            proportionTopH = 0.6f;
          }
          if (proportionBottomH >= 0.6f){ // not too big
            proportionBottomH = 0.6f;
          }
          // proportions used when splitting a vertical split horizontally
          int splitW = split.getWidth();
          float proportionW = w / (float) splitW;
          if (proportionW >= 0.8f){ // not too big
            proportionW = 0.8f;
          }
          
          if (anchoredTop) {
            if (anchoredLeft) {
              if (anchoredBottom) {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, split, DockingConstants.SPLIT_LEFT, proportionW);
              } else {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, top, DockingConstants.SPLIT_LEFT, proportionW);
              }
            } else if (anchoredRight) {
              if (anchoredBottom) {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, split, DockingConstants.SPLIT_RIGHT, 1-proportionW);
              } else {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, top, DockingConstants.SPLIT_RIGHT, 1-proportionW);
              }
            } else {
              // just anchored top
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, top, DockingConstants.SPLIT_TOP, proportionTopH);
            }
          } else if (anchoredBottom) { // but not anchoredTop
            if (anchoredLeft) {
              if (anchoredRight) {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, bottom, DockingConstants.SPLIT_BOTTOM, 1-proportionBottomH);
              } else {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, bottom, DockingConstants.SPLIT_LEFT, proportionW);
              }
            } else if (anchoredRight) {
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bottom, DockingConstants.SPLIT_RIGHT, 1-proportionW);
            } else {
              // just anchored bottom
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bottom, DockingConstants.SPLIT_BOTTOM, 1-proportionBottomH);
            }
          } else if (anchoredLeft) { // not anchored on top / bottom
            if (anchoredRight){ //2005/10/10
              // left + right on a vertical split, means we have to insert a new component
              // in between
              int yTop = y;
              int yBottom = split.getHeight() - y - h;
              int splitTop = top.getHeight();
              int splitBottom = bottom.getHeight();
              
              /*SplitContainer newSplit = new SplitContainer(JSplitPane.
                  VERTICAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);*/
              
              if (bestComp == top){
                float hParent =( split.getHeight() - yBottom)/(float) split.getHeight() ;
                float hChild = yTop/(float) split.getHeight();
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, bestComp, DockingConstants.SPLIT_BOTTOM, hChild, hParent);
              } else {
                float hChild =  h/(float) splitBottom;
                float hParent = yTop/(float) split.getHeight();
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, bestComp, DockingConstants.SPLIT_TOP, hChild, hParent);
              }
            } else {// only left
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_LEFT, proportionW);
            }
          } else if (anchoredRight) {
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, bestComp, DockingConstants.SPLIT_RIGHT, 1-proportionW);
          } else { // not anchored at all, split verticaly and add
            
            SplitContainer newSplit = new SplitContainer(JSplitPane.
                VERTICAL_SPLIT);
            DockingUtilities.replaceChild(split, bestComp, newSplit);
            if (bestCenter.y < centerY) {
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_BOTTOM, 1-proportionBottomH);
            } else {
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_TOP, proportionTopH);
            }
          }
        } else {
          
          
          // horizontal split
          // proportions used when splitting a horizontal split horizontally
          float proportionLeftW = w / (float)left.getWidth();
          float proportionRightW = w / (float)right.getWidth();
          if (proportionLeftW >= 0.8f){ // not too big
            proportionLeftW = 0.8f;
          }
          if (proportionRightW >= 0.8f){ // not too big
            proportionRightW = 0.8f;
          }
          // proportions used when splitting a horizontal split vertically
          int splitH = split.getHeight();
          float proportionH = h / (float) splitH;
          if (proportionH >= 0.6f){ // not too big
            proportionH = 0.6f;
          }
          
          if (anchoredTop) {
            if (anchoredLeft) {
              if (anchoredBottom) {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, left, DockingConstants.SPLIT_LEFT, proportionLeftW);
              } else if (anchoredRight){
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, split, DockingConstants.SPLIT_TOP, proportionH);
              } else { // just anchored top/left
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, left, DockingConstants.SPLIT_TOP, proportionH);
              }
            } else if (anchoredRight) {
              if (anchoredBottom) { // top + right + bottom
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, right, DockingConstants.SPLIT_RIGHT, 1-proportionRightW);
              } else { // top + right
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, right, DockingConstants.SPLIT_TOP, proportionH);
              }
            } else if (anchoredBottom){ //2005/10/10
              // top + bottom : create a horizontal split
              // as we are inserting a vertical element into a horizontal split
              // we have to adjust the width on both sides
              int xLeft = x;
              int xRight = split.getWidth() - x - w;
              int splitLeft = left.getWidth();
              int splitRight = right.getWidth();
              
              /*SplitContainer newSplit = new SplitContainer(JSplitPane.
                  HORIZONTAL_SPLIT);
              DockingUtilities.replaceChild(split, bestComp, newSplit);*/
              
              if (bestComp == left){
                float parentW = (split.getWidth() - xRight)/(float) split.getWidth();
                float childW = xLeft/(float) split.getWidth();
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, bestComp, DockingConstants.SPLIT_RIGHT, childW, parentW);
              } else {
                float parentW = xLeft/(float) split.getWidth();
                float childW =  w/(float) splitRight;
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, bestComp, DockingConstants.SPLIT_LEFT, childW, parentW);
              }
            } else {
              // just anchored top
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_TOP, proportionH);
            }
          } else if (anchoredBottom) { // but not anchoredTop
            if (anchoredLeft) {
              if (anchoredRight) {
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, split, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
                
              } else { // bottom + left
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, left, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
              }
            } else if (anchoredRight) { // bottom + right
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, right, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
            } else {
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
            }
          } else if (anchoredLeft) { // not anchored on top / bottom
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, left, DockingConstants.SPLIT_LEFT, proportionLeftW);
          } else if (anchoredRight) {
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, right, DockingConstants.SPLIT_RIGHT, 1-proportionRightW);
          } else { // not anchored at all, split verticaly and add
            /*SplitContainer newSplit = new SplitContainer(JSplitPane.
                VERTICAL_SPLIT);
            DockingUtilities.replaceChild(split, bestComp, newSplit);*/
            if (bestCenter.y < centerY) {
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
            } else {
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, bestComp, DockingConstants.SPLIT_TOP, proportionH);
            }
          }
        }
      } else { // 2004/04/01 (this part was missing) ------------------------------
        // one component, but not a split container
        // heuristics : try to find an anchor
        boolean anchoredTop = y < 5;
        boolean anchoredLeft = x < 5;
        boolean anchoredBottom = Math.abs(y + h - relativeAncestorContainer.getHeight()) < 5;
        boolean anchoredRight = Math.abs(x + w - relativeAncestorContainer.getWidth()) < 5;
        Component singleComp = relativeAncestorContainer.getComponent(0);
        
        float proportionW = w / (float) relativeAncestorContainer.getWidth();
        if (proportionW >= 0.8f){ // not too big
          proportionW = 0.8f;
        }
        float proportionH = h / (float) relativeAncestorContainer.getHeight();
        if (proportionH >= 0.6f){ // not too big
          proportionH = 0.6f;
        }
        
        
        if (anchoredTop){
          if (anchoredLeft){
            if (anchoredBottom){
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_LEFT, proportionW);
            } else if (anchoredRight){ // top + left + right == TOP
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_TOP, proportionH);
            } else { // top + left... what should we do ?
              if (w > h){ // vsplit
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, singleComp, DockingConstants.SPLIT_TOP, proportionH);
              } else { // hsplit
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, singleComp, DockingConstants.SPLIT_LEFT, proportionW);
              }
            }
          } else if (anchoredRight){ // top + right (not left)
            if (anchoredBottom){ // top + right + bottom == on the right
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_RIGHT, 1-proportionW);
            } else { // top + right
              if (w > h){ // on top
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, singleComp, DockingConstants.SPLIT_TOP, proportionH);
              } else { // on the right
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, singleComp, DockingConstants.SPLIT_RIGHT, 1-proportionW);
              }
            }
          } else { // top only
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, singleComp, DockingConstants.SPLIT_TOP, proportionH);
          }
        } else if (anchoredLeft){ //left (but not top)
          if (anchoredBottom){ // left + bot
            if (anchoredRight){ // == bottom
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
            } else { // left + bottom
              if (w > h){ // bottom
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, singleComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
              } else { // on the left
                return new DockingActionSplitComponentEvent(desktop, dockable,
                    initialState, nextState, singleComp, DockingConstants.SPLIT_LEFT, proportionW);
              }
            }
          } else if (anchoredRight){ // left + right, but not top/bottom...
            if (centerY < relativeAncestorContainer.getHeight()/2){ // center is upper part
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_TOP, proportionH);
            } else { // lower part
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
            }
          } else { // left only
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, singleComp, DockingConstants.SPLIT_LEFT, proportionW);
          }
        } else if (anchoredBottom){ // bottom, but not top/left
          if (anchoredRight){
            if (w > h){ // bottom
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
            } else { // on the right
              return new DockingActionSplitComponentEvent(desktop, dockable,
                  initialState, nextState, singleComp, DockingConstants.SPLIT_RIGHT, 1-proportionW);
            }
          } else { // just bottom
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, singleComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
          }
        } else if (anchoredRight){ // just right
          return new DockingActionSplitComponentEvent(desktop, dockable,
              initialState, nextState, singleComp, DockingConstants.SPLIT_RIGHT, 1-proportionW);
        } else { // no anchors
          if (centerY < relativeAncestorContainer.getHeight()/2){ // center is upper part
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, singleComp, DockingConstants.SPLIT_TOP, proportionH);
          } else {
            return new DockingActionSplitComponentEvent(desktop, dockable,
                initialState, nextState, singleComp, DockingConstants.SPLIT_BOTTOM, 1-proportionH);
          }
        }
      }
    }
  }
  
  public static SingleDockableContainer applyDockingAction(Dockable dockable, DockingActionEvent action){
    SingleDockableContainer dockableContainer;
    switch (action.getActionType()){
      case DockingActionEvent.ACTION_ADD_DOCKABLE:
        DockingActionAddDockableEvent addAction = (DockingActionAddDockableEvent) action;
        dockableContainer = DockableContainerFactory.getFactory().
            createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_DESKTOP);
        Container relativeAncestorContainer = addAction.getParentContainer();
        // default central insertion
        relativeAncestorContainer.add((Component)dockableContainer, BorderLayout.CENTER);
        relativeAncestorContainer.invalidate(); // 2005/05/04
        relativeAncestorContainer.validate();
        relativeAncestorContainer.repaint();
        return dockableContainer;
      case DockingActionEvent.ACTION_SPLIT_COMPONENT:
        dockableContainer = DockableContainerFactory.getFactory().
            createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
        DockingActionSplitComponentEvent splitAction = (DockingActionSplitComponentEvent) action;
        Component base = splitAction.getBase();
        float div = splitAction.getDividorLocation();
        DockingConstants.Split splitPosition = splitAction.getSplitPosition();
        float parentDiv = splitAction.getParentDividorLocation();
        switch (splitPosition.value()){
          case DockingConstants.INT_SPLIT_TOP:
            vSplitAndResize(base, (Container) dockableContainer, base, div);
            break;
          case DockingConstants.INT_SPLIT_LEFT:
            hSplitAndResize(base, (Container) dockableContainer, base, div);
            break;
          case DockingConstants.INT_SPLIT_BOTTOM:
            vSplitAndResize(base, base, (Container) dockableContainer, div);
            break;
          case DockingConstants.INT_SPLIT_RIGHT:
            hSplitAndResize(base, base, (Container) dockableContainer, div);
            break;
        }
        if (parentDiv != -1 && base.getParent() instanceof SplitContainer){
          SplitContainer parent = (SplitContainer) base.getParent();
          new SplitResizer(parent, parentDiv);
        }
        return dockableContainer;
      default:
        throw new IllegalArgumentException("Action type not managed : "+ action.getActionType());
    }
    
    
  }
}