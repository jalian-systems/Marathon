package net.sourceforge.marathon.mpf;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IRuntimeLauncherModel;

public class ApplicationPanel extends CompositePanel implements IPropertiesPanel {

    public static final Icon ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/app_obj.gif"));

    public static final String NODIALOGBORDER = "no-dialog-border";

    public ApplicationPanel(JDialog parent) {
        super(parent);
    }

    protected String getResourceName() {
        return "launcher";
    }

    public ApplicationPanel(JDialog parent, String nodialogborder) {
        super(parent, nodialogborder);
    }

    public String getName() {
        return "Application";
    }

    public Icon getIcon() {
        return ICON;
    }

    protected String getClassProperty() {
        return Constants.PROP_PROJECT_LAUNCHER_MODEL;
    }

    public IRuntimeLauncherModel getSelectedModel() {
        try {
            return (IRuntimeLauncherModel) getLauncherModel(getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override protected String getOptionFieldName() {
        return "La&uncher: ";
    }

}
