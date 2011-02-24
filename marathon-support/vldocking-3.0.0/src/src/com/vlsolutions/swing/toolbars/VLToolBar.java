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
import com.vlsolutions.swing.docking.animation.AnimationEvent;
import com.vlsolutions.swing.docking.animation.AnimationListener;
import com.vlsolutions.swing.docking.animation.ComponentAnimator;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;


/** A JToolbar suitable for drag and drop purposes.
 *<p>
 * To use the toolbar feature, you must follow this pattern :
 *<ul>
 * <li> The toolbars must be contained initially in a ToolBarPanel (which can contain multiple toolbars)
 * <li> The ToolBarPanel must be contained in a ToolBarContainer (which can contain as much as 4 ToolBarPanels
 * , one on each border).
 * <li> The center component of the ToolBarContainer can be the DockingDesktop, or any user component.
 *</ul>
 *
 * <p align="center"><img src="doc-files/overview.gif"><br>
 *  The VLToolBar contains buttons and other components, and is included in a ToolBarPanel of a ToolBarContainer .
 * </p>
 *<p>
 * Loading and saving toolbar configurations is done with the {@link ToolBarIO} utility class.
 *<p>
 * Toolbars can now be collapsed (to gain space on screen). this property is disabled by default to
 * keep compatibility with previous releases. For a better usage of collapsing it is better to
 * set a tooltip text on the toolbar (it will be propagated to the gripper, which will be the only
 * one component visible when collapsed).
 *
 * @author Lilian Chamontin, VLSolutions
 * @update 2006/02/20 Lilian Chamontin : added support for collapsible toolbars
 * @update 2006/03/16 Lilian Chamontin : added animation effect when collapsing toolbars
 * @update 2006/12/01 Lilian Chamontin : added protection againt class cast when dragging 
 */
public class VLToolBar extends JPanel{
  
  private ToolBarGripper gripper = new ToolBarGripper();
  
  private boolean rolloverBorderPainted = true;
  private boolean rolloverContentAreaFilled = false;
  private boolean useCustomUI = true;
  
  private int orientation = SwingConstants.HORIZONTAL;
  
  private MouseListener buttonMouseHandler = new MouseAdapter(){
    public void mouseEntered(MouseEvent e){
      AbstractButton btn = (AbstractButton) e.getSource();
      if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0){
        if (rolloverBorderPainted){
          btn.setBorderPainted(true);
        }
        if (rolloverContentAreaFilled){
          btn.setContentAreaFilled(true);
        }
      }
    }
    
