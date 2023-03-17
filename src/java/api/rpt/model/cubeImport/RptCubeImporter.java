package api.rpt.model.cubeImport;

import api.rpt.api.dataTypes.DataType;
import api.rpt.model.CubeInfo;
import api.rpt.model.RptCubeCond;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptCubeTbl;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utilities.MySQLQuery;
import static utilities.MySQLQuery.getEnumOptionsAsMatrix;
import utilities.Strings;

public class RptCubeImporter {

    private static final List<String> CAST_TYPES = new ArrayList<>();

    static {
        CAST_TYPES.add("DATE");
        CAST_TYPES.add("DATETIME");
        CAST_TYPES.add("TIME");
        CAST_TYPES.add("CHAR");
        CAST_TYPES.add("SIGNED");
        CAST_TYPES.add("UNSIGNED");
        CAST_TYPES.add("BINARY");
    }

    private static String removeAS(String l) {
        for (int i = 0; i < CAST_TYPES.size(); i++) {
            String type = CAST_TYPES.get(i);
            l = l.replaceAll("(?i)AS " + type, "AS_" + type);
        }
        l = l.replaceAll("(?i) AS ", " ").replaceAll("[ ]+", " ").trim();

        for (int i = 0; i < CAST_TYPES.size(); i++) {
            String type = CAST_TYPES.get(i);
            l = l.replaceAll("(?i)AS_" + type, "AS " + type);
        }
        return l;
    }

    private static List<String> getAliases(String line) {
        Matcher mat = Pattern.compile("[A-Za-z0-9_]+\\.").matcher(line);
        List<String> tblAlias = new ArrayList<>();
        while (mat.find()) {
            String alias = line.substring(mat.start(), mat.end() - 1);
            if (!tblAlias.contains(alias)) {
                tblAlias.add(alias);
            }
        }
        return tblAlias;
    }

