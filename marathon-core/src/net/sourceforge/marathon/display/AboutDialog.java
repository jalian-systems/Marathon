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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.marathon.Version;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AboutDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private JButton okButton;

    public AboutDialog() {
    }

    public void display() {
        setResizable(false);
        setModal(true);
        FormLayout layout = new FormLayout("pref", "fill:pref, pref, 3dlu, pref, 3dlu, pref, pref, pref, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        JLabel image = new JLabel(SplashScreen.SPLASH);
        image.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        CellConstraints constraints = new CellConstraints();
        builder.add(image, constraints.xy(1, 1));
        JPanel versionPanel = new JPanel();
        versionPanel.setAlignmentX(0.5f);
        JLabel version = new JLabel("Version: " + Version.id() + " Build: " + Version.build());
        version.setFont(version.getFont().deriveFont(11.0f));
        versionPanel.add(version);
        builder.add(versionPanel, constraints.xy(1, 2));
        builder.addSeparator(Version.blurbTitle(), constraints.xy(1, 4));
        builder.add(new JLabel("    " + Version.blurbCompany()), constraints.xy(1, 6));
        builder.add(new JLabel("    " + Version.blurbWebsite()), constraints.xy(1, 7));
        builder.add(new JLabel("    " + Version.blurbCredits()), constraints.xy(1, 8));
        JButton creditsButton = UIUtils.createCreditsButton();
        creditsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CreditsDialog(AboutDialog.this).setVisible(true);
            }
        });
        okButton = UIUtils.createOKButton();
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bbuilder = ButtonBarFactory.buildRightAlignedBar(creditsButton, okButton);
        bbuilder.setBackground(new Color(255, 255, 255));
        builder.getPanel().setBackground(new Color(255, 255, 255));
        builder.add(bbuilder, constraints.xy(1, 9));
        getContentPane().add(builder.getPanel());
        pack();
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((size.width - getSize().width) / 2, (size.height - getSize().height) / 2);
        setVisible(true);
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return okButton;
    }

}
