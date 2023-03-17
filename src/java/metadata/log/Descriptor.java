package metadata.log;

import metadata.model.Field;
import metadata.model.Table;
import api.MySQLCol;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Descriptor {

    public static String getDescription(String tblName, int id, Connection conn) throws Exception {
        return new MySQLQuery(getDescQuery(tblName, id, null, null)).getAsString(conn);
    }

    public static String getDescQuery(String tblName, Integer id, Field fkFld, Boolean active) throws Exception {
        Table tbl = Table.getByName(tblName);
        if (tbl == null) {
            throw new Exception("No hay metadatos de la tabla " + tblName);
        }
        List<Field> flds = tbl.getToStrFields();
        if (flds.isEmpty()) {
            throw new Exception("No hay toStrFields para describir " + tblName);
        }

        StringBuilder sb = new StringBuilder("(SELECT " + (id == null && fkFld == null ? tbl.name + ".id, " : "") + "IFNULL(CONCAT(");
        for (int i = 0; i < flds.size(); i++) {
            Field fld = flds.get(i);
            if (fld.pk) {
                sb.append(fld.name);
            } else if (fld.fk) {
                sb.append(getDescQuery(fld.fkTblName, null, fld, null));
            } else {
                int type = MySQLCol.getConstFromStr(fld.format);
                if (MySQLCol.hasFormat(type)) {
                    sb.append(MySQLCol.getDefaultSQLFormat(fld.name, type));
                } else if (type == MySQLCol.TYPE_ENUM) {
                    sb.append("CASE ").append(fld.name).append(" ");
                    for (String[] emunOpt : fld.emunOpts) {
                        sb.append("when '").append(emunOpt[0]).append("' then '").append(emunOpt[1]).append("' ");
                    }
                    sb.append("END ");
                } else if (type == MySQLCol.TYPE_BOOLEAN) {
                    sb.append("IF(").append(fld.name).append(", 'Si', 'No')");
                } else {
                    sb.append(fld.name);
                }
            }
            if (i < flds.size() - 1) {
                sb.append(", ' - ' ,");
            }
        }

        sb.append("), '') FROM ").append(tbl.name);
        if (fkFld != null) {
            sb.append(" WHERE ").append(tbl.name).append(".id = ");
            sb.append(Table.getByName(fkFld.tblName).name).append(".").append(fkFld.name);
        } else if (id != null) {
            sb.append(" WHERE ").append(tbl.name).append(".id = ");
            sb.append(id);
        } else if (active != null && tbl.activeFldId != null) {
            Field aFld = Field.getById(tbl.activeFldId);
            sb.append(" WHERE ").append(tbl.name).append(".").append(aFld.name).append(" = ").append(active ? "1" : "0");
        }
        sb.append(")");
        return sb.toString();
    }

}
