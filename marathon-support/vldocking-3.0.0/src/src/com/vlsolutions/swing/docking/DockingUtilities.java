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

import java.awt.*;
import java.awt.peer.LightweightPeer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.*;

/** Utility class implementing search/replace algorithms used by the framework.
 * <p>
 * This class is not inteded for API users, it should be let to framework developpers.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 *
 */
public class DockingUtilities {
  
  private DockingUtilities(){
    // singleton
  }
  
  
  /** returns the first DockableContainer which is parent of this dockable component */
  public static DockableContainer findDockableContainer(Dockable dockable){
    if (dockable == null) return null;
    Component comp = dockable.getComponent();
    if (comp == null) return null;
    
    while (comp != null){
      if (comp instanceof DockableContainer){
        return (DockableContainer) comp;
      }
      comp = comp.getParent();
    }
    return null;
  }
  
  /** returns the first DockableContainer which is parent of this dockable component */
  public static SingleDockableContainer findSingleDockableContainer(Dockable dockable){
    if (dockable == null) return null;
    Component comp = dockable.getComponent();
    if (comp == null) return null;
    
    while (comp != null){
      if (comp instanceof SingleDockableContainer){
        return (SingleDockableContainer) comp;
      }
      comp = comp.getParent();
    }
    return null;
  }
  
  /** Returns the first TabbedDockableContainer which is parent of this dockable component, or
   * null is there isn't any.
   * */
  public static TabbedDockableContainer findTabbedDockableContainer(Dockable dockable){
    if (dockable == null) return null;
    Component comp = dockable.getComponent();
    if (comp == null) return null;
/*    while (comp != null){ //2005/10/10
      if (comp instanceof TabbedDockableContainer){
        return (TabbedDockableContainer) comp;
      }
      comp = comp.getParent();
    }
 */
    while (comp != null){
      // new version to support compoundDockable : stop after the first parent of
      // the first SingleDockableContainer found
      if (comp instanceof SingleDockableContainer){
        comp = comp.getParent();
        if (comp instanceof TabbedDockableContainer){
          return (TabbedDockableContainer)comp;
        } else {
          return null;
        }
      }
      comp = comp.getParent();
    }
    return null;
  }
  
  private static void swapComponents(SplitContainer split1, SplitContainer split2, Component comp1, Component comp2){
    boolean isLeft1 = split1.getLeftComponent()== comp1;
    int divider1 = split1.getDividerLocation();
    if (split1 == split2){
      split1.remove(comp1);
      split1.remove(comp2);
      if (isLeft1){
        split1.setLeftComponent(comp2);
        split1.setRightComponent(comp1);
      } else {
        split1.setLeftComponent(comp1);
        split1.setRightComponent(comp2);
      }
      split1.setDividerLocation(divider1);
      split1.revalidate();
    } else {
      boolean isLeft2 = split2.getLeftComponent()== comp2;
      int divider2 = split2.getDividerLocation();
      split1.remove(comp1);
      split2.remove(comp2);
      if (isLeft1){
        split1.setLeftComponent(comp2);
      } else {
        split1.setRightComponent(comp2);
      }
      split1.setDividerLocation(divider1);
      if (isLeft2){
        split2.setLeftComponent(comp1);
      } else {
        split2.setRightComponent(comp1);
      }
      split2.setDividerLocation(divider2);
    }
  }
  
  /** Swaps two toplevel DockableContainers (their parent must be a SplitContainer) */
  public static void swapComponents(Component comp1, Component comp2){
    Container parent1 = comp1.getParent();
    Container parent2 = comp2.getParent();
    if (parent1 instanceof SplitContainer){
      if (parent2 instanceof SplitContainer){
        swapComponents((SplitContainer) parent1, (SplitContainer) parent2,  comp1, comp2);
      } else {
        // nothing
      }
    }
  }
  
