package net.sourceforge.marathon.api;

import java.util.List;
import java.util.Properties;



public interface IRuntimeLauncherModel extends ISubpanelProvider {

    public List<String> getPropertyKeys();

    public IRuntimeFactory getRuntimeFactory();

    public String createLaunchCommand(Properties props);

}
