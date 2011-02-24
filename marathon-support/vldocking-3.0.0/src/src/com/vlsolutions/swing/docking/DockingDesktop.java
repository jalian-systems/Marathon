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
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import com.vlsolutions.swing.docking.event.*;
import com.vlsolutions.swing.docking.ui.DockingUISettings;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The DockingDesktop is the main class of the VLDocking Framework.
 * <P> It is the equivalent of what is JDesktopPane for JInternalWindow :
 *  a JLayeredPane customized to include :
 * <UL>
 * <LI> Four auto-hide borders used to show Dockable iconified as buttons.
 * <LI> A nested containment hierarchy (made of JSplitPanes) with drag and drop (dock)
 * capabilities.
 * <LI> An extensive API to programmatically change its contents.
 * <LI> An XML encoder/decoder to create / save / reload workspaces (sets of dockable components)
 * <LI> An abstraction of the Dockable notion, with factories to allow different implementations
 * or customization of the Dockable containers.
 * <LI> An event model to track changes and react to them.
 * </UL>
 *
 * @author Lilian Chamontin, VLSolutions.
 * @version 2.0
 *
 * @update 2005/10/06 Lilian Chamontin : added support for dnd to floatables
 * @update 2005/10/07 Lilian Chamontin : cancel drag operation with ESCAPE key
 * @update 2005/11/08 Lilian Chamontin : added support for global width/height drop (in split method)
 * @update 2005/11/14 Lilian Chamontin : reworked setFloating methods.
 * @update 2005/12/08 Lilian Chamontin : fixed a bug related to multiple desktop usage (when moving a window after
 *    the desktop has been removed from hierarchy, the listeners were still invoked and caused a NPE in moveFloatingWindows).
 *
 * @update 2006/12/01 Lilian Chamontin : fixed a NPE when closing a window with some floating children remaining
 * @update 2006/12/19 Lilian Chamontin : fixed a memory leak issue  due to keyboard focus manager handling
 * @update 2007/03/19 reformulate addHiddenDockable tests to support closed dockables.
 * @update 2007/08/11 added safety check to avoid creating tabs on a maximized component
 * @update 2008/07/05 fixed a NPE occuring when saving workspace and a tabbed component was still maximized
 */


public class DockingDesktop extends JLayeredPane {
  
  private static final String CURRENT_VERSION_NUMBER = "2.1.5";
  private static final String BUILD_DATE = "2008/07/05";
  
  
  static {
    // install UI settings if not already done
    DockingUISettings.getInstance().installUI();
  }
  
  /** contextual data that can be shared by multiple desktops */
  private DockingContext context;
  
  
  /** panel containing autohidepanels on borders and dockingpanel at center */
  private JPanel contentPane = new JPanel(new DockingBorderLayout());
  
  /** main central container for dockables */
  protected DockingPanel dockingPanel = new DockingPanel();
  
  /** container responsible for auto-hidden components expansion */
  protected AutoHideExpandPanel expandPanel = createAutoHideExpandPanel();
  
  /** panel used to display autohide buttons */
  protected AutoHideButtonPanel topBorderPane= new AutoHideButtonPanel(expandPanel, DockingConstants.INT_HIDE_TOP);
  /** panel used to display autohide buttons */
  protected AutoHideButtonPanel leftBorderPane = new AutoHideButtonPanel(expandPanel, DockingConstants.INT_HIDE_LEFT);
  /** panel used to display autohide buttons */
  protected AutoHideButtonPanel bottomBorderPane = new AutoHideButtonPanel(expandPanel, DockingConstants.INT_HIDE_BOTTOM);
  /** panel used to display autohide buttons */
  protected AutoHideButtonPanel rightBorderPane = new AutoHideButtonPanel(expandPanel, DockingConstants.INT_HIDE_RIGHT);
  
  /** array containing the border panes */
  protected AutoHideButtonPanel[] borderPanes = {topBorderPane,
  leftBorderPane, bottomBorderPane, rightBorderPane};
  
  
  private HashMap <DockKey, AutoHideButton> autoHideButtons = new HashMap(); // key : DockKey / value : AutoHideButton
  
  
  private DragControler dragControler;
  
  
  /**  a component used to track position of the current maximized component */
  private MaximizedComponentReplacer dummyMaximedReplacer = new MaximizedComponentReplacer();// 2007/01/18
  //new JLabel();
  
  /** the current maximized component */
  private Component maximizedComponent;
  /** a flag set when adding a maximized component : true is this one is heavyweight
   * Only used with heavyweight support AND singleHeavyWeightComponent
   */
  private boolean currentMaximizedComponentIsHeavyWeight = false;
  
  
  private JComponent mouseMotionGrabber = new JComponent() {
    // component used to grab mouse events under the expansion panel
    // only visible (and active) when expansion panel is visible
  };
  
  /** this timer is only used when the java version is < 1.5 (version>=1.5 uses the MouseInfo component)*/
  private javax.swing.Timer mouseOutOfExpandedPanelTimer
      = new javax.swing.Timer(1000, new ActionListener() {
    // timer used to hide the expanded panel when mouse is out too long
    public void actionPerformed(ActionEvent actionEvent) {
      if (!expandPanel.isActive() && expandPanel.shouldCollapse()) {
        // do not hide it if it has got the focus
        // or if a non-collapsible operation is occuring
        expandPanel.collapse();
      }
    }
  });
  
  /** groups of tabs (used to re-tab autohidden dockable)*/
  protected HashMap <Dockable, LinkedList<Dockable>> tabbedGroups = new HashMap(); // <Dockable>/<LinkedList<Dockable>
  
  private FocusHandler focusHandler = new FocusHandler();
  
  /** return state for floating dockables */
  protected HashMap <Dockable, DockableState> previousFloatingDockableStates = new HashMap(); // key dockable / value dockable state
  
  /** Unique name for this desktop : used since 2.1 to support multiple desktops*/
  private String desktopName;
  
  
  // 2005/10/10 added support for moving the floating dialogs with the frame
  private Point lastWindowLocation = null;
  private ComponentAdapter windowMovementListener = new ComponentAdapter(){
    public void componentMoved(ComponentEvent e){
      moveFloatingWindows();
    }
  };
  
  WindowListener windowListener = new WindowAdapter() {
    public void windowActivated(WindowEvent e) {
      context.windowActivated(e); //foward activation events to the context
    }
  };
  
  /** Action used for keyboard binding : closes the current dockable */
  private AbstractAction closeAction = new AbstractAction(){
    public void actionPerformed(ActionEvent e){
      Dockable d = focusHandler.getCurrentDockable();
      if (d != null){
        if (d.getDockKey().isCloseEnabled()){
          DockableState.Location location = d.getDockKey().getLocation();
          
          if (location == DockableState.Location.DOCKED || location == DockableState.Location.FLOATING
              || location == DockableState.Location.HIDDEN){
            close(d);
          }
        }
      }
    }
  };
  
  /** Action used for keyboard binding : maximize/restore the current dockable */
  private AbstractAction maximizeAction = new AbstractAction(){
    public void actionPerformed(ActionEvent e){
      Dockable d = focusHandler.getCurrentDockable();
      if (d != null){
        if (d.getDockKey().isMaximizeEnabled()){
          DockableState.Location location = d.getDockKey().getLocation();
          if (location == DockableState.Location.DOCKED){
            maximize(d);
          } else if (location == DockableState.Location.MAXIMIZED){
            restore(d);
          }
        }
      }
    }
  };
  
  /** Action used for keyboard binding : autohide/dock the current dockable */
  private AbstractAction dockAction = new AbstractAction(){
    public void actionPerformed(ActionEvent e){
      Dockable d = focusHandler.getCurrentDockable();
      if (d != null){
        if (d.getDockKey().isAutoHideEnabled()){
          DockableState.Location location = d.getDockKey().getLocation();
          if (location == DockableState.Location.DOCKED){
            setAutoHide(d, true);
          } else if (location == DockableState.Location.HIDDEN){
            setAutoHide(d, false);
          }
        }
      }
    }
  };
  
  /** Action used for keyboard binding : float/attach the current dockable */
  private AbstractAction floatAction = new AbstractAction(){
    public void actionPerformed(ActionEvent e){
      Dockable d = focusHandler.getCurrentDockable();
      if (d != null){
        if (d.getDockKey().isFloatEnabled()){
          DockableState.Location location = d.getDockKey().getLocation();
          if (location == DockableState.Location.DOCKED){
            setFloating(d, true);
          } else if (location == DockableState.Location.FLOATING){
            setFloating(d, false);
          }
        }
      }
    }
  };
  
  /** Action used for keyboard binding : cancel the current operation */
  private AbstractAction cancelAction = new AbstractAction(){
    public void actionPerformed(ActionEvent e){
      dragControler.cancelDrag();
    }
  };
  
  
  
  /** Constructs a DockingDesktop with a default name (suitable for single-desktop applications).
   */
  public DockingDesktop() {
    this("", null);
  }
  
