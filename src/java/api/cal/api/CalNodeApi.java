package api.cal.api;

import api.BaseAPI;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.cast;

@Path("/calNode")
public class CalNodeApi extends BaseAPI {

    static class DocInfo {

        Integer id;
        Integer parId;
        Integer num;
        String letter;
        Integer zeroFill;
        String prefix;
        String numSep;
        Boolean showNum;
        Boolean startWithType;
        Boolean endWithNum;
        Boolean showMacro;
        Boolean encoded;

        public DocInfo(Connection conn, int nodeId) throws Exception {
            Object[] rs = new MySQLQuery("SELECT "
                    + "n.id, "//1
                    + "par_id, "//2                    
                    + "num, "//3
                    + "letter, "//4                    
                    + "zero_fill, "//5
                    + "prefix, "//6
                    + "num_sep, "//7
                    + "show_num, "//8
                    + "start_with_type,"//9
                    + "end_with_num,"//10
                    + "show_macro, "//11
                    + "encoded "//12
                    + "FROM cal_node n "
                    + "LEFT JOIN cal_doc_type dt ON dt.id = doc_id "
                    + "WHERE n.id = " + nodeId).getRecord(conn);

            id = cast.asInt(rs, 1);
            parId = cast.asInt(rs, 2);
            num = cast.asInt(rs, 3);
            letter = cast.asString(rs, 4);
            zeroFill = cast.asInt(rs, 5);
            prefix = cast.asString(rs, 6);
            numSep = cast.asString(rs, 7);
            showNum = cast.asBoolean(rs, 8);
            startWithType = cast.asBoolean(rs, 9);
            endWithNum = cast.asBoolean(rs, 10);
            showMacro = cast.asBoolean(rs, 11);
            encoded = cast.asBoolean(rs, 12);
        }
    }

    @PUT
    @Path("/codes")
    public Response generateCodes() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Boolean generate = new MySQLQuery("SELECT generate_codes FROM cal_cfg").getAsBoolean(conn);
            Object[][] data = new MySQLQuery("SELECT id FROM cal_node").getRecords(conn);
            for (Object[] row : data) {
                int id = cast.asInt(row, 0);
                if (generate) {
                    new MySQLQuery("UPDATE cal_node SET code = '" + getCode(id, conn) + "' WHERE id = " + id).executeUpdate(conn);
                } else {
                    new MySQLQuery("UPDATE cal_node SET code = '' WHERE code IS NULL AND id = " + id).executeUpdate(conn);
                }
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/code")
    public Response updateCode(@QueryParam("nodeId") int nodeId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (new MySQLQuery("SELECT generate_codes FROM cal_cfg").getAsBoolean(conn)) {
                new MySQLQuery("UPDATE cal_node SET code = '" + getCode(nodeId, conn) + "' WHERE id = " + nodeId).executeUpdate(conn);
            } else {
                new MySQLQuery("UPDATE cal_node SET code = '' WHERE code IS NULL AND id = " + nodeId).executeUpdate(conn);
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static String getCode(int nodeId, Connection conn) throws Exception {
        Object[] cfgRow = new MySQLQuery("SELECT doc_root, doc_root_prefix, doc_macro_prefix, doc_proc_prefix FROM cal_cfg").getRecord(conn);
        String root = cfgRow[0] != null ? (String) cfgRow[0] : "";
        String rootPrefix = cfgRow[1] != null ? (String) cfgRow[1] : "";
        String macroPrefix = cfgRow[2] != null ? (String) cfgRow[2] : "";
        String procPrefix = cfgRow[3] != null ? (String) cfgRow[3] : "";

        Integer parId = nodeId;

        List<DocInfo> parents = new ArrayList<>();
        while (parId != null && parId > 0) {
            parents.add(new DocInfo(conn, parId));
            parId = parents.get(parents.size() - 1).parId;
        }

        DocInfo doc = parents.get(0);
        if (!doc.encoded) {
            return "";
        }

        String docNum = null;
        if (doc.num != null) {
            if (doc.zeroFill > 0) {
                docNum = String.format("%0" + doc.zeroFill + "d", doc.num);
            } else {
                docNum = doc.num.toString();
            }
        }
        //agregando la abreviatura del tipo de documento
        String cod = (doc.startWithType && doc.letter != null) ? doc.letter : "";
        //pegando el número
        cod += (!doc.endWithNum ? doc.numSep + docNum : "");

        //raiz
        if (root != null && !root.isEmpty()) {
            cod += (cod.isEmpty() ? "" : rootPrefix);
            cod += root;
        }
        //macroproceso
        if (doc.showMacro) {
            String macro = new MySQLQuery("SELECT t.letter FROM cal_proc_type AS t INNER JOIN cal_proc AS p ON p.proc_type_id = t.id INNER JOIN cal_node AS n ON n.proc_id = p.id WHERE n.id = " + nodeId).getAsString(conn);
            cod += (cod.isEmpty() ? "" : macroPrefix);
            cod += (macro);
        }

        //proceso
        Object[] procRow = new MySQLQuery("SELECT show_in_codes, p.short_name FROM cal_proc AS p INNER JOIN cal_node AS n ON n.proc_id = p.id WHERE n.id = " + nodeId).getRecord(conn);
        if ((Boolean) procRow[0]) {
            cod += (cod.isEmpty() ? "" : procPrefix);
            cod += (procRow[1]);
        }

        //el primero es el propio nodo y se ignora en este paso
        for (int i = parents.size() - 1; i > 0; i--) {
            DocInfo row = parents.get(i);
            String num;
            if (row.zeroFill > 0) {
                num = String.format("%0" + row.zeroFill + "d", row.num);
            } else {
                num = row.num.toString();
            }
            cod += (row.prefix + row.letter + row.numSep + num);
        }

        if (doc.startWithType) {
            if (doc.showNum && doc.endWithNum) {
                cod += doc.numSep + docNum;
            }
        } else {
            if (doc.showNum && (doc.letter != null && !doc.letter.isEmpty())) {
                cod += (doc.prefix + doc.letter);
                if (doc.endWithNum) {
                    cod += (doc.numSep + docNum);
                }
            }
        }
        return cod;
    }
}
