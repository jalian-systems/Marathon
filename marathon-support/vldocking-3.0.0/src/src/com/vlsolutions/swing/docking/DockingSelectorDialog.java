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

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import javax.swing.table.*;
import java.util.ArrayList;

/** A Helper class providing information about the visibility of user components.
 * <p>
 * This dialog shows which user components are docked, auto-hidden,
 * not visible, and is capable of dynamicaly altering the dockingpanel layout
 * on user selection (for example, to show an unvisible component).
 *
 * <p>
 * User components shown are those which are already docked and those
 * which have been registered with the registerDockableComponent() method of DockingPanel.
 *
 * <p>
 * Example of usage :
 * <pre>
 * Frame f = ...  // frame containing the docking panel
 * DockingPanel dockingPanel = ...
 * DockingSelectorDialog dlg = new DockingSelectorDialog(f);
 * dlg.setDockingPanel(dockingPanel);
 * dlg.pack();
 * dlg.setLocationRelativeTo(f);
 * dlg.setVisible(true); // modal
 *
 * // optional
 * if (dlg.getClosingState() == DockingSelectorDialog.CONFIRM){
 *    // do something more
 * }
 * </pre>
 *
 * @see DockingPanel
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */
public class DockingSelectorDialog extends JDialog {
  /** Closing state of the dialog when the user has selected the <b>confirm</b> option
   * @see #getClosingState() */
  public static final int CONFIRM = 0;

  /** Closing state of the dialog when the user has selected the <b>cancel</b> option
   * @see #getClosingState() */
  public static final int CANCEL = 1;

  private DockablesTableModel model;

