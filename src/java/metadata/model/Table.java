package metadata.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import metadata.FrmCfg;
import metadata.log.Diff;
import utilities.json.JSONDecoder;

public class Table {

    public String name;
    public String plural;
    public String singular;
    public boolean male;
    public String alias;
    public String module;
    public List<Field> fields = new ArrayList<>();
    public List<String> toStrFldIds = new ArrayList<>();
    public List<Grid> grids = new ArrayList<>();
    public List<GridGroup> groups = new ArrayList<>();
    public List<Form> forms = new ArrayList<>();
    public List<Unique> uniques = new ArrayList<>();
    public boolean billing = false;

    public String activeFldId;
    public static final Map<String, Table> TABLES_CACHE = new HashMap<>();
    public static final Map<String, Field> FIELDS_CACHE = new HashMap<>();
    private static Table[] ALL_TBLS = null;

    public static final boolean DEVEL_MODE;

    static {

        String path = Table.class.getResource("").getPath();
        boolean domain = path.contains("domain1");
        boolean build = path.contains("build");

        if (domain && build) {
            throw new RuntimeException("No se pudo determinar el tipo de entorno");
        } else if (domain && build) {
            throw new RuntimeException("No se pudo determinar el tipo de entorno");
        }

        DEVEL_MODE = !domain && build;

        if (!DEVEL_MODE) {
            try {
                List<String> tblNames = ((Tables) readJson("tables", Tables.class)).tables;
                for (String tblName : tblNames) {
                    Table tbl = (Table) readJson(tblName, Table.class);
                    TABLES_CACHE.put(tblName, tbl);
                    for (int i = 0; i < tbl.fields.size(); i++) {
                        Field fld = tbl.fields.get(i);
                        FIELDS_CACHE.put(fld.id, fld);
                    }
                    ALL_TBLS = TABLES_CACHE.values().toArray(new Table[0]);
                }
            } catch (Exception ex) {
                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static boolean hasUpperOrNum(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (65 <= c && c <= 90) {
                return true;
            } else if (48 <= c && c <= 57) {
                return true;
            }
        }
        return false;
    }

    public static Table getByName(String name) throws Exception {
        if (hasUpperOrNum(name)) {
            name = Diff.toDbName(name);
        }
        if (!TABLES_CACHE.isEmpty()) {
            return TABLES_CACHE.get(name);
        } else {
            return (Table) readJson(name, Table.class);
        }
    }

    public static Object readJson(String name, Class cs) throws Exception {
        if (DEVEL_MODE) {
            File f = new File(FrmCfg.getBackPath() + "\\json\\" + name + ".json");
            if (f.exists()) {
                try (InputStream is1 = new FileInputStream(f)) {
                    return new JSONDecoder().getObject(is1, cs);
                }
            } else {
                throw new NotFoundException("No se halló: " + name + ".json");
            }
        } else {
            try (InputStream is = FrmCfg.class.getResourceAsStream("./json/" + name + ".json")) {
                if (is != null) {
                    return new JSONDecoder().getObject(is, cs);
                } else {
                    throw new NotFoundException("No se halló: " + name + ".json");
                }
            }
        }
    }

    public static Table[] getAll() throws Exception {
        if (!TABLES_CACHE.isEmpty()) {
            return ALL_TBLS;
        } else {
            List<String> tblNames = ((Tables) readJson("tables", Tables.class)).tables;
            Table[] tbls = new Table[tblNames.size()];
            for (int i = 0; i < tbls.length; i++) {
                tbls[i] = (Table) readJson(tblNames.get(i), Table.class);
                if (tbls[i] == null) {
                    throw new Exception(tblNames.get(i));
                }
            }
            return tbls;
        }
    }

    public Field getFieldByName(String name) {
        if (hasUpperOrNum(name)) {
            name = Diff.toDbName(name);
        }
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            //System.out.println("SERVIDOR ACTUALIZACION VALIDACION CAMPOS f "+f.name +" A COMPARAR "+name);
            if (f.name.equals(name)) {
                return f;
            }
        }
        return null;
    }

    public Field getFieldById(String id) {
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            if (f.id.equals(id)) {
                return f;
            }
        }
        return null;
    }

    public Grid getGridByName(String name) {
        for (int i = 0; i < grids.size(); i++) {
            Grid g = grids.get(i);
            if (g.name.equals(name)) {
                return g;
            }
        }
        return null;
    }

    public GridGroup getGroupByName(String name) {
        if (groups != null) {
            for (int i = 0; i < groups.size(); i++) {
                GridGroup g = groups.get(i);
                if (g.name.equals(name)) {
                    return g;
                }
            }
        }
        return null;
    }

    public Form getFormByName(String name) {
        for (int i = 0; i < forms.size(); i++) {
            Form f = forms.get(i);
            if (f.name.equals(name)) {
                return f;
            }
        }
        return null;
    }

    public List<Field> getToStrFields() {
        List<Field> rta = new ArrayList<>();
        for (int i = 0; i < toStrFldIds.size(); i++) {
            rta.add(getFieldById(toStrFldIds.get(i)));
        }
        return rta;
    }

}
