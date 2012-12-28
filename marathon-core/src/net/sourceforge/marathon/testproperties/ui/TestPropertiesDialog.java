package net.sourceforge.marathon.testproperties.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.display.DisplayWindow;
import net.sourceforge.marathon.display.FileEventHandler;
import net.sourceforge.marathon.testproperties.ui.TestProperty.DisplayType;
import net.sourceforge.marathon.testproperties.ui.TestProperty.PropertyType;
import net.sourceforge.marathon.util.EscapeDialog;
import net.sourceforge.marathon.util.UIUtils;

import org.yaml.snakeyaml.Yaml;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class TestPropertiesDialog extends EscapeDialog {
    private static final String PROPERTIES_START_MONIKER = "=begin #{{{ Marathon Testcase Properties";
    private static final String PROPERTIES_END_MONIKER = "=end #}}} Marathon Testcase Properties";

    /**
     * serial
     */
    private static final long serialVersionUID = 1L;

    /**
     * File for which test properties are being modified.
     */
    private final File testFile;

    /**
     * OK Button
     */
    private JButton okButton;

    /**
     * Cancel Button
     */
    private JButton cancelButton;

    /**
     * List of properties in the configuration file.
     */
    private List<TestProperty> configTestPropList;

    /**
     * Properties in the test case.
     */
    private Properties testProperties;

    /**
     * Names of the properties in the configuration file.
     */
    private List<String> propertyNames;

    /**
     * List of text components in the dialog which holds the value for the
     * corresponding properties.
     */
    private List<JTextComponent> txtComponentList;

    /**
     * Marker to indicate whether the file has been saved.
     */
    private boolean saved;
    private DisplayWindow displayWindow;

    @SuppressWarnings("unchecked") public TestPropertiesDialog(DisplayWindow displayWindow, File file) throws IOException {
        super(displayWindow, "Marathon Testcase Properties", true);
        this.displayWindow = displayWindow;
        this.testFile = file;
        testProperties = new Properties();
        propertyNames = new ArrayList<String>();
        txtComponentList = new ArrayList<JTextComponent>();

        try {
            FileReader reader = new FileReader(new File(System
                    .getProperty(Constants.PROP_PROJECT_DIR), Constants.FILE_TESTPROPERTIES));
            configTestPropList = (List<TestProperty>) new Yaml().load(reader);
            reader.close();
        } catch (FileNotFoundException e) {
            configTestPropList = createDefault();
        }
        readPropertiesFromTestFile();
        initUI();
    }

    private List<TestProperty> createDefault() {
        List<TestProperty> testPropList = new ArrayList<TestProperty>();
        TestProperty pid = new TestProperty("ID", PropertyType.INTEGER, DisplayType.TEXTFIELD, "0");
        testPropList.add(pid);
        pid = new TestProperty("Name", PropertyType.STRING, DisplayType.TEXTFIELD, "");
        testPropList.add(pid);
        pid = new TestProperty("Description", PropertyType.STRING, DisplayType.TEXTBOX, "");
        testPropList.add(pid);
        try {
            File file = new File(System.getProperty(Constants.PROP_PROJECT_DIR),
                    Constants.FILE_TESTPROPERTIES);
            FileWriter output = new FileWriter(file);
            new Yaml().dump(testPropList, output);
            output.close();
            FileEventHandler fileEventHandler = displayWindow.getFileEventHandler();
            fileEventHandler.fireNewEvent(file, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testPropList;
    }

    /**
     * Constructs the UI
     */
    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        setSize(500, 300);
        setLocationRelativeTo(getParent());

        FormLayout layout = new FormLayout("3dlu, left:pref, 5dlu, pref:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        int row = 0;
        RowSpec rowSpec = RowSpec.decode("top:pref");
        RowSpec emptyRowSpec = RowSpec.decode("3dlu");

        for (TestProperty property : configTestPropList) {
            layout.appendRow(emptyRowSpec);
            layout.appendRow(rowSpec);
            row += 2;
            String propertyName = property.getName();
            JLabel label = new JLabel(propertyName);
            propertyNames.add(propertyName);

            String value = testProperties.getProperty(propertyName);
            value = value != null ? value : "";
            JComponent display = null;
            if (property.getDisplay() == DisplayType.TEXTFIELD) {
                JTextField txtDisplay = new JTextField();
                txtDisplay.setText(value);
                txtComponentList.add(txtDisplay);
                txtDisplay.setBorder(BorderFactory.createLoweredBevelBorder());
                display = txtDisplay;
            } else if (property.getDisplay() == DisplayType.TEXTBOX) {
                JTextArea txtDisplay = new JTextArea(4, 20);
                txtDisplay.setText(value);
                txtComponentList.add(txtDisplay);
                display = new JScrollPane(txtDisplay);
            }

            builder.add(label, cc.xy(2, row));
            builder.add(display, cc.xy(4, row));
        }

        okButton = UIUtils.createOKButton();
        cancelButton = UIUtils.createCancelButton();

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    saveProperties();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel okCancelBar = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
        JScrollPane scrollPane = new JScrollPane(builder.getPanel());
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(okCancelBar, BorderLayout.SOUTH);
    }

    /**
     * Read the test file for the properties from the begin moniker.
     * 
     * @throws IOException
     */
    private void readPropertiesFromTestFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String line = reader.readLine();
        while (line != null) {
            if (line.equals(PROPERTIES_START_MONIKER))
                readProperties(reader);
            line = reader.readLine();
        }
    }

    private void readProperties(BufferedReader reader) throws IOException {
        StringBuilder sbr = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null && !line.equals(PROPERTIES_END_MONIKER)) {
            sbr.append(line + "\n");
        }
        byte[] bytes = sbr.toString().getBytes();
        InputStream bais = new ByteArrayInputStream(bytes);
        testProperties.load(bais);
    }

    /**
     * Saves the entered properties to the test case file.
     * 
     * @throws IOException
     */
    private void saveProperties() throws IOException {
        Properties newProperties = getProperties();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newProperties.store(baos, testFile.getName() + " Properties");
        String propsString = new String(baos.toByteArray());

        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String read = "";
        StringBuilder sbr = new StringBuilder();
        while ((read = reader.readLine()) != null) {
            sbr.append(read + "\n");
        }
        reader.close();

        int beginIndex = sbr.indexOf(PROPERTIES_START_MONIKER);
        int endIndex = -1;
        if (beginIndex != -1) {
            int endIndexOfMoniker = sbr.indexOf(PROPERTIES_END_MONIKER);
            endIndex = endIndexOfMoniker != -1 ? endIndexOfMoniker + PROPERTIES_END_MONIKER.length() : -1;
        } else {
            int indexOfMoniker = sbr.indexOf("#}}} Marathon");
            beginIndex = indexOfMoniker != -1 ? indexOfMoniker + "#}}} Marathon".length() : -1;
        }
        if (beginIndex == -1)
            beginIndex = 0;

        if (endIndex != -1) {
            sbr.replace(beginIndex, endIndex, PROPERTIES_START_MONIKER + "\n" + propsString + PROPERTIES_END_MONIKER);
        } else
            sbr.insert(beginIndex, "\n\n" + PROPERTIES_START_MONIKER + "\n" + propsString + PROPERTIES_END_MONIKER + "\n");

        Writer writer = new FileWriter(testFile);
        for (int j = 0; j < sbr.length(); j++) {
            writer.write(sbr.charAt(j));
        }
        writer.close();
        saved = true;
        FileEventHandler fileEventHandler = displayWindow.getFileEventHandler();
        fileEventHandler.fireUpdateEvent(testFile);
        dispose();
    }

    /**
     * Gets the properties entered in the Dialog displayed.
     * 
     * @return new properties as entered in dialog displayed.
     */
    private Properties getProperties() {
        Properties props = new Properties();
        for (int i = 0; i < configTestPropList.size(); i++) {
            String value = txtComponentList.get(i).getText();
            if (value != null && value.trim().length() != 0)
                props.put(propertyNames.get(i), value);
        }
        return props;
    }

    public boolean isSaved() {
        return saved;
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}
