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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Main;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.FileUtils;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

public class MPFConfigurationUI extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    public static final ImageIcon BANNER = new ImageIcon(MPFConfigurationUI.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/banner.gif"));;
    private IPropertiesPanel[] panels;
    private String dirName = null;
    private JTabbedPane tabbedPane;
    private String namingStrategy = Constants.DEFAULT_NAMING_STRATEGY;
    boolean marathonNamingStrategy = true;
    private String selectedScript;
    private IPropertiesPanel[] scriptPanels;

    public MPFConfigurationUI(JDialog parent) {
        this(null, parent);
    }

    public MPFConfigurationUI(String dirName, JDialog parent) {
        super(parent, "Configure", true);
        initConfigurationUI(dirName);
    }

    public MPFConfigurationUI(String dirName, JFrame parent) {
        super(parent, "Configure", true);
        initConfigurationUI(dirName);
    }

    private void initConfigurationUI(String dirName) {
        this.dirName = dirName;
        panels = new IPropertiesPanel[] { new ProjectPanel(this), new MainPanel(this), new ClassPathPanel(this),
                new AssertionsPanel(this), new VariablePanel(this), new IgnoreComponentsPanel(this), new ResolverPanel(this) };
        BannerPanel bannerPanel = new BannerPanel();
        String[] lines;
        if (dirName != null)
            lines = new String[] { "Update a Marathon Project" };
        else
            lines = new String[] { "Create a Marathon Project" };
        BannerPanel.Sheet sheet = new BannerPanel.Sheet("Create and manage configuration", lines, BANNER);
        bannerPanel.addSheet(sheet, "main");
        getContentPane().add(bannerPanel, BorderLayout.NORTH);
        tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);
        for (int i = 0; i < panels.length; i++) {
            tabbedPane.addTab(panels[i].getName(), panels[i].getIcon(), panels[i].getPanel());
        }
        getContentPane().add(tabbedPane);
        JButton testButton = new JButton("Test");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!validateInput())
                    return;
                Properties props = getProperties();
                TestApplication application = new TestApplication(MPFConfigurationUI.this, props);
                try {
                    application.launch();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(MPFConfigurationUI.this, "Unable to launch application " + e1);
                    e1.printStackTrace();
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MPFConfigurationUI.this.dispose();
            }
        });
        JPanel buttonPanel;
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saveProjectFile();
                    dispose();
                }
            }
        });
        buttonPanel = ButtonBarFactory.buildRightAlignedBar(new JButton[] { testButton, saveButton, cancelButton });
        buttonPanel.setBorder(Borders.createEmptyBorder("0dlu, 0dlu, 3dlu, 9dlu"));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setCloseButton(cancelButton);
        pack();
        Properties properties = new Properties();
        if (dirName != null) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(new File(dirName, Constants.PROJECT_FILE));
                properties.load(fileInputStream);
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                return;
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            properties.setProperty(Constants.PROP_PROJECT_DIR, dirName);
            if ((namingStrategy = properties.getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY)) == null)
                marathonNamingStrategy = false;
        } else {
            properties = getDefaultProperties();
        }
        setProperties(properties);
    }

    private Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty(Constants.PROP_IGNORE_COMPONENTS, getDefaultIgnoreComponents());
        props.setProperty(Constants.PROP_RECORDER_ASSERTIONS, getDefaultAssertions());
        props.setProperty(Constants.PROP_USE_FIELD_NAMES, Boolean.TRUE.toString());
        props.setProperty(Constants.PROP_PROPPREFIX + "java.util.logging.config.file", "%marathon.project.dir%/logging.properties");
        return props;
    }

    private String getDefaultIgnoreComponents() {
        StringBuffer ignoreComponents = new StringBuffer();
        addIgnoreComponent(ignoreComponents, JMenuBar.class, false);
        addIgnoreComponent(ignoreComponents, JToolBar.class, false);
        addIgnoreComponent(ignoreComponents, JPanel.class, false);
        addIgnoreComponent(ignoreComponents, JViewport.class, false);
        addIgnoreComponent(ignoreComponents, JScrollBar.class, true);
        return ignoreComponents.toString();
    }

    private void addIgnoreComponent(StringBuffer ignoreComponents, Class<?> klass, boolean ignoreChild) {
        ignoreComponents.append(klass.getName());
        ignoreComponents.append("(ignorechild:" + ignoreChild + ")");
        ignoreComponents.append(";");
    }

    private String getDefaultAssertions() {
        StringBuffer assertions = new StringBuffer();
        addAssertion(assertions, "Enabled");
        addAssertion(assertions, "Background");
        addAssertion(assertions, "Foreground");
        addAssertion(assertions, "RowCount", JTable.class);
        addAssertion(assertions, "ColumnCount", JTable.class);
        addAssertion(assertions, "ItemCount", JComboBox.class);
        addAssertion(assertions, "Model.Size", JList.class, "ItemCount");
        addAssertion(assertions, "Font");
        addAssertion(assertions, "Font.Family", null, "FontFamily");
        addAssertion(assertions, "Border");
        addAssertion(assertions, "Border.LineColor", null, "BorderLineColor");
        return assertions.toString();
    }

    private void addAssertion(StringBuffer assertions, String property) {
        addAssertion(assertions, property, null);
    }

    private void addAssertion(StringBuffer assertions, String property, Class<?> class1) {
        addAssertion(assertions, property, class1, null);
    }

    private void addAssertion(StringBuffer b, String property, Class<?> class1, String displayName) {
        b.append(property + ":" + (class1 == null ? "" : class1.getName()) + ":" + (displayName == null ? "" : displayName) + ";");
    }

    private void setProperties(Properties props) {
        for (int i = 0; i < panels.length; i++) {
            panels[i].setProperties(props);
        }
        if (scriptPanels != null)
            for (int i = 0; i < scriptPanels.length; i++)
                scriptPanels[i].setProperties(props);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        for (int i = 0; i < panels.length; i++) {
            panels[i].getProperties(properties);
        }
        if (scriptPanels != null)
            for (int i = 0; i < scriptPanels.length; i++)
                scriptPanels[i].getProperties(properties);
        if (marathonNamingStrategy) {
            properties.setProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY, namingStrategy);
        }
        properties.setProperty(Constants.PROP_APPLICATION_LAUNCHTIME, "60000");
        return properties;
    }

    public String getProjectDirectory() {
        setLocation(getParent().getX() + 20, getParent().getY() + 20);
        setVisible(true);
        return dirName;
    }

    private boolean validateInput() {
        for (int i = 0; i < panels.length; i++) {
            if (!panels[i].isValidInput()) {
                tabbedPane.setSelectedIndex(i);
                return false;
            }
        }
        if (scriptPanels != null)
            for (int i = 0; i < scriptPanels.length; i++) {
                if (!scriptPanels[i].isValidInput()) {
                    tabbedPane.setSelectedIndex(i + panels.length);
                    return false;
                }
            }
        return true;
    }

    private void saveProjectFile() {
        Properties props = getProperties();
        File projectDir = new File(props.getProperty(Constants.PROP_PROJECT_DIR));
        Main.convertPathChar(props);
        props = Main.removePrefixes(props);
        Main.replaceEnviron(props);
        setDirectories(props);
        try {
            getModel(selectedScript).createFixture(this, props);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        try {
            Properties storeProps = getProperties();
            copyMarathonDirProperties(props, storeProps);
            storeProps.remove(Constants.PROP_PROJECT_DIR);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(projectDir, Constants.PROJECT_FILE));
            try {
                storeProps.store(fileOutputStream, "Marathon Project File");
            } finally {
                fileOutputStream.close();
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Can't store the settings: " + e.getMessage());
            return;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Can't store the settings: " + e.getMessage());
            return;
        }
        Main.processMPF(projectDir.getAbsolutePath());
        dirName = projectDir.toString();
        if (props.getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY) != null) {
            try {
                Class<?> forName = Class.forName(props.getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY) + "Init");
                Method method = forName.getMethod("initialize");
                if (method != null)
                    method.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dispose();
    }

    private void copyMarathonDirProperties(Properties source, Properties dest) {
        if (dest.getProperty(Constants.PROP_TEST_DIR) == null)
            dest.setProperty(Constants.PROP_TEST_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_TESTCASES);
        if (dest.getProperty(Constants.PROP_CHECKLIST_DIR) == null)
            dest.setProperty(Constants.PROP_CHECKLIST_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_CHECKLIST);
        if (dest.getProperty(Constants.PROP_MODULE_DIRS) == null)
            dest.setProperty(Constants.PROP_MODULE_DIRS, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_MODULE);
        if (dest.getProperty(Constants.PROP_DATA_DIR) == null)
            dest.setProperty(Constants.PROP_DATA_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_DATA);
        if (dest.getProperty(Constants.PROP_FIXTURE_DIR) == null)
            dest.setProperty(Constants.PROP_FIXTURE_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_FIXTURES);
    }

    private void setDirectories(Properties props) {
        if (props.getProperty(Constants.PROP_TEST_DIR) == null)
            setMarathonDir(props, Constants.DIR_TESTCASES, Constants.PROP_TEST_DIR);
        if (props.getProperty(Constants.PROP_CHECKLIST_DIR) == null) {
            setMarathonDir(props, Constants.DIR_CHECKLIST, Constants.PROP_CHECKLIST_DIR);
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            };
            FileUtils.copyFiles(new File(System.getProperty(Constants.PROP_HOME), "Checklists"),
                    new File(props.getProperty(Constants.PROP_CHECKLIST_DIR)), filter);
            File srcFile = new File(System.getProperty(Constants.PROP_HOME), "logging.properties");
            File destFile = new File(props.getProperty(Constants.PROP_PROJECT_DIR), "logging.properties");
            try {
                FileUtils.copyFile(srcFile, destFile);
            } catch (IOException e) {
                System.err.println("Copy file failed: src = " + srcFile + " dest = " + destFile);
                e.printStackTrace();
            }
        }
        if (props.getProperty(Constants.PROP_MODULE_DIRS) == null)
            setMarathonDir(props, Constants.DIR_MODULE, Constants.PROP_MODULE_DIRS);
        if (props.getProperty(Constants.PROP_DATA_DIR) == null)
            setMarathonDir(props, Constants.DIR_DATA, Constants.PROP_DATA_DIR);
        if (props.getProperty(Constants.PROP_FIXTURE_DIR) == null)
            setMarathonDir(props, Constants.DIR_FIXTURES, Constants.PROP_FIXTURE_DIR);
    }

    private void setMarathonDir(Properties props, String dir, String propKey) {
        File file = new File(props.getProperty(Constants.PROP_PROJECT_DIR), dir);
        file.mkdirs();
        props.setProperty(propKey, file.toString());
    }

    public void updateScript(String script) {
        if (script.equals(this.selectedScript))
            return;
        this.selectedScript = script;
        int tabCount = tabbedPane.getTabCount();
        if (tabCount > panels.length) {
            for (int i = panels.length; i < tabCount; i++)
                tabbedPane.remove(i);
        }
        scriptPanels = getScriptPanels();
        for (int i = 0; i < scriptPanels.length; i++) {
            IPropertiesPanel scriptPanel = scriptPanels[i];
            tabbedPane.addTab(scriptPanel.getName(), scriptPanel.getIcon(), scriptPanel.getPanel());
        }
    }

    private IPropertiesPanel[] getScriptPanels() {
        if (selectedScript == null)
            return new IPropertiesPanel[0];
        try {
            return getModel(selectedScript).getPropertiesPanels(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new IPropertiesPanel[0];
    }

    private IScriptModelClientPart getModel(String selectedScript) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Class<?> klass = Class.forName(selectedScript);
        return (IScriptModelClientPart) klass.newInstance();
    }
}
