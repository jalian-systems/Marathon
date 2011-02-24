package net.sourceforge.marathon.util.osx;

public interface IOSXApplicationListener {

    void handleAbout();

    void handlePreferences();

    boolean handleQuit();

}
