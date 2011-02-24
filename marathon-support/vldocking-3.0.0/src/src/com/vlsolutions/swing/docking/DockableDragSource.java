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

import java.awt.*;

/** An interface implemented by visual components used for drag and drop operations
 * on a DockableContainer.
 * <p>
 * This interface is used by API Extenders to create new kind of drag sources.
 * <p>
 * A "Drag Source" is a component, linked to a <code>Dockable<code>, responsible
 * for drag gesture recognition. There can be more than one DockableDragSource per
 * Dockable.
 * <p>
 * For example, the title bar of a docked component is usually the drag source of
 * this component (meaning : when a user drags the title bar, he expects the dockable
 * to be dragged around).
 * <p>
 * These draggable components must be able to give informations about their
 * target (the component known to the user, which is actually dragged).
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 */

public interface DockableDragSource {

    /** Notifies this source that a drag operation has begun.
     * <P> The source may reject the drag according to internal conditions (in that case
     * this method shall return <code>false</code>) or to wrong mouse position .
     *
     * @return true if drag operation is accepted (i.e the zone pointed by <code>p</code> refers to
     * a draggable component), false otherwise.
     * */
    public boolean startDragComponent(Point p);

    /** Returns the <code>Dockable</code> component this source is for */
    public Dockable getDockable();

    /** returns the DockableContainer responsible for displaying the associated dockable */
    public Container getDockableContainer();
    
    /** notifies the source when the drag operation has ended (by a drop or cancelled) 
     * @since 2.1.3
     */ 
    public void endDragComponent(boolean dropped);

}
