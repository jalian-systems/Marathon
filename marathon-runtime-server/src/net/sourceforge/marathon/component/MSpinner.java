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

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JSpinner;

import net.sourceforge.marathon.recorder.WindowMonitor;

public class MSpinner extends MComponent {
    public MSpinner(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    public JSpinner getSpinner() {
        return (JSpinner) getComponent();
    }

    public String getText() {
        return getEditor().getText();
    }

    public void setText(String text) {
        getEditor().setText(text, true);
    }

    public void click(int numberOfClicks, boolean isPopupTrigger) {
    }

    public MComponent getEditor() {
        JComponent editorComponent = (JComponent) eventQueueRunner.invoke(getSpinner(), "getEditor");
        if (editorComponent == null)
            throw new ComponentException("Null value returned by getEditor() on spinner '" + getMComponentName() + "'", finder.getScriptModel(), windowMonitor);
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            JComponent editor = (JComponent) eventQueueRunner.invoke(getSpinner(), "getEditor");
            editorComponent = ((JSpinner.DefaultEditor) editor).getTextField();
        }
        return finder.getMComponentByComponent(editorComponent, "Doesn't matter", null);
    }

    public boolean keyNeeded(KeyEvent e) {
        return getEditor().keyNeeded(e) && super.keyNeeded(e);
    }
}
