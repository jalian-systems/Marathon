package com.vlsolutions.swing.docking;

import com.vlsolutions.swing.tabbedpane.JTabbedPaneSmartIcon;
import com.vlsolutions.swing.tabbedpane.JTabbedPaneSmartIconManager;
import com.vlsolutions.swing.tabbedpane.SmartIconJButton;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/** This component is used to display single dockables like if they were in a tabbed pane.
 * <p>
 * Please note this component is still in its early stages and might change in future releases.
 * <p>
 * To use it, just install the TabFactory as DockableContainerFactory
 *
 * @author Lilian Chamontin, VLSolutions
 * @see TabFactory
 * @since 2.1.3
 */
public class DockViewAsTab  extends DockView implements SingleDockableContainer {
  
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
  
  /** The tab container in which the components are added */
  protected TabHeader tabHeader = createTabHeader();
  
  /** smart icon manager */
  protected JTabbedPaneSmartIconManager tpManager = new JTabbedPaneSmartIconManager(tabHeader);
  
  /** smart icon used to display the title  */
  protected JTabbedPaneSmartIcon smartIcon = new JTabbedPaneSmartIcon(null, " ", null);
  
  protected AbstractAction closeAction = new AbstractAction("Close"){
    public void actionPerformed(ActionEvent e){
      desktop.close(getDockable());
    }
  };
  protected SmartIconJButton closeSmartIcon = new SmartIconJButton(closeAction);
  
  protected AbstractAction maximizeAction = new AbstractAction("Maximize"){
    public void actionPerformed(ActionEvent e){
      maximizeAction();
    }
  };
  protected SmartIconJButton maximizeSmartIcon = new SmartIconJButton(maximizeAction);
  
  protected AbstractAction hideAction = new AbstractAction("Hide"){
    public void actionPerformed(ActionEvent e){
      desktop.setAutoHide(getDockable(), true);
    }
  };
  protected SmartIconJButton hideSmartIcon = new SmartIconJButton(hideAction);
  
  protected AbstractAction floatAction = new AbstractAction("Float"){
    public void actionPerformed(ActionEvent e){
      desktop.setFloating(getDockable(), true);
    }
  };
  