  /** Invoked every time the layout is changed, to rebuild the weighting of split containers */
  public static void updateResizeWeights(DockingPanel dockingPanel){
    if (dockingPanel.getComponentCount() > 0){
      resetSplitWeight(dockingPanel.getComponent(0));
    }
  }
  
  
  private static float resetSplitWeight(Component comp){
    if (comp instanceof SplitContainer){
      SplitContainer split = (SplitContainer) comp;
      float leftWeight = resetSplitWeight(split.getLeftComponent());
      float rightWeight = resetSplitWeight(split.getRightComponent());
      float sum = leftWeight + rightWeight;
      if (sum == 0){
        split.setResizeWeight(0.5); // half weight for each side
        return 0;
      } else if (leftWeight == 0){ // and rightWeight != 0
        split.setResizeWeight(0); // every resize goes to the right
        return rightWeight;
      } else if (rightWeight == 0){ // and leftWeight != 0
        split.setResizeWeight(1); // every resize goes to the left
        return leftWeight;
      } else {
        float proportion = leftWeight / sum; // when near 0 => right, when near 1 => left
        split.setResizeWeight(proportion);
        return Math.max(leftWeight, rightWeight);
      }
    } else if (comp instanceof SingleDockableContainer){
      return ((SingleDockableContainer)comp).getDockable().getDockKey().getResizeWeight();
    } else if (comp instanceof TabbedDockableContainer){
      TabbedDockableContainer tab = (TabbedDockableContainer) comp;
      float max = 0;
      for (int i=0; i < tab.getTabCount(); i++){
        Dockable d = tab.getDockableAt(i);
        if (d != null){
          float v = d.getDockKey().getResizeWeight();
          if (v > max) {
            max = v;
          }
        }
      }
      return max;
    } else if (comp instanceof MaximizedComponentReplacer){ //2007/01/18
      // ignore as this component replaces the maximized dockable in the 
      // docking container hierarchy
    } else {
      System.err.println("Wrong hierarchy in docking panel (resetSplitWeight error) " + comp);
    }
    return 0;
  }
  
  /** Child replacement */
  public static void replaceChild(Container parent, Component child, Component newChild){
    //System.out.println("replace child : PARENT=" + parent + ", CHILD=" + child + ", NEW=" + newChild);
    if (parent instanceof SplitContainer) {
      final SplitContainer split = (SplitContainer) parent;
      final int location = split.getDividerLocation();
      if (split.getLeftComponent() == child) {
        split.remove(child);
        split.setLeftComponent(newChild);
      } else if (split.getRightComponent() == child) {
        split.remove(child);
        split.setRightComponent(newChild);
      } else {
        throw new IllegalArgumentException("wrong hierarchy");
      }
//      SwingUtilities.invokeLater(new Runnable() {
//        public void run() {
      split.setDividerLocation(location);
      split.revalidate();
      split.doLayout();
//        }
//      });
    } else if (parent instanceof TabbedDockableContainer){ // 2005/07/12...
      // interface trick : we need to access the JTabbedPane internals
      // the newChild can be a dummy JComponent (when used in maximize/restore)
      // or a fully featured dockable (other usages)
      // this is not really clean : it's the problem when using interfaces over subclasses...
      if (parent instanceof JTabbedPane){
        TabbedDockableContainer tparent = (TabbedDockableContainer) parent;
        JTabbedPane jtp = (JTabbedPane) tparent;
        int index = jtp.indexOfComponent(child);
        jtp.remove(index);
        if (newChild instanceof SingleDockableContainer){
          tparent.addDockable(((SingleDockableContainer) newChild).getDockable(), index);
          jtp.setSelectedIndex(index);
        } else { // dummy component
          jtp.add(newChild, index);
          jtp.setSelectedIndex(index);
        }
      } else {
        throw new RuntimeException("Unknown TabbedDockableContainer");
      }                                                   // ...2005/07/12
    } else { // we're on top level (panel with borderlayout)
      parent.remove(child);
      parent.add(newChild, BorderLayout.CENTER);
      parent.invalidate();
      parent.validate();
    }
    
  }
  
  /** Returns the split pane containing this dockable (if any), or null if this dockable
   * isn't contained in a splitpane.
   * <p>
   * If the dockable is nested in a TabbedDockableContainer, the split pane returned will
   * be the one containing the tabbed container (if any).
   *
   * */
  public static SplitContainer getSplitPane(Dockable dockable, int orientation ){
    Component comp = dockable.getComponent();
    boolean found = false;
    while (comp != null && !found){
      if (comp instanceof SplitContainer && ((SplitContainer)comp).getOrientation() == orientation){
        found = true;
      } else {
        comp = comp.getParent();
      }
    }
    return (SplitContainer)comp;
  }
  
