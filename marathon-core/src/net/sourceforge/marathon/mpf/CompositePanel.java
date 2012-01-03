package net.sourceforge.marathon.mpf;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sourceforge.marathon.api.ISubpanelProvider;
import net.sourceforge.marathon.mpf.ModelInfo.PlugInModelInfo;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class CompositePanel implements IPropertiesPanel {

    public static final Icon ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/app_obj.gif"));

    public static final String NODIALOGBORDER = "no-dialog-border";

    private final JDialog parent;

    private JComboBox launcherField;
    private JTabbedPane launchInfo;

    private ModelInfo launcherModels;

    private IPropertiesPanel[] launcherPanels;

    private JPanel panel;

    private boolean needDialogBorder = true;

    public CompositePanel(JDialog parent) {
        this.parent = parent;
        launcherModels = new ModelInfo(getResourceName(), parent);
        initComponents();
    }

    abstract protected String getResourceName();

    public CompositePanel(JDialog parent, String nodialogborder) {
        this(parent);
        this.needDialogBorder = false ;
    }

    public JPanel getPanel() {
        if (panel == null) {
            PanelBuilder builder = new PanelBuilder(new FormLayout("left:pref, 3dlu, pref:grow, 3dlu, fill:pref",
                    "3dlu, pref, 3dlu, fill:pref:grow"));
            if (needDialogBorder)
                builder.setDefaultDialogBorder();
            CellConstraints labelConstraints = new CellConstraints();
            CellConstraints compConstraints = new CellConstraints();
            builder.addLabel(getOptionFieldName(), labelConstraints.xy(1, 2), launcherField, compConstraints.xywh(3, 2, 3, 1));
            builder.add(launchInfo, compConstraints.xyw(1, 4, 5));
            panel = builder.getPanel();
        }
        return panel;
    }

    abstract protected String getOptionFieldName() ;

    private void initComponents() {
        launcherField = new JComboBox(launcherModels);
        launcherField.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateLauncher(getClassName());
                }
            }
        });
        launchInfo = new JTabbedPane();
    }

    public void updateLauncher(String launcher) {
        launchInfo.removeAll();
        launcherPanels = getLauncherPanels();
        for (int i = 0; i < launcherPanels.length; i++) {
            IPropertiesPanel p = launcherPanels[i];
            launchInfo.addTab(p.getName(), p.getIcon(), p.getPanel());
        }
    }

    private IPropertiesPanel[] getLauncherPanels() {
        String selectedLauncher = getClassName();
        if (selectedLauncher == null)
            return new IPropertiesPanel[] {};
        try {
            ISubpanelProvider model = getLauncherModel(selectedLauncher);
            return model.getSubPanels(parent);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(parent, "Could not find launcher", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (InstantiationException e) {
            JOptionPane.showMessageDialog(parent, "Could not find launcher", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            JOptionPane.showMessageDialog(parent, "Could not find launcher", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return new IPropertiesPanel[] {};
    }

    protected ISubpanelProvider getLauncherModel(String launcher) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Class<?> klass = Class.forName(launcher);
        return (ISubpanelProvider) klass.newInstance();
    }

    abstract public String getName();

    abstract public Icon getIcon();

    public void getProperties(Properties props) {
        props.setProperty(getClassProperty(), getClassName());
        for (IPropertiesPanel p : launcherPanels) {
            p.getProperties(props);
        }
    }

    abstract protected String getClassProperty();

    public String getClassName() {
        return ((PlugInModelInfo) launcherField.getSelectedItem()).className;
    }

    public void setProperties(Properties props) {
        setPlugInSelection(launcherField, launcherModels, props, getClassProperty());
        updateLauncher(getClassName());
        for (IPropertiesPanel p : launcherPanels) {
            p.setProperties(props);
        }
    }

    private void setPlugInSelection(JComboBox comboBox, ModelInfo models, Properties props, String key) {
        String model = (String) props.get(key);
        if (model == null) {
            comboBox.setSelectedIndex(0);
        } else {
            comboBox.setSelectedItem(models.getPluginModel(model));
            if (!isSelectable())
                comboBox.setEnabled(false);
        }
    }

    protected boolean isSelectable() {
        return true;
    }

    public boolean isValidInput() {
        for (IPropertiesPanel p : launcherPanels) {
            if (!p.isValidInput())
                return false;
        }
        return true;
    }
}
