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

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

import javax.swing.JComponent;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MCellComponent;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class DragAndDropAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private ComponentId source;
    private ComponentId target;
    private int action;

    public DragAndDropAction(ComponentId source, ComponentId target, int action, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(source, scriptModel, windowMonitor);
        this.source = source;
        this.target = target;
        this.action = action;
    }

    public DragAndDropAction(ComponentId source, ComponentId target, String action, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        this(source, target, toActionID(action), scriptModel, windowMonitor);
    }

    private static int toActionID(String action) {
        if (action.equals("copy"))
            return DnDConstants.ACTION_COPY;
        else if (action.equals("move"))
            return DnDConstants.ACTION_MOVE;
        else if (action.equals("link"))
            return DnDConstants.ACTION_LINK;
        else
            throw new RuntimeException("Unknown action " + action + " in a call to DragAndDrop");
    }

    public void play(ComponentFinder resolver) {
        MComponent mSource = resolver.getMComponentById(source);
        MComponent mTarget = resolver.getMComponentById(target);
        if (!(mSource.getComponent() instanceof JComponent) || !(mTarget.getComponent() instanceof JComponent)) {
            throw new RuntimeException("DragAndDrop: can't handle non JComponents");
        }
        JComponent jSource = getTransferable(mSource.getComponent(), mSource.getMComponentName());
        JComponent jTarget = getTransferable(mTarget.getComponent(), mTarget.getMComponentName());
        Clipboard clip = new Clipboard("DnD");
        jSource.getTransferHandler().exportToClipboard(jSource, clip, action);
        if (mTarget instanceof MCellComponent)
            ((MCellComponent) mTarget).setCurrentSelection();
        Transferable contents = clip.getContents(jTarget);
        if(contents == null)
            throw new RuntimeException("Can't get contents from the clipboard");
        jTarget.getTransferHandler().importData(jTarget, contents);
    }

    private JComponent getTransferable(Component component, String name) {
        if (component == null) {
            throw new RuntimeException("Can't find transfer handler for component " + name);
        }
        if (component instanceof JComponent && ((JComponent) component).getTransferHandler() != null)
            return ((JComponent) component);
        return getTransferable(component.getParent(), name);
    }

    public String toScriptCode() {
        ComponentId source2 = source;
        ComponentId target2 = target;
        int action2 = action;
        return scriptModel.getScriptCodeForDragAndDrop(source2, target2, action2);
    }

}
