package net.sourceforge.marathon.mpf;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

import net.sourceforge.marathon.Constants;

public class ScriptPanel extends CompositePanel implements IPropertiesPanel {

    public ScriptPanel(JDialog parent) {
        super(parent);
    }

    public static final Icon ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource(
            "net/sourceforge/marathon/mpf/images/script_obj.gif"));

    @Override protected String getResourceName() {
        return "scriptmodel";
    }

    @Override public String getName() {
        return "Language";
    }

    @Override public Icon getIcon() {
        return ICON;
    }

    @Override protected String getClassProperty() {
        return Constants.PROP_PROJECT_SCRIPT_MODEL;
    }

    @Override protected boolean isSelectable() {
        return false;
    }

    @Override protected String getOptionFieldName() {
        return "Script:";
    }
}
