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

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Color;
import java.beans.*;

/** The button used to show Dockables in auto-hide borders.
 * <p>
 * This component displays the label, tooltip and icon properties of a
 * <code>Dockable</code>'s DockKey, with a custom look and feel.
 *
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class AutoHideButton extends JLabel {
  
  /** property name associated to the "zone" (border) property */
  public static final String PROPERTY_ZONE = "autohide.zone";
  
  public static final String uiClassID = "AutoHideButtonUI";


  private boolean selected = false;
  private DockKey key;
  private Dockable dockable;


  private int zone; // the border used

  private boolean isNotification = false;
  private Timer notificationTimer; // blinking timer
  private int blinkCount = 0;
  private int MAX_BLINKS = UIManager.getInt("DockingDesktop.notificationBlinkCount");
  

  private PropertyChangeListener keyListener =  new PropertyChangeListener(){
    // this is a DockKey listener, not a KeyEvent listener...
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(DockKey.PROPERTY_NAME)){
        setText((String)e.getNewValue());
      } else if (e.getPropertyName().equals(DockKey.PROPERTY_TOOLTIP)){
        setToolTipText((String)e.getNewValue());
      } else if (e.getPropertyName().equals(DockKey.PROPERTY_ICON)){
        setIcon((Icon) e.getNewValue());
      } else if (e.getPropertyName().equals(DockKey.PROPERTY_NOTIFICATION)){
        boolean isOn = ((Boolean)e.getNewValue()).booleanValue();
        if (isOn){

          if (notificationTimer == null){
           notificationTimer = new Timer(1000,
                new ActionListener() {
              public void actionPerformed(ActionEvent actionEvent) {
                setNotification(!isNotification);
                if (!isNotification){
                  blinkCount ++;
                  if (blinkCount >= MAX_BLINKS){
                    blinkCount = 0;
                    notificationTimer.stop(); // enough blinking
                  }
                }
                repaint();
              }
            });
          }

          setNotification(true);
          notificationTimer.restart();
        } else {
          if (notificationTimer != null){
            notificationTimer.stop();
            blinkCount = 0;
          }
          setNotification(false);
        }
      }
    }
  };

  public AutoHideButton() {
  }

  /** Sets the background color according to the <code>notification</code> boolean */
  private void setNotification(boolean notification){
    this.isNotification = notification;
    if (notification){
      setBackground(UIManager.getColor("DockingDesktop.notificationColor")); 
      setOpaque(true);
    } else {
      setOpaque(false);
    }
    repaint();
  }

  /** Returns the dockable this button is for */
  public Dockable getDockable(){
    return dockable;
  }

  /** Returns the key of the dockable */
  public DockKey getKey(){
    return key;
  }


  /** Initialize the button for a Dockable and a border zone  */
  public void init(Dockable dockable, int zone){
    DockKey key = dockable.getDockKey();
    this.dockable = dockable;
    this.key = key;

    int oldZone = this.zone;
    this.zone = zone;    
    firePropertyChange("autohide.zone", oldZone, zone);

    key.addPropertyChangeListener(keyListener);

    setText(key.getName());
    setToolTipText(key.getTooltip());
    if (key.getIcon() != null) {
      setIcon(key.getIcon());
    }

    setFocusable(true);

    setOpaque(false);

    setIconTextGap(4);
    setAlignmentY(1);
  }


  public Dimension getPreferredSize() {
    if (zone == DockingConstants.INT_HIDE_TOP
        || zone == DockingConstants.INT_HIDE_BOTTOM) {
      return super.getPreferredSize();
    } else {
      Dimension d = super.getPreferredSize();
      Insets i = getInsets();
      return new Dimension(d.height - i.top - i.bottom + i.left + i.right,
          d.width - i.left - i.right + i.top + i.bottom); // rotate the shape
    }
  }

  public Dimension getMaximumSize() {
    if (zone == DockingConstants.INT_HIDE_TOP
        || zone == DockingConstants.INT_HIDE_BOTTOM) {
      return super.getMaximumSize();
    } else {
      Insets i = getInsets();
      Dimension d = super.getMaximumSize();
      return new Dimension(d.height - i.top - i.bottom + i.left + i.right,
          d.width - i.left - i.right + i.top + i.bottom); // rotate the shape
    }
  }

  public Dimension getMinimumSize() {
    if (zone == DockingConstants.INT_HIDE_TOP
        || zone == DockingConstants.INT_HIDE_BOTTOM) {
      return super.getMinimumSize();
    } else {
      Insets i = getInsets();
      Dimension d = super.getMinimumSize();
      return new Dimension(d.height - i.top - i.bottom + i.left + i.right,
          d.width - i.left - i.right + i.top + i.bottom); // rotate the shape
    }
  }

  

  /** Returns the border zone of this button*/
  public int getZone(){
    return zone;
  }

  /** True when the button is currently selected. */
  public boolean isSelected(){
    return selected;
  }

  /** Selects or unselects the button */
  public void setSelected(boolean selected){
    this.selected = selected;
    setOpaque(selected);
    repaint();
    if (selected){
      key.setNotification(false); // in case we were in notification mode
    }
  }

  public String getUIClassID() {
    return uiClassID;
  }
  



}
