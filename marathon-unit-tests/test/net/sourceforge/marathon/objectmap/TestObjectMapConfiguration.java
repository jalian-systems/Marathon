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
package net.sourceforge.marathon.objectmap;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

import org.junit.Before;
import org.junit.Test;

public class TestObjectMapConfiguration {

    private ObjectMapConfiguration configuration;
    private ObjectMapNamingStrategy strategy;

    @Before public void setup() {
        configuration = new ObjectMapConfiguration();
        configuration.createDefault();
        strategy = new ObjectMapNamingStrategy();
        strategy.init();
    }

    @Test public void testGetGeneralProperties() {
        List<String> expected = Arrays.asList(new String[] { "labeledBy", "toolTipText", "name", "labelText", "precedingLabel",
                "cText", "iconFile", "position", "size", "accelerator", "enabled", "toolTipText", "fieldName", "layoutData.gridx",
                "layoutData.gridy", "layoutData.x", "layoutData.y", "accessibleContext.accessibleName" });
        List<String> generalProperties = configuration.getGeneralProperties();
        assertEquals(expected.toString(), generalProperties.toString());
    }

    @Test public void testFindNamingProperties() {
        String[] values = new String[] { "labelText", "buttonText", "labeledBy", "accessibleName", "buttonIconFile", "precedingLabel",
                "toolTipText", "name", "fieldName", "actionCommand" };
        List<List<String>> expected = new ArrayList<List<String>>();
        for (String string : values) {
            expected.add(Arrays.asList(new String[] { string }));
        }
        List<List<String>> labelProperties = strategy.findNamingProperties(JLabel.class.getName());
        assertEquals(expected.toString(), labelProperties.toString());
    }

    @Test public void testFindRecognitionProperties() {
        String[] values = new String[] { "name", "fieldName", "actionCommand", "buttonText", "labeledBy", "accessibleName", "buttonIconFile", "precedingLabel", "toolTipText" };
        List<List<String>> expected = new ArrayList<List<String>>();
        for (String string : values) {
            expected.add(Arrays.asList(new String[] { string, "type" }));
        }
        List<List<String>> labelProperties = strategy.findRecognitionProperties(JLabel.class.getName());
        assertEquals(expected.toString(), labelProperties.toString());
    }

    @Test public void testFindContainerNamingProperties() {
        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "title" }));
        expected.add(Arrays.asList(new String[] { "component.class.simpleName" }));
        List<List<String>> labelProperties = strategy.findContainerNamingProperties(JWindow.class.getName());
        assertEquals(expected, labelProperties);

        expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "title", "internalFrameIndex2" }));
        labelProperties = strategy.findContainerNamingProperties(JInternalFrame.class.getName());
        assertEquals(expected.toString(), labelProperties.toString());
    }

    @Test public void testFindContainerRecognitionProperties() {
        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "oMapClassName" }));
        expected.add(Arrays.asList(new String[] { "component.class.name", "title" }));
        expected.add(Arrays.asList(new String[] { "component.class.simpleName" }));
        expected.add(Arrays.asList(new String[] { "component.class.name", "title" }));
        List<List<String>> labelProperties = strategy.findContainerRecognitionProperties(JWindow.class.getName());
        assertEquals(expected.toString(), labelProperties.toString());

        expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "oMapClassName" }));
        expected.add(Arrays.asList(new String[] { "component.class.name", "title" }));
        expected.add(Arrays.asList(new String[] { "component.class.name", "title" }));
        labelProperties = strategy.findContainerRecognitionProperties(JInternalFrame.class.getName());
        assertEquals(expected, labelProperties);
    }

}
