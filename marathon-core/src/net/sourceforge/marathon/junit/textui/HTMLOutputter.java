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
package net.sourceforge.marathon.junit.textui;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

public class HTMLOutputter extends XMLOutputter {
    private Transformer transformer;

    public HTMLOutputter() {
        super();
        TransformerFactory factory = TransformerFactory.newInstance();
        InputStream xsltStream = getClass().getClassLoader().getResourceAsStream("report.xsl");
        try {
            transformer = factory.newTransformer(new StreamSource(xsltStream));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void output(Writer writer, Test testSuite, Map<Test, MarathonTestResult> testOutputMap) {
        StringWriter xmlWriter = new StringWriter();
        super.output(xmlWriter, testSuite, testOutputMap);
        StringReader reader = new StringReader(xmlWriter.toString());
        if (transformer != null) {
            try {
                transformer.transform(new StreamSource(reader), new StreamResult(writer));
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }
}
