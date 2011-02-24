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
package net.sourceforge.marathon.extensions;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.marathon.action.AbstractScriptElement;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.recorder.AbstractContextMenu;
import net.sourceforge.marathon.recorder.ContextMenuWindow;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Indent;

public class CustomContextMenu extends AbstractContextMenu {

    public static final class RawStringScriptElement extends AbstractScriptElement {
        private static final long serialVersionUID = 1L;
        private final String content;

        private RawStringScriptElement(String content) {
            super(null, null);
            this.content = content;
        }

        public String toScriptCode() {
            return Indent.getIndent() + "# " + content + "\n";
        }

    }

    public CustomContextMenu(ContextMenuWindow window, IRecorder recorder, ComponentFinder finder,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(window, recorder, finder, scriptModel, windowMonitor);
    }

    public Component getContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton button = new JButton("Add Comment");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane pane = new JOptionPane("Comment", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                pane.setWantsInput(true);
                pane.setSelectionValues(null);
                JDialog dialog = pane.createDialog(null, "Comments");
                dialog.setName("### Ignore");
                dialog.setVisible(true);
                if (pane.getInputValue() != null)
                    getRecorder().record(new RawStringScriptElement(pane.getInputValue().toString()));
            }
        });
        panel.add(button);
        return panel;
    }

    public String getName() {
        return "Example";
    }

    public void setComponent(Component component, Point point, boolean isTriggered) {
    }

}
