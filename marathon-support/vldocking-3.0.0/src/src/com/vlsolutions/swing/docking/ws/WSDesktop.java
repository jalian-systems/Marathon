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


package com.vlsolutions.swing.docking.ws;

import com.vlsolutions.swing.docking.AutoHidePolicy;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.RelativeDockablePosition;
import com.vlsolutions.swing.docking.SplitContainer;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JSplitPane;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/** A desktop part of a workspace.
 *<p>
 * Use this object to define the layout of a DockingDesktop. This layout can be applied later
 * to that desktop through the Workspace.apply(DockingContext ctx) method.
 *<p>
 * The API is a subset of the one of DockingDesktop  : limited to layout building.
 * you can addDockable(), split(), createTab(), addHiddenDockable(), addFloatingDockable(), see these
 * method description for further information.
 *<p>
 * Node : WSDesktop doens't support layout updates : you should use it to build a layout, but not to
 * move dockables around. (if you add a dockable somewhere, don't add it elsewhere later as removal isn't
 * fully implemented).
 *<p>
 * In a WSDesktop, dockables are identified by a WSDockKey (a limited version of DockKey) : this is to allow
 * you to create workspaces with dockables that haven't been constructed yet (lazy loading).
 * <p>
 *Example :
 *<pre>
 *  Workspace w = new Workspace();
 *  WSDesktop desk = w.getDesktop(0); // gets the default (single) desktop
 * // define some dockable keys
 *  WSDockKey editorKey = new WSDockKey("editor");
 *  WSDockKey treeKey, tableKey, imgToolKey // other dockable keys
 *
 * // now define a workspace layout
 *  desk.addDockable(editorKey); // initial dockable
 *  desk.split(editorKey, treeKey, DockingConstants.SPLIT_LEFT, 0.5);  // splitted
 *  desk.split(treeKey, tableKey, DockingConstants.SPLIT_TOP, 0.7);  // tree is also splitted
 *
 *  desk.createTab(tableKey, imgToolsKey, 0);  // tableKey is transformed into a tabbed container
 * // and that's it
 * // ...
 * // later
 * //
 * DockingContext ctx = ... // your real desktop
 * w.apply(ctx); // applies the workspace to this desktop
 * // now the dektops are loaded as specified in the 'w' workspace
 *</pre>
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1.2
 */
public class WSDesktop {
  /** The name of this desktop (which must match the name of a real desktop if more than one desktops are used )
   */
  private String desktopName;
  
  /** root node for desktop (<=> DockingPanel) */
  private TopLevelNode mainNode = new TopLevelNode();
  
  /** Map of Nodes (key : WSDockKey, value : Node)*/
  private HashMap <WSDockKey, Node> nodesByKey = new HashMap();
  
  /** 4 borders of this desktop */
  private ArrayList [] borders = new ArrayList[4];
  
  /** <=> floating windows  */
  private ArrayList floatingNodes = new ArrayList();
  
  private WSDockKey maximizedDockable = null;

  /** groups of tabs (used to re-tab autohidden dockable)*/
  protected HashMap <WSDockKey, LinkedList<WSDockKey>> tabbedGroups = new HashMap(); // <Dockable>/<LinkedList<Dockable>
  
  /** Constructs a WSDesktop with "default" as name */
  public WSDesktop() {
    this("default");
  }
  
  /** Constructs a WSDesktop with a given name */
  public WSDesktop(String desktopName){
    this.desktopName = desktopName;
    for (int i=0; i < borders.length; i++){
      borders[i] = new ArrayList();
    }
  }
  
  /** removes all information stored into this desktop, which becomes ready to
   * be reused.
   */
  public void clear(){
    mainNode = new TopLevelNode();
    nodesByKey.clear();
    for (int i=0; i < borders.length; i++){
      borders[i].clear();
    }
    floatingNodes.clear();
    maximizedDockable = null;
  }
  
  /** Adds the first dockable to this desktop. This method will fail if called more than once */
  public void addDockable(WSDockKey key){
    if (mainNode.child != null){
      throw new IllegalArgumentException("this workspace isn't empty");
    }
    mainNode.setChild(new SingleDockableNode(key));
  }
  
