package api.rpt.model.rptTbls;

import java.util.List;

public class Key {

    public String name;
    public Object[] key;

    public Key() {

    }

    public Key(String name, Object[] key) {
        this.name = name;
        this.key = key;
    }

    public static boolean contains(List<Key> keys, String keyName) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).name.equals(keyName)) {
                return true;
            }
        }
        return false;
    }
}
