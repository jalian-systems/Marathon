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
import java.awt.Image;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.Border;


/** A border suitable for toolbar buttons.
 *<p>
 * Paints a nice button effect with rounded corners.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * @since 2.0
 */
public class ToolBarButtonBorder  implements Border {

  private boolean pressed;
  
  public ToolBarButtonBorder() {
  }
  

  private static Image borderImage = new ImageIcon(ToolBarButtonBorder.class.getResource("toolbarbuttonborder.png")).getImage();
  
  private static Image pressedBorderImage = new ImageIcon(ToolBarButtonBorder.class.getResource("toolbarbuttonborder_pressed.png")).getImage();
  
  static int borderWidth = borderImage.getWidth(null);
  static int borderHeight = borderImage.getHeight(null);

  private boolean paintTopLeft = true;
  private Insets insets = new Insets(2, 2, 2, 2);

  public boolean isBorderOpaque() {
    return false;
  }

  public void paintBorder(Component component, Graphics graphics, int x,
      int y, int w, int h) {
    Image img;
    if (pressed){
      img = pressedBorderImage;
    } else {
      img = borderImage;
    }
     // top right corner
     graphics.drawImage(img,
         x+ w -5, y, x+w, y+5, borderWidth-5,0, borderWidth, 5, null);
     // vertical right
     graphics.drawImage(img,
         x+ w -5, y+5, x+w, y+h-5, borderWidth-5,5, borderWidth, borderHeight-5, null);
     // bottom-right corner
     graphics.drawImage(img,
         x+ w -5, y+h-5, x+w, y+h, borderWidth-5,borderHeight-5, borderWidth, borderHeight, null);
     
     // horizontal bottom
     graphics.drawImage(img,
         x+5, y+h-5, x+w-5, y+h, 5,borderHeight-5, borderWidth-5, borderHeight, null);
     // bottom left corner
     graphics.drawImage(img,
             x, y+h-5, x+5, y+h,
             0, borderHeight-5, 5, borderHeight, null);

     // horizontal top
     graphics.drawImage(img,
         x+5, y, x+w-5, y+5, 5,0, borderHeight-5, 5, null);
     // vertical left
     graphics.drawImage(img,
         x, y+5, x+5, y+h-5, 0,5, 5, borderHeight-5, null);
     // top left corner
     graphics.drawImage(img,
         x, y, x+5, y+5, 0,0, 5, 5, null);
     
     
  }

  public Insets getBorderInsets(Component component) {
    if (component instanceof AbstractButton){
      AbstractButton btn = (AbstractButton) component;
      Insets i = btn.getMargin();
      i.top+= insets.top;
      i.left += insets.left;
      i.right += insets.right;
      i.bottom += insets.bottom;
      return i;
    } else {
      return insets;
    }
  }

  public boolean isPressed() {
    return pressed;
  }

  public void setPressed(boolean pressed) {
    this.pressed = pressed;
  }
}