  /** Constructs a DockingDesktop with a given name (suitable for multiple-desktop applications).
   *  <p>
   * This also constructs a new Docking Context, that can be shared with other Desktops.
   */
  public DockingDesktop(String desktopName) {
    this(desktopName, null);
  }
  
  
  /** Constructs a DockingDesktop with a given name (suitable for multiple-desktop applications).
   */
  public DockingDesktop(String desktopName, DockingContext context) {
    setDesktopName(desktopName);
    if (context == null){
      this.context = new DockingContext(); // initial (single desktop) context
    } else {
      this.context = context; // shared context issued from another dekstop
    }
    
    this.context.addDesktop(this);
    
    this.dragControler = DragControlerFactory.getInstance().createDragControler(this);
    
//
//    add(contentPane, BorderLayout.CENTER);
    
    topBorderPane.setVisible(false);
    leftBorderPane.setVisible(false);
    bottomBorderPane.setVisible(false);
    rightBorderPane.setVisible(false);
    contentPane.add(topBorderPane, BorderLayout.NORTH);
    contentPane.add(leftBorderPane, BorderLayout.WEST);
    contentPane.add(bottomBorderPane, BorderLayout.SOUTH);
    contentPane.add(rightBorderPane, BorderLayout.EAST);
    contentPane.add(dockingPanel, BorderLayout.CENTER);
    
    add(contentPane, DEFAULT_LAYER);
    mouseMotionGrabber.setVisible(false);
    
    
    mouseMotionGrabber.addMouseListener(new MouseAdapter() {
      boolean canUseMouseInfo = DockingUtilities.canUseMouseInfo();
      public void mouseEntered(MouseEvent e) {
        // means that the mouse is not above the expanded panel
        if (! canUseMouseInfo){ // only for 1.4 codebase or untrusted envs
          mouseOutOfExpandedPanelTimer.restart(); //2005/11/01
        }
      }
      
      public void mouseExited(MouseEvent e) {
        // means that the mouse is probably above the expanded panel (or out of the window)
        if (! canUseMouseInfo){ // only for 1.4 codebase or untrusted envs
          mouseOutOfExpandedPanelTimer.stop(); //2005/11/01
        }
      }
      
      public void mousePressed(MouseEvent e) {
        // collapse the expanded panel and redispatch the event to the underlying frame
        // for processing by the real component clicked by the user
        
        expandPanel.collapse();
        Window w = SwingUtilities.getWindowAncestor(DockingDesktop.this);
        if (w != null) {
          w.dispatchEvent(e);
        }
      }
    });
    
    expandPanel.installDocking(this);
    expandPanel.setVisible(false);
    
    add(mouseMotionGrabber, new Integer(MODAL_LAYER.intValue() - 1));
    add(expandPanel, MODAL_LAYER);
    
    ComponentListener resizeListener = new ComponentAdapter() {
      /* resize listener is added to the desktop and to the contentPane */
      public void componentResized(ComponentEvent event) {
        int w = getWidth();
        int h = getHeight();
        Insets i = getDockingPanelInsets();
        
        if (event.getComponent() == DockingDesktop.this){
          // hide the expanded panel if necessary
          expandPanel.collapse();
          // resize the panel
          contentPane.setBounds(0, 0, w, h);
          revalidate();
        } else if (event.getComponent() == dockingPanel){
          if (maximizedComponent != null) {
            if (DockingPreferences.isLightWeightUsageEnabled()){
              maximizedComponent.setBounds(i.left, i.top, w - i.left - i.right,
                  h - i.top - i.bottom);
            } else {
              if (DockingPreferences.isSingleHeavyWeightComponent()){
                if (DockingUtilities.isHeavyWeightComponent(maximizedComponent)){
                  maximizedComponent.setBounds(i.left, i.top, w - i.left - i.right,
                      h - i.top - i.bottom);
                } else {
                  maximizedComponent.getParent().setBounds(i.left, i.top, w - i.left - i.right,
                      h - i.top - i.bottom);
                }
              } else {
                maximizedComponent.getParent().setBounds(i.left, i.top, w - i.left - i.right,
                    h - i.top - i.bottom);
              }
            }
          }
          mouseMotionGrabber.setBounds(i.left, i.top, w - i.left - i.right,  h - i.top - i.bottom);
          dockingPanel.revalidate();
        }
      }
    };
    
    addComponentListener(resizeListener);
    
    dockingPanel.addComponentListener(resizeListener);
    
    expandPanel.addPropertyChangeListener(AutoHideExpandPanel.PROPERTY_EXPANDED,
        new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getNewValue().equals(Boolean.TRUE)){
          mouseMotionGrabber.setVisible(true);
        } else {
          mouseMotionGrabber.setVisible(false);
        }
      }
    });
    
    mouseOutOfExpandedPanelTimer.setRepeats(false); // avoid loops
    
    /*KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
        "focusOwner", focusHandler); //2006/08/21 : back to life (was in comments in 2.1.0)*/
    
    installKeyboardBindings();
    
    // optional window following
    final boolean floatingContainerFollowParentWindow = UIManager.getBoolean("FloatingContainer.followParentWindow");
    addAncestorListener(new AncestorListener(){
      public void ancestorAdded(AncestorEvent event) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
            "focusOwner", focusHandler); //2006/08/21 : back to life (was in comments in 2.1.0)
        // 2006/12/19 now integrated to ancestor hierarchy to avoid gc leaks
        
        Window w = SwingUtilities.getWindowAncestor(DockingDesktop.this);
        w.addWindowListener(windowListener);
        DockingDesktop.this.context.registerWindow(w); // first activation required to register the window
        if (floatingContainerFollowParentWindow){
          w.addComponentListener(windowMovementListener);
        }
      }
      public void ancestorRemoved(AncestorEvent event){// 2005/12/08
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(
            "focusOwner", focusHandler); //2006/12/19
        
        if (event == null || event.getAncestorParent() == null){
          // 2006/12/01 protection againts NPE
          return;
        } else {
          Window w = SwingUtilities.getWindowAncestor(event.getAncestorParent());
          if (w !=null){ // 2007/03/07 hope it helps with desktops on JDialogs
            w.removeWindowListener(windowListener);
            if (floatingContainerFollowParentWindow){
              w.removeComponentListener(windowMovementListener);
            }
          }
          
        }
      }
      public void ancestorMoved(AncestorEvent event){
      }
    });
    
    
  }
  
  /** Registers listeners for notable keyboard events (CLOSE, DOCK/HIDE, MAXIMIZE/RESTORE, float/attach).
   * KeyStroke bindings can be defined and overriden this UIManager properties (see DockingUISettings for
   * details).
   *
   */
  private void installKeyboardBindings(){
    
    KeyStroke ks = (KeyStroke) UIManager.get("DockingDesktop.closeActionAccelerator");
    if (ks != null){
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "close");
      getActionMap().put("close", closeAction);
    }
    ks = (KeyStroke) UIManager.get("DockingDesktop.maximizeActionAccelerator");
    if (ks != null){
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "maximize");
      getActionMap().put("maximize", maximizeAction);
    }
    ks = (KeyStroke) UIManager.get("DockingDesktop.dockActionAccelerator");
    if (ks != null){
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "dock");
      getActionMap().put("dock", dockAction);
    }
    ks = (KeyStroke) UIManager.get("DockingDesktop.floatActionAccelerator");
    if (ks != null){
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "float");
      getActionMap().put("float", floatAction);
    }
    
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // general cancel action 2005/10/07
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "cancel");
    getActionMap().put("cancel", cancelAction);
    
    
  }
  
  /** Returns the currently selected dockable, or null if none is selected.
   *<p>
   * A selected dockable is a dockable whose component contains the keyboard focus.
   */
  public Dockable getSelectedDockable(){
    return focusHandler.currentDockable;
  }
  
  /** Set the desktop contents to be opaque or transparent.
   * <p>
   * Transparent contents allow adding background images/information when no dockable is visible (everything closed
   * or autohidden).
   * <p>
   * Default value is true (opaque).
   *
   * @since 2.0.4
   */
  public void setOpaqueContents(boolean opaque){
    contentPane.setOpaque(opaque);
    dockingPanel.setOpaque(opaque);
  }
  
  /** Returns whether this desktop's contents are opaque or not.
   * <p>
   * Default value is true (opaque).
   *
   * @see #setOpaqueContents(boolean).
   * @since 2.0.4
   */
  public boolean isOpaqueContents(){
    return contentPane.isOpaque();
  }
  
  
  /** Returns the docking panel used by this desktop.
   * 
   * Usage of this method should be limited to VLDocking extensions (simple users
   * shouldn't rely on the underlying DockingPanel existence)
   */
  DockingPanel getDockingPanel(){
    /* package protected method */
    return dockingPanel;
  }
  
  
  /** Every dockable must be registered in order to be shown in the DockingDesktop.
   *
   * <P> Registration is automatic for shown dockables :
   *   methods setCentralDockable(), split(), createTab(), hide()... leading to have the dockable
   * visible call registerDockable() )
   * <P> for not shown dockable (in order to read a configuration from an XML stream, or to list
   * the dockable in DockingSelectorDialog ), this method must be called manually.
   * <p>
   * As of version 2.1, this method call is forwarded to the DockingContext
   * */
  public void registerDockable(Dockable dockable){
    context.registerDockable(dockable);
  }
  
  /** Unregisters the dockable, which can be garbage collected (no longer used
   * by the docking desktop.
   * <p>
   * As of version 2.1, this method call is forwarded to the DockingContext
   *  */
  public void unregisterDockable(Dockable dockable){
    //context.registerDockable(dockable); // 2006/09/06
    context.unregisterDockable(dockable); // 2006/09/06
  }
  
  /** Returns a String containing the version of the docking framework in the format M.m.r
   * where M is the major , m the minor and r the release.
   *@since 2.0
   */
  public static String getDockingFrameworkVersion(){
    return CURRENT_VERSION_NUMBER;
  }
  
  /** Returns a String containing the release date of the current version.
   *@since 2.0
   */
  public static String getDockingFrameworkBuildDate(){
    return BUILD_DATE;
  }
  
  
  /** Adds a view in a tab, or create it if it doesn't exist.
   *
   * @param base     the reference dockable
   * @param dockable  a dockable to add at the same position than <code>base</code>.
   * if base is not already child of a tabbedpane, a new tabbedpane will be created and inserted
   * at base's location.
   * @param order the tab order of view in its tabbed pane.
   *
   */
  public void createTab(Dockable base, Dockable dockable, int order){
    createTab(base, dockable, order, false);
  }
  
  /** Add a view in a tab, or create it if it doesn't exist.
   * <P> Optional added tab selection.
   *
   * @param base  an existing dockable, either displayed in a DockableContainer or
   * in a TabbedDockableContainer.
   * <P>If base is displayed by a DockableContainer, this container will be replaced
   * by a TabbedDockableContainer.
   * @param dockable     the dockable to add
   * @param order    the tab order of view in its tabbed pane.
   * @param select   if true, will select the added tab (make it appear at front)
   *
   */
  public void createTab(Dockable base, Dockable dockable, int order, boolean select){
    createTab(base, dockable, order, select, true);
  }
  
  /** private implementation wich allows for triggering state change event or not
   * (depending on the caller).
   */
  private void createTab(Dockable base, Dockable dockable, int order, boolean select, boolean triggerEvents){
    /* createTab() is called by :
     *  - DockView/DetachedDockView during DnD
     *  - DockTabbedPane during DnD
     *  - show() which is called by
     *    - setFloating(false)
     *    - setAutoHide(false)
     */
    
    if (base == null)throw new NullPointerException("base must not be null");
    if (dockable == null)throw new NullPointerException(
        "dockable must not be null");
    
    context.registerDockable(dockable); //2007/08/11
    
    if (getMaximizedDockable() == base){ // 2007/08/11 added safety restore
      restore(getMaximizedDockable());
    }
    
    DockableState currentState = getDockableState(dockable);
    DockableState.Location currentLocation = getLocation(currentState);
    
    // currentState might be null CLOSED HIDDEN DOCKED FLOATING
    
    DockableState newState = null ; // 2005/10/06 - support for floatable tabs : the future state can be docked or floating
    if (base.getDockKey().getLocation() == DockableState.Location.FLOATING){
      RelativeDockablePosition position = new RelativeDockablePosition(dockingPanel, dockable);
      newState = new DockableState(this, dockable,
          base.getDockKey().getLocation(), position);
    } else {
      newState = new DockableState(this, dockable,
          base.getDockKey().getLocation());
    }
    
    DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
    DockingActionEvent dae = new DockingActionCreateTabEvent(this, dockable,
        currentLocation, newState.getLocation(), base, order);
    
    //boolean stateChange = currentState == null || !currentState.isDocked();
    //boolean stateChange = currentState == null || currentState.getLocation() != newState.getLocation(); //2005/10/07
    if (triggerEvents){
      if (! isDockingActionAccepted(dae, dswe)) {
        return; // vetoed
      }
    }
    
    TabbedDockableContainer dockableTab = null;
    if (currentLocation != DockableState.Location.CLOSED){
      // don't search tabs for closed dockables // 2007/02/27
      dockableTab = DockingUtilities.findTabbedDockableContainer(dockable);
    }
//    TabbedDockableContainer baseTab = DockingUtilities.findTabbedDockableContainer(dockable); // 2005/05/19
    TabbedDockableContainer baseTab = DockingUtilities.findTabbedDockableContainer(base); // 2005/05/19
    
    if (dockableTab != null
        && dockableTab != baseTab){
      // don't belong to the same tabbed pane, so remove first the dockable
      remove(dockable); // TODO : check this : removal should have been done earlier (in dropRemove() )
    }
    
//    if ( currentState != null && (!currentState.isFloating()) && newState.isFloating()){
    if ( currentState != null && currentState.isFloating() && newState.isFloating()){
      // this case is when dragging a floating dockable into another floating window
      Window w = SwingUtilities.getWindowAncestor(dockable.getComponent());
      Window w2 = SwingUtilities.getWindowAncestor(base.getComponent());
      if (w == null){
        // the dockable has already been removed
      } else if (w != w2){
        // when creating a floating tab, we have to manually remove the dockable
        remove(dockable);
      } else { // else same window : no need to remove it
        remove(dockable);
      }
    } else if ( currentState != null && (!currentState.isFloating()) && newState.isFloating()){
      // from !floating to floating :
      // when creating a floating tab, we have to manually remove the dockable
      remove(dockable);
    }
    
    if (!newState.isFloating()){
      // remove old attachment except when creating a floating tab
      removeFromTabbedGroup(dockable); // 2005/07/13
    }
    
    
    if (baseTab != null){
      // add a tab
      baseTab.addDockable(dockable, order);
    } else {
      // base isn't included in a tabbed pane, so we have to create one
      DockableContainer baseOldContainer = DockingUtilities.findSingleDockableContainer(base);
      
      
      baseOldContainer.uninstallDocking(this);      
      
      baseTab = DockableContainerFactory.getFactory().createTabbedDockableContainer();
      baseTab.installDocking(this);
      baseTab.addDockable(base,0);
      baseTab.addDockable(dockable,1);
      
      
      
      ((JTabbedPane) baseTab).addChangeListener(focusHandler); // our best way to track selection (focus) changes
      
      DockingUtilities.replaceChild(((Component)baseOldContainer).getParent(),
          (Component)baseOldContainer, (Component)baseTab);
      
    }
    if (select){
      baseTab.setSelectedDockable(dockable);
    }
    //context.registerDockable(dockable); //2007/08/11
    context.setDockableState(dockable, newState);
    
    if (newState.isFloating() && !currentState.isFloating()){
      // we need to store the return information
      storePreviousFloatingState(dockable, currentState);
    }
    
    
    if (triggerEvents){
      dockable.getDockKey().setLocation(base.getDockKey().getLocation()); //2005/10/06
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    }
    
    if (!newState.isFloating()){
      // register tab group association except if we are going floating...
      addToTabbedGroup(base, dockable); //2005/07/13
    }
    DockingUtilities.updateResizeWeights(dockingPanel);
  }


  /** Splits a Dockable in 2 parts, if possible.
   * <p>
   *  The base dockable is the reference, the second newDockable will be
   * added according to the position parameter.
   * <p>
   *  If base is contained in a non splitable container (like a tab of DockTabbedPane)
   * then, a splitable ancestor will be searched (until the root desktop pane is reached)
   * to apply splitting.
   *
   * @param base   an already docked Dockable
   * @param newDockable   the added dockable
   * @param position  position of newDockable relative to base
   * @param proportion proportion of the initial dockable space taken by the new dockable
   * a negative proportion, like -1, will be ignored (and split will be based on component preferred
   *  sizes and weights). This parameter is an alternative to DockingDesktop.setDockableHeight()
   * and setDockableWidth() methods
   * @see DockingDesktop#setDockableHeight(com.vlsolutions.swing.docking.Dockable, double)
   * @see DockingDesktop#setDockableWidth(com.vlsolutions.swing.docking.Dockable, double)
   */
  public void split(Dockable base, Dockable newDockable, DockingConstants.Split position, double proportion){
    /* split() is used internally by the framework in the following cases :
     *  - from a move() API invocation
     *  - from a DnD on DockTabbedPane (overriding BorderSplitter) but only when the dockable is not floating
     *
     * => we can safely assume that the future state of the dockable will be "Docked"
     */
    if (base == null) throw new NullPointerException("base must not be null");
    if (newDockable == null) throw new NullPointerException("newDockable must not be null");


    DockableState currentState = getDockableState(newDockable);
    DockableState.Location currentLocation = getLocation(currentState);

    boolean stateChange = currentState == null || !currentState.isDocked();
    // todo : new state should be DOCKED *OR* FLOATING
    DockableState newState = new DockableState(this, newDockable,
        DockableState.Location.DOCKED);
    DockingActionEvent dae = new DockingActionSplitDockableEvent(this, newDockable,
        currentLocation, newState.getLocation(), base, position, 0.5f);

    DockableStateWillChangeEvent event = new DockableStateWillChangeEvent(
        currentState, newState);

    if (!isDockingActionAccepted(dae,event)) {
      return; // vetoed
    }

    TabbedDockableContainer baseTab = DockingUtilities.findTabbedDockableContainer(base);

    DockableContainer dockableContainer = DockableContainerFactory.getFactory().
        createDockableContainer(newDockable, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
    dockableContainer.installDocking(this);


    // create a new splitcontainer that will replace baseDockable's container
    SplitContainer split;
    if (position == DockingConstants.SPLIT_TOP
        || position == DockingConstants.SPLIT_BOTTOM){
      split = new SplitContainer(JSplitPane.VERTICAL_SPLIT);
    } else /*if (position == DockingConstants.SPLIT_LEFT
        || position == DockingConstants.SPLIT_RIGHT)*/{
      split = new SplitContainer(JSplitPane.HORIZONTAL_SPLIT);
    }

    Component left, right;

//    baseTab = null; // V2.1 TEST

    if (baseTab != null){
      // base is contained in a tab, so the split will occur between the tab and
      // newDockable (a tabbed component cannot be splitted)
      DockingUtilities.replaceChild(((Component)baseTab).getParent(),
          ((Component)baseTab), split);

      if (position == DockingConstants.SPLIT_TOP
          || position == DockingConstants.SPLIT_LEFT){
        left = (Component)dockableContainer;
        right = (Component)baseTab;
      } else {
        left = (Component)baseTab;
        right = (Component)dockableContainer;
      }
    } else {
      // base is not contained in a tab, so it must be a DockableContainer
      DockableContainer baseContainer = DockingUtilities.findSingleDockableContainer(
          base);

      assert baseContainer != null;

      DockingUtilities.replaceChild(((Component)baseContainer).getParent(),
          ((Component)baseContainer), split);

      if (position == DockingConstants.SPLIT_TOP
          || position == DockingConstants.SPLIT_LEFT){
        left = (Component)dockableContainer;
        right = (Component)baseContainer;
      } else {
        left = (Component)baseContainer;
        right = (Component)dockableContainer;
      }

    }

    // now, we have to fill split with base and dockable
    split.setLeftComponent(left);
    split.setRightComponent(right);

    context.registerDockable(newDockable);
    context.setDockableState(newDockable, newState);
    newDockable.getDockKey().setLocation(DockableState.Location.DOCKED);

    fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));


    removeFromTabbedGroup(newDockable); //2005/10/10

    DockingUtilities.updateResizeWeights(dockingPanel);

    if (proportion >= 0){
        SwingUtilities.invokeLater(new SplitResizer(split, proportion));
    }

  }

  
  /** Splits a Dockable in 2 parts, if possible.
   * <p>
   *  The base dockable is the reference, the second newDockable will be
   * added according to the position parameter.
   * <p>
   *  If base is contained in a non splitable container (like a tab of DockTabbedPane)
   * then, a splitable ancestor will be searched (until the root desktop pane is reached)
   * to apply splitting.
   *
   * @param base   an already docked Dockable
   * @param newDockable   the added dockable
   * @param position  position of newDockable relative to base
   */
  public void split(Dockable base, Dockable newDockable, DockingConstants.Split position){
      split(base, newDockable, position, -1);
  }
  
  /** Replaces the base component by a split pane oriented according to position,
   * and put dockable at position.
   * <p>
   * <b>Note : </b> This method is for DockableContainers. API users should
   * use the {@link #split(Dockable,Dockable,DockingConstants.Split) split} method.
   *
   * @param base      the reference component (a dockablecontainer or a split container)
   * @param dockable  the dockable to add
   * @param position  the position of <code>dockable</code>.
   *
   * */
  public void splitComponent(Component base, Dockable dockable, DockingConstants.Split position){
    /* splitComponent() is called from SplitContainer and BorderSplitter
     *  BorderSplitter is used by :
     *    - DockView (not floating, as the DetachedDockView child overrides the scanDrop method and doesn't use the BorderSplitter
     *    - DockTabbedPane (only when not floating)
     * SplitContainer is used for DnD operations that can occur from Hide/Docked/Floating elements
     *
     *=> The future state will be DOCKED, but we cannot guess the current state (HIDE/FLOAT/DOCKED)
     *=> as of 2.1 : the future state can be DOCKED or FLOATING (with the help of compound dockable)
     */
    
    if (base == null) throw new NullPointerException("base");
    if (dockable == null) throw new NullPointerException("dockable");
    
    context.registerDockable(dockable);
    
    //int futureState = DockableState.DOCKED;
    DockableState.Location futureLocation = DockingUtilities.getDockableLocationFromHierarchy(base);
    /*if (!dockingPanel.isAncestorOf(base)){
      futureState = DockableState.FLOATING;
    }*/
    
    DockableState currentState = getDockableState(dockable);
    DockableState.Location currentLocation = getLocation(currentState);
    
    boolean stateChange = false;
    if (currentState == null){
      stateChange = true;
    } else if (currentState.getLocation() != futureLocation){
      stateChange = true;
    }
    
    DockableState newState = new DockableState(this, dockable, futureLocation);
//        DockableState.DOCKED);
    DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(
        currentState, newState);
    DockingActionEvent dae = new DockingActionSplitComponentEvent(this, dockable, currentLocation,
        newState.getLocation(), base, position, 0.5f);
    
    if (!isDockingActionAccepted(dae, dswe)) {
      return; // vetoed
    }
    if (stateChange){
      if (currentState.isFloating()){
        removePreviousFloatingState(dockable); // clear reference of the previous state
      }
    }
    
    Container oldContainer = (Container)DockingUtilities.findSingleDockableContainer(
        dockable);
    int oldWidth = 0;
    int oldHeight = 0;
    if (oldContainer != null){
      oldWidth = oldContainer.getWidth();
      oldHeight = oldContainer.getHeight();
    } else {
      oldWidth = base.getWidth() / 2;
      oldHeight = base.getHeight()/2;
    }
    
    DockableContainer dockableContainer = DockableContainerFactory.getFactory().
        createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
    dockableContainer.installDocking(this);
    
    // create a new splitcontainer that will replace baseDockable's container
    SplitContainer split;
    if (position == DockingConstants.SPLIT_TOP
        || position == DockingConstants.SPLIT_BOTTOM){
      split = new SplitContainer(JSplitPane.VERTICAL_SPLIT);
    } else /*if (position == DockingConstants.SPLIT_LEFT
        || position == DockingConstants.SPLIT_RIGHT)*/{
      split = new SplitContainer(JSplitPane.HORIZONTAL_SPLIT);
    }
    
    if (base != dockingPanel){ // 2005/11/08 support for splitting from dockingPanel
      DockingUtilities.replaceChild(base.getParent(), base, split);
      
      if (position == DockingConstants.SPLIT_TOP
          || position == DockingConstants.SPLIT_LEFT){
        split.setLeftComponent((Component)dockableContainer);
        split.setRightComponent(base);
      } else {
        split.setRightComponent((Component)dockableContainer);
        split.setLeftComponent(base);
      }
    } else {
      // docking panel must remain the top level ancestor
      if (dockingPanel.getComponentCount() != 0){
        Component dockingPanelChild = dockingPanel.getComponent(0);
        DockingUtilities.replaceChild(dockingPanel, dockingPanelChild, split);
        if (position == DockingConstants.SPLIT_TOP
            || position == DockingConstants.SPLIT_LEFT){
          split.setLeftComponent((Component)dockableContainer);
          split.setRightComponent(dockingPanelChild);
        } else {
          split.setRightComponent((Component)dockableContainer);
          split.setLeftComponent(dockingPanelChild);
        }
      } else { // dockingpanel is empty
        dockingPanel.add((Component) dockableContainer);
      }
      
    }
    
//    if (oldContainer != null){
    // now try to keep same size as before the drop
    // we do this in invokelater because split.getWidth()/getHeight() returns 0 at this time
    // and we need this value to do a correct setDividerLocation.
    
    if (position == DockingConstants.SPLIT_TOP) {
      SwingUtilities.invokeLater(new SplitResizer(split, oldHeight));
    } else if (position == DockingConstants.SPLIT_BOTTOM) {
      SwingUtilities.invokeLater(new SplitResizer(split, -oldHeight));
    } else if (position == DockingConstants.SPLIT_LEFT) {
      SwingUtilities.invokeLater(new SplitResizer(split, oldWidth));
    } else if (position == DockingConstants.SPLIT_RIGHT) {
      SwingUtilities.invokeLater(new SplitResizer(split, -oldWidth));
    }
//    }
    
    context.setDockableState(dockable, newState);
    
    if (stateChange){
      if (futureLocation == DockableState.Location.FLOATING ){
        // splitting from ? (should be Docked) to  floating
        // we have to store a return state.
        storePreviousFloatingState(dockable, currentState);
      }
    }
    
    dockable.getDockKey().setLocation(futureLocation);
    fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    
    
    removeFromTabbedGroup(dockable); //2005/10/10
    DockingUtilities.updateResizeWeights(dockingPanel);
  }
  
  
  /** Replaces the base component by a split pane oriented according to position,
   * and put the dockablesContainer at position.
   * <p>
   * <b>Note : </b> This method is for DockableContainers. API users should
   * use the {@link #split(Dockable,Dockable,DockingConstants.Split) split} method.
   *
   * @param base      the reference component (a dockablecontainer or a split container)
   * @param dockablesContainer  the dockable container
   * @param position  the position of <code>dockable</code>.
   *
   * */
  public void splitComponent(Component base, Container dockablesContainer, DockingConstants.Split position){
    if (dockablesContainer instanceof SingleDockableContainer){
      splitComponent(base, ((SingleDockableContainer)dockablesContainer).getDockable(), position);
    } else if (dockablesContainer instanceof TabbedDockableContainer){
      splitTab(base, (TabbedDockableContainer)dockablesContainer, position);
    } else {
      System.err.println("unknow split request !" + dockablesContainer );
    }
  }
  
  private void splitTab(Component base, TabbedDockableContainer tdc, DockingConstants.Split position){
    // should be used only when drag and dropping
    
    if (base == null) throw new NullPointerException("base");
    Dockable firstDockable = tdc.getDockableAt(0);
    DockableState currentState = getDockableState(firstDockable);
    boolean stateChange = false;
    if (currentState.isFloating()){
      removePreviousFloatingStates(tdc); // clear reference of the previous state
      stateChange = true;
    }
    
    Container oldContainer = (Container)tdc;
    int oldWidth = 0;
    int oldHeight = 0;
    if (oldContainer != null){
      oldWidth = oldContainer.getWidth();
      oldHeight = oldContainer.getHeight();
    } else {
      oldWidth = base.getWidth() / 2;
      oldHeight = base.getHeight()/2;
    }
    
    DockableContainer dockableContainer = tdc;
    
    // create a new splitcontainer that will replace baseDockable's container
    SplitContainer split;
    if (position == DockingConstants.SPLIT_TOP
        || position == DockingConstants.SPLIT_BOTTOM){
      split = new SplitContainer(JSplitPane.VERTICAL_SPLIT);
    } else /*if (position == DockingConstants.SPLIT_LEFT
        || position == DockingConstants.SPLIT_RIGHT)*/{
      split = new SplitContainer(JSplitPane.HORIZONTAL_SPLIT);
    }
    
    if (base != dockingPanel){ // 2005/11/08 support for splitting from dockingPanel
      DockingUtilities.replaceChild(base.getParent(), base, split);
      
      if (position == DockingConstants.SPLIT_TOP
          || position == DockingConstants.SPLIT_LEFT){
        split.setLeftComponent((Component)dockableContainer);
        split.setRightComponent(base);
      } else {
        split.setRightComponent((Component)dockableContainer);
        split.setLeftComponent(base);
      }
    } else {
      // docking panel must remain the top level ancestor
      if (dockingPanel.getComponentCount() != 0){
        Component dockingPanelChild = dockingPanel.getComponent(0);
        DockingUtilities.replaceChild(dockingPanel, dockingPanelChild, split);
        if (position == DockingConstants.SPLIT_TOP
            || position == DockingConstants.SPLIT_LEFT){
          split.setLeftComponent((Component)dockableContainer);
          split.setRightComponent(dockingPanelChild);
        } else {
          split.setRightComponent((Component)dockableContainer);
          split.setLeftComponent(dockingPanelChild);
        }
      } else { // dockingpanel is empty
        dockingPanel.add((Component) dockableContainer);
      }
      
    }
    
    //if (oldContainer != null){
    // now try to keep same size as before the drop
    // we do this in invokelater because split.getWidth()/getHeight() returns 0 at this time
    // and we need this value to do a correct setDividerLocation.
    
    if (position == DockingConstants.SPLIT_TOP) {
      SwingUtilities.invokeLater(new SplitResizer(split, oldHeight));
    } else if (position == DockingConstants.SPLIT_BOTTOM) {
      SwingUtilities.invokeLater(new SplitResizer(split, -oldHeight));
    } else if (position == DockingConstants.SPLIT_LEFT) {
      SwingUtilities.invokeLater(new SplitResizer(split, oldWidth));
    } else if (position == DockingConstants.SPLIT_RIGHT) {
      SwingUtilities.invokeLater(new SplitResizer(split, -oldWidth));
    }
    //}
    
    if (stateChange){
      // this part is tricky : we assume the caller is DragContoller (it's the only
      // legal way to invoke this method). DragController triggers event change
      
      for (int i=0; i < tdc.getTabCount(); i++){
        Dockable d = tdc.getDockableAt(i);
        DockableState newState = new DockableState(this, d, DockableState.Location.DOCKED);
        context.setDockableState(d, newState);
      }
    }
    
//    registerDockable(dockable);
//    dockableStates.put(dockable, newState);
    
//    if (stateChange){
//      dockable.getDockKey().setLocation(DockableState.DOCKED);
//      fireDockableStateChange(new DockableStateChangeEvent(currentState, newState));
//    }
//
//    removeFromTabbedGroup(dockable); //2005/10/10
//    DockingUtilities.updateResizeWeights(dockingPanel);
  }
  
  
  
  /** Moves a dockable to another position (relative to a destination dockable)
   *
   *
   * @param dockable  must be a registered Dockable
   * @param base  must be a visible Dockable
   * @param position  relative positionning
   */
  public void move(Dockable base, Dockable dockable, DockingConstants.Split position){
    if (base == null) throw new NullPointerException("base must not be null");
    if (dockable == null) throw new NullPointerException("dockable must not be null");
    if (dockable == base) return;
    
    remove(dockable);
    
    removeFromTabbedGroup(dockable); // 2005/07/13
    
    split(base, dockable, position);
  }
  
  
  
  
  /** Shows again a dockable (if previously hidden, floating or closed).
   * <p>
   * The dockable is inserted in the docking desktop where it was before, if possible.
   * if not, an approximated place will be looked for.
   * */
  private void show(Dockable dockable, DockingActionEvent action){
    if (dockable == null) throw new NullPointerException("dockable must not be null");
    
    DockableState state = context.getDockableState(dockable);
    RelativeDockablePosition position = state.getPosition();
    
    if (position == null){
      // not shown before, ask best position to the preferences
      // TODO
      throw new NullPointerException("trying to show a view that was not hidden");
    }
    
    LinkedList group = tabbedGroups.get(dockable); //2005/07/13...
    boolean tabbed = false;
    if (group != null){
      // look for a still visible dockable in the group
      Iterator it = group.iterator();
      while (it.hasNext() && ! tabbed){
        Dockable d = (Dockable) it.next();
        if (d != dockable && getDockableState(d).isDocked()){
          createTab(d, dockable, Integer.MAX_VALUE, true, false);
          tabbed = true;
        }
      }
    }
    
    if (! tabbed){ // ...2005/07/13
      SingleDockableContainer sdc = RelativeDockingUtilities.applyDockingAction(dockable, action);
//      SingleDockableContainer sdc = RelativeDockingUtilities.insertDockable(
//          position.getRelativeAncestorContainer(), dockable, position);
      sdc.installDocking(this);
    }
    
  }
  
  /** Removes a visible dockable from the containment hierarchy (including autohide border buttons).
   * <p>
   * This method shouldn't be used by user applications (only by the framework).
   * <p>
   * To poperly remove a component from the desktop, use the close(Dockable) method instead
   * <p>
   * If the dockable is not visible, an error will occur
   * <p>
   * This method does not unregister the dockable.
   *
   * @throws IllegalArgumentException if the dockable is not visible (docked or auto-hide)
   *  */
  public void remove(Dockable dockable) {
    // TODO : check this : split this method in two (removeImpl() should be used form inside the framework, and
    // remove should trigger CLOSE state change events
    
    //if (dockingPanel.isAncestorOf(dockable.getComponent())){ //2005/10/06 ...
    DockableState.Location dockLocation = dockable.getDockKey().getLocation();
    SingleDockableContainer dockableContainer = null;
    Container parentOfSdc = null;
    
    boolean isChildOfCompoundDockable = false; // v2.1
    
    if ( dockLocation == DockableState.Location.DOCKED
        || dockLocation == DockableState.Location.FLOATING){ // ...2005/10/06
      
      // we must guess that before removing the dockable from its parent...
      isChildOfCompoundDockable = DockingUtilities.isChildOfCompoundDockable(dockable);
      
      dockableContainer = DockingUtilities.findSingleDockableContainer(dockable);
      parentOfSdc = ((JComponent)dockableContainer).getParent();
      removeContainer(dockableContainer);
    }
    
    if (dockLocation == DockableState.Location.FLOATING){
      FloatingDockableContainer fdc = (FloatingDockableContainer) SwingUtilities.getWindowAncestor(dockable.getComponent());
      if (parentOfSdc instanceof TabbedDockableContainer){
        // dockable was contained in a tab on the floatable : we must not dispose the window
      } else if (isChildOfCompoundDockable){
        // dockable was a child of a compound dockable : don't dispose
      } else {
        DockingUtilities.dispose(fdc);
        //((JDialog)dlg).dispose();
      }
    } else if (dockLocation == DockableState.Location.HIDDEN){
//      DockableState state = (DockableState) dockableStates.get(dockable);
      
//      if (state.isHidden()) {
      // component is removed from the auto-hide list
      //dockableStates.put(dockable, new DockableState(dockable, DockableState.CLOSED));
      // 2005/10/10 removed this : wrong place to change a dockable state
      
      AutoHideButton btn = autoHideButtons.get(dockable.
          getDockKey());
      
      if (btn == null){
        // (v2.1) no button : that means this dockable is a child of a hidden compound dockable
        
        // so we just use the standard removing pattern
        dockableContainer = DockingUtilities.findSingleDockableContainer(dockable);
        parentOfSdc = ((JComponent)dockableContainer).getParent();
        removeContainer(dockableContainer);
      } else {
        // single auto-hide dockable
        btn.setVisible(false);
        expandPanel.remove(dockable); // in case the dockable was the current visible dockable
      }
      
      
      revalidate();
/*      } else if (state.isFloating()){
 
        Window w = SwingUtilities.getWindowAncestor(dockable.getComponent());
        w.dispose();
      } else { // well, it's not shown, and not hidden...
        // nop
      }*/
    }
  }
  
  /** Removes a visible dockable : called from a drag and drop operation.
   *<p> Don't call this method directly, as it is meant to be used only by the drag and drop
   *  event components.
   */
  public void dropRemove(DockableDragSource dragSource){
    // before removing the dockable, we update its state to store its new relative position
    Dockable d = dragSource.getDockable();
    DockableState state = context.getDockableState(d);
    if (state != null){
      // some drag source don't have state associated with them (like whole TabbedContainers)
      DockableState updatedState = new DockableState(state, new RelativeDockablePosition(getRelativeAncestorContainer(d), d));
      context.setDockableState(dragSource.getDockable(), updatedState);
    }
    
    Container dockableContainer = dragSource.getDockableContainer();
    if (dockableContainer instanceof TabbedDockableContainer){
      remove((TabbedDockableContainer)dockableContainer);
    } else {
      remove(dragSource.getDockable());
    }
    //removeFromTabbedGroup(dockable); 2005/10/10 don't remove from tab : it's too early to be sure we won't need it again
    // for example : when removing from a tab to create a floating tab (we have to keep the old attachment)
  }
  
  
  /** Removes a whole tab container (which is beeing moved to somewhere else)
   *<p>
   * This operation is done only during a drag and drop process.
   *
   * @throws IllegalArgumentException if the dockable is not visible (docked or auto-hide)
   *
   */
  private void remove(TabbedDockableContainer tdc) {
        
    // TODO : check this : split this method in two (removeImpl() should be used form inside the framework, and
    // remove should trigger CLOSE state change events
    Dockable firstDockable = tdc.getDockableAt(0);
    //if (dockingPanel.isAncestorOf(dockable.getComponent())){ //2005/10/06 ...
    DockableState.Location dockLocation = firstDockable.getDockKey().getLocation();
    
    if (dockLocation == DockableState.Location.FLOATING){
      FloatingDockableContainer fdc = (FloatingDockableContainer) SwingUtilities.getWindowAncestor((Container) tdc);
      DockingUtilities.dispose(fdc);
      //((JDialog)dlg).dispose();
    }
    
    boolean invalidateDesktop = true; // always, except for floating dockables
    
    Component parent = ((Component)tdc).getParent();
    
    if (parent != null){
      if (parent instanceof SplitContainer) {
        SplitContainer viewParent = (SplitContainer) parent;
        if (viewParent.getLeftComponent() == tdc) {
          viewParent.remove((Component)tdc);
          Component other = viewParent.getRightComponent();
          // replace viewParent by other in viewParent's hierarchy
          Container viewGParent = (Container) viewParent.getParent();
          DockingUtilities.replaceChild(viewGParent, viewParent, other);
        } else {
          viewParent.remove((Component)tdc);
          Component other = viewParent.getLeftComponent();
          Container viewGParent = (Container) viewParent.getParent();
          DockingUtilities.replaceChild(viewGParent, viewParent, other);
        }
      } else if (parent == dockingPanel) { // no more views to show
        dockingPanel.remove(0);
      } else if (SwingUtilities.getWindowAncestor(parent) instanceof FloatingDockableContainer){
        // removing from a FloatingDockableContainer... nothing to do
        invalidateDesktop = false;
      } else {
        throw new IllegalStateException("View is not contained in desktop hierarchy " + parent);
      }
      
      if (invalidateDesktop){
        dockingPanel.invalidate();
        dockingPanel.validate();
        dockingPanel.repaint();
      }
    }
    
    
  }
  
  
  /** Extends the size of this dockable to fill the docking panel.
   *<p>
   *  The component must be docked before beeing maximized, otherwise an IllegalArgumentException will
   *  be thrown.
   *<p>
   * The opposite method of maximize is restore(Dockable).
   * @see #restore(Dockable)
   * */
  public void maximize(Dockable dockable){
    if (dockable.getDockKey().getLocation() != DockableState.Location.DOCKED){
      // development error (could have been an assertion).
      throw new IllegalArgumentException("Dockable isn't currently in the DOCKED state : " + dockable);
    }
    
    
    DockableState currentState = getDockableState(dockable);
    boolean stateChange = currentState == null || !currentState.isMaximized();
    
    DockableState newState = new DockableState(this, dockable, DockableState.Location.MAXIMIZED);
    
    DockableState.Location currentLocation = getLocation(currentState);
    DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(
        currentState, newState);
    DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, dockable,
        currentLocation, DockableState.Location.MAXIMIZED);
    
    if (!isDockingActionAccepted(dae, dswe)){
      return; // vetoed
    }
    
    // v2.1 : if the currently maximized dockable is a compound dockable, than its children
    // can still be maximized.
    // to avoid "stacking" maximized component, we just "restore" the compound dockable
    SingleDockableContainer currentMaximizedContainer = (SingleDockableContainer)maximizedComponent;
    if (currentMaximizedContainer != null && currentMaximizedContainer.getDockable() != dockable){
      // there's already a dockable which is maximized : restore it first
      restore(currentMaximizedContainer.getDockable());
    }
    
    
    
    SingleDockableContainer sdc = DockingUtilities.findSingleDockableContainer(dockable);
    Component dockableContainer = (Component) sdc;
    DockingUtilities.replaceChild(dockableContainer.getParent(), dockableContainer, dummyMaximedReplacer);
    SingleDockableContainer maxDockableContainer  = DockableContainerFactory.getFactory()
    .createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_DESKTOP);
    
    maxDockableContainer.installDocking(this);
    
    this.maximizedComponent = (Component) maxDockableContainer;
    Insets i = getDockingPanelInsets();
    if (DockingPreferences.isLightWeightUsageEnabled()){
      maximizedComponent.setBounds(i.left,i.top, getWidth() - i.left - i.right,
          getHeight() - i.top - i.bottom);
      add(maximizedComponent, JLayeredPane.PALETTE_LAYER);
    } else {
      this.currentMaximizedComponentIsHeavyWeight = false;
      if (DockingPreferences.isSingleHeavyWeightComponent()){
        // first, check if the heavyweight component IS this dockable
        Component comp = dockable.getComponent(); //2005/10/20 support for single heavyweight
        if (DockingUtilities.isHeavyWeightComponent(comp)){
          // it's the heavyweight component (and the only one)
          // so we just have to put it there (no need for an in-between awt panel)
          maximizedComponent.setBounds(i.left,i.top, getWidth() - i.left - i.right,
              getHeight() - i.top - i.bottom);
          add(maximizedComponent, JLayeredPane.PALETTE_LAYER);
          currentMaximizedComponentIsHeavyWeight = true;
        } else {
          // this one is not the heavyweight : we have to put it on top of a fresh awt Panel
          Panel awtPanel = new Panel(new BorderLayout());
          awtPanel.setBounds(i.left,i.top, getWidth() - i.left - i.right,
              getHeight() - i.top - i.bottom);
          awtPanel.add(maximizedComponent, BorderLayout.CENTER);
          awtPanel.validate();
          add(awtPanel, JLayeredPane.PALETTE_LAYER);
        }
      } else {
        // if there are heavyweight components around, we need to add an in-between
        // awt Panel to avoid those components to be drawn on top of the maximized component
        Panel awtPanel = new Panel(new BorderLayout());
        awtPanel.setBounds(i.left,i.top, getWidth() - i.left - i.right,
            getHeight() - i.top - i.bottom);
        awtPanel.add(maximizedComponent, BorderLayout.CENTER);
        awtPanel.validate();
        add(awtPanel, JLayeredPane.PALETTE_LAYER);
      }
    }
    
    context.setDockableState(dockable, newState);
    
    dockable.getDockKey().setLocation(DockableState.Location.MAXIMIZED);
    fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    
    dockable.getComponent().requestFocus();
    
    repaint();
    
  }
  
  /** Restore the dockable to the docking position it was before beeing maximized.
   *<p>
   * The dockable must be already maximized (otherwise an IllegalArgumentException will be thrown)
   *
   * @see #maximize(Dockable)
   * */
  public void restore(final Dockable dockable){
    if (dockable.getDockKey().getLocation() != DockableState.Location.MAXIMIZED){
      // development error (could have been an assertion).
      throw new IllegalArgumentException("Dockable isn't currently in the MAXIMIZED state : " + dockable);
    }
    
    DockableState currentState = getDockableState(dockable);
    boolean stateChange = currentState != null || currentState.isMaximized();
    DockableState newState = new DockableState(this, dockable, DockableState.Location.DOCKED);
    DockableState.Location currentLocation = getLocation(currentState);
    
    DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(
        currentState, newState);
    DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, dockable, 
            currentLocation, DockableState.Location.DOCKED);
    
    if (! isDockingActionAccepted(dae, dswe)){
      return; // vetoed
    }
    
    if (!DockingPreferences.isLightWeightUsageEnabled()){
      if (DockingPreferences.isSingleHeavyWeightComponent() && this.currentMaximizedComponentIsHeavyWeight){ // 2005/10/20
        remove(maximizedComponent);
      } else {
        remove(maximizedComponent.getParent()); // remove the awt panel
      }
    } else {
      remove(maximizedComponent);
    }
    ((SingleDockableContainer) maximizedComponent ).uninstallDocking(this);
    
    SingleDockableContainer sdc = null;
    if (dummyMaximedReplacer.getParent() instanceof TabbedDockableContainer){
      sdc = DockableContainerFactory.getFactory().createDockableContainer(dockable,
          DockableContainerFactory.ParentType.PARENT_TABBED_CONTAINER);
    } else {
      sdc = DockableContainerFactory.getFactory().createDockableContainer(dockable,
          DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
    }
    
    DockingUtilities.replaceChild(dummyMaximedReplacer.getParent(), dummyMaximedReplacer, (Component)sdc);
    
    context.setDockableState(dockable, newState);
    dockable.getDockKey().setLocation(DockableState.Location.DOCKED);
    fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        dockable.getComponent().requestFocus();
      }
    });
    
    sdc.installDocking(this);
    
    maximizedComponent = null;
    
    repaint();
  }
  
  
  /** Detach or attach the dockable from/to the desktop.
   * <p>
   * When detached, the dockable is centered on the desktop.
   */
  public void setFloating(final Dockable dockable, boolean floating){
    setFloating(dockable, floating, null);
  }
  
  /** Used only with DOCKED dockables : returns the nearest ancestor container
   * (can be a CompoundDockingPanel if nested, or the DockingPanel).
   */
  private Container getRelativeAncestorContainer(Dockable dockable){
    Container compoundAncestor = DockingUtilities.findCompoundAncestorContainer(dockable);
    if (compoundAncestor != null){
      return compoundAncestor;
    } else {
      return dockingPanel;
    }
  }
  
  /** Detach or attach the dockable from/to the desktop.
   *<p>
   * show the dockable at sceenPosition (when not null) or centered (if screenPosition is null)
   *
   */
  public void setFloating(final Dockable dockable, boolean floating, Point screenPosition){
    if (dockable == null)throw new NullPointerException("dockable");
    
    DockableState currentState = getDockableState(dockable);
    DockableState.Location currentLocation = getLocation(currentState);
    SingleDockableContainer dockableContainer = DockingUtilities.
        findSingleDockableContainer(dockable);
    
    if (floating){
      DockableState.Location location = dockable.getDockKey().getLocation();
      RelativeDockablePosition position = null;
      switch (location){
          case DOCKED:
              position = new RelativeDockablePosition(getRelativeAncestorContainer(dockable), dockable);
              break;
          case HIDDEN:
            position = currentState.getPosition(); // get the position that was stored before
            break;
          case FLOATING:
            // from floating..to floating. It's still possible, if the component was previously tabbed (now it will have its
            // own window
            if (DockingUtilities.findTabbedDockableContainer(dockable) != null){
              // ok, it was tabbed and floating
            } else {
              throw new IllegalArgumentException("floating not tabbed");
            }
            break;
          case CLOSED:
              break;
          default:
            throw new IllegalArgumentException("not docked " + location);
      }

      DockableState newState = new DockableState(this, dockable, DockableState.Location.FLOATING, position);
      DockableStateWillChangeEvent event = new DockableStateWillChangeEvent(currentState, newState);
      
      if (location == DockableState.Location.FLOATING){
        // floating to floating (create a new floating dialog by removing a dockable from a detached tab)
        // note (@todo): as of version 2.1, we should trigger a dockingActionEvent here... but which one ?
        // an addDockable with a floating state ?
        Dimension previousSize = null;
        remove(dockable);
        if (dockableContainer !=null){
          previousSize = ((Component)dockableContainer).getSize();
        }
        FloatingDockableContainer fdc = createFloatingDockableContainer(dockable);
        //JDialog dialog  = (JDialog) fdc;
        
        if (previousSize != null){
          DockingUtilities.setSize(fdc, previousSize); //2006/02/20
          DockingUtilities.validate(fdc);
          //dialog.setSize(previousSize);
          //dialog.validate();
        } else {
          DockingUtilities.pack(fdc);
          //dialog.pack();
        }
        
        if (screenPosition == null){
          DockingUtilities.setLocationRelativeTo(fdc, this);
          //dialog.setLocationRelativeTo(this);
        } else {
          DockingUtilities.setLocation(fdc, screenPosition);
          //dialog.setLocation(screenPosition);
        }
        DockingUtilities.setVisible(fdc, true);
        //dialog.setVisible(true);
        
      } else if (currentState != null){
        DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, dockable, currentLocation,
            DockableState.Location.FLOATING);
        if (isDockingActionAccepted(dae, event)){
          // not already floating so we trigger state events
          Dimension previousSize = null;
          if (dockableContainer !=null){
            previousSize = ((Component)dockableContainer).getSize();
          }
          remove(dockable);
          
          // store a return state, based on the current state + updated relative position
          storePreviousFloatingState(dockable, new DockableState(currentState, position));
          
          FloatingDockableContainer fdc = createFloatingDockableContainer(dockable);
          //JDialog dialog  = (JDialog) fdc;
          
          if (previousSize != null){
            DockingUtilities.setSize(fdc, previousSize);
            DockingUtilities.validate(fdc);
            //dialog.setSize(previousSize);
            //dialog.validate();
          } else {
            DockingUtilities.pack(fdc);
            //dialog.pack();
          }
          
          if (screenPosition == null){
            DockingUtilities.setLocationRelativeTo(fdc, this);
            //dialog.setLocationRelativeTo(this);
          } else {
            DockingUtilities.setLocation(fdc, screenPosition);
            //dialog.setLocation(screenPosition);
          }
          DockingUtilities.setVisible(fdc, true);
          //dialog.setVisible(true);
          
          context.setDockableState(dockable, newState);
          dockable.getDockKey().setLocation(DockableState.Location.FLOATING);
          fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
          
          if (dockable instanceof CompoundDockable){
            updateCompoundChildrenState((CompoundDockable)dockable, DockableState.Location.FLOATING);
          }
          
          DockingUtilities.updateResizeWeights(dockingPanel);
          revalidate();
        }
      }
    } else { // attach !
      // remove the button, show again the view
      
      // was the dockable docked or hidden ?
      DockableState previousState = getPreviousDockableState(dockable);
      
      DockableState newState = new DockableState(this, dockable, previousState.getLocation(), previousState.getPosition());
      DockableStateWillChangeEvent event = new DockableStateWillChangeEvent(currentState, newState);
      DockingActionEvent dae;
      if (currentState != null && currentState.isFloating()){
        if (previousState.isDocked()){
          
          RelativeDockablePosition prevPos = previousState.getPosition();
          Container relativeAncestor = prevPos.getRelativeAncestorContainer();
          if (relativeAncestor == null){
            relativeAncestor = dockingPanel; // safety : @todo we have to refactor xmlBuildFloatingNode to
            // store the relativeAncestorContainer
          }
          dae = RelativeDockingUtilities.getInsertionDockingAction(
              relativeAncestor, dockable, currentState, newState);
        } else if (previousState.isHidden()){ // it was hidden
          dae = new DockingActionSimpleStateChangeEvent(this, dockable, DockableState.Location.FLOATING, DockableState.Location.HIDDEN);
        } else {
          // shoudn't be possible
          throw new RuntimeException();
        }
        if (isDockingActionAccepted(dae, event)){
          removePreviousFloatingState(dockable);
          
          Container parentOfSdc = null; //2005/10/07
          parentOfSdc = ((JComponent)dockableContainer).getParent();
          removeContainer(dockableContainer);
          FloatingDockableContainer fdc = (FloatingDockableContainer) SwingUtilities.getWindowAncestor(dockable.getComponent());
          if (parentOfSdc instanceof TabbedDockableContainer){
            // dockable was contained in a tab on the floatable : we must not dispose the window
          } else {
            DockingUtilities.dispose(fdc);
            //((JDialog)fdc).dispose();
          }
          
          context.setDockableState(dockable, newState);
          
          if (previousState.isDocked()){
            show(dockable, dae); // TODO Fix this null
          } else if (previousState.isHidden()){ // it was hidden
            floatingToHide(dockable, previousState.getPosition());
          } else { // unmanaged state
            throw new RuntimeException();
          }
          
          dockable.getDockKey().setLocation(previousState.getLocation());
          
          fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
          
          dockable.getComponent().requestFocus();
          
          if (dockable instanceof CompoundDockable){
            updateCompoundChildrenState((CompoundDockable)dockable, newState.getLocation());
          }
          
          DockingUtilities.updateResizeWeights(dockingPanel);
          revalidate();
          
        }
      }
      
    }
  }
  
  /** Detach or attach a whole tabbed dockable container from/to the desktop.
   *<p>
   * displays the component at sceenPosition (when not null) or centered (if screenPosition is null)
   *
   */
  void setFloating(final TabbedDockableContainer tdc, Point screenPosition){
    /* package protected, used by OldDragControler to detach a tabbedcontainer */
    
    
    Dockable firstDockable = tdc.getDockableAt(0);
    // all dockables have the same state, as they are in the same tabpane
    
    DockableState currentState = getDockableState(firstDockable);
    
    if (!currentState.isDocked()){
      throw new IllegalArgumentException("not docked");
    } else {
      if (checkDockableStateWillChange(tdc, DockableState.Location.FLOATING)){
        RelativeDockablePosition position = new RelativeDockablePosition(dockingPanel, firstDockable);
        // no veto has been raised by the compound dockables
        Dimension previousSize = ((Component)tdc).getSize();
        
        remove(tdc);
        storePreviousFloatingStates(tdc);
        FloatingDockableContainer fdc = createFloatingDockableContainer(tdc);
        //JDialog dialog  = (JDialog) fdc;
        //dialog.setSize(previousSize);
        //dialog.validate();
        DockingUtilities.setSize(fdc, previousSize);
        DockingUtilities.validate(fdc);
        if (screenPosition == null){
          DockingUtilities.setLocationRelativeTo(fdc, this);
          //dialog.setLocationRelativeTo(this);
        } else {
          DockingUtilities.setLocation(fdc, screenPosition);
          //dialog.setLocation(screenPosition);
        }
        DockingUtilities.setVisible(fdc, true);
        //dialog.setVisible(true);
        fireStateChanged(tdc, DockableState.Location.FLOATING, position);
        DockingUtilities.updateResizeWeights(dockingPanel);
        revalidate();
      }
    }
  }
  
  private void storePreviousFloatingStates(TabbedDockableContainer tdc){
    for (int i=0; i < tdc.getTabCount(); i++){
      Dockable d = tdc.getDockableAt(i);
      DockableState current = getDockableState(d);
      storePreviousFloatingState(d, current);
    }
  }
  
  private boolean checkDockableStateWillChange(TabbedDockableContainer tdc, DockableState.Location futureLocation){
    for (int i=0; i < tdc.getTabCount(); i++){
      Dockable d = tdc.getDockableAt(i);
      DockableState currentState = getDockableState(d);
      DockableState.Location currentLocation = getLocation(currentState);
      DockableState newState = new DockableState(this, d, futureLocation, null);
      DockableStateWillChangeEvent event = new DockableStateWillChangeEvent(currentState, newState);
      DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, d, currentLocation, newState.getLocation());
      if (!isDockingActionAccepted(dae,event)){
        return false;
      }
    }
    return true;
  }
  
  private void fireStateChanged(TabbedDockableContainer tdc, DockableState.Location futureLocation, RelativeDockablePosition position){
    for (int i=0; i < tdc.getTabCount(); i++){
      Dockable d = tdc.getDockableAt(i);
      DockableState currentState = getDockableState(d);
      DockableState.Location currentLocation = getLocation(currentState);
      DockableState newState = new DockableState(this, d, futureLocation, position);
      
      context.setDockableState(d, newState);
      d.getDockKey().setLocation(DockableState.Location.FLOATING);
      DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, d, currentLocation, newState.getLocation());
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    }
  }
  
  private DockableState removePreviousFloatingStates(TabbedDockableContainer tdc){
    DockableState first = removePreviousFloatingState(tdc.getDockableAt(0));
    for (int i=1; i < tdc.getTabCount(); i++){
      Dockable d = tdc.getDockableAt(i);
      removePreviousFloatingState(d);
    }
    return first;
  }
  
  /** Returns the previous state of a hidden or floating dockable (when that dockable
   * was in the DOCKED state.
   *@since 2.0.4
   *
   */
  public DockableState getPreviousDockableState(Dockable dockable){
    /* method  mostly used internally */
    return previousFloatingDockableStates.get(dockable);
  }
  
  private DockableState removePreviousFloatingState(Dockable dockable){
    if (dockable instanceof CompoundDockable){
      // we also need to clear states of the compound children
      ArrayList children = DockingUtilities.findCompoundDockableChildren((CompoundDockable)dockable);
      for (int i=0; i < children.size(); i++){
        Dockable d = (Dockable) children.get(i);
        previousFloatingDockableStates.remove(d);
      }
    }
    
    return previousFloatingDockableStates.remove(dockable);
  }
  private void storePreviousFloatingState(Dockable dockable, DockableState state){
    
    // when storing a previous dockable state before going floating, we have
    // to ensure the relativeDockableAncestorContainer is defined
    RelativeDockablePosition pos = state.getPosition();
    if (pos.getRelativeAncestorContainer() == null){
      Container c = DockingUtilities.findCompoundAncestorContainer(dockable);
      if (c != null){
        pos.resetRelativePosition(c, dockable);
      } else {
        pos.resetRelativePosition(dockingPanel, dockable);
      }
    }
    
    previousFloatingDockableStates.put(dockable, state);
    if (dockable instanceof CompoundDockable){
      // more to do : the compund dockable may have children : they will share the
      // same return position
      ArrayList children = DockingUtilities.findCompoundDockableChildren((CompoundDockable)dockable);
      for (int i=0; i < children.size(); i++){
        Dockable d = (Dockable) children.get(i);
        previousFloatingDockableStates.put(d, new DockableState(this, d, state.getLocation(), state.getPosition()));
      }
    }
  }
  
  /** simplified auto-hide version used when the dockable was previously in FLOATING state */
  private void floatingToHide(Dockable dockable, RelativeDockablePosition position){
    // doesn't trigger change event as it is managed by setFloatin(false)
    DockKey k = dockable.getDockKey();
    AutoHideButton btn = autoHideButtons.get(k);
    if (btn == null) {
      btn = new AutoHideButton();
      autoHideButtons.put(k, btn);
      
      int zone;
      if (k.getAutoHideBorder() == null) {
        zone = AutoHidePolicy.getPolicy().getDefaultHideBorder().value();
      } else {
        zone = k.getAutoHideBorder().value();
      }
      btn.init(dockable, zone);
      
      borderPanes[zone].setVisible(true); // border may not be visible
      borderPanes[zone].add(btn);
      borderPanes[zone].revalidate();
    } else { // btn already existing, show it again
      int zone = btn.getZone();
      borderPanes[zone].setVisible(true); // may not be visible
      btn.setVisible(true);
      borderPanes[zone].revalidate();
    }
  }
  
  private void moveFloatingWindows(){ //2005/10/10
    Window w = SwingUtilities.getWindowAncestor(this);
    if (w == null){
      return; // 2007/02/27 fixed NPE
    }
    
    Point newLocation = w.getLocation();
    if (this.lastWindowLocation != null){
      int dx = newLocation.x - lastWindowLocation.x;
      int dy = newLocation.y - lastWindowLocation.y;
      Window [] childWindow = w.getOwnedWindows();
      for (int i=0; i < childWindow.length; i++){
        if (childWindow[i] instanceof FloatingDockableContainer && childWindow[i].isVisible()){
          Point p = childWindow[i].getLocation();
          childWindow[i].setLocation(p.x + dx, p.y +dy);
        }
      }
    }
    
    lastWindowLocation = newLocation;
  }
  
  /** Creates a floating JDialog to be used with this dockable.
   * <p>
   * This method is protected to give access to implementors wanting to
   * customize its look and feel (for example : removing the title bar by
   * setting it to "undecorated").
   * <p>
   * The old API has been removed (it returned a JDialog) as the DockingDesktop can only
   * work with FloatingDockableContainers.
   */
  protected FloatingDockableContainer createFloatingDockableContainer(final Dockable dockable){
    Window ownerWindow = SwingUtilities.getWindowAncestor(this);
    FloatingDockableContainer fdc = DockableContainerFactory.getFactory().createFloatingDockableContainer(ownerWindow);
    
    fdc.installDocking(this);
    
    fdc.setInitialDockable(dockable);
    
    //JDialog dialog = (JDialog) fdc;
    
    KeyStroke ks = (KeyStroke) UIManager.get("DockingDesktop.floatActionAccelerator");
    if (ks != null){
      JRootPane root = DockingUtilities.getRootPane(fdc);
      root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "float");
      root.getActionMap().put("float", floatAction);
    }
    
