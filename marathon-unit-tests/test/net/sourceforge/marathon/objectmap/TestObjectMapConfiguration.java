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

    @Before public void setup() {
        configuration = new ObjectMapConfiguration();
        configuration.createDefault();
    }

    @Test public void testGetGeneralProperties() {
        List<String> expected = Arrays.asList(new String[] { "position", "size", "accelerator", "enabled", "toolTipText",
                "fieldName", "layoutData.gridx", "layoutData.gridy", "layoutData.x", "layoutData.y" });
        List<String> generalProperties = configuration.getGeneralProperties();
        assertEquals(expected, generalProperties);
    }

    @Test public void testFindNamingProperties() {
        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "name" }));
        expected.add(Arrays.asList(new String[] { "labelText" }));
        expected.add(Arrays.asList(new String[] { "labeledBy" }));
        expected.add(Arrays.asList(new String[] { "precedingLabel" }));
        expected.add(Arrays.asList(new String[] { "fieldName" }));
        expected.add(Arrays.asList(new String[] { "type", "indexInContainer" }));
        List<List<String>> labelProperties = configuration.findNamingProperties(new JLabel());
        assertEquals(expected.toString(), labelProperties.toString());
    }

    @Test public void testFindRecognitionProperties() {
        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "name", "type" }));
        expected.add(Arrays.asList(new String[] { "fieldName", "type" }));
        expected.add(Arrays.asList(new String[] { "precedingLabel", "type" }));
        expected.add(Arrays.asList(new String[] { "labelText", "type" }));
        expected.add(Arrays.asList(new String[] { "labeledBy", "type" }));
        expected.add(Arrays.asList(new String[] { "type", "indexInContainer" }));
        List<List<String>> labelProperties = configuration.findRecognitionProperties(new JLabel());
        assertEquals(expected, labelProperties);
    }

    @Test public void testFindContainerNamingProperties() {
        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "title" }));
        List<List<String>> labelProperties = configuration.findContainerNamingProperties(new JWindow());
        assertEquals(expected, labelProperties);

        expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "title", "internalFrameIndex" }));
        labelProperties = configuration.findContainerNamingProperties(new JInternalFrame());
        assertEquals(expected.toString(), labelProperties.toString());
    }

    @Test public void testFindContainerRecognitionProperties() {
        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "oMapClassName" }));
        expected.add(Arrays.asList(new String[] { "component.class.name", "title" }));
        List<List<String>> labelProperties = configuration.findContainerRecognitionProperties(new JWindow());
        assertEquals(expected, labelProperties);

        expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList(new String[] { "oMapClassName" }));
        expected.add(Arrays.asList(new String[] { "component.class.name", "title" }));
        labelProperties = configuration.findContainerRecognitionProperties(new JInternalFrame());
        assertEquals(expected, labelProperties);
    }

}
