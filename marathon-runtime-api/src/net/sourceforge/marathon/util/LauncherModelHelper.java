package net.sourceforge.marathon.util;

import net.sourceforge.marathon.api.IRuntimeLauncherModel;

public class LauncherModelHelper {

    public static IRuntimeLauncherModel getLauncherModel(String launcher) {
        Class<?> klass;
        try {
            klass = Class.forName(launcher);
            return (IRuntimeLauncherModel) klass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