//    dialog.pack();
    
//    dialog.setVisible(true);
    
    return fdc;
    
  }
  
  /** Creates a floating JDialog to be used for a whole TabbedDockableContainer.
   * <p>
   * This method is protected to give access to implementors wanting to
   * customize its look and feel (for example : removing the title bar by
   * setting it to "undecorated").
   * <p>
   * The old API has been removed (it returned a JDialog) as the DockingDesktop can only
   * work with FloatingDockableContainers.
   */
  protected FloatingDockableContainer createFloatingDockableContainer(TabbedDockableContainer tdc){
    Window ownerWindow = SwingUtilities.getWindowAncestor(this);
    FloatingDockableContainer fdc = DockableContainerFactory.getFactory().createFloatingDockableContainer(ownerWindow);
    
    fdc.installDocking(this);
    
    fdc.setInitialTabbedDockableContainer(tdc);
    
    //JDialog dialog = (JDialog) fdc;
    
    KeyStroke ks = (KeyStroke) UIManager.get("DockingDesktop.floatActionAccelerator");
    if (ks != null){
      JRootPane root = DockingUtilities.getRootPane(fdc);
      root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "float");
      root.getActionMap().put("float", floatAction);
    }
    
//    dialog.pack();
    
//    dialog.setVisible(true);
    
    return fdc;
    
  }
  
  
  
  
  /** Removes a dockable container. */
  protected void removeContainer(SingleDockableContainer dc){
    
    /* to be improved : this method supposes a direct ancestor link
     between DockableContainer and TabbedDockableContainer */
    dc.uninstallDocking(this);
    
    boolean invalidateDesktop = true; // always, except for floating dockables
    
    
    Component parent = ((Component)dc).getParent();
    if (parent != null){
      try {
        if (parent instanceof SplitContainer) {
          SplitContainer viewParent = (SplitContainer) parent;
          if (viewParent.getLeftComponent() == dc) {
            viewParent.remove((Component)dc);
            Component other = viewParent.getRightComponent();
            // replace viewParent by other in viewParent's hierarchy
            Container viewGParent = (Container) viewParent.getParent();
            DockingUtilities.replaceChild(viewGParent, viewParent, other);
          } else {
            viewParent.remove((Component)dc);
            Component other = viewParent.getLeftComponent();
            Container viewGParent = (Container) viewParent.getParent();
            DockingUtilities.replaceChild(viewGParent, viewParent, other);
          }
        } else if (parent instanceof TabbedDockableContainer) {
          TabbedDockableContainer tparent = (TabbedDockableContainer) parent;
          tparent.removeDockable(dc.getDockable());
          
          if (tparent.getTabCount() == 1) { // no more use for tabs
            Dockable last = tparent.getDockableAt(0);
            remove(last);
            /*tparent.removeDockable(last); //2007/11/14*/
            tparent.uninstallDocking(this);
            ((JTabbedPane)tparent).removeChangeListener(focusHandler);
            
            boolean floating = last.getDockKey().getLocation() == DockableState.Location.FLOATING;
            DockableContainer lastContainer = null;
            if (floating) {
              lastContainer = DockableContainerFactory.getFactory().
                  createDockableContainer(last,  DockableContainerFactory.ParentType.PARENT_DETACHED_WINDOW);
            } else {
              lastContainer = DockableContainerFactory.getFactory().
                  createDockableContainer(last,  DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
            }
            lastContainer.installDocking(this);
            
            DockingUtilities.replaceChild( ( (Component) tparent).getParent(), (Component) tparent,
                (Component)lastContainer);
          }
        } else if (parent == dockingPanel) { // no more views to show
          dockingPanel.remove(0);
        } else if (SwingUtilities.getWindowAncestor(parent) instanceof FloatingDockableContainer){
          // removing from a FloatingDockableContainer... nothing to do
          invalidateDesktop = false;
        } else {
          ((Container) parent).remove((Component)dc);
          //throw new IllegalStateException("View is not contained in desktop hierarchy " + parent);
        }
        
        if (invalidateDesktop){
          dockingPanel.invalidate();
          dockingPanel.validate();
          dockingPanel.repaint();
        }
      } catch (Exception e){
      e.printStackTrace();
    }
  }
}

/** Returns the current state of a dockable (CLOSED, HIDDEN, DOCKED, MAXIMIZED, FLOATING) */
public DockableState getDockableState(Dockable dockable){
  return context.getDockableState(dockable);
}


/** Disposes the dockable container of this dockable.
 * <P> If the dockable is not currently displayed (or auto-hidden), this method will do nothing.
 * <P> the dockable remains registered and can later be shown again at the same
 * location, using addDockable(Dockable, RelativeDockablePosition).
 * <p>
 * To have access to the RelativeDockablePosition at closing time, one has to
 * install a DockableEventListener like this :
 * <pre>
 * desk.addDockingActionListener(new DockingActionListener() {
 *     public boolean acceptDockingAction(DockingActionEvent event) {
 *       return true;
 *     }
 *     public void dockingActionPerformed(DockingActionEvent event) {
 *       if (event.getActionType() == DockingActionEvent.ACTION_CLOSE){
 *         closedDockable = ((DockingActionCloseEvent)event).getDockable();
 *         <b> RelativeDockablePostion position</b> = desk.getLocation(closedDockable).getPosition();
 *          // we now have a position we'll be able to reuse in addDockable()
 *       }
 *     }
 *   });
 *
 * </pre>
 *  */
public void close(Dockable dockable){
  // keep track of where the dockable was
  DockableState currentState = getDockableState(dockable);
  
  if (currentState == null || currentState.isClosed()){// 2006/12/01
    return; // no need to continue
  }
  
  DockableState.Location currentLocation = getLocation(currentState);
  
  RelativeDockablePosition position = new RelativeDockablePosition(dockingPanel, dockable);
  DockableState newState = new DockableState(this, dockable, DockableState.Location.CLOSED, position);
  DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
  DockingActionEvent dae = new DockingActionCloseEvent(this, dockable, currentLocation);
  
  if (dockingPanel.isAncestorOf(dockable.getComponent())){
    if (isDockingActionAccepted(dae, dswe)){
      if (dockable instanceof CompoundDockable){
        // V2.1 : propagate closing state to children
        updateCompoundChildrenState((CompoundDockable)dockable, DockableState.Location.CLOSED);
      }
      
      remove(dockable);
      
      removeFromTabbedGroup(dockable); // 2005/07/13
      
      context.setDockableState(dockable, newState);
      dockable.getDockKey().setLocation(DockableState.Location.CLOSED);
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
      DockingUtilities.updateResizeWeights(dockingPanel);
    }
  } else if (currentState.isHidden()) {
    if (isDockingActionAccepted(dae, dswe)){
      if (dockable instanceof CompoundDockable){
        // V2.1 : propagate closing state to children
        updateCompoundChildrenState((CompoundDockable)dockable, DockableState.Location.CLOSED);
      }
      remove(dockable);
      
      removeFromTabbedGroup(dockable); // 2005/07/13
      
      context.setDockableState(dockable, new DockableState(this, dockable,
          DockableState.Location.CLOSED, currentState.getPosition()));
      dockable.getDockKey().setLocation(DockableState.Location.CLOSED);
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    }
  } else if (currentState.isFloating()){
    if (isDockingActionAccepted(dae, dswe)){
      if (dockable instanceof CompoundDockable){
        // V2.1 : propagate closing state to children
        updateCompoundChildrenState((CompoundDockable)dockable, DockableState.Location.CLOSED);
      }
      remove(dockable);
      removeFromTabbedGroup(dockable);
      
      context.setDockableState(dockable, newState);
      dockable.getDockKey().setLocation(DockableState.Location.CLOSED);
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    }
  } else if (currentState.isMaximized()){ // 2006/11/20
    restore(dockable);
    if (getDockableState(dockable).isDocked()){
      // ok restore hasn't been vetoed
      close(dockable); // once again we close, but this time with standard (DOCKED) behaviour
    }
  }
}

/** Iterates through this compound children and properly close them
 * (without removing them from their parent container)
 */
private void updateCompoundChildrenState(CompoundDockable cDockable, DockableState.Location state){
  ArrayList children = DockingUtilities.findCompoundDockableChildren(cDockable);
  for (int i=0; i < children.size(); i++){
    Dockable d = (Dockable) children.get(i);
    d.getDockKey().setLocation(state);
    DockableState childState = getDockableState(d);
    DockableState childNewState = new DockableState(this, d, state);
    context.setDockableState(d, childNewState);
    context.fireDockableStateChange(new DockableStateChangeEvent(childState, childNewState));
  }
}

/** Convenience method to close all dockables of a TabbedDockableContainer except one.
 * <p>
 * Note : is a dockable is not allowed to close (key.isCloseEnabled() returns false) then it
 * won't be closed by this method.
 *<p>
 * If the 'exception' dockable isn't contained in a tab, then nothing will happen.
 */
public void closeAllOtherDockablesInTab(Dockable exception){
  TabbedDockableContainer tabContainer = DockingUtilities.findTabbedDockableContainer(exception);
  if (tabContainer != null){
    ArrayList dockables = new ArrayList(tabContainer.getTabCount()-1);
    for (int i=0; i < tabContainer.getTabCount(); i++){
      if (tabContainer.getDockableAt(i) != exception){
        dockables.add(tabContainer.getDockableAt(i));
      }
    }
    for (int i=0; i < dockables.size(); i++){
      Dockable d = (Dockable) dockables.get(i);
      if (d.getDockKey().isCloseEnabled()){
        this.close(d);
      }
    }
  }
}

/**  Convenience method to close all dockables of the tabbedContainer
 * containing "base" (including the base dockable).
 *<p>
 * see also {@link #closeAllOtherDockablesInTab(Dockable)}.
 * <p>
 * Note : is a dockable is not allowed to close (key.isCloseEnabled() returns false) then it
 * won't be closed by this method.
 *<p>
 * If the 'base' dockable isn't contained in a tab, then nothing will happen.
 *
 */
public void closeAllDockablesInTab(Dockable base){
  TabbedDockableContainer tabContainer = DockingUtilities.findTabbedDockableContainer(base);
  if (tabContainer != null){
    ArrayList dockables = new ArrayList(tabContainer.getTabCount());
    for (int i=0; i < tabContainer.getTabCount(); i++){
      dockables.add(tabContainer.getDockableAt(i));
    }
    for (int i=0; i < dockables.size(); i++){
      Dockable d = (Dockable) dockables.get(i);
      if (d.getDockKey().isCloseEnabled()){
        this.close(d);
      }
    }
  }
}

/** Toggles the position of a view between hidden (true) and docked (false).
 *
 */
public void setAutoHide(Dockable dockable, boolean hide){
    /* setAutoHide is called by user applications (or addHidenDockable() ) and from inside the framework :
     *  - Tab actions (only when in the DOCKED state)
     *  - DockView (listening to DockViewTitleBar property change
     *  - key events from the desktop
     *  - the AutoHideExpandPanel listening to DockViewTitleBar changes
     *
     * this method is not used when hiding from FLOATING state,
     * the "floatingToHide" method is used istead to avoid triggering unused events
     */
  DockKey k = dockable.getDockKey();
  
  DockableState currentState = getDockableState(dockable);
  DockableState.Location currentLocation = getLocation(currentState);
  
  
  if (hide){
    // build a relative position based on the compound container or top container.
    RelativeDockablePosition position = new RelativeDockablePosition(getRelativeAncestorContainer(dockable), dockable);
    DockableState newState = new DockableState(this, dockable, DockableState.Location.HIDDEN, position);
    DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
    DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, dockable,
        currentLocation, DockableState.Location.HIDDEN);
    
    if (currentLocation != DockableState.Location.HIDDEN
        && isDockingActionAccepted(dae, dswe)){
      AutoHideButton btn = autoHideButtons.get(k);
      if (btn == null) {
        btn = new AutoHideButton();
        autoHideButtons.put(k, btn);
        
        int zone;
        if (k.getAutoHideBorder() == null) {
          zone = AutoHidePolicy.getPolicy().getDefaultHideBorder().value();
        } else {
          zone = k.getAutoHideBorder().value();
        }
        btn.init(dockable, zone);
        
        borderPanes[zone].setVisible(true); // border may not be visible
        borderPanes[zone].add(btn);
        borderPanes[zone].revalidate();
      } else { // btn already existing, show it again
        int zone = btn.getZone();
        borderPanes[zone].setVisible(true); // may not be visible
        btn.setVisible(true);
        borderPanes[zone].revalidate();
      }
      // now, remove the view from the splitcontainers
      hide(dockable);
      context.setDockableState(dockable, newState);
      k.setLocation(DockableState.Location.HIDDEN);
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
      if (dockable instanceof CompoundDockable){
        // V2.1 : also trigger a dockable state change event for compound dockable children
        updateCompoundChildrenState((CompoundDockable) dockable, DockableState.Location.HIDDEN);
      }
      
      DockingUtilities.updateResizeWeights(dockingPanel);
      revalidate();
    }
  } else { // SHOW
    
    // remove the button, show again the view
    DockableState newState = new DockableState(this, dockable, DockableState.Location.DOCKED);
    DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
    
    RelativeDockablePosition pos = currentState.getPosition();
    Container relativeContainer = pos.getRelativeAncestorContainer();
    if (relativeContainer == null){
      relativeContainer = dockingPanel; // safety : when a component is loaded from workspace,
      // we currently loose the container information @todo fix this container in xmlBuildAutoHideNode
    }
    DockingActionEvent dae = RelativeDockingUtilities.getInsertionDockingAction(
        relativeContainer, dockable, currentState, newState);
    
    if (! currentState.isDocked() && isDockingActionAccepted(dae, dswe)){
      AutoHideButton btn = autoHideButtons.get(k);
      
      assert btn != null;
      
      int zone = btn.getZone();
      
      btn.setVisible(false);
      expandPanel.collapse();
      if (borderPanes[zone].getVisibleButtonCount() == 0){
        // hide button panel if no more buttons (to avoid a remaining visible border)
        borderPanes[zone].setVisible(false);
        revalidate();
      }
      
      expandPanel.remove(dockable);
      
      show(dockable, dae);
      
      context.setDockableState(dockable, newState);
      
      k.setLocation(DockableState.Location.DOCKED);
      fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
      
      if (dockable instanceof CompoundDockable){
        // V2.1 : also trigger a dockable state change event for compound dockable children
        updateCompoundChildrenState((CompoundDockable) dockable, DockableState.Location.DOCKED);
      }
      
      
      DockingUtilities.updateResizeWeights(dockingPanel);
      revalidate();
    }
    
  }
}

