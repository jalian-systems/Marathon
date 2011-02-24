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

import com.vlsolutions.swing.tabbedpane.JTabbedPaneSmartIcon;
import com.vlsolutions.swing.tabbedpane.JTabbedPaneSmartIconManager;
import com.vlsolutions.swing.tabbedpane.SmartIconJButton;
import java.awt.*;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import com.vlsolutions.swing.docking.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** A JTabbedPane customized for integration with the docking framework.
 *
 * <p>
 * Defaults :
 * <ul>
 * <li> tab position is TOP (look at DockingUISettings to change that),
 * <li> layout policy is WRAP_TAB_LAYOUT
 * (due to a java bug which limits usage of MouseMotionListeners in JTabbedPanes,
 * it is not possible to use SCROLL_LAYOUT_POLICY)
 * </ul>
 *
 * <p>
 * This tabbed pane is not meant to be used outside of DockingDesktop because of
 * their connected behaviour (drag/drop support, docking constraints, etc).
 * <p>
 * As of version 2.0 of the framework, the tabbed pane displays a close icon (other functions
 * are available with a right click menu).
 *
 * @see DockingDesktop
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * @update 2005/11/01 Lilian Chamontin : enhanced shape painting (tries to avoid
 * going outside the tab bounds)
 * @update 2005/11/10 Lilian Chamontin : added support for DnD multiple tabs at once.
 * @update 2005/12/08 Lilian Chamontin : removed a portion of code as its been refactored in BorderSplitter
 * @update 2007/11/04 Lilian Chamontin : fixed a memory leak when removing dockables
 * @update 2008/07/05 Lilian Chamontin : removed a hardcoded icon reference
 * 
 */
