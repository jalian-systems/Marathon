package net.sourceforge.marathon.mpf;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.sourceforge.marathon.Constants;

public class NamingStrategyPanel extends CompositePanel implements IPropertiesPanel, ISubPropertiesPanel {

    public NamingStrategyPanel(JDialog parent) {
        super(parent);
    }

    public static final Icon ICON = null;

    @Override protected String getResourceName() {
        return "objectlookup";
    }

    @Override public String getName() {
        return "Object Lookup";
    }

    @Override public Icon getIcon() {
        return ICON;
    }

    @Override protected String getClassProperty() {
        return Constants.PROP_RECORDER_NAMINGSTRATEGY;
    }

    @Override protected boolean isSelectable() {
        return true;
    }

    @Override protected String getOptionFieldName() {
        return "O&bject Lookup Strategy:";
    }

    @Override protected void errorMessage() {
        JOptionPane.showMessageDialog(parent, "Select a Object Lookup Strategy", "Object Lookup", JOptionPane.ERROR_MESSAGE);
    }

    public int getMnemonic() {
        return 0;
    }
}
