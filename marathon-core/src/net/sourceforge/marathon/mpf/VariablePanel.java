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
package net.sourceforge.marathon.mpf;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class VariablePanel implements IPropertiesPanel {
    public static final Icon ICON = new ImageIcon(VariablePanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/prop_obj.gif"));
    PropertyTableModel model = new PropertyTableModel();
    private JButton removeButton = null;
    private JButton addButton = null;
    private JButton editButton = null;
    private JTable table = null;
    private JDialog parent = null;
    private JPanel panel;

    public VariablePanel(JDialog parent) {
        this.parent = parent;
    }

    void initComponents() {
        table = new JTable(model);
        addButton = UIUtils.createAddButton();
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PropertyDialog dialog = new PropertyDialog(parent);
                Property property = dialog.getProperty();
                if (property != null) {
                    model.addRow(property);
                }
            }
        });
        editButton = UIUtils.createEditButton();
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                Property property = model.getPropertyAt(index);
                PropertyDialog dialog = new PropertyDialog(parent, property);
                property = dialog.getProperty();
                if (property != null) {
                    model.updateRow(index, property);
                    table.getSelectionModel().setSelectionInterval(index, index);
                }
            }
        });
        editButton.setMnemonic(KeyEvent.VK_D);
        editButton.setEnabled(false);
        removeButton = UIUtils.createRemoveButton();
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1)
                    model.removeRow(selectedRow);
            }
        });
        removeButton.setEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = table.getSelectedRow();
                removeButton.setEnabled(selectedRow != -1);
                editButton.setEnabled(selectedRow != -1);
            }
        });
        table.setPreferredScrollableViewportSize(new Dimension(200, 200));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = table.getSelectedRow();
                if (index == -1 || e.getClickCount() < 2)
                    return;
                editButton.doClick();
            }
        });
    }

    public JPanel createPanel() {
        initComponents();
        JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        PanelBuilder builder = new PanelBuilder(new FormLayout("fill:pref:grow, 3dlu, center:pref:none", "fill:p:grow"));
        builder.setDefaultDialogBorder();
        CellConstraints constraints = new CellConstraints();
        builder.add(scrollPane, constraints.xy(1, 1));
        builder.add(getButtonStackPanel(), constraints.xy(3, 1));
        return builder.getPanel();
    }

    private JPanel getButtonStackPanel() {
        ButtonStackBuilder buttonStack = new ButtonStackBuilder();
        buttonStack.addButtons(new JButton[] { addButton, editButton, removeButton });
        return buttonStack.getPanel();
    }

    public String getName() {
        return "Properties";
    }

    public Icon getIcon() {
        return ICON;
    }

    public void getProperties(Properties props) {
        model.getProperties(props);
    }

    public void setProperties(Properties props) {
        model.setProperties(props);
    }

    public boolean isValidInput() {
        return true;
    }

    public JPanel getPanel() {
        if (panel == null)
            panel = createPanel();
        return panel;
    }
}
