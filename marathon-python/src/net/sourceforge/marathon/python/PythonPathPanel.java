/*******************************************************************************
 *  
 *  $Id: PythonPathPanel.java 175 2008-12-22 10:07:39Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
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
package net.sourceforge.marathon.python;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.marathon.mpf.ISubPropertiesPanel;
import net.sourceforge.marathon.mpf.ListPanel;
import net.sourceforge.marathon.util.TextPrompt;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PythonPathPanel extends ListPanel implements ISubPropertiesPanel {
    private JTextField home = new JTextField();

    public PythonPathPanel(JDialog parent) {
        super(parent, true);
        this.parent = parent;
        TextPrompt prompt = new TextPrompt("(Bundled Jython)", home);
        prompt.changeAlpha((float) 0.5);
    }

    public static final Icon _icon = new ImageIcon(PythonPathPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/cp_obj.gif"));

    public String getName() {
        return "Python Path";
    }

    public Icon getIcon() {
        return _icon;
    }

    public String getPropertyKey() {
        return PythonScript.PROP_APPLICATION_PYTHONPATH;
    }

    public boolean isAddArchivesNeeded() {
        return false;
    }

    public boolean isAddClassesNeeded() {
        return false;
    }

    public boolean isAddFoldersNeeded() {
        return true;
    }

    public boolean isValidInput() {
        if (home.getText().equals(""))
            return true;
        File jar = new File(home.getText(), "jython.jar");
        if (jar.exists())
            return true;
        jar = new File(home.getText(), "jython-complete.jar");
        if (jar.exists())
            return true;
        int r = JOptionPane.showConfirmDialog(parent,
                "Could not find jython.jar or jython-complete.jar in given Home directory. Do you want to continue?",
                "Jython Home", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            home.requestFocusInWindow();
            return false;
        }
        return true;
    }

    public void setProperties(Properties props) {
        super.setProperties(props);
        home.setText(props.getProperty(PythonScript.PROP_APPLICATION_PYTHONHOME, ""));
    }

    public void getProperties(Properties props) {
        super.getProperties(props);
        props.setProperty(PythonScript.PROP_APPLICATION_PYTHONHOME, home.getText());
    }

    protected PanelBuilder getBuilder() {
        PanelBuilder builder = super.getBuilder();
        builder.appendRow("pref");
        CellConstraints constraints = new CellConstraints();
        builder.add(getHomePanel(), constraints.xyw(1, 2, 3));
        return builder;
    }

    private JPanel getHomePanel() {
        FormLayout layout = new FormLayout("pref, 3dlu, fill:pref:grow", "fill:p:grow, 3dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints constraints = new CellConstraints();
        builder.addLabel("P&ython Home:", new CellConstraints().xyw(1, 3, 1), home, constraints.xyw(3, 3, 1));
        return builder.getPanel();
    }

    public int getMnemonic() {
        return KeyEvent.VK_Y;
    }

}