  /** Sets the maximized dockable of this desktop, (or null if no dockable maximized).
   *<p>
   * Don't forget that this dockable MUST ALSO BE in the DOCKED state (added to the desktop
   * in a split, tab or compound node) otherwise the missing restore information will cause
   * unwanted behaviour, or exceptions.
   */
  public void setMaximizedDockable(WSDockKey max){
    this.maximizedDockable = max;
  }
  
  
  private SingleDockableNode getNode(WSDockKey key){
    return (SingleDockableNode) nodesByKey.get(key);
  }
  
  /** Splits a dockable (with a given split location)
   *
   * @param base          the dockable to be splitted
   * @param newDockable   the new dockable
   * @param split         the orientation of split
   * @param splitLocation where the split divisor is (always relative from the top/left dockable, so
   *  split(a, b, SPLIT_TOP, 0.1f) and split(a, b, SPLIT_BOTTOM, 0.1f) have the same dividor location (somewhere near
   *  the top of the split).
   */
  public void split(WSDockKey base, WSDockKey newDockable, DockingConstants.Split split, double splitLocation){
    Node baseNode = getNode(base);
    if (baseNode == null){
      throw new IllegalArgumentException("base dockable not found " + base);
    }
    
    SplitNode splitNode = new SplitNode();
    replaceChild(baseNode.parent, baseNode, splitNode);
    
    switch(split.value()){
      case DockingConstants.INT_SPLIT_TOP:
        splitNode.setTop(new SingleDockableNode(newDockable));
        splitNode.setBottom(baseNode);
        splitNode.isHorizontal = false;
        break;
      case DockingConstants.INT_SPLIT_LEFT:
        splitNode.setLeft(new SingleDockableNode(newDockable));
        splitNode.setRight(baseNode);
        splitNode.isHorizontal = true;
        break;
      case DockingConstants.INT_SPLIT_BOTTOM:
        splitNode.setBottom(new SingleDockableNode(newDockable));
        splitNode.setTop(baseNode);
        splitNode.isHorizontal = false;
        break;
      case DockingConstants.INT_SPLIT_RIGHT:
        splitNode.setRight(new SingleDockableNode(newDockable));
        splitNode.setLeft(baseNode);
        splitNode.isHorizontal = true;
        break;
    }
    splitNode.location = splitLocation;
  }
  
  /** Creates a tab containing baseTab and newTab (if baseTab is already into a Tab, then
   * newTab will just be added at the "order" position.
   */
  public void createTab(WSDockKey baseTab, WSDockKey newTab, int order){
    SingleDockableNode baseTabNode = getNode(baseTab);
    if (baseTabNode == null){
      throw new IllegalArgumentException("base dockable not found " + baseTab);
    }
    
    if (baseTabNode.parent instanceof TabNode){ // already in a tab
      TabNode parent = (TabNode) baseTabNode.parent;
      parent.addTab(order, new SingleDockableNode(newTab));
    } else {
      // replace parent by a tab
      TabNode gParent = new TabNode();
      replaceChild(baseTabNode.parent, baseTabNode, gParent);
      gParent.addTab(0, baseTabNode);
      gParent.addTab(order, new SingleDockableNode(newTab));
    }
  }
  
  /** Adds a dockable into a compound dockable */
  public void addDockable(WSDockKey compoundDockable, WSDockKey childDockable){
    SingleDockableNode node = getNode(compoundDockable);
    
    Node parent = node.parent;
    CompoundDockableNode cnode = new CompoundDockableNode(compoundDockable);
    replaceChild(parent, node, cnode);
    cnode.setNestedNode(new SingleDockableNode(childDockable));
  }
  
