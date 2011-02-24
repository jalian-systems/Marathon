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


package com.vlsolutions.swing.docking.ui;

import com.vlsolutions.swing.docking.AutoHideButton;
import com.vlsolutions.swing.docking.DockingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

/** The UI delegate for AutoHideButtons.
 *<p>
 * This UI uses the following properties :
 * <ul>
 * <li> UIManager.getBorder("AutoHideButton.expandBorderTop")
 * <li> UIManager.getBorder("AutoHideButton.expandBorderLeft")
 * <li> UIManager.getBorder("AutoHideButton.expandBorderBottom")
 * <li> UIManager.getBorder("AutoHideButton.expandBorderRight")
 * </ul>
 * <p>
 * Feel free to provide different borders they will be used accordingly to the 
 * positionning of the button (TOP, LEFT..)
 *
 * @see DockingUISettings
 * @author Lilian Chamontin, VLSolutions
 */
public class AutoHideButtonUI extends BasicLabelUI implements PropertyChangeListener {
  
  protected static AutoHideButtonUI instance = new AutoHideButtonUI();
  

  public AutoHideButtonUI() {
  }
  
  /**
   * Creates a new DockingSplitPaneUI instance
   */
  public static ComponentUI createUI(JComponent x) {
    return instance;
  }

  public void installUI(JComponent comp){
    super.installUI(comp);
    
    AutoHideButton btn = (AutoHideButton) comp;
    
    installBorder(btn);
    
    btn.addPropertyChangeListener(AutoHideButton.PROPERTY_ZONE, this);
    
    //Font f = UIManager.getFont("MenuItem.font");
    Font f = UIManager.getFont("AutoHideButton.font");
    if (f != null) {
      btn.setFont(f);
    } else {
      btn.setFont(new Font("Dialog", Font.PLAIN, 10));
    }
  }
  
  public void uninstallUI(JComponent comp){
    super.uninstallUI(comp);    
    comp.setBorder(null);
    comp.removePropertyChangeListener(AutoHideButton.PROPERTY_ZONE, this);    
  }
  
  public void propertyChange(PropertyChangeEvent e){
    if (e.getPropertyName().equals(AutoHideButton.PROPERTY_ZONE)){
      installBorder((AutoHideButton)e.getSource());
    } else {
      super.propertyChange(e);
    }
  }
  
  protected void installBorder(AutoHideButton btn){
    switch (btn.getZone()){
      case DockingConstants.INT_HIDE_TOP:
        btn.setBorder(UIManager.getBorder("AutoHideButton.expandBorderTop"));
        break;
      case DockingConstants.INT_HIDE_LEFT:
        btn.setBorder(UIManager.getBorder("AutoHideButton.expandBorderLeft"));
        break;
      case DockingConstants.INT_HIDE_BOTTOM:
        btn.setBorder(UIManager.getBorder("AutoHideButton.expandBorderBottom"));
        break;
      case DockingConstants.INT_HIDE_RIGHT:
        btn.setBorder(UIManager.getBorder("AutoHideButton.expandBorderRight"));
        break;
    }
  }



  
  /** Overriden to paint properly the button on vertical sides.
   */
  public void paint(Graphics g, JComponent comp){
    AutoHideButton btn = (AutoHideButton) comp;
    int zone = btn.getZone();
    if (zone == DockingConstants.INT_HIDE_TOP || zone == DockingConstants.INT_SPLIT_BOTTOM){
      super.paint(g, comp);
    } else {
      // vertical button : we have to rely on a custom paint
      if (btn.isOpaque()) {
        g.setColor(btn.getBackground());
        g.fillRect(0, 0, btn.getWidth(), btn.getHeight());
      }
      
      double pid2 = Math.PI / 2d;
      Graphics2D g2 = ( (Graphics2D) g.create());
      g2.setFont(btn.getFont());
      g2.setColor(btn.getForeground());
      FontMetrics fm = btn.getFontMetrics(btn.getFont());
      Icon icon = btn.getIcon();
      Insets i = btn.getInsets();
      
      String text = btn.getText(); // 2005/07/12 added text != null controls
      
      if (zone == DockingConstants.INT_HIDE_LEFT) {
        g2.translate(0, btn.getHeight());
        g2.rotate( -pid2);
        if (icon != null) {
          icon.paintIcon(btn, g2, i.bottom, i.left);
          if (text != null){
            g2.drawString(text,
                i.bottom + icon.getIconWidth() + btn.getIconTextGap(),
                i.left + btn.getWidth() / 2 +
                fm.getAscent() / 2 /*       fm.getAscent()*/);
          }
        } else {
          if (text != null){
            g2.drawString(text, i.bottom,
                i.left + btn.getWidth() / 2 + fm.getAscent() / 2);
          }
        }
      } else {
        g2.translate(btn.getWidth(), 0);
        g2.rotate(pid2);
        if (icon != null) {
          icon.paintIcon(btn, g2, 1, 1);
          if (text != null){
            g2.drawString(text, i.top + icon.getIconWidth() + btn.getIconTextGap(),
                btn.getWidth() / 2 + fm.getAscent() / 2);
          }
        } else {
          if (text != null){
            g2.drawString(text, i.top,
                btn.getWidth() / 2 + fm.getAscent() / 2);
          }
        }
      }
    }
  }
}

