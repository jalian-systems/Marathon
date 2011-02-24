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

import java.awt.Component;
import java.awt.Shape;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** This interface defines the features required for a drag controler.
 *
 * @since 3.0
 * @author Lilian Chamontin, vlsolutions.
 */
public interface DragControler extends MouseListener, MouseMotionListener{

    public void cancelDrag();

    public Dockable getDockable();

    public DockDropReceiver getDropReceiver();

    public Shape getDropShape();

    public boolean isFloatingShape();

}
