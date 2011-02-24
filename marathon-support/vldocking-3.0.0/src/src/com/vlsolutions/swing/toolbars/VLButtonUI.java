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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;


/** A UI for toolbar buttons : replaces the look and feel default UI to
 * have a unified toolbar rendering.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class VLButtonUI extends BasicToggleButtonUI {
  private Color highlight = UIManager.getColor("VLDocking.highlight");
  
  
  public VLButtonUI() {
  }
  
  
  public void paint(Graphics g, JComponent comp){
    AbstractButton btn = (AbstractButton) comp;
    boolean rollover = btn.getModel().isRollover();
    boolean selected = btn.getModel().isSelected();
    boolean armed = btn.getModel().isArmed();
    btn.setBorderPainted(selected || rollover);
    if (rollover || selected){
      if (armed){
        g.translate(1,1);
      } else {
        Insets i = btn.getInsets();
        if (! selected){ // avoid too much effects
          g.setColor(highlight);
          g.fillRect(1,1, btn.getWidth() - 2, btn.getHeight() - 2);
        }
      }
    }
    
    Border b = comp.getBorder();
    if (b instanceof ToolBarButtonBorder){
      ((ToolBarButtonBorder)b).setPressed(selected || armed);
    }
    
    super.paint(g, comp);
  }
  
}