  /** A utility method to find the first single dockable container ancestor of the given component.
   *<p>
   * This method may return null if no SingleDockableContainer ancestor is found.
   */
  public static SingleDockableContainer findSingleDockableContainerAncestor(Component component){
    Component parent = component.getParent();
    while (parent != null && ! (parent instanceof SingleDockableContainer)){
      parent = parent.getParent();
    }
    return (SingleDockableContainer)parent;
  }
  
  /** Utility method to find out if a component is heavyweight (of if it contains a heavyweight comp)*/
  public static boolean isHeavyWeightComponent(Component comp){
    if (comp instanceof Container){
      // short cut
      Object peer = comp.getPeer();
      if (!(peer == null || peer instanceof LightweightPeer)){
        // it's not a lightweight
        return true;
      } else {
        // long way
        Container c = (Container) comp;
        for (int i=0; i < c.getComponentCount(); i++){
          Component child = c.getComponent(i);
          if (isHeavyWeightComponent(child)){
            return true;
          }
        }
        return false;
      }
    } else {
      Object peer = comp.getPeer();
      return !(peer == null || peer instanceof LightweightPeer);
    }
  }
  
  /** Returns whether we can use the secured and 1.5 MouseInfo class */
  public static boolean canUseMouseInfo(){
    if (System.getProperty("java.version").compareTo("1.5")>=0){
      return getMouseLocation() != null;
    } else {
      return false; // not present in pre 1.5 versions
    }
  }
  
  /** Returns the mouse location on screen or null if ran in an untrusted environement/ java 1.4  */
  public static Point getMouseLocation(){
    try {
      //Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
      // this class in not compatible with 1.4
      // so instead we use reflection for allowing 1.4 compilation
      Class mouseInfoClass = Class.forName("java.awt.MouseInfo");
      final Class [] noArgs = new Class[0];
      Method m = mouseInfoClass.getMethod("getPointerInfo", noArgs);
      Object pointerInfo = m.invoke(null, (Object[])null);
      Class pointerInfoClass = Class.forName("java.awt.PointerInfo");
      Method getLocationMethod = pointerInfoClass.getMethod("getLocation", noArgs);
      Point mouseLocation = (Point)getLocationMethod.invoke(pointerInfo, (Object[])null);
      return mouseLocation;
    } catch (ClassNotFoundException ignore){
    } catch (NoSuchMethodException ignore){
    } catch (IllegalAccessException e){
    } catch (InvocationTargetException ignore){
    }
    return null;
  }
  
  /** packs a detached dockable, regardless of its type (frame or dialog) */
  public static void pack(FloatingDockableContainer fdc){ //2006/02/20
    if (fdc instanceof JFrame){
      ((JFrame) fdc).pack();
    } else if (fdc instanceof JDialog){
      ((JDialog) fdc).pack();
    }
  }
  
  /** resizes a detached dockable, regardless of its type (frame or dialog) */
  public static void setSize(FloatingDockableContainer fdc, Dimension size){ //2006/02/20
    if (fdc instanceof JFrame){
      ((JFrame) fdc).setSize(size);
    } else if (fdc instanceof JDialog){
      ((JDialog) fdc).setSize(size);
    }
  }
  
  /** validates a detached dockable, regardless of its type (frame or dialog) */
  public static void validate(FloatingDockableContainer fdc){ //2006/02/20
    ((Window)fdc).validate();
  }
  
  /** positions a detached dockable, regardless of its type (frame or dialog) */
  public static void setLocation(FloatingDockableContainer fdc, Point location){ //2006/02/20
    ((Window)fdc).setLocation(location);
  }
  
  /** positions a detached dockable, regardless of its type (frame or dialog) */
  public static void setLocationRelativeTo(FloatingDockableContainer fdc, Component rel){ //2006/02/20
    ((Window)fdc).setLocationRelativeTo(rel);
  }
  
  /** shows a detached dockable, regardless of its type (frame or dialog) */
  public static void setVisible(FloatingDockableContainer fdc, boolean visible){ //2006/02/20
    ((Window)fdc).setVisible(visible);
  }
  
  /** disposes a detached dockable, regardless of its type (frame or dialog) */
  public static void dispose(FloatingDockableContainer fdc) {
    ((Window)fdc).dispose();
  }
  
  /** returns the root pane used by this detached dockable container, regardless of its type (frame or dialog)*/
  public static JRootPane getRootPane(FloatingDockableContainer fdc) {
    if (fdc instanceof JFrame){
      return ((JFrame) fdc).getRootPane();
    } else if (fdc instanceof JDialog){
      return ((JDialog) fdc).getRootPane();
    } else {
      return null; // not reachable
    }
  }
  