    public static void importSQL(File file, int cubeId, Connection ep) throws Exception {
        try {
            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM rpt_rpt r WHERE r.cube_id = ?1").setParam(1, cubeId).getAsBoolean(ep)) {
                throw new Exception("El cubo ya tiene reportes.");
            }
            String q = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            List<String> lines = new ArrayList<>();
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    lines.add(removeAS(scanner.nextLine()));
                }
            }

            new MySQLQuery("set foreign_key_checks=0;").executeUpdate(ep);
            new MySQLQuery("DELETE FROM rpt_cube_fld WHERE cube_id = " + cubeId + ";").executeUpdate(ep);
            new MySQLQuery("DELETE FROM rpt_cube_tbl WHERE cube_id = " + cubeId + ";").executeUpdate(ep);
            new MySQLQuery("DELETE FROM rpt_cube_cond WHERE cube_id = " + cubeId + ";").executeUpdate(ep);

            Map<String, RptCubeTbl> tbls = new HashMap<>();

            //READING TABLES
            boolean from = false;
            for (String line : lines) {
                switch (line.trim().toUpperCase()) {
                    case "SELECT":
                        from = false;
                        break;
                    case "FROM":
                        from = true;
                        break;
                    case "WHERE":
                        from = false;
                        break;
                    default:
                        if (from) {
                            RptCubeTbl tbl = new RptCubeTbl();
                            tbl.cubeId = cubeId;
                            if (line.toUpperCase().startsWith("INNER JOIN") || line.startsWith("LEFT JOIN")) {
                                String[] parts = line.split(" ");
                                tbl.type = parts[0].toLowerCase();
                                tbl.tbl = parts[2];
                                tbl.alias = parts[3];
                                if (parts[4].toUpperCase().equals("ON")) {
                                    String extra = "";
                                    for (int i = 5; i < parts.length; i++) {
                                        extra += parts[i] + " ";
                                    }
                                    if (!extra.isEmpty()) {
                                        extra = extra.replaceAll(tbl.alias + "\\.", "@.");
                                        List<String> aliases = getAliases(extra);

                                        for (int i = 0; i < aliases.size(); i++) {
                                            String alias = aliases.get(i);
                                            if (!tbls.containsKey(alias)) {
                                                throw new Exception("La tabla " + alias + " no encuentra.");
                                            }
                                            extra = extra.replaceAll(alias + "\\.", "@" + (i + 1) + ".");
                                        }
                                        tbl.cond = extra;

                                        if (aliases.size() > 0) {
                                            tbl.tbl1Id = tbls.get(aliases.get(0)).id;
                                        }
                                        if (aliases.size() > 1) {
                                            tbl.tbl2Id = tbls.get(aliases.get(1)).id;
                                        }
                                        if (aliases.size() > 2) {
                                            tbl.tbl3Id = tbls.get(aliases.get(2)).id;
                                        }
                                        if (aliases.size() > 3) {
                                            tbl.tbl4Id = tbls.get(aliases.get(3)).id;
                                        }
                                        if (aliases.size() > 4) {
                                            tbl.tbl5Id = tbls.get(aliases.get(4)).id;
                                        }
                                    }
                                } else {
                                    throw new Exception("Line no cumple el inner: " + line);
                                }
                            } else {
                                tbl.type = "main";
                                String[] parts = line.split(" ");
                                tbl.tbl = parts[0];
                                tbl.alias = parts[1];
                            }

                            tbl.place = tbls.size();
                            tbl.insert(ep);
                            tbls.put(tbl.alias, tbl);
                        }
                }
            }
            
            Object[][] rawData = new MySQLQuery(q + " LIMIT 200").getRecords(ep);
            //Object[][] rawData = new MySQLQuery(q + "\nORDER BY " + main.alias + ".id DESC\nLIMIT 200").getRecords(ep);

            int flds = 0;
            String stage = "";
            for (String line : lines) {
                switch (line.trim().toUpperCase()) {
                    case "SELECT":
                        stage = "SELECT";
                        break;
                    case "FROM":
                        stage = "FROM";
                        break;
                    case "WHERE":
                        stage = "WHERE";
                        break;
                    default:
                        switch (stage) {
                            case "SELECT":
                                RptCubeFld fld = new RptCubeFld();
                                fld.cubeId = cubeId;
                                fld.fldType = "fld";
                                Pattern p = Pattern.compile("^([a-zA-Z_0-9]+)[.]([a-zA-Z_0-9`]+)[ ]*([a-zA-Z_0-9áéíóúÁÉÍÓÚ]*),*");
                                Matcher m = p.matcher(line);
                                if (m.find()) {
                                    if (!tbls.containsKey(m.group(1))) {
                                        throw new Exception("No se encuentra la tabla: " + m.group(1) + "\n" + line);
                                    }

                                    fld.tbl1Id = tbls.get(m.group(1)).id;
                                    fld.name = m.group(2).replaceAll("`", "");
                                    if (m.groupCount() > 2 && (m.group(3) != null && !m.group(3).isEmpty())) {
                                        fld.dspName = m.group(3).replaceAll("_", " ");
                                    } else {
                                        fld.dspName = tbls.get(m.group(1)).tbl + "." + fld.name + "(" + flds + ")";
                                    }
                                    String type = (String) new MySQLQuery("SHOW COLUMNS FROM " + tbls.get(m.group(1)).tbl + " WHERE field = '" + fld.name + "';").getRecord(ep)[1];
                                    fld.dataType = DataType.getFromMySQLType(type).getName();
                                } else {
                                    fld.fldType = "custom";

                                    List<String> tblAlias = getAliases(line);
                                    List<String> foundAlias = new ArrayList<>();

                                    for (int i = 0; i < tblAlias.size(); i++) {
                                        if (tbls.containsKey(tblAlias.get(i))) {
                                            line = line.replaceAll(tblAlias.get(i) + "\\.", "@" + (foundAlias.size() + 1) + ".");
                                            foundAlias.add(tblAlias.get(i));
                                        }
                                    }

                                    try {
                                        if (foundAlias.size() > 0) {
                                            fld.tbl1Id = tbls.get(foundAlias.get(0)).id;
                                        }
                                        if (foundAlias.size() > 1) {
                                            fld.tbl2Id = tbls.get(foundAlias.get(1)).id;
                                        }
                                        if (foundAlias.size() > 2) {
                                            fld.tbl3Id = tbls.get(foundAlias.get(2)).id;
                                        }
                                        if (foundAlias.size() > 3) {
                                            fld.tbl4Id = tbls.get(foundAlias.get(3)).id;
                                        }
                                        if (foundAlias.size() > 4) {
                                            fld.tbl5Id = tbls.get(foundAlias.get(4)).id;
                                        }
                                    } catch (NullPointerException ex) {
                                        throw ex;
                                    }

                                    Matcher dspMatch = Pattern.compile("([ ]+[a-zA-Z_0-9áéíóúÁÉÍÓÚ']+,*)$").matcher(line);
                                    if (dspMatch.find()) {
                                        fld.dspName = line.substring(dspMatch.start(), dspMatch.end()).replaceAll(",", "").replaceAll("_", " ").replaceAll("'", " ").trim();
                                        fld.dspName = Strings.toTitleType(fld.dspName);
                                        fld.query = line.replaceAll("([ ]+[a-zA-Z_0-9áéíóúÁÉÍÓÚ']+,*)$", "");
                                    } else {
                                        fld.dspName = flds + "";
                                        fld.query = line.replaceAll("[ ]*,$", "");
                                    }

                                    for (Object[] row : rawData) {
                                        Object val = row[flds];
                                        if (val != null) {
                                            fld.dataType = DataType.getFromJavaClass(val.getClass()).getName();
                                            break;
                                        }
                                    }
                                }

                                fld.place = flds;
                                fld.insert(ep);
                                flds++;
                            case "FROM":
                                break;
                            case "WHERE":
                                RptCubeCond cond = new RptCubeCond();
                                cond.cubeId = cubeId;

                                List<String> tblAlias = getAliases(line);
                                for (int i = 0; i < tblAlias.size(); i++) {
                                    line = line.replaceAll(tblAlias.get(i) + "\\.", "@" + (i + 1) + ".");
                                }
                                try {
                                    if (tblAlias.size() > 0) {
                                        cond.tbl1Id = tbls.get(tblAlias.get(0)).id;
                                    }
                                    if (tblAlias.size() > 1) {
                                        cond.tbl2Id = tbls.get(tblAlias.get(1)).id;
                                    }
                                    if (tblAlias.size() > 2) {
                                        cond.tbl3Id = tbls.get(tblAlias.get(2)).id;
                                    }
                                    if (tblAlias.size() > 3) {
                                        cond.tbl4Id = tbls.get(tblAlias.get(3)).id;
                                    }
                                    if (tblAlias.size() > 4) {
                                        cond.tbl5Id = tbls.get(tblAlias.get(4)).id;
                                    }
                                } catch (NullPointerException ex) {
                                    throw ex;
                                }
                                cond.query = line;
                                cond.insert(ep);
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            new MySQLQuery("set foreign_key_checks=0;").executeUpdate(ep);
            new MySQLQuery("DELETE FROM rpt_cube_fld WHERE cube_id = " + cubeId + ";").executeUpdate(ep);
            new MySQLQuery("DELETE FROM rpt_cube_tbl WHERE cube_id = " + cubeId + ";").executeUpdate(ep);
            new MySQLQuery("DELETE FROM rpt_cube_cond WHERE cube_id = " + cubeId + ";").executeUpdate(ep);
            throw ex;
        }

    }

    public static String createQuery(boolean html, int cubeId, boolean count, Connection conn) throws Exception {
        Builder hb = (html ? new HtmlBuilder() : new PlainBuilder());

        CubeInfo info = new CubeInfo(cubeId, conn);
        hb.blue("SELECT").br();
        if (count) {
            hb.black(" count(*) ");
        } else {
            for (int i = 0; i < info.cubeFlds.size(); i++) {
                RptCubeFld fld = info.cubeFlds.get(i);
                if (!fld.fldType.equals("label")) {
                    switch (fld.fldType) {
                        case "fld": {
                            RptCubeTbl tbl = fld.tbls[0];
                            if (fld.dataType.equals("enum")) {
                                if (tbl.dataClass == null) {
                                    throw new RuntimeException("Debe configurar la clase de la tabla " + tbl.tbl);
                                }

                                String[][] opts = getEnumOptionsAsMatrix(fld.enumOpts);
                                hb.black("CASE ").olive(tbl.alias).blue(".").olive(fld.name).br();
                                for (String[] optRow : opts) {
                                    hb.black("WHEN ").olive("'" + optRow[0] + "'").black(" THEN ").olive("'" + optRow[1] + "'").br();
                                }
                                hb.blue("END");
                            } else {
                                DataType dt = DataType.getType(fld.dataType);
                                String fx = dt.getFunction(tbl.alias + "." + fld.name);
                                if (fx.equals(tbl.alias + "." + fld.name)) {
                                    hb.olive(tbl.alias).blue(".").olive(fld.name);
                                } else {
                                    hb.blueReg(fx);
                                }
                            }
                        }
                        break;
                        case "custom":
                            String q = fld.query;
                            for (int j = 0; j < fld.tbls.length; j++) {
                                q = q.replaceAll("@" + (j + 1), fld.tbls[j].alias);
                            }
                            hb.blueReg(q);
                            break;
                        default:
                            throw new Exception("Tipo no reconocido: " + fld.fldType);
                    }
                    if (i < info.cubeFlds.size() - 1) {
                        hb.blue(",");
                    }
                    if (html) {
                        hb.gray(" -- ").gray(fld.dspName).br();
                    } else {
                        hb.sp();
                    }
                }
            }
        }

        hb.blue("FROM").br();
        for (RptCubeTbl tbl : info.cubeTbls) {
            if (tbl.type.equals("main")) {
                hb.fucsia(tbl.tbl).sp();
                hb.olive(tbl.alias);
            } else {
                if (tbl.type.equals("inner")) {
                    hb.blue("INNER ");
                } else if (tbl.type.equals("left")) {
                    hb.blue("LEFT ");
                }
                hb.blue("JOIN ").fucsia(tbl.tbl).sp().olive(tbl.alias).blue(" ON ");

                //hb.olive(tbl.alias).blue(".").olive(tbl.ownKey).blueReg(" = ").olive(ft.alias).blue(".").olive(tbl.extKey);
                if (tbl.cond != null && !tbl.cond.isEmpty()) {
                    String cond = tbl.cond.replaceAll("@\\.", tbl.alias + ".");
                    for (int j = 0; j < tbl.tbls.length; j++) {
                        RptCubeTbl t = tbl.tbls[j];
                        cond = cond.replaceAll("@" + (j + 1) + "\\.", t.alias + ".");
                    }
                    hb.blueReg(" " + cond);
                }
            }
            hb.br();
        }
        if (info.cubeConds.size() > 0) {
            hb.blue("WHERE ").br();
            for (int i = 0; i < info.cubeConds.size(); i++) {
                RptCubeCond cond = info.cubeConds.get(i);
                for (int j = 0; j < cond.tbls.length; j++) {
                    RptCubeTbl tbl = cond.tbls[j];
                    cond.query = cond.query.replaceAll("@" + (j + 1) + "\\.", tbl.alias + ".");
                }
                hb.blueReg(cond.query);
                if (i < info.cubeConds.size() - 1) {
                    hb.blue(" AND ");
                }
            }
        }
        return hb.toString();
    }
}