  /** Adds a new dockable (and stores its return-to-docked position) */
  public void addHiddenDockable(WSDockKey dockable, RelativeDockablePosition dockedPosition){
    int zone;
    if (dockable.getAutoHideBorder() == null) {
      zone = AutoHidePolicy.getPolicy().getDefaultHideBorder().value();
    } else {
      zone = dockable.getAutoHideBorder().value();
    }
    
    ArrayList border = borders[zone];
    border.add(new HiddenNode(new SingleDockableNode(dockable), dockedPosition));
    
  }
  
  /** Adds a new floating dockable
   *
   * @param dockable  the dockable to add as floating
   * @param windowRect rectangle defining the window (relative to screen) of the floating dockable
   * @param returnPosition  where to put the dockable when returning to the desktop
   *
   */
  public void setFloating(WSDockKey dockable, Rectangle windowRect, RelativeDockablePosition returnPosition){
    FloatingNode f = new FloatingNode(new SingleDockableNode(dockable), windowRect, returnPosition);
    floatingNodes.add(f);
  }
  
  
  /** Returns the name of this desktop (which must match the name of a real desktop if more than one desktops are used ) */
  public String getDesktopName() {
    return desktopName;
  }
  
  /** Updates the name of this workspace desktop */
  public void setDesktopName(String name) {
    this.desktopName = name;
  }
  
  /** Package method : used to encode this desktop into an XML stream */
  void writeDesktopNode(PrintWriter out) {
    out.println("<DockingDesktop name=\""+ desktopName + "\">");
    out.println("<DockingPanel>");
    if (mainNode.child != null){
      xmlWriteComponent(mainNode.child, out);
    }
    if (maximizedDockable != null){
      out.println("<MaximizedDockable>");
      out.println("<Key dockName=\"" + maximizedDockable.getKey()+ "\"/>");
      out.println("</MaximizedDockable>");
    }
    out.println("</DockingPanel>");
    
    for (int i = 0; i < borders.length; i++) {
      xmlWriteBorder(i, borders[i], out);
    }
    
    // finish with the floating dockables
    for (int i=0; i < floatingNodes.size(); i++){
      FloatingNode f = (FloatingNode) floatingNodes.get(i);
      xmlWriteFloating(f, out);
    }
    
    
    // and the tab groups
    xmlWriteTabGroups(out);
     
    
    out.println("</DockingDesktop>");
  }
  
  private void xmlWriteComponent(Node node, PrintWriter out) {
    if (node instanceof SplitNode){
      xmlWriteSplit((SplitNode) node, out);
    } else if (node instanceof TabNode){
      xmlWriteTab((TabNode)node, out);
    } else if (node instanceof SingleDockableNode){
      xmlWriteDockable((SingleDockableNode)node, out);
    }
    
  }
  
  private void xmlWriteSplit(SplitNode splitNode, PrintWriter out) {
    double location = splitNode.location;
    int orientation = splitNode.isHorizontal ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;
    
    out.println("<Split orientation=\""+ orientation  + "\" location=\"" + location + "\">");
    xmlWriteComponent(splitNode.getLeft(), out);
    xmlWriteComponent(splitNode.getRight(), out);
    out.println("</Split>");
    
  }
  
  private void xmlWriteTab(TabNode tabNode, PrintWriter out) {
    out.println("<TabbedDockable>");
    for (int i = 0; i < tabNode.tabs.size(); i++) {
      xmlWriteDockable(tabNode.getTab(i), out);
    }
    out.println("</TabbedDockable>");
  }
  
  private void xmlWriteDockable(SingleDockableNode dockable, PrintWriter out) {
    boolean isCompound = dockable instanceof CompoundDockableNode;
    if (isCompound){
      out.println("<Dockable compound=\"true\">");
    } else {
      out.println("<Dockable>");
    }
    WSDockKey key = dockable.key;
    out.println("<Key dockName=\"" + key.getKey()+ "\"/>");
    
    if (isCompound){
      CompoundDockableNode d = (CompoundDockableNode)dockable;
      
      if (d.nestedNode != null){
        // compound panels cannont have more than one child (split/tab/single)
        xmlWriteComponent(d.nestedNode, out);
      }
    }
    
    out.println("</Dockable>");
  }
  
