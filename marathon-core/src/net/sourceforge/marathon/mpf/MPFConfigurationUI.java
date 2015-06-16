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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Logger;

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
import net.sourceforge.marathon.api.IRuntimeLauncherModel;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.ITestApplication;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.junit.textui.StdOutLogger;
import net.sourceforge.marathon.runtime.TestApplication;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.FileUtils;
import net.sourceforge.marathon.util.MPFUtils;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

public class MPFConfigurationUI extends EscapeDialog {

    private final static Logger logger = Logger.getLogger(MPFConfigurationUI.class.getName());

    private static final long serialVersionUID = 1L;
    public static final ImageIcon BANNER = new ImageIcon(MPFConfigurationUI.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/banner.gif"));;
    private IPropertiesPanel[] panels;
    private String dirName = null;
    private JTabbedPane tabbedPane;

    private ApplicationPanel applicationPanel;

    private JButton cancelButton;

    private JButton saveButton;

    public MPFConfigurationUI(JDialog parent) {
        this(null, parent);
    }

    public MPFConfigurationUI(String dirName, JDialog parent) {
        super(parent, "Configure - (New Project)", true);
        RuntimeLogger.setRuntimeLogger(new StdOutLogger());
        initConfigurationUI(dirName);
    }

    public MPFConfigurationUI(String dirName, JFrame parent) {
        super(parent, "Configure", true);
        RuntimeLogger.setRuntimeLogger(new StdOutLogger());
        initConfigurationUI(dirName);
    }

    private void initConfigurationUI(String dirName) {
        this.dirName = dirName;
        applicationPanel = new ApplicationPanel(this);
        panels = new IPropertiesPanel[] { new ProjectPanel(this), applicationPanel, new ScriptPanel(this), new VariablePanel(this),
                new AssertionsPanel(this), new IgnoreComponentsPanel(this), new ResolverPanel(this) };
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
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_P);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_A);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_R);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_L);
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_S);
        tabbedPane.setMnemonicAt(5, KeyEvent.VK_I);
        tabbedPane.setMnemonicAt(6, KeyEvent.VK_E);
        getContentPane().add(tabbedPane);
        JButton testButton = UIUtils.createTestButton();
        testButton.setMnemonic(KeyEvent.VK_T);
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!validateInput())
                    return;
                ITestApplication application = getApplicationTester();
                try {
                    application.launch();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(MPFConfigurationUI.this, "Unable to launch application " + e1);
                    e1.printStackTrace();
                }
            }
        });
        cancelButton = UIUtils.createCancelButton();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MPFConfigurationUI.this.dispose();
            }
        });
        saveButton = UIUtils.createSaveButton();
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saveProjectFile();
                    dispose();
                }
            }
        });
        JPanel buttonPanel;
        buttonPanel = ButtonBarFactory.buildOKCancelApplyBar(saveButton, cancelButton, testButton);
        buttonPanel.setBorder(Borders.createEmptyBorder("0dlu, 0dlu, 3dlu, 9dlu"));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
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
            String name = properties.getProperty(Constants.PROP_PROJECT_NAME);
            if (name != null)
                setTitle("Configure - " + name);
        } else {
            properties = getDefaultProperties();
        }
        setProperties(properties);
        setSize(800, 600);
    }

    protected ITestApplication getApplicationTester() {
        Properties props = getProperties();
        ITestApplication applciationTester = new TestApplication(MPFConfigurationUI.this, props);
        return applciationTester;
    }

    private IRuntimeLauncherModel getLauncherModel() {
        return applicationPanel.getSelectedModel();
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
        setPropertiesToPanels(panels, props);
    }

    private void setPropertiesToPanels(IPropertiesPanel[] panelsArray, Properties props) {
        if (panelsArray != null)
            for (int i = 0; i < panelsArray.length; i++)
                panelsArray[i].setProperties(props);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        getPropertiesFromPanels(panels, properties);

        properties.setProperty(Constants.PROP_APPLICATION_LAUNCHTIME, "60000");
        return properties;
    }

    private void getPropertiesFromPanels(IPropertiesPanel[] panelsArray, Properties properties) {
        if (panelsArray != null)
            for (int i = 0; i < panelsArray.length; i++) {
                panelsArray[i].getProperties(properties);
            }
    }

    public String getProjectDirectory() {
        setLocation(getParent().getX() + 20, getParent().getY() + 20);
        setVisible(true);
        return dirName;
    }

    private boolean validateInput() {
        return validatePanelInputs(panels);
    }

    private boolean validatePanelInputs(IPropertiesPanel[] panelsArray) {
        if (panelsArray != null)
            for (int i = 0; i < panelsArray.length; i++) {
                if (!panelsArray[i].isValidInput()) {
                    tabbedPane.setSelectedComponent(panelsArray[i].getPanel());
                    return false;
                }
            }
        return true;
    }

    private void saveProjectFile() {
        Properties propsFromPanels = getProperties();
        File projectDir = new File(propsFromPanels.getProperty(Constants.PROP_PROJECT_DIR));
        MPFUtils.convertPathChar(propsFromPanels);
        createMarathonDirectories(propsFromPanels);
        createDefaultFixture(propsFromPanels, new File(projectDir, Constants.DIR_FIXTURES));
        copyMarathonDirProperties(propsFromPanels);
        try {
            Properties saveProps = getProperties();
            copyMarathonDirProperties(saveProps);
            saveProps.remove(Constants.PROP_PROJECT_DIR);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(projectDir, Constants.PROJECT_FILE));
            try {
                saveProps.store(fileOutputStream, "Marathon Project File");
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
        if (propsFromPanels.getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY) != null) {
            try {
                Class<?> forName = Class.forName(propsFromPanels.getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY) + "Init");
                Method method = forName.getMethod("initialize");
                if (method != null)
                    method.invoke(null);
            } catch (Exception e) {
            }
        }
        dispose();
    }

    private void createDefaultFixture(Properties props, File fixtureDir) {
        try {
            if (getLauncherModel() == null)
                return;
            getSelectedScriptModel(props.getProperty(Constants.PROP_PROJECT_SCRIPT_MODEL)).createDefaultFixture(this, props,
                    fixtureDir, getLauncherModel().getPropertyKeys());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    private void copyMarathonDirProperties(Properties props) {
        if (props.getProperty(Constants.PROP_TEST_DIR) == null)
            props.setProperty(Constants.PROP_TEST_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_TESTCASES);
        if (props.getProperty(Constants.PROP_SUITE_DIR) == null)
            props.setProperty(Constants.PROP_SUITE_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_TESTSUITES);
        if (props.getProperty(Constants.PROP_CHECKLIST_DIR) == null)
            props.setProperty(Constants.PROP_CHECKLIST_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_CHECKLIST);
        if (props.getProperty(Constants.PROP_MODULE_DIRS) == null)
            props.setProperty(Constants.PROP_MODULE_DIRS, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_MODULE);
        if (props.getProperty(Constants.PROP_DATA_DIR) == null)
            props.setProperty(Constants.PROP_DATA_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_DATA);
        if (props.getProperty(Constants.PROP_FIXTURE_DIR) == null)
            props.setProperty(Constants.PROP_FIXTURE_DIR, "%" + Constants.PROP_PROJECT_DIR + "%/" + Constants.DIR_FIXTURES);
    }

    private void createMarathonDirectories(Properties props) {
        String projectDir = props.getProperty(Constants.PROP_PROJECT_DIR);
        if (props.getProperty(Constants.PROP_TEST_DIR) == null)
            createMarathonDir(projectDir, Constants.DIR_TESTCASES);
        if (props.getProperty(Constants.PROP_SUITE_DIR) == null)
            createMarathonDir(projectDir, Constants.DIR_TESTSUITES);
        if (props.getProperty(Constants.PROP_CHECKLIST_DIR) == null) {
            createMarathonDir(projectDir, Constants.DIR_CHECKLIST);
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            };
            FileUtils.copyFiles(new File(System.getProperty(Constants.PROP_HOME), "Checklists"), new File(projectDir,
                    Constants.DIR_CHECKLIST), filter);
            File srcFile = new File(System.getProperty(Constants.PROP_HOME), "logging.properties");
            File destFile = new File(projectDir, "logging.properties");
            try {
                FileUtils.copyFile(srcFile, destFile);
            } catch (IOException e) {
                System.err.println("Copy file failed: src = " + srcFile + " dest = " + destFile);
                e.printStackTrace();
            }
        }
        if (props.getProperty(Constants.PROP_MODULE_DIRS) == null)
            createMarathonDir(projectDir, Constants.DIR_MODULE);
        if (props.getProperty(Constants.PROP_DATA_DIR) == null)
            createMarathonDir(projectDir, Constants.DIR_DATA);
        if (props.getProperty(Constants.PROP_FIXTURE_DIR) == null)
            createMarathonDir(projectDir, Constants.DIR_FIXTURES);
    }

    private void createMarathonDir(String projectDir, String dir) {
        File file = new File(projectDir, dir);
        if (!file.mkdirs()) {
            logger.warning("Unable to create folder: " + file + " - Marathon might not be able to use the project folder");
        }
    }

    private IScriptModelClientPart getSelectedScriptModel(String selectedScript) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        Class<?> klass = Class.forName(selectedScript);
        return (IScriptModelClientPart) klass.newInstance();
    }

    @Override public JButton getOKButton() {
        return saveButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }

}
