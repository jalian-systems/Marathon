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
package net.sourceforge.marathon.action;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MCollectionComponent;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class AssertContent extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private String[][] content;

    public AssertContent(ComponentId componentId, String[][] content, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(componentId, scriptModel, windowMonitor);
        this.content = content;
    }

    public void play(ComponentFinder resolver) {
        MComponent c = resolver.getMComponentById(getComponentId());
        assertTrue("Invoking assertContent on a non-collection component", c instanceof MCollectionComponent);
        MCollectionComponent component = (MCollectionComponent) c;
        assertEquals("Invalid Length", Integer.valueOf(content.length), Integer.valueOf(component.getContent().length));
        String[][] actualData = component.getContent();
        for (int i = 0; i < actualData.length; i++) {
            String[] actualElements = actualData[i];
            String[] expectedElements = content[i];
            assertEquals("Invalid Length at index " + i, Integer.valueOf(expectedElements.length), Integer.valueOf(actualElements.length));
            for (int j = 0; j < expectedElements.length; j++) {
                if (expectedElements[j] != null && expectedElements[j].length() != 0)
                    assertEquals("Data Mismatch at (" + i + "," + j + ") ", expectedElements[j], actualElements[j]);
            }
        }
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForAssertContent(getComponentId(), content);
    }

}
