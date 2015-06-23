package net.sourceforge.marathon;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sourceforge.marathon.util.Blurb;

public class WelcomeMessage {

    public static void showWelcomeMessage() {
        new Blurb("about/welcome", "Welcome") {
        };
    }

    public static void main(String[] args) throws BackingStoreException {
        Preferences prefs = Preferences.userNodeForPackage(WelcomeMessage.class);
        prefs.remove("version");
        prefs.flush();
        WelcomeMessage.showWelcomeMessage();
        System.exit(0);
    }
}
