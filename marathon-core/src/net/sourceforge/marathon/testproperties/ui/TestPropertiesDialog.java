package net.sourceforge.marathon.testproperties.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import net.sourceforge.marathon.testproperties.ui.TestProperty.DisplayType;
import net.sourceforge.marathon.testproperties.ui.TestProperty.PropertyType;
import net.sourceforge.marathon.util.EscapeDialog;

import org.yaml.snakeyaml.Yaml;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class TestPropertiesDialog extends EscapeDialog {
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

    /**
     * Moniker which indicates the beginning of the test case properties.
     */
    private String testPropsBeginMoniker;

    /**
     * Moniker which indicates the rnd of the test case properties.
     */
    private String testPropsEndMoniker;

    private static final ImageIcon OK_ICON = new ImageIcon(TestPropertiesDialog.class.getClassLoader().getResource(
            "net/sourceforge/marathon/display/icons/enabled/ok.gif"));;
    private static final ImageIcon CANCEL_ICON = new ImageIcon(TestPropertiesDialog.class.getClassLoader().getResource(
            "net/sourceforge/marathon/display/icons/enabled/cancel.gif"));;

    @SuppressWarnings("unchecked") public TestPropertiesDialog(DisplayWindow displayWindow, File file) throws IOException {
        super(displayWindow, "Marathon Testcase Properties", true);
        this.testFile = file;
        testProperties = new Properties();
        propertyNames = new ArrayList<String>();
        txtComponentList = new ArrayList<JTextComponent>();
        testPropsBeginMoniker = "#{{{ Marathon Testcase Properties";
        testPropsEndMoniker = "#}}} Marathon Testcase Properties";

        try {
            configTestPropList = (List<TestProperty>) new Yaml().load(new FileReader(new File(System
                    .getProperty(Constants.PROP_PROJECT_DIR), Constants.FILE_TESTPROPERTIES)));
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

        okButton = new JButton("OK", OK_ICON);
        cancelButton = new JButton("Cancel", CANCEL_ICON);

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
        setCloseButton(cancelButton);
        getRootPane().setDefaultButton(okButton);
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
            if (line.equals(testPropsBeginMoniker))
                readProperties(reader);
            line = reader.readLine();
        }
    }

    private void readProperties(BufferedReader reader) throws IOException {
        StringBuilder sbr = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null && !line.equals(testPropsEndMoniker)) {
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
        final StringWriter propsString = new StringWriter();
        Properties newProperties = getProperties();
        newProperties.store(new OutputStream() {
            @Override public void write(int b) throws IOException {
                propsString.write(b);
            }
        }, "");

        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String read = "";
        StringBuilder sbr = new StringBuilder();
        while ((read = reader.readLine()) != null) {
            sbr.append(read + "\n");
        }

        int beginIndex = sbr.indexOf(testPropsBeginMoniker);
        int endIndex = -1;
        if (beginIndex != -1) {
            int endIndexOfMoniker = sbr.indexOf(testPropsEndMoniker);
            endIndex = endIndexOfMoniker != -1 ? endIndexOfMoniker + testPropsEndMoniker.length() : -1;
        } else {
            int indexOfMoniker = sbr.indexOf("#}}} Marathon");
            beginIndex = indexOfMoniker != -1 ? indexOfMoniker + "#}}} Marathon".length() : -1;
        }
        if (beginIndex == -1)
            beginIndex = 0;

        if (endIndex != -1) {
            sbr.replace(beginIndex, endIndex, "\n" + testPropsBeginMoniker + "\n" + propsString.toString() + testPropsEndMoniker
                    + "\n");
        } else
            sbr.insert(beginIndex, "\n" + testPropsBeginMoniker + "\n" + propsString.toString() + testPropsEndMoniker + "\n");

        Writer writer = new FileWriter(testFile);
        for (int j = 0; j < sbr.length(); j++) {
            writer.write(sbr.charAt(j));
        }
        writer.flush();
        saved = true;
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
}
