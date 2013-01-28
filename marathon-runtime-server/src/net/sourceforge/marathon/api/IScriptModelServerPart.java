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
package net.sourceforge.marathon.api;

import java.awt.Point;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.KeyStroke;

import net.sourceforge.marathon.action.WindowState;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;

public interface IScriptModelServerPart {

    public abstract String getScriptCodeForAssertContent(ComponentId componentId, String[][] content);

    public abstract String getScriptCodeForAssertProperty(ComponentId componentId, String property, String value);

    public abstract String getScriptCodeForClick(ComponentId componentId, int numberOfClicks, int modifiers, int record_click,
            Point position);

    public abstract String getScriptCodeForDragAndDrop(ComponentId source, ComponentId target, int action);

    public abstract String getScriptCodeForKeystroke(char keyChar2, KeyStroke keyStroke2, ComponentId componentId,
            String textForKeyChar);

    public abstract String getScriptCodeForCapture(String windowName, String fileName);

    public abstract String getScriptCodeForSelect(ComponentId componentId, String text);

    public abstract String getScriptCodeForSelectMenu(KeyStroke ks, ArrayList<Object> menuList);

    public abstract String getScriptCodeForWaitProperty(ComponentId componentId, String property, String value);

    public abstract String getScriptCodeForWindowClosing(WindowId id);

    public abstract String getScriptCodeForWindowState(WindowId id, WindowState state);

    public abstract String getScriptCodeForCustom(ComponentId componentId, String string, Object[] objects);

    public abstract String getScriptCodeForDrag(int modifiers, Point start, Point end, ComponentId componentId);

    public abstract IScript getScript(Writer out, Writer err, String script, String filename, ComponentFinder resolver,
            boolean isDebugging, WindowMonitor windowMonitor, MarathonAppType type);

    public abstract String getFunctionCallForInsertDialog(Function f, String[] arguments);

    public abstract String[][] getCustomAssertions(IScript script, MComponent mcomponent);

    public abstract String getJavaRecordedVersionTag();

}