/** hide the view (used with autohide features)  */
private void hide(Dockable dockable) {
  if (dockable == null){
    throw new NullPointerException("dockable");
  }
  
  DockableContainer dockableContainer = DockingUtilities.
      findSingleDockableContainer(dockable);
  
  if (dockableContainer == null){// || !dockingPanel.isAncestorOf((Component) dockableContainer)){
    throw new IllegalArgumentException("not docked");
  }
  
  remove(dockable);
  
}


/** recalculates and returns the insets taken by autoexpand
 * borders around the docking panel.
 * */
public Insets getDockingPanelInsets() {
  // recalculate the location and size of the mouse grabber
  Insets i = new Insets(0, 0, 0, 0);
  if (topBorderPane != null && topBorderPane.isVisible()) {
    i.top += topBorderPane.getHeight();
  }
  if (leftBorderPane != null && leftBorderPane.isVisible()) {
    i.left += leftBorderPane.getWidth();
  }
  if (bottomBorderPane != null && bottomBorderPane.isVisible()) {
    i.bottom += bottomBorderPane.getHeight();
  }
  
  if (rightBorderPane != null && rightBorderPane.isVisible()) {
    i.right += rightBorderPane.getWidth();
  }
  return i;
}

/** Creates and returns an array of all registered dockable with their current
 * state.
 * <p>
 * Visibility states are [DockableState.CLOSED, DOCKED, HIDDEN]
 * @return an array of DockableState
 */
