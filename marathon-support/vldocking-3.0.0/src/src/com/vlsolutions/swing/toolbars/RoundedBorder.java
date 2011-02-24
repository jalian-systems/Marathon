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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** A simple rounded border. 
 *
 * Can be used anywhere a rounded border is needed (no specific purpose).
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class RoundedBorder implements Border {

  private Insets insets = new Insets(2,2,2,2);
  private Color shadow = UIManager.getColor("controlShadow");

  public RoundedBorder() {
  }

  public boolean isBorderOpaque() {
    return false;
  }

  public void paintBorder(Component component, Graphics graphics, int x,
      int y, int w, int h) {
    graphics.setColor(shadow);
    graphics.drawRoundRect(x,y, w-1, h-1, 9, 9);
     
  }

  public Insets getBorderInsets(Component component) {
    return insets;
  }
}
