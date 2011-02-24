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


package com.vlsolutions.swing.tabbedpane;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/** An Icon allowing the use of closeable tabs by simulating the entire tabpane tab-painting.
 *
 *<p>
 * As JTabbedPanes cannot use any JComponents as tab selectors (the access if protected and we just have
 * a label, an icon and a tooltip), we have to rely on tricks to bypass them.
 *
 *
 *
 * @author Lilian Chamontin, VLSolutions
 * @update 2005/11/01 Lilian Chamontin : fixed NPE when otherIcons == null (tabs without smart icons)
 * @update 2005/11/08 Lilian Chamontin : fixed bug related to event management when multiple other-icons are used
 * (big thanks to Emmanuel GAUVRIT).
 * @update 2005/11/21 Lilian Chamontin : enhanced width calculation of the icon size.
 */
public class JTabbedPaneSmartIcon implements Icon, Cloneable {
  /* Implementation : This icon is larger than standard icons : it also paints the tab title and optional buttons.
   * Inner Event management (with JTabbedPaneSmartIconManager) allows simulation of action on the buttons.
   *
   */
  
  /** The traditional tab selector icon */
  private Icon icon;
  
  /** calculated used width (icon + label + other icons) + gaps */
  private int width;
  
  /** x location where the other icons are drawn*/
  private int otherIconsOffset;
  
  /** tab height */
  private int height;
  
  /** the traditional tab selector label */
  private String label;
  
  /** the traditional tab selector tooltip text*/
  private String tooltipText;
  
  /** local tooltip text : the one to use during mouse movements (depends on the mouse position,
   * and can be either the tab selector tooltip text or one of the smart buttons included into this icon).
   */
  private String localTooltipText;
  
  // list of additional icons (presented as buttons)
  private SmartIconJButton [] otherIcons;
  
  /** currently pressed inner button */
  private SmartIconJButton pressedButton;
  
  /** currently rolled-over inner button */
  private SmartIconJButton rolloverButton;
  
  
  /** gap between the icon and the text*/
  private int textIconGap;
  
  /** gap between the text and the following icons*/
  private int otherIconsGap;
  
  private boolean antialiased = false;
  
  /* The container this icon is for (required to calculate proper widths and heights) */
  private JTabbedPane container;
  
  private int inBetweenOtherIconsGap;
  
  /** Constructs a new smart icon with a given set of additional buttons */
  public JTabbedPaneSmartIcon(Icon icon, String label, SmartIconJButton [] otherIcons) {
    this.icon = icon;
    this.label = label;
    this.otherIcons = otherIcons;
    
    this.textIconGap = UIManager.getInt("TabbedPane.textIconGap");
    this.otherIconsGap = UIManager.getInt("TabbedPane.otherIconsGap");
    this.inBetweenOtherIconsGap = UIManager.getInt("TabbedPane.inBetweenOtherIconsGap");
    invalidateSize();
    try {
      //mimic the unofficial aa settings of Swing
      this.antialiased = "true".equals(System.getProperty("swing.aatext"));
    } catch (SecurityException ignore){ // for untrusted web start apps failing gracefully
    }
    
  }
  
  public SmartIconJButton getSmartButton(int index){
    return otherIcons[index];
  }
  
  public void setSmartButton(int index, SmartIconJButton btn){
    otherIcons[index] = btn;
    invalidateSize();
  }
  
  
  /** Creates a shalow copy of this icon */
  public JTabbedPaneSmartIcon copy(){
    return (JTabbedPaneSmartIcon) clone();
  }
  
  /** {@inheritDoc} */
  public Object clone(){
    try {
      return super.clone();
    } catch (CloneNotSupportedException ignore){
      ignore.printStackTrace();
      return null;
    }
  }
  
  /** Update the tooltip of this icon */
  public void setTooltipText(String tooltip){
    this.tooltipText = tooltip;
  }
  
  /** Return the tooltip of this icon */
  public String getTooltipText(){
    return this.tooltipText;
  }
  
  /** Return the local tooltip of this icon (the one associated with inner mouse movements)*/
  public String getLocalTooltipText(){
    return this.localTooltipText;
  }
  
  /** Update the label to be displayed on the tab */
  public void setLabel(String label){
    this.label = label;
    invalidateSize();
  }
  
  /** Return the tab label */
  public String getLabel(){
    return this.label;
  }
  
  /** Update the main icon (left) to be displayed on the tab */
  public void setIcon(Icon icon){
    this.icon = icon;
    invalidateSize();
  }
  
  /** Return the tab icon */
  public Icon getIcon(){
    return this.icon;
  }
  
  private void invalidateSize(){
    this.width = this.height = -1;
  }
  
