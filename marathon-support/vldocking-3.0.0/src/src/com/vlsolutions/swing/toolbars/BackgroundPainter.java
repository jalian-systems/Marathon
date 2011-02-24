package com.vlsolutions.swing.toolbars;

import java.awt.Graphics;
import javax.swing.JComponent;

/** An interface implemented by objects which can paint toolbar backgrounds. 
 *
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.1.4
 */
public interface BackgroundPainter {
  
  public void paintBackground(JComponent component, Graphics g);
  
}
