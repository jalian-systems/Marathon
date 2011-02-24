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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * This class contains methods to keep a set of toolbars persistent.
 * <p> 
 * The class uses the preferences framework of the java runtime, and installs a 
 * shutdown hook to save the toolbars state on application exit.
 * <p>
 * Warning : this class cannot be run from a sandboxed (untrusted) environment 
 *  (unsigned applet/ java web start) as it uses classes requiring a SecurityManager 
 * (Preferences API and Runtime shutdown hook).
 * <p>
 * Call loadToolBarLayout() upon application startup, after registering all ToolBars.
 *
 * @author Michael Westergaard, revised by Lilian Chamontin.
 *
 * @see ToolBarIO
 */
public class ToolBarPersistence implements Runnable {
  private final static String TOOLBAR_LAYOUT = "toolbar_layout"; //$NON-NLS-1$
  private ToolBarContainer container;
  private ToolBarIO io;
  private Preferences prefs;
  
  /**
   * Construct a new set of persistent toolbars.
   *
   * @param mainClass the main class of the application.  Will be used to
   * generate a reasonable name for the preferences to store toolbar
   * configuration
   * @param container the toolbarcontainer to keep persistent
   * @throws SecurityException if this class is invoked from an untrusted environment
   */
  public ToolBarPersistence(Object mainClass, ToolBarContainer container) {
    this.container = container;
    this.io = new ToolBarIO(container);
    String nodeName = mainClass.getClass().getName().replaceFirst(mainClass.getClass().getPackage().getName() + ".", ""); //$NON-NLS-1$ //$NON-NLS-2$
    prefs = Preferences.userNodeForPackage(mainClass.getClass()).node(nodeName);
    Runtime.getRuntime().addShutdownHook(new Thread(this));
    
  }
  
  protected void handleUnloadedToolBar(VLToolBar toolbar) {
    ToolBarPanel panel =
        container.getToolBarPanelAt(BorderLayout.NORTH);
    if (panel != null)
      panel.add(toolbar, new ToolBarConstraints());
  }
  
  /**
   * Load the toolbar configuration.
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public void loadToolBarLayout() throws IOException, ParserConfigurationException, SAXException {
    byte data[] = prefs.getByteArray(TOOLBAR_LAYOUT, null);
    if (data != null) {
      InputStream in = new ByteArrayInputStream(data);
      ToolBarIOReadInfo tri = io.readXML(in);
      Collection unloadedToolBars = tri.getNotInstalledToolbars();
      in.close();
      for (Iterator i = unloadedToolBars.iterator(); i.hasNext(); ) {
        VLToolBar toolbar = (VLToolBar) i.next();
        handleUnloadedToolBar(toolbar);
      }
    }
  }
  
  /**
   * Save the toolbar configuration.  
   *<p> Masks all errors.
   *<p> don't call this method directly : it is used by the shudown hook.
   *
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      saveToolBarLayout();
    } catch (Exception exception) {
      // Mask error as we are quitting anyway
    }
  }
  
  /**
   * Save the toolbar configuration.
   *
   * @throws IOException
   */
  public void saveToolBarLayout() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    io.writeXML(out);
    out.close();
    prefs.putByteArray(TOOLBAR_LAYOUT, out.toByteArray());
  }
}