  private void xmlWriteBorder(int zone, ArrayList border, PrintWriter out) {
    if (border.size() > 0){
      out.println("<Border zone=\"" + zone + "\">");
      for (int i=0; i < border.size(); i++){
        HiddenNode node = (HiddenNode) border.get(i);
        xmlWriteBorderDockable(node, out);
        
      }
      out.println("</Border>");
    }
  }
  
  private void xmlWriteBorderDockable(HiddenNode node, PrintWriter out) {
    RelativeDockablePosition position = node.position;
    SingleDockableNode child = node.node;
    boolean isCompound = child instanceof CompoundDockableNode;
    if (isCompound){
      out.println("<Dockable compound=\"true\">");
    } else {
      out.println("<Dockable>");
    }
    WSDockKey key = child.key;
    out.println("<Key dockName=\"" + key.getKey() + "\"/>");
    out.println("<RelativePosition x=\"" + position.getX()
    + "\" y=\"" + position.getY()
    + "\" w=\"" + position.getWidth()
    + "\" h=\"" + position.getHeight()
    + "\" />");
    if (isCompound){
      CompoundDockableNode d = (CompoundDockableNode)child;
      if (d.nestedNode != null){
        // compound panels cannont have more than one child (split/tab/single)
        xmlWriteComponent(d.nestedNode, out);
      }
    }
    out.println("</Dockable>");
  }
  
  private void replaceChild(Node parent, Node child, Node newChild) {
    if (parent instanceof TopLevelNode){
      TopLevelNode t = ((TopLevelNode)parent);
      if (t.child == child){
        t.setChild(newChild);
      } else {
        throw new IllegalArgumentException("child not found in top level node " + child);
      }
    } else if (parent instanceof CompoundDockableNode){
      CompoundDockableNode c = ((CompoundDockableNode)parent);
      if (c.nestedNode == child){
        c.setNestedNode(newChild);
      } else {
        throw new IllegalArgumentException("child not found in compound " + child);
      }
    } else if (parent instanceof SplitNode){
      SplitNode split = (SplitNode) parent;
      if (split.left == child){
        split.setLeft(newChild);
      } else if (split.right == child){
        split.setRight(newChild);
      } else {
        throw new IllegalArgumentException("child not found in split " + child);
      }
    } else if (parent instanceof TabNode){
      TabNode tab = (TabNode) parent;
      for (int i=0; i < tab.tabs.size(); i++){
        Node n = tab.getTab(i);
        if (n == child){
          tab.replaceTab(i, (SingleDockableNode) newChild);
          break;
        }
      }
      throw new IllegalArgumentException("child not found in tabs " + child);
    } else if (parent instanceof HiddenNode){
      HiddenNode h = (HiddenNode) parent;
      if (h.node == child){
        h.setChild((SingleDockableNode) newChild);
      } else {
        throw new IllegalArgumentException("child not found in hidden dockable " + child);
      }
    } else if (parent instanceof FloatingNode){
      FloatingNode f = (FloatingNode) parent;
      if (f.child == child){
        f.setChild(newChild);
      } else {
        throw new IllegalArgumentException("child not found in floating dockable " + child);
      }
    } else {
      throw new IllegalArgumentException("wrong type for parent " + parent);
    }
  }
  
  private void xmlWriteFloating(FloatingNode node, PrintWriter out) {
    Rectangle r = node.windowRect;
    out.println("<Floating x=\"" + (r.x) + "\" y=\""
        + (r.y) + "\" width=\""
        + r.width + "\" height=\"" + r.height + "\">");
    if (node.child instanceof TabNode){
      TabNode tab = (TabNode) node.child;
      for (int i=0; i < tab.tabs.size(); i++){
        SingleDockableNode n = tab.getTab(i);
        xmlWriteFloatingDockable(n, out, node.returnPosition);
      }
    } else { // single
      xmlWriteFloatingDockable((SingleDockableNode)node.child, out, node.returnPosition);
    }
    out.println("</Floating>");
    
  }
  
