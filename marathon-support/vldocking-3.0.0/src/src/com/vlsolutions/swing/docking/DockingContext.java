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

import com.vlsolutions.swing.docking.DockableState.Location;
import com.vlsolutions.swing.docking.event.DockableSelectionEvent;
import com.vlsolutions.swing.docking.event.DockableSelectionListener;
import com.vlsolutions.swing.docking.event.DockableStateChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateChangeListener;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeListener;
import com.vlsolutions.swing.docking.event.DockingActionEvent;
import com.vlsolutions.swing.docking.event.DockingActionListener;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.WeakHashMap;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** A context that can be shared by multiple docking desktops.
 *<p>
 * Keeps track of registered dockables and manages import export.
 *<p>
 * A DockingContext can be seen as the top-most grouping element of docking
 * (as DockingDesktop can be contained is a context), and as such, all
 * methods related to workspace management have been promoted to this class.
 *
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1
 * @update 2007/01/08 Lilian Chamontin : updated registerDockable to 
 * put the dockable in the CLOSED state if not previously registered (was null before v2.1.3)
 */
public class DockingContext {
  /** All dockables must be registered (even if not shown) in order to be processed
   * by the XML Parser or to be displayed in the DockingSelectorDialog
   */
  protected ArrayList <Dockable> registeredDockables = new ArrayList();
  
  /** inner state of the dockables */
  protected HashMap <Dockable, DockableState> dockableStates = new HashMap(); // key : Dockable / value : <DockableState>
  
  /* DockableState change listeners */
  // before state change :
  private ArrayList <DockableStateWillChangeListener> dockableStateWillChangeListeners = new ArrayList();
  // after state change :
  private ArrayList <DockableStateChangeListener> dockableStateChangeListeners = new ArrayList();
  
  /** selection change */
  private ArrayList <DockableSelectionListener> dockableSelectionListeners = new ArrayList();
  
  /** Docking action listener (preferred way to track docking changes) */
  private ArrayList <DockingActionListener> dockingActionListeners = new ArrayList();
  
  
  private ArrayList <DockingDesktop> desktops = new ArrayList();
  
  /** An always ordered list of window (used to keep track of which one is above which one, to resolve some
   * DnD issues)
   */
  private LinkedList <Window> ownedWindowActivationOrder = new LinkedList();
  
  /** Finds new Dockables given a key name (useful when processing an XML stream if the dockable
   * hasn't been registered yet.
   */
  private DockableResolver dockableResolver = null;
  
  /** Constructs a new DockingContext  */
  public DockingContext() {
  }
  
  /** Adds a desktop to this context  */
  public void addDesktop(DockingDesktop desktop){
    if (! desktops.contains((desktop))){
      desktops.add(desktop);
    }
  }
  
  /** Removes a desktop from this context */
  public void removeDesktop(DockingDesktop desktop){
    desktops.remove(desktop);
    // time to check if some windows should be removed as not ancestors of remaining desktops
    Iterator it = ownedWindowActivationOrder.iterator();
    while (it.hasNext()){
      Window w = (Window) it.next();
      boolean ancestor = false;
      for (int i=0; i < desktops.size(); i++){
        DockingDesktop desk = (DockingDesktop) desktops.get(i);
        if (w.isAncestorOf(desk)){
          ancestor = true;
          break;
        }
      }
      if (! ancestor){
        // this window isn't an ancestor of any managed desktops : we drop it
        it.remove();
      }
    }
  }
  
  
  /** Every dockable must be registered in order to be managed by a DockingDesktop.
   *<p>
   * if this method is invoked and the dockable is already registered, nothing happens.
   * if the dockable is still unknown, it is added to the dockables set and (since 2.1.3)
   * its state is set to CLOSED (prior 2.1.3, the state was null).
   *
   * */
  public void registerDockable(Dockable dockable){
    if (! registeredDockables.contains(dockable)){
      registeredDockables.add(dockable);
    }
    if (getDockableState(dockable) == null){
        // should be always the case here // 2007/01/08
        setDockableState(dockable, new DockableState(null, dockable, DockableState.Location.CLOSED));
    }
  }
  
