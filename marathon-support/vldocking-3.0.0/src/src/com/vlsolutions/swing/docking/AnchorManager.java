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

import com.vlsolutions.swing.docking.event.DockingActionAddDockableEvent;
import com.vlsolutions.swing.docking.event.DockingActionCreateTabEvent;
import com.vlsolutions.swing.docking.event.DockingActionEvent;
import com.vlsolutions.swing.docking.event.DockingActionListener;
import com.vlsolutions.swing.docking.event.DockingActionSplitComponentEvent;
import com.vlsolutions.swing.docking.event.DockingActionSplitDockableContainerEvent;
import com.vlsolutions.swing.docking.event.DockingActionSplitDockableEvent;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JSplitPane;

/** A helper class used to specify and enforce constraints within a container
 * (DockingPanel or CompoundDockingPanel).
 * <p>
 * Constraints are currently defined as anchors (AnchorConstraints objects associated to Dockables).
 *
 * @author Lilian Chamontin, VLSolutions
 * @see AnchorConstraints
 * @since 2.1
 */
public class AnchorManager {
  
  /** Top most ancestor container */
  private Container container;
  
  /** Map of AnchorConstraints (value) associated to Dockables (key) */
  private HashMap <Dockable, AnchorConstraints> constraintsByDockable = new HashMap<Dockable, AnchorConstraints>();
  
  /** Context where docking action events are listened to */
  private DockingContext context;
  
  /** Appropriate reaction upon docking actions (vetoing the ones that would break constraints) */
  private DockingActionListener actionListener = new DockingActionListener() {
    public boolean acceptDockingAction(DockingActionEvent event) {
      switch (event.getActionType()){
        case DockingActionEvent.ACTION_ADD_DOCKABLE:{
          DockingActionAddDockableEvent e = (DockingActionAddDockableEvent) event;
          return acceptAddDockable(e);
        }
        case DockingActionEvent.ACTION_CREATE_TAB:{
          DockingActionCreateTabEvent e = (DockingActionCreateTabEvent) event;
          return acceptCreateTab(e);
        }
        case DockingActionEvent.ACTION_SPLIT_COMPONENT:{
          DockingActionSplitComponentEvent e = (DockingActionSplitComponentEvent) event;
          return acceptSplitComponent(e);
        }
        case DockingActionEvent.ACTION_SPLIT_DOCKABLE: {
          DockingActionSplitDockableEvent e = (DockingActionSplitDockableEvent) event;
          return acceptSplitDockable(e);
        }
        case DockingActionEvent.ACTION_SPLIT_DOCKABLE_CONTAINER:{
          DockingActionSplitDockableContainerEvent e = (DockingActionSplitDockableContainerEvent) event;
          return acceptSplitDockableContainer(e);
        }
        default :
          return true; // accepting everything else
      }
    }
    public void dockingActionPerformed(DockingActionEvent event) {
    }
  };
  
  /** Constructs a new AnchorManager responsible for a container (desktop or compound)
   *
   * @param context    the context (can be taken from DockingDesktop.getDockingContext() used by this manager
   * @param container  the "top level" container managed (usually a DockingDesktop or a CompoundDockingPanel)
   */
  public AnchorManager(DockingContext context, Container container) {
    this.context = context;
    this.container = container;
    context.addDockingActionListener(actionListener);
  }
  
  /** Cleanup for this manager : removes references and listeners */
  public void clear(){
    context.removeDockingActionListener(actionListener);
    constraintsByDockable.clear();
  }
  
  /** Associates an anchor constraints to a given dockable
   * @param dockable    the dockable to anchor
   * @param constraints associated anchor constraints
   */
  public void putDockableContraints(Dockable dockable, AnchorConstraints constraints){
    constraintsByDockable.put(dockable, constraints);
  }
  
  /** Returns the anchor constraints associated to a given dockable
   * @param dockable    the dockable to anchor
   * @return the constraints for this dockable, or null if no constraints is associated
   */
  public AnchorConstraints getDockableConstraints(Dockable dockable){
    return constraintsByDockable.get(dockable);
  }
  
