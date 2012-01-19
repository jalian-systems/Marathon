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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

public abstract class EscapeDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private boolean defaultActionsEnabled = false;

    public EscapeDialog() {
    }

    public EscapeDialog(Dialog parent, String title, boolean modal) {
        super(parent, title, modal);
    }

    public EscapeDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
    }

    @Override public void setVisible(boolean arg0) {
        if (!defaultActionsEnabled)
            enableDefaultActions();
        super.setVisible(arg0);
    }

    private void enableDefaultActions() {
        setCloseButton(getCloseButton());
        setOKButton(getOKButton());
        defaultActionsEnabled = true;
    }

    private void setOKButton(JButton okButton) {
        if (okButton != null)
            getRootPane().setDefaultButton(okButton);
    }

    private void setCloseButton(final JButton button) {
        if (button == null)
            return;
        Action action = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        };
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", action);
    }

    public abstract JButton getOKButton();

    public abstract JButton getCloseButton();

}
