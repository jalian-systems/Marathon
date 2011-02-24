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
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** This is an optional class that can be used as a border for toolbar panels.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class ToolBarPanelBorder implements Border {
  public static final int TOP_PANEL = 0;
  public static final int LEFT_PANEL = 1;
  public static final int BOTTOM_PANEL = 2;
  public static final int RIGHT_PANEL = 3;
  
  private static Image borderImage = new ImageIcon(ToolBarButtonBorder.class.getResource("toolbarpanelborder.png")).getImage();
  static int borderWidth = borderImage.getWidth(null);
  static int borderHeight = borderImage.getHeight(null);

  private Insets insets;
  
  private int panelPosition;

  public ToolBarPanelBorder() {
    this(TOP_PANEL);
  }
  public ToolBarPanelBorder(int panelPosition) {
    this.panelPosition = panelPosition;
    switch(panelPosition){
       case TOP_PANEL:
         insets = new Insets(1, 1, 4, 1);
         break;
       case LEFT_PANEL:
         insets = new Insets(1, 1, 1, 4);
         break;
       case BOTTOM_PANEL:
         insets = new Insets(4, 1, 1, 1);
         break;
       case RIGHT_PANEL:
         insets = new Insets(1, 4, 1, 1);
         break;
    }
    
  }

  public boolean isBorderOpaque() {
    return false;
  }

  public void paintBorder(Component component, Graphics graphics, int x,
      int y, int w, int h) {

     switch (panelPosition){
       case TOP_PANEL:
         // horizontal bottom
         graphics.drawImage(borderImage,
             x, y+h-5, x+w, y+h, 5,borderHeight-5, borderWidth-5, borderHeight, null);
         // bottom-right corner
/*         graphics.drawImage(borderImage,
             x+ w -SIDE, y+h-SIDE, x+w, y+h, borderWidth-SIDE,borderHeight-SIDE, borderWidth, borderWidth, null);
         // bottom left
         graphics.drawImage(borderImage,
                 x, y+h-SIDE, x+SIDE, y+h, 0, borderHeight-SIDE, SIDE, borderHeight, null);*/
         break;
       case LEFT_PANEL:
         // vertical right
         graphics.drawImage(borderImage,
             x+ w -5, y, x+w, y+h, borderWidth-5,5, borderWidth, borderHeight-5, null);
         break;
       case BOTTOM_PANEL:
         // horizontal top
         graphics.drawImage(borderImage,
             x, y, x+w, y+5, 5,0, borderHeight-5, 5, null);
         break;
       case RIGHT_PANEL:
         // vertical left
         graphics.drawImage(borderImage,
             x, y, x+5, y+h, 0,5, 5, borderHeight-5, null);
         break;
     }
/*
     // top right corner
     graphics.drawImage(borderImage,
         x+ w -5, y, x+w, y+5, borderWidth-5,0, borderWidth, 5, null);
     // vertical right
     graphics.drawImage(borderImage,
         x+ w -5, y+5, x+w, y+h-5, borderWidth-5,5, borderWidth, borderHeight-5, null);
     // bottom-right corner
     graphics.drawImage(borderImage,
         x+ w -5, y+h-5, x+w, y+h, borderHeight-5,borderWidth-5, borderWidth, borderHeight, null);
     
     // horizontal bottom
     graphics.drawImage(borderImage,
         x+5, y+h-5, x+w-5, y+h, 5,borderHeight-5, borderWidth-5, borderHeight, null);
     // bottom left corner
     graphics.drawImage(borderImage,
             x, y+h-5, x+5, y+h,
             0, borderHeight-5, 5, borderHeight, null);

     // horizontal top
     graphics.drawImage(borderImage,
         x+5, y, x+w-5, y+5, 5,0, borderHeight-5, 5, null);
     // vertical left
     graphics.drawImage(borderImage,
         x, y+5, x+5, y+h-5, 0,5, 5, borderHeight-5, null);
     // top left corner
     graphics.drawImage(borderImage,
         x, y, x+5, y+5, 0,0, 5, 5, null);
  */   
  }

  public Insets getBorderInsets(Component component) {
    return insets;
  }
}
