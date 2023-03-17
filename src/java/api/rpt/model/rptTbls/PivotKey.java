package api.rpt.model.rptTbls;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PivotKey {

    Object[] key;
    Integer join;
    int pos;

    public PivotKey(Object[] key, int pos, int join) {
        this.key = key;
        this.pos = pos;
        this.join = join;
    }

    public PivotKey(Object[] key, int pos) {
        this(key, pos, 0);
        join = null;
    }

    public static PivotKey find(List<PivotKey> lst, Object[] key, Integer join) {
        for (int i = 0; i < lst.size(); i++) {
            PivotKey lKey = lst.get(i);
            if (lKey.key.length == key.length) {
                boolean match = true;
                for (int j = 0; j < key.length && match; j++) {
                    if (!Objects.equals(lKey.key[j], key[j])) {
                        match = false;
                    }
                }
                if (match && Objects.equals(join, lKey.join)) {
                    return lKey;
                }
            }
        }
        return null;
    }

    public static void addKey(List<Object[]> keys, Object[] key) {
        for (int i = 0; i < keys.size(); i++) {
            if (equals(key, keys.get(i))) {
                return;
            }
        }
        keys.add(key);
    }

    private static boolean equals(Object[] k1, Object[] k2) {
        if (k1.length != k2.length) {
            return false;
        }
        for (int j = 0; j < k1.length; j++) {
            if (!Objects.equals(k1[j], k2[j])) {
                return false;
            }
        }
        return true;
    }

    public static void sortMat(List<Object[]> mat) {
        Collections.sort(mat, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] l1, Object[] l2) {
                for (int i = 0; i < l1.length; i++) {
                    int c;
                    if (l1[i] == null && l2[i] == null) {
                        c = 0;
                    } else if (l1[i] != null && l2[i] == null) {
                        c = -1;
                    } else if (l1[i] == null && l2[i] != null) {
                        c = 1;
                    } else if (l1[i] != null && l2[i] != null) {
                        c = ((Comparable) l1[i]).compareTo((Comparable) l2[i]);
                    } else {
                        throw new RuntimeException();
                    }
                    if (c != 0) {
                        return c;
                    }
                }
                return 0;
            }
        });
    }
}
