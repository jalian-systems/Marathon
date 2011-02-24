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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** A Heavyweight implementation of the drag controler, and its associated classes
 *
 * @since 3.0
 * @author Lilian Chamontin, vlsolutions.
 */
class HeavyWeightDragControler extends AbstractDragControler {
    private boolean paintBackgroundUnderDragRect = UIManager.getBoolean("DragControler.paintBackgroundUnderDragRect");

    /** instantiates a controler for a given docking panel*/
    HeavyWeightDragControler(DockingDesktop desktop) {
        super(desktop);
    }

    /** find the top most component of this container */
    protected Component findComponentAt(Container container, int x, int y) {
        Rectangle bounds = new Rectangle();
        int count = container.getComponentCount();

        //ShapePainterStrategy shapePainterStrategy = getShapePainter(container, null);

        for (int i = 0; i < count; i++) {
            Component child = container.getComponent(i);
            if (child.isVisible()) {
                child.getBounds(bounds);
                if (bounds.contains(x, y)) {
                    if (child instanceof Container) {
                        // recursive
                        Component found = findComponentAt((Container) child, x - bounds.x, y - bounds.y);
                        if (found != null) {
                            return found;
                        } else {
                            return child;
                        }
                    } else if (!(child instanceof HeavyShape || child instanceof HeavyLabel)) {
                        // skip our dedicated components for heavyweight support
                        return child;
//          } else if (child != shapePainterStrategy.heavyShape &&
//              child != shapePainterStrategy.heavyShape.label){
//            // skip our dedicated components for heavyweight support
//            return child;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected ShapePainterStrategy createShapePainterStrategy(Window w) {
        return new HWShapePainterStrategy(w);
    }

    public Dockable getDockable() {
        if (dockableDragSource != null) {
            return dockableDragSource.getDockable();
        } else {
            return null;
        }
    }

    // -----------------------------------------------------------------------------------------------------------
    // --------------------------------- you don't want to look at the code underneath this line... --------------
    // -----------------------------------------------------------------------------------------------------------
    // I'll rewrite it entirely someday as i'm not pleased with it (much too complex)
    /** This class holds implementation strategies of shapes painting.
     *<p>
     * As painting is different when pure Swing is used (glasspane) and
     * heavyweight components are mixed in (glasspane + canvas).
     */
    private class HWShapePainterStrategy implements ShapePainterStrategy {
        private DragControlerGlassPane dragGlassPane = new DragControlerGlassPane(HeavyWeightDragControler.this);

        private Component oldGlassPane = null;

        private boolean oldGlassPaneVisible = false;

        private Window window = null;

        private boolean isDragStarted = false;

        // heavyweight support
        private HeavyShape heavyShape; // instanciated only when heavyweight suport is enabled

        /*public ShapePainterStrategy(){
        if (! isLightWeight){
        dragGlassPane.setPaintShapes(false);
        heavyShape = new HeavyShape();
        }
        }*/
        public HWShapePainterStrategy(Window window) {
            this.window = window;
            dragGlassPane.setPaintShapes(false);
            heavyShape = new HeavyShape();
        }

        /** show the drag cursor */
        public void showDragCursor() {
            dragGlassPane.showDragCursor();
            heavyShape.setCursor(dragGlassPane.getCursor());
        }

        /** show the stop-drag cursor  (drag not enabled)*/
        public void showStopDragCursor() {
            dragGlassPane.showStopDragCursor();
            heavyShape.setCursor(dragGlassPane.getCursor());
        }

        /** show the stop-drag cursor  (drag not enabled)*/
        public void showSwapDragCursor() {
            dragGlassPane.showSwapDragCursor();
            heavyShape.setCursor(dragGlassPane.getCursor());
        }

        /** show the float (detached) cursor  */
        public void showFloatCursor() {
            dragGlassPane.showFloatCursor();
            heavyShape.setCursor(dragGlassPane.getCursor());
        }

        public void repaint() {
            /* this is a hack that will be refactored : we post a repaint when there is
             * a need to show a drag shape */
            if (dropShape != null) {
                heavyShape.moveToShape(dropShape); // adjust to the shape before repainting
            } else if (heavyShape.isVisible()) {
                heavyShape.setVisible(false);
            }
        }

        public void startDrag(DockableDragSource source) {
            if (isDragStarted || source == null) {
                // safety checks
                return;
            }
            Window aboveWindow = this.window;

            isDragStarted = true;

            if (heavyShape.getParent() == null) {
                // first use of the heavyshapes components
                if (aboveWindow instanceof JFrame) {
                    JFrame fr = (JFrame) aboveWindow;
                    fr.getLayeredPane().add(heavyShape, JLayeredPane.DRAG_LAYER);
                    heavyShape.validate();
                    fr.getLayeredPane().add(heavyShape.label, JLayeredPane.DRAG_LAYER);
                } else if (aboveWindow instanceof JDialog) {
                    JDialog dlg = (JDialog) aboveWindow;
                    dlg.getLayeredPane().add(heavyShape, JLayeredPane.DRAG_LAYER);
                    heavyShape.validate();
                    dlg.getLayeredPane().add(heavyShape.label, JLayeredPane.DRAG_LAYER);
                }
                heavyShape.setZOrder();
            }
            heavyShape.label.setName(source.getDockable().getDockKey().getName());

            // take a snapshot of the frame... ugly trick, but couldn't find anything better !
            heavyShape.startDrag();



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

            heavyShape.setVisible(false);
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

    /** heavyweight component used to paint shapes on top of heavyweight components
     */
    private class HeavyShape extends Canvas {
        private Rectangle cachedShapeBounds;

        private BufferedImage desktopImage; // used for heavyweight painting

        private Image subImage;

        private Rectangle subImageBounds;

        private ShapeOutlinePainter outlinePainter = new ShapeOutlinePainter();

        private HeavyLabel label = new HeavyLabel();

        public HeavyShape() {
            setEnabled(false); // don't override the glaspane cursor
        }

        public void setZOrder() {
            // jdk1.5 only
            try {
                //desktop.setComponentZOrder(this, -1); // on top
                Method m = Container.class.getMethod("setComponentZOrder", new Class[]{
                            Component.class, int.class});
                m.invoke(getParent(), new Object[]{this, new Integer(0)});
            } catch (Exception ignore) {
            }
            label.setZOrder();

        }

        private void startDrag() {
            Container parent = getParent();

            if (paintBackgroundUnderDragRect) { // 2007/02/27
                if (desktopImage == null || (desktopImage.getWidth() != parent.getWidth()) || (desktopImage.getHeight() != parent.getHeight())) {
                    desktopImage = (BufferedImage) parent.createImage(parent.getWidth(),
                            parent.getHeight());
                    subImage = null;
                }
                Graphics g = desktopImage.getGraphics();
                parent.paint(g);
                g.dispose();
            }
        }

        public void paint(Graphics g) {
            if (dropShape != null) {
                Point p = SwingUtilities.convertPoint((Component) dropReceiver, 0, 0, getParent());
                Rectangle r = dropShape.getBounds();
                int shapeX = r.x, shapeY = r.y;
                if (paintBackgroundUnderDragRect) {
                    r.x += p.x;
                    r.y += p.y;
                    r.width += 2; // stroked shape (+3 pixels, centered)
                    r.height += 2;
                    if (r.x + r.width > desktopImage.getWidth()) {
                        r.width = desktopImage.getWidth() - r.x;
                    }
                    if (r.y + r.height > desktopImage.getHeight()) {
                        r.height = desktopImage.getHeight() - r.y;
                    }
                    if (subImage == null || !r.equals(subImageBounds)) {
                        subImageBounds = r;
                        subImage = desktopImage.getSubimage(r.x, r.y, r.width, r.height);
                    }
                    g.drawImage(subImage, 0, 0, null);
                }

                Shape s = AffineTransform.getTranslateInstance(-shapeX, -shapeY).createTransformedShape(dropShape);

                Graphics2D g2 = (Graphics2D) g;
                outlinePainter.paintShape(g2, s);

            }
        }

        public void setVisible(boolean visible) {
            super.setVisible(visible);
            label.setVisible(visible);
        }

        public void setCursor(Cursor cursor) {
            super.setCursor(cursor);
            label.setCursor(cursor);
        }

        /** moves and resizes the canvas to the position of the drop shape */
        public void moveToShape(Shape newShape) {
            setVisible(true);
            Shape s = dropShape;
            Container container = getParent();
            Point p = SwingUtilities.convertPoint((Component) dropReceiver, 0, 0, container);
            Rectangle r = dropShape.getBounds();
            r.x += p.x;
            r.y += p.y;
            r.width += 2; // shape has stroke(3), so we must extend it a little bit
            r.height += 2;
            if (r.x + r.width > container.getWidth()) { // check extend not out of bounds
                r.width = container.getWidth() - r.x;
            }
            if (r.y + r.height > container.getHeight()) {
                r.height = container.getHeight() - r.y;
            }

            if (!r.equals(cachedShapeBounds)) {
                setBounds(r);
                cachedShapeBounds = r;
                label.moveToCenter(r);
            }
        }
    }

    private class HeavyLabel extends Canvas {
        private Color textColor = Color.WHITE;

        private Color textFillColor = new Color(128, 128, 128);

        private Color textBorderColor = new Color(64, 64, 64);

        private String name;

        private Icon icon;

        public HeavyLabel() {
            setEnabled(false); // don't override the glaspane cursor
        }

        public void setZOrder() {
            // jdk1.5 only (but we compile with source=1.4)
            try {
                //desktop.setComponentZOrder(this, -2); // on top of heavy panel
                Method m = Container.class.getMethod("setComponentZOrder", new Class[]{
                            Component.class, int.class});
                m.invoke(getParent(), new Object[]{this, new Integer(0)});
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public void moveToCenter(Rectangle shapeBounds) {
            Font f = getFont();
            if (f != null) {
                FontMetrics fm = getFontMetrics(f);
                int w = fm.stringWidth(name) + 10;
                int h = fm.getHeight() + 5;
                setBounds(shapeBounds.x + shapeBounds.width / 2 - w / 2,
                        shapeBounds.y + shapeBounds.height / 2 - h / 2,
                        w, h);
            }
        }

        public void paint(Graphics g) {
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(name);
            int h = fm.getHeight();
            g.setColor(textFillColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(textBorderColor);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(textColor);
            g.drawString(name, getWidth() / 2 - w / 2, getHeight() / 2 + h / 2);
        }
    }
}
