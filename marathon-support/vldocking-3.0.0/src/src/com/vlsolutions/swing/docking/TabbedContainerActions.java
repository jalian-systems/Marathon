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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/** An utility class providing default implementation of usefull actions for pop-up
 * menus in tab container selectors.
 *<p>
 *
 * @author Lilian Chamontin, VLSolutions
 *
 *@update 2005/10/07 Lilian Chamontin : added the attach action for floating tabs
 */
public class TabbedContainerActions {
  // label resources taken from BasicInternalFrameUI...
  private static final String CLOSE_TEXT = UIManager.getString("DockTabbedPane.closeButtonText");
  private static final String ICONIFY_TEXT = UIManager.getString("DockTabbedPane.minimizeButtonText");
  private static final String RESTORE_TEXT = UIManager.getString("DockTabbedPane.restoreButtonText");
  private static final String MAXIMIZE_TEXT = UIManager.getString("DockTabbedPane.maximizeButtonText");
  private static final String FLOAT_TEXT = UIManager.getString("DockTabbedPane.floatButtonText");
  private static final String ATTACH_TEXT = UIManager.getString("DockTabbedPane.attachButtonText");

  private static final Icon CLOSE_ICON = UIManager.getIcon("DockTabbedPane.menu.close");
  private static final Icon CLOSE_ALL_ICON = UIManager.getIcon("DockTabbedPane.closeAll");
  private static final Icon CLOSE_OTHER_ICON = UIManager.getIcon("DockTabbedPane.closeAllOther");
  private static final Icon ICONIFY_ICON = UIManager.getIcon("DockTabbedPane.menu.hide");
  private static final Icon MAXIMIZE_ICON = UIManager.getIcon("DockTabbedPane.menu.maximize");
  private static final Icon FLOAT_ICON = UIManager.getIcon("DockTabbedPane.menu.float");
  private static final Icon ATTACH_ICON = UIManager.getIcon("DockTabbedPane.menu.attach");

  private TabbedContainerActions() {
  }

  /** returns an action suitable for closing a dockable contained in a tab.
   *<p> 
   * Action properties : The icon is provided, and also the standard localized "close" message.
   */
  public static Action createCloseAction(final Dockable dockable, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction(CLOSE_TEXT){
      public void actionPerformed(ActionEvent e){
          desktop.close(dockable);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, CLOSE_ICON);    
    KeyStroke ks = (KeyStroke)UIManager.get("DockingDesktop.closeActionAccelerator");
    if (ks != null){
      action.putValue(AbstractAction.ACCELERATOR_KEY, ks);    
    }
    
    return action;
  }

  /** returns an action suitable for closing every dockable contained in a tab.
   *<p>
   * Action properties : The icon is provided, and also an unlocalized "Close all documents" message.
   */
  public static Action createCloseAllAction(final Dockable base, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction("Close all documents"){
      public void actionPerformed(ActionEvent e){
          desktop.closeAllDockablesInTab(base);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, CLOSE_ALL_ICON);    
    
    return action;
  }

  /** returns an action suitable for closing every dockable contained in a tab excepted the current one.
   *<p>
   * Action properties : The icon is provided, and also an unlocalized "Close all other documents" message.
   */
  public static Action createCloseAllOtherAction(final Dockable exception, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction("Close all other documents"){
      public void actionPerformed(ActionEvent e){
          desktop.closeAllOtherDockablesInTab(exception);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, CLOSE_OTHER_ICON);    
    
    return action;
  }

  /** returns an action suitable for maximizing a dockable contained in a tab.
   *<p>
   * Action properties : The icon is provided, and also a localized "Maximize" message.
   */
  public static Action createMaximizeTabAction(final Dockable dockable, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction(MAXIMIZE_TEXT){
      public void actionPerformed(ActionEvent e){
          desktop.maximize(dockable);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, MAXIMIZE_ICON);    
    KeyStroke ks = (KeyStroke)UIManager.get("DockingDesktop.maximizeActionAccelerator");
    if (ks != null){
      action.putValue(AbstractAction.ACCELERATOR_KEY, ks);    
    }
    
    return action;
  }

  /** returns an action suitable for hiding a dockable contained in a tab.
   *<p>
   * Action properties : The icon is provided, and also a localized "Iconify" message.
   */
  public static Action createHideTabAction(final Dockable dockable, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction(ICONIFY_TEXT){
      public void actionPerformed(ActionEvent e){
          desktop.setAutoHide(dockable, true);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, ICONIFY_ICON);    
    KeyStroke ks = (KeyStroke)UIManager.get("DockingDesktop.dockActionAccelerator");
    if (ks != null){
      action.putValue(AbstractAction.ACCELERATOR_KEY, ks);    
    }

    return action;
  }

  /** returns an action suitable for floating (detach) a dockable contained in a tab.
   *<p>
   * Action properties : The icon is provided, and a default "Detach" message.
   */
  public static Action createFloatTabAction(final Dockable dockable, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction(FLOAT_TEXT){
      public void actionPerformed(ActionEvent e){
          desktop.setFloating(dockable, true);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, FLOAT_ICON);    
    KeyStroke ks = (KeyStroke)UIManager.get("DockingDesktop.floatActionAccelerator");
    if (ks != null){
      action.putValue(AbstractAction.ACCELERATOR_KEY, ks);    
    }
    
    return action;
  }

  /** returns an action suitable for attaching a dockable contained in a floating tab.
   *<p>
   * Action properties : The icon is provided, and a default "Attach" message.
   * @since 2.0.1
   */
  public static Action createAttachTabAction(final Dockable dockable, final DockingDesktop desktop){
    
    AbstractAction action = new AbstractAction(ATTACH_TEXT){
      public void actionPerformed(ActionEvent e){
          desktop.setFloating(dockable, false);        
      }
    };
    action.putValue(AbstractAction.SMALL_ICON, ATTACH_ICON);    
    KeyStroke ks = (KeyStroke)UIManager.get("DockingDesktop.floatActionAccelerator");
    if (ks != null){
      action.putValue(AbstractAction.ACCELERATOR_KEY, ks);    
    }
    
    return action;
  }
  
  
}
