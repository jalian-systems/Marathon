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
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.PropertyChangeEvent;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Point;
import java.util.*;

/** A title bar, associated to a DockView (container of a single user component).
 * <p>
 * Here is an example of a title bar : <img src="doc-files/titlebar.gif">.
 * <p>
 * DockViewTitleBar is able to display the following properties of a DockKey :
 * <ul>
 * <li> Icon
 * <li> Name
 * <li> Tooltip
 * <li> Notification (blinking background).
 * <li> And some buttons if their corresponding Action contains the
 * </ul>
 * <p>
 * This title bar supports buttons used for docking features :
 * <table border="1"><tr><th> function </th><th>Version 1.1</th><th>Version 2.0</th></tr>
 *<tr><td>maximize</td><td><img src="doc-files/maximize16.gif"></td><td><img src="doc-files/maximize16v2rollover.png"></td></tr>
 *<tr><td>restore</td><td><img src="doc-files/restore16.gif"></td><td><img src="doc-files/restore16v2rollover.png"></td></tr>
 *<tr><td>hide</td><td><img src="doc-files/hide16.gif"></td><td><img src="doc-files/hide16v2rollover.png"></td></tr>
 *<tr><td>dock</td><td><img src="doc-files/dock16.gif"></td><td><img src="doc-files/dock16v2rollover.png"></td></tr>
 *<tr><td>close</td><td><img src="doc-files/close16.gif"></td><td><img src="doc-files/close16v2rollover.png"></td></tr>
 *<tr><td>float (detach)</td><td>n/a </td><td><img src="doc-files/float16v2rollover.png"></td></tr>
 *<tr><td>attach</td><td>n/a </td><td><img src="doc-files/attach16v2rollover.png"></td></tr>
 * </table>
 *
 * <p>
 * The buttons managed have no effect on the state of the dockable : they just fire
 * property change events, and it is the responsibility of the DockableContainer to
 * listen to those events and to relay the operation to the docking desktop.
 * <p>
 * Note : the UI Delegate of the DockViewTitleBar is the {@link com.vlsolutions.swing.docking.ui.DockViewTitleBarUI}
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class DockViewTitleBar extends JPanel implements DockableDragSource {
  
  /** Property name designating the autohide button selection */
  public static final String PROPERTY_AUTOHIDE = "DockTitle.AUTOHIDE";
  
  /** Property name designating the close button selection */
  public static final String PROPERTY_CLOSED = "DockTitle.CLOSED";
  
  /** Property name designating a drag gesture beginning */
  public static final String PROPERTY_DRAGGED = "DockTitle.DRAGGED";
  
  /** Property name designating the maximized button selection */
  public static final String PROPERTY_MAXIMIZED = "DockTitle.MAXIMIZED";
  
  /** Property name designating the float button selection */
  public static final String PROPERTY_FLOAT = "DockTitle.FLOAT";
  
  
  private static final String uiClassID = "DockViewTitleBarUI";
  
  private static final String CLOSE_TEXT = UIManager.getString("DockViewTitleBar.closeButtonText");
  private static final String ICONIFY_TEXT = UIManager.getString("DockViewTitleBar.minimizeButtonText");
  private static final String RESTORE_TEXT = UIManager.getString("DockViewTitleBar.restoreButtonText");
  private static final String MAXIMIZE_TEXT = UIManager.getString("DockViewTitleBar.maximizeButtonText");
  private static final String FLOAT_TEXT = UIManager.getString("DockViewTitleBar.floatButtonText");
  private static final String ATTACH_TEXT = UIManager.getString("DockViewTitleBar.attachButtonText");
  
  private static Icon closeIcon = UIManager.getIcon("DockViewTitleBar.menu.close");
  private static Icon maximizeIcon = UIManager.getIcon("DockViewTitleBar.menu.maximize");
  private static Icon restoreIcon = UIManager.getIcon("DockViewTitleBar.menu.restore");
  private static Icon hideIcon = UIManager.getIcon("DockViewTitleBar.menu.hide");
  private static Icon dockIcon = UIManager.getIcon("DockViewTitleBar.menu.dock");
  private static Icon floatIcon = UIManager.getIcon("DockViewTitleBar.menu.float");
  private static Icon attachIcon = UIManager.getIcon("DockViewTitleBar.menu.attach");
  
  // ------------ member fields --------------
  
  // this creation tricks to manage the UI class requiring access to the components
  // before they are created
  private JLabel titleLabel = getTitleLabel();
  
  private JButton closeButton = getCloseButton();
  private JButton dockButton = getHideOrDockButton();
  private JButton maximizeButton = getMaximizeOrRestoreButton();
  private JButton floatButton = getFloatButton();
  private boolean active;
  
  private ActionListener actionListener = new ActionListener(){
    public void actionPerformed(ActionEvent e){
      if (e.getActionCommand().equals("dock")){
        dockAction();
      } else if (e.getActionCommand().equals("close")){
        closeAction();
      } else if (e.getActionCommand().equals("maximize")){
        maximizeAction();
      } else if (e.getActionCommand().equals("float")){
        floatAction();
      }
    }
  };
  
  
  private Dockable target; // the component this title is for
  
  /** used to have a state of blinking (notification) */
  private boolean isNotification = false;
  
  private JPopupMenu currentPopUp = null;
  
  /** current blinking count (the limit to the notification timer) */
  private int blinkCount = 0;
  
  private int MAX_BLINKS = UIManager.getInt("DockingDesktop.notificationBlinkCount");
  
  /** reacts to single and double click on title bar */
  private MouseListener titleMouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        maximizeAction();
      } else {
        //requestFocus();
        // replaced by a better focus behaviour for 1.2
        target.getComponent().requestFocus();
      }
    }
    public void mousePressed(MouseEvent e){
      if (e.isPopupTrigger()){
        checkForPopUp(e);
      }
    }
    public void mouseReleased(MouseEvent e){
      if (e.isPopupTrigger()){
        checkForPopUp(e);
      }
    }
    
    
  };
  
  
  /** listen to the key changes */
  private PropertyChangeListener dockKeyListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent e) {
      String pName = e.getPropertyName();
      if (pName.equals(DockKey.PROPERTY_ICON)) {
        titleLabel.setIcon( (Icon) e.getNewValue());
      } else if (pName.equals(DockKey.PROPERTY_NAME)) {
        titleLabel.setText( (String) e.getNewValue());
        revalidate();
      } else if (pName.equals(DockKey.PROPERTY_TOOLTIP)) {
        setToolTipText( (String) e.getNewValue());
      } else if (pName.equals(DockKey.PROPERTY_NOTIFICATION)) {
        // attract user attention
        boolean isOn = ( (Boolean) e.getNewValue()).booleanValue();
        if (isOn && !isActive()) {
          if (notificationTimer == null) {
            notificationTimer = new javax.swing.Timer(1000,
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
              }
            });
          }
          notificationTimer.restart();
        } else {
          if (notificationTimer != null) {
            notificationTimer.stop();
            blinkCount = 0;
          }
          setNotification(false);
        }
      }
      /*else if (pName.equals(DockKey.PROPERTY_DOCKABLE_STATE)){
       *
        int newState = ((Integer)e.getNewValue()).intValue();
        int oldState = ((Integer)e.getOldValue()).intValue();
        switch (newState){
          case DockableState.HIDDEN :
            setAutoHide(true);
            break;
          case DockableState.MAXIMIZED :
            setMaximized(true);
            break;
          case DockableState.DOCKED :
            if (oldState == DockableState.HIDDEN){
              setAutoHide(false);
            }
            break;
          default:
            //@todo : managed the floatable state
            // nothing to track here
        }
      }*/
    }
  };
  
  /** Timer used to trigger repaint event for notification (blinking title bar)  */
  private javax.swing.Timer notificationTimer;
  
  /** singleton for keyboard management */
  private static FocusHighlighter focusHighlighter = new FocusHighlighter(); //2005/11/10

  private DockingDesktop desktop;
  
  
  /** Constructs an empty title bar (no dockable yet associated).
   */
  public DockViewTitleBar() {
    this(null);
  }
  
  /** Constructs a title bar for the specified dockable.
   *<p>
   * Warning : a DockViewTitleBar can be used with multiple dockables (this is the case 
   * for example in autohide borders, where a single titlebar is shared by all hidden dockables
   * (shown only when one is expanding).
   */
  public DockViewTitleBar(Dockable dockable) {
    setDockable(dockable);
    
    closeButton.setText("");
    dockButton.setText("");
    maximizeButton.setText("");
    closeButton.setActionCommand("close");
    dockButton.setActionCommand("dock");
    maximizeButton.setActionCommand("maximize");
    floatButton.setActionCommand("float");
    
    closeButton.addActionListener(actionListener);
    dockButton.addActionListener(actionListener);
    maximizeButton.addActionListener(actionListener);
    floatButton.addActionListener(actionListener);
    
    this.addMouseListener(titleMouseListener);
    
    //maximizeButton.addMouseListener(new ButtonRolloverEffect());
    
  }

  /** Notification of completion of layout.
   * <p> This hook can be used to insert customized buttons without otherwise
   * having to fully replace the UI delegate
   */
  public void finishLayout() {
        
  }
  
  /** Returns the desktop associated to this title bar, if one has been registered 
   * with #installDocking(DockingDesktop), or null.
   */
  public DockingDesktop getDesktop(){
    return desktop;
  }
  
  
  /** Overriden as a means to unregister internal listeners, do not call directly */
  public void removeNotify(){
    super.removeNotify();        
/*    KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(
        focusHighlighter);  2005/11/10: now it's shared between title bars*/
    if (target != null){
      DockKey k = target.getDockKey();
      if (k!= null){
        k.removePropertyChangeListener(dockKeyListener);//2007/04/16
      }      
    }
    
  }
  
  
  /** Returns the label used to display the dockkey name.
   *<p> Shouldn't be used to update the title : the best way is to update the
   * DockKey (property listener ensure the labels and buttons stay in sync).
   *
   *@since 2.0
   */
  public JLabel getTitleLabel(){
    if (titleLabel == null){
      titleLabel = new JLabel();
    }
    return titleLabel;
  }
  
  
  public JButton getCloseButton(){
    if (closeButton == null){
      closeButton = new JButton();
    }
    return closeButton;
  }
  
  /** returns the button used for hiding or docking the view.
   * <p>
   * As hiding and docking are mutually exclusive, the same button is used for both purposes
   */
  public JButton getHideOrDockButton(){
    if (dockButton == null){
      dockButton = new JButton();
    }
    return dockButton;
  }
  
  /** returns the button used for maximizing or restoring the view.
   * <p>
   * As those operations are mutually exclusive, the same button is used for both purposes
   */
  public JButton getMaximizeOrRestoreButton(){
    if (maximizeButton == null){
      maximizeButton = new JButton();
    }
    return maximizeButton;
  }
  
  /** returns the button used for floating (detach) the view.
   */
  public JButton getFloatButton(){
    if (floatButton == null){
      floatButton = new JButton();
    }
    return floatButton;
  }
  
  
  private void setNotification(boolean notification) {
    boolean old = this.isNotification;
    this.isNotification = notification;
    firePropertyChange("titlebar.notification", old, notification);
  }
  
  private boolean isAutoHide(){
    return target.getDockKey().getLocation() == DockableState.Location.HIDDEN;
  }
  private boolean isMaximized(){
    return target.getDockKey().getLocation() == DockableState.Location.MAXIMIZED;
  }
  
  private void dockAction() {
    boolean old = isAutoHide();
    firePropertyChange(PROPERTY_AUTOHIDE, old, !old);
  }
  
  private void maximizeAction() {
    // we use the visible property of the button to check if maximization
    // is enabled or not
    if (target.getDockKey().getLocation() == DockableState.Location.FLOATING){
      // ignore : floating cannot be maximized
      return;
    }
    if (target.getDockKey().isMaximizeEnabled()){
    //if (maximizeButton.isVisible()){ //2008/04/17
      boolean old = isMaximized();
      // we don't really change the maximized property,
      // which will be updated if the desktop accepts this maximization
      /* We do not use fireVetoableChange, to keep maximization processing outside
       * the title bar (everything is driven par the dockingdesktop and the
       * SingleDockableContainer
       */
      firePropertyChange(PROPERTY_MAXIMIZED, old, !old);
    }
  }
  
  private void closeAction() {
    firePropertyChange(PROPERTY_CLOSED, false, true);
  }
  
  private void floatAction() {
    if (target.getDockKey().getLocation() == DockableState.Location.FLOATING){
      firePropertyChange(PROPERTY_FLOAT, true, false);
    } else {
      firePropertyChange(PROPERTY_FLOAT, false, true);
    }
  }
  
  
  /** This method is invoked to hide the pop-up that could still be visible
   * (To avoid a visible pop-up for an invisible component)
   */
  public void closePopUp(){
    if (currentPopUp != null){
      currentPopUp.setVisible(false);
      // @todo uncertain about correct gc of the pop-up in that case
      currentPopUp = null;
    }
  }
  
  
  /** {@inheritDoc}
   * @since 2.0
   */
  public String getUIClassID() {
    return uiClassID;
  }
  
  
  /** Returns true if the dockable is the currently active one.
   * <p>
   * There is at most one active dockable for a dekstop, and it there is one,
   * it is the one which contains the keybord focused component.
   * */
  public boolean isActive() {
    return active;
  }
  
  /** Updates the active property.
   * A title bar is active when the dockable it is for is ancestor of
   * the keybord focused component.
   *   */
  public void setActive(boolean active) {
    boolean old = this.active;
    this.active = active;
    firePropertyChange("active", old, active);
  }
  
  /** Changes the dockable this title bar is for */
  public void setDockable(Dockable dockable) {
    Dockable old = target;
    if (target != null && target != dockable) {
      target.getDockKey().removePropertyChangeListener(dockKeyListener);
    }
    if (dockable != null) {
      this.target = dockable;
  
      DockKey key = dockable.getDockKey();
      titleLabel.setText(key.getName());
      titleLabel.setIcon(key.getIcon());
      setToolTipText(key.getTooltip());
      revalidate();
      
      /*dockButton.setVisible(key.isAutoHideEnabled());
       
      this.autoHide = key.getLocation() == DockableState.HIDDEN;
       
      setDockButtonAsAutoHide(autoHide);
       
      closeButton.setVisible(key.isCloseEnabled());
      maximizeButton.setVisible(key.isMaximizeEnabled() && !autoHide);*/
      
      key.addPropertyChangeListener(dockKeyListener);
      
    }
    firePropertyChange("dockable", old, dockable);
  }
  
  
  
  
  /** {@inheritDoc} */
  public Dockable getDockable() {
    return target;
  }
  
  /** {@inheritDoc} */
  public boolean startDragComponent(Point p) {
    // disable DnD for some cases : 
    // - child of a compound dockable, in hidden state
    // - child of a maximized compund dockable
    // - maximized dockable
    DockableState.Location targetLocation = target.getDockKey().getLocation();
    if (targetLocation == DockableState.Location.HIDDEN){
      if (DockingUtilities.isChildOfCompoundDockable(target)){
        // nested hidden dockables cannot be drag and dropped
        return false;
      }
    } else if (targetLocation == DockableState.Location.DOCKED){
      boolean isChildOfMaximizedContainer = false;      
      if (desktop != null){
          Dockable max = desktop.getMaximizedDockable();
          if (max != null && max.getComponent().getParent().isAncestorOf(this)){
            isChildOfMaximizedContainer = true;
          }
      }
      if (isChildOfMaximizedContainer){
        return false;
      }
    } else if (targetLocation == DockableState.Location.MAXIMIZED){
      return false;
    }
    
    // notify our listeners that drag has begun
    firePropertyChange(PROPERTY_DRAGGED, false, true);
    return true;
  }
  
  /** Returns a readeable String representing this title bar */
  public String toString() {
    if (target != null) {
      return "DockViewTitleBar of [" + target.getDockKey() + "]";
    } else {
      return "DockViewTitleBar";
    }
  }
  private JMenuItem createPopUpItem(String text, Icon icon, String tooltip, String actionCommand, KeyStroke accelerator){
    JMenuItem menuItem = new JMenuItem(text, icon);
    menuItem.setActionCommand(actionCommand);
    menuItem.addActionListener(actionListener);
    if (accelerator != null){
      menuItem.setAccelerator(accelerator);
    }
    return menuItem;
  }
  
  private void checkForPopUp(MouseEvent e){
    JPopupMenu popup = new JPopupMenu(target.getDockKey().getName());
    // first add the standard menu
    DockKey key = target.getDockKey();
    switch (key.getLocation()){
      case DOCKED:
        initDockedPopUp(popup);
        break;
      case HIDDEN:
        initAutoHidePopUp(popup);
        break;
      case MAXIMIZED:
        initMaximizedPopUp(popup);
        break;
      case FLOATING:
        initFloatingPopUp(popup);
        break;
      default:
        // nothing to do
    }
    
    DockableActionCustomizer customizer = target.getDockKey().getActionCustomizer();
    if (customizer != null && customizer.isSingleDockableTitleBarPopUpCustomizer()){
      if (popup.getComponentCount() > 0){
        popup.addSeparator();
      }
      customizer.visitSingleDockableTitleBarPopUp(popup, target);
    }
    if (popup.getComponentCount() > 0){
      popup.show(DockViewTitleBar.this, e.getX(), e.getY());
      this.currentPopUp = popup;
    }
  }
  
  /** Init the popup displayed as the title bar contextual menu */
  protected void initMaximizedPopUp(JPopupMenu popup){
    
    popup.add(createPopUpItem(RESTORE_TEXT, restoreIcon, RESTORE_TEXT,
        "maximize", (KeyStroke)UIManager.get("DockingDesktop.maximizeActionAccelerator") ));
  }
  
  protected void initAutoHidePopUp(JPopupMenu popup){
    if (DockingUtilities.isChildOfCompoundDockable(target)){
      // restore option not allowed for children of a compound dockable
    } else {      
      popup.add(createPopUpItem(RESTORE_TEXT, dockIcon, RESTORE_TEXT,
          "dock", (KeyStroke)UIManager.get("DockingDesktop.dockActionAccelerator")));
    }
    if (target.getDockKey().isCloseEnabled()){
      popup.add(createPopUpItem(CLOSE_TEXT, closeIcon, CLOSE_TEXT,
          "close", (KeyStroke)UIManager.get("DockingDesktop.closeActionAccelerator")));
    }
  }
  
  protected void initDockedPopUp(JPopupMenu popup){
    DockKey key = target.getDockKey();
    if (key.isAutoHideEnabled()){
      popup.add(createPopUpItem(ICONIFY_TEXT, hideIcon, ICONIFY_TEXT,
          "dock", (KeyStroke)UIManager.get("DockingDesktop.dockActionAccelerator")));
    }
    if (key.isFloatEnabled()){
      popup.add(createPopUpItem(FLOAT_TEXT, floatIcon, FLOAT_TEXT,
          "float", (KeyStroke)UIManager.get("DockingDesktop.floatActionAccelerator")));
    }
    if (key.isMaximizeEnabled()){
      popup.add(createPopUpItem(MAXIMIZE_TEXT, maximizeIcon, MAXIMIZE_TEXT,
          "maximize", (KeyStroke)UIManager.get("DockingDesktop.maximizeActionAccelerator")));
    }
    if (key.isCloseEnabled()){
      popup.add(createPopUpItem(CLOSE_TEXT, closeIcon, CLOSE_TEXT,
          "close", (KeyStroke)UIManager.get("DockingDesktop.closeActionAccelerator")));
    }
  }
  
  /** Init the popup displayed as the title bar contextual menu */
  protected void initFloatingPopUp(JPopupMenu popup){
    if (DockingUtilities.isChildOfCompoundDockable(target)){
      // attach option not allowed for children of a compound dockable
    } else {
      popup.add(createPopUpItem(ATTACH_TEXT, attachIcon, ATTACH_TEXT,
          "float", (KeyStroke)UIManager.get("DockingDesktop.floatActionAccelerator")));
    }
  }
  
  /** Returns the container of the dockable's component */
  public Container getDockableContainer() {
    // easy
    return getParent();
  }

  public void installDocking(DockingDesktop desktop) {
    this.desktop = desktop;
  }
  
  public void uninstallDocking(DockingDesktop desktop){
    //System.out.println("uninstallDocking TITLE on " + target.getDockKey());
    this.desktop = null;
    setUI(null); //2007/11/14
  }

  /** notifies the source when the drag operation has ended (by a drop or cancelled) 
   * @since 2.1.3
   */ 
  public void endDragComponent(boolean dropped){
    // nothing more to do
  }
  
  
  /* used to highlight the title bar when its parent is ancestor of the
   *   focused component
   */
  private static class FocusHighlighter implements PropertyChangeListener {
    private DockViewTitleBar activeTitleBar;
    
    FocusHighlighter(){
      // this is a singleton so we register here for keyboard focus events properties
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
          "focusOwner", this);
    }
    
    
    // focusOwner
    public void propertyChange(PropertyChangeEvent e) {
      Component c = (Component) e.getNewValue();
      Component ancestor = c;
      // is the focus contained in a single dockable container
      while(ancestor != null && !(ancestor instanceof SingleDockableContainer) ){
        ancestor = ancestor.getParent();
      }
      if (ancestor != null){
        if (ancestor instanceof DockView){
          DockView view = (DockView) ancestor;
          DockViewTitleBar tb = view.getTitleBar();
          if (tb == activeTitleBar){
            // no view change in focus
          } else if (tb != null){
            if (activeTitleBar != null){
              activeTitleBar.setActive(false);
            }
            tb.setActive(true);
            // reset notification (blinking)
            Dockable target = tb.target;
            if (target != null && target.getDockKey() != null){ //2007/02/27 fixed NPE
              target.getDockKey().setNotification(false);
            }
          } else { // tb == null && tb != activeTitleBar
            activeTitleBar.setActive(false);
          }
          activeTitleBar = tb;
        } else if (ancestor instanceof AutoHideExpandPanel){
          DockViewTitleBar tb = ((AutoHideExpandPanel)ancestor).getTitleBar();
          if (tb == activeTitleBar){
            // no view change in focus
          } else if (tb != null){
            if (activeTitleBar != null){
              activeTitleBar.setActive(false);
            }
            tb.setActive(true);
            // reset notification (blinking)
            tb.target.getDockKey().setNotification(false);
          } else { // tb == null && tb != activeTitleBar
            activeTitleBar.setActive(false);
          }
          activeTitleBar = tb;
        } else {
          if (activeTitleBar != null){
            activeTitleBar.setActive(false);
          }
          activeTitleBar = null;
        }
      } else {
        if (activeTitleBar != null){
          activeTitleBar.setActive(false);
        }
        activeTitleBar = null;
      }
      
      
/*      // try to find if the component taking the focus is the ancestor
      if (activeTitleBar != null){
      Container parent = activeTitleBar.getParent();
      if (parent != null && c != null && parent.isAncestorOf(c)) {
        if (!active) {
          setActive(true);
        }
        // reset notification (blinking)
        target.getDockKey().setNotification(false);
      } else if (active) {
        setActive(false);
      }
 */
    }
  }
  
  
  
}