  static void setBounds(FloatingDockableContainer fdc, Rectangle bounds) {
    ((Window)fdc).setBounds(bounds);
  }
  
  /** Creates a list of all dockable children contained in the given compound dockable.
   * <p> If the compound dockable contains another compound dockable this one is also added,
   * along with its own children.
   * @return an ArrayList of Dockable
   */
  public static ArrayList findCompoundDockableChildren(CompoundDockable compoundDockable) {
    ArrayList list = new ArrayList();
    CompoundDockingPanel cdp = (CompoundDockingPanel) compoundDockable.getComponent();
    fillCompoundChildren(cdp, list);
    return list;
    
  }
  
  private static void fillCompoundChildren(CompoundDockingPanel cdp, ArrayList list) {
    if (cdp.getComponentCount() > 0){
      Component c = cdp.getComponent(0);
      fillCompoundChildren(c, list);
    }
  }
  
  private static void fillCompoundChildren(Component c, ArrayList list) {
    if (c instanceof CompoundDockingPanel){
      fillCompoundChildren((CompoundDockingPanel)c, list);
    } else if (c instanceof SingleDockableContainer){
      list.add(((SingleDockableContainer)c).getDockable());
    } else if (c instanceof SplitContainer){
      fillCompoundChildren(((SplitContainer)c).getLeftComponent(), list);
      fillCompoundChildren(((SplitContainer)c).getRightComponent(), list);
    } else if (c instanceof TabbedDockableContainer){
      TabbedDockableContainer tdc = (TabbedDockableContainer)c;
      for (int i=0; i < tdc.getTabCount(); i++){
        Dockable d = tdc.getDockableAt(i);
        if (d instanceof CompoundDockable){
          list.add(d);
          CompoundDockingPanel cdp = (CompoundDockingPanel) d.getComponent();
          fillCompoundChildren(cdp, list);
        } else {
          list.add(d);
        }
      }
    }
  }
  
  /** checks if this dockable is a child of a compound dockable */
  public static boolean isChildOfCompoundDockable(Dockable dockable) {
    Container container = dockable.getComponent().getParent();
    while (container != null){
      if (container instanceof CompoundDockingPanel
          && ((CompoundDockingPanel)container).getDockable() != dockable){
        // avoid returning true if the dockable if itself a compound
        return true;
      }
      container = container.getParent();
    }
    return false;
  }
  
  /** searches up the dockable container hierarchy and returns the first ancestor
   * which is a CompoundDockable (or null if not found).
   */
  public static Container findCompoundAncestorContainer(Dockable dockable) {
    Container container = dockable.getComponent().getParent();
    while (container != null){
      if (container instanceof CompoundDockingPanel
          && ((CompoundDockingPanel)container).getDockable() != dockable){
        // avoid returning true if the dockable if itself a compound
        return container;
      }
      container = container.getParent();
    }
    return null;
  }
  
  
  /** searches up the dockable container hierarchy and returns the dockable state of the
   * <b>last<b> (top most) ancestor which is a CompoundDockable (or null if not found).
   */
  public static DockableState.Location getTopMostAncestorContainerState(Dockable dockable) {
    Container container = dockable.getComponent().getParent();
    Container topMostContainer = null;
    while (container != null){
      if (container instanceof CompoundDockingPanel
          && ((CompoundDockingPanel)container).getDockable() != dockable){
        // avoid returning true if the dockable if itself a compound
        topMostContainer = container;
      }
      container = container.getParent();
    }
    if (topMostContainer != null){
      CompoundDockingPanel cdp = (CompoundDockingPanel) topMostContainer;
      return cdp.getDockable().getDockKey().getLocation();
    }
    return null;
  }
  
  /** Returns a DockableState value corresponding to this component or null if not found.
   *<p>
   * Two states are currently managed : docked and floating (not hidden/maximized/closed).
   */
  public static DockableState.Location getDockableLocationFromHierarchy(Component comp) {
    if (comp == null){
      return null;
    }
    
    while (comp != null){
      if (comp instanceof DockingPanel){
        return DockableState.Location.DOCKED;
      } else if (comp instanceof FloatingDockableContainer){
        return DockableState.Location.FLOATING;
      }
      comp = comp.getParent();
    }
    return null;
  }
  
  
}
