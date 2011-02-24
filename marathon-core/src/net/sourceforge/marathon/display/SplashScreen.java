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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.sourceforge.marathon.Version;

public class SplashScreen extends JDialog {
    public static final Icon SPLASH = new ImageIcon(SplashScreen.class.getClassLoader().getResource(
            "net/sourceforge/marathon/display/images/marathon.png"));

    private static final long serialVersionUID = 1L;
    private static final int SPLASH_DISPLAY_TIME = 2000;

    public SplashScreen() {
        setUndecorated(true);
        setModal(true);
        try {
            getContentPane().add(new JLabel(SPLASH), BorderLayout.NORTH);
            JPanel versionPanel = new JPanel();
            versionPanel.setAlignmentX(0.5f);
            JLabel version = new JLabel(Version.product() + " Version: " + Version.id() + " Build: " + Version.build());
            version.setFont(version.getFont().deriveFont(11.0f));
            versionPanel.add(version);
            getContentPane().add(versionPanel, BorderLayout.SOUTH);
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
        centerScreen();
        Timer timer = new Timer(SPLASH_DISPLAY_TIME, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        setVisible(true);
    }

    private void centerScreen() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
    }
}
