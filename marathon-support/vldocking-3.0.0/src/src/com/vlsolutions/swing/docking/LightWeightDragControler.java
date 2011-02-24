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
import java.awt.Container;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JFrame;

/** A Lightweight (standard) implementation of the drag controler, and its associated classes
 *
 * @author Lilian Chamontin, vlsolutions.
 * @since 3.0
 */
class LightWeightDragControler extends AbstractDragControler {
    /** instantiates a controler for a given docking panel*/
    LightWeightDragControler(DockingDesktop desktop) {
        super(desktop);
    }

    /** find the top most component of this container */
    @Override
    protected Component findComponentAt(Container container, int x, int y) {
        return container.findComponentAt(x, y);
    }

    @Override
    protected ShapePainterStrategy createShapePainterStrategy(Window w) {
        return new LWShapePainterStrategy(w);
    }


    /** This class holds implementation strategies of shapes painting.
     *<p>
     * As painting is different when pure Swing is used (glasspane) and
     * heavyweight components are mixed in (glasspane + canvas).
     */
    private class LWShapePainterStrategy implements ShapePainterStrategy {
        private DragControlerGlassPane dragGlassPane = new DragControlerGlassPane(LightWeightDragControler.this);

        private Component oldGlassPane = null;

        private boolean oldGlassPaneVisible = false;

        private Window window = null;

        private boolean isDragStarted = false;

        public LWShapePainterStrategy(Window window) {
            this.window = window;
        }

        /** show the drag cursor */
        public void showDragCursor() {
            dragGlassPane.showDragCursor();
        }

        /** show the stop-drag cursor  (drag not enabled)*/
        public void showStopDragCursor() {
            dragGlassPane.showStopDragCursor();
        }

        /** show the stop-drag cursor  (drag not enabled)*/
        public void showSwapDragCursor() {
            dragGlassPane.showSwapDragCursor();
        }

        /** show the float (detached) cursor  */
        public void showFloatCursor() {
            dragGlassPane.showFloatCursor();
        }

        public void repaint() {
            /* this is a hack that will be refactored : we post a repaint when there is
             * a need to show a drag shape */
            dragGlassPane.repaint();
        }

        public void startDrag(DockableDragSource source) {
            if (isDragStarted || source == null) {
                // safety checks
                return;
            }
            Window aboveWindow = this.window;

            isDragStarted = true;

            if (aboveWindow instanceof JFrame) {
                oldGlassPane = ((JFrame) aboveWindow).getGlassPane();
                oldGlassPaneVisible = oldGlassPane.isVisible();
                ((JFrame) aboveWindow).setGlassPane(dragGlassPane);
                dragGlassPane.setVisible(true);
            } else if (aboveWindow instanceof JDialog) {
                oldGlassPane = ((JDialog) aboveWindow).getGlassPane();
                oldGlassPaneVisible = oldGlassPane.isVisible();
                ((JDialog) aboveWindow).setGlassPane(dragGlassPane);
                dragGlassPane.setVisible(true);
            }
        }

        public void endDrag() {
            Window aboveWindow = this.window;//SwingUtilities.getWindowAncestor(desktop);
            if (aboveWindow instanceof JFrame) {
                ((JFrame) aboveWindow).setGlassPane(oldGlassPane);
            } else if (aboveWindow instanceof JDialog) {
                ((JDialog) aboveWindow).setGlassPane(oldGlassPane);
            }
            oldGlassPane.setVisible(oldGlassPaneVisible);
            isDragStarted = false;
        }
    }
}