public DockableState[] getDockables(){
  return context.getDockables();
}


/** Installs multiple drag sources.
 * @see  #installDockableDragSource(DockableDragSource)
 *
 * */
public void installDockableDragSources(DockableDragSource[] sources){
  if (sources != null){
    for (int i = 0; i < sources.length; i++) {
      installDockableDragSource(sources[i]);
    }
  }
}

/** Uninstalls multiple drag sources.
 *@see #uninstallDockableDragSource(DockableDragSource)
 *
 * */
public void uninstallDockableDragSources(DockableDragSource[] sources){
  if (sources != null){
    for (int i = 0; i < sources.length; i++) {
      uninstallDockableDragSource(sources[i]);
    }
  }
}

/** This method is used by DockableContainers in order to register their
 * DockableDragSource(s) to the OldDragControler.
 * <p>
 * Adds a MouseListener and a MouseMotionListener to the
 * Component-DockableDragSource <code>source</code>. These listeners
 * are used to perform start-dragging-docking actions over the source component */
public void installDockableDragSource(DockableDragSource source) {
  ((Component)source).addMouseListener(dragControler);
  ((Component)source).addMouseMotionListener(dragControler);
}

/** This method is used by DockableContainers in order to unregister their
 * DockableDragSource(s) to the OldDragControler.
 * <p>
 * Removes the MouseListener and MouseMotionListener added on installDockableDragSource()
 *
 * @see #installDockableDragSource(DockableDragSource)
 * */