  /** Unregisters the dockable, which can be garbage collected (no longer used
   * by docking desktops.
   *  */
  public void unregisterDockable(Dockable dockable){
    close(dockable); // in case it was still visible
    registeredDockables.remove(dockable);
    dockableStates.remove(dockable); // 2005/11/09
  }
  
  /** Close this dockable */
  public void close(Dockable dockable){
    DockableState state = dockableStates.get(dockable);
    DockingDesktop desk = state.getDesktop();
    if (desk != null){
      desk.close(dockable);
    }
  }
  
  /** Returns the current state of a dockable (CLOSED, HIDDEN, DOCKED, MAXIMIZED, FLOATING) */
  public DockableState getDockableState(Dockable dockable){
    return  dockableStates.get(dockable);
  }
  
  /** Updates the current state of a dockable */
  public void setDockableState(Dockable dockable, DockableState state){
    dockableStates.put(dockable, state);
  }
  
  /** Adds a new DockableStateChangeListener to this desktop.
   * <p>
   * DockableStateChange Events are triggered after the state change.
   *
   * <p> As of version 2.0 of the framework, this method can also be replaced by
   * adding a propertychangeListener on the DockKey object (and react to its DockableState
   * property).
   *
   * */
  public void addDockableStateChangeListener(DockableStateChangeListener listener){
    if (!dockableStateChangeListeners.contains(listener)){
      dockableStateChangeListeners.add(listener);
    }
  }
  
  /** Removes a DockableStateChangeListener from this desktop.
   * */
  public void removeDockableStateChangeListener(DockableStateChangeListener listener){
    dockableStateChangeListeners.remove(listener);
  }
  
  /** Adds a new DockableStateWillChangeListener to this desktop.
   * <p>
   * DockableStateWillChange Events are triggered <b>before</b> the state change, and
   * are vetoable.
   * */
  
  public void addDockableStateWillChangeListener(DockableStateWillChangeListener listener){
    if (!dockableStateWillChangeListeners.contains(listener)){
      dockableStateWillChangeListeners.add(listener);
    }
  }
  
  /** Removes a DockableStateWillChangeListener from this desktop.
   * */
  public void removeDockableStateWillChangeListener(DockableStateWillChangeListener listener){
    dockableStateWillChangeListeners.remove(listener);
  }
  
  /** Creates and returns an array of all registered dockable with their current
   * state.
   * <p>
   * Visibility states are [DockableState.CLOSED, DOCKED, HIDDEN]
   * @return an array of DockableState
   */
  public DockableState[] getDockables(){
    DockableState [] states = new DockableState[registeredDockables.size()];
    for (int i = 0; i < states.length; i++) {
      Dockable d = registeredDockables.get(i);
      states[i] = dockableStates.get(d);
      if (states[i] == null){
        DockableState closed = new DockableState(null, d, DockableState.Location.CLOSED);
        states[i] = closed;
      }
    }
    return states;
  }
  
  /** Saves the current desktop configuration into an XML stream.
   * <p>
   * The workspace is composed of every desktop layouts associated
   * with this context (desktops are identified by their 'desktopName' property).
   *
   * <p>
   * The stream is not closed at the end of the operation.
   *
   * @see #readXML(InputStream)
   * */
  public void writeXML(OutputStream stream) throws IOException {
    PrintWriter out = new PrintWriter(stream);
    out.println("<?xml version=\"1.0\"?>");
    out.println("<VLDocking version=\"2.1\">");
    for (int i=0; i < desktops.size(); i++){
      DockingDesktop desktop = (DockingDesktop) desktops.get(i);
      desktop.writeDesktopNode(out);
    }
    out.println("</VLDocking>");
    
    out.flush();
  }
  
  
  
