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

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

/** The UI of the "detached" (floating) dockview.
 *
 * @see com.vlsolutions.swing.docking.DockView
 * @see com.vlsolutions.swing.docking.DetachedDockView
 * @author Lilian Chamontin, VLSolutions
 * @since 2.0
 */
public class DetachedDockViewUI extends BasicPanelUI {
 /* Note that this is not a subclass of DockViewUI, (to avoid border management problems due to ancestor listener) */
  
  private static DetachedDockViewUI instance = new DetachedDockViewUI();
  private Color highlight = UIManager.getColor("VLDocking.highlight");

  
  public DetachedDockViewUI() {
  }
  
  /**  Creates a UI for the given component (shared instance) */
  public static ComponentUI createUI(JComponent c) {
    return instance;
  }
  
  /** Installs the component's UI */
  public void installUI(JComponent c) {
    super.installUI(c);
    c.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    /*    //BorderFactory.createCompoundBorder(
        //BorderFactory.createEmptyBorder(1,0,0,0), 
        BorderFactory.createMatteBorder(0, 1,1,1, Color.GRAY));
     */
  }
  
  /** Uninstalls the component's UI */
  public void uninstallUI(JComponent c){
    super.uninstallUI(c);
  }

  
}
