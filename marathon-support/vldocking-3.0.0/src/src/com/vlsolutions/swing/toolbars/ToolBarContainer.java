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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.FlowLayout;



/** The container for toolbar panels.
 *<p>
 * The layout used is BorderLayout and the CENTER component is dedicated to the user
 * (the other 4 places (NORTH, EAST, WEST and SOUTH are used to display the toolbar panels)).
 *
 * <p align="center"><img src="doc-files/overview.gif"><br>
 *  The ToolBarContainer is a JPanel with a BorderLayout, containing a user component and a maximum of four
 * ToolBarPanels.
 * </p>
 *
 *<p>
 * Loading and saving toolbar configurations is done with the {@link ToolBarIO} utility class, and works if
 * VLToolbars are registered (given a name and made know to their container by {@link #registerToolBar(VLToolBar)} ).
 *
 * @author Lilian Chamontin, VLSolutions
 * @update 2006/09/09 Support for LTR/RTL orientation
 */
public class ToolBarContainer extends JPanel {
  
  private Map componentsByContraints = new HashMap(); // this is private in borderlayout, so we need to keep a copy here
  private Map contraintsByComponents = new HashMap();
  
  private Map toolBarsByName = new HashMap();
  
  public ToolBarContainer() {
    setLayout(new BorderLayout());
  }
  
  /** Registers a new ToolBar.
   *<p>
   * Registering is used when reading a xml configuration
   */
  public void registerToolBar(VLToolBar toolbar){
    if (toolbar.getName() == null){
      throw new IllegalArgumentException("This toolbar hasn't got a name : cannot be registered");
    }
    toolBarsByName.put(toolbar.getName(), toolbar);
  }
  
  /** Unregisters a ToolBar.
   *<p>
   * Registering is used when reading a xml configuration
   */
  public void unregisterToolBar(VLToolBar toolbar){
    if (toolbar.getName() == null){
      return;
    }
    toolBarsByName.remove(toolbar.getName());
  }
  
  /** Returns the registered toolbar associated with the given name, or null if not found
   */
  public VLToolBar getToolBarByName(String name){
    return (VLToolBar)toolBarsByName.get(name);
  }
  
  /** Returns the list of currently registered toolbars.
   *
   *<p> (eturns a new list at each invocation.)
   */
  public List getRegisteredToolBars() {
    return new ArrayList(toolBarsByName.values());
  }
  
  /** Overriden to track component constraints  */
  public void add(Component comp, Object constraints){
    super.add(comp, constraints);
    componentsByContraints.put(constraints, comp);
    contraintsByComponents.put(comp, constraints);
    if (comp instanceof ToolBarPanel){
      ToolBarPanel panel = (ToolBarPanel) comp;
      if (constraints.equals(BorderLayout.EAST) || constraints.equals(BorderLayout.WEST)){
        panel.setOrientation(SwingConstants.VERTICAL);
      }
      // install the UI border
      if (constraints.equals(BorderLayout.NORTH)){
        panel.setBorder(UIManager.getBorder("ToolBarPanel.topBorder"));
      } else if (constraints.equals(BorderLayout.WEST)){
        panel.setBorder(UIManager.getBorder("ToolBarPanel.leftBorder"));
      } else if (constraints.equals(BorderLayout.EAST)){
        panel.setBorder(UIManager.getBorder("ToolBarPanel.rightBorder"));
      } else if (constraints.equals(BorderLayout.SOUTH)){
        panel.setBorder(UIManager.getBorder("ToolBarPanel.bottomBorder"));
      }
    }
  }
  
  /** Overriden to keep track of component constraints  */
  public void remove(Component comp){
    super.remove(comp);
    Object constraints = contraintsByComponents.remove(comp);
    componentsByContraints.remove(constraints);
  }
  
  /** Overriden to keep track of component constraints  */
  public void remove(int index){
    Component comp = getComponent(index);
    super.remove(index);
    Object constraints = contraintsByComponents.remove(comp);
    componentsByContraints.remove(constraints);
  }
  /** Overriden to keep track of component constraints  */
  public void removeAll(){
    super.removeAll();
    componentsByContraints.clear();
    contraintsByComponents.clear();
  }
  
  /** Returns the component for a given BorderLayout constraints */
  public Component getComponentAt(Object constraints){
    return (Component)componentsByContraints.get(constraints);
  }
  
  /** Returns the ToolBarPanel for a given BorderLayout constraints  */
  public ToolBarPanel getToolBarPanelAt(Object constraints){
    return (ToolBarPanel) getComponentAt(constraints);
  }
  
  /** Returns the BorderLayout constraints of the given component */
  public Object getConstraints(Component comp){
    return contraintsByComponents.get(comp);
  }
  
  /** Creates a default ToolBarContainer with preinstalled toolbar panels on the borders with
   * the LEADING alignment.
   *<p>
   * The toolbarPanels are then accessible with getToolBarPanelAt(constraints) where constraints values
   * are BorderLayout.NORTH, EAST, WEST and SOUTH.
   */
  public static ToolBarContainer createDefaultContainer(boolean topToolbar, boolean leftToolBar,
      boolean bottomToolBar, boolean rightToolBar){
    return createDefaultContainer(topToolbar, leftToolBar, bottomToolBar, rightToolBar,
        FlowLayout.LEADING);
  }
  
  /** Creates a default ToolBarContainer with preinstalled toolbar panels on the borders with
   * the specified alignment
   * The value of the alignment argument must be one of
   * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
   * <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
   * <code>FlowLayout.TRAILING</code>.
   *<p>
   * The toolbarPanels are then accessible with getToolBarPanelAt(constraints) where constraints values
   * are BorderLayout.NORTH, EAST, WEST and SOUTH.
   *
   *@author KDMurthy, Marathon Project.
   */
  public static ToolBarContainer createDefaultContainer(boolean topToolbar, boolean leftToolBar,
      boolean bottomToolBar, boolean rightToolBar, int alignment){
    
    
    ToolBarContainer container = new ToolBarContainer();
    if (topToolbar){
      ToolBarPanel panel = new ToolBarPanel(alignment);
      panel.setVisible(false);
      container.add(panel, BorderLayout.NORTH);
    }
    if (leftToolBar){
      ToolBarPanel panel = new ToolBarPanel(alignment);
      panel.setVisible(false);
      container.add(panel, BorderLayout.WEST);
    }
    if (bottomToolBar){
      ToolBarPanel panel = new ToolBarPanel(alignment);
      panel.setVisible(false);
      container.add(panel, BorderLayout.SOUTH);
    }
    if (rightToolBar){
      ToolBarPanel panel = new ToolBarPanel(alignment);
      panel.setVisible(false);
      container.add(panel, BorderLayout.EAST);
    }
    return container;
    
  }
  
}