  /** Reads an XML encoded stream as the new desktop configuration.
   * <p>
   * When the method returns, the desktops associated to this context are totally
   * reconfigured with posiibly different dockables at different positions.
   * <p>
   * <b>Note : </b> The <code>DockKey</code>s of the stream must be registered with
   * the {@link #registerDockable(Dockable) registerDockable} method,
   * prior readXML.<br>
   *
   * This is the case if the desktop is already open and dockables
   * laid out, but might not be the case if this method is used at application startup
   * to populate an empty desktop.
   * <p>
   * Note : altenatively (since 2.1.2) if a DockableResolver has been set, it will
   * be use to auto-register the new dockables encountered along the stream. <br>
   * <p>
   * Dismisses all visible dockables (docked and auto-hidden), and clear their DockableState.
   * <p>
   * The stream is not closed at the end of the operation.
   * <p>
   * Note : When multiple desktops are loaded from the stream, the context uses their "desktopName"
   * property (desk.getDesktopName()) to identify them.
   *
   * @see #writeXML(OutputStream)
   * @see #registerDockable(Dockable)
   * @see #setDockableResolver(DockableResolver)
   *  */
  public void readXML(InputStream in)
  throws ParserConfigurationException, IOException, SAXException {
    // remove all dockable states
    
    for (int i=0; i < desktops.size(); i++){
      DockingDesktop desk = (DockingDesktop) desktops.get(i);
      desk.clear();
    }
    
    dockableStates.clear();
    
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(in);
    Element root = doc.getDocumentElement();
    if (root.getNodeName().equals("DockingDesktop")){
      // this is an old desktop file, missing the VLDocking root node (VLDocking 2.0 format)
      // so we noly have a single desktop
      DockingDesktop desk = getDesktopByName(null); // only one
      desk.readDesktopNode(root);
    } else {
      NodeList desktopChildren = root.getChildNodes();
      for (int i = 0, len = desktopChildren.getLength(); i < len; i++) {
        Node node = desktopChildren.item(i);
        if (node instanceof Element){
          Element deskChild = (Element) node;
          DockingDesktop desk = getDesktopByName(deskChild.getAttribute("name"));
          desk.readDesktopNode(deskChild);
        }
      }
    }
    
  }

  
  private DockingDesktop getDesktopByName(String name){
    if (desktops.size() == 1){
      // always found if only one desktop
      DockingDesktop desktop = desktops.get(0);
      return desktop;
    }
    for (int i=0; i < desktops.size(); i++){
      DockingDesktop desktop = desktops.get(i);
      if (desktop.getDesktopName().equals(name)){
        return desktop;
      }
    }
    
    throw new RuntimeException("No desktop found with name : " + name);
    
  }

  /** constructs and returns the list of dockables corresponding to a desktop, at a specific state*/
  public ArrayList<Dockable> getDockablesByState(DockingDesktop desktop, DockableState.Location state) {
    ArrayList<Dockable> list = new ArrayList();
    for (int i=0; i < registeredDockables.size(); i++){
      Dockable d = registeredDockables.get(i);
      DockableState dState = dockableStates.get(d);
      if (dState != null && dState.getDesktop() == desktop && dState.getLocation() == state){
        list.add(d);
      }
    }
    return list;
  }
  
  /** Returns the (registered) dockable corresponding to the given key id,
   * or null if not found
   *<p>
   * Note : since VLDocking 2.1.2, this method also tries to use its DockableResolver
   * if null would have been returned.
   * @see DockableResolver
   */
  public Dockable getDockableByKey(String dockKey){
    for (int i = 0; i < registeredDockables.size(); i++) {
      Dockable d = registeredDockables.get(i);
      if (d.getDockKey().getKey().equals(dockKey)){
        return d;
      }
    }
    // not found
    if (dockableResolver != null){
      Dockable d = dockableResolver.resolveDockable(dockKey);
      if (d != null){
        registerDockable(d); // a new one
      }
      return d; // might be null
    }
    return null;
  }
  
  /*package protected */ void fireDockableStateChange(DockableStateChangeEvent e){
    
    for (int i = 0; i < dockableStateChangeListeners.size(); i++) {
      DockableStateChangeListener listener = dockableStateChangeListeners.get(i);
      listener.dockableStateChanged(e);
    }
  }
  
