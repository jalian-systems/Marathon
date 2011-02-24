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

import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.HtmlNormalize;

public class MEditorPane extends MTextComponent {
    // This is to store the value for links when the editor type is text/html
    private int linkPosition;
    private String hRef;
    private char SEPARATER = ',';

    public MEditorPane(JEditorPane editor, String name, Object obj, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(editor, name, finder, windowMonitor);
        if (!isHtmlDocument())
            return;
        if (obj instanceof Point) {
            Point pt = (Point) obj;
            int location = eventQueueRunner.invokeInteger(editor, "viewToModel", new Object[] { pt }, new Class[] { Point.class });
            Document document = (Document) eventQueueRunner.invoke(editor, "getDocument");
            final String hRef = getHRef(location, document);
            boolean editable = eventQueueRunner.invokeBoolean(editor, "isEditable");
            if (!editable && location > 0 && isLink(hRef)) {
                // get to the first location
                while (isLink(--location, document))
                    ;
                linkPosition = ++location;
                this.hRef = hRef;
            }
        } else if (obj instanceof String) {
            parseLastClickSpec((String) obj);
        }
    }

    public String getComponentInfo() {
        String result = (linkPosition > 0) ? "" + linkPosition : null;
        if (hRef != null && !hRef.equals(""))
            result = hRef + SEPARATER + result;
        return result;
    }

    // This extends the click function by specifying where in the JEditorPanel
    // to click
    public void click(int numberOfClicks, boolean isPopupTrigger) {
        if (!isHtmlDocument()) {
            super.click(numberOfClicks, isPopupTrigger);
        } else {
            swingWait();
            FireableMouseClickEvent event = new FireableMouseClickEvent(getComponent(), numberOfClicks, isPopupTrigger);
            Point p = null;
            Rectangle rect = (Rectangle) eventQueueRunner.invoke(getEditor(), "modelToView", new Object[] { new Integer(
                    linkPosition) }, new Class[] { Integer.TYPE });
            p = rect.getLocation();
            event.fire(p, numberOfClicks);
            swingWait();
        }
    }

    public String getHRef(int pos, Document doc) {
        if (!(doc instanceof HTMLDocument))
            return null;
        HTMLDocument hdoc = (HTMLDocument) doc;
        Element e = hdoc.getCharacterElement(pos);
        AttributeSet anchor = (AttributeSet) e.getAttributes().getAttribute(HTML.Tag.A);
        if (anchor == null)
            return null;
        String href = (String) anchor.getAttribute(HTML.Attribute.HREF);
        return href;
    }

    /**
     * Use after getHRef or call the one with pos and doc as input
     * 
     * @param href
     * @return
     */
    public boolean isLink(String href) {
        return href != null;
    }

    public boolean isLink(int pos, Document doc) {
        return isLink(getHRef(pos, doc));
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

    private JEditorPane getEditor() {
        return (JEditorPane) getComponent();
    }

    private void parseLastClickSpec(String spec) {
        try {
            int index = spec.lastIndexOf(SEPARATER);
            if (index >= 0) {
                hRef = spec.substring(0, index).trim();
                spec = spec.substring(index + 1).trim();
            }
            linkPosition = Integer.parseInt(spec);
        } catch (Exception e) {
            throw new ComponentException("extra info for last click is invalid : " + spec, finder.getScriptModel(), windowMonitor);
        }
    }
}
