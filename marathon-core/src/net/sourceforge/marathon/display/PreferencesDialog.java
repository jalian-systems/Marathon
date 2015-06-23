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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.junit.TestCreator;
import net.sourceforge.marathon.mpf.BannerPanel;
import net.sourceforge.marathon.navigator.Navigator;
import net.sourceforge.marathon.util.ContextMenuTriggers;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PreferencesDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private JTextField mouseTrigger = new JTextField(15);
    private JButton mouseTriggerClick = new JButton("Click here");
    private JTextField keyTrigger = new JTextField(15);
    private JTextField hideFilesNavigator = new JTextField(15);
    private JTextField hideFilesJUnit = new JTextField(15);
    private JCheckBox doNotHideMarathonITEBlurbs = new JCheckBox();
    private JButton okButton = UIUtils.createOKButton();
    private JButton cancelButton = UIUtils.createCancelButton();
    private JButton defaultsButton = UIUtils.createLoadDefaultsButton();
    private Preferences prefs = Preferences.userNodeForPackage(Constants.class);
    private JFrame parent;
    private boolean needRefresh = true;

    public PreferencesDialog(JFrame parent) {
        super(parent, "Preferences", true);
        this.parent = parent;
        setTitle("Preferences");
        setModal(true);
        BannerPanel bannerPanel = new BannerPanel();
        String[] lines = { "Set marathon preferences" };
        BannerPanel.Sheet sheet = new BannerPanel.Sheet("Preferences", lines);
        bannerPanel.addSheet(sheet, "main");
        getContentPane().add(bannerPanel, BorderLayout.NORTH);
        getContentPane().add(getPreferencesPanel());
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                needRefresh = false;
                dispose();
            }
        });
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prefs.put(Constants.PREF_RECORDER_MOUSE_TRIGGER, mouseTrigger.getText());
                prefs.put(Constants.PREF_RECORDER_KEYBOARD_TRIGGER, keyTrigger.getText());
                prefs.put(Constants.PREF_NAVIGATOR_HIDEFILES, hideFilesNavigator.getText());
                prefs.put(Constants.PREF_JUNIT_HIDEFILES, hideFilesJUnit.getText());
                prefs.put(Constants.PREF_ITE_BLURBS, Boolean.toString(doNotHideMarathonITEBlurbs.isSelected()));
                try {
                    prefs.flush();
                } catch (BackingStoreException e1) {
                    JOptionPane.showMessageDialog(PreferencesDialog.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
                System.setProperty(Constants.PROP_RECORDER_KEYTRIGGER, keyTrigger.getText());
                System.setProperty(Constants.PROP_RECORDER_MOUSETRIGGER, mouseTrigger.getText());
                ContextMenuTriggers.setContextMenuKey();
                ContextMenuTriggers.setContextMenuModifiers();
                Navigator.setHideFilePattern(hideFilesNavigator.getText());
                TestCreator.setHideFilePattern(hideFilesJUnit.getText());
                dispose();
            }
        });
        defaultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.clear();
                    prefs.flush();
                } catch (BackingStoreException e1) {
                    JOptionPane.showMessageDialog(PreferencesDialog.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
                mouseTrigger.setText(OSUtils.inputEventGetModifiersExText(ContextMenuTriggers.getContextMenuModifiers()));
                keyTrigger.setText(OSUtils.inputEventGetModifiersExText(ContextMenuTriggers.getContextMenuKeyModifiers()) + "+"
                        + OSUtils.keyEventGetKeyText(ContextMenuTriggers.getContextMenuKeyCode()));
                System.setProperty(Constants.PROP_RECORDER_KEYTRIGGER, keyTrigger.getText());
                System.setProperty(Constants.PROP_RECORDER_MOUSETRIGGER, mouseTrigger.getText());
                ContextMenuTriggers.setContextMenuKey();
                ContextMenuTriggers.setContextMenuModifiers();
                Navigator.setHideFilePattern(null);
                TestCreator.setHideFilePattern(null);
                hideFilesJUnit.setText(TestCreator.getHideFilePattern());
                hideFilesNavigator.setText(Navigator.getHideFilePattern());
                doNotHideMarathonITEBlurbs.setSelected(false);
            }
        });
        JPanel buttonPanel = ButtonBarFactory.buildRightAlignedBar(new JButton[] { defaultsButton, okButton, cancelButton });
        buttonPanel.setBorder(Borders.createEmptyBorder("0dlu, 0dlu, 3dlu, 7dlu"));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setResizable(false);
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

    public void dispose() {
        super.dispose();
        if (parent == null)
            System.exit(0);
    }

    private JPanel getPreferencesPanel() {
        PanelBuilder builder = new PanelBuilder(new FormLayout("left:p:none, 3dlu, pref, 3dlu, pref",
                "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
        builder.setDefaultDialogBorder();
        CellConstraints constraints = new CellConstraints();
        builder.addLabel("Mouse Trigger:", constraints.xy(1, 1));
        mouseTrigger.setToolTipText("Mouse trigger to popup the Recorder assert menu");
        mouseTrigger.setEditable(false);
        mouseTrigger.setBackground(Color.GRAY);
        mouseTrigger.setText(OSUtils.inputEventGetModifiersExText(ContextMenuTriggers.getContextMenuModifiers()));
        builder.add(mouseTrigger, constraints.xy(3, 1));
        mouseTriggerClick.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseTrigger.setText(OSUtils.inputEventGetModifiersExText(e.getModifiersEx()));
            }
        });
        builder.add(mouseTriggerClick, constraints.xy(5, 1));
        builder.addLabel("Keyboard Trigger:", constraints.xy(1, 3));
        keyTrigger.setToolTipText("Keyboard trigger to popup the Recorder assert menu");
        keyTrigger.setEditable(false);
        keyTrigger.setBackground(Color.GRAY);
        keyTrigger.setText(OSUtils.inputEventGetModifiersExText(ContextMenuTriggers.getContextMenuKeyModifiers()) + "+"
                + OSUtils.keyEventGetKeyText(ContextMenuTriggers.getContextMenuKeyCode()));
        keyTrigger.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_SHIFT
                        || e.getKeyCode() == KeyEvent.VK_ALT || e.getKeyCode() == KeyEvent.VK_META)
                    return;
                String keyText = e.getModifiersEx() == 0 ? "" : (OSUtils.inputEventGetModifiersExText(e.getModifiersEx()) + "+");
                keyText += OSUtils.keyEventGetKeyText(e.getKeyCode());
                keyTrigger.setText(keyText);
            }
        });
        builder.add(keyTrigger, constraints.xy(3, 3));
        builder.addLabel("Files to hide from navigator view:", constraints.xy(1, 5));
        hideFilesNavigator.setToolTipText("Give patterns to exclude from the navigator view of Marathon");
        hideFilesNavigator.setText(Navigator.getHideFilePattern());
        builder.add(hideFilesNavigator, constraints.xy(3, 5));
        builder.addLabel("Files to hide from test view:", constraints.xy(1, 7));
        hideFilesJUnit.setToolTipText("Give patterns to exclude from the test view of Marathon");
        hideFilesJUnit.setText(TestCreator.getHideFilePattern());
        builder.add(hideFilesJUnit, constraints.xy(3, 7));
        doNotHideMarathonITEBlurbs.setToolTipText("Hide the MarathonITE shameless plug");
        doNotHideMarathonITEBlurbs.setSelected(Boolean.parseBoolean(prefs.get(Constants.PREF_ITE_BLURBS, "false")));
        doNotHideMarathonITEBlurbs.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(PreferencesDialog.this, "Restart Marathon for this option to take effect", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        builder.addLabel("Hide MarathonITE options from view:", constraints.xy(1, 9));
        builder.add(doNotHideMarathonITEBlurbs, constraints.xy(3, 9));
        return builder.getPanel();
    }

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}