  private DockingDesktop desktop;
  private static final String TITLE = "Docking Configuration";
  private JButton confirmButton = new JButton("Ok");
  private JButton cancelButton = new JButton("Cancel");
  private JTable table = new JTable();
  JLabel wizardLabel = new JLabel() {
    public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      Paint p = g2.getPaint();
      g2.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), 0,
          getBackground()));
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setPaint(p);
      super.paintComponent(g);
    }
  };
  private String wizardLabelText =
      "<HTML><BODY><P>This window helps you to manage the "
      + " views composing your application workspace.</P>"
      + "<P><P>By clicking on the table checkboxes below, you can select if "
      + "a view will be diplayed or not </P>"

      + " <P><P> Click on the <b>Ok</b> button to apply your changes or on "
      + "the <b>Cancel</b> button if you do not want to save your changes."
      + " </BODY></HTML>";

  private int closingState = CANCEL;

  private HashMap <DockableState, Boolean>visibleViews = new HashMap(); // DockableState / Boolean (visible?)

  /** Default constructor.
   * <P> modal dialog with DISPOSE_ON_CLOSE option,
   * needs a pack() and setVisible(true) after creation
   * */
  public DockingSelectorDialog() {
    setModal(true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setTitle(TITLE);
    init();
  }

    /** Constructor for Dialog parent window.
     * <P> modal dialog with DISPOSE_ON_CLOSE option,
     * needs a pack() and setVisible(true) after creation.
     */
    public DockingSelectorDialog(Dialog owner) {
      super(owner, true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setTitle(TITLE);
      init();
    }

    /** Constructor for Frame parent window.
     * <P> modal dialog with DISPOSE_ON_CLOSE option,
     * needs a pack() and setVisible(true) after creation.
     * */
    public DockingSelectorDialog(Frame owner) {
      super(owner, true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setTitle(TITLE);
      init();
    }

    private void init(){
      getContentPane().setLayout(new BorderLayout(5,5));
      JScrollPane jsp = new JScrollPane(table);
      jsp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,5,0,5),
          jsp.getBorder()));
      getContentPane().add(jsp, BorderLayout.CENTER);

      confirmButton.setToolTipText("Close this window and update the views");
      cancelButton.setToolTipText("Close this window without updating the views");

      confirmButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          closingState = CONFIRM;
          for (int i = 0; i < model.states.length; i++) {
            DockableState state = model.states[i];
            boolean newVisible = ((Boolean)visibleViews.get(state)).booleanValue();
            boolean oldVisible = state.getLocation() != DockableState.Location.CLOSED;
            if ( oldVisible != newVisible){
              if (newVisible){
                desktop.addDockable(state.getDockable(), state.getPosition());
              } else {
                desktop.close(state.getDockable());
              }
            }
          }
           DockingSelectorDialog.this.dispose();
        }
      });

      Action closeAction = new AbstractAction("Cancel"){
        public void actionPerformed(ActionEvent actionEvent) {
           closingState = CANCEL;
           DockingSelectorDialog.this.dispose();
        }
      };

      cancelButton.setAction(closeAction);


      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      JPanel grid = new JPanel(new GridLayout(1,0, 5, 0)); // buttons of same size
      grid.add(confirmButton);
      grid.add(cancelButton);
      buttons.add(grid);

      getContentPane().add(buttons, BorderLayout.SOUTH);

      wizardLabel.setText(wizardLabelText);
      wizardLabel.setBorder(
          BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0,0,1,0, Color.DARK_GRAY),
      BorderFactory.createEmptyBorder(5,5,5,5)));

      getContentPane().add(wizardLabel, BorderLayout.NORTH);

      // ESCAPE == CLOSE
      KeyStroke ESC = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
          ESC, "CLOSE");
      getRootPane().getActionMap().put("CLOSE", closeAction);
      table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
         ESC, "CLOSE" );
      table.getActionMap().put("CLOSE", closeAction );

      addWindowListener(new WindowAdapter(){
        public void windowOpened(WindowEvent e){
          getRootPane().setDefaultButton(confirmButton);
        }
      });

    }


    /** Change the confirm button text (hook for i18n)*/
    public void setConfirmButtonText(String text){
      confirmButton.setText(text);
    }
    /** Change the cancel button text (hook for i18n)*/
    public void setCancelButtonText(String text){
      cancelButton.setText(text);
    }

    /** Change the confirm button tooltip text (hook for i18n)*/
    public void setConfirmButtonTooltipText(String text){
      confirmButton.setToolTipText(text);
    }

    /** Change the cancel button tootip text (hook for i18n)*/
    public void setCancelButtonTooltipText(String text){
      cancelButton.setToolTipText(text);
    }

    /** Change the wizard label text (an explanation of the behaviour of this dialog).
     * <P>HTML format is supported like in any JLabel.
     * */
    public void setWizardLabelText(String wizardLabelText) {
      this.wizardLabel.setText(wizardLabelText);
    }

    /** Requiered to initialize this dialog.
     * <P> Invoke this method before packing the dialog.
     *
     * */
    public void setDockingDesktop(DockingDesktop desktop){
      this.desktop = desktop;

      DockableState [] states = desktop.getDockables();
      Arrays.sort(states);
      this.model = new DockablesTableModel(states);
      table.setModel(model);
      table.getColumnModel().getColumn(0).setPreferredWidth(25);
      table.getColumnModel().getColumn(1).setPreferredWidth(200);
      table.getColumnModel().getColumn(2).setPreferredWidth(70);
      table.setPreferredScrollableViewportSize(new Dimension(300,300));
      table.setCellSelectionEnabled(true);
      table.setShowVerticalLines(false);
      table.setRowHeight(20);
    }

    /** Returns the users close choice as CONFIRM or CANCEL (remember this dialog is modal).
     * <P> Use this method if you need to know how is was closed.
     * If the used has confirmed, the docking panel is automaticaly updated
     * by the confirm button action.
     */
    public int getClosingState(){
       return closingState;
    }



    class DockablesTableModel extends AbstractTableModel{
          String[] colNames = {" ", "Name", "Visible"};


          private DockableState [] states;

          DockablesTableModel(DockableState [] states) {
            this.states = states;
            for (int i = 0; i < states.length; i++) {
                visibleViews.put(states[i],
                    states[i].getLocation() == DockableState.Location.CLOSED ?
                    Boolean.FALSE : Boolean.TRUE);
            }
          }

          public int getColumnCount() {
            return colNames.length;
          }

          public int getRowCount() {
            return states.length;
          }
          public String getColumnName(int col){
            return colNames[col];
          }


          public boolean isCellEditable(int row, int col){
            if (col != 2) return false;
            Dockable dockable = states[row].getDockable();
            if (dockable.getDockKey().isCloseEnabled()) {
              return true;
            } else {
              return false;
            }
          }

          public Class getColumnClass(int col) {
            switch (col) {
              case 0:
                return Icon.class;
              case 1:
                return String.class;
              case 2:
                return Boolean.class;
            }
            return null;
          }

          public Object getValueAt(int row, int col) {
            DockableState state = states[row];
            switch (col) {
              case 0:
                return state.getDockable().getDockKey().getIcon();
              case 1:
                return state.getDockable().getDockKey().getName();
              case 2:
                return (Boolean) visibleViews.get(state);
            }
            return null;
          }

          public void setValueAt(Object v, int row, int col){
            DockableState state = states[row];
            visibleViews.put(state, (Boolean)v);

          }

        }

}
