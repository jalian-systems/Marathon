package com.vlsolutions.swing.docking;

/** Contract for a shape painter (during drag and drop operation)
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 3.0
 */
interface ShapePainterStrategy {
    /** show the drag cursor */
    public void showDragCursor();

    /** show the stop-drag cursor  (drag not enabled)*/
    public void showStopDragCursor();

    /** show the stop-drag cursor  (drag not enabled)*/
    public void showSwapDragCursor();

    /** show the float (detached) cursor  */
    public void showFloatCursor();

    public void repaint();

    public void startDrag(DockableDragSource source);

    public void endDrag();
}
