package net.sourceforge.marathon.api;

import javax.swing.JDialog;

import net.sourceforge.marathon.mpf.ISubPropertiesPanel;

public interface ISubpanelProvider {

    public abstract ISubPropertiesPanel[] getSubPanels(JDialog parent);

}