  private void xmlWriteFloatingDockable(SingleDockableNode dockable, PrintWriter out, RelativeDockablePosition returnPosition) {
    
    boolean isCompound = dockable instanceof CompoundDockableNode;
    if (isCompound){
      out.println("<Dockable compound=\"true\">");
    } else {
      out.println("<Dockable>");
    }
    WSDockKey key = dockable.key;
    out.println("<Key dockName=\"" + key.getKey() + "\"/>");
    out.println("<RelativePosition x=\"" + returnPosition.getX()
    + "\" y=\"" + returnPosition.getY()
    + "\" w=\"" + returnPosition.getWidth()
    + "\" h=\"" + returnPosition.getHeight()
    + "\" />");
    out.println("<PreviousState state=\"" + DockableState.Location.DOCKED.ordinal() + "\"/>"); // @todo : see how to implement that
    
    if (isCompound){
      CompoundDockableNode d = (CompoundDockableNode)dockable;
      if (d.nestedNode != null){
        // compound panels cannont have more than one child (split/tab/single)
        xmlWriteComponent(d.nestedNode, out);
      }
    }
    out.println("</Dockable>");
  }

  private void xmlWriteTabGroups(PrintWriter out) {
    ArrayList uniqueGroups = new ArrayList();
    ArrayList processedDockables = new ArrayList();
    Iterator <WSDockKey> it = tabbedGroups.keySet().iterator();
    while (it.hasNext()){
      WSDockKey d = it.next();
      if (! processedDockables.contains(d)){
        processedDockables.add(d);
        LinkedList<WSDockKey> tabList = tabbedGroups.get(d);
        Iterator <WSDockKey> listIt = tabList.iterator();
        while (listIt.hasNext()){
          WSDockKey d2 =  listIt.next();
          if (!processedDockables.contains(d2)){
            processedDockables.add(d2);
          }
        }
        uniqueGroups.add(tabList);
      }
    }
    
    out.println("<TabGroups>");
    for (int i = 0; i < uniqueGroups.size(); i++) {
      out.println("<TabGroup>");
      LinkedList group = (LinkedList) uniqueGroups.get(i);
      Iterator listIt = group.iterator();
      while (listIt.hasNext()) {
        WSDockKey d = (WSDockKey) listIt.next();
        xmlWriteDockableTab(d, out);
      }
      out.println("</TabGroup>");
    }
    out.println("</TabGroups>");
  }

  private void xmlWriteDockableTab(WSDockKey key, PrintWriter out) {
    out.println("<Dockable>");
    out.println("<Key dockName=\"" + key.getKey() + "\"/>");
    out.println("</Dockable>");
  }

 
  void readDesktopNode(Element root) throws SAXNotRecognizedException, SAXException {
    NodeList children = root.getChildNodes();
    for (int i = 0, len = children.getLength(); i < len; i++) {
      org.w3c.dom.Node child = children.item(i);
      xmlBuildRootNode(child);
    }
  }
  
