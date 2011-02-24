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

import com.vlsolutions.swing.docking.AutoHideButtonPanel;
import com.vlsolutions.swing.docking.DockingConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

/** The UI associated with the AutoHideButtonPanel.
 * <p> 
 * This UI uses the following properties :
 * <ul>
 * <li> UIManager.getBorder("AutoHideButtonPanel.topBorder")
 * <li> UIManager.getBorder("AutoHideButtonPanel.bottomBorder")
 * <li> UIManager.getBorder("AutoHideButtonPanel.leftBorder")
 * <li> UIManager.getBorder("AutoHideButtonPanel.rightBorder")
 * </ul>
 *<p> 
 * These borders are used accordingly to the border zone (TOP, LEFT..) of the used
 * AutoHideButtonPanel.
 *
 * @see DockingUISettings 
 * @author Lilian Chamontin, VLSolutions
 *
 */
public class AutoHideButtonPanelUI extends BasicPanelUI implements PropertyChangeListener {
  
  private static AutoHideButtonPanelUI instance = new AutoHideButtonPanelUI();
  
  
  public static ComponentUI createUI(JComponent c) {
    return instance;
  }
  
  public AutoHideButtonPanelUI() {
  }
  
  public void installUI(JComponent comp){
    super.installUI(comp);    
    installBorder((AutoHideButtonPanel)comp);
    comp.addPropertyChangeListener(AutoHideButtonPanel.PROPERTY_BORDERZONE, this);
  }
  
  public void uninstallUI(JComponent comp){
    super.uninstallUI(comp);
    comp.setBorder(null);
    comp.removePropertyChangeListener(this);
  }

  protected void installBorder(AutoHideButtonPanel btnPanel){
    switch (btnPanel.getBorderZone()){
      case DockingConstants.INT_HIDE_TOP :
        btnPanel.setBorder(UIManager.getBorder("AutoHideButtonPanel.topBorder"));
        break;
      case DockingConstants.INT_HIDE_BOTTOM :
        btnPanel.setBorder(UIManager.getBorder("AutoHideButtonPanel.bottomBorder"));
        break;
      case DockingConstants.INT_HIDE_LEFT:
        btnPanel.setBorder(UIManager.getBorder("AutoHideButtonPanel.leftBorder"));
        break;
      case DockingConstants.INT_HIDE_RIGHT:
        btnPanel.setBorder(UIManager.getBorder("AutoHideButtonPanel.rightBorder"));
        break;
    }
  }

  public void propertyChange(PropertyChangeEvent e) {
    installBorder((AutoHideButtonPanel) e.getSource());
  }

  
}
