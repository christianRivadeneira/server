package metadata;

import java.util.prefs.Preferences;

public class FrmCfg {

    private static final Preferences PREF = Preferences.userRoot().node("/sigma/generatorBack");

    public static String getBackPath() {
        return PREF.get("backPath", "");
    }
    
     public static String getFrontPath() {
        return PREF.get("frontPath", "");
    }

    
}
