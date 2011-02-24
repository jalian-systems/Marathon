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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/** A sub component of JTabbedPaneSmartIcon, used to describe a button included in a tabbedpane.
 *<p>
 * As JTabbedPanes cannot use any JComponents as tab selectors (the access if protected and we just have
 * a label, an icon and a tooltip), we have to rely on tricks to bypass them.
 *<p>
 * This trick, the SmartIconJButton is an icon faking the behaviour of a button. It uses an Action 
 * for reacting to clicks and manages a set of images (default, rollover, pressed, disabled) to 
 * behave like a rollover button.
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class SmartIconJButton implements Icon, PropertyChangeListener {
  
  /** The action triggered when the clicks on the icon */
  protected Action action;
  
  /** the default icon (enabled/visible)*/
  protected Icon defaultIcon;

  /** the icon for the rollover effect*/
  protected Icon rolloverIcon;

  /** the icno for the pressed effect */
  protected Icon pressedIcon;
  
  /** the icon for the disabled effect */
  protected Icon disabledIcon;
  
  /** the tooltip associated with the button */
  protected String tooltipText;
  
  /* internal state variables*/
  private boolean isRollover, isPressed, isEnabled, isVisible;
  
  /** Constructs a new button with an action.
   *
   * The button is enabled and visible.
   */
  public SmartIconJButton(Action action) {
    this.action = action;
    defaultIcon = (Icon) action.getValue(AbstractAction.SMALL_ICON);
    tooltipText = (String) action.getValue(AbstractAction.SHORT_DESCRIPTION);
    isVisible = true;
    isEnabled = action.isEnabled();
    action.addPropertyChangeListener(this);    
  }

  /** Update the default icon property */
  public void setIcon(Icon icon){
    this.defaultIcon = icon;
  }
  
  /** Returns the default icon */
  public Icon getIcon(){
    return defaultIcon;
  }
  /** Update the rollover icon property */
  public void setRolloverIcon(Icon icon){
    this.rolloverIcon = icon;
  }

  /** Returns the rollover icon */
  public Icon getRolloverIcon(){
    return rolloverIcon;
  }
  
  /** Update the pressed icon property */
  public void setPressedIcon(Icon icon){
    this.pressedIcon = icon;
  }
  
  /** Returns the pressed icon */
  public Icon getPressedIcon(){
    return pressedIcon;
  }
  
  /** Update the disabled icon property */
  public void setDisabledIcon(Icon icon){
    this.disabledIcon = icon;
  }
  
  /** Returns the disabled icon */
  public Icon getDisabledIcon(){
    return disabledIcon;
  }

  /** Update the rollover state */
  public void setRollover(boolean isRollover){
    this.isRollover = isRollover;
  }

  /** Update the enabled state */
  public void setEnabled(boolean isEnabled){
    this.isEnabled = isEnabled;
  }
  
  /** Update the visible state */
  public void setVisible(boolean isVisible){
    this.isVisible = isVisible;
  }
  
  /** Update the pressed state */
  public void setPressed(boolean isPressed){
    this.isPressed = isPressed;
  }
  
  /** Returns the rollover state*/
  public boolean isRollover(){
    return isRollover;
  }

  /** Returns the pressed state*/
  public boolean isPressed(){
    return isPressed;
  }
  
  /** Returns the enabled state*/
  public boolean isEnabled(){
    return isEnabled;
  }
 
  /** Returns the visible state*/
  public boolean isVisible(){
    return isVisible;
  }

  /** paints the appropriate icon according to its internal state (pressed, rollover...)
   */
  public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
    if (! isVisible){
      return;
    } else if (isEnabled){
      if (isPressed){
        if (pressedIcon != null){
          pressedIcon.paintIcon(c, g, x, y);
        } else {
          defaultIcon.paintIcon(c, g, x+1, y+1);
        }
      } else if (isRollover){
        if (rolloverIcon != null){
          rolloverIcon.paintIcon(c, g, x, y);
        } else {
          defaultIcon.paintIcon(c, g, x-1, y-1); // "push" effect
        }
      } else { // just the default
        if (defaultIcon != null){
           defaultIcon.paintIcon(c, g, x, y);
        }
      }
    } else { // disabled
      if (disabledIcon == null){
        disabledIcon = createDisabledIcon();
      }
      disabledIcon.paintIcon(c,g, x, y);
    }
  }
  
  private Icon createDisabledIcon(){
    if (defaultIcon instanceof ImageIcon){
      Image i = GrayFilter.createDisabledImage (((ImageIcon)defaultIcon).getImage());
      return new ImageIcon(i);
    } else {
      BufferedImage bi = new BufferedImage(defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics g = bi.createGraphics();
      g.setColor(new Color(0,0,0,0));
      g.fillRect(0,0, defaultIcon.getIconWidth(), defaultIcon.getIconHeight());
      defaultIcon.paintIcon(null, g, 0,0);
      g.dispose();
      Image i = GrayFilter.createDisabledImage(bi);
      return new ImageIcon(i);
    }
  }

  public int getIconWidth() {
    int w = 0;
    if (defaultIcon != null){
      w = Math.max(w, defaultIcon.getIconWidth());
    }
    if (rolloverIcon != null){
      w = Math.max(w, rolloverIcon.getIconWidth());
    }
    if (pressedIcon != null){
      w = Math.max(w, pressedIcon.getIconWidth());
    }
    return w;
  }

  public int getIconHeight() {
    int h = 0;
    if (defaultIcon != null){
      h = Math.max(h, defaultIcon.getIconHeight());
    }
    if (rolloverIcon != null){
      h = Math.max(h, rolloverIcon.getIconHeight());
    }
    if (pressedIcon != null){
      h = Math.max(h, pressedIcon.getIconHeight());
    }
    return h;
  }
  
  /** triggers the associated action */
  public void fireAction(ActionEvent e){    
    action.actionPerformed(e);
  }
  
  public String getTooltipText(){
    return tooltipText;
  }
  
  public void setTooltipText(String tooltip){
    this.tooltipText = tooltip;
  }

  /** Do not call directly as it a side effect of listening to the action changes.
   */
  public void propertyChange(PropertyChangeEvent evt) {
    // track changes in the Action 
    String prop = evt.getPropertyName();
    if (prop.equals(AbstractAction.SHORT_DESCRIPTION)){
      setTooltipText((String)evt.getNewValue());
    } else if (prop.equals(AbstractAction.SMALL_ICON)){
      setIcon((Icon) evt.getNewValue());
    } else if (prop.equals("enabled")){
      setEnabled(((Boolean)evt.getNewValue()).booleanValue());
    }
  }
  
}
