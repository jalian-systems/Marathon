import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Prefs {

    public static void main(String[] args) throws BackingStoreException {
        Preferences prefs;
        prefs = Preferences.userRoot();
        int index = 0 ;
        boolean remove = false;
        if(args.length > 0 && args[0].equals("-r")) {
            index = 1 ;
            remove = true ;
        }
        for (;index < args.length; index++) {
            prefs = prefs.node(args[index]);
        }
        listPrefs(prefs, "");
        if(remove) {
            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args.length - 1);
            remove(args2);
        }
    }

    public static void listPrefs(Preferences prefs, String prefix) throws BackingStoreException {
        System.out.println(prefix + prefs.name());
        String[] keys = prefs.keys();
        for (String key : keys) {
            System.out.println(prefix + "  " + key + " = " + prefs.get(key, ""));
        }
        String[] childrenNames = prefs.childrenNames();
        for (int i = 0; i < childrenNames.length; i++) {
            listPrefs(prefs.node(childrenNames[i]), "    " + prefix);
            ;
        }
    }
    
    public static void remove(String[] args) throws BackingStoreException {
        Preferences prefs;
        prefs = Preferences.userRoot();
        for (String arg : args) {
            prefs = prefs.node(arg);
        }
        prefs.removeNode();
        prefs.flush();
    }
}
