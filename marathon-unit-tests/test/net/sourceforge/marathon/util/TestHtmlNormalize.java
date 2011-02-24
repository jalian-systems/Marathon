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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class TestHtmlNormalize {
    @Test
    public void testNomalize() throws Exception {
        String a = "foo";
        String b = "bar";
        assertEquals(getNormalizedHtml(a, false), getNormalizedHtml(a, true));
        assertNotSame(getNormalizedHtml(a, false), getNormalizedHtml(b, false));
    }

    @Test
    public void testEmptyString() throws Exception {
        assertEquals("", HtmlNormalize.normalize(""));
        assertEquals("", HtmlNormalize.normalize("  "));
    }

    @Test
    public void testSimpleString() throws Exception {
        assertEquals(HtmlNormalize.normalize("hello"), "hello");
    }

    private String getNormalizedHtml(String text, boolean switchAttribute) {
        return HtmlNormalize.normalize(getHTML(text, switchAttribute));
    }

    private String getHTML(String text, boolean switchAttribute) {
        String attributes = (switchAttribute) ? "color='red' size='24'" : " size='24' color='red'";
        return "<html><head></head><body><font " + attributes + ">" + text + "</font></body></html>";
    }
}
