package net.sourceforge.marathon.player;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.component.ComponentFinder;

public class Marathon {

    public ComponentFinder finder;
    public PlaybackResult result = null;

    public Marathon() {
        String property = System.getProperty(Constants.PROP_RUNTIME_DELAY);
        int delayInMS = 0;
        if (property == null || "".equals(property))
            delayInMS = 0;
        else {
            try {
                delayInMS = Integer.parseInt(property);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        this.delayInMS = delayInMS;
    }

    private final int delayInMS;
    
    public int getDelayInMS() {
        return delayInMS;
    }
}