public class DockTabbedPane extends JTabbedPane
    implements DockDropReceiver, DockableDragSource, TabbedDockableContainer {
  
  /* no UI for this component, as it is look and feel dependent : we cannot extend BasicTabbedPaneUI */
  
  /** cache for reusing the general path lastDropPath between calls to getDropShape */
  private Rectangle lastDropBounds, lastDropTabBounds;
  private GeneralPath lastDropPath;
  
  /** set by a drag operation */
  private SingleDockableContainer draggedDockable = null;
  
  /** True when the whole tabbedpane is dragged */
  private boolean isMultipleDrag = false;
  
  protected DockingDesktop desktop;
  
  /** The pop-up menu used to provide fast "close" shortcuts (close all, close others) */
  protected JPopupMenu popup = new JPopupMenu();
  
  /** the tab currently associated with the pop-up */
  protected int popupTab = -1;
  
  /** The tab that was selected before the current one */
  protected int previousSelectedDockable = -1;
  
  
  private HashMap <DockKey, Action> closeActions = new HashMap(); // DockKey , Action
  
  private JTabbedPaneSmartIconManager tabManager = new JTabbedPaneSmartIconManager(this);
  
  
  /* This pseudo-dockable is used during drag and drop operations of the whole tabbed pane */
  static int instanceCount = 0;
  private Dockable selfDockable=  new Dockable(){
    DockKey selfKey = new DockKey("dockTab" + instanceCount++);
    public DockKey getDockKey(){
      selfKey.setName(getDockableAt(0).getDockKey().getName() + "...");
      return selfKey;
    }
    public Component getComponent(){
      return DockTabbedPane.this;
    }
  };
  
  private PropertyChangeListener keyChangeListener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent e){
      DockKey k =(DockKey)e.getSource();
      if (e.getPropertyName().equals(DockKey.PROPERTY_CLOSEABLE)){
        Action closeAction = closeActions.get(k);
        if (closeAction != null){
          closeAction.setEnabled(k.isCloseEnabled());
        }
      } else if (e.getPropertyName().equals(DockKey.PROPERTY_ICON)){
        int tab = getDockableIndex(k);
        if (tab >= 0){
          JTabbedPaneSmartIcon tabIcon = (JTabbedPaneSmartIcon) getIconAt(tab);
          tabIcon.setIcon((Icon) e.getNewValue());
          JTabbedPaneSmartIcon newIcon = tabIcon.copy();
          // we create a copy to trigger a repaint event
          setIconAt(tab, newIcon);
        }
        
      } else if (e.getPropertyName().equals(DockKey.PROPERTY_NAME)){
        int tab = getDockableIndex(k);
        if (tab >= 0){
          JTabbedPaneSmartIcon tabIcon = (JTabbedPaneSmartIcon) getIconAt(tab);
          String old = tabIcon.getLabel();
          String label = (String) e.getNewValue();
          tabIcon.setLabel(label);
          JTabbedPaneSmartIcon newIcon = tabIcon.copy();
          // we create a copy to trigger a repaint event
          setIconAt(tab, newIcon);
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              // we have to revalidate/repaint to ensure the new icon size is correctly
              // taken into account
              revalidate();
              repaint();
            }
          });
        }
      } else if (e.getPropertyName().equals(DockKey.PROPERTY_TOOLTIP)){
        int tab = getDockableIndex(k);
        if (tab >= 0){
          JTabbedPaneSmartIcon tabIcon = (JTabbedPaneSmartIcon) getIconAt(tab);
          tabIcon.setTooltipText((String) e.getNewValue());
          setToolTipTextAt(tab, (String) e.getNewValue()); // 2008/04/15 also propagate to the tab
        }
      }
    }
  };
  
  
  public DockTabbedPane() {
    int tabPlacement = UIManager.getInt("TabbedDockableContainer.tabPlacement");
    setTabLayoutPolicy(WRAP_TAB_LAYOUT);
    setTabPlacement(tabPlacement);
    
    addMouseListener(new MouseAdapter(){
      // depending on the platform, pop-up is triggered either by mousePressed or mouseReleased
      public void mousePressed(MouseEvent e){
        if (e.isPopupTrigger()){
          // popup only when left button is not pressed
          checkForPopUp(e);
        }
      }
      public void mouseReleased(MouseEvent e){
        if (e.isPopupTrigger()){
          checkForPopUp(e);
        }
      }
      public void mouseClicked(MouseEvent e){
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)){
          Dockable d = findDockableAt(e);
          if (d != null && d.getDockKey().isMaximizeEnabled()
          && d.getDockKey().getLocation() == DockableState.Location.DOCKED){ // 2005/10/10 don't maximize floatings
            desktop.maximize(d);
          }
        }
      }
    });
    
    if (UIManager.getBoolean("TabbedPane.alternateTabIcons")){ //2005/11/14
      // we use a different set of icons
      addChangeListener(new ChangeListener(){
        int prevTab = -1;
        public void stateChanged(ChangeEvent e){
          int selectedTab = getSelectedIndex();
          if (selectedTab>=0){
            installTabIcons(selectedTab, true);
            if (prevTab != -1){
              installTabIcons(prevTab, false);
            }
            previousSelectedDockable = prevTab;
            prevTab = selectedTab;
          }
        }
      });
    }
    
  }
  
  private void installTabIcons(int tab, boolean selected){
    if (tab < getTabCount()){
      JTabbedPaneSmartIcon icon = (JTabbedPaneSmartIcon) getIconAt(tab);
      if (icon != null){
        SmartIconJButton closeIcon = icon.getSmartButton(0);
        if (selected){
          closeIcon.setIcon(UIManager.getIcon("DockTabbedPane.close"));
          closeIcon.setPressedIcon(UIManager.getIcon("DockTabbedPane.close.pressed"));
          closeIcon.setRolloverIcon(UIManager.getIcon("DockTabbedPane.close.rollover"));
        } else {
          closeIcon.setIcon(UIManager.getIcon("DockTabbedPane.unselected_close"));
          closeIcon.setPressedIcon(UIManager.getIcon("DockTabbedPane.unselected_close.pressed"));
          closeIcon.setRolloverIcon(UIManager.getIcon("DockTabbedPane.unselected_close.rollover"));
        }
        icon.setSmartButton(0, closeIcon);
      }
    }
  }
  
  /** Returns the tab index of the dockable corresponding to the given key, or null if the dockable
   * doesn't belong to this tab.
   */
  public int getDockableIndex(DockKey key){
    for (int i=0; i < getTabCount(); i++){
      Dockable d = getDockableAt(i);
      if (d != null                             //2006/12/07
          && d.getDockKey().equals(key)){
        return i;
      }
    }
    return -1;
  }
  
  private void checkForPopUp(MouseEvent e){
    /*  Tabbed dockable can be of two states : docked and floating
     * when floating, the only option is to attach again.
     */
    Dockable d = findDockableAt(e);
    if (d != null){
      DockKey key = d.getDockKey();
      // first add the standard menu
      JPopupMenu popup = new JPopupMenu(key.getName());
      int menuCount = 0;
      if (key.getLocation() == DockableState.Location.DOCKED){
        if (key.isAutoHideEnabled()){
          popup.add(new JMenuItem(TabbedContainerActions.createHideTabAction(d,
              desktop)));
          menuCount++;
        }
        if (key.isFloatEnabled()){
          popup.add(new JMenuItem(TabbedContainerActions.createFloatTabAction(d,
              desktop)));
          menuCount++;
        }
        if (key.isMaximizeEnabled()){
          popup.add(new JMenuItem(TabbedContainerActions.createMaximizeTabAction(d,
              desktop)));
          menuCount++;
        }
        if (key.isCloseEnabled()){
          JMenuItem mItem = new JMenuItem(TabbedContainerActions.createCloseAction(d,
              desktop));
          // 2008/07/05 removed hardcoded icon
          final Icon closeIcon = UIManager.getIcon("DockTabbedPane.close");          
          mItem.setPressedIcon(closeIcon);          
          //mItem.setPressedIcon(new ImageIcon(getClass().getResource("close16.gif")));
          popup.add(mItem);
          menuCount++;
        }
      } else if (key.getLocation() == DockableState.Location.FLOATING){
        if (DockingUtilities.isChildOfCompoundDockable(d)){
          // as of v2.1 floating dockable which is a child of compound dockable don't have "attach" menu
        } else {
          popup.add(new JMenuItem(TabbedContainerActions.createAttachTabAction(d,
              desktop)));
          menuCount++;
        }
      } else if (key.getLocation() == DockableState.Location.HIDDEN){
        // a tabbed hidden dockable can only occur when the dockable is child of a compound dockable
        // in that case, there isn't any general popup option available
      } else {
        throw new RuntimeException("invalid state for a tabbed dockable");
      }
      
      DockableActionCustomizer customizer = d.getDockKey().getActionCustomizer();
      if (customizer != null && customizer.isTabSelectorPopUpCustomizer()){
        if (menuCount > 0){
          popup.addSeparator();
        }
        customizer.visitTabSelectorPopUp(popup, d);
      }
      if (menuCount>0){
        popup.pack();
        popup.show(this, e.getX(), e.getY());
      }
    }
    
  }
  
  
  private Dockable findDockableAt(MouseEvent e){
    Point p = e.getPoint();
    // find the tab  on which the mouse has been pressed
    popupTab = -1;
    for (int i = 0; i < getTabCount(); i++) {
      Rectangle tabbounds = getBoundsAt(i);
      if (tabbounds.contains(p)) {
        // all right
        return getDockableAt(i);
      }
    }
    return null;
  }
  
  
  
  /** Adds a new tab respecting the presentation and constraints of the component.
   *
   * {@inheritDoc}
   * */
  public void addDockable(Dockable dockable, int tab){
    SingleDockableContainer dc = DockableContainerFactory.getFactory().createDockableContainer(dockable,
        DockableContainerFactory.ParentType.PARENT_TABBED_CONTAINER);
    dc.installDocking(desktop);
    addDockableContainer(dc, tab);
    
  }
  
  private void addDockableContainer(final SingleDockableContainer dc, int tab){
    
    final DockKey key = dc.getDockable().getDockKey();
    AbstractAction closeAction = new AbstractAction("Close"){
      public void actionPerformed(ActionEvent e){
        desktop.close(dc.getDockable());
      }
    };
    closeActions.put(key, closeAction);
    
    SmartIconJButton closeIcon = new SmartIconJButton(closeAction);
    
    if (UIManager.getBoolean("TabbedPane.alternateTabIcons")){ //2005/12/09
      closeIcon.setIcon(UIManager.getIcon("DockTabbedPane.unselected_close"));
      closeIcon.setPressedIcon(UIManager.getIcon("DockTabbedPane.unselected_close.pressed"));
      closeIcon.setRolloverIcon(UIManager.getIcon("DockTabbedPane.unselected_close.rollover"));
    } else {
      closeIcon.setIcon(UIManager.getIcon("DockTabbedPane.close"));
      closeIcon.setPressedIcon(UIManager.getIcon("DockTabbedPane.close.pressed"));
      closeIcon.setRolloverIcon(UIManager.getIcon("DockTabbedPane.close.rollover"));
    }
    
    closeAction.setEnabled(key.isCloseEnabled());
    // add a tooltip
    closeAction.putValue(AbstractAction.SHORT_DESCRIPTION,
        UIManager.get("DockTabbedPane.closeButtonText"));
    
    final JTabbedPaneSmartIcon smartIcon = new JTabbedPaneSmartIcon(key.getIcon(), key.getName(),
        new SmartIconJButton[]{ closeIcon});
    smartIcon.setTooltipText(key.getTooltip());
    smartIcon.setIconForTabbedPane(this);
    
    if (tab >= getTabCount()){
      //addTab(key.getName(), key.getIcon(), (Component)dc, key.getTooltip());
      addTab("",smartIcon, (Component) dc, key.getTooltip());
      //addTab(key.getName(),smartIcon, (Component) dc, key.getTooltip());
      tab = getTabCount()-1;
    } else {
      //insertTab(key.getName(), key.getIcon(), (Component)dc, key.getTooltip(), tab);
      insertTab("", smartIcon, (Component)dc, key.getTooltip(), tab);
      //insertTab(key.getName(), smartIcon, (Component)dc, key.getTooltip(), tab);
    }
    
    key.addPropertyChangeListener(keyChangeListener);
    
    revalidate();
    repaint();
    
/*    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        setTitleAt(getDockableIndex(key), ""); // workaround for painting problems
      }
    });*/
    
  }
  
  
  public void processDockableDrag(DockDragEvent e) {
    scanDrop(e, false);
  }
  
  
  private void scanDrop(DockEvent event, boolean drop) {
    // dockDropReceiver
    DockableDragSource dragSource = event.getDragSource();
    
    // reject operation if the source is an ancestor of this component.
    if (dragSource.getDockableContainer().isAncestorOf(this)){
      // this is possible for compound containers (as they contain sub-dockables)
      // in that case, you cannnot drop a compound into one of its children  // 2007/01/08
      if (drop){
        ((DockDropEvent) event).rejectDrop();
      } else {
        ((DockDragEvent) event).delegateDrag();
      }
      return;
    }
    
    Dockable dockable = null;
    SingleDockableContainer dc = null;
    Container dragContainer = event.getDragSource().getDockableContainer();
    if (dragContainer instanceof TabbedDockableContainer){
      if (dragContainer == this){ // drop onto itself not possible
        if (drop){
          ((DockDropEvent)event).rejectDrop();
        } else {
          ( (DockDragEvent) event).rejectDrag();
        }
        return;
      }
    } else {
      // fast check to avoid processing a dockable of unknown type
      dockable = dragSource.getDockable();
      dc = DockingUtilities.findSingleDockableContainer(dockable);
      if (dc == null){
        if (drop){
          ((DockDropEvent)event).rejectDrop();
        } else {
          ( (DockDragEvent) event).rejectDrag();
        }
        return;
      }
    }
    
    Point p = event.getMouseEvent().getPoint();
    Rectangle vbounds = getSelectedComponent().getBounds();
    
    // is point inside the main tab area ?
    if (vbounds.contains(p)) {
      scanMainTabZone(event, drop, dragSource, vbounds);
      return;
    }
    
    // deny DnD coming from whole tabs
    if (dragSource.getDockableContainer() instanceof TabbedDockableContainer){
      if (drop){
        ((DockDropEvent)event).rejectDrop();
      } else {
        ( (DockDragEvent) event).rejectDrag();
      }
      return;
    }
    
    // not in the main zone, check for tabs positions
    
    // (but reject first if groups are not compatible)
    DockableDragSource s = event.getDragSource();
    Dockable d = s.getDockable();
    DockGroup dragGroup = d.getDockKey().getDockGroup();
    for (int i=0; i < getTabCount(); i++){
      DockGroup thisGroup = getDockableAt(i).getDockKey().getDockGroup();
      if (!DockGroup.areGroupsCompatible( thisGroup, dragGroup)){
        if (drop){
          ((DockDropEvent) event).rejectDrop();
        } else {
          ((DockDragEvent) event).rejectDrag();
        }
        return;
      }
    }
    
    
    for (int i = 0; i < getTabCount(); i++) {
      Rectangle tabbounds = getBoundsAt(i);
      if (tabbounds.contains(p)) {
        // insert before this tab if allowed
        SingleDockableContainer tab = (SingleDockableContainer)getComponentAt(i);
        if (tab.getDockable() == dragSource.getDockable()){
          if (drop){
            ((DockDropEvent)event).rejectDrop();
          } else {
            ((DockDragEvent) event).rejectDrag();
          }
          return;
        }
        if (i > 0){
          tab = (SingleDockableContainer) getComponentAt(i - 1);
          if (tab.getDockable() == dragSource.getDockable()) {
            if (drop){
              ((DockDropEvent)event).rejectDrop();
            } else {
              ((DockDragEvent) event).rejectDrag();
            }
            return;
          }
        }
        
        if (vbounds.equals(lastDropBounds) && tabbounds.equals(lastDropTabBounds)){
          // optimized (cached)
        } else {
          GeneralPath gp = buildPathForCurrentTab(vbounds, tabbounds);
          lastDropBounds = vbounds;
          lastDropTabBounds = tabbounds;
          lastDropPath = gp;
        }
        
        Dockable draggedDockable = dragSource.getDockable();
        DockableState.Location initialLocation = draggedDockable.getDockKey().getLocation();
        Dockable base = getDockableAt(0);
        DockableState.Location nextLocation = base.getDockKey().getLocation();
        event.setDockingAction(new DockingActionCreateTabEvent(event.getDesktop(), draggedDockable,
            initialLocation, nextLocation, base, i));
        
        
        if (drop){
          if (DockingUtilities.findTabbedDockableContainer(dockable) == this){
            // the dockable IS another tab of this container, so we just have to
            // change its tab order.
            ((DockDropEvent)event).acceptDrop(false); // don't remove
            addDockableContainer(dc, i); // simply move
          } else {
            // request the desktop to create the tab
            if (base.getDockKey().getLocation() == DockableState.Location.FLOATING){ //2005/12/09
              if (dragSource.getDockable().getDockKey().getLocation() == DockableState.Location.FLOATING){
                // this is new 2.1 feature : allowed DnD if the tab is a child of a compund dockable
                ( (DockDropEvent) event).acceptDrop(); // remove
              } else {
                ( (DockDropEvent) event).acceptDrop(false); // don't remove it yet, we need to store its previous dockable state
              }
            } else {
              ( (DockDropEvent) event).acceptDrop();
            }
            desktop.createTab(base, dragSource.getDockable(), i); //2005/10/08
            //addDockable(dragSource.getDockable(), i); //2005/10/08
          }
          setSelectedIndex(i);
        } else {
          ((DockDragEvent) event).acceptDrag(lastDropPath);
        }
        return;
      }
    }
    
    // not on the tabs, check if after the last
    int lastTab = getTabCount() -1;
    Rectangle lasttabbounds = getBoundsAt(lastTab);
    Rectangle afterlast = new Rectangle(lasttabbounds.x + lasttabbounds.width,
        lasttabbounds.y, getX() + getWidth() - (lasttabbounds.x+ lasttabbounds.width),
        lasttabbounds.height);
    if (afterlast.contains(p)){
      SingleDockableContainer dockableContainer = (SingleDockableContainer)getComponentAt(lastTab);
      if (dockableContainer.getDockable() == dragSource.getDockable()){
        if (drop){
          ((DockDropEvent)event).rejectDrop();
        } else {
          ((DockDragEvent) event).rejectDrag();
        }
        return;
      }
      
      if (vbounds.equals(lastDropBounds) && afterlast.equals(lastDropTabBounds)){
      } else {
        GeneralPath gp = buildPathAfterLastTab(vbounds, afterlast);
        lastDropBounds = vbounds;
        lastDropTabBounds = afterlast;
        lastDropPath = gp;
      }
      Dockable draggedDockable = dragSource.getDockable();
      DockableState.Location initialLocation = draggedDockable.getDockKey().getLocation();
      Dockable base = getDockableAt(0);
      DockableState.Location nextLocation = base.getDockKey().getLocation();
      event.setDockingAction(new DockingActionCreateTabEvent(event.getDesktop(), draggedDockable,
          initialLocation, nextLocation, base, lastTab+1));
      
      if (drop){
        
        if (DockingUtilities.findTabbedDockableContainer(dockable) == this){
          // the dockable IS another tab of this container, so we just have to
          // change its tab order.
          ((DockDropEvent)event).acceptDrop(false); // don't remove
          addDockableContainer(dc, lastTab+1); // simply move
        } else {
          if (base.getDockKey().getLocation() == DockableState.Location.FLOATING){ //2005/12/09
            if (dragSource.getDockable().getDockKey().getLocation() == DockableState.Location.FLOATING){
              // this is new 2.1 feature : allowed DnD if the tab is a child of a compund dockable
              ( (DockDropEvent) event).acceptDrop(); // remove
            } else {
              ( (DockDropEvent) event).acceptDrop(false); // don't remove it yet, we need to store its previous dockable state
            }
          } else {
            ( (DockDropEvent) event).acceptDrop();
          }
          desktop.createTab(base, dragSource.getDockable(), lastTab+1); //2005/10/08
          //addDockable(dragSource.getDockable(), lastTab+1);
        }
        setSelectedIndex(getTabCount()-1);
        
      } else {
        ((DockDragEvent) event).acceptDrag(lastDropPath);
      }
      return;
    }
    
    lastDropBounds = null;
    lastDropTabBounds = null;
    lastDropPath = null;
    
    if (drop){
      ((DockDropEvent)event).rejectDrop();
    } else {
      ((DockDragEvent) event).rejectDrag();
    }
    
  }
  
  /** Creates a general path suitable for showing tab insertion before the currentTab*/
  protected GeneralPath buildPathForCurrentTab(Rectangle vbounds, Rectangle tabbounds){
    GeneralPath gp = new GeneralPath();
    if (getTabPlacement() == SwingConstants.BOTTOM){
      gp.moveTo(vbounds.x, vbounds.y);
      gp.lineTo(vbounds.x + vbounds.width, vbounds.y);
      gp.lineTo(vbounds.x + vbounds.width, vbounds.y + vbounds.height);
      int height = Math.min(16, tabbounds.height);
      if (getTabRunCount() > 1 && (tabbounds.y > vbounds.y + vbounds.height + 10) ){
        gp.lineTo(vbounds.x , vbounds.y + vbounds.height);
        gp.closePath();
        gp.moveTo(tabbounds.x, tabbounds.y);
        gp.lineTo(tabbounds.x + 30, tabbounds.y);
        gp.lineTo(tabbounds.x + 25, tabbounds.y + height);
        gp.lineTo(tabbounds.x, tabbounds.y + height);
        gp.closePath();
      } else {
        gp.lineTo(tabbounds.x + 30, vbounds.y + vbounds.height);
        gp.lineTo(tabbounds.x + 25,
            vbounds.y + vbounds.height + height);
        gp.lineTo(tabbounds.x, vbounds.y + vbounds.height + height);
        gp.lineTo(tabbounds.x, vbounds.y + vbounds.height);
        gp.lineTo(vbounds.x, vbounds.y + vbounds.height);
        gp.closePath();
      }
    } else { // TOP
      int height = Math.min(16, tabbounds.height);
      gp.moveTo(vbounds.x, vbounds.y);
      if (getTabRunCount() > 1 && (tabbounds.y + tabbounds.height < vbounds.y - 10) ){
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y);
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y + vbounds.height);
        gp.lineTo(vbounds.x , vbounds.y + vbounds.height);
        gp.closePath();
        gp.moveTo(tabbounds.x, tabbounds.y);
        gp.lineTo(tabbounds.x + 25, tabbounds.y);
        gp.lineTo(tabbounds.x + 30, tabbounds.y + height);
        gp.lineTo(tabbounds.x, tabbounds.y + height);
        gp.closePath();
      } else {
        gp.lineTo(tabbounds.x, vbounds.y);
        gp.lineTo(tabbounds.x, vbounds.y - height);
        gp.lineTo(tabbounds.x + 25,
            vbounds.y - height);
        gp.lineTo(tabbounds.x + 30, vbounds.y);
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y);
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y + vbounds.height);
        gp.lineTo(vbounds.x, vbounds.y + vbounds.height);
        gp.closePath();
      }
    }
    return gp;
  }
  
  /** Creates a general path suitable for showing tab insertion after the last tab*/
  protected GeneralPath buildPathAfterLastTab(Rectangle vbounds, Rectangle afterlast){
    GeneralPath gp = new GeneralPath();
    if (getTabPlacement() == SwingConstants.BOTTOM){
      int height = Math.min(16, afterlast.height);
      gp.moveTo(vbounds.x, vbounds.y);
      gp.lineTo(vbounds.x + vbounds.width, vbounds.y);
      gp.lineTo(vbounds.x + vbounds.width, vbounds.y + vbounds.height);
      if (getTabRunCount() > 1){
        gp.lineTo(vbounds.x , vbounds.y + vbounds.height);
        gp.closePath();
        gp.moveTo(afterlast.x, afterlast.y);
        gp.lineTo(afterlast.x + 30, afterlast.y);
        gp.lineTo(afterlast.x + 25, afterlast.y + height);
        gp.lineTo(afterlast.x, afterlast.y + height);
        gp.closePath();
      } else {
        if (afterlast.x + 30 > vbounds.x +vbounds.width){
          // we're outside the component bounds, so we go back a little bit
          int newX = vbounds.x + vbounds.width - 30;
          gp.lineTo(newX + 30, vbounds.y + vbounds.height);
          gp.lineTo(newX + 25,
              vbounds.y + vbounds.height + height);
          gp.lineTo(newX, vbounds.y + vbounds.height + height);
          gp.lineTo(newX, vbounds.y + vbounds.height);
        } else {
          gp.lineTo(afterlast.x + 30, vbounds.y + vbounds.height);
          gp.lineTo(afterlast.x + 25,
              vbounds.y + vbounds.height + height);
          gp.lineTo(afterlast.x, vbounds.y + vbounds.height + height);
          gp.lineTo(afterlast.x, vbounds.y + vbounds.height);
        }
        gp.lineTo(vbounds.x, vbounds.y + vbounds.height);
        gp.closePath();
      }
    } else { // TOP
      int height = Math.min(16, afterlast.height);
      gp.moveTo(vbounds.x, vbounds.y);
      if (getTabRunCount() > 1){
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y);
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y + vbounds.height);
        gp.lineTo(vbounds.x , vbounds.y + vbounds.height);
        gp.closePath();
        gp.moveTo(afterlast.x, afterlast.y);
        gp.lineTo(afterlast.x + 25, afterlast.y);
        gp.lineTo(afterlast.x + 30, afterlast.y + height);
        gp.lineTo(afterlast.x, afterlast.y + height);
        gp.closePath();
      } else {
        if (afterlast.x + 30 > vbounds.x +vbounds.width){//2005/11/01
          // we're outside the component bounds, so we go back a little bit
          int newX = vbounds.x + vbounds.width - 30;
          gp.lineTo(newX, vbounds.y);
          gp.lineTo(newX, vbounds.y - height);
          gp.lineTo(newX + 25, vbounds.y - height);
          gp.lineTo(newX + 30, vbounds.y);
        } else {
          gp.lineTo(afterlast.x, vbounds.y);
          gp.lineTo(afterlast.x, vbounds.y - height);
          gp.lineTo(afterlast.x + 25, vbounds.y - height);
          gp.lineTo(afterlast.x + 30, vbounds.y);
        }
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y);
        gp.lineTo(vbounds.x + vbounds.width, vbounds.y + vbounds.height);
        gp.lineTo(vbounds.x, vbounds.y + vbounds.height);
        gp.closePath();
      }
    }
    return gp;
  }
  
  
  private void scanMainTabZone(DockEvent event, boolean drop,
      DockableDragSource dragSource, Rectangle vbounds) {
    
    Point p = event.getMouseEvent().getPoint();
    
    /* allow drop :
     *  - on the current tab
     *  - between tabs
     * delegate drop if mouse to near of the borders
     */
    if (scanBorderBounds(event, drop, p)) {
      return;
    }
    
    // deny DnD coming from whole tabs
    if (dragSource.getDockableContainer() instanceof TabbedDockableContainer){
      if (drop){
        ((DockDropEvent)event).rejectDrop();
      } else {
        ( (DockDragEvent) event).rejectDrag();
      }
      return;
    }
    
    
    if (scanSameComponent(event, drop, dragSource)){
      return;
    }
    
    if (scanAdjacentTab(event, drop, dragSource)) {
      return ;
    }
    
    // reject if all key groups aren't compatible
    DockGroup dragGroup = event.getDragSource().getDockable().getDockKey().getDockGroup();
    for (int i=0; i < getTabCount(); i++){
      DockGroup thisGroup = getDockableAt(i).getDockKey().getDockGroup();
      if (!DockGroup.areGroupsCompatible( thisGroup, dragGroup)){
        if (drop){
          ((DockDropEvent) event).rejectDrop();
        } else {
          ((DockDragEvent) event).rejectDrag();
        }
        return;
      }
    }
    
    
    // now we know we can insert the dragged component here
    int tab = getSelectedIndex();
    Rectangle tabbounds = getBoundsAt(tab);
    if (vbounds.equals(lastDropBounds) && tabbounds.equals(lastDropTabBounds)) {
      // optimized (cached)
    } else {
      // insert before tab
      GeneralPath gp = buildPathForCurrentTab(vbounds, tabbounds);
      lastDropBounds = vbounds;
      lastDropTabBounds = tabbounds;
      lastDropPath = gp;
    }
    Dockable base = getDockableAt(0);
    Dockable draggedDockable = dragSource.getDockable();
    DockableState.Location initialLocation = draggedDockable.getDockKey().getLocation();
    DockableState.Location nextLocation = base.getDockKey().getLocation();
    
    if (drop) {
      event.setDockingAction(new DockingActionCreateTabEvent(event.getDesktop(), draggedDockable,
          initialLocation, nextLocation, base, tab ));
      if (base.getDockKey().getLocation() == DockableState.Location.FLOATING){//2005/12/09
        if (dragSource.getDockable().getDockKey().getLocation() == DockableState.Location.FLOATING){
          // this is new 2.1 feature : allowed DnD if the tab is a child of a compound dockable
          ( (DockDropEvent) event).acceptDrop(); // remove
        } else {
          ( (DockDropEvent) event).acceptDrop(false); // don't remove it yet, we need to store its previous dockable state
        }
      } else {
        ( (DockDropEvent) event).acceptDrop();
      }
      
      //addDockable(event.getDragSource().getDockable(), tab);
      //event.getDesktop().addToTabbedGroup(base, event.getDragSource().getDockable()); //2005/07/13
      desktop.createTab(base, dragSource.getDockable(), tab); //2005/10/08
      
      setSelectedIndex(tab);
      
      //        dockingPanel.moveDock(dragSource.getTargetKey(), dc);
    } else {
      event.setDockingAction(new DockingActionCreateTabEvent(event.getDesktop(), draggedDockable,
          initialLocation, nextLocation, base, tab ));
      ( (DockDragEvent) event).acceptDrag(lastDropPath);
    }
    return;
  }
  
  private boolean scanAdjacentTab(DockEvent event, boolean drop,
      DockableDragSource dragSource) {
    if (getSelectedIndex() > 0){
      // check and reject a drag/drop of [tab-1] on [tab]
      SingleDockableContainer dockableContainer = (SingleDockableContainer)getComponentAt(getSelectedIndex()-1);
      if (dockableContainer.getDockable() == dragSource.getDockable()){
        if (drop){
          ((DockDropEvent)event).rejectDrop();
        } else {
          ((DockDragEvent) event).rejectDrag();
        }
        return true;
      }
    }
    return false;
  }
  
  private boolean scanSameComponent(DockEvent event, boolean drop,
      DockableDragSource dragSource) {
    SingleDockableContainer dockableContainer = (SingleDockableContainer)getSelectedComponent();
    if (dockableContainer.getDockable() == dragSource.getDockable()){
      // cannot drag/drop onto itself
      if (drop) {
        ( (DockDropEvent) event).rejectDrop();
      } else {
        ( (DockDragEvent) event).rejectDrag();
      }
      return true;
    }
    return false;
  }
  
  private boolean scanBorderBounds(DockEvent event, boolean drop, Point p) {
    /** we early reject this operation if the tab is used by a floating dialog  */
    Dockable firstDockable = getDockableAt(0);
    if (firstDockable.getDockKey().getLocation() == DockableState.Location.FLOATING){
      // as of v2.1 this DnD operation is allowed if the tabbed pane is a child of a compund dockable
      if (!DockingUtilities.isChildOfCompoundDockable(firstDockable)){
        // not a nested child
        return false;
      }
    }
    
    Rectangle innerBounds = null;
    
    if (getTabPlacement() == SwingConstants.BOTTOM){
      int yTab = getBoundsAt(0).y; // upper coordinate of tabbed selectors
      
      innerBounds = new Rectangle(20, 20, getWidth()-20-20, yTab-20-20);
      /** @todo : clamp values */
    } else { // TOP
      Rectangle tabBounds = getBoundsAt(0);
      int yTab = tabBounds.y + tabBounds.height; // lower coordinate of tabbed selectors
      
      innerBounds = new Rectangle(20, yTab+20, getWidth()-20-20, getHeight() - yTab-20-20);
      
    }
    
    if (! innerBounds.contains(p)){
      // too near of the bounds, suggest a split docking
      
      BorderSplitter splitter = new BorderSplitter(this)
      /* NO LONGER NEEDED : REIMPLEMENTED IN BORDER SPLITTER 2005/12/08
       {
        // we override split in order to resolve the case where
        // the drop operation would remove this tabpane from the containment
        // hierarchy
         public void split(DockDropEvent event, Container draggedContainer, DockingConstants.Split position){
           if (getTabCount() == 1){
               // there is only one tab left, meaning
               // this tabbedpane  has already been removed from containment hierarchy
             Dockable remainingDockable = getDockableAt(0);
             event.getDesktop().split(
                 remainingDockable, // one of the remaining dockables
                 event.getDragSource().getDockable(),
                 position);
           } else {
             super.split(event, draggedContainer, position); // standard behaviour
           }
         }
      }*/;
      if (drop){
        splitter.processDockableDrop((DockDropEvent) event);
      } else {
        splitter.processDockableDrag((DockDragEvent) event);
      }
      return true;
    }
    return false;
  }
  
  /**  {@inheritDoc} */
  public void processDockableDrop(DockDropEvent event) {
    scanDrop(event, true);
    
  }
  
  /**  {@inheritDoc} */
  public boolean startDragComponent(Point p) {
    clearDragState();
    
    // which component is dragged ?
    for (int i = 0; i < getTabCount(); i++) {
      Rectangle tabbounds = getBoundsAt(i);
      if (tabbounds.contains(p)) {
        draggedDockable = (SingleDockableContainer) getComponentAt(i);
        if (i > 2 && i == getTabCount()-1){
          // workaround for a focus problem : when the JTabbedPane has focus on the last tab
          // and we start a drag (and drop outside the tabbedpane, there is a
          // nonblocking stacktrace due to a bad focusIndex in BasicTabbedPaneUI (method focusLost() of the Handler)
          KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle();
          // by putting the focus up before the drop occurs, we ensure a good focusIndex is set
        }
        return true;
      }
    }
    // search for whole tab drag : look after last tab and in-between tabs
    Rectangle header = getBoundsAt(0);
    header.x = 0;
    int lastTab = getTabCount() -1;
    Rectangle lasttabbounds = getBoundsAt(lastTab);
    header = header.union(lasttabbounds);
    
    Rectangle afterlast = new Rectangle(lasttabbounds.x + lasttabbounds.width,
        lasttabbounds.y, getX() + getWidth() - (lasttabbounds.x+ lasttabbounds.width),
        lasttabbounds.height);
    if (afterlast.contains(p) || header.contains(p)){ // either after the last tab or between tabs
      this.isMultipleDrag = true;
      return true;
    }
    
    return false;
  }
  
  
  /**  {@inheritDoc} */
  public Dockable getDockable() { // from DockableDragSource
    if (isMultipleDrag){
      return selfDockable;
    } else {
      if (draggedDockable != null){
        return draggedDockable.getDockable();
      }
      return null;
    }
  }
  
  
  /**  {@inheritDoc} */
  public Dockable getSelectedDockable() {
    if (getSelectedIndex() <0){
      return null; // safety
    }
    Component comp = getComponentAt(getSelectedIndex());
    if (comp instanceof SingleDockableContainer){
      return ((SingleDockableContainer)comp).getDockable() ;
    } else {
      // this may happen when the dockable is maximized : it is temporarily replaced by an
      // impostor (a JLabel)
      return null;
    }
  }
  
  /**  {@inheritDoc} */
  public void setSelectedDockable(Dockable dockable) {
    Component c = (Component)DockingUtilities.findSingleDockableContainer(dockable);
    if (c != null && indexOfComponent(c) >= 0){
      setSelectedComponent(c);
    }
  }
  
  /**  {@inheritDoc} */
  public Dockable getDockableAt(int index) {
    Component c = getComponentAt(index);
    if (c instanceof SingleDockableContainer){
      return ((SingleDockableContainer)c).getDockable() ;
    } else {
      return null;
    }
  }
  
  
  /**  {@inheritDoc} */
  public void removeDockable(Dockable dockable) {
    DockableContainer dc = DockingUtilities.findSingleDockableContainer(dockable);
    if (dc != null){
      // after a dockable is removed, we go back to the previously selected if still visible
      if (previousSelectedDockable != -1 && previousSelectedDockable < getTabCount()){
        final Dockable d = getDockableAt(previousSelectedDockable);
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            setSelectedDockable(d);
          }
        });
      }
      remove( (Component) dc);
    } //2007/11/04
    // clean up key / actions references / listeners
    closeActions.remove(dockable.getDockKey());
    dockable.getDockKey().removePropertyChangeListener(keyChangeListener);
  }
  
  /**  {@inheritDoc} */
  public void removeDockable(int index) {
    Dockable dockable = getDockableAt(index);
    final int prev = this.previousSelectedDockable;
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        if (prev != -1 && prev < getTabCount()){
          setSelectedIndex(prev);
        }
      }
    });
    removeTabAt(index);
    // clean up key / actions references / listeners
    closeActions.remove(dockable.getDockKey());
    dockable.getDockKey().removePropertyChangeListener(keyChangeListener);
    
  }
  
  private void clearDragState(){
    // reset drag variables
    this.draggedDockable = null;
    this.isMultipleDrag = false;
  }
  
  /**  {@inheritDoc} */
  public String toString(){
    return "DockTabbedPane [" + Integer.toHexString(hashCode()) + " - tabcount=" + getTabCount() + "]";
  }
  
  /**  {@inheritDoc} */
  public int indexOfDockable(Dockable dockable) {
    DockableContainer dc = DockingUtilities.findSingleDockableContainer(dockable);
    if (dc != null){
      return indexOfComponent((Component)dc);
    } else {
      return -1;
    }
  }
  
  /**  {@inheritDoc} */
  public void installDocking(DockingDesktop desktop) {
    this.desktop = desktop;
    desktop.installDockableDragSource(this);
  }
  
  /**  {@inheritDoc} */
  public void uninstallDocking(DockingDesktop desktop) {
    desktop.uninstallDockableDragSource(this);
    
    
  }
  
  
  public Container getDockableContainer() {
    if (isMultipleDrag){
      return this;
    } else {
      return ((Container)draggedDockable);
    }
  }
  
  public void endDragComponent(boolean dropped) {
    // nothing more to do
  }
  
  
 
  
}


