package net.sourceforge.marathon.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.util.Properties;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.DialogForTesting;
import net.sourceforge.marathon.junit.swingui.Icons;
import net.sourceforge.marathon.objectmap.ObjectMapNamingStrategy;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestPropertyWrapper {

    private static DialogForTesting dialog;

    @BeforeClass public static void setupOnce() {
        System.setProperty(Constants.PROP_PROJECT_SCRIPT_MODEL, "net.sourceforge.marathon.mocks.MockScriptModel");
        dialog = new DialogForTesting(TestPropertyWrapper.class.getName());
        dialog.addTable("table", true, new String[][] { { "a", "b", "c" }, { "d", "e", "f" } }, new String[] { "col1", "col2",
                "col3" });
        dialog.addTabbedPane("tabbedPane", "charles", "brilly");
        dialog.addButton(null, "Press Me", Icons.RUN);
        dialog.addTextField("textField", "some text");
        dialog.addLabel("label", "This is a label");
        dialog.addComboBox("comboBox", new String[] { "foo", "bar", "baz" });
        dialog.addMessageBoxButton(null, "Show Message Dialog", "Message Dialog");
        dialog.show();
    }

    @AfterClass public static void teardownOnce() {
        dialog.dispose();
        Properties properties = System.getProperties();
        properties.remove(Constants.PROP_PROJECT_SCRIPT_MODEL);
        System.setProperties(properties);
    }

    private MComponent wrapperTextField;
    private MComponent wrapperButton;
    private MComponent wrapperLabel;

    @Before
    public void setUp() {
        wrapperTextField = new MComponent(dialog.getTextField(), WindowMonitor.getInstance());
        wrapperButton = new MComponent(dialog.getButton(), WindowMonitor.getInstance());
        wrapperLabel = new MComponent(dialog.getLabel(), WindowMonitor.getInstance());
    }
    
    @Test(expected = NullPointerException.class) public void properyWrapperGetPropertyWhenComponentIsNull() {
        MComponent wrapper = new MComponent(null, WindowMonitor.getInstance());
        wrapper.getProperty("type");
    }

    @Test public void testGetMComponentName() {
        assertEquals("No Name", wrapperTextField.getMComponentName());
        wrapperTextField.setMComponentName("My Name");
        assertEquals("My Name", wrapperTextField.getMComponentName());
    }

    @Test public void testGetType() {
        assertEquals("JTextField", wrapperTextField.getType());
    }

    @Test public void testGetName() {
        assertEquals("textField", wrapperTextField.getName());
    }

    @Test public void testGetButtonText() {
        assertEquals("Press Me", wrapperButton.getButtonText());
    }

    @Test public void testGetButtonIconFile() {
        assertEquals("run", wrapperButton.getButtonIconFile());
    }

    @Test public void testGetCText() {
        assertEquals("Press Me", wrapperButton.getCText());
    }

    @Test public void testGetIconFile() {
        assertEquals("run", wrapperButton.getIconFile());
    }

    @Test public void testGetLabelText() {
        assertEquals("lbl:This is a label", wrapperLabel.getLabelText());
    }

    @Test public void testGetLabeledBy() {
        dialog.getLabel().setLabelFor(dialog.getTextField());
        assertEquals("This is a label", wrapperTextField.getLabeledBy());
    }

    @Test public void testGetPrecedingLabel() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog.getComboBox());
        assertEquals(wrapper.getPrecedingLabel(), "This is a label");
    }

    @Test public void testGetIndexInParent() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog.getTextField());
        assertEquals(wrapper.getIndexInParent(), 3);
    }

    @Test public void testGetIndexInContainer() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog.getTextField());
        assertEquals(wrapper.getIndexInContainer(), 12);
    }

    @Test public void testGetLayoutData() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog.getTextField());
        assertEquals("Hello World", wrapper.getLayoutData());
    }

    @Test public void testGetOMapClassName() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog);
        assertEquals("net.sourceforge.marathon.DialogForTesting", wrapper.getOMapClassName());
    }

    @Test public void testGetOMapClassSimpleName() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog);
        assertEquals("DialogForTesting", wrapper.getOMapClassSimpleName());
    }

    @Test public void testGetInstanceOf() {
        assertEquals("javax.swing.JTextField", wrapperTextField.getInstanceOf());
    }

    @Test public void testGetFieldName() {
        ObjectMapNamingStrategy strategy = new ObjectMapNamingStrategy();
        strategy.init();
        strategy.setTopLevelComponent(dialog);
        MComponent wrapper = findPropertyWrapper(dialog.getTextField());
        assertEquals ("textField", wrapper.getFieldName());
    }

    private MComponent findPropertyWrapper(Component c) {
        return new MComponent(c, null);
    }

    @Test @Ignore public void testGetInternalFrameIndex() {
        fail("Not yet implemented");
    }

}
