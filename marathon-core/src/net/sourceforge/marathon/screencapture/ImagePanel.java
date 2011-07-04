/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.screencapture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImagePanel extends JPanel {
    private static final Color ANNOTATION_COLOR = new Color(1.0f, 1.0f, 0.0f, 0.5f);
    private static final Color SELECTED_ANNOTATION_COLOR = new Color(0.8f, 0.8f, 0.0f, 0.8f);

    private static final long serialVersionUID = 1L;

    public static class Annotation extends Rectangle {
        private static final long serialVersionUID = 1L;

        private String text = "";

        private int index;

        public Annotation(Rectangle rect) {
            super(rect);
        }

        public Annotation(int index, Rectangle rect) {
            super(rect);
            this.index = index;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getIndex() {
            return index;
        }

        public void drawDecoration(Graphics g, int index, boolean selected) {
            if (selected)
                g.setColor(SELECTED_ANNOTATION_COLOR);
            else
                g.setColor(ANNOTATION_COLOR);
            g.fillRect(x, y, width - 1, height - 1);
            g.setColor(Color.RED);
            g.fillArc(x - 8, y - 8, 16, 16, 0, 359);
            g.setColor(Color.WHITE);
            Font f = g.getFont().deriveFont(Font.BOLD, 9);
            g.setFont(f);
            g.drawString(index + 1 + "", x - 4, y + 4);
        }

        @Override public boolean equals(Object arg0) {
            return super.equals(arg0);
        }
        
        @Override public int hashCode() {
            return super.hashCode();
        }
    }

    private ArrayList<Annotation> annotations = new ArrayList<Annotation>();

    private Annotation selectedAnnotation = null;
    private Rectangle rectToDraw = null;
    private Rectangle previousRectDrawn = new Rectangle();

    public boolean newRect;

    private IAnnotationListener annotationListener;
    private BufferedImage image;
    private InputStream imageFile;
    private boolean edit;
    private boolean dirty;

    public ImagePanel(InputStream imageFile, boolean edit) throws IOException {
        this.imageFile = imageFile;
        this.edit = edit;
        initializeAnnotations();
        setOpaque(true);
        setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
        MyListener myListener = new MyListener();
        addMouseListener(myListener);
        addMouseMotionListener(myListener);
        ActionMap actionMap2 = getActionMap();
        if (edit)
            actionMap2.put("Remove", new AbstractAction("Remove") {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    if (selectedAnnotation != null) {
                        dirty = true;
                        annotations.remove(selectedAnnotation);
                        setSelectedAnnotation(null);
                        if (annotationListener != null)
                            annotationListener.annotationRemoved();
                    }
                }
            });
        actionMap2.put("Next", new AbstractAction("Next") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                if (annotations.size() == 0)
                    return;
                int index = 0;
                if (selectedAnnotation != null) {
                    index = annotations.indexOf(selectedAnnotation);
                    if (index == -1)
                        index = 0;
                    else
                        index++;
                    if (index >= annotations.size())
                        index = 0;
                }
                setSelectedAnnotation((Annotation) annotations.get(index));
            }
        });
        actionMap2.put("Previous", new AbstractAction("Previous") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                if (annotations.size() == 0)
                    return;
                int index = 0;
                if (selectedAnnotation != null) {
                    index = annotations.indexOf(selectedAnnotation);
                    if (index == -1)
                        index = annotations.size() - 1;
                    else
                        index--;
                    if (index < 0)
                        index = annotations.size() - 1;
                }
                setSelectedAnnotation((Annotation) annotations.get(index));
            }
        });
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        if (edit)
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Remove");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Next");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Previous");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Next");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Previous");
    }

    private void initializeAnnotations() throws FileNotFoundException, IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (readers.hasNext()) {
            ImageReader reader = (ImageReader) readers.next();
            reader.setInput(iis);
            image = reader.read(0);
            IIOMetadata imageMetadata = reader.getImageMetadata(0);
            Node root = imageMetadata.getAsTree(imageMetadata.getNativeMetadataFormatName());
            NodeList childNodes = root.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeName().equals("tEXt")) {
                    Node textNode = item;
                    NodeList entryNodes = textNode.getChildNodes();
                    for (int j = 0; j < entryNodes.getLength(); j++) {
                        Node entry = entryNodes.item(j);
                        if (entry.getNodeName().equals("tEXtEntry")) {
                            NamedNodeMap attributes = entry.getAttributes();
                            String kw = attributes.getNamedItem("keyword").getNodeValue();
                            String value = attributes.getNamedItem("value").getNodeValue();
                            Pattern p = Pattern.compile("a1810-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)");
                            Matcher matcher = p.matcher(kw);
                            if (matcher.matches()) {
                                Rectangle rect = new Rectangle(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher
                                        .group(3)), Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)));
                                int index = Integer.parseInt(matcher.group(1));
                                Annotation annotation = new Annotation(index, rect);
                                annotation.setText(value);
                                annotations.add(annotation);
                                Collections.sort(annotations, new Comparator<Annotation>() {
                                    public int compare(Annotation o1, Annotation o2) {
                                        return o1.getIndex() - o2.getIndex();
                                    }

                                });
                            }
                        }
                    }
                }
            }
            reader.dispose();
        }
    }

    private class MyListener extends MouseInputAdapter {
        private Point lastMousePress;

        public void mousePressed(MouseEvent e) {
            ImagePanel.this.requestFocusInWindow();
            lastMousePress = e.getPoint();
            newRect = false;
            setSelectedAnnotation(findAnnotation(lastMousePress));
        }

        private Annotation findAnnotation(Point p) {
            Iterator<Annotation> iterator = annotations.iterator();
            while (iterator.hasNext()) {
                Annotation annotation = (Annotation) iterator.next();
                if (annotation.contains(p))
                    return annotation;
            }
            return null;
        }

        public void mouseDragged(MouseEvent e) {
            if (!edit)
                return;
            if (selectedAnnotation == null) {
                setSelectedAnnotation(new Annotation(new Rectangle(lastMousePress)));
                newRect = true;
            }
            if (newRect) {
                updateCurrentRect(e);
                updateSize(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (!edit)
                return;
            if (!newRect)
                return;
            updateCurrentRect(e);
            setSelectedAnnotation(new Annotation(normalize(selectedAnnotation)));
            dirty = true;
            annotations.add(selectedAnnotation);
            if (annotationListener != null)
                annotationListener.annotationAdded(selectedAnnotation);
            newRect = false;
        }

        /*
         * Update the size of the current rectangle and call repaint. Because
         * currentRect always has the same origin, translate it if the width or
         * height is negative.
         * 
         * For efficiency (though that isn't an issue for this program), specify
         * the painting region using arguments to the repaint() call.
         */
        void updateSize(MouseEvent e) {
            updateDrawableRect(getWidth(), getHeight());
            Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
            repaint(totalRepaint.x, totalRepaint.y, totalRepaint.width, totalRepaint.height);
        }

        private void updateCurrentRect(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            selectedAnnotation.setSize(x - selectedAnnotation.x, y - selectedAnnotation.y);
            ImagePanel.this.scrollRectToVisible(selectedAnnotation);
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), 0, 0, image.getWidth(null), image.getHeight(null),
                null);
        // If currentRect exists, paint a box on top.
        if (newRect && selectedAnnotation != null) {
            // Draw a rectangle on top of the image.
            g.setColor(ANNOTATION_COLOR);
            g.fillRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1, rectToDraw.height - 1);

        }
        if (!newRect)
            drawImageHandles(g);
        // depending on image colors
        for (int i = 0; i < annotations.size(); i++) {
            Annotation annotation = (Annotation) annotations.get(i);

            annotation.drawDecoration(g, i, annotation == selectedAnnotation);
        }
    }

    private void drawImageHandles(Graphics g) {
        if (selectedAnnotation != null) {
            g.setColor(Color.BLACK);
            g.drawRect(selectedAnnotation.x, selectedAnnotation.y, selectedAnnotation.width - 1, selectedAnnotation.height - 1);
            int handleX = selectedAnnotation.x - 2;
            int handleY = selectedAnnotation.y - 2;
            handleX = selectedAnnotation.x + selectedAnnotation.width - 2;
            g.drawRect(handleX, handleY, 4, 4);
            handleY = selectedAnnotation.y + selectedAnnotation.height - 2;
            g.drawRect(handleX, handleY, 4, 4);
            handleX = selectedAnnotation.x - 2;
            g.drawRect(handleX, handleY, 4, 4);
        }
    }

    private Rectangle normalize(Rectangle r) {
        r = new Rectangle(r);
        if (r.width < 0) {
            r.width *= -1;
            r.x -= r.width;
        }
        if (r.height < 0) {
            r.height *= -1;
            r.y -= r.height;
        }
        return r;
    }

    private void updateDrawableRect(int compWidth, int compHeight) {
        if (selectedAnnotation == null)
            return;
        int x = selectedAnnotation.x;
        int y = selectedAnnotation.y;
        int width = selectedAnnotation.width;
        int height = selectedAnnotation.height;

        // Make the width and height positive, if necessary.
        if (width < 0) {
            width = 0 - width;
            x = x - width + 1;
            if (x < 0) {
                width += x;
                x = 0;
            }
        }
        if (height < 0) {
            height = 0 - height;
            y = y - height + 1;
            if (y < 0) {
                height += y;
                y = 0;
            }
        }

        // The rectangle shouldn't extend past the drawing area.
        if ((x + width) > compWidth) {
            width = compWidth - x;
        }
        if ((y + height) > compHeight) {
            height = compHeight - y;
        }

        // Update rectToDraw after saving old value.
        if (rectToDraw != null) {
            previousRectDrawn.setBounds(rectToDraw.x, rectToDraw.y, rectToDraw.width, rectToDraw.height);
            rectToDraw.setBounds(x, y, width, height);
        } else {
            rectToDraw = new Rectangle(x, y, width, height);
        }
    }

    public void addAnnotationListener(IAnnotationListener listener) {
        this.annotationListener = listener;

    }

    public ArrayList<Annotation> getAnnotations() {
        return annotations;
    }

    public void setSelectedAnnotation(Annotation selectedAnnotation, boolean fireListener) {
        this.selectedAnnotation = selectedAnnotation;
        if (fireListener && annotationListener != null)
            annotationListener.annotationSelected(selectedAnnotation);
        repaint();
    }

    public void setSelectedAnnotation(Annotation selectedAnnotation) {
        setSelectedAnnotation(selectedAnnotation, true);
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean isDirty() {
        return dirty;
    }
}
