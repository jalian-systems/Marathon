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
package net.sourceforge.marathon.component;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLDocument.Iterator;

import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.HtmlNormalize;

public class MEditorPane extends MTextComponent {
    // This is to store the value for links when the editor type is text/html
    private int linkPosition;
    private String hRef;
    private String text;
    private char SEPARATER = ',';
    private int hRefIndex;
    private int textIndex;

    public MEditorPane(JEditorPane editor, String name, Object obj, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(editor, name, finder, windowMonitor);
        if (!isHtmlDocument())
            return;
        if (obj instanceof Point) {
            Point pt = (Point) obj;
            int location = eventQueueRunner.invokeInteger(editor, "viewToModel", new Object[] { pt }, new Class[] { Point.class });
            Document document = (Document) eventQueueRunner.invoke(editor, "getDocument");
            setHRef(location, document);
            linkPosition = location;
        } else if (obj instanceof String) {
            parseLastClickSpec((String) obj);
        }
    }

    public String getComponentInfo() {
        if (text != null && !"".equals(text)) {
            return "text=" + text + (textIndex > 0 ? "(" + textIndex + ")" : "");
        }
        if (hRef != null && !"".equals(hRef)) {
            return "link=" + hRef + (hRefIndex > 0 ? "(" + hRefIndex + ")" : "");
        }
        String result = (linkPosition > 0) ? "" + linkPosition : null;
        if (hRef != null && !hRef.equals(""))
            result = hRef + SEPARATER + result;
        return result;
    }

    @Override public void click(int numberOfClicks, int modifiers, Point position) {
        if (!isHtmlDocument() || position != null) {
            super.click(numberOfClicks, modifiers, position);
            return;
        }
        swingWait();
        Point p = null;
        Rectangle rect = (Rectangle) eventQueueRunner.invoke(getEditor(), "modelToView",
                new Object[] { Integer.valueOf(linkPosition) }, new Class[] { Integer.TYPE });
        if (rect == null)
            return;
        p = rect.getLocation();
        if(SwingUtilities.isEventDispatchThread()) {
            getEditor().setCaretPosition(linkPosition);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        getEditor().setCaretPosition(linkPosition);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        new FireableMouseClickEvent(getComponent(), numberOfClicks, (modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0).fire(p,
                numberOfClicks, modifiers);
        swingWait();
    }

    public void setHRef(int pos, Document doc) {
        hRef = null;
        text = null;
        if (!(doc instanceof HTMLDocument))
            return;
        HTMLDocument hdoc = (HTMLDocument) doc;
        Iterator iterator = hdoc.getIterator(HTML.Tag.A);
        while (iterator.isValid()) {
            if (pos >= iterator.getStartOffset() && pos <= iterator.getEndOffset()) {
                AttributeSet attributes = iterator.getAttributes();
                if (attributes != null && attributes.getAttribute(HTML.Attribute.HREF) != null) {
                    try {
                        text = hdoc.getText(iterator.getStartOffset(), iterator.getEndOffset() - iterator.getStartOffset()).trim();
                        hRef = attributes.getAttribute(HTML.Attribute.HREF).toString();
                        setIndexOfHrefAndText(hdoc, pos, text, hRef);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            iterator.next();
        }
    }

    private void setIndexOfHrefAndText(HTMLDocument hdoc, int pos, String text, String hRef) {
        this.hRefIndex = 0;
        this.textIndex = 0;
        Iterator iterator = hdoc.getIterator(HTML.Tag.A);
        while (iterator.isValid()) {
            if (pos >= iterator.getStartOffset() && pos <= iterator.getEndOffset()) {
                return;
            } else {
                AttributeSet attributes = iterator.getAttributes();
                if (attributes != null && attributes.getAttribute(HTML.Attribute.HREF) != null) {
                    try {
                        String t = hdoc.getText(iterator.getStartOffset(), iterator.getEndOffset() - iterator.getStartOffset())
                                .trim();
                        String h = attributes.getAttribute(HTML.Attribute.HREF).toString();
                        if (t.equals(text))
                            this.textIndex++;
                        if (h.equals(hRef))
                            this.hRefIndex++;
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
            iterator.next();
        }
    }

    public boolean isHtmlDocument() {
        return "text/html".equalsIgnoreCase((String) eventQueueRunner.invoke(getEditor(), "getContentType"));
    }

    /**
     * @return HTMLDocument so that you can compare in html context
     */
    public Object getComparableObject() {
        // Compare as normalized text if it's not html document
        if (!isHtmlDocument())
            return super.getComparableObject();
        // Is HTML document, return HTMLDocument for comparison
        return HtmlNormalize.normalize((String) eventQueueRunner.invoke(getEditor(), "getText"));
    }

    /**
     * @return HTMLDocument so that you can compare in html context
     */
    public Object getComparableObject(String text) {
        // Compare as normalized text if it's not html document
        if (!isHtmlDocument())
            return super.getComparableObject(text);
        return HtmlNormalize.normalize(text);
    }

    public JEditorPane getEditor() {
        return (JEditorPane) getComponent();
    }

    private void parseLastClickSpec(String spec) {
        if (spec.startsWith("text="))
            searchAsText(spec.substring(5), true);
        else if (spec.startsWith("link="))
            searchAsText(spec.substring(5), false);
        else {
            try {
                int index = spec.lastIndexOf(SEPARATER);
                if (index >= 0) {
                    hRef = spec.substring(0, index).trim();
                    spec = spec.substring(index + 1).trim();
                }
                linkPosition = Integer.parseInt(spec);
            } catch (Exception e) {
                throw new ComponentException("extra info for last click is invalid : " + spec, finder.getScriptModel(),
                        windowMonitor);
            }
        }
    }

    private void searchAsText(String spec, boolean isText) {
        Document document = (Document) eventQueueRunner.invoke(getEditor(), "getDocument");
        hRef = null;
        text = null;
        hRefIndex = 0;
        textIndex = 0;
        int lastIndexOf = spec.lastIndexOf('(');
        if (lastIndexOf != -1) {
            if (isText) {
                textIndex = Integer.parseInt(spec.substring(lastIndexOf + 1, spec.length() - 1));
            } else {
                hRefIndex = Integer.parseInt(spec.substring(lastIndexOf + 1, spec.length() - 1));
            }
            spec = spec.substring(0, lastIndexOf);
        }
        if (!(document instanceof HTMLDocument))
            return;
        HTMLDocument hdoc = (HTMLDocument) document;
        Iterator iterator = hdoc.getIterator(HTML.Tag.A);
        int curIndex = 0;
        while (iterator.isValid()) {
            String t;
            AttributeSet attributes = iterator.getAttributes();
            try {
                if (isText)
                    t = hdoc.getText(iterator.getStartOffset(), iterator.getEndOffset() - iterator.getStartOffset());
                else
                    t = attributes.getAttribute(HTML.Attribute.HREF).toString();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
                return;
            }
            if (t.contains(spec) && ((isText && curIndex++ == textIndex) || (!isText && curIndex++ == hRefIndex))) {
                if (attributes != null && attributes.getAttribute(HTML.Attribute.HREF) != null) {
                    try {
                        text = hdoc.getText(iterator.getStartOffset(), iterator.getEndOffset() - iterator.getStartOffset()).trim();
                        hRef = attributes.getAttribute(HTML.Attribute.HREF).toString();
                        linkPosition = (iterator.getStartOffset() + iterator.getEndOffset()) / 2;
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            iterator.next();
        }
        throw new ComponentException("extra info for last click is invalid : " + spec, finder.getScriptModel(), windowMonitor);
    }
}
