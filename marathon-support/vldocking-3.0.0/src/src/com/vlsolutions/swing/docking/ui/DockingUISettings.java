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

import com.vlsolutions.swing.toolbars.ToolBarPanelBorder;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Central class to manage Look and feel settings for the docking framework.
 *<p> 
 * There are two ways of modifying the look and feel of the docking framework : 
 *<ul>
 *<li> provide a subclass of DockingUISettings and override the installXXX methods
 *<li> directly put UI properties (UIManager.put(key,value)) awaited by the desktop UI delegates.
 *  Those are described below.
 *</ul>
 *<table border="1"><tr><th>UI property</th> <th>type</th> <th>effect</th></tr>
 *<tr><td>DockView.singleDockableBorder</td> <td>Border</td> <td>border used when the DockView is docked alone (not in a tab)</td></tr>
 *<tr><td>DockView.tabbedDockableBorder</td> <td>Border</td> <td>border used when the DockView is contained in a tabbed pane</td></tr>
 *<tr><td>DockView.maximizedDockableBorder</td> <td>Border</td> <td>border used when the DockView is maxmized</td></tr>
 *<tr> <td>AutoHideButtonUI</td> <td>class name</td> <td>UI delegate for the AutoHideButton</td></tr>
 *<tr> <td>AutoHideButtonPanelUI</td> <td>class name</td> <td>UI delegate for the AutoHideButtonPanel</td></tr>
 *<tr> <td>AutoHideExpandPanelUI</td> <td>class name</td> <td>UI delegate for the AutoHideExpandPanel</td></tr>
 *<tr> <td>AutoHideButton.expandBorderTop</td> <td>Border</td> <td>Border of the autohide button when it is on top of the desktop</td></tr>
 *<tr> <td>AutoHideButton.expandBorderBottom</td> <td>Border</td> <td>Border of the autohide button when it is at bottom of the desktop</td></tr>
 *<tr> <td>AutoHideButton.expandBorderLeft</td> <td>Border</td> <td>Border of the autohide button when it is on the left of the desktop</td></tr>
 *<tr> <td>AutoHideButton.expandBorderRight</td> <td>Border</td> <td>Border of the autohide button when it is on the right of the desktop</td></tr>
 *<tr> <td>AutoHideButtonPanel.topBorder</td> <td>Border</td> <td>Border of the AutoHideButtonPanel when it is on top of the desktop</td></tr>
 *<tr> <td>AutoHideButtonPanel.bottomBorder</td> <td>Border</td> <td>Border of the AutoHideButtonPanel when it is at bottom of the desktop</td></tr>
 *<tr> <td>AutoHideButtonPanel.leftBorder</td> <td>Border</td> <td>Border of the AutoHideButtonPanel when it is on the left of the desktop</td></tr>
 *<tr> <td>AutoHideButtonPanel.rightBorder</td> <td>Border</td> <td>Border of the AutoHideButtonPanel when it is on the right of the desktop</td></tr>
 *<tr> <td>DockViewUI</td> <td>class name</td> <td>UI delegate for DockView</td></tr>
 *<tr> <td>DetachedDockViewUI</td> <td>class name</td> <td>UI delegate for DetachedDockView</td></tr>
 *<tr> <td>DockViewTitleBarUI</td> <td>class name</td> <td>UI delegate for DockViewTitleBar</td></tr>
 *<tr> <td>DockViewTitleBar.height</td> <td>int</td> <td>Height of the title bars. If set to 0, then every title bar will compute its preferred size (based on fonts and icons)</td></tr>
 *<tr> <td>DockViewTitleBar.closeButtonText</td> <td>String</td> <td>Text of the close button</td></tr>
 *<tr> <td>DockViewTitleBar.minimizeButtonText</td> <td>String</td> <td>Text of the minimize (hide) button</td></tr>
 *<tr> <td>DockViewTitleBar.maximizeButtonText</td> <td>String</td> <td>Text of the maximize button</td></tr>
 *<tr> <td>DockViewTitleBar.restoreButtonText</td> <td>String</td> <td>Text of the restore button(opposite of maximize)</td></tr>
 *<tr> <td>DockViewTitleBar.floatButtonText</td> <td>String</td> <td>Text of the float button (detach)</td></tr>
 *<tr> <td>DockViewTitleBar.attachButtonText</td> <td>String</td> <td>Text of the attach button(opposite of float)</td></tr>
 *<tr> <td>DockViewTitleBar.titleFont</td> <td>Font</td> <td>Font used by the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.isCloseButtonDisplayed</td> <td>boolean</td> <td>display or not the close button in the title bar (still accessible from pop-up menu)</td></tr>
 *<tr> <td>DockViewTitleBar.isHideButtonDisplayed</td> <td>boolean</td> <td>display or not the hide button in the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.isDockButtonDisplayed</td> <td>boolean</td> <td>display or not the dock button in the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.isMaximizeButtonDisplayed</td> <td>boolean</td> <td>display or not the maximize button in the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.isRestoreButtonDisplayed</td> <td>boolean</td> <td>display or not the restore button in the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.isFloatButtonDisplayed</td> <td>boolean</td> <td>display or not the float button in the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.isAttachButtonDisplayed</td> <td>boolean</td> <td>display or not the attach button in the title bar</td></tr>
 *<tr> <td>DockViewTitleBar.border</td> <td>Border</td> <td>Border of the title bar</td></tr>
 *<tr> <td>DockingSplitPaneUI</td> <td>class name</td> <td>UI delegate for SplitContainer component</td></tr>
 *<tr> <td>SplitContainer.dividerSize</td> <td>int</td> <td>Divider size of the split panes</td></tr>
 *<tr> <td>TabbedDockableContainer.tabPlacement</td> <td>int (SwingConstants.TOP / BOTTOM)</td> <td>Global tab style</td></tr>
 *<tr> <td>DockTabbedPane.closeButtonText</td> <td>String</td> <td>Text for the close button in tab</td></tr>
 *<tr> <td>DockTabbedPane.minimizeButtonText</td> <td>String</td> <td>Text for the minimize button in tab</td></tr>
 *<tr> <td>DockTabbedPane.restoreButtonText</td> <td>String</td> <td>Text for the restore button in tab</td></tr>
 *<tr> <td>DockTabbedPane.maximizeButtonText</td> <td>String</td> <td>Text for the maximize button in tab</td></tr>
 *<tr> <td>DockTabbedPane.floatButtonText</td> <td>String</td> <td>Text for the float button in tab</td></tr>
 *<tr> <td>DockTabbedPane.attachButtonText</td> <td>String</td> <td>Text for the attach button in tab (when floating)</td></tr>
 *<tr> <td>TabbedContainer.requestFocusOnTabSelection</td> <td>boolean</td> <td>Automatically puts focus on the selected tabbed component (default false)</td></tr>
 *<tr> <td>TabbedPane.otherIconsGap</td> <td>int</td> <td>Gap between text and close icon in closeable tab</td></tr>
 *<tr> <td>TabbedPane.inBetweenOtherIconsGap</td> <td>int</td> <td>Gap between two icons</td></tr>
 *<tr> <td>DockViewTitleBar.close</td> <td>Icon</td> <td>Icon for the close button</td></tr>
 *<tr> <td>DockViewTitleBar.close.rollover</td> <td>Icon</td> <td>Icon for the close button</td></tr>
 *<tr> <td>DockViewTitleBar.close.pressed</td> <td>Icon</td> <td>Icon for the close button</td></tr>
 *<tr> <td>DockViewTitleBar.dock</td> <td>Icon</td> <td>Icon for the dock button</td></tr>
 *<tr> <td>DockViewTitleBar.dock.rollover</td> <td>Icon</td> <td>Icon for the dock button</td></tr>
 *<tr> <td>DockViewTitleBar.dock.pressed</td> <td>Icon</td> <td>Icon for the dock button</td></tr>
 *<tr> <td>DockViewTitleBar.hide</td> <td>Icon</td> <td>Icon for the hide button</td></tr>
 *<tr> <td>DockViewTitleBar.hide.rollover</td> <td>Icon</td> <td>Icon for the hide button</td></tr>
 *<tr> <td>DockViewTitleBar.hide.pressed</td> <td>Icon</td> <td>Icon for the hide button</td></tr>
 *<tr> <td>DockViewTitleBar.maximize</td> <td>Icon</td> <td>Icon for the maximize button</td></tr>
 *<tr> <td>DockViewTitleBar.maximize.rollover</td> <td>Icon</td> <td>Icon for the maximize button</td></tr>
 *<tr> <td>DockViewTitleBar.maximize.pressed</td> <td>Icon</td> <td>Icon for the maximize button</td></tr>
 *<tr> <td>DockViewTitleBar.restore</td> <td>Icon</td> <td>Icon for the restore button</td></tr>
 *<tr> <td>DockViewTitleBar.restore.rollover</td> <td>Icon</td> <td>Icon for the restore button</td></tr>
 *<tr> <td>DockViewTitleBar.restore.pressed</td> <td>Icon</td> <td>Icon for the restore button</td></tr>
 *<tr> <td>DockViewTitleBar.float</td> <td>Icon</td> <td>Icon for the float button</td></tr>
 *<tr> <td>DockViewTitleBar.float.rollover</td> <td>Icon</td> <td>Icon for the float button</td></tr>
 *<tr> <td>DockViewTitleBar.float.pressed</td> <td>Icon</td> <td>Icon for the float button</td></tr>
 *<tr> <td>DockViewTitleBar.attach</td> <td>Icon</td> <td>Icon for the attach button</td></tr>
 *<tr> <td>DockViewTitleBar.attach.rollover</td> <td>Icon</td> <td>Icon for the attach button</td></tr>
 *<tr> <td>DockViewTitleBar.attach.pressed</td> <td>Icon</td> <td>Icon for the attach button</td></tr>
 *<tr> <td>DockViewTitleBar.menu.close</td> <td>Icon </td> <td>Icon for the close button, in pop-up menu</td></tr>
 *<tr> <td>DockViewTitleBar.menu.hide</td> <td>Icon </td> <td>Icon for the hide button, in pop-up menu</td></tr>
 *<tr> <td>DockViewTitleBar.menu.maximize</td> <td>Icon </td> <td>Icon for the maximize button, in pop-up menu</td></tr>
 *<tr> <td>DockViewTitleBar.menu.restore</td> <td>Icon </td> <td>Icon for the restore button, in pop-up menu</td></tr>
 *<tr> <td>DockViewTitleBar.menu.dock</td> <td>Icon </td> <td>Icon for the dock button, in pop-up menu</td></tr>
 *<tr> <td>DockViewTitleBar.menu.float</td> <td>Icon </td> <td>Icon for the float button, in pop-up menu</td></tr>
 *<tr> <td>DockViewTitleBar.menu.attach</td> <td>Icon </td> <td>Icon for the attach button, in pop-up menu</td></tr>
 *<tr> <td>DockTabbedPane.close</td> <td>Icon </td> <td>Icon for the close button, in tabs</td></tr>
 *<tr> <td>DockTabbedPane.close.rollover</td> <td>Icon </td> <td>Icon for the close button, in tabs</td></tr>
 *<tr> <td>DockTabbedPane.close.pressed</td> <td>Icon </td> <td>Icon for the close button, in tabs</td></tr>
 *<tr> <td>DockTabbedPane.menu.close</td> <td>Icon </td> <td>Icon for the close button, in tab pop-up menu</td></tr>
 *<tr> <td>DockTabbedPane.menu.hide</td> <td>Icon </td> <td>Icon for the hide button, in tab pop-up menu</td></tr>
 *<tr> <td>DockTabbedPane.menu.maximize</td> <td>Icon </td> <td>Icon for the maximize button, in tab pop-up menu</td></tr>
 *<tr> <td>DockTabbedPane.menu.float</td> <td>Icon </td> <td>Icon for the float button, in tab pop-up menu</td></tr>
 *<tr> <td>DockTabbedPane.menu.attach</td> <td>Icon </td> <td>Icon for the attach button, in tab pop-up menu (when floating)</td></tr>
 *<tr> <td>DockTabbedPane.menu.closeAll</td> <td>Icon </td> <td>Icon for the "close all" button, in tab pop-up menu</td></tr>
 *<tr> <td>DockTabbedPane.menu.closeAllOther</td> <td>Icon </td> <td>Icon for the "close all other" button, in tab pop-up menu</td></tr>
 *<tr> <td>DockingDesktop.closeActionAccelerator</td> <td>KeyStroke</td> <td>KeyStroke for close action (on selected dockable)</td></tr>
 *<tr> <td>DockingDesktop.maximizeActionAccelerator</td> <td>KeyStroke</td> <td>KeyStroke for maximize/restore action (on selected dockable)</td></tr>
 *<tr> <td>DockingDesktop.dockActionAccelerator</td> <td>KeyStroke</td> <td>KeyStroke for hide/dock action (on selected dockable)</td></tr>
 *<tr> <td>DockingDesktop.floatActionAccelerator</td> <td>KeyStroke</td> <td>KeyStroke for float/attach action (on selected dockable)</td></tr>
 *<tr> <td>DockingDesktop.notificationColor</td> <td>Color</td> <td>blinking color for notifications</td></tr>
 *<tr> <td>DockingDesktop.notificationBlinkCount</td> <td>int</td> <td>maximum number of blinking for notifications </td></tr>
 *<tr> <td>DragControler.stopDragCursor"</td> <td>Image</td><td>Cursor image used when a drag and drop move is not allowed</td></tr>
 *<tr> <td>DragControler.detachCursor"</td> <td>Image</td><td>Cursor image used when a drag and drop move will detach the dockable</td></tr>
 *<tr> <td>DragControler.dragCursor"</td> <td>Image</td><td>Cursor image used when a drag and drop move is allowed(not leading to a detached dockable)</td></tr>
 *<tr> <td>DragControler.swapDragCursor</td> <td>Image</td><td>Cursor image used when doing a drag and drop with Ctrl key (hot swap) </td></tr>
 *<tr> <td>DragControler.isDragAndDropEnabled</td><td>Boolean</td><td>Global switch to turn on/off drag and drop support in vldocking (default set to true)</td></tr>
 *<tr> <td>DragControler.paintBackgroundUnderDragRect</td><td>Boolean</td><td>Global switch to turn on/off background painting under drag shapes (which can be slow on some linux distributions) (default set to true)</td></tr>
 *<tr> <td>ToolBarGripperUI</td> <td>class name</td> <td>UI delegate for the toolbar "gripper"</td></tr>
 *<tr> <td>ToolBarPanel.topBorder</td> <td>Border</td> <td>Border used when a toolbar in on the top of a container</td></tr>
 *<tr> <td>ToolBarPanel.leftBorder</td> <td>Border</td> <td>Border used when a toolbar in on the left of a container</td></tr>
 *<tr> <td>ToolBarPanel.bottomBorder</td> <td>Border</td> <td>Border used when a toolbar in at the bottom of a container</td></tr>
 *<tr> <td>ToolBarPanel.rightBorder</td> <td>Border</td> <td>Border used when a toolbar in on the right of a container</td></tr>
 *<tr> <td>FloatingDialog.dialogBorder</td> <td>Border</td> <td>Border used for the FloatingDialog</td></tr>
 *<tr> <td>FloatingDialog.titleBorder</td> <td>Border</td> <td>Border used for the title (drag header) of the FloatingDialog</td></tr>
 *<tr> <td>FloatingContainer.followParentWindow</td> <td>Boolean</td> <td>if true, the floating dialogs will follow the movements of their parent window on screen</td></tr>
 *<tr> <td>FloatingContainer.paintDragShape </td> <td>Boolean</td> <td>if true, a drag outline shape will follow the mouse when dragging </td></tr>
 * </table>
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class DockingUISettings {
  static DockingUISettings instance = null;
  
  /** Field for installing settings only once */
  protected boolean isSettingsInstalled = false;

  private Color shadow = UIManager.getColor("controlShadow");
  private Color highlight = UIManager.getColor("controlLtHighlight");

  private Color darkShadow = UIManager.getColor("controlDkShadow");

  
  public DockingUISettings() {
  }
  
  /** returns the singleton instance used to store and install UI settings for the framework  */
  public static DockingUISettings getInstance(){
    // give a chance to subclassers to install their own subclass instance
    if (instance == null){
      instance = new DockingUISettings();
    }
    return instance;
  }
  
  /** Allows replacement of the settings instance (used to override global look and feel 
   * settings of the framework.
   *<p>
   * This method must be called before DockingDesktop is referenced, as the settings are statically 
   * installed at that moment.
   */
  public static void setInstance(DockingUISettings newInstance){
    instance = newInstance;
  }
  
  /** Installs the UI settings. This is executed only once, and automatically called 
   * at DockingDesktop class loading in case it was not called by the application.
   */
  public void installUI(){
    if (!isSettingsInstalled){
        installColors();
      installAutoHideSettings();    
      installBorderSettings();    
      installDockViewSettings();
      installDockViewTitleBarSettings();
      installSplitContainerSettings();
      installCloseableTabs();
      installTabbedContainerSettings();
      installIcons();
      installAccelerators();
      installDesktopSettings();
      installFloatingSettings();
      installToolBarSettings();
      isSettingsInstalled = true;
    }
  }
  
  /** Allows updating of the ui after a look and feel change.
   * <p>
   *  The Docking framework uses references of UI elements from this class to 
   *  install its UI according to the look and feel. When Laf is changed, and before
   *  calling SwingUtilities.updateComponentTreeUI(topLevelComponent), invoke updateUI() in 
   *  order to reset everything.
   * <p> 
   *  Calling this method after SwingUtilities.updateComponentTreeUI(topLevelComponent) is 
   *  unspecified (some things will be updated, others not).
   */
  public void updateUI(){
    isSettingsInstalled = false;
    installUI();
  }
  
  /** installs the borders */
  public void installBorderSettings(){
    // this is for the "flat style"  (comment this line, or put a FALSE to revert to "shadow style"

    // flat style is the default (outside : empty 1 pix / inside : hightlight-top-left + shadow-bottom-right

    Border innerFlatSingleBorder =  BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(1,1,0,0, highlight),
      BorderFactory.createMatteBorder(0,0,1,1, shadow));

    Border flatSingleBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(1,1,1,1), 
        innerFlatSingleBorder);
    UIManager.put("DockView.singleDockableBorder", flatSingleBorder);
    UIManager.put("DockView.tabbedDockableBorder", null);
    UIManager.put("DockView.maximizedDockableBorder", null);
  }
  
  /** installs the autohide related properties */
  public void installAutoHideSettings(){
      UIManager.put("AutoHideButtonUI", "com.vlsolutions.swing.docking.ui.AutoHideButtonUI");
      UIManager.put("AutoHideButtonPanelUI", "com.vlsolutions.swing.docking.ui.AutoHideButtonPanelUI");
      UIManager.put("AutoHideExpandPanelUI", "com.vlsolutions.swing.docking.ui.AutoHideExpandPanelUI");

      UIManager.put("AutoHideButton.expandBorderTop",
          BorderFactory.createCompoundBorder(
          BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(1, 1, 0, 1, shadow),
          BorderFactory.createMatteBorder(1, 1, 0, 1, highlight)),
          BorderFactory.createEmptyBorder(0, 6, 0, 6)
          ));
    UIManager.put("AutoHideButton.expandBorderBottom",
        BorderFactory.createCompoundBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 1, 1, 1, shadow),
        BorderFactory.createMatteBorder(0, 1, 1, 1, highlight)),
        BorderFactory.createEmptyBorder(0, 6, 0, 6)
        ));
    UIManager.put("AutoHideButton.expandBorderLeft",
        BorderFactory.createCompoundBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 1, 1, 0, shadow),
        BorderFactory.createMatteBorder(1, 1, 1, 0, highlight)),
        BorderFactory.createEmptyBorder(6, 0, 6, 0)
        ));
    UIManager.put("AutoHideButton.expandBorderRight",
        BorderFactory.createCompoundBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 1, 1, shadow),
        BorderFactory.createMatteBorder(1, 0, 1, 1, highlight)),
        BorderFactory.createEmptyBorder(6, 0, 6, 0)
        ));

    
    UIManager.put("AutoHideButtonPanel.topBorder",
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(1,0,0,0),
        BorderFactory.createMatteBorder(0,0,1,0, shadow) ));
    UIManager.put("AutoHideButtonPanel.bottomBorder",
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(0,0,1,0),
        BorderFactory.createMatteBorder(1,0,0,0, shadow) ));
    
    UIManager.put("AutoHideButtonPanel.leftBorder",
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(0,1,0,1),
        BorderFactory.createMatteBorder(0,0,0,1, shadow) ));
    UIManager.put("AutoHideButtonPanel.rightBorder",
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(0,1,0,1),
        BorderFactory.createMatteBorder(0,1,0,0, shadow) ));
    
    UIManager.put("AutoHideButton.font", UIManager.get("MenuItem.font")); //2006/01/23


  }
  
  /** installs the DockView related properties */
  public void installDockViewSettings(){
      UIManager.put("DockViewUI", "com.vlsolutions.swing.docking.ui.DockViewUI");
      UIManager.put("DetachedDockViewUI", "com.vlsolutions.swing.docking.ui.DetachedDockViewUI");
  }
  
  /** installs the DockVieTitleBar related properties */
  public void installDockViewTitleBarSettings(){
      UIManager.put("DockViewTitleBarUI", "com.vlsolutions.swing.docking.ui.DockViewTitleBarUI");
            
      UIManager.put("DockViewTitleBar.height", new Integer(20));
      UIManager.put("DockViewTitleBar.closeButtonText", 
          UIManager.getString("InternalFrameTitlePane.closeButtonText"));
      UIManager.put("DockViewTitleBar.minimizeButtonText", 
          UIManager.getString("InternalFrameTitlePane.minimizeButtonText"));
      UIManager.put("DockViewTitleBar.restoreButtonText", 
          UIManager.getString("InternalFrameTitlePane.restoreButtonText"));
      UIManager.put("DockViewTitleBar.maximizeButtonText", 
          UIManager.getString("InternalFrameTitlePane.maximizeButtonText"));
      UIManager.put("DockViewTitleBar.floatButtonText", "Detach");
      UIManager.put("DockViewTitleBar.attachButtonText", "Attach");
      
      // font to be used in the title bar
      UIManager.put("DockViewTitleBar.titleFont", UIManager.get("InternalFrame.titleFont"));
      
      // are buttons displayed or just accessible from the contextual menu ?
      // setting one of these flags to false hide the button from the title bar
      // setting to true not necessarily shows the button, as it then depends
      // on the DockKey allowed states.
      UIManager.put("DockViewTitleBar.isCloseButtonDisplayed", Boolean.TRUE);
      UIManager.put("DockViewTitleBar.isHideButtonDisplayed", Boolean.TRUE);
      UIManager.put("DockViewTitleBar.isDockButtonDisplayed", Boolean.TRUE);
      UIManager.put("DockViewTitleBar.isMaximizeButtonDisplayed", Boolean.TRUE);
      UIManager.put("DockViewTitleBar.isRestoreButtonDisplayed", Boolean.TRUE);
      UIManager.put("DockViewTitleBar.isFloatButtonDisplayed", Boolean.TRUE);
      UIManager.put("DockViewTitleBar.isAttachButtonDisplayed", Boolean.TRUE);
      
      UIManager.put("DockViewTitleBar.border", BorderFactory.createMatteBorder(0, 0, 1, 0,
        shadow));


  }
  
  /** installs the splitpanes related properties */
  public void installSplitContainerSettings(){
      UIManager.put("DockingSplitPaneUI", "com.vlsolutions.swing.docking.ui.DockingSplitPaneUI");    
      UIManager.put("SplitContainer.dividerSize", new Integer(4));    
      UIManager.put("SplitContainer.isResizingEnabled", Boolean.TRUE);    //2007/08/11
      
  }

  /** installs the tabbed pane related properties */
  public void installTabbedContainerSettings(){
      final String prefix = "/com/vlsolutions/swing/docking/";
      UIManager.put("TabbedDockableContainer.tabPlacement", new Integer(SwingConstants.TOP));
      
    
      UIManager.put("DockTabbedPane.closeButtonText", 
          UIManager.getString("InternalFrameTitlePane.closeButtonText"));
      UIManager.put("DockTabbedPane.minimizeButtonText", 
          UIManager.getString("InternalFrameTitlePane.minimizeButtonText"));
      UIManager.put("DockTabbedPane.restoreButtonText", 
          UIManager.getString("InternalFrameTitlePane.restoreButtonText"));
      UIManager.put("DockTabbedPane.maximizeButtonText", 
          UIManager.getString("InternalFrameTitlePane.maximizeButtonText"));
      UIManager.put("DockTabbedPane.floatButtonText", "Detach");
      UIManager.put("DockTabbedPane.attachButtonText", "Attach");//2005/10/07

      UIManager.put("JTabbedPaneSmartIcon.font", UIManager.getFont("TabbedPane.font")); //2006/01/23
      
      // set to true to set focus directly into a tabbed component when it becomes 
      // selected
      UIManager.put("TabbedContainer.requestFocusOnTabSelection", Boolean.FALSE);

  }

  /** installs the closable tabs properties */
  public void installCloseableTabs(){
      // this one is already provided by the look and feel
      // UIManager.put("TabbedPane.textIconGap", new Integer(4));
      UIManager.put("TabbedPane.otherIconsGap", new Integer(8));
      UIManager.put("TabbedPane.inBetweenOtherIconsGap", new Integer(4));
      UIManager.put("TabbedPane.alternateTabIcons", Boolean.FALSE);
  }


  /** installs icons used by the framework */
  public void installIcons(){
    final String prefix = "/com/vlsolutions/swing/docking/";
    Icon closeIcon = new ImageIcon(getClass().getResource(prefix + "close16v2.png"));
    Icon closeRolloverIcon = new ImageIcon(getClass().getResource(prefix + "close16v2rollover.png"));
    Icon closePressedIcon = new ImageIcon(getClass().getResource(prefix + "close16v2pressed.png"));

    Icon closeTabIcon = new ImageIcon(getClass().getResource(prefix + "close16tab.png"));
    Icon closeTabRolloverIcon = new ImageIcon(getClass().getResource(prefix + "close16tabRollover.png"));
    Icon closeTabPressedIcon = new ImageIcon(getClass().getResource(prefix + "close16tabPressed.png"));
    
    Icon hideIcon = new ImageIcon(getClass().getResource(prefix + "hide16v2.png"));
    Icon hideRolloverIcon = new ImageIcon(getClass().getResource(prefix + "hide16v2rollover.png"));
    Icon maximizeIcon = new ImageIcon(getClass().getResource(prefix + "maximize16v2.png"));
    Icon maximizeRolloverIcon = new ImageIcon(getClass().getResource(prefix + "maximize16v2rollover.png"));
    Icon restoreIcon = new ImageIcon(getClass().getResource(prefix + "restore16v2.png"));
    Icon restoreRolloverIcon = new ImageIcon(getClass().getResource(prefix + "restore16v2rollover.png"));
    Icon dockRolloverIcon = new ImageIcon(getClass().getResource(prefix + "dock16v2rollover.png"));

    Icon floatIcon = new ImageIcon(getClass().getResource(prefix + "float16v2.png"));
    Icon floatRolloverIcon = new ImageIcon(getClass().getResource(prefix + "float16v2rollover.png"));
    Icon floatPressedIcon = new ImageIcon(getClass().getResource(prefix + "float16v2pressed.png"));

    Icon attachIcon = new ImageIcon(getClass().getResource(prefix + "attach16v2.png"));
    Icon attachRolloverIcon = new ImageIcon(getClass().getResource(prefix + "attach16v2rollover.png"));
    Icon attachPressedIcon = new ImageIcon(getClass().getResource(prefix + "attach16v2pressed.png"));
    
    UIManager.put("DockViewTitleBar.close", closeIcon);
    UIManager.put("DockViewTitleBar.close.rollover", closeRolloverIcon);
    UIManager.put("DockViewTitleBar.close.pressed", closePressedIcon);
    
    UIManager.put("DockViewTitleBar.dock", new ImageIcon(getClass().getResource(prefix + "dock16v2.png")));
    UIManager.put("DockViewTitleBar.dock.rollover",  
        dockRolloverIcon);
    UIManager.put("DockViewTitleBar.dock.pressed", 
        new ImageIcon(getClass().getResource(prefix + "dock16v2pressed.png")));
    
    UIManager.put("DockViewTitleBar.hide", hideIcon);
    UIManager.put("DockViewTitleBar.hide.rollover", hideRolloverIcon);
    UIManager.put("DockViewTitleBar.hide.pressed", 
        new ImageIcon(getClass().getResource(prefix + "hide16v2pressed.png")));
    
    UIManager.put("DockViewTitleBar.maximize", maximizeIcon);
    UIManager.put("DockViewTitleBar.maximize.pressed", 
        new ImageIcon(getClass().getResource(prefix + "maximize16v2pressed.png")));
    UIManager.put("DockViewTitleBar.maximize.rollover", maximizeRolloverIcon);

    UIManager.put("DockViewTitleBar.restore", restoreIcon);
    UIManager.put("DockViewTitleBar.restore.pressed", 
        new ImageIcon(getClass().getResource(prefix + "restore16v2pressed.png")));
    UIManager.put("DockViewTitleBar.restore.rollover", restoreRolloverIcon);

    UIManager.put("DockViewTitleBar.float", floatIcon);    
    UIManager.put("DockViewTitleBar.float.rollover", floatRolloverIcon);    
    UIManager.put("DockViewTitleBar.float.pressed", floatPressedIcon);    

    UIManager.put("DockViewTitleBar.attach", attachIcon);    
    UIManager.put("DockViewTitleBar.attach.rollover", attachRolloverIcon);    
    UIManager.put("DockViewTitleBar.attach.pressed", attachPressedIcon);    

    UIManager.put("DockViewTitleBar.menu.close", closeRolloverIcon);
    UIManager.put("DockViewTitleBar.menu.hide", hideRolloverIcon);    
    UIManager.put("DockViewTitleBar.menu.maximize", maximizeRolloverIcon);  
    UIManager.put("DockViewTitleBar.menu.restore", restoreRolloverIcon);  
    UIManager.put("DockViewTitleBar.menu.dock", dockRolloverIcon);  
    UIManager.put("DockViewTitleBar.menu.float", floatRolloverIcon);  
    UIManager.put("DockViewTitleBar.menu.attach", attachRolloverIcon);      
    
    UIManager.put("DockTabbedPane.close", closeIcon);    
    UIManager.put("DockTabbedPane.close.rollover", closeRolloverIcon);    
    UIManager.put("DockTabbedPane.close.pressed", closePressedIcon);    

    UIManager.put("DockTabbedPane.unselected_close", null);    //2005/11/14
    UIManager.put("DockTabbedPane.unselected_close.rollover", closeRolloverIcon);    
    UIManager.put("DockTabbedPane.unselected_close.pressed", closePressedIcon);    

    
    UIManager.put("DockTabbedPane.menu.close", closeRolloverIcon);
    UIManager.put("DockTabbedPane.menu.hide", hideRolloverIcon);    
    UIManager.put("DockTabbedPane.menu.maximize", maximizeRolloverIcon);    
    UIManager.put("DockTabbedPane.menu.float", floatRolloverIcon);    
    UIManager.put("DockTabbedPane.closeAll", new ImageIcon(getClass().getResource(prefix + "closeAll16.png")));    
    UIManager.put("DockTabbedPane.closeAllOther", new ImageIcon(getClass().getResource(prefix + "closeAllOther16.png")));    
    UIManager.put("DockTabbedPane.menu.attach", attachRolloverIcon);     //2005/10/07
    

  }
  
  /** installs the eyboard shortcuts */
  public void installAccelerators(){
    
    int MENU_SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // this returns CTRL_MASK or META_MASK depending on the platform (win/mac os)
    
    UIManager.put("DockingDesktop.closeActionAccelerator", KeyStroke.getKeyStroke(
        KeyEvent.VK_F4, MENU_SHORTCUT_MASK));
    // toggle maximize/restore
    UIManager.put("DockingDesktop.maximizeActionAccelerator", KeyStroke.getKeyStroke(
        KeyEvent.VK_ESCAPE, KeyEvent.SHIFT_MASK));
    
    // toggle autohide/dock
    UIManager.put("DockingDesktop.dockActionAccelerator", KeyStroke.getKeyStroke(
        KeyEvent.VK_BACK_SPACE, MENU_SHORTCUT_MASK));
    
    UIManager.put("DockingDesktop.floatActionAccelerator", KeyStroke.getKeyStroke(
        KeyEvent.VK_F5, MENU_SHORTCUT_MASK));
    
  }

  /** installs the DockinDesktop related properties */
  public void installDesktopSettings(){
     UIManager.put("DockingDesktop.notificationColor", Color.ORANGE);
     UIManager.put("DockingDesktop.notificationBlinkCount", new Integer(5));
     UIManager.put("DragControler.stopDragCursor", new ImageIcon(
         getClass().getResource("/com/vlsolutions/swing/docking/stopdragcursor.gif")).getImage());
     
     UIManager.put("DragControler.detachCursor", new ImageIcon(
         getClass().getResource("/com/vlsolutions/swing/docking/detachCursor.png")).getImage());
     
     
     UIManager.put("DragControler.dragCursor", new ImageIcon(
         getClass().getResource("/com/vlsolutions/swing/docking/dragcursor.gif")).getImage());
        
     UIManager.put("DragControler.swapDragCursor", new ImageIcon(
         getClass().getResource("/com/vlsolutions/swing/docking/swapdragcursor.gif")).getImage());
     
     UIManager.put("DragControler.isDragAndDropEnabled", Boolean.TRUE);
     
     UIManager.put("DragControler.paintBackgroundUnderDragRect", Boolean.FALSE);
     
     
  }

  /** installs the FloatingDialog related properties */
  public void installFloatingSettings(){
    
//    Border border = BorderFactory.createMatteBorder(1,1,1,1, darkShadow);
//    Border titleBorder = BorderFactory.createMatteBorder(0,0,1,0, highlight);
    
    
    Border border = null;//BorderFactory.createEmptyBorder(1,1,1,1);
    Border titleBorder = null;//BorderFactory.createMatteBorder(0,0,1,0, shadow);
      
    UIManager.put("FloatingDialog.dialogBorder", border);
    UIManager.put("FloatingDialog.titleBorder", titleBorder);
    
    
    UIManager.put("FloatingContainer.followParentWindow", Boolean.TRUE);
    UIManager.put("FloatingContainer.paintDragShape", Boolean.TRUE);
  }

  /** installs the toolbar related properties */
  public void installToolBarSettings(){
    UIManager.put("ToolBarGripperUI", "com.vlsolutions.swing.toolbars.ToolBarGripperUI");
    // borders to use with toolbarpanels depending on their position
    UIManager.put("ToolBarPanel.topBorder", new ToolBarPanelBorder(ToolBarPanelBorder.TOP_PANEL)); 
    UIManager.put("ToolBarPanel.leftBorder", new ToolBarPanelBorder(ToolBarPanelBorder.LEFT_PANEL)); 
    UIManager.put("ToolBarPanel.bottomBorder", new ToolBarPanelBorder(ToolBarPanelBorder.BOTTOM_PANEL)); 
    UIManager.put("ToolBarPanel.rightBorder", new ToolBarPanelBorder(ToolBarPanelBorder.RIGHT_PANEL)); 
  }

    private void installColors() {
        Color shadow = UIManager.getColor("controlShadow");
        Color highlight = UIManager.getColor("controlLtHighlight");
        if (shadow == null){
            shadow = Color.GRAY;
        }
        if (highlight == null){
            highlight = shadow.brighter();
        }
                
        UIManager.put("VLDocking.shadow", shadow);
        UIManager.put("VLDocking.highlight", highlight);

    }

}