  /** Specify which container will use this icon.
   * <p> If the icon is shared between containers, please provide at least one as
   * this allows the icon to properly estimate its dimension.
   */
  public void setIconForTabbedPane(JTabbedPane container){
    this.container = container;
  }
  
  
  /** paints the icon (and the associated label and sub-icons) */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g;
    Object renderingValue = null;
    if (this.antialiased){
      renderingValue = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    if (icon != null){
      icon.paintIcon(c, g, x, y);
    }
    
    Font f = UIManager.getFont("JTabbedPaneSmartIcon.font"); // 2006/01/23
    if (f != null){
      g.setFont(f);
    }
    
    // reevaluate the width with correct graphics object, in case something has changed
    FontMetrics fm = g.getFontMetrics();
    int iconsOffset = otherIconsOffset;
    
    if (icon != null){
      g.drawString(label, x+icon.getIconWidth() + textIconGap, y + height - fm.getDescent());
    } else {
      g.drawString(label, x, y + height - fm.getDescent());
    }
    if (otherIcons != null){    //2005/11/01
      for (int i=0; i < otherIcons.length; i++){
        otherIcons[i].paintIcon(c, g, x + iconsOffset, y + height / 2 - otherIcons[i].getIconHeight()/2);
        iconsOffset += otherIcons[i].getIconWidth();
        if (i < otherIcons.length-1){
          iconsOffset+= inBetweenOtherIconsGap;
        }
      }
    }
    
    if (antialiased){
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingValue);
    }
    
  }
  
  
  private SmartIconJButton findButtonAt(Point p){
    int start = otherIconsOffset;
    if (otherIcons != null){    //2005/11/01
      for (int i=0; i < otherIcons.length; i++){
        SmartIconJButton btn = otherIcons[i];
        if (p.x >= start && p.x < start + btn.getIconWidth()){
          if (p.y >= height/2 - btn.getIconHeight() /2
              && p.y < height/2 + btn.getIconHeight() /2){
            return btn;
          } else {
            return null;
          }
        }
        start += btn.getIconWidth(); // bug corrected thanks to Emmanuel GAUVRIT 2005/11/08
        if (i < otherIcons.length-1){
          start += inBetweenOtherIconsGap;
        }
      }
    }
    return null;
  }
  
  /** Returns the width of this icon */
  public int getIconWidth() {
    if (width == -1){
      if (container == null){
        throw new NullPointerException("container for this smart icon not specified with setIconForTabbedPane()");
      }
      // reevaluate the width with correct graphics object, in case something has changed
      //Font f = UIManager.getFont("DockTabbedPane.font"); // 2006/01/23
      Font f = UIManager.getFont("JTabbedPaneSmartIcon.font"); // 2006/01/23
      FontMetrics fm = container.getFontMetrics(f);
      this.width = 0;
      if (icon != null){
        width = icon.getIconWidth();
      }
      width += textIconGap +  fm.stringWidth(label) + otherIconsGap;
      
      this.otherIconsOffset = width;
      int iconsOffset = otherIconsOffset;
      if (otherIcons != null){
        for (int i=0; i < otherIcons.length; i++){
          width += otherIcons[i].getIconWidth(); // additional width for icons
          if (i < otherIcons.length-1){
            width += inBetweenOtherIconsGap;
          }
        }
      }
    }
    return width;
    
  }
  
  /** Returns the height of this icon */
  public int getIconHeight() {
    if (height == -1){
      if (icon != null){
        height = icon.getIconHeight();
      } else {
        height = 16; // standard height (as if there was an icon) : should be calculated instead of fixed
      }
    }
    return height;
  }
  
  /** Process the mouse pressed event.
   *<p>
   * Mouse coordinates are given relative to this icon
   */
  public boolean onMousePressed(MouseEvent e){
    // find the icon under the point
    SmartIconJButton btn = findButtonAt(e.getPoint());
    if (btn != null && btn.isEnabled()){
      btn.setPressed(true);
      pressedButton = btn;
      return true;
    }
    return false;
  }
  
  /** Process the mouse released event.
   *<p>
   * Mouse coordinates are given relative to this icon
   */
  public boolean onMouseReleased(final MouseEvent e){
    if (pressedButton != null && pressedButton.isEnabled()){
      pressedButton.setPressed(false);
      final SmartIconJButton btn = findButtonAt(e.getPoint());
      if (btn == pressedButton){
        btn.fireAction(new ActionEvent(e.getSource(),
            ActionEvent.ACTION_PERFORMED, "" ));
        return true;
      }
    }
    return false;
  }
  
  /** Process the mouse exited event.
   *<p>
   * Mouse coordinates are given relative to this icon
   */
  public boolean onMouseExited(MouseEvent e){
    // reset all rollover states
    if (rolloverButton != null){
      rolloverButton.setRollover(false);
      rolloverButton = null;
      localTooltipText = tooltipText;
      return true;
    }
    rolloverButton = null;
    return false;
  }
  
  
  /** Process the mouse moved event.
   *<p>
   * Mouse coordinates are given relative to this icon
   */
  public boolean onMouseMoved(MouseEvent e){
    // check for a rollover effect
    SmartIconJButton btn = findButtonAt(e.getPoint());
    boolean shouldRepaint = false;
    if (btn != null){
      String tip = btn.getTooltipText();
      if (tip != null){
        if (!tip.equals(localTooltipText)){
          this.localTooltipText = tip;
          shouldRepaint = true;
        }
      } else if (localTooltipText != tooltipText){
        this.localTooltipText = tooltipText;
        shouldRepaint = true;
      }
      if (btn == rolloverButton){
        // still on the same button
      } else {
        // another button
        if (rolloverButton != null){
          rolloverButton.setRollover(false);
        }
        rolloverButton = btn;
        rolloverButton.setRollover(true);
        shouldRepaint = true;
      }
    } else if (rolloverButton != null){
      rolloverButton.setRollover(false);
      rolloverButton = null;
      localTooltipText = tooltipText;
      shouldRepaint = true;
    }
    
    return shouldRepaint;
  }
  
}
