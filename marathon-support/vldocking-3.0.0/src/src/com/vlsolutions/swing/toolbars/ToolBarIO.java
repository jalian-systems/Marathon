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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/** A utility class used to save and restore toolbars to/from an XML configuration.
 *
 * @author Lilian Chamontin, VLSolutions 
 */
public class ToolBarIO {
  
  /** The main container */
  private ToolBarContainer container;
  
  /** Constructs a new ToolBarIO utility based on the given container */
  public ToolBarIO(ToolBarContainer container) {
    this.container = container;
  }
  
  /** Saves the current toolbar configuration into an XML stream.
   * <p>
   * The stream is not closed at the end of the operation.
   *
   * @see #readXML(InputStream)
   * */
  public void writeXML(OutputStream stream) throws IOException {
    PrintWriter out = new PrintWriter(stream);
    out.println("<?xml version=\"1.0\"?>");
    out.println("<VLToolBars version=\"1.0\">");
    xmlWriteContainer(out);


    out.println("</VLToolBars>");
    out.flush();
  }

  private void xmlWriteContainer(PrintWriter out){
    out.println("<Container>");
    Component [] comps = container.getComponents();
    for (int i=0; i < comps.length; i++){
      Object constraints = container.getConstraints(comps[i]);
      if (constraints.equals(BorderLayout.NORTH)){
        ToolBarPanel panel = container.getToolBarPanelAt(constraints);
        xmlWriteToolBarPanel(out, panel, "top");
      } else if (constraints.equals(BorderLayout.WEST)){
        ToolBarPanel panel = container.getToolBarPanelAt(constraints);
        xmlWriteToolBarPanel(out, panel, "left");
      } else if (constraints.equals(BorderLayout.SOUTH)){
        ToolBarPanel panel = container.getToolBarPanelAt(constraints);
        xmlWriteToolBarPanel(out, panel, "bottom");
      } else if (constraints.equals(BorderLayout.EAST)){
        ToolBarPanel panel = container.getToolBarPanelAt(constraints);
        xmlWriteToolBarPanel(out, panel, "right");
      }
    }
    out.println("</Container>");
  }
  
  private void xmlWriteToolBarPanel(PrintWriter out, ToolBarPanel panel, String position){
    out.println("<ToolBarPanel position=\"" + position + "\">");
    ToolBarPanelLayout layout = (ToolBarPanelLayout) panel.getLayout();
    Component [] children = layout.getComponents(); // toolbars only
    for (int i=0; i < children.length; i++){
      VLToolBar tb = (VLToolBar) children[i];
      ToolBarConstraints tc = layout.getConstraints(tb);
      out.println("<ToolBar name=\"" + tb.getName() + "\" major=\"" + tc.majorOrder + "\" minor=\"" + tc.minorOrder + "\" collapsed=\"" + tb.isCollapsed() + "\"/>");
    }
    out.println("</ToolBarPanel>");
  }
  
  /** Reads an XML encoded stream as the toolbar configuration.
   * <p>
   * When the method returns, the container is totally reconfigured with possibly different
   * toolbars at different positions.
   * <p>
   * <b>Note : </b> The <code>VLToolBar</code>s of the stream must be registered with
   * the {@link ToolBarContainer#registerToolBar(VLToolBar) registerToolBar} method,
   * prior readXML. 
   * <p> Also note that the container must already contain its ToolBarPanels before reading 
   * the configuration.
   *
   * <p>
   * Dismisses all visible toolbars.
   * <p>
   * The stream is not closed at the end of the operation.
   * @see #writeXML(OutputStream)
   * @see ToolBarContainer#registerToolBar(VLToolBar)
   * @return a descriptor of the reading operation(useful for example to know the registered toolbars, that weren't
   *         loaded (missing) from the input file).
   *  */
  public ToolBarIOReadInfo readXML(InputStream in) throws ParserConfigurationException, IOException, SAXException {

    ToolBarIOReadInfo tri = new ToolBarIOReadInfo();
    
    // first clear the toolbar panels
    Component [] comps = container.getComponents();
    for (int i=0; i < comps.length; i++){
      Object constraints = container.getConstraints(comps[i]);
      if (constraints.equals(BorderLayout.NORTH)){
        ((Container)comps[i]).removeAll();
      } else if (constraints.equals(BorderLayout.WEST)){
        ((Container)comps[i]).removeAll();
      } else if (constraints.equals(BorderLayout.SOUTH)){
        ((Container)comps[i]).removeAll();
      } else if (constraints.equals(BorderLayout.EAST)){
        ((Container)comps[i]).removeAll();
      }
    }

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(in);
    Element root = doc.getDocumentElement();
    NodeList children = root.getChildNodes();
    for (int i = 0, len = children.getLength(); i < len; i++) {
      Node child = children.item(i);
      xmlReadRootNode(child, tri);
    }

    tri.setRegisteredToolbars(container.getRegisteredToolBars());
    tri.finishLoading();
    
    container.revalidate();
    
    return tri;
  }

  private void xmlReadRootNode(Node node, ToolBarIOReadInfo tri) throws SAXException{
    if (node.getNodeType() == Node.ELEMENT_NODE){
      Element elt = (Element) node;
      String name = elt.getNodeName();
      if (name.equals("Container")){
        NodeList children = node.getChildNodes();
        for (int i = 0, len = children.getLength(); i < len; i++) {
          Node child = children.item(i);
          xmlReadContainerNode(child, tri);
        }
      } else {
        throw new SAXNotRecognizedException(name);
      }
    }    
  }

  private void xmlReadContainerNode(Node node, ToolBarIOReadInfo tri) throws SAXException{
    if (node.getNodeType() == Node.ELEMENT_NODE){
      Element elt = (Element) node;
      String name = elt.getNodeName();
      if (name.equals("ToolBarPanel")){
        String position = elt.getAttribute("position");
        ToolBarPanel tp = null;
        if (position.equals("top")){
          tp = container.getToolBarPanelAt(BorderLayout.NORTH);
        } else if (position.equals("left")){
          tp = container.getToolBarPanelAt(BorderLayout.WEST);
        } else if (position.equals("bottom")){
          tp = container.getToolBarPanelAt(BorderLayout.SOUTH);
        } else if (position.equals("right")){
          tp = container.getToolBarPanelAt(BorderLayout.EAST);
        }
        NodeList children = elt.getElementsByTagName("ToolBar");
        for (int i = 0, len = children.getLength(); i < len; i++) {
          xmlReadToolBarPanelNode(tp, (Element)children.item(i), tri);
	    }
        tp.revalidate();
      } else {
        throw new SAXNotRecognizedException(name);
      }
    }    
  }

  private void xmlReadToolBarPanelNode(ToolBarPanel panel, Element toolbarElt, ToolBarIOReadInfo tri) {
    String toolbarName = toolbarElt.getAttribute("name");
    int major = Integer.parseInt(toolbarElt.getAttribute("major"));
    int minor = Integer.parseInt(toolbarElt.getAttribute("minor"));
    boolean collapsed = Boolean.valueOf(toolbarElt.getAttribute("collapsed")).booleanValue(); //2.0.6b
    ToolBarConstraints tc = new ToolBarConstraints(major, minor);

    VLToolBar tb = container.getToolBarByName(toolbarName);
    
    if (tb != null) {
        tb.setCollapsed(collapsed); // 2.0.6b
        panel.add(tb, tc);
        tri.notifyToolbarInstalled(tb);
    } else {
        // If the ToolBar is not registered ignore it
      tri.notifyUnknownToolbarFound(toolbarName);
    }
  }
  
}