    public void mouseExited(MouseEvent e){
      AbstractButton btn = (AbstractButton) e.getSource();
      btn.setBorderPainted(false);
    }
  };
  
  /** The mouse listener in charge for toolbar drag and drop */
  private GripperHandler gripperListener = new GripperHandler();
  
  /** The border used during DnD to show that the toolbar is selected */
  private Border draggedBorder = new ToolBarButtonBorder();
  
  private VLToolBarLayout horizontalLayout = new VLToolBarLayout(true, 0);
  private VLToolBarLayout verticalLayout = new VLToolBarLayout(false, 0);
  
  
  /** Constructs a toolbar with a null name (this toolbar will not be
   * able to write its structure in a XML encoder, unless you call setName(String)
   * with a non null name before saving).
   * ).
   */
  public VLToolBar() {
    this(null);
  }
  
  /** Constructs a toolbar with the given name.
   * <p>
   * The name is used when reading/writing XML configuration. It must not be null if
   * you use this feature.
   */
  public VLToolBar(String name) {
    setName(name);
    setLayout(horizontalLayout);
    gripper.addMouseMotionListener(gripperListener);
    gripper.addMouseListener(gripperListener);
    gripper.setPreferredSize(new Dimension(4, 20));
    //gripper.setMinimumSize(new Dimension(4, 4));
    add(gripper);
    setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    gripper.setToolTipText(name);
    
  }
  
  /** Adds a new separator to this toolbar */
  public void addSeparator(){
    Dimension size = (Dimension)(UIManager.get("ToolBar.separatorSize"));
    JToolBar.Separator sep = new JToolBar.Separator();
    sep.setOrientation(orientation);
    if (orientation == SwingConstants.HORIZONTAL){
      sep.setOrientation(SwingConstants.VERTICAL);
      sep.setPreferredSize(size);
      add(sep);
    } else {
      sep.setOrientation(SwingConstants.HORIZONTAL);
      sep.setPreferredSize(size);
      add(sep);
    }
  }
  
  /** Overriden to track AbstractButton added */
  public Component add(Component comp){
    if (comp instanceof AbstractButton){
      return add((AbstractButton) comp);
    } else {
      if (!(comp instanceof ToolBarGripper)) {
        comp.setVisible(!gripper.isCollapsed());
      }
      return super.add(comp);
    }
  }
  
  /** Adds a new button to this toolbar  */
  public Component add(AbstractButton button){
    if (useCustomUI){
      installButtonUI(button);
    }
    
    button.setVisible(!gripper.isCollapsed());
    super.add(button);
    
    configureButton(button);
    
    installMouseHandler(button);
    
    return button;
  }
  
  /** Adds a new button to this toolbar  */
  public Component add(JButton button){
    // this method is here to maintain backward compatibility
    return add((AbstractButton) button);
  }
  
  
  /** Install custom UI for this button : a light rollover effet and a custom rounded/shaded border.
   * <p>
   * This method can be overriden to replace the provided "look and feel" which uses the follwing configuration :
   * <ul>
   * <li> install a VLButtonUI
   * <li> set 2 pixels margins
   * <li> set a ToolBarButtonBorder.
   * </ul>
   */
  public void installButtonUI(AbstractButton button){
    button.setMargin(new Insets(2,2,2,2));
    button.setUI(new VLButtonUI());
    button.setBorder(new ToolBarButtonBorder());
  }
  
  /** Used internally to add a mouse listener to the button.
   * <p>
   * Can be overriden to implement custom event handling.
   */
  public void installMouseHandler(AbstractButton button){
    button.addMouseListener(buttonMouseHandler);
  }
  
  /** This method is invoked upon adding a button to the toolbar. It can be
   * overriden to provide another look or feel.
   *<p>
   * Default settings are :
   *<ul><li> setRolloverEnabled(true)
   * <li> setContentAreaFilled(false);
   * <li> setOpaque(false)
   * <li> setBorderPainted(false)
   *</ul>
   */
  public void configureButton(AbstractButton button){
    button.setRolloverEnabled(true);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setBorderPainted(false);
  }
  
  /** Updates the rolloverBorderPainted property.
   * <p>
   * If true, when one of the  toolbar buttons is rolled-over, its border will be shown.
   * <P>
   * DefaultValue is true
   */
  public void setRolloverBorderPainted(boolean painted){
    this.rolloverBorderPainted = painted;
  }
  
  /** Returns the state of the rolloverBorderPainted property */
  public boolean isRolloverBorderPainter(){
    return rolloverBorderPainted;
  }
  
  /**  Updates the rolloverContentAreaFilled property.
   * <p>
   * If true, when one of the toolbar buttons is rolled-over, its content will be filled.
   * <p>
   * Default value is <b>false</b> to accomodate with VLButtonUI which paints itself the button
   * interiors.
   *
   */
  public void setRolloverContentAreaFilled(boolean filled){
    this.rolloverContentAreaFilled = filled;
  }
  
  /** Returns the value of the rolloverContentAreaFilled property  */
  public boolean isRolloverContentAreaFilled(){
    return rolloverContentAreaFilled;
  }
  
  /**  Updates the useCustomUI property.
   *<p>
   * Default value is true.
   *<p>
   * When set to true the installButtonUI() method will be called when a button is added to this
   * toolbar.
   */
  public void setUseCustomUI(boolean useCustomUI){
    this.useCustomUI = useCustomUI;
  }
  
  /** Return the value of the useCustomUI property */
  public boolean isUseCustomUI(){
    return useCustomUI;
  }
  
  /** Changes the border used during drag and drop of the toolbar.
   * <p>
   * For a better user experience, please note to install an empty border of the
   * same Insets than this one (otherwise, there will be a size change of the toolbar at the beginning
   * of the drag and after the drop).
   */
  public void setDraggedBorder(Border border){
    this.draggedBorder = border;
  }
  
  /** Returns the border used during drag and drop or the toolbar */
  public Border getDraggedBorder(){
    return this.draggedBorder;
  }
  
  private void gripperDragged(MouseEvent e){
    // where are we ?
    Component gripper = e.getComponent();
    ToolBarPanel panel = (ToolBarPanel) this.getParent();
    if (!(panel.getParent() instanceof ToolBarContainer)){ //2006/12/01
      // this is a safety for users willing to include toolbar panels outside
      // a toolbar container (in that case, these toolbars aren't draggable).
      return;
    }
    ToolBarContainer container = (ToolBarContainer) panel.getParent();
    
    Point tbPanelPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
    if (panel.contains(tbPanelPoint)){
      // we're dragging inside the same ToolbarPanel
      ToolBarPanelLayout layout = (ToolBarPanelLayout) panel.getLayout();
      ToolBarConstraints constraints = layout.getInsertionContraintsAt(this, tbPanelPoint);
      if (constraints != null){
        panel.remove(this);
        panel.add(this, constraints);
        panel.revalidate();
      }
    } else {
      // we're outside this toolbar panel, check if we're above another toolbar
      // or near an empty one
      Rectangle containerBounds = container.getBounds();
      Rectangle invisiblePanelBounds = new Rectangle(containerBounds);
      ToolBarPanel topPanel = (ToolBarPanel)container.getComponentAt(BorderLayout.NORTH);
      invisiblePanelBounds.height = 10;
      if (checkGripperOnOtherPanel(topPanel, e, invisiblePanelBounds)){
        return;
      }
      invisiblePanelBounds.y = containerBounds.y + containerBounds.height - 10;
      ToolBarPanel bottomPanel = (ToolBarPanel)container.getComponentAt(BorderLayout.SOUTH);
      if (checkGripperOnOtherPanel(bottomPanel, e, invisiblePanelBounds)){
        return;
      }
      
      
      invisiblePanelBounds.y = containerBounds.y;
      invisiblePanelBounds.height = containerBounds.height;
      invisiblePanelBounds.width = 10;
      ToolBarPanel leftPanel = (ToolBarPanel)container.getComponentAt(BorderLayout.WEST);
      if (checkGripperOnOtherPanel(leftPanel, e, invisiblePanelBounds)){
        return;
      }
      
      invisiblePanelBounds.x = containerBounds.x + containerBounds.width - 10;
      ToolBarPanel rightPanel = (ToolBarPanel)container.getComponentAt(BorderLayout.EAST);
      if (checkGripperOnOtherPanel(rightPanel, e, invisiblePanelBounds)){
        return;
      }
    }
  }
  
  private boolean checkGripperOnOtherPanel(ToolBarPanel panel, MouseEvent e, Rectangle invisibleBounds){
    if (panel != null && panel != getParent()){
      if(panel.isVisible()){
        Point panelPoint = SwingUtilities.convertPoint(this, e.getPoint(), panel);
        if (panel.contains(panelPoint)){
          gripperDraggedOnOtherVisiblePanel(panel, panelPoint);
          return true;
        }
      } else { // not visible
        Point containerPoint = SwingUtilities.convertPoint(this, e.getPoint(), panel.getParent());
        if (invisibleBounds.contains(containerPoint)){
          ToolBarPanel parent = (ToolBarPanel)getParent();
          parent.remove(this);
          panel.add(this, new ToolBarConstraints(0,0)); // the panel is not visible == empty
          panel.revalidate();
          if (parent.getComponentCount() == 0){
            parent.setVisible(false); // last toolbar : hideit
          }
          return true;
        }
      }
    }
    return false;
  }
  
  private void gripperDraggedOnOtherVisiblePanel(ToolBarPanel panel, Point point){
    // we're dragging inside another ToolbarPanel
    ToolBarPanelLayout layout = (ToolBarPanelLayout) panel.getLayout();
    ToolBarConstraints constraints = layout.getInsertionContraintsAt(this, point);
    if (constraints != null){
      ToolBarPanel parent = (ToolBarPanel)getParent();
      parent.remove(this);
      panel.add(this, constraints);
      panel.revalidate();
    }
    
  }
  
  public String toString(){
    return "VLToolBar " + getName();
  }
  
  /** Updates the orientation of this toolbar.
   *<p>
   * This method is called directly by the framework on toolbar installation or
   * during drag and drop.
   */
  public void setOrientation(int orientation){
    if (this.orientation != orientation){
      this.orientation = orientation;
      gripper.setOrientation(orientation);
      Component [] comps = getComponents();
      removeAll();
      if (orientation == SwingConstants.HORIZONTAL){
        gripper.setPreferredSize(new Dimension(4, 20));
        setLayout(horizontalLayout);
        for (int i=0; i < comps.length; i++){
          if (comps[i] instanceof JToolBar.Separator){
            JToolBar.Separator sep = (JToolBar.Separator) comps[i];
            sep.setOrientation(SwingConstants.VERTICAL);
          }
          add(comps[i]);
        }
      } else {
        gripper.setPreferredSize(new Dimension(20,4));
        setLayout(verticalLayout);
//        gripper.setIcon(vGripIcon);
        for (int i=0; i < comps.length; i++){
          if (comps[i] instanceof JSeparator){
            JSeparator sep = (JSeparator) comps[i];
            sep.setOrientation(SwingConstants.HORIZONTAL);
          }
          add(comps[i]);
        }
      }
      revalidate();
    }
  }
  
  /** Updates the tooltip of the toolbar, and propagates it to the toolbar gripper. */
  public void setToolTipText(String tooltip){
    super.setToolTipText(tooltip);
    gripper.setToolTipText(tooltip);
  }
  
  boolean isInAnimationCycle = false;
  private void gripperClicked(MouseEvent e){
    if (isCollapsible() && ! isInAnimationCycle){
      setCollapsed(!gripper.isCollapsed());
    }
  }
  
  /** Hides the toolbar (just showing the gripper) */
  public void setCollapsed(final boolean collapsed) {
    if (collapsed){
      // added a subtle animation effect to give more polish to the collapsing //2006/03/16
      Rectangle bounds = getBounds();
      Rectangle newBounds;
      float animSpeed ;
      if (orientation == SwingConstants.HORIZONTAL){
         newBounds = new Rectangle(bounds.x,bounds.y, 20, getHeight());
         animSpeed = getWidth() / 800f; //  pix / sec
      } else {
         newBounds = new Rectangle(bounds.x,bounds.y, getWidth(), 20);
         animSpeed = getHeight()/ 800f; //  pix / sec
      }
      
      ComponentAnimator canim = new ComponentAnimator(this, bounds, newBounds, animSpeed);
      canim.addAnimationListener(new AnimationListener() {
        public void animation(AnimationEvent e) {
          if (e.getState() == AnimationEvent.ANIMATION_END){
            gripperListener.resetBorder();
            finishAnimation(collapsed);
          }
        }
      });
      gripperListener.installDragBorder();
      canim.start();
    } else {
      // no animation when expanding
      finishAnimation(collapsed);
    }
    
    
  }
  
  private void finishAnimation(boolean collapsed){
    gripper.setCollapsed(collapsed);
    int maxSize = 20; //2.0.6b
    Component [] comps = getComponents();
    for (int i=0; i < comps.length; i++){
      if (!(comps[i] instanceof ToolBarGripper)) {
        if (orientation == SwingConstants.HORIZONTAL){
          maxSize = Math.max(maxSize, comps[i].getHeight());
        } else {
          maxSize = Math.max(maxSize, comps[i].getWidth());
        }
        comps[i].setVisible(!collapsed);
      }
    }
    if (collapsed){
      if (orientation == SwingConstants.HORIZONTAL){
        gripper.setPreferredSize(new Dimension(6, maxSize));
      } else {
        gripper.setPreferredSize(new Dimension(maxSize, 6));
      }
    }
    
    revalidate();
  }
  
  /** Returns true if this toolbar is currently collapsed */
  public boolean isCollapsed() {
    return gripper.isCollapsed();
  }
  
  public boolean isCollapsible() {
    return gripper.isCollapsible();
  }
  
  public void setCollapsible(boolean collapsible) {
    gripper.setCollapsible(collapsible);
  }
  
  private class GripperHandler implements MouseListener, MouseMotionListener {
    Border oldBorder;
    public void mouseDragged(MouseEvent e){
      gripperDragged(e);
    }
    
    public void mouseReleased(MouseEvent mouseEvent) {      
      resetBorder();
    }
    
    /** puts back the old border (the one when not selected) */
    public void resetBorder(){
      setBorder(oldBorder);
    }
    
    public void installDragBorder(){
      oldBorder = getBorder();
      setBorder(draggedBorder);
    }
    
    public void mousePressed(MouseEvent mouseEvent) {
      installDragBorder();
    }
    
    public void mouseMoved(MouseEvent mouseEvent) {    }
    
    public void mouseExited(MouseEvent mouseEvent) {    }
    
    public void mouseEntered(MouseEvent mouseEvent) {    }
    
    public void mouseClicked(MouseEvent mouseEvent) {
      gripperClicked(mouseEvent);
    }
  }
  
  
}