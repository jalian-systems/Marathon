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

import javax.swing.border.*;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.*;
import javax.swing.*;

/** A border using a shadow on right-bottom sides.
 *
 * <p>
 *  Two types of border can be drawn : full shadow border and half shadow border.
 * <ul>
 *  <li> full shadow border paints darker lines on top and left to define a rectangular shape
 * (this is the kind of border used by DockableContainers (DockViews).
 *  <liI> half shadow border paints only a shadow (nothing on top and right) : it is used for
 * DockedTabbedPane contents.
 * </ul>
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class ShadowBorder implements Border {
  private static Insets INSETS_FULL = new Insets(3, 2, 5, 5);
  private static Insets INSETS_HALF = new Insets(1, 1, 5, 5);

  private static Image shadowImage = new ImageIcon(ShadowBorder.class.getResource("shadow.png")).getImage();
  static int shadowW = shadowImage.getWidth(null);
  static int shadowH = shadowImage.getHeight(null);

  private Color highlight = UIManager.getColor("VLDocking.highlight");
  private Color shadow = UIManager.getColor("VLDocking.shadow");

  private boolean paintTopLeft = true;
  private Insets insets;

  /* Constructs a full shadow border */
  public ShadowBorder() {
    this(true);
  }

  /** Constructs a full shadow border (if paintTopLeft is true) or a half shadow border
   * (is false).
   * */
  public ShadowBorder(boolean paintTopLeft) {
    this.paintTopLeft = paintTopLeft;
    insets = paintTopLeft? INSETS_FULL : INSETS_HALF;
  }

  public boolean isBorderOpaque() {
    return false;
  }

  public void paintBorder(Component component, Graphics graphics, int x,
      int y, int w, int h) {
    // top corner
     graphics.drawImage(shadowImage,
         x+ w -5, y, x+w, y+5, shadowW-5,0, shadowW, 5, null);
     // vertical
     graphics.drawImage(shadowImage,
         x+ w -5, y+5, x+w, y+h-5, shadowW-5,5, shadowW, shadowH-5, null);
     // bottom-right corner
     graphics.drawImage(shadowImage,
         x+ w -5, y+h-5, x+w, y+h, shadowW-5,shadowH-5, shadowW, shadowH, null);
     // horizontal
     graphics.drawImage(shadowImage,
         x+5, y+h-5, x+w-5, y+h, 5,shadowH-5, shadowW-5, shadowH, null);
     // bottom left corner
     graphics.drawImage(shadowImage,
             x, y+h-5, x+5, y+h,
             0, shadowH-5, 5, shadowH, null);
     if (paintTopLeft){
       graphics.setColor(highlight);
       graphics.drawRect(x + 1, y + 2, w - 7, 1);
//       graphics.drawRect(x + 1, y + 2, 1, h - 7);
       graphics.drawLine(x + 1, y + 2, x+1, y + h - 5);
       graphics.setColor(shadow);
       graphics.drawRect(x, y + 1, w - 5, h - 6);
     }
  }

  public Insets getBorderInsets(Component component) {
    return insets;
  }
}
