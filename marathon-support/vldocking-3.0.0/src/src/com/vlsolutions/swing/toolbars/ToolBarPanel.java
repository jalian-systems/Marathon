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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import javax.swing.SwingConstants;


/** The toolbar panel is able to receive multiple toolbars and arrange them in
 * many columns or rows (depending on the horizontal/vertical orientation).
 *
 * <p align="center"><img src="doc-files/overview.gif"><br>
 *  The ToolBarPanel is a JPanel with a ToolBarPanelLayout. It support horizontal and vertical orientation,
 *  and can contain one or more VLToolbars (with given ToolBarContraints for positionning).
 * </p>
 * @author Lilian Chamontin, VLSolutions
 * @update 2006/09/09 Support for LTR and RTL component orientation
 */
public class ToolBarPanel extends JPanel {
  
  private int orientation = SwingConstants.HORIZONTAL;

  /** an optionnal background painter  */
  private BackgroundPainter painter; 
  
  /**
   * Constructs a new toobar panel with a LEADING alignment.
   */
  public ToolBarPanel() {
    this(FlowLayout.LEADING);
  }
  

  /**
   * Constructs a new <code>ToolBarPanel</code> with the specified
   * alignment.
   * @param align the alignment value
   *    The value of the alignment argument must be one of
   *    <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
   *    <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
   *    <code>FlowLayout.TRAILING</code>.
   * 
   */
  public ToolBarPanel(int align) {
    this(align, null);
  }
  
  /**
   * Constructs a new <code>ToolBarPanel</code> with the specified
   * alignment and background painter.
   * @param align the alignment value
   *    The value of the alignment argument must be one of
   *    <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
   *    <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
   *    <code>FlowLayout.TRAILING</code>.
   * @param painter  the background painter.
   * @since 2.1.4
   */
  public ToolBarPanel(int align, BackgroundPainter painter) {
    setLayout(new ToolBarPanelLayout(this, align));
    this.painter = painter;
  }
  
  
  
  /** Adds a new toolbar respecting the given positionning constraints.
   */
  public void add(JToolBar toolbar){
    add(toolbar, 0);
    setVisible(true);
  }
  
  
  /** Adds a new JToolBar to the Panel at the given row. Multiple VLToolBars
   * are constructed using Separators in the original JToolBar
   */
  public void add(JToolBar toolbar, int major){
    toolbar.setOrientation(orientation);
    VLToolBar[] bars = getVLToolBarsFromJToolBar(toolbar);
    for (int i = 0; i < bars.length; i++) {
      ToolBarConstraints toolBarConstraints = new ToolBarConstraints(major, i);
      add(bars[i], toolBarConstraints) ;
    }
  }
  
  /**
   * Helper function to convert a JToolBar into multiple VLToolBar objects. The JToolBar
   * is split at the separator boundary to create VLToolbars
   * @param bar, the JToolBar object
   * @return array of VLToolBar objects
   */
  private VLToolBar[] getVLToolBarsFromJToolBar(JToolBar bar) {
    ArrayList vlBars = new ArrayList();
    Component[] components = bar.getComponents();
    VLToolBar current = new VLToolBar();
    for (int i = 0; i < components.length; i++) {
      if (components[i] instanceof JButton) {
        JButton button = (JButton) components[i];
        if (button.getIcon() != null)
          button.setText(null);
        current.add(button);
      } else if (components[i] instanceof Separator) {
        vlBars.add(current);
        current = new VLToolBar();
      } else {
        System.err.println("Don't know how to handle this component " + components[i].getClass());
      }
    }
    vlBars.add(current);
    return (VLToolBar[]) vlBars.toArray(new VLToolBar[vlBars.size()]);
  }
  
  
  /** Adds a new toolbar respecting the given positionning constraints.
   */
  public void add(VLToolBar toolbar, ToolBarConstraints constraints){
    toolbar.setOrientation(orientation);
    super.add(toolbar, constraints);
    setVisible(true);
  }
  
  public void remove(Component comp){
    super.remove(comp);
    if (getComponentCount() == 0){
      setVisible(false);
    }
  }
  
  /* Updates the orientation of this toolbar. Valid values are SwingConstants.HORIZONTAL and VERTICAL*/
  public void setOrientation(int orientation){
    if (this.orientation != orientation){
      this.orientation = orientation;
      updateOrientation();
      revalidate();
    }
  }
  
  /** Returns the orientation of this toolbarpanel */
  public int getOrientation(){
    return this.orientation;
  }
  
  
  /** Propagates the orientation to the contained toolbars */
  private void updateOrientation(){
    for (int i=0; i < getComponentCount(); i++){
      VLToolBar tb = (VLToolBar) getComponent(i);
      tb.setOrientation(orientation);
    }
  }
  
  /** Overriden for optional background painting */
  public void paintComponent(Graphics g){
    if (painter != null){
      painter.paintBackground(this, g);
    }
    super.paintComponent(g);
  }

  /** Returns the optional background painter for this toolbar panel (may be null) 
   *@since 2.1.4
   */
  public BackgroundPainter getBackgroundPainter() {
    return painter;
  }

  /** Updates the optional background painter for this toolbar panel
   * @param painter the new painer (may be null)
   * @since 2.1.4
   */
  public void setPainter(BackgroundPainter painter) {
    this.painter = painter;
  }
  
}
