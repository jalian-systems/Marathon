package com.vlsolutions.swing.docking;

import com.vlsolutions.swing.docking.event.DockDragEvent;
import com.vlsolutions.swing.docking.event.DockDropEvent;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;

/** A Button that can be used to store a dockable (for example to allow moving a dockable
 * between two workspaces).
 *
 * <p>
 * The button is able to display the Icon, text and toolip of a dockable key, and can be used as a drag
 * source (to initiate a drop into a desktop).
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class DockableStoreButton extends JButton implements DockDropReceiver, DockableDragSource {
  
  private Dockable dockable;
  
  private String emptyText = "Nothing stored";
  private String emptyTooltip = "<html>Move and drop a view of your application above this button to store it";
  private Icon emptyIcon = null;
  private String fullText = "View &1 stored";
  private String fullTooltip = "<html>Drag this button to move the view <b>&1</b> to a new position";
  
  public DockableStoreButton(String emptyText) {
    super(emptyText);
    this.emptyText = emptyText;
    updateButtonText();
  }
  
  public DockableStoreButton() {
    updateButtonText();
  }
  
  
  /** when a dockable is dragged above this button, the button stores it as its new
   * target dockable.
   * @see #getDockable()
   */
  public void processDockableDrag(DockDragEvent event) {
    event.rejectDrag();
  }
  
  /** Rejects the drop as this action isn't possible on this component */
  public void processDockableDrop(DockDropEvent event) {
    setDockable(event.getDragSource().getDockable());
    event.rejectDrop();
  }
  
  public void setDockable(Dockable dockable) {
    this.dockable = dockable;
    updateButtonText();
  }

  /** Override this method if you want to change the display (text and icon) of this button and 
   * the default settings don't suit your needs.
   */
  public void updateButtonText(){
    if (dockable == null){
      setText(emptyText);
      setToolTipText(emptyTooltip);
      setIcon(null);
    } else {
      String text = fullText.replaceAll("&1", dockable.getDockKey().getName());
      setText(text);
      String tttext = fullTooltip.replaceAll("&1", dockable.getDockKey().getName());
      setToolTipText(tttext);
      setIcon(dockable.getDockKey().getIcon());
    }
  }
  
  
  public boolean startDragComponent(Point p) {
    return dockable != null;
  }
  
  /** Return the dockable currently stored (may be null) */
  public Dockable getDockable() {
    return dockable;
  }
  
  public Container getDockableContainer() {
    return (Container) DockingUtilities.findDockableContainer(dockable);
  }
  
  public void endDragComponent(boolean dropped) {
    if (dropped){
      dockable = null;
      updateButtonText();
    }
  }
  
  /** Returns the text used for this button when no dockable is stored*/
  public String getEmptyText() {
    return emptyText;
  }
  
  /** updates the text used for this button when no dockable is stored*/
  public void setEmptyText(String emptyText) {
    this.emptyText = emptyText;
  }
  
  /** Returns the text used as tooltip for this button when no dockable is stored*/
  public String getEmptyTooltip() {
    return emptyTooltip;
  }
  
  /** Updates the text used as tooltip for this button when no dockable is stored*/
  public void setEmptyTooltip(String emptyTooltip) {
    this.emptyTooltip = emptyTooltip;
  }
  
  /** Returns the Icon used with this button when no dockable is selected */
  public Icon getEmptyIcon() {
    return emptyIcon;
  }
  
  /** Updates the icon used for this button when no dockable is stored*/
  public void setEmptyIcon(Icon emptyIcon) {
    this.emptyIcon = emptyIcon;
  }
  
  /** Returns the text used for this button when a dockable is stored*/  
  public String getFullText() {
    return fullText;
  }
  
  /** Returns the text used for this button when a dockable is stored*/  
  public void setFullText(String fullText) {
    this.fullText = fullText;
  }
  
  
  /** Returns the text used for as tooltip this button when a dockable is stored*/
  public String getFullTooltip() {
    return fullTooltip;
  }
  
  /** Updates the text used as tooltip for this button when no dockable is stored.
   * The special value '&1' will be replaced by the name of this dockable.
   *<p>
   * For example "&1 stored" could be replaced by "MyDockable stored"
   */
  public void setFullTooltip(String fullTooltip) {
    this.fullTooltip = fullTooltip;
  }
  




}
