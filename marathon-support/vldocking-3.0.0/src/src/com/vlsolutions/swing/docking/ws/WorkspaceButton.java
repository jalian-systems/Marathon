package com.vlsolutions.swing.docking.ws;

import com.vlsolutions.swing.docking.DockingContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;

/** A simple button that applies a workspace to a given Docking context when pressed .
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1.3
 *
 */
public class WorkspaceButton extends JButton {
  private Workspace workspace;
  private DockingContext ctx;
  
  private ActionListener actionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      try {
        applyWorkspace();
      } catch (WorkspaceException ex) {
        ex.printStackTrace();
      }
    }
  };
  
  /** default constructor  */
  public WorkspaceButton() {
    addActionListener(actionListener);
  }
  
  /** constructor with a menu text */
  public WorkspaceButton(String text){
    super(text);
    addActionListener(actionListener);
  }

  /** constructor with a menu text and an icon */
  public WorkspaceButton(String text, Icon icon){
    super(text, icon);
    addActionListener(actionListener);
  }
  
  /** Invoked when the button is pressed : applies the workspace to the associated context */
  public void applyWorkspace() throws WorkspaceException {
    workspace.apply(ctx);    
  }

  /** Returns the workspace used by this button */
  public Workspace getWorkspace() {
    return workspace;
  }

  /** updates the workspace to be used by this button  */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /** Returns the docking context used by this button  */
  public DockingContext getDockingContext() {
    return ctx;
  }
  
  /** Updates the docking context used by this button  */
  public void setDockingContext(DockingContext ctx) {
    this.ctx = ctx;
  }
  
}
