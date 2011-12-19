package net.sourceforge.marathon.api;

import javax.swing.JDialog;

import net.sourceforge.marathon.mpf.IPropertiesPanel;

public interface ISubpanelProvider {

    public abstract IPropertiesPanel[] getSubPanels(JDialog parent);

}