public void uninstallDockableDragSource(DockableDragSource source) {
  ((Component)source).removeMouseListener(dragControler);
  ((Component)source).removeMouseMotionListener(dragControler);
}


/** Request this desktop to reset it's contained views to their preferred size, if
 * possible.
 * <p>
 * This method should be invoked when the component is realized (visible) due
 * to Split Panes implementation.
 *   */
public void resetToPreferredSize() {
  dockingPanel.resetToPreferredSize();
}

/** Saves the current desktop configuration into an XML stream.
 * <p>
 * The stream is not closed at the end of the operation.
 *<p>
 * As of version 2.1, this method delegates the work to DockingContext.writeXML, which
 * will export every dekstop sharing the same context (and not only this one)
 *
 * @see #readXML(InputStream)
 * */
public void writeXML(OutputStream stream) throws IOException {
  context.writeXML(stream);
  // will call back writeDesktopXML for each desktop
}

/* package protected for callback from DockingContext */
void writeDesktopNode(PrintWriter out) throws IOException {
  
  out.println("<DockingDesktop name=\""+ desktopName + "\">");
  out.println("<DockingPanel>");
  if (dockingPanel.getComponentCount() > 0){
    // only one top component (DockableContainer or SplitContainer)
    xmlWriteComponent(dockingPanel.getComponent(0), out);
  }
  
  Dockable max = getMaximizedDockable(); // 2007/01/08
  if ( max != null){
    out.println("<MaximizedDockable>");
    DockKey key = max.getDockKey();
    out.println("<Key dockName=\"" + key.getKey() + "\"/>");
    out.println("</MaximizedDockable>");
  }
  
  out.println("</DockingPanel>");
  
  for (int i = 0; i < borderPanes.length; i++) {
    xmlWriteBorder(borderPanes[i], out);
  }
  
  // finish with the floating dockables
  xmlWriteFloating(out);
  
  // and the tab groups
  xmlWriteTabGroups(out);
  
  out.println("</DockingDesktop>");
}

private void xmlWriteBorder(AutoHideButtonPanel borderPanel, PrintWriter out) throws IOException {
  if (borderPanel.isVisible()){
    out.println("<Border zone=\"" + borderPanel.getBorderZone() + "\">");
    Component[] comps = borderPanel.getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof AutoHideButton) {
        xmlWriteBorderDockable(((AutoHideButton)comps[i]), out);
      }
    }
    out.println("</Border>");
  }
}


private void xmlWriteFloating(PrintWriter out) throws IOException {
  // there is no list of floating : we have to fetch the dockable states
  
  // updated : we also need to store grouping information
  ArrayList processedDockables = new ArrayList();
  
  Window desktopWindow = SwingUtilities.getWindowAncestor(this);
  Point windowLocation;
  if (desktopWindow != null){
    windowLocation = desktopWindow.getLocation();
  } else {
    windowLocation = new Point(); // fail safe
  }
  
  ArrayList <Dockable>floatingDockables = context.getDockablesByState(this, DockableState.Location.FLOATING);
  
  for (int i=0; i < floatingDockables.size(); i++){
    Dockable d = floatingDockables.get(i);
    
    //DockableState state = (DockableState)floatingDockables.get(i);
    // Dockable d = (Dockable) state.getDockable();
    if (processedDockables.contains(d)){
      continue; // ignore this dockable : already processed
    }
    
    Window w = SwingUtilities.getWindowAncestor(d.getComponent());
    Rectangle r = w.getBounds();
    out.println("<Floating x=\"" + (r.x - windowLocation.x) + "\" y=\""
        + (r.y - windowLocation.y) + "\" width=\""
        + r.width + "\" height=\"" + r.height + "\">");
    
    TabbedDockableContainer tabContainer = DockingUtilities.findTabbedDockableContainer(d);
    if (tabContainer != null){
      // fetch all the dockables of this container
      for (int t=0; t < tabContainer.getTabCount(); t++){
        Dockable tabDockable = tabContainer.getDockableAt(t);
        processedDockables.add(tabDockable);
        xmlWriteFloatingDockable(tabDockable, out);
      }
    } else {
      // single floating
      processedDockables.add(d);
      xmlWriteFloatingDockable(d, out);
    }
    out.println("</Floating>");
  }
  
}

private void xmlWriteComponent(Component comp, PrintWriter out) throws IOException {
  if (comp instanceof SplitContainer){
    xmlWriteSplit((SplitContainer) comp, out);
  } else if (comp instanceof TabbedDockableContainer){
    xmlWriteTab((TabbedDockableContainer)comp, out);
  } else if (comp instanceof SingleDockableContainer){
    Dockable d = ((SingleDockableContainer)comp).getDockable();
    xmlWriteDockable(d, out);
  } else if (comp == dummyMaximedReplacer){ //2006/12/01 (maximized component wasn't saved)
    SingleDockableContainer maxContainer = (SingleDockableContainer)maximizedComponent;
    Dockable d = maxContainer.getDockable();
    xmlWriteDockable(d, out);
  }
  
}

private void xmlWriteTab(TabbedDockableContainer tpane, PrintWriter out) throws IOException {
  out.println("<TabbedDockable>");
  for (int i = 0; i < tpane.getTabCount(); i++) {
    Dockable d = tpane.getDockableAt(i);
    if (d == null){ //2008/07/05
      // means that the dockable has been (and is still) maximized 
      SingleDockableContainer maxContainer = (SingleDockableContainer)maximizedComponent;
      d = maxContainer.getDockable();
      xmlWriteDockable(d, out);
    } else {
      xmlWriteDockable(d, out);
    }
  }
  out.println("</TabbedDockable>");
}

private void xmlWriteDockable(Dockable dockable, PrintWriter out) throws IOException {
  boolean isCompound = dockable instanceof CompoundDockable;
  if (isCompound){
    out.println("<Dockable compound=\"true\">");
  } else {
    out.println("<Dockable>");
  }
  DockKey key = dockable.getDockKey();
  out.println("<Key dockName=\"" + key.getKey() + "\"/>");
  
  if (isCompound){
    CompoundDockable d = (CompoundDockable)dockable;
    CompoundDockingPanel cdp = (CompoundDockingPanel) d.getComponent(); // no choice here
    if (cdp.getComponentCount() > 0){
      // compound panels cannont have more than one child (split/tab/single)
      Component child = cdp.getComponent(0);
      xmlWriteComponent(child, out);
    }
  }
  
  out.println("</Dockable>");
}

private void xmlWriteCompoundDockable(CompoundDockable dockable, PrintWriter out) throws IOException {
  out.println("<Dockable compound=\"true\">");
  DockKey key = dockable.getDockKey();
  out.println("<Key dockName=\"" + key.getKey() + "\"/>");
  CompoundDockingPanel cdp = (CompoundDockingPanel) dockable.getComponent(); // no choice here
  if (cdp.getComponentCount() > 0){
    // compound panels cannont have more than one child (split/tab/single)
    Component child = cdp.getComponent(0);
    xmlWriteComponent(child, out);
  }
  
  out.println("</Dockable>");
}

private void xmlWriteBorderDockable(AutoHideButton btn, PrintWriter out) throws IOException {
  if (btn.isVisible()){
    Dockable dockable = btn.getDockable();
    xmlWriteDockableWithRelativePosition(dockable, out);
  }
}

private void xmlWriteDockableWithRelativePosition(Dockable dockable, PrintWriter out) throws IOException {
  DockableState state = context.getDockableState(dockable);
  RelativeDockablePosition position = (RelativeDockablePosition) state.getPosition();
  boolean isCompound = dockable instanceof CompoundDockable;
  if (isCompound){
    out.println("<Dockable compound=\"true\">");
  } else {
    out.println("<Dockable>");
  }
  DockKey key = dockable.getDockKey();
  out.println("<Key dockName=\"" + key.getKey() + "\"/>");
  out.println("<RelativePosition x=\"" + position.getX()
  + "\" y=\"" + position.getY()
  + "\" w=\"" + position.getWidth()
  + "\" h=\"" + position.getHeight()
  + "\" />");
  if (isCompound){
    CompoundDockable d = (CompoundDockable)dockable;
    CompoundDockingPanel cdp = (CompoundDockingPanel) d.getComponent(); // no choice here
    if (cdp.getComponentCount() > 0){
      // compound panels cannont have more than one child (split/tab/single)
      Component child = cdp.getComponent(0);
      xmlWriteComponent(child, out);
    }
  }
  out.println("</Dockable>");
}

private void xmlWriteCompoundDockableWithRelativePosition(CompoundDockable dockable, PrintWriter out) {
  /* The autohide component is a compound dockable : we'll have to save its position then traverse its children */
  DockableState state = context.getDockableState(dockable);
  RelativeDockablePosition position = (RelativeDockablePosition)
  state.getPosition();
  DockKey key = dockable.getDockKey();
  out.println("<Key dockName=\"" + key.getKey() + "\"/>");
  out.println("<RelativePosition x=\"" + position.getX()
  + "\" y=\"" + position.getY()
  + "\" w=\"" + position.getWidth()
  + "\" h=\"" + position.getHeight()
  + "\" />");
  
  out.println("</Dockable>");
}

private void xmlWriteFloatingDockable(Dockable dockable, PrintWriter out) throws IOException{
  DockableState state = context.getDockableState(dockable);
  RelativeDockablePosition position = (RelativeDockablePosition) state.getPosition();
  boolean isCompound = dockable instanceof CompoundDockable;
  if (isCompound){
    out.println("<Dockable compound=\"true\">");
  } else {
    out.println("<Dockable>");
  }
  DockKey key = dockable.getDockKey();
  out.println("<Key dockName=\"" + key.getKey() + "\"/>");
  out.println("<RelativePosition x=\"" + position.getX()
  + "\" y=\"" + position.getY()
  + "\" w=\"" + position.getWidth()
  + "\" h=\"" + position.getHeight()
  + "\" />");
  DockableState prev =  previousFloatingDockableStates.get(dockable);
  out.println("<PreviousState state=\"" + prev.getLocation().ordinal() + "\"/>");
  
  if (isCompound){
    CompoundDockable d = (CompoundDockable)dockable;
    CompoundDockingPanel cdp = (CompoundDockingPanel) d.getComponent(); // no choice here
    if (cdp.getComponentCount() > 0){
      // compound panels cannont have more than one child (split/tab/single)
      Component child = cdp.getComponent(0);
      xmlWriteComponent(child, out);
    }
  }
  
  
  out.println("</Dockable>");
}


private void xmlWriteSplit(SplitContainer split, PrintWriter out) throws IOException {
  double location;
  if (split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT){
    location = split.getLeftComponent().getWidth() / (double)(split.getWidth() - split.getDividerSize()); // 2006/09/25
  } else {
    location = split.getTopComponent().getHeight() / (double)(split.getHeight() - split.getDividerSize());
  }
  out.println("<Split orientation=\""+ split.getOrientation()
  + "\" location=\"" + location + "\">");
  xmlWriteComponent(split.getLeftComponent(), out);
  xmlWriteComponent(split.getRightComponent(), out);
  out.println("</Split>");
}

/** TabGroups are the memory of tabs (even for hidden or floating dockables) used to
 * put them back on the right tab when docked again
 */
private void xmlWriteTabGroups(PrintWriter out) throws IOException {
  // there is redundant information in the tabbedGroups Map... so we have to simplify it
  
  ArrayList uniqueGroups = new ArrayList();
  ArrayList processedDockables = new ArrayList();
  Iterator <Dockable> it = tabbedGroups.keySet().iterator();
  while (it.hasNext()){
    Dockable d = it.next();
    if (! processedDockables.contains(d)){
      processedDockables.add(d);
      LinkedList tabList = tabbedGroups.get(d);
      Iterator listIt = tabList.iterator();
      while (listIt.hasNext()){
        Dockable d2 = (Dockable) listIt.next();
        if (!processedDockables.contains(d2)){
          processedDockables.add(d2);
        }
      }
      uniqueGroups.add(tabList);
    }
  }
  
  out.println("<TabGroups>");
  for (int i = 0; i < uniqueGroups.size(); i++) {
    out.println("<TabGroup>");
    LinkedList group = (LinkedList) uniqueGroups.get(i);
    Iterator listIt = group.iterator();
    while (listIt.hasNext()) {
      Dockable d = (Dockable) listIt.next();
      xmlWriteDockableTab(d, out);
    }
    out.println("</TabGroup>");
  }
  out.println("</TabGroups>");
}

private void xmlWriteDockableTab(Dockable dockable, PrintWriter out) throws IOException {
  out.println("<Dockable>");
  DockKey key = dockable.getDockKey();
  out.println("<Key dockName=\"" + key.getKey() + "\"/>");
  out.println("</Dockable>");
}



/** Reads an XML encoded stream as the new desktop configuration.
 * <p>
 * When the method returns, the desktop is totally reconfigured with posiibly different
 * dockable at different positions.
 * <p>
 * <b>Note : </b> The <code>DockKey</code>s of the stream must be registered with
 * the {@link #registerDockable(Dockable) registerDockable} method,
 * prior readXML. <br>
 * This is the case if the desktop is already open and dockables
 * laid out, but might not be the case if this method is used at application startup
 * to populate an empty desktop.
 *
 * <p>
 * Dismisses all visible dockables (docked and auto-hidden), and clear their DockableState.
 * <p>
 * The stream is not closed at the end of the operation.
 * @see #writeXML(OutputStream)
 * @see #registerDockable(Dockable)
 *  */
public void readXML(InputStream in) throws ParserConfigurationException, IOException, SAXException {
  context.readXML(in);
}

/** removes every dockables from this desktop */
public void clear(){
  dockingPanel.removeAll();
  for (int i = 0; i < borderPanes.length; i++) {
    borderPanes[i].removeAll();
    borderPanes[i].setVisible(false);
  }
  
  boolean wasHeavyMaximized = false;
  if (maximizedComponent != null){ // clean up maximization state
    if (DockingPreferences.isLightWeightUsageEnabled()){
      remove(maximizedComponent);          // remove the single dockable container
    } else {
      remove(maximizedComponent.getParent()); // remove the awt panel
      wasHeavyMaximized = true;
    }
    maximizedComponent = null; // 2006/11/20 ooops !
  }
  
  
  autoHideButtons.clear();
  expandPanel.collapse();
  
  expandPanel.clear();
  
  previousFloatingDockableStates.clear();
  
  tabbedGroups.clear();
  
  
  // clear the floatables windows
  ArrayList <Dockable> floatingDockables = context.getDockablesByState(this, DockableState.Location.FLOATING);
  for (int i=0; i < floatingDockables.size(); i++){
    Dockable d = (Dockable) floatingDockables.get(i);
    remove(d);
  }
  
  
}

/* package protected */
void readDesktopNode(Element desktopElement) throws  SAXException {
    /* called back by DockingContext to read (and install) a desktop configuration from an xml stream
     */
  
  
  NodeList children = desktopElement.getChildNodes();
  for (int i = 0, len = children.getLength(); i < len; i++) {
    Node child = children.item(i);
    xmlBuildRootNode(child);
  }
  
  DockingUtilities.updateResizeWeights(dockingPanel); // 2006/12/01
  
  revalidate();
  
}

private void xmlBuildRootNode(Node node) throws SAXException{
  
  if (node.getNodeType() == Node.ELEMENT_NODE){
    Element elt = (Element) node;
    String name = elt.getNodeName();
    if (name.equals("DockingPanel")){
      // only one child at most
      NodeList children = elt.getChildNodes();
      for (int i = 0, len = children.getLength(); i < len; i++) {
        xmlBuildDockingPanelNode(elt.getChildNodes().item(i));
      }
    } else if (name.equals("Border")){
      int zone = Integer.parseInt(elt.getAttribute("zone"));
      AutoHideButtonPanel borderPanel = borderPanes[zone];
      borderPanel.setVisible(true); // border may not be visible
      NodeList children = elt.getElementsByTagName("Dockable");
      for (int i = 0, len = children.getLength(); i < len; i++) {
        xmlBuildAutoHideNode(borderPanel, (Element)children.item(i));
      }
      borderPanel.revalidate();
    } else if (name.equals("Floating")){
      int x = Integer.parseInt(elt.getAttribute("x"));
      int y = Integer.parseInt(elt.getAttribute("y"));
      int width = Integer.parseInt(elt.getAttribute("width"));
      int height = Integer.parseInt(elt.getAttribute("height"));
      
      NodeList children = elt.getElementsByTagName("Dockable");
      xmlBuildFloatingNode(children, new Rectangle(x, y, width, height)); //2005/10/10
      
/*        for (int i = 0, len = children.getLength(); i < len; i++) {
          xmlBuildFloatingNode((Element)children.item(i), new Rectangle(x, y, width, height));
        }*/
    } else if (name.equals("TabGroups")){
      NodeList children = elt.getElementsByTagName("TabGroup");
      xmlBuildTabGroup(children); //2005/10/10
    } else {
      throw new SAXNotRecognizedException(name);
    }
  }
}

private void xmlBuildAutoHideNode(AutoHideButtonPanel borderPanel, Element dockableElt) throws SAXException {
  
  Element hideElt = (Element)dockableElt.getElementsByTagName("RelativePosition").item(0);
  float x = Float.parseFloat(hideElt.getAttribute("x"));
  float y = Float.parseFloat(hideElt.getAttribute("y"));
  float w = Float.parseFloat(hideElt.getAttribute("w"));
  float h = Float.parseFloat(hideElt.getAttribute("h"));
  RelativeDockablePosition position = new RelativeDockablePosition(x, y, w, h);
  
  AutoHideButton btn = new AutoHideButton();
  Dockable dockable = xmlGetDockable(dockableElt);
  autoHideButtons.put(dockable.getDockKey(), btn);
  
  context.setDockableState(dockable, new DockableState(this, dockable, DockableState.Location.HIDDEN, position));
  dockable.getDockKey().setLocation(DockableState.Location.HIDDEN);
  btn.init(dockable, borderPanel.getBorderZone());
  borderPanel.add(btn);
  
  if (dockable instanceof CompoundDockable){
    // check for children and build them
    xmlBuildCompoundDockable((CompoundDockable) dockable, dockableElt, DockableState.Location.HIDDEN);
  }
  
  
}

  /* private void xmlBuildFloatingNode(Element dockableElt, Rectangle bounds) {
   */
private void xmlBuildFloatingNode(NodeList dockables, Rectangle bounds) throws SAXException {
  
  // @todo not optimal.. we should refactor it oustide this method
  Window desktopWindow = SwingUtilities.getWindowAncestor(this);
  if (desktopWindow != null){
    Point windowLocation = desktopWindow.getLocation();
    bounds.x += windowLocation.x; // position is relative
    bounds.y += windowLocation.y;
  }
  
  //JDialog dialog = null;
  FloatingDockableContainer fdc = null;
  TabbedDockableContainer tdc  = null;
  if (dockables.getLength()>1){ // it's a floating tab
    tdc = DockableContainerFactory.getFactory().createTabbedDockableContainer();
    tdc.installDocking(this);
    ((JTabbedPane) tdc).addChangeListener(focusHandler); // our best way to track selection (focus) changes
  }
  
  Dockable baseDockable = null; // used when there are tabs
  
  for (int i=0; i < dockables.getLength(); i++){
    Element dockableElt = (Element) dockables.item(i);
    Element hideElt = (Element)dockableElt.getElementsByTagName("RelativePosition").item(0);
    float x = Float.parseFloat(hideElt.getAttribute("x"));
    float y = Float.parseFloat(hideElt.getAttribute("y"));
    float w = Float.parseFloat(hideElt.getAttribute("w"));
    float h = Float.parseFloat(hideElt.getAttribute("h"));
    RelativeDockablePosition position = new RelativeDockablePosition(dockingPanel, x, y, w, h);
    
    Dockable dockable = xmlGetDockable(dockableElt);
    if (i == 0){
      baseDockable = dockable;
    }
    
    Element previousState = (Element)dockableElt.getElementsByTagName("PreviousState").item(0);
    int istate = Integer.parseInt(previousState.getAttribute("state"));
    DockableState state = new DockableState(this, dockable, DockableState.getLocationFromInt(istate), position);
    storePreviousFloatingState(dockable, state);
    
    if (fdc == null){
      //if (dialog == null}){
      //        dialog = (JDialog) createFloatingDockableContainer(dockable);
      fdc = createFloatingDockableContainer(dockable);
    } else {
      // add as a tab
      if (tdc.getTabCount() == 0){
        // first tab : replace the current DetachedDockView by the tab container
        // not very efficient... @todo : sort this out
        DockableContainer base = DockingUtilities.findDockableContainer(baseDockable);
        DockingUtilities.replaceChild(((Component)base).getParent(),
            (Component)base, (Component)tdc);
        tdc.addDockable(baseDockable, 0);
      }
      tdc.addDockable(dockable, tdc.getTabCount());
    }
    
    context.setDockableState(dockable, new DockableState(this, dockable, 
            DockableState.Location.FLOATING, position));
    dockable.getDockKey().setLocation(DockableState.Location.FLOATING);
    
    if (dockable instanceof CompoundDockable){
      // check for children and build them
      xmlBuildCompoundDockable((CompoundDockable) dockable, dockableElt, DockableState.Location.FLOATING);
    }
  }
  
  DockingUtilities.setBounds(fdc, bounds);
  //dialog.setBounds(bounds);
  //dialog.validate();
  //dialog.setVisible(true);
  DockingUtilities.validate(fdc);
  DockingUtilities.setVisible(fdc, true);
  
  
}

private void xmlBuildDockingPanelNode(Node node) throws SAXException {
  if (node.getNodeType() == Node.ELEMENT_NODE){
    Component comp = xmlCreateComponent((Element) node, DockableState.Location.DOCKED);
    if (comp != null){
      dockingPanel.add(comp, BorderLayout.CENTER);
    }
  }
}

