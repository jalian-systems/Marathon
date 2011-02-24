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

import com.vlsolutions.swing.docking.DockingPreferences;
import javax.swing.plaf.basic.*;
import javax.swing.*;
import javax.swing.plaf.*;
import java.awt.*;
import java.awt.image.*;

/** A specific UI for removing border effects of the JSplitPanes used for docking.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class DockingSplitPaneUI extends BasicSplitPaneUI{
  Color shadow = UIManager.getColor("VLDocking.shadow");
  Color highlight = UIManager.getColor("VLDocking.highlight");
  TexturePaint tp = null;

  private static boolean drawDelimitors = UIManager.getBoolean("SplitContainer.drawDotsDelimitors");
  
  private static boolean useCustomImages = UIManager.getBoolean("SplitContainer.useCustomImages");
  private static BufferedImage horizontalImage = (BufferedImage) UIManager.get("SplitContainer.hImage");
  private static BufferedImage verticalImage = (BufferedImage) UIManager.get("SplitContainer.vImage");

  private static int dividerSize = UIManager.getInt("SplitContainer.dividerSize");
  
  private static boolean isSplitResizingEnabled = UIManager.getBoolean("SplitContainer.isResizingEnabled");
  
  public DockingSplitPaneUI() {
  }

  /** if true, the UI will paint shadowed dots along the split pane divider,
   * if false, the divider will remain empty. Default is false 
   * @deprecated use UIManager.put("SplitContainer.drawDotsDelimitors", Boolean) instead
   */
  public static void setDrawDelimitors(boolean draw){
     drawDelimitors = draw;
  }

  /**
   * Creates a new DockingSplitPaneUI instance
   */
  public static ComponentUI createUI(JComponent x) {
    return new DockingSplitPaneUI();
  }

  public void installUI(JComponent comp){
    super.installUI(comp);
    ((JSplitPane)comp).setDividerSize(dividerSize);    
    comp.setEnabled(isSplitResizingEnabled);
    
  }


  protected void installDefaults(){
    super.installDefaults();
    divider.setBorder(null);    
  }



  /**
   * Creates the default divider.
   */
  public BasicSplitPaneDivider createDefaultDivider() {
    DockingSplitPaneDivider bd = new DockingSplitPaneDivider(this);
    return bd;
  }

  class DockingSplitPaneDivider extends BasicSplitPaneDivider {
    DockingSplitPaneDivider(BasicSplitPaneUI ui){
      super(ui);
    }


    public void paint(Graphics g) {
      Dimension size = getSize();
      Color bgColor = getBackground();

      if (bgColor != null) {
        g.setColor(bgColor);
        g.fillRect(0, 0, size.width, size.height);
      }

      if (useCustomImages){
        Graphics2D g2 = (Graphics2D) g.create();
        BufferedImage image = getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? verticalImage : horizontalImage;
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a texture paint from the buffered image
        Rectangle r = new Rectangle(0, 0, width, height);
        if (tp == null){
            tp = new TexturePaint(image, r);
        }
          
        if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
          g2.setPaint(tp);
          g2.fillRect(size.width / 2 - width/2, 0, width, size.height);
        } else {
          g2.setPaint(tp);
          g2.fillRect(0, size.height / 2 - height/2, size.width, height);
        }
      } else if (drawDelimitors){
        // special case : custom integrated dots delimitors
        if (tp == null) {
          // Create a buffered image texture patch of size 5x5
          BufferedImage bi = new BufferedImage(4, 4,
              BufferedImage.TYPE_INT_RGB);
          Graphics2D big = bi.createGraphics();
          // Render into the BufferedImage graphics to create the texture
          big.setColor(getBackground());
          big.fillRect(0, 0, 4, 4);
          big.setColor(highlight);
          big.fillRect(1, 1, 1, 1);
          big.setColor(shadow);
          big.fillRect(2, 2, 1, 1);

          // Create a texture paint from the buffered image
          Rectangle r = new Rectangle(0, 0, 4, 4);
          tp = new TexturePaint(bi, r);
          big.dispose();
        }

//      g.setColor(shadow);
        Graphics2D g2 = (Graphics2D) g.create();

        // Add the texture paint to the graphics context.
        g2.setPaint(tp);

        if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {

          g2.fillRect(size.width / 2 - 2, 0, 4, size.height);
        } else {
          g2.fillRect(0, size.height / 2 - 2, size.width, 4);
        }
      }

      //paintDragRectangle(g);
      super.paint(g);
    }
  }


}
