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
package net.sourceforge.marathon.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.marathon.mpf.BannerPanel;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FixtureSelectionDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private JList fixtureList;
    protected boolean isOKSelected = false;
    private JButton okButton = UIUtils.createSelectButton();
    private JButton cancelButton = UIUtils.createCancelButton();
    private String selectedFixture;

    public FixtureSelectionDialog(JFrame parent, String[] fixtures, final String selectedFixture) {
        super(parent, "Select", true);
        setTitle("Marathon - Select Fixture");
        BannerPanel bannerPanel = new BannerPanel();
        String[] lines = { "Fixtures allows to customize the setup to be done for a test case",
                "Note that you need to create a new testcase for this fixture to be used", "for recording" };
        BannerPanel.Sheet sheet = new BannerPanel.Sheet("Select Fixture", lines);
        bannerPanel.addSheet(sheet, "main");
        getContentPane().add(bannerPanel, BorderLayout.NORTH);

        getContentPane().add(getFixturePanel(fixtures), BorderLayout.CENTER);
        this.selectedFixture = selectedFixture;
        fixtureList.setSelectedValue(selectedFixture, true);

        JPanel buttonPanel = ButtonBarFactory.buildRightAlignedBar(new JButton[] { okButton, cancelButton });
        buttonPanel.setBorder(Borders.createEmptyBorder("0dlu, 0dlu, 3dlu, 7dlu"));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSelectedFixture((String) fixtureList.getSelectedValue());
                dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pack();
        setWindowInCenter();
    }

    private void setWindowInCenter() {
        Dimension size = getParent().getSize();
        Dimension oursize = getSize();
        if (oursize.height > size.height || oursize.width > size.width) {
            setLocationRelativeTo(getParent());
            return;
        }

        Point newLocation = new Point((size.width - oursize.width) / 2, (size.height - oursize.height) / 2);
        setLocation(newLocation);
    }

    private JPanel getFixturePanel(String[] fixtures) {
        PanelBuilder builder = new PanelBuilder(new FormLayout("left:p:none, 3dlu, fill:p:grow", "fill:p:grow"));
        builder.setDefaultDialogBorder();
        CellConstraints constraints = new CellConstraints();
        fixtureList = getFixtureList(fixtures);
        builder.add(new JScrollPane(fixtureList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), constraints.xy(3, 1));
        return builder.getPanel();
    }

    private JList getFixtureList(String[] fixtures) {
        final JList list = new JList(fixtures);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    setSelectedFixture((String) fixtureList.getSelectedValue());
                    dispose();
                }
            }
        });
        list.setVisibleRowCount(10);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                list.ensureIndexIsVisible(list.getSelectedIndex());
            }
        });
        return list;
    }

    public String getSelectedFixture() {
        return selectedFixture;
    }

    public void setSelectedFixture(String selectedFixture) {
        this.selectedFixture = selectedFixture;
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}
