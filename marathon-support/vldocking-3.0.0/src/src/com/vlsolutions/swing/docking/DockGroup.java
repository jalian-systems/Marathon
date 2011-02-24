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

import java.util.ArrayList;
import javax.swing.Action;

/** This class describes a logical group of Dockables.
 *<p>
 * When a dockable is associated to a DockGroup (via its DockKey) drag and drop operations 
 * are limited to dockables of the same group (or new location) or compatible group (a group with 
 * a common ancestor see {@link #isCompatibleGroup(DockGroup)} ). 
 *<p>
 * It becomes easy to develop, for example, and MDI program : all documents are given the same
 * DockGroup, and other dockable are given another group so they don't mess with each other.
 *<p>
 * The DockGroups can be nested to allow some mixing (for example, an editor application can consist of 
 * 4 groups : "Formaters" and "Files" the parent of "TextFiles" and "HTMLfile". In that case, 
 * Textfiles and Htmlfiles can be part of tha same tabbed container whereas Formaters are kept outside.
 *
 *
 * @see DockKey#setDockGroup(DockGroup)
 * @author Lilian Chamontin, VLSolutions
 * @since 2.0
 */
public class DockGroup {
  
  /** The optional parent of this group */
  private DockGroup parent;
  
  /** internal name for comparisons */
  private String name;
  
    
  /** Constructs a new dockgroup with a given name.
   *<p> Note that names must be unique (this feature is not controlled by the framework)
   * through all DockGroups. 
   */
  public DockGroup(String name) {
    this.name = name;
  }

  /** Constructs a new dockgroup with a given name and a parent group.
   *<p> Note that names must be unique (this feature is not controlled by the framework)
   * through all DockGroups. 
   */
  public DockGroup(String name, DockGroup parent) {
    this.name = name;
    this.parent = parent;
  }
  
  /** returns the parent of this group (may be null) */
  public DockGroup getParent(){
    return parent;
  }
  
  /** Updates the parent of this group */
  public void setParent(DockGroup parent){
    this.parent = parent;
  }
  
  /** returns the name of this group (which is used as an internal key) */
  public String getName(){
    return name;
  }
  
  /** overriden for storage/comparisons with the "name" property */
  public boolean equals(Object o){
    return o != null && o instanceof DockGroup && ((DockGroup)o).name.equals(name);
  }
  
  /** overriden for storage/comparisons with the "name" property */
  public int hashCode(){
    return name.hashCode();
  }

  /** returns true if this group is the ancestor of group g.
   * To be an ancestor, this group must be either equel to g, or to one of g's parents chain.
   */
  public boolean isAncestorOf(DockGroup g){
    if (this.equals(g)){
      return true;
    } else {
      DockGroup parent = g.parent;
      while (parent != null){
        if (this.equals(parent)){
          return true;
        } else {
          parent = parent.parent;
        }
      }
    }
    return false;
  }
  
  /** returns true is this group is compatible with the given parameter.
   *<p> 
   * two groups are compatible if they share the same ancestor or if one is the 
   * ancestor of the other. 
   * <p>
   * When compatibility  is found, dockables of those groups can be docked in 
   * the same tab container.
   */
  public boolean isCompatibleGroup(DockGroup g){
    if (g == null) return false;
    if (this.equals(g)){
      return true;
    } else if (this.isAncestorOf(g)) {
      return true;
    } else if (g.isAncestorOf(this)){
      return true;
    } else if (parent != null){
      return parent.isCompatibleGroup(g.parent);
    } else {
      return false;
    }
  }
  
  /** convenience method to check compatibility between groups */
  public static boolean areGroupsCompatible(DockGroup g1, DockGroup g2){
    if (g1 == null){
      return g2 == null;
    } else if (g2 == null){
      return g1 == null;
    } else {
      return g1.isCompatibleGroup(g2);
    }
  }
  
  
}
