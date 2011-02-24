package com.vlsolutions.swing.docking;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** A glasspane use to paint drag / drop shape on top of the desktop
 *
 * @author Lilian Chamontin, VLSolutions
 */
public class DragControlerGlassPane extends JComponent {
    Cursor stopDragCursor, dragCursor, floatCursor, swapDragCursor;

    Color innerColor = new Color(64, 64, 64, 64);

    Color textColor = Color.WHITE;

    Color textFillColor = new Color(32, 32, 32, 128);

    Color textBorderColor = new Color(64, 64, 64);

    boolean paintShapes = true;

    ShapeLabelPainter labelPainter = new ShapeLabelPainter();

    ShapeOutlinePainter outlinePainer = new ShapeOutlinePainter();

    private DragControler controler;

    DragControlerGlassPane(DragControler controler) {
        this.controler = controler;
        addMouseListener(new MouseAdapter() {
        }); // grab events
        addMouseMotionListener(new MouseMotionAdapter() {
        });
        showDragCursor();
    }

    /** Enables or disables shape painting */
    public void setPaintShapes(boolean paintShapes) {
        this.paintShapes = paintShapes;
    }

    public void paintComponent(Graphics g) {
        if (paintShapes) {
            Graphics2D g2 = (Graphics2D) g;
            if (controler.getDropShape() != null) {
                Shape s = controler.getDropShape();
                Point p = SwingUtilities.convertPoint((Component) controler.getDropReceiver(), 0, 0, this);
                Shape s2 = AffineTransform.getTranslateInstance(p.x,
                        p.y).createTransformedShape(s);
                outlinePainer.paintShape(g2, s2);
                labelPainter.paintLabel(g2, s2, controler.getDockable().getDockKey().getName());
            }
        }
    }

    public void showStopDragCursor() {
        if (stopDragCursor == null) {
            Image stopDragImage = (Image) UIManager.get("DragControler.stopDragCursor"); //2005/11/01
            stopDragCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    stopDragImage, new Point(16, 16), "stopdragcursor");
        }
        setCursor(stopDragCursor);

    }

    public void showSwapDragCursor() {
        if (swapDragCursor == null) {
            Image swapDragImage = (Image) UIManager.get("DragControler.swapDragCursor"); //2005/11/01
            swapDragCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    swapDragImage, new Point(16, 16), "swapdragcursor");
        }
        setCursor(swapDragCursor);

    }

    public void showFloatCursor() {
        if (floatCursor == null) {
            Image floatImage = (Image) UIManager.get("DragControler.detachCursor"); //2005/11/01
            floatCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    floatImage, new Point(16, 16), "floatcursor");
        }
        setCursor(floatCursor);

    }

    public void showDragCursor() {
        if (dragCursor == null) {
            Image dragImage = (Image) UIManager.get("DragControler.dragCursor"); //2005/11/01
            dragCursor = Toolkit.getDefaultToolkit().createCustomCursor(dragImage,
                    new Point(16, 16), "dragcursor");
        }
        setCursor(dragCursor);
    }

    /** the object responsible for painting the shape label */
    class ShapeLabelPainter {
        private Color textColor = Color.WHITE;

        private Color textFillColor = new Color(32, 32, 32, 128);
//    private Color textFillColor = new Color(128, 128,128);

        private Color textBorderColor = new Color(64, 64, 64);

        public void paintLabel(Graphics2D g2, Shape s, String name) {
            Rectangle bounds = s.getBounds();
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(name);

            g2.setColor(textFillColor);
            int bx, by;
            if (controler.isFloatingShape()) {
                bx = bounds.x + bounds.width / 2 - w / 2;
                by = bounds.y + bounds.height / 2 - fm.getHeight() / 2;
            } else {
                bx = 4 * ((bounds.x + bounds.width / 2 - w / 2) / 4);
                // 2005/11/01 small hack to overcome small drifts of the label
                // (for example when used on a tabbedpane and when the selected tab is
                // one or two pixels bigger than a non selected tab.
                // just snapping it with a 4*4 grid avoid those glitches (without changing
                // too much the shapes algorithm).
                by = 4 * ((bounds.y + bounds.height / 2 - fm.getHeight() / 2) / 4);
            }
            g2.fillRect(bx - 5, by, w + 10, fm.getHeight());
            g2.setStroke(new BasicStroke(1));
            g2.setColor(textBorderColor);
            g2.drawRect(bx - 5, by, w + 10, fm.getHeight());
            g2.setColor(textColor);
            g2.drawString(name, bx, by + fm.getAscent());
        }
    }
}

/** the object responsible for painting the shape outline */
class ShapeOutlinePainter {
    private Color innerColor = new Color(64, 64, 64);

    public void paintShape(Graphics2D g2, Shape s) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setStroke(new BasicStroke(3));
        g2.setColor(innerColor);
        g2.fill(s);
        g2.setComposite(old);
        g2.setColor(Color.DARK_GRAY);
        g2.draw(s);
    }
}