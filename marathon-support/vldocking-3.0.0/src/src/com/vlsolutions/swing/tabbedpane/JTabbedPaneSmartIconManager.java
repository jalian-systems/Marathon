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


package com.vlsolutions.swing.tabbedpane;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/** This class is the interface between a JTabbedPane and a smart icon.
 *<p>
 * As JTabbedPanes cannot use any JComponents as tab selectors (the access if protected and we just have
 * a label, an icon and a tooltip), we have to rely on tricks to bypass them. 
 * <p>
 * This class is used (with a mouse listener) to forward events into the smart icon. which is responsible
 * for faking an enhanced tab selector (including label and optional buttons).
 *
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class JTabbedPaneSmartIconManager implements MouseListener, MouseMotionListener {
  
  private JTabbedPane tabbedPane;
  
  private JTabbedPaneSmartIcon pressedIcon;
  private int pressedTab; // the tab associated to the pressed icon  

  private JTabbedPaneSmartIcon movedIcon;
  private int movedTab;
  
  /** Constructs a new tabbedPane manager for a given JTabbedPane */
  public JTabbedPaneSmartIconManager(JTabbedPane tabbedPane) {
    this.tabbedPane = tabbedPane;
    tabbedPane.addMouseListener(this);
    tabbedPane.addMouseMotionListener(this);
  }

  /** MouseListener implementation, use to track mouse behaviour inside the tab selector bounds
   * and forward them to the appropriate smart icon.
   */
  public void mouseReleased(MouseEvent e) {
    checkTabCount();
    // forward the event to the pressed smart icon
    if (pressedIcon != null){
      Point p = e.getPoint();
      
      final Rectangle r = tabbedPane.getBoundsAt(pressedTab);
      Point iPoint = convertPointToIcon(r, p, pressedIcon);
      
      final MouseEvent eSmart = new MouseEvent(
            (Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
            iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
      try {
        if (pressedIcon.onMouseReleased(eSmart)){
          tabbedPane.repaint(r.x, r.y, r.width, r.height); // no choice but trigger a repaint        
        }
      } catch (Exception ignore){ // bug database : 5075526 this is to remove the stack trace        
      }
      pressedIcon = null;
    }    
  }

  private Point convertPointToIcon(Rectangle r, Point p, Icon icon){
    int x = p.x - (r.x + r.width/2 - icon.getIconWidth()/2) ;
    int y = p.y - (r.y + r.height/2 - icon.getIconHeight()/2);
    return new Point(x, y);
  }

  
  /** MouseListener implementation, use to track mouse behaviour inside the tab selector bounds
   * and forward them to the appropriate smart icon.
   */
  public void mousePressed(MouseEvent e) {
    // where is the mouse pressed ?
    Point p = e.getPoint();
    
    this.pressedIcon = null; // reset the pressed state
    
    int targetTab = findTabAt(p);
    if (targetTab != -1){
      Icon icon = tabbedPane.getIconAt(targetTab);
      if (icon instanceof JTabbedPaneSmartIcon){
        JTabbedPaneSmartIcon smartIcon = (JTabbedPaneSmartIcon) icon;
        // convert point into smartIcon coordinates
                
        // get the tab bounds
        Rectangle r = tabbedPane.getBoundsAt(targetTab);
        
        // as the icon is the only thing visible, we consider it centered in the tab
        // (which is the default paint behaviour of BasicTabbedPaneUI, and should be okay
        // with most look and feels.
        int x = p.x - (r.x + r.width/2 - icon.getIconWidth()/2) ;
        int y = p.y - (r.y + r.height/2 - icon.getIconHeight()/2);
        if (x >=0 && y >= 0 && x < icon.getIconWidth() && y < icon.getIconHeight()){
          // forward the event to the smart icon
          MouseEvent eSmart = new MouseEvent(
              (Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
              x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
          if (smartIcon.onMousePressed(eSmart)){
            tabbedPane.repaint(r.x, r.y, r.width, r.height); // no choice but trigger a repaint          
          }
          pressedIcon = smartIcon;
          pressedTab = targetTab;
        } 
      } 
    }
  }

  /** verify if the tab count hasn't changed. 
   *<p>
   * There is always the risk that the last tab has been removed (and movedTab will be
   * out of bounds)
   *
   */
  private void checkTabCount(){
    if (movedTab >= tabbedPane.getTabCount()){ // too late : tab has been removed
      movedTab = -1;
      movedIcon = null;
    }
    if (pressedTab >= tabbedPane.getTabCount()){
      pressedTab = -1;
      pressedIcon = null;
    }
  }

  private int findTabAt(Point p){
    int x= p.x;
    int y = p.y;
    for (int i=0; i < tabbedPane.getTabCount(); i++){
      if (tabbedPane.getBoundsAt(i).contains(x, y)){
        return i;
      }
    }
    return -1;
  }
  
  
  /** MouseListener implementation, use to track mouse behaviour inside the tab selector bounds
   * and forward them to the appropriate smart icon.
   */
  public void mouseMoved(MouseEvent e) {
    checkTabCount();

    // where is the mouse moved ?
    Point p = e.getPoint();
    
    int targetTab = findTabAt(p);
    if (targetTab != -1){
      Icon icon = tabbedPane.getIconAt(targetTab);
      if (icon instanceof JTabbedPaneSmartIcon){
        JTabbedPaneSmartIcon smartIcon = (JTabbedPaneSmartIcon) icon;
        if (movedIcon != null && movedIcon != smartIcon){
          // trigger a mouseExit from the movedIcon
          Rectangle prevRect = tabbedPane.getBoundsAt(movedTab);
          Point iPoint = convertPointToIcon(prevRect, p, movedIcon);
          MouseEvent eSmart = new MouseEvent(
              (Component) e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(),
              iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
          if (movedIcon.onMouseExited(eSmart)){
            String tip = movedIcon.getLocalTooltipText(); 
            if (tip != null && ! tip.equals(tabbedPane.getToolTipTextAt(targetTab))){
              tabbedPane.setToolTipTextAt(targetTab, tip);
            }
//            tabbedPane.repaint(prevRect.x, prevRect.y, prevRect.width, prevRect.height); 
           tabbedPane.revalidate(); 
           tabbedPane.repaint(); 
          }
          movedIcon = null;
          movedTab = -1;
        }
        
        // convert point into smartIcon coordinates
        // get the tab bounds
        Rectangle r = tabbedPane.getBoundsAt(targetTab);
        Point iPoint = convertPointToIcon(r, p, icon);
        
        if (iPoint.x >=0 && iPoint.y >= 0 
            && iPoint.x < icon.getIconWidth() 
            && iPoint.y < icon.getIconHeight()){
          // forward the event to the smart icon
          MouseEvent eSmart = new MouseEvent(
              (Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
              iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
          if (smartIcon.onMouseMoved(eSmart)){
            String tip = smartIcon.getLocalTooltipText();
            if (tip != null && ! tip.equals(tabbedPane.getToolTipTextAt(targetTab))){
              tabbedPane.setToolTipTextAt(targetTab, tip);
            }
           tabbedPane.revalidate(); 
           tabbedPane.repaint(); 
//            tabbedPane.repaint(r.x, r.y, r.width, r.height); 
          }
          movedIcon = smartIcon;
          movedTab = targetTab;
        } else { // in tab, but not on icon
          if (movedIcon != null){
            // trigger a mouseExit from the movedIcon
            Rectangle prevRect = tabbedPane.getBoundsAt(movedTab);
            iPoint = convertPointToIcon(prevRect, p, movedIcon);
            MouseEvent eSmart = new MouseEvent(
                (Component) e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(),
                iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
            if (movedIcon.onMouseExited(eSmart)){
              String tip = movedIcon.getLocalTooltipText();
              if (tip != null && ! tip.equals(tabbedPane.getToolTipTextAt(targetTab))){
                tabbedPane.setToolTipTextAt(targetTab, tip);
              }

           tabbedPane.revalidate(); 
           tabbedPane.repaint(); 
//            tabbedPane.repaint(prevRect.x, prevRect.y, prevRect.width, prevRect.height); 
            }
          }          
          movedIcon = null;
          movedTab = -1;
        } 
      } else { // not a smart icon ? 
        if (movedIcon != null){
          // trigger a mouseExit from the movedIcon
          Rectangle prevRect = tabbedPane.getBoundsAt(movedTab);
          Point iPoint = convertPointToIcon(prevRect, p, movedIcon);
          MouseEvent eSmart = new MouseEvent(
              (Component) e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(),
              iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
          if (movedIcon.onMouseExited(eSmart)){
            String tip = movedIcon.getLocalTooltipText();
            if (tip != null && ! tip.equals(tabbedPane.getToolTipTextAt(targetTab))){
              tabbedPane.setToolTipTextAt(targetTab, tip);
            }

           tabbedPane.revalidate(); 
           tabbedPane.repaint(); 
//          tabbedPane.repaint(prevRect.x, prevRect.y, prevRect.width, prevRect.height); 
          }
          movedIcon = null;
          movedTab = -1;
        }
      } 
    } else { // not on a tab
      if (movedIcon != null){
        // trigger a mouseExit from the movedIcon
        Rectangle prevRect = tabbedPane.getBoundsAt(movedTab);
        Point iPoint = convertPointToIcon(prevRect, p, movedIcon);
        MouseEvent eSmart = new MouseEvent(
            (Component) e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(),
            iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
        if (movedIcon.onMouseExited(eSmart)){
          String tip = movedIcon.getLocalTooltipText();
          if (tip != null && ! tip.equals(tabbedPane.getToolTipTextAt(movedTab))){
            tabbedPane.setToolTipTextAt(movedTab, tip);
          }
          
           tabbedPane.revalidate(); 
           tabbedPane.repaint(); 
//        tabbedPane.repaint(prevRect.x, prevRect.y, prevRect.width, prevRect.height); 
        }
        movedIcon = null;
        movedTab = -1;
      }
    }     

  }

  /** MouseListener implementation, use to track mouse behaviour inside the tab selector bounds
   * and forward them to the appropriate smart icon.
   */
  public void mouseExited(MouseEvent e) {
    checkTabCount();
    if (movedIcon != null){
      Point p = e.getPoint();
      // trigger a mouseExit from the movedIcon
      Rectangle prevRect = tabbedPane.getBoundsAt(movedTab);
      Point iPoint = convertPointToIcon(prevRect, p, movedIcon);
      MouseEvent eSmart = new MouseEvent(
          (Component) e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(),
          iPoint.x, iPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
      if (movedIcon.onMouseExited(eSmart)){
        String tip = movedIcon.getLocalTooltipText();
        if (tip != null && ! tip.equals(tabbedPane.getToolTipTextAt(movedTab))){
          tabbedPane.setToolTipTextAt(movedTab, tip);
        }
        
        tabbedPane.repaint(prevRect.x, prevRect.y, prevRect.width, prevRect.height);
      }
      movedIcon = null;
      movedTab = -1;      
    }
    
  }

  /** MouseListener implementation, not used.
   */
  public void mouseEntered(MouseEvent e) {
  }

  /** MouseMotionListener implementation, not used.
   */
  public void mouseDragged(MouseEvent e) {
  }

  /** MouseListener implementation, not used.
   */
  public void mouseClicked(MouseEvent e) {
  }
  
}