private Component xmlCreateComponent(Element elt, DockableState.Location dockableLocation) throws SAXException {
  if (elt.getNodeName().equals("Split")){
    SplitContainer split = xmlBuildSplitContainer(elt, dockableLocation);
    return split;
  } else if (elt.getNodeName().equals("Dockable")){
    Dockable d = xmlGetDockable(elt);
    SingleDockableContainer sdc = DockableContainerFactory.getFactory()
    .createDockableContainer(d, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
    context.setDockableState(d, new DockableState(this, d, dockableLocation));
    d.getDockKey().setLocation(dockableLocation);
    sdc.installDocking(this);
    
    if (d instanceof CompoundDockable){
      // check for children and build them
      xmlBuildCompoundDockable((CompoundDockable) d, elt, dockableLocation);
    }
    return (Component) sdc;
  } else if (elt.getNodeName().equals("TabbedDockable")){
    
    TabbedDockableContainer tdc = xmlBuildTabbedDockableContainer(elt, dockableLocation);
    return (Component) tdc;
  } else if (elt.getNodeName().equals("MaximizedDockable")){
    
    // this should be the last element from DockingPanel node
    Dockable d = xmlGetDockable(elt);
    maximize(d);
    return null;
  } else {
    throw new SAXNotRecognizedException(elt.getNodeName());
  }
}

private void xmlBuildCompoundDockable(CompoundDockable dockable,
    Element compoundElt, DockableState.Location dockableLocation) throws SAXException{
  /*  a compound dockable can hold a sub dockable (or split/tabs) */
  CompoundDockingPanel compoundPanel = (CompoundDockingPanel) dockable.getComponent();
  compoundPanel.removeAll(); // cleanup the compound at every workspace reloading 2007/01/08
  NodeList children = compoundElt.getChildNodes();
  for (int i = 0, len = children.getLength(); i < len; i++) {
    Node node = children.item(i);
    if (node.getNodeType() == Node.ELEMENT_NODE){
      Element elt = (Element) node;
      if (elt.getNodeName().equals("Key")){
        // ignore : it's the key of the compound dockable
        
      } else {
        Component comp = xmlCreateComponent(elt, dockableLocation);
        compoundPanel.add(comp, BorderLayout.CENTER);
        return; // no more than a single child
      }
    }
  }
  
}


private SplitContainer xmlBuildSplitContainer(Node node, DockableState.Location dockableLocation) throws SAXException{
  Element elt = (Element) node;
  int orientation = Integer.parseInt(elt.getAttribute("orientation"));
  String loc = elt.getAttribute("location");
  double location = 0.5;
  if (loc != null && !loc.equals("")){
    location = Double.parseDouble(loc);
  }
  
  SplitContainer split = new SplitContainer(orientation);
  split.setDividerLocation(location);
  
  boolean left = true;
  for (int i = 0; i < node.getChildNodes().getLength(); i++) {
    Node child = node.getChildNodes().item(i);
    if (child.getNodeType() == Node.ELEMENT_NODE){
      Component comp = xmlCreateComponent((Element)child, dockableLocation);
      if (left){
        split.setLeftComponent(comp);
        left = false;
      } else {
        split.setRightComponent(comp);
      }
    }
  }
  return split;
}

private TabbedDockableContainer xmlBuildTabbedDockableContainer(Element elt,
    DockableState.Location dockableLocation) throws SAXException {
  
  TabbedDockableContainer tdc = DockableContainerFactory.getFactory().createTabbedDockableContainer();
  tdc.installDocking(this);
  ((JTabbedPane) tdc).addChangeListener(focusHandler); // our best way to track selection (focus) changes
  Dockable base = null;
  for (int i = 0; i < elt.getChildNodes().getLength(); i++) {
    Node child = elt.getChildNodes().item(i);
    if (child.getNodeType() == Node.ELEMENT_NODE){
      Dockable d = xmlGetDockable((Element) child);
      if (base == null){
        base = d;
      }
      context.setDockableState(d, new DockableState(this, d, dockableLocation));
      d.getDockKey().setLocation(dockableLocation);
      tdc.addDockable(d, tdc.getTabCount());
      
      // update the tab group
      if (base != d){
        addToTabbedGroup(base, d);
      }
      
      if (d instanceof CompoundDockable){
        // check for children and build them is needed
        xmlBuildCompoundDockable((CompoundDockable) d, (Element) child, dockableLocation);
      }
    }
  }
  return tdc;
}

private Dockable xmlGetDockable(Element dockableElt) {
  Element key = (Element)dockableElt.getElementsByTagName("Key").item(0);
  String name = key.getAttribute("dockName");
  Dockable dockable = context.getDockableByKey(name);
  if (dockable == null){
    throw new NullPointerException("Dockable " + name + " not registered");
  }
  
  return dockable;
}

/** rebuild the tabbedGroups structure (memory of tabbed dockables) */
private void xmlBuildTabGroup(NodeList group) {
  // this pass is useful for hidden dockable that need to be reassociated
  // with their tab group.
  // (tabbed dockable don't need it as this information is implicit (during xml tab creation)
  for (int i=0; i < group.getLength(); i++){
    Element groupElt = (Element)group.item(i);
    NodeList dockables = groupElt.getElementsByTagName("Dockable");
    Dockable base = null;
    for (int j=0; j < dockables.getLength(); j++){
      Element dockableElt = (Element)dockables.item(j);
      Dockable d = xmlGetDockable(dockableElt);
      if (j == 0){
        base = d;
      } else {
        addToTabbedGroup(base, d);
      }
    }
  }
}


/** Adds a dockable in the docking desktop.
 * <p>
 *  This method can be used mainly in two situations :
 * <ul>
 * <li> the desktop is empty (when not empty, you can use split() or createTab() to
 * add a dockable relatively to another)
 * <li> the dockable is currently closed, and it must be shown again
 * <li> note that if the desktop is not empty, the dockable will be added in the bottom
 * of the desktop (equivalent to addDockable(dockable 0,0.8,1,0.2)).
 * </ul>
 */
public void addDockable(Dockable dockable) {
  /* addDockable isn't called internally by the API : just for user applications */
  addDockable(dockable, new RelativeDockablePosition(0,0.8,0.5,0.2));
}


/** Adds a dockable in the docking desktop, and tries to respect the relative
 * positionning provided.
 * <p>
 *  This method is used to reposition a closed dockable at its previous location
 * on the desktop. As it relies on the Component.findComponentAt(Point) method ,
 *  the desktop must already be visible.
 *
 * <p>
 *  The preferred way to obtain a particular visual docking configuration is to
 * use a combination of add(), split() and createTab() methods, as these methods do
 * not rely on an interpretation (and approximation) of constraints.
 *
 * <p>
 *  Precision of constraints : as the docking management is based on a mix of
 * horizontal and vertical splitting zones, it is not always possible to
 * respect the constraints given.
 * <p>
 *  The current implementation will do the following :
 * <ul>
 * <li> find the splitter containing the given center (x + width/2, y + height/2) of the dockable
 * <li> try to respect the  x and y, constraints.
 * <li> try to respect the width and height constraints.
 * <li> sub-split the splitter (horizontally or vertically) zone and position
 * the Dockable in the most appropriate zone (top, left, bottom, right).
 * </ul>
 *
 * @param dockable   the dockable to add (must not be already visible)
 * @param position   relative position of the dockable
 *
 * @throws IllegalArgumentException if the dockable already belongs to the desktop containment
 * hierarchy.
 */
public void addDockable(Dockable dockable, RelativeDockablePosition position){
  /* addDockable isn't called internally by the API : just for user applications */
  if (this.isAncestorOf(dockable.getComponent())){
    throw new IllegalArgumentException("Dockable is already contained in the desktop");
  }
  
  registerDockable(dockable);
  
  if (DockingUtilities.findSingleDockableContainer(dockable) != null){
    remove(dockable);
  }
  
  DockableState newState = new DockableState(this, dockable, DockableState.Location.DOCKED, position);
  
  DockableState currentState = getDockableState(dockable);
  DockableState.Location currentLocation = getLocation(currentState);
  
  DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
  DockingActionEvent dae = new DockingActionAddDockableEvent(this, dockable, currentLocation, newState.getLocation(), dockingPanel);
  if (isDockingActionAccepted(dae, dswe)){
    SingleDockableContainer sdc = RelativeDockingUtilities.insertDockable(
        dockingPanel, dockable, position);
    sdc.installDocking(this);
    
    context.setDockableState(dockable, newState);
    dockable.getDockKey().setLocation(DockableState.Location.DOCKED);
    
    DockableStateChangeEvent dse = new DockableStateChangeEvent(currentState, newState);
    fireDockingAction(dae, dse);
    
    DockingUtilities.updateResizeWeights(dockingPanel);
  }
}

/** Replaces a dockable by another one.
 * <p>
 * Useful for example to reserve some space on a desktop by using a "placeholder' when
 * other dockables aren't visible (Multiple Tabbed Document Interface with always a dockable
 * visible even when no document is loaded).
 * <p>
 * Implementation note : this method assumes only the "base" dockable is visible (the replacer must be
 * in the CLOSED state). It also assumes the base dockable isn't a compound dockable. These limitations
 * will be removed in a later release.
 *
 * <p>
 * There is currently no DnD gesture associated to this action (although the
 * "HotSwap" gesture would be a good candidate). So this method currently doesn't trigger any event
 * (state change, action), yet it could change later.
 *
 * @since 2.1
 * @throws IllegalArgumentException when dockables aren't in the appropriate state or hierarchy.
 */
public void replace(Dockable base, Dockable replacer){
  DockableState baseState = context.getDockableState(base);
  DockableState replacerState = context.getDockableState(replacer);
  
  DockableState newBaseState = new DockableState(replacerState.getDesktop(), base, replacerState.getLocation());
  DockableState newReplacerState = new DockableState(baseState.getDesktop(), replacer, baseState.getLocation());
  
  
  TabbedDockableContainer tdcBase = DockingUtilities.findTabbedDockableContainer(base);
  
  if (!replacerState.isClosed()){
    throw new IllegalArgumentException("replacer isn't closed");
  }
  if (base instanceof CompoundDockable){
    throw new IllegalArgumentException("base cannot be a compound dockable");
  }
  
  if (tdcBase != null){
    int iBase = tdcBase.indexOfDockable(base);
    tdcBase.removeDockable(base);
    tdcBase.addDockable(replacer, iBase);
  } else {
    SingleDockableContainer sdcBase = DockingUtilities.findSingleDockableContainer(base);
    Container sdcBaseParent = ((Component)sdcBase).getParent();
    if (sdcBaseParent instanceof SplitContainer){
      SplitContainer split = (SplitContainer) sdcBaseParent;
      boolean isLeft = split.getLeftComponent() == sdcBase;
      
      SingleDockableContainer sdc = DockableContainerFactory.getFactory().createDockableContainer(
          replacer, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
      sdc.installDocking(this);
      if (isLeft){
        split.setLeftComponent((Component)sdc);
      } else {
        split.setRightComponent((Component)sdc);
      }
      split.revalidate();
    } else if (sdcBaseParent instanceof DockingPanel){
      // a single component
      DockingPanel dp = (DockingPanel)sdcBaseParent;
      dp.remove(0);
      SingleDockableContainer sdc = DockableContainerFactory.getFactory().createDockableContainer(
          replacer, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
      sdc.installDocking(this);
      dp.add((Component) sdc, BorderLayout.CENTER);
      dp.revalidate();
    } else if (sdcBaseParent instanceof CompoundDockingPanel){
      // a single component
      CompoundDockingPanel cdp = (CompoundDockingPanel)sdcBaseParent;
      cdp.remove(0);
      SingleDockableContainer sdc = DockableContainerFactory.getFactory().createDockableContainer(
          replacer, DockableContainerFactory.ParentType.PARENT_SPLIT_CONTAINER);
      sdc.installDocking(this);
      cdp.add((Component) sdc, BorderLayout.CENTER);
      cdp.revalidate();
    } else {
      throw new RuntimeException("Wrong container hierarchy : " + sdcBaseParent);
    }
  }
  
  // swap states
  context.setDockableState(base, newBaseState);
  context.setDockableState(replacer, newReplacerState);
  
  
}

/* small utility method to avoid bloating code everywhere (return a default state value if state object is null) */
private static DockableState.Location getLocation(DockableState state){
  if (state == null){
    return DockableState.Location.CLOSED;
  } else {
    return state.getLocation();
  }
}

/** Adds a dockable inside a compound dockable.
 * <p>
 *  This is the method to start a nesting hierarchy inside a compound dockable
 * (once the first dockable is added, you add subsequent dockables with standard split/createTab methods)
 * <ul>
 * <li> note that if the compound dockable is not empty, this method will raise an exception.
 * </ul>
 * @since 2.1
 */
public void addDockable(CompoundDockable base, Dockable dockable) {
  
  registerDockable(dockable);
  
  DockableState currentState = getDockableState(dockable);
  DockableState.Location currentLocation = getLocation(currentState);
  
  DockableState newState = new DockableState(this, dockable, DockableState.Location.DOCKED);
  DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
  
  CompoundDockingPanel panel = (CompoundDockingPanel) base.getComponent();
  
  DockingActionEvent dae = new DockingActionAddDockableEvent(this, dockable,
      currentLocation, newState.getLocation(), panel);
  
  if (isDockingActionAccepted(dae, dswe)){
    DockableContainer dockableContainer = DockableContainerFactory.getFactory().
        createDockableContainer(dockable, DockableContainerFactory.ParentType.PARENT_DESKTOP);
    dockableContainer.installDocking(this);
    //TODO : check and raise exception if not empty
    panel.add((JComponent)dockableContainer, BorderLayout.CENTER);
    
    context.setDockableState(dockable, newState);
    dockable.getDockKey().setLocation(DockableState.Location.DOCKED);
    fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
  }
  
}




/** Evaluates and returns the preferred size of the desktop, according to
 * its contents and auto-hide borders.*/
public Dimension getPreferredSize(){
  Insets i = getDockingPanelInsets();
  Dimension dim = dockingPanel.getPreferredSize();
  dim.width += i.left + i.right;
  dim.height += i.top + i.bottom;
  return dim;
}



/** Adds a new DockableStateChangeListener to this desktop.
 * <p>
 * DockableStateChange Events are triggered after the state change.
 *
 * <p> As of version 2.0 of the framework, this method can also be replaced by
 * adding a propertychangeListener on the DockKey object (and react to its DockableState
 * property).
 *
 * */
public void addDockableStateChangeListener(DockableStateChangeListener listener){
  context.addDockableStateChangeListener(listener);
}

/** Removes a DockableStateChangeListener from this desktop.
 * */
public void removeDockableStateChangeListener(DockableStateChangeListener listener){
  context.removeDockableStateChangeListener(listener);
}

/** Adds a new DockableStateWillChangeListener to this desktop.
 * <p>
 * DockableStateWillChange Events are triggered <b>before</b> the state change, and
 * are vetoable.
 * */

public void addDockableStateWillChangeListener(DockableStateWillChangeListener listener){
  context.addDockableStateWillChangeListener(listener);
}

/** Removes a DockableStateWillChangeListener from this desktop.
 * */
public void removeDockableStateWillChangeListener(DockableStateWillChangeListener listener){
  context.removeDockableStateWillChangeListener(listener);
}

/** Adds a new DockingActionListener to this desktop.
 * <p>
 * DockingAction Events are triggered before and after any docking action (split, tab, close, hide...) and
 * allow for precise tracking (and vetoing) of these operations.
 *
 * @see #removeDockingActionListener(DockingActionListener)
 */
public void addDockingActionListener(DockingActionListener listener){
  context.addDockingActionListener(listener);
}

/** Removes a DockingActionListener from this desktop.
 *
 * @see #addDockingActionListener(DockingActionListener)
 */
public void removeDockingActionListener(DockingActionListener listener){
  context.removeDockingActionListener(listener);
}



/** Adds a new DockableSelectionListener to this desktop.
 * <p>
 * DockableSelection Events are triggered when a dockable takes the focus.
 *
 */
public void addDockableSelectionListener(DockableSelectionListener listener){
  context.addDockableSelectionListener(listener);
}

/** Removes a DockableSelectionListener from this desktop.
 * */
public void removeDockableSelectionListener(DockableSelectionListener listener){
  context.removeDockableSelectionListener(listener);
}


/** Registers and add a dockable on an auto-hide border.
 * <P> this method should be called at startup time (when the dockable isn't yet
 * displayed). to toggle a dockable from docked to auto-hide, please use
 *  setAutoHide(Dockable, boolean) instead.
 *
 * <P> However, this method will not fail if the dockable is already displayed :
 * in that case, it will delegate autohide to setAutoHide(Dockable, boolean), thus
 * dropping the dockedPosition argument.
 *
 * @param dockable the dockable to add (to select the border where the dockable
 * will be positionned, use the DockKey.setAutoHideBorder mehod
 *
 * @param dockedPosition relative positionning (may be null) indicating where to
 * dock the dockable when leaving its auto-hide border.
 *   */
public void addHiddenDockable(Dockable dockable, RelativeDockablePosition dockedPosition){
  /* not used from inside the API : only for user applications */
  
  
  DockableState currentState = getDockableState(dockable); // should be null
  if (currentState == null || currentState.isClosed()){ //2007/03/19 reformulate tests
    // ok, that's the normal use case
  } else if (currentState.isHidden()){
    // already hidden... avoid !
    return; // should I throw an exception ?
  } else if (currentState.isDocked()){
    //if (currentState != null && !currentState.isHidden()){
    // delegate autohide to the appropriate method
    setAutoHide(dockable, true);
    return;
  }
  registerDockable(dockable);
  currentState = getDockableState(dockable); // now should be CLOSED
  
  DockableState.Location currentLocation = getLocation(currentState);
  
  DockableState newState = new DockableState(this, dockable, DockableState.Location.HIDDEN, dockedPosition);
  DockableStateWillChangeEvent dswe = new DockableStateWillChangeEvent(currentState, newState);
  DockingActionEvent dae = new DockingActionSimpleStateChangeEvent(this, dockable, currentLocation,
      DockableState.Location.HIDDEN);
  DockKey k = dockable.getDockKey();
  
  if (currentLocation != DockableState.Location.HIDDEN && isDockingActionAccepted(dae, dswe)){
    AutoHideButton btn = autoHideButtons.get(k);
    if (btn == null) {
      btn = new AutoHideButton();
      autoHideButtons.put(k, btn);
      
      int zone;
      if (k.getAutoHideBorder() == null) {
        zone = AutoHidePolicy.getPolicy().getDefaultHideBorder().value();
      } else {
        zone = k.getAutoHideBorder().value();
      }
      btn.init(dockable, zone);
      
      borderPanes[zone].setVisible(true); // border may not be visible
      borderPanes[zone].add(btn);
      borderPanes[zone].revalidate();
    } else { // btn already existing, show it again
      int zone = btn.getZone();
      borderPanes[zone].setVisible(true); // may not be visible
      btn.setVisible(true);
      borderPanes[zone].revalidate();
    }
    context.setDockableState(dockable, newState);
    dockable.getDockKey().setLocation(DockableState.Location.HIDDEN);
    fireDockingAction(dae, new DockableStateChangeEvent(currentState, newState));
    revalidate();
  }
  
}

/** Changes the width of a dockable (the dockable must already be visible).
 *
 * <p>
 * Note that this method works by modifying the divider location of the
 * nearest SplitContainer ancestor of the dockable (the first splitted horizontally).
 *
 * <p>
 * As this method must be called when the desktop is visible, a simple way to
 * do this is to add a window listener to the parent frame of the desktop, and call this
 * method from the windowOpened(WindowEvent) notification.
 *
 * @param width  new width (if between 0 and 1, width is taken as a proportional width,
 * otherwise it is a pixel width.
 *
 * */
public void setDockableWidth(Dockable dockable, double width){
  SplitContainer split = DockingUtilities.getSplitPane(dockable, JSplitPane.HORIZONTAL_SPLIT);
  if (split == null) return;
  if ( ( (Container) split.getLeftComponent()).isAncestorOf(dockable.
      getComponent())) {
    split.setDividerLocation(width);
  } else {
    split.setDividerLocation(1.0 - width);
  }
}

/** Changes the height of a dockable (the dockable must already be visible).
 *
 * <P> Note that this method works by modifying the divider location of the
 * nearest SplitContainer ancestor of the dockable (the first splitted vertically).
 *
 * <P> As this method must be called when the desktop is visible, a simple way to
 * do this is to add a window listener to the parent frame of the desktop, and call this
 * method from the windowOpened(WindowEvent) notification.
 *
 * @param height new height (if between 0 and 1, height is taken as a proportional height,
 * otherwise it is a pixel height.
 *
 * */
public void setDockableHeight(Dockable dockable, double height){
  SplitContainer split = DockingUtilities.getSplitPane(dockable, JSplitPane.VERTICAL_SPLIT);
  if (split == null) return;
  if ( ( (Container) split.getTopComponent()).isAncestorOf(dockable.
      getComponent())) {
    split.setDividerLocation(height);
  } else {
    split.setDividerLocation(1 - height);
  }
}

/** Updates the resizing behaviour of the desktop in order to resize
 * this dockable (and keep others at fixed size).
 * <P> This method should be called at least once (prior to showing the desktop) with an
 * already docked dockable.
 * <P> If the dockable doesn't belong to the desktop containment hierarchy,
 * this method will do nothing.
 * @deprecated use setResizeWeight() in every dockKey for a better resizing behaviour
 *
 */
public void setAutoResizableDockable(Dockable dockable){
  /*this.autoResizeableDockable = dockable; */
  DockingUtilities.updateResizeWeights(dockingPanel);
}





/** Registers a dockable as belonging to a tab group.
 * <p> It is used to have a memory of grouped (tabbed) dockables in order to keep the
 * group together when dockable are restored from auto-hide mode.
 * <p> This method is generally called by the tabbed container management, and not directly
 * by the developper.
 *
 * <p> However, there is a case where calling this method can be usefull :
 *  when, at startup, a desktop is built with multiple hidden dockables, and the developper wants
 * them to be grouped in a tab container when they are restored to the desktop.
 *
 * <p> note that the method is symetric when a group is empty  : it such a case base and newTab
 *   args can be swapped.
 *
 * @param base   an already tabbed dockable
 * @param newTab a dockable to add to the tab group
 *
 *@since 1.1.2
 */
public void addToTabbedGroup(Dockable base, Dockable newTab){//2005/07/13
  /* this method is called when a dockable is added to a dockableTabbedContainer */
  LinkedList <Dockable> group = tabbedGroups.get(base);
  if (group == null){
    group = new LinkedList();
    group.add(base);
    tabbedGroups.put(base, group);
  }
  if (!group.contains(newTab)){
    group.add(newTab);
    tabbedGroups.put(newTab, group);
  }
}

/** Unregisters a dockable from its current tab group (when it's removed from it).
 * <p> For an application developper, there should be no need to call this method as it is managed
 * internally by the framework, unless the developper wants to explicitely remove a component from
 * a tab group when this component is in the auto-hide state.
 *@since 1.1.2
 */
public void removeFromTabbedGroup(Dockable dockable){ // 2005/07/13
  /* package protected */
  
    /* This method is invoked when a component is dragged outside of a tab group : meaning
     * the user doesn't want anymore this dockable to be grouped in that tab.
     *
     */
  LinkedList <Dockable> group = tabbedGroups.get(dockable);
  if (group != null){
    tabbedGroups.remove(dockable);
    group.remove(dockable);
    if (group.size() == 1){ // end of grouping as there are no more dockables linked.
      Dockable d = (Dockable)group.removeFirst();
      tabbedGroups.remove(d);
    }
  }
  
  
}

/** Creates the autohide expand panel to be used in this desktop.
 * <p>
 *  This method gives a chance to the developer to override expand panel creation
 *  and provide a custom subclass.
 */
protected AutoHideExpandPanel createAutoHideExpandPanel(){
  return new AutoHideExpandPanel();
}

/** used to track focus changes for DockableSelection events
 */
private class FocusHandler implements PropertyChangeListener, ChangeListener {
  /** this one is never null (except at first time) */
  Dockable lastFocusedDockable;
  
  /** this one can be null between focusLost() and focusGained() */
  Dockable currentDockable;
  
  // focusOwner
  public void propertyChange(PropertyChangeEvent e) {
    Component c = (Component) e.getNewValue();
    while (c != null && ! (c instanceof SingleDockableContainer)){
      c = c.getParent();
    }
    
    if (c instanceof SingleDockableContainer){
      SingleDockableContainer sdc = (SingleDockableContainer) c;
      currentDockable = sdc.getDockable();
      if (sdc.getDockable() != lastFocusedDockable){
        context.fireDockableSelectionChange(new DockableSelectionEvent(sdc.getDockable()));
      }
      lastFocusedDockable = sdc.getDockable();
    } else { // c == null
      currentDockable = null;
    }
  }
  
  // This method is called whenever the selected tab of all TabbedDockableContainers changes
  public void stateChanged(ChangeEvent evt) {
    // our only problem is at startup : the focus may be out of the tabbed pane
    // and we are firing a false selection event.
    // @todo correct this behaviour by checking if the focus is not already
    // outside this component
    JTabbedPane pane = (JTabbedPane)evt.getSource();
    TabbedDockableContainer tdc = ((TabbedDockableContainer)pane);
    Dockable selDockable = tdc.getSelectedDockable();
    if (selDockable == null){ // this may happen when maximization occurs
      // ignore
    } else {
      currentDockable = selDockable;
      if (selDockable != lastFocusedDockable){
        lastFocusedDockable = selDockable;
        context.fireDockableSelectionChange(new DockableSelectionEvent(selDockable));
      }
    }
  }
  
  
  Dockable getCurrentDockable(){
    return currentDockable;
  }
}

/** Returns the name of this desktop (used by workspace management) */
public String getDesktopName() {
  return desktopName;
}

/** Updates the name of this desktop (used by workspace management) */
public void setDesktopName(String desktopName) {
  this.desktopName = desktopName;
}

/** Returns the docking context used by this desktop (might be shared with other desktop).
 *
 * @since 2.1
 */
public DockingContext getContext() {
  return context;
}

/** Updates the docking context used by this desktop.
 *<p>
 * Warning : this method should only be used by the framework itself : changing
 * a context "live" can have unpredicted and undesired side effect.
 * @since 2.1
 */
public void setContext(DockingContext context) {
  this.context = context;
}

/** returns the currently maximized dockable
 * (or null if no dockable is in that state)
 */
public Dockable getMaximizedDockable() {
  if (maximizedComponent == null){
    return null;
  } else {
    SingleDockableContainer maxContainer = (SingleDockableContainer)maximizedComponent;
    return maxContainer.getDockable();
  }
}

/** combines a docking action event and vetoable state change event to accept or reject
 * a docking action.
 */
private boolean isDockingActionAccepted(DockingActionEvent dae, DockableStateWillChangeEvent dse){
  boolean accepted = context.fireAcceptDockingAction(dae);
  if (accepted){
    if (dse != null && dae.getInitialLocation() != dae.getNextLocation()){
      return context.fireDockableStateWillChange(dse);
    } else {
      return true;
    }
  } else {
    return false;
  }
}

private void fireDockingAction(DockingActionEvent dae, DockableStateChangeEvent dse){
  context.fireDockingActionPerformed(dae);
  if (dse != null && dae.getInitialLocation() != dae.getNextLocation()){
    context.fireDockableStateChange(dse);
  }
}



}
