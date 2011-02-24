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
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/** A custom Icon to have a nice rollover effect for toolbar icons.
 * <p> 
 * This icon uses a gray version of the provided image and paints it under the original one.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class RolloverIcon implements Icon{ 
    private int shadowWidth = 1; 
    private int shadowHeight = 1; 
    private Icon icon, shadowIcon; 
 
    public RolloverIcon(Icon icon){ 
        this.icon = icon; 
        shadowIcon = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)icon).getImage())); 
    } 
 
    public int getIconHeight(){ 
        return icon.getIconWidth(); 
    } 
 
    public int getIconWidth(){ 
        return icon.getIconHeight(); 
    } 
 
    public void paintIcon(Component c, Graphics g, int x, int y){ 
      Color highlight = UIManager.getColor("VLDocking.highlight");
      Color shadow = UIManager.getColor("VLDocking.shadow");
      g.setColor(highlight);
      g.fillRect(x, y, getIconWidth(), getIconHeight());
      shadowIcon.paintIcon(c, g, x + shadowWidth-1, y + shadowHeight-1); 
      icon.paintIcon(c, g, x-1,  y-1); 
    } 


  
}