  protected SmartIconJButton floatSmartIcon = new SmartIconJButton(floatAction);
  
  
  // flags to hide/show buttons in the title bar (they are always visible in the contextual menu, but might
  // take too much space on the titles (for example a minimum set could be hide/float/close
  //  as maximize is accessed by double click)
  protected  boolean isCloseButtonDisplayed = UIManager.getBoolean("DockViewTitleBar.isCloseButtonDisplayed");
  protected boolean isHideButtonDisplayed = UIManager.getBoolean("DockViewTitleBar.isHideButtonDisplayed");
  protected boolean isDockButtonDisplayed = UIManager.getBoolean("DockViewTitleBar.isDockButtonDisplayed");
  protected boolean isMaximizeButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isMaximizeButtonDisplayed");
  protected boolean isRestoreButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isRestoreButtonDisplayed");
  protected boolean isFloatButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isFloatButtonDisplayed");
  protected boolean isAttachButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isAttachButtonDisplayed");
  
  
  /** listen to the key changes */
  private PropertyChangeListener keyListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent e) {
      String pName = e.getPropertyName();
      if (pName.equals(DockKey.PROPERTY_ICON)) {
        smartIcon.setIcon( (Icon) e.getNewValue());
      } else if (pName.equals(DockKey.PROPERTY_NAME)) {
        smartIcon.setLabel( (String) e.getNewValue());
        revalidate();
      } else if (pName.equals(DockKey.PROPERTY_TOOLTIP)) {
        smartIcon.setTooltipText( (String) e.getNewValue());
      } else if (pName.equals(DockKey.PROPERTY_AUTOHIDEABLE)){
        resetTabIcons();
      } else if (pName.equals(DockKey.PROPERTY_CLOSEABLE)){
        resetTabIcons();
      } else if (pName.equals(DockKey.PROPERTY_FLOATABLE)){
        resetTabIcons();
      } else if (pName.equals(DockKey.PROPERTY_MAXIMIZABLE)){
        resetTabIcons();
      }
    }
  };
  
  private JPopupMenu currentPopUp = null;
  
  /** reacts to single and double click on title bar */
  private MouseListener titleMouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        maximizeAction();
      } else {
        getDockable().getComponent().requestFocus();
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
  
  public DockViewAsTab(Dockable dockable){
    super(dockable, false);
    add(tabHeader, BorderLayout.CENTER);
    tabHeader.addMouseListener(titleMouseListener);
    configureCloseButton();
    configureHideButton();
    configureMaximizeButton();
    configureFloatButton();
    setDockableAsTab(dockable);
    
  }
  
  private void checkForPopUp(MouseEvent e){
    Dockable target = getDockable();
    JPopupMenu popup = new JPopupMenu(target.getDockKey().getName());
    // first add the standard menu
    DockKey key = target.getDockKey();
    switch (key.getLocation()){
      case DOCKED:
        initDockedPopUp(popup);
        break;
      case HIDDEN:
        //initAutoHidePopUp(popup);
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
      popup.show(tabHeader, e.getX(), e.getY());
      this.currentPopUp = popup;
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
  
  /** Init the popup displayed as the title bar contextual menu */
  protected void initMaximizedPopUp(JPopupMenu popup){
    
    popup.add(createPopUpItem(RESTORE_TEXT, restoreIcon, RESTORE_TEXT,
        "maximize", (KeyStroke)UIManager.get("DockingDesktop.maximizeActionAccelerator") ));
  }
  
  protected void initAutoHidePopUp(JPopupMenu popup){
    if (DockingUtilities.isChildOfCompoundDockable(getDockable())){
      // restore option not allowed for children of a compound dockable
    } else {
      popup.add(createPopUpItem(RESTORE_TEXT, dockIcon, RESTORE_TEXT,
          "dock", (KeyStroke)UIManager.get("DockingDesktop.dockActionAccelerator")));
    }
    if (getDockable().getDockKey().isCloseEnabled()){
      popup.add(createPopUpItem(CLOSE_TEXT, closeSmartIcon, CLOSE_TEXT,
          "close", (KeyStroke)UIManager.get("DockingDesktop.closeActionAccelerator")));
    }
  }
  
  protected void initDockedPopUp(JPopupMenu popup){
    DockKey key = getDockable().getDockKey();
    if (key.isAutoHideEnabled()){
      popup.add(createPopUpItem(ICONIFY_TEXT, hideSmartIcon, ICONIFY_TEXT,
          "dock", (KeyStroke)UIManager.get("DockingDesktop.dockActionAccelerator")));
    }
    if (key.isFloatEnabled()){
      popup.add(createPopUpItem(FLOAT_TEXT, floatSmartIcon, FLOAT_TEXT,
          "float", (KeyStroke)UIManager.get("DockingDesktop.floatActionAccelerator")));
    }
    if (key.isMaximizeEnabled()){
      popup.add(createPopUpItem(MAXIMIZE_TEXT, maximizeSmartIcon, MAXIMIZE_TEXT,
          "maximize", (KeyStroke)UIManager.get("DockingDesktop.maximizeActionAccelerator")));
    }
    if (key.isCloseEnabled()){
      popup.add(createPopUpItem(CLOSE_TEXT, closeSmartIcon, CLOSE_TEXT,
          "close", (KeyStroke)UIManager.get("DockingDesktop.closeActionAccelerator")));
    }
  }
  
  /** Init the popup displayed as the title bar contextual menu */
  protected void initFloatingPopUp(JPopupMenu popup){
    if (DockingUtilities.isChildOfCompoundDockable(getDockable())){
      // attach option not allowed for children of a compound dockable
    } else {
      popup.add(createPopUpItem(ATTACH_TEXT, attachIcon, ATTACH_TEXT,
          "float", (KeyStroke)UIManager.get("DockingDesktop.floatActionAccelerator")));
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
  private void dockAction() {
    if (isAutoHide()){
      desktop.setAutoHide(getDockable(), false);
    } else {
      desktop.setAutoHide(getDockable(), true);
    }
    
  }
  
  protected void maximizeAction(){
    if (isMaximized()) {
      desktop.restore(getDockable());
    } else {
      desktop.maximize(getDockable());
    }
  }
  private void closeAction() {
    desktop.close(getDockable());
  }
  
  private void floatAction() {
    if (getDockable().getDockKey().getLocation() == DockableState.Location.FLOATING){
      desktop.setFloating(getDockable(), true);
    } else {
      desktop.setFloating(getDockable(), false);
    }
  }
  private boolean isAutoHide(){
    return getDockable().getDockKey().getLocation() == DockableState.Location.HIDDEN;
  }
  private boolean isMaximized(){
    return getDockable().getDockKey().getLocation() == DockableState.Location.MAXIMIZED;
  }
  
  protected void configureFloatButton() {
    
    if (isFloatButtonDisplayed){
      floatSmartIcon.setIcon(UIManager.getIcon("DockViewTitleBar.float"));
      floatSmartIcon.setPressedIcon(UIManager.getIcon("DockViewTitleBar.float.pressed"));
      floatSmartIcon.setRolloverIcon(UIManager.getIcon("DockViewTitleBar.float.rollover"));
      // add a tooltip
      floatAction.putValue(AbstractAction.SHORT_DESCRIPTION,
          UIManager.get("DockViewTitleBar.floatButtonText"));
    }
  }
  
  protected void configureMaximizeButton() {
    if (isMaximizeButtonDisplayed){
      
      maximizeSmartIcon.setIcon(UIManager.getIcon("DockViewTitleBar.maximize"));
      maximizeSmartIcon.setPressedIcon(UIManager.getIcon("DockViewTitleBar.maximize.pressed"));
      maximizeSmartIcon.setRolloverIcon(UIManager.getIcon("DockViewTitleBar.maximize.rollover"));
      // add a tooltip
      maximizeAction.putValue(AbstractAction.SHORT_DESCRIPTION,
          UIManager.get("DockViewTitleBar.maximizeButtonText"));
    }
    
  }
  
  protected void configureHideButton() {
    
    if (isHideButtonDisplayed){
      hideSmartIcon.setIcon(UIManager.getIcon("DockViewTitleBar.hide"));
      hideSmartIcon.setPressedIcon(UIManager.getIcon("DockViewTitleBar.hide.pressed"));
      hideSmartIcon.setRolloverIcon(UIManager.getIcon("DockViewTitleBar.hide.rollover"));
      // add a tooltip
      hideAction.putValue(AbstractAction.SHORT_DESCRIPTION,
          UIManager.get("DockViewTitleBar.minimizeButtonText"));
    }
  }
  
  protected void configureCloseButton() {
    
    if (isCloseButtonDisplayed){
      closeSmartIcon.setIcon(UIManager.getIcon("DockTabbedPane.close"));
      closeSmartIcon.setPressedIcon(UIManager.getIcon("DockTabbedPane.close.pressed"));
      closeSmartIcon.setRolloverIcon(UIManager.getIcon("DockTabbedPane.close.rollover"));
      // add a tooltip
      closeAction.putValue(AbstractAction.SHORT_DESCRIPTION,
          UIManager.get("DockTabbedPane.closeButtonText"));
    }
  }
  
  
  public void setDockable(Dockable d){
    // overriden to disable DockView settings
  }
  
  public void setDockableAsTab(final Dockable d){
    this.dockable = d;
    if (title != null){ // still needed as removeing it triggers Exception... we'll have to
      // remove it one day (when building a proper ui class)
      title.setDockable(dockable);
    }
    
    
    resetTabIcons();
    
    
    // allow resizing  of split pane beyond minimum size
    // could be replaced by adding a JScrollPane instead of panels
    setMinimumSize(new Dimension(30,30));
  }
  
  public void resetTabIcons(){
    ArrayList icons = new ArrayList();
    DockKey k = getDockable().getDockKey();
    if (k.isCloseEnabled() && isCloseButtonDisplayed){
      icons.add(closeSmartIcon);
    }
    if (k.isMaximizeEnabled() && isMaximizeButtonDisplayed){
      icons.add(maximizeSmartIcon);
    }
    if (k.isAutoHideEnabled()&& isHideButtonDisplayed){
      icons.add(hideSmartIcon);
    }
    if (k.isFloatEnabled() && isFloatButtonDisplayed){
      icons.add(floatSmartIcon);
    }
    
    
    // this is an attempt to create the set of icons displayed for this dockable
    // currently, only "close" is managed, but other buttons could be added, as
    // the jTabebdPaneSmartIcon supports(simulates) many sub-buttons.
    if (icons.size()> 0){
      SmartIconJButton [] iconsArray = (SmartIconJButton[]) icons.toArray(new SmartIconJButton[0]);
      smartIcon = new JTabbedPaneSmartIcon(k.getIcon(), k.getName(), iconsArray);
      smartIcon.setIconForTabbedPane(tabHeader);
      tabHeader.addTab("", smartIcon, getDockable().getComponent());
    } else {
      tabHeader.addTab(k.getName(), k.getIcon(), getDockable().getComponent());
    }
    
  }
  
  /** {@inheritDoc} */
  public void installDocking(DockingDesktop desktop) {
    this.desktop = desktop;
    desktop.installDockableDragSource(tabHeader);
    dockable.getDockKey().addPropertyChangeListener(keyListener);
  }
  
  /** {@inheritDoc} */
  public void uninstallDocking(DockingDesktop desktop) {
    desktop.uninstallDockableDragSource(tabHeader);
    dockable.getDockKey().removePropertyChangeListener(keyListener);
  }
  
  public String getUIClassID() {
    return "PanelUI";  // default panel UI
  }
  
  protected TabHeader createTabHeader(){
    return new TabHeader();
  }
  
  
  protected class TabHeader extends JTabbedPane implements DockableDragSource {
    
    public boolean startDragComponent(Point p) {
      Rectangle bounds = getBoundsAt(0);
      return bounds.contains(p);
    }
    
    public Container getDockableContainer() {
      return DockViewAsTab.this;
    }
    
    public Dockable getDockable() {
      return dockable;
    }
    
    public void endDragComponent(boolean dropped) {
    }
    
  }
  
}
