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
import java.awt.event.*;
import java.beans.*;

/** A Swing panel used as a toolbar for autohide buttons.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 *  */
public class AutoHideButtonPanel extends JPanel  {

  /** The property name associated with the "border" property  */
  public static final String PROPERTY_BORDERZONE = "borderzone";
  
  public static final String uiClassID = "AutoHideButtonPanelUI";

  /** <=> Zone  */
  private int border;

  /** highlight effect (rollover) on the contained buttons */
  private ButtonHighlighter buttonHighlighter = new ButtonHighlighter();

  /** expansion management  */
  private AutoHideExpandPanel expandPanel;

  /** Constructs a new AutoHideButtonPanel, with a shared expandPanel, and for
   * a specified border zone.
   *
   * @param border constant taken from AutoHideButtonPanel.TOP, AutoHideButtonPanel.LEFT,
   * AutoHideButtonPanel.BOTTOM, AutoHideButtonPanel.RIGHT
   * */
  public AutoHideButtonPanel(AutoHideExpandPanel expandPanel, int border) {
    this.border = border;
    this.expandPanel = expandPanel;
    boolean isHorizontal = (border == DockingConstants.INT_HIDE_TOP)
        || (border == DockingConstants.INT_HIDE_BOTTOM);
    setLayout(new AutoHideBorderLayout(isHorizontal));
    
    firePropertyChange("borderzone", -1, border);


  }
  
  /** Returns the number of buttons currently displayed by this panel.
   */
  public int getVisibleButtonCount(){
    Component [] comps = getComponents();
    int count = 0;
    for (int i=0; i < comps.length; i++){
      if (comps[i].isVisible()){
        count ++;
      }
    }
    return count;
  }

  /** Returns the border this panel is for.
   * <p>
   * Values are : AutoHideButtonPanel.TOP, AutoHideButtonPanel.LEFT,
   * AutoHideButtonPanel.BOTTOM, AutoHideButtonPanel.RIGHT
   */
  public int getBorderZone(){
    return border;
  }

  /** Adds a new AutoHideButton.
   * */
  public void add(AutoHideButton btn){
    add((Component)btn);

    btn.addMouseListener(buttonHighlighter);
    btn.addMouseListener(expandPanel.getControler());
  }

  /** Removes an AutoHideButton */
  public void remove(AutoHideButton btn){
    super.remove(btn);
    btn.removeMouseListener(buttonHighlighter);

  }

  private class ButtonHighlighter extends MouseAdapter {
    Color highlight = UIManager.getColor("VLDocking.highlight");
    public void mouseEntered(MouseEvent e){
      AutoHideButton btn = (AutoHideButton) e.getSource();
      if (! btn.isSelected()){ // selected buttons have their own pain style
        btn.setBackground(highlight);          
        btn.setOpaque(true);
        btn.repaint();
      }
    }

    public void mouseExited(MouseEvent e){
      AutoHideButton btn = (AutoHideButton) e.getSource();
      if (! btn.isSelected()){ // selected buttons have their own pain style
        btn.setOpaque(false);
        btn.repaint();
      }
    }
  }
  
  public String getUIClassID() {
    return uiClassID;
  }
  

}