  private void xmlBuildRootNode(org.w3c.dom.Node node) throws SAXNotRecognizedException, SAXException {
    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
      Element elt = (Element) node;
      String name = elt.getNodeName();
      if (name.equals("DockingPanel")){
        // only one child at most
        NodeList children = elt.getChildNodes();
        for (int i = 0, len = children.getLength(); i < len; i++) {
          xmlBuildDockingPanelNode(elt.getChildNodes().item(i));
        }
      } else if (name.equals("Border")){
        int zone = Integer.parseInt(elt.getAttribute("zone"));
        NodeList children = elt.getElementsByTagName("Dockable");
        for (int i = 0, len = children.getLength(); i < len; i++) {
          xmlBuildAutoHideNode(zone, (Element)children.item(i));
        }
      } else if (name.equals("Floating")){
        int x = Integer.parseInt(elt.getAttribute("x"));
        int y = Integer.parseInt(elt.getAttribute("y"));
        int width = Integer.parseInt(elt.getAttribute("width"));
        int height = Integer.parseInt(elt.getAttribute("height"));
        
        NodeList children = elt.getElementsByTagName("Dockable");
        xmlBuildFloatingNode(children, new Rectangle(x, y, width, height)); //2005/10/10
        
/*        for (int i = 0, len = children.getLength(); i < len; i++) {
          xmlBuildFloatingNode((Element)children.item(i), new Rectangle(x, y, width, height));
        }*/
      } else if (name.equals("TabGroups")){
        NodeList children = elt.getElementsByTagName("TabGroup");
        xmlBuildTabGroup(children); //2005/10/10
      } else {
        throw new SAXNotRecognizedException(name);
      }
    }
  }
  
  private void xmlBuildDockingPanelNode(org.w3c.dom.Node node) throws SAXException {
    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
      Node child = xmlCreateComponent((Element) node, DockableState.Location.DOCKED);
      if (node != null){
        mainNode.setChild(child);
      }
    }
  }
  
  private Node xmlCreateComponent(Element elt, DockableState.Location dockableLocation) throws SAXException {
    if (elt.getNodeName().equals("Split")){
      SplitNode split = xmlBuildSplitContainer(elt, dockableLocation);
      return split;
    } else if (elt.getNodeName().equals("Dockable")){
      SingleDockableNode sdc = xmlGetDockable(elt);
      if (sdc instanceof CompoundDockableNode){
        // check for children and build them
        xmlBuildCompoundDockable((CompoundDockableNode) sdc, elt, dockableLocation);
      }
      return sdc;
    } else if (elt.getNodeName().equals("TabbedDockable")){
      TabNode tdc = xmlBuildTabbedDockableContainer(elt, dockableLocation);
      return tdc;
    } else if (elt.getNodeName().equals("MaximizedDockable")){
      // this should be the last element from DockingPanel node
      SingleDockableNode sdc = xmlGetDockable(elt);
      setMaximizedDockable(sdc.key);
      return null;
    } else {
      throw new SAXNotRecognizedException(elt.getNodeName());
    }
  }
  
  private SingleDockableNode xmlGetDockable(Element dockableElt) {
    Element key = (Element)dockableElt.getElementsByTagName("Key").item(0);
    String name = key.getAttribute("dockName");
    WSDockKey wsKey = new WSDockKey(name);
    SingleDockableNode sdn = (SingleDockableNode) nodesByKey.get(wsKey);
    
    if (sdn == null){
      String compound = dockableElt.getAttribute("compound");
      if ("true".equals(compound)){
        sdn = new CompoundDockableNode(wsKey);
      } else {
        sdn = new SingleDockableNode(wsKey);
        
      }
    }
    
    return sdn;
  }
  
  private void xmlBuildCompoundDockable(CompoundDockableNode cdn, Element elt, DockableState.Location dockableLocation) throws SAXException {
    /*  a compound dockable can hold a sub dockable (or split/tabs) */
    NodeList children = elt.getChildNodes();
    for (int i = 0, len = children.getLength(); i < len; i++) {
      org.w3c.dom.Node node = children.item(i);
      if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
        if (elt.getNodeName().equals("Key")){
          // ignore : it's the key of the compound dockable
          
        } else {
          Node comp = xmlCreateComponent(elt, dockableLocation);
          cdn.nestedNode = comp;
          return; // no more than a single child
        }
      }
    }
    
  }
  
  private SplitNode xmlBuildSplitContainer(Element elt, DockableState.Location dockableLocation) throws SAXException {
    int orientation = Integer.parseInt(elt.getAttribute("orientation"));
    String loc = elt.getAttribute("location");
    double location = 0.5;
    if (loc != null && !loc.equals("")){
      location = Double.parseDouble(loc);
    }
    
    SplitNode split = new SplitNode();
    split.isHorizontal = orientation == SplitContainer.HORIZONTAL_SPLIT;
    split.location = location;
    
    boolean left = true;
    for (int i = 0; i < elt.getChildNodes().getLength(); i++) {
      org.w3c.dom.Node child = elt.getChildNodes().item(i);
      if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
        Node comp = xmlCreateComponent((Element)child, dockableLocation);
        if (left){
          split.setLeft(comp);
          left = false;
        } else {
          split.setRight(comp);
        }
      }
    }
    return split;
  }
  
  private TabNode xmlBuildTabbedDockableContainer(Element elt, DockableState.Location dockableLocation) throws SAXException {
    TabNode tdc = new TabNode();
    
    int index = 0;
    for (int i = 0; i < elt.getChildNodes().getLength(); i++) {
      org.w3c.dom.Node child = elt.getChildNodes().item(i);
      if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
        SingleDockableNode sdn = xmlGetDockable((Element) child);
        tdc.addTab(index++, sdn);
        // we don't update the tab group as it is done in a separate pass (xmlBuildTabGroups())
        
        if (sdn instanceof CompoundDockableNode){
          // check for children and build them is needed
          xmlBuildCompoundDockable((CompoundDockableNode) sdn, (Element) child, dockableLocation);
        }
      }
    }
    return tdc;
  }
  
  private void xmlBuildAutoHideNode(int zone, Element dockableElt) throws SAXException {
    Element hideElt = (Element)dockableElt.getElementsByTagName("RelativePosition").item(0);
    float x = Float.parseFloat(hideElt.getAttribute("x"));
    float y = Float.parseFloat(hideElt.getAttribute("y"));
    float w = Float.parseFloat(hideElt.getAttribute("w"));
    float h = Float.parseFloat(hideElt.getAttribute("h"));
    RelativeDockablePosition position = new RelativeDockablePosition(x, y, w, h);
    
    SingleDockableNode dockable = xmlGetDockable(dockableElt);
    borders[zone].add(new HiddenNode(dockable, position));
    
    if (dockable instanceof CompoundDockableNode){
      // check for children and build them
      xmlBuildCompoundDockable((CompoundDockableNode) dockable, dockableElt, DockableState.Location.HIDDEN);
    }
    
  }
  
  private void xmlBuildFloatingNode(NodeList dockables, Rectangle rectangle) throws SAXException {
    // @todo not optimal.. we should refactor it oustide this method

    FloatingNode floating = null;
    SingleDockableNode baseDockable = null; // used when there are tabs
    
    for (int i=0; i < dockables.getLength(); i++){
      Element dockableElt = (Element) dockables.item(i);
      Element hideElt = (Element)dockableElt.getElementsByTagName("RelativePosition").item(0);
      float x = Float.parseFloat(hideElt.getAttribute("x"));
      float y = Float.parseFloat(hideElt.getAttribute("y"));
      float w = Float.parseFloat(hideElt.getAttribute("w"));
      float h = Float.parseFloat(hideElt.getAttribute("h"));
      
      RelativeDockablePosition position = new RelativeDockablePosition(x, y, w, h);
      
      SingleDockableNode dockable = xmlGetDockable(dockableElt);
      if (i == 0){
        baseDockable = dockable;
        floating = new FloatingNode(baseDockable, rectangle, position);
        floatingNodes.add(floating);
      }
      
      Element previousState = (Element)dockableElt.getElementsByTagName("PreviousState").item(0);
      int istate = Integer.parseInt(previousState.getAttribute("state"));
      // @todo : see how to manage this value
      
      if (i > 0){
        createTab(baseDockable.key, dockable.key, i);
      }
      
      if (dockable instanceof CompoundDockableNode){
        // check for children and build them
        xmlBuildCompoundDockable((CompoundDockableNode) dockable, dockableElt, DockableState.Location.FLOATING);
      }
    }
    
    
  }
  
  private void xmlBuildTabGroup(NodeList group) {
    for (int i=0; i < group.getLength(); i++){
      Element groupElt = (Element)group.item(i);
      NodeList dockables = groupElt.getElementsByTagName("Dockable");
      SingleDockableNode base = null;
      for (int j=0; j < dockables.getLength(); j++){
        Element dockableElt = (Element)dockables.item(j);
        SingleDockableNode d = xmlGetDockable(dockableElt);
        if (j == 0){
          base = d;
        } else {
          addToTabbedGroup(base.key, d.key);
        }
      }
    }
  }

   /** Registers a dockable as belonging to a tab group.
   * <p> It is used to have a memory of grouped (tabbed) dockables in order to keep the
   * group together when dockable are restored from auto-hide mode.
   * <p> This method is generally called by the tabbed container management, and not directly
   * by the developper.
   *
   * <p> However, there is a case where calling this method can be usefull :
   *  when, at startup, a desktop is built with multiple hidden dockables, and the developper wants
   * them to be grouped in a tab container when they are restored to the desktop.
   *
   * @param base   an already tabbed dockable
   * @param newTab a dockable to add to the tab group
   *
   *@since 1.1.2
   */
  public void addToTabbedGroup(WSDockKey base, WSDockKey newTab){
    /* this method is called when a dockable is added to a dockableTabbedContainer */
    LinkedList<WSDockKey> group = tabbedGroups.get(base);
    if (group == null){
      group = new LinkedList<WSDockKey>();
      group.add(base);
      tabbedGroups.put(base, group);
    }
    group.add(newTab);
    tabbedGroups.put(newTab, group);
  }

  
  
  
  
  // ------- inner classes used to handle dockable layout
  
  
  /** abstract node into the layout */
  private abstract class Node {
    Node parent;
  }
  
  /** a node describing a tab */
  private class TabNode extends Node {
    ArrayList tabs = new ArrayList();
    SingleDockableNode getTab(int index){
      return (SingleDockableNode) tabs.get(index);
    }
    void addTab(int index, SingleDockableNode tab){
      tabs.add(index, tab);
      tab.parent = this;
    }
    
    private void replaceTab(int index, SingleDockableNode node) {
      tabs.set(index, node);
      node.parent = this;
    }
  }
  
  /** a node containing a single dockable  */
  private class SingleDockableNode extends Node {
    WSDockKey key;
    SingleDockableNode(WSDockKey key){
      this.key = key;
      nodesByKey.put(key, this);
    }
  }
  
  /** a single node that contains a whole dockable hierarchy */
  private class CompoundDockableNode extends SingleDockableNode {
    Node nestedNode;
    CompoundDockableNode(WSDockKey key){
      super(key);
    }
    void setNestedNode(Node nested){
      nested.parent = this;
      nestedNode = nested;
    }
  }
  
  /** a node that represents a split  */
  private class SplitNode extends Node{
    
    void setTop(Node node) {
      this.left = node;
      node.parent = this;
    }
    void setBottom(Node node) {
      this.right = node;
      node.parent = this;
    }
    void setLeft(Node node) {
      this.left = node;
      node.parent = this;
    }
    void setRight(Node node) {
      this.right = node;
      node.parent = this;
    }
    Node getTop(){
      return left;
    }
    Node getBottom(){
      return right;
    }
    Node getLeft(){
      return left;
    }
    Node getRight(){
      return right;
    }
    Node left, right;
    
    private double location; // proportional location (0-1)
    
    boolean isHorizontal;
  }
  
  /** a node describing a auto-hide dockable */
  private class HiddenNode extends Node {
    
    private SingleDockableNode node;
    private RelativeDockablePosition position;
    
    HiddenNode(SingleDockableNode node, RelativeDockablePosition position){
      this.node = node;
      node.parent = this;
      this.position = position;
    }
    void setChild(SingleDockableNode child){
      child.parent = this;
      this.node = child;
    }
  }
  
  /** used as root node for main and hidden hierarchies */
  private class TopLevelNode extends Node {
    Node child;
    void setChild(Node child){
      this.child = child;
      child.parent = this;
    }
  }
  
  /** top container for floating dockables */
  private class FloatingNode extends Node {
    private Node child;
    private Rectangle windowRect;
    private RelativeDockablePosition returnPosition;
    FloatingNode(Node child, Rectangle windowRect, RelativeDockablePosition returnPosition){
      this.windowRect = windowRect;
      this.returnPosition = returnPosition;
      setChild(child);
    }
    void setChild(Node child){
      this.child = child;
      child.parent = this;
    }
    
  }
  
}