  /*package protected */ boolean fireDockableStateWillChange(DockableStateWillChangeEvent e){
    
    DockingDesktop desk = e.getFutureState().getDesktop();
    
    
    if (desk.getMaximizedDockable() != null){
      // veto events coming from autohide and floating components if a component is maximized
      // @todo : this could be improved by disabling the dock and attach properties of
      // the remaining visible dockables to avoid having the user don't understand
      // why a button doesn't react.
      if (desk.getMaximizedDockable() != e.getCurrentState().getDockable()){
        if (e.getFutureState().isDocked()){
          if (e.getCurrentState().isHidden()){
            return false; // refused
          } else if (e.getCurrentState().isFloating()){
            return false;
          }
        }
      }
    }
    
    // dispatch events and listen to vetoes
    
    for (int i = 0; i < dockableStateWillChangeListeners.size(); i++) {
      DockableStateWillChangeListener listener = dockableStateWillChangeListeners.get(i);
      listener.dockableStateWillChange(e);
      if (!e.isAccepted()){ // stop as soon as the operation is cancelled
        return false;
      }
    }
    return true;
  }
  
  /** Adds a new DockableSelectionListener to this desktop.
   * <p>
   * DockableSelection Events are triggered when a dockable takes the focus.
   *
   */
  public void addDockableSelectionListener(DockableSelectionListener listener){
    if (!dockableSelectionListeners.contains(listener)){
      dockableSelectionListeners.add(listener);
    }
  }
  
  /** Removes a DockableSelectionListener from this desktop.
   * */
  public void removeDockableSelectionListener(DockableSelectionListener listener){
    dockableSelectionListeners.remove(listener);
  }
  
  /*package protected */ void fireDockableSelectionChange(DockableSelectionEvent e){
    for (int i = 0; i < dockableSelectionListeners.size(); i++) {
      DockableSelectionListener listener = dockableSelectionListeners.get(i);
      listener.selectionChanged(e);
    }
  }
  
  public void addDockingActionListener(DockingActionListener listener) {
    if (!dockingActionListeners.contains(listener)){
      dockingActionListeners.add(listener);
    }
    
  }
  
  public void removeDockingActionListener(DockingActionListener listener) {
    dockingActionListeners.remove(listener);
  }
  
  
  /** returns false if the docking action is rejected, or true if accepted by all listeners*/
  boolean fireAcceptDockingAction(DockingActionEvent e){
    /*package protected */
    for (int i = 0; i < dockingActionListeners.size(); i++) {
      DockingActionListener listener = dockingActionListeners.get(i);
      if (!listener.acceptDockingAction(e)){
        return false;
      }
    }
    return true;
  }
  
  /*package protected */ void fireDockingActionPerformed(DockingActionEvent e){
    for (int i = 0; i < dockingActionListeners.size(); i++) {
      DockingActionListener listener = dockingActionListeners.get(i);
      listener.dockingActionPerformed(e);
    }
  }
  
  /** Returns a list of the desktops sharing this context */
  public ArrayList getDesktopList() {
    return desktops;
  }
  
  /** used to track window activation (to have an up to date z ordered list of window) */
  void windowActivated(WindowEvent e){
    /* package protected */
    /* these events are forwarded by docking desktops  */
    Window w = e.getWindow();
    if (ownedWindowActivationOrder.size()>0 && ownedWindowActivationOrder.getFirst() != w){
      ownedWindowActivationOrder.remove(w);
      ownedWindowActivationOrder.addFirst(w);
    }
  }
  
  /** Returns a list of owned windows, ordered by window z-order */
  LinkedList getOwnedWindowActionOrder(){
    /* package protected */
    return ownedWindowActivationOrder;
  }
  
  /** Registers a new window */
  void registerWindow(Window w) {
    /* package protected */
    ownedWindowActivationOrder.addLast(w);
  }
  
  /** Returns the dockable resolver used by this context (or null if none defined)
   * @since 2.1.2
   */
  public DockableResolver getDockableResolver() {
    return dockableResolver;
  }
  
  /** Updates the dockable resolver used by this context (can be set to null)
   * @since 2.1.2
   */
  public void setDockableResolver(DockableResolver dockableResolver) {
    this.dockableResolver = dockableResolver;
  }
  
}