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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;

/** The default UI for the toolbar gripper.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class ToolBarGripperUI extends ComponentUI{
  
  /** the "grip" (an alpha blended dot image, 3*3 pixels, hightlighted underneath ) */
  protected static Image gripImage = new ImageIcon(ToolBarGripperUI.class.getResource("gripper.png")).getImage();
  
  protected static Image gripExpandHImage = new ImageIcon(ToolBarGripperUI.class.getResource("grip_expand_h.png")).getImage();
  protected static Image gripExpandVImage = new ImageIcon(ToolBarGripperUI.class.getResource("grip_expand_v.png")).getImage();
  
  
  /** Constructs a new gripper UI */
  public ToolBarGripperUI() {
  }
  
  public static ComponentUI createUI(JComponent c) {
    return new ToolBarGripperUI();
  }
   
  /** installs the UI and sets the preferred size of the gripper */
  public void installUI(JComponent comp){
    super.installUI(comp);   
    comp.setPreferredSize(new Dimension(4,4));
  }

  /** Paints gripper dots  */
  public void paint(Graphics g, JComponent comp){
    ToolBarGripper gripper = (ToolBarGripper)comp;
    Insets insets = gripper.getInsets();
    if (gripper.getOrientation() == SwingConstants.HORIZONTAL){
      int dots = (gripper.getHeight())/4 -1;
      int dotsSize = dots * 4;
      int top = insets.top + gripper.getHeight()/2 - dotsSize/2;
      int centerX = insets.left + (gripper.getWidth() - insets.left - insets.right) / 2;
      for (int i=0; i < dots; i++){
         g.drawImage(gripImage, centerX-1 , top+ i*4+1, null);
      }
      if (gripper.isCollapsed()){
        int centerY = gripper.getHeight()/2;
        g.drawImage(gripExpandHImage, 0, centerY - 4, null);        
      }
    } else {      
      int dots = (gripper.getWidth())/4-1;
      for (int i=0; i < dots; i++){
         g.drawImage(gripImage, insets.left+ i*4+2,insets.top, null);
      }
      if (gripper.isCollapsed()){
        int centerX = gripper.getWidth() / 2;        
        g.drawImage(gripExpandVImage, centerX - 4, 0, null);        
      }
    }
    super.paint(g, comp);
  }
  
  
}
