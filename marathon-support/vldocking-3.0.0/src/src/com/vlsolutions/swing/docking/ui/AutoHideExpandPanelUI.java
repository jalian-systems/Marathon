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

import com.vlsolutions.swing.docking.AutoHideExpandPanel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

/** The UI delegate for the AutoHideExpandPanel component.
 *<p>
 * This class is mostly a placeholder in the case the developper would like 
 * to provide a custom replacement for the expand panel look and feel.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class AutoHideExpandPanelUI extends BasicPanelUI {
  
  private static AutoHideExpandPanelUI instance = new AutoHideExpandPanelUI();
  
  public AutoHideExpandPanelUI() {
  }
  
  public static ComponentUI createUI(JComponent c) {
    return instance;
  }
  
  public void installUI(JComponent comp){
    super.installUI(comp);    
    AutoHideExpandPanel panel  = (AutoHideExpandPanel)comp;    
    panel.resetBorders();    
  }
  
  public void uninstallUI(JComponent comp){
    super.uninstallUI(comp);        
  }

  
  
}
