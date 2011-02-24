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

import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** A logical organization of dockables. Used to define and save desktop layouts (xml loading in not supported yet).
 *<p>
 * A Workspace is composed of WSDesktop elements (one per involved desktop).
 * <p>
 * Each WSDesktop supports an API similar to the DockingDesktop (addDockable, split, createTab), where
 * arguments are simple DockKeys (and not Dockables).
 *
 * <p> Currently this version doesn't support dockable removal : you can create a workspace, but shouldn't alter its
 * layout by moving already positionned dockables elsewhere (e.g. workspace will fail if you install a dockable
 * on an auto-hide border, then add is as a docked tab later).
 *
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1.2
 */
public class Workspace {
  private ArrayList desktops = new ArrayList();
  
  /** Creates a workspace with a single default WSDesktop */
  public Workspace() {
    desktops.add(new WSDesktop());
  }
  
  /** Removes the contained desktops */
  public void clear(){
    desktops.clear();
  }
  
  /** Adds a new desktop to this workspace */
  public void addDesktop(WSDesktop desktop){
    desktops.add(desktop);
  }
  
  /** Return the number of desktops contained in this workspace (default is 1 ) */
  public int getDesktopCount(){
    return desktops.size();
  }
  
  /** Returns the index-th desktop contained */
  public WSDesktop getDesktop(int index){
    return (WSDesktop) desktops.get(index);
  }
  
  /** Returns a desktop identified by its name or null if not found */
  public WSDesktop getDesktop(String desktopName){
    if (desktops.size() == 1){
      return (WSDesktop) desktops.get(0);
    }
    for (int i=0; i < desktops.size(); i++){
      WSDesktop d = (WSDesktop) desktops.get(i);
      if (d.getDesktopName().equals(desktopName) ){
        return d;
      }
    }
    return null;
  }
  
  
  /** Applies this workspace to the given docking context (this is equivalent as loading a
   * workspace file from DockingContext.readXML() : it removes every dockable from the context and
   * associated desktops, and clears their dockable states, then it reloads the dockables as specified
   * by this workspace layout.
   */
  public void apply(DockingContext dockingContext) throws WorkspaceException {
    ByteArrayOutputStream outb = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(outb);
    out.println("<?xml version=\"1.0\"?>");
    out.println("<VLDocking version=\"2.1\">");
    for (int i=0; i < desktops.size(); i++){
      WSDesktop desktop = (WSDesktop) desktops.get(i);
      desktop.writeDesktopNode(out);
    }
    out.println("</VLDocking>");
    out.close();
    byte [] bytes = outb.toByteArray();
    //System.out.println(new String(bytes));
    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    try {
      dockingContext.readXML(is);
    } catch (Exception ex) {
      throw new WorkspaceException(ex);
    } finally {
      try {
        is.close();
      } catch (Exception ignore) {
      }
    }
  }
  
  /** Loads and configures this workspace from a given docking context.
   *<p>
   * The workspace is then ready to be applied or saved as a stream.
   * @since 2.1.3
   * @see #apply(DockingContext)
   */
  public void loadFrom(DockingContext context) throws WorkspaceException{    
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      context.writeXML(out);
      out.close();
      byte [] bytes = out.toByteArray();
      //System.out.println(new String(bytes));
      ByteArrayInputStream is = new ByteArrayInputStream(bytes);
      readXML(is);
      is.close();
    } catch (Exception e){
      throw new WorkspaceException(e);
    }
  }
  
  /** Saves the workspace layout into an XML stream.
   * <p>
   * The workspace is composed of every desktop layouts associated
   * with this workspace (desktops are identified by their 'desktopName' property).
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
      WSDesktop desktop = (WSDesktop) desktops.get(i);
      desktop.writeDesktopNode(out);
    }
    out.println("</VLDocking>");
    
    out.flush();
  }
  
  public void readXML(InputStream in) throws ParserConfigurationException, IOException, SAXException {
  
    // remove all dockable states
    
    for (int i=0; i < desktops.size(); i++){
      WSDesktop desk = (WSDesktop) desktops.get(i);
      desk.clear();
    }
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(in);
    Element root = doc.getDocumentElement();
    if (root.getNodeName().equals("DockingDesktop")){
      // this is an old desktop file, missing the VLDocking root node (VLDocking 2.0 format)
      // so we noly have a single desktop
      WSDesktop desk = getDesktop(0); // only one
      desk.readDesktopNode(root);
    } else {
      NodeList desktopChildren = root.getChildNodes();
      for (int i = 0, len = desktopChildren.getLength(); i < len; i++) {
        Node node = desktopChildren.item(i);
        if (node instanceof Element){
          Element deskChild = (Element) node;
          WSDesktop desk = getDesktop(deskChild.getAttribute("name"));
          desk.readDesktopNode(deskChild);
        }
      }
    }
    
  }
  
  
  
}
