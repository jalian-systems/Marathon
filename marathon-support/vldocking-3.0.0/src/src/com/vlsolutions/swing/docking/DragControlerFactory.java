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

/** A Basic factory providing a lightweight and heavyweight implementation of the
 * DragControler interface
 *
 * @author Lilian Chamontin, vlsolutions.
 */
public class DragControlerFactory {
    private static DragControlerFactory instance;

    public DragControler createDragControler(DockingDesktop desktop) {
        if (DockingPreferences.isLightWeightUsageEnabled()) {
            return new LightWeightDragControler(desktop);
        } else {
            return new HeavyWeightDragControler(desktop);
        }
    }

    public static DragControlerFactory getInstance() {
        if (instance == null) {
            instance = new DragControlerFactory();
        }
        return instance;
    }

    public static void setInstance(DragControlerFactory instance) {
        DragControlerFactory.instance = instance;
    }
}