  /** Removes an anchor constraints to a given dockable
   * @param dockable    the dockable whose anchor is to be removed
   */
  public AnchorConstraints removeDockableConstraints(Dockable dockable){
    return constraintsByDockable.remove(dockable);
  }
  
  /** Look up the spilt hierarchy to find which borders a dockable is touching.
   *
   */
  private int getContactBorders(Dockable dockable){
    return RelativeDockingUtilities.findAnchors(dockable.getComponent(), container);
  }
  
  /** Returns a list of all dockables contained into base
   */
  private ArrayList findDockables(Container base){
    ArrayList dockables = new ArrayList(10);
    Iterator <Dockable> it = constraintsByDockable.keySet().iterator();
    while (it.hasNext()){
      Dockable d = it.next();
      if (base.isAncestorOf(d.getComponent())){
        dockables.add(d);
      }
    }
    return dockables;
  }
  
  
  private boolean acceptSplitComponent(DockingActionSplitComponentEvent event ){
    Component base = event.getBase();
    ArrayList baseDockables = findDockables((Container)base);
    
    // we suppose that dockables in "base" are ok (respecting their own set of constraints before the split)
    // we still have to check is splitting will break a constraint or not
    int contactBorders = RelativeDockingUtilities.findAnchors(base, container);
    switch (event.getSplitPosition().value()){
      case DockingConstants.INT_SPLIT_TOP:
        // as we won't be touching TOP anymore, check if this
        // constraint is not set in contained dockables
        if (isConstraintSet(AnchorConstraints.ANCHOR_TOP, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_BOTTOM;
        break;
      case DockingConstants.INT_SPLIT_LEFT:
        if (isConstraintSet(AnchorConstraints.ANCHOR_LEFT, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_RIGHT;
        break;
      case DockingConstants.INT_SPLIT_BOTTOM:
        if (isConstraintSet(AnchorConstraints.ANCHOR_BOTTOM, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_TOP;
        break;
      case DockingConstants.INT_SPLIT_RIGHT:
        if (isConstraintSet(AnchorConstraints.ANCHOR_RIGHT, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_LEFT;
        break;
    }
    
    // now, we're sure we won't break a 'base' constraint
    // we have to check if the moved dockable will respect its own constraints
    
    AnchorConstraints acDockable = getDockableConstraints(event.getDockable());
    if (acDockable != null){
      int anchor = acDockable.getAnchor();
      if ((anchor & contactBorders) != anchor){ // anchor is not respected
        return false;
      }
    }
    
    return true;
  }
  private boolean acceptSplitDockable(DockingActionSplitDockableEvent event ){
    // two rules to enforce :
    // the added component must respect its own set of constraints
    // the base dockable, once the new component added, must also respect its own set of constraints
    Dockable base = event.getBase();
    AnchorConstraints acBase = getDockableConstraints(base);
    
    int contactBorders = getContactBorders(base);
    switch (event.getSplitPosition().value()){
      case DockingConstants.INT_SPLIT_TOP:
        // as we won't be touching TOP anymore, check if this
        // constraint is not set in contained dockables
        if (isConstraintSet(AnchorConstraints.ANCHOR_TOP, base)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_BOTTOM;
        break;
      case DockingConstants.INT_SPLIT_LEFT:
        if (isConstraintSet(AnchorConstraints.ANCHOR_LEFT, base)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_RIGHT;
        break;
      case DockingConstants.INT_SPLIT_BOTTOM:
        if (isConstraintSet(AnchorConstraints.ANCHOR_BOTTOM, base)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_TOP;
        break;
      case DockingConstants.INT_SPLIT_RIGHT:
        if (isConstraintSet(AnchorConstraints.ANCHOR_RIGHT, base)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_LEFT;
        break;
    }
    
    // now, we're sure we won't break a 'base' constraint
    // we have to check if the moved dockable will respect its own constraints
    
    AnchorConstraints acDockable = getDockableConstraints(event.getDockable());
    if (acDockable != null){
      int anchor = acDockable.getAnchor();
      if ((anchor & contactBorders) != anchor){ // anchor is not respected
        return false;
      }
    }
    
    return true;
  }
  
  private boolean acceptSplitDockableContainer(DockingActionSplitDockableContainerEvent event ){
    Component base = event.getBase();
    ArrayList baseDockables = findDockables((Container)base);
    
    // we suppose that dockables in "base" are ok (respecting their own set of constraints before the split)
    // we still have to check is splitting will break a constraint or not
    int contactBorders = RelativeDockingUtilities.findAnchors(base, container);
    switch (event.getSplitPosition().value()){
      case DockingConstants.INT_SPLIT_TOP:
        // as we won't be touching TOP anymore, check if this
        // constraint is not set in contained dockables
        if (isConstraintSet(AnchorConstraints.ANCHOR_TOP, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_BOTTOM;
        break;
      case DockingConstants.INT_SPLIT_LEFT:
        if (isConstraintSet(AnchorConstraints.ANCHOR_LEFT, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_RIGHT;
        break;
      case DockingConstants.INT_SPLIT_BOTTOM:
        if (isConstraintSet(AnchorConstraints.ANCHOR_BOTTOM, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_TOP;
        break;
      case DockingConstants.INT_SPLIT_RIGHT:
        if (isConstraintSet(AnchorConstraints.ANCHOR_RIGHT, baseDockables)){
          return false;
        }
        contactBorders = contactBorders & ~AnchorConstraints.ANCHOR_LEFT;
        break;
    }
    
    // now, we're sure we won't break a 'base' constraint
    // we have to check if the moved dockable container will respect its own constraints
    
    if (event.getDockableContainer() instanceof TabbedDockableContainer){
      // the only one managed currently (drag gesture : move a whole tabbed pane around)
      ArrayList containersDockables = findDockables(event.getDockableContainer());
      
      // check if these dockable have constraints
      for (int i=0; i < containersDockables.size(); i++){
        Dockable d = (Dockable) containersDockables.get(i);
        AnchorConstraints acDockable = getDockableConstraints(d);
        if (acDockable != null){
          int anchor = acDockable.getAnchor();
          if ((anchor & contactBorders) != anchor){ // anchor is not respected
            return false;
          }
        }
      }
    }
    
    return true;
  }
  
  private boolean acceptAddDockable(DockingActionAddDockableEvent event ){
    Container parent = event.getParentContainer();
    int contactBorders = RelativeDockingUtilities.findAnchors(parent, container);
    
    Dockable d = event.getDockable();
    AnchorConstraints acDockable = getDockableConstraints(d);
    if (acDockable != null){
      int anchor = acDockable.getAnchor();
      if ((anchor & contactBorders) != anchor){ // anchor is not respected
        return false;
      }
    }
    
    return true;
  }
  
  private boolean acceptCreateTab(DockingActionCreateTabEvent event ){
    int contactBorders = getContactBorders(event.getBase());
    
    Dockable d = event.getDockable();
    AnchorConstraints acDockable = getDockableConstraints(d);
    if (acDockable != null){
      int anchor = acDockable.getAnchor();
      if ((anchor & contactBorders) != anchor){ // anchor is not respected
        return false;
      }
    }
    
    return true;
  }
  
  /** checks if a given anchor is set into one of the given dockables*/
  private boolean isConstraintSet(int anchor, ArrayList baseDockables) {
    for (int i=0; i < baseDockables.size(); i++){
      AnchorConstraints ac = constraintsByDockable.get(baseDockables.get(i));
      if (ac != null){ // 2007/01/08
        if ((ac.getAnchor() & anchor) > 0){
          return true;
        }
      }
    }
    return false;
  }
  
  /** checks if a given anchor is set for a dockable*/
  private boolean isConstraintSet(int anchor, Dockable dockable) {
    AnchorConstraints ac = constraintsByDockable.get(dockable);
    if (ac == null){ //2007/01/08
      return false;
    }
    if ((ac.getAnchor() & anchor) > 0){
      return true;
    }
    return false;
  }
  
  
}
