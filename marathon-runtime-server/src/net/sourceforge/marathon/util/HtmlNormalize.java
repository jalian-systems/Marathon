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
package net.sourceforge.marathon.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.TreeMap;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HtmlNormalize extends DefaultHandler {
    public final String INDENT = "    "; // Amount
                                         // to
                                         // indent
    public final String NEW_LINE = System.getProperty("line.separator");
    private int indentLevel = 0;
    private StringBuffer out = new StringBuffer(100);

    private HtmlNormalize() {
    }

    public static String normalize(String html) {
        if ("".equals(html.trim()))
            return "";
        HtmlNormalize handler = new HtmlNormalize();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(new StringReader(html)), handler);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Marathon internal error: Unable to setup the xml parser when try to normalize html");
        } catch (SAXException e) {
            return html;
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw new RuntimeException("Marathon internal error: Unable to setup the xml parser when try to normalize html");
        } catch (IOException e) {
            throw new RuntimeException("Marathon internal error: Unable to setup the xml parser when try to normalize html");
        }
        return handler.getResult();
    }

    public String getResult() {
        return out.append("\n").toString();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        indentLevel++;
        outputNewLine();
        String elementName = ("".equals(localName)) ? qName : localName;
        out.append("<" + elementName);
        if (attributes != null) {
            SortedAttributes sorted = new SortedAttributes(attributes);
            String name, value;
            for (Iterator<String> i = sorted.getNames(); i.hasNext();) {
                name = (String) i.next();
                value = sorted.getValue(name);
                out.append(" " + name + "=\"");
                out.append(value + "\"");
            }
        }
        out.append(">");
    }

    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        outputNewLine();
        indentLevel--;
        String elementName = ("".equals(sName)) ? qName : sName;
        out.append("</" + elementName + ">");
    }

    public void characters(char buf[], int offset, int len) throws SAXException {
        String s = new String(buf, offset, len);
        if (!s.trim().equals("")) {
            indentLevel++;
            outputNewLine();
            indentLevel--;
            out.append(s.trim());
        }
    }

    private void outputNewLine() {
        out.append(NEW_LINE);
        for (int i = 0; i < indentLevel; i++)
            out.append(INDENT);
    }

    static class SortedAttributes {
        TreeMap<String, String> _store = new TreeMap<String, String>();

        public SortedAttributes(Attributes attributes) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String key = attributes.getLocalName(i);
                if ("".equals(key))
                    key = attributes.getQName(i);
                String value = attributes.getValue(i);
                _store.put(key, value);
            }
        }

        public Iterator<String> getNames() {
            return _store.keySet().iterator();
        }

        public String getValue(String key) {
            return (String) _store.get(key);
        }
    }
}
