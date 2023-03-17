package controller.marketing;

import api.GridResult;
import api.MySQLCol;
import java.sql.Connection;
import utilities.MySQLQuery;

public class ContractController {

    public static int getSearchGridCount(String type, String find, int by, Connection conn) throws Exception {
        ///type es por comodato o afiliado 
        String qb = "";
        if (find != null && !find.isEmpty()) {
            if (by == 1 || by == 3) {
                qb = "SELECT count(*) FROM contract co ";
                if (by == 1) {
                    qb += "WHERE co.document like '%" + find + "%' ";
                } else if (by == 3) {
                    qb += "WHERE concat(ifnull(co.first_name,' '),' ',ifnull(co.last_name,' '))  like '%" + find + "%' ";
                }
            } else if (by == 2 || by == 4 || by == 5) {
                qb = "SELECT count(*) FROM contract co "
                        + "LEFT JOIN neigh n ON n.id  = co.neigh_id ";
                if (by == 2) {
                    qb += "WHERE co.contract_num like '%" + find + "%' ";
                } else if (by == 4) {
                    qb += "WHERE concat(co.address,\" \",n.name) like '%" + find + "%' ";
                } else if (by == 5) {
                    qb += "WHERE co.phones like '%" + find + "%' ";
                }
            }
            qb += " AND co.ctr_type = '" + type + "'";

            Long nRec = new MySQLQuery(qb).getAsLong(conn);
            return (int) Math.ceil(nRec / 40.0d);
        } else {
            return 0;
        }
    }

    public static GridResult getSearchGrid(String type, String find, int by, int page, Connection conn) throws Exception {
        GridResult r = new GridResult();
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 12, "Fecha"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 12, "Contrato"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 27, "Cliente"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Documento"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 28, "Dirección"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Poblado"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Teléfono"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Estado"),};

        r.sortColIndex = 1;

        if (by < 6) {
            String qb = "SELECT "
                    + "co.id, "
                    + "c.id, "
                    + "z.id, "
                    + "co.`sign_date`, "
                    + "co.`contract_num`, "
                    + "CONCAT(COALESCE(co.first_name,''), \" \" ,COALESCE(co.last_name,'')), "
                    + "co.document, "
                    + "TRIM(CONCAT(COALESCE(co.address ,''),\" \",COALESCE(n.name,''))), "
                    + "c.name, "
                    + "co.phones, "
                    + "         IF(co.anull_cause_id is not null,"
                    + "              \"Anulado\", "
                    + "              IF(co.cancel_cause_id is not null, "
                    + "                      \"Cancelado\","
                    + "                  IF(co.ctr_type = 'afil', \"Normal\", IF(como_collect_date IS NULL, \"Normal\", \"Recogido\"))       ) )  "
                    + "          ";

            if (by == 1 || by == 3) {
                qb += "FROM  contract co "
                        + "LEFT JOIN neigh n      ON n.id  = co.neigh_id "
                        + "LEFT JOIN sector s     ON s.id  = n.sector_id "
                        + "LEFT JOIN city c       ON c.id  = s.city_id "
                        + "LEFT JOIN zone z       ON z.id  = c.zone_id "
                        + "LEFT JOIN employee e   ON e.id  = co.sower "
                        + "LEFT JOIN vehicle v    ON v.id = co.vehicle_id ";
                if (by == 1) {
                    qb += "WHERE co.ctr_type = '" + type + "' AND co.document like '%" + find + "%' ";

                } else if (by == 3) {
                    qb += "WHERE co.ctr_type = '" + type + "' AND concat(ifnull(co.first_name,' '),' ',ifnull(co.last_name,' '))  like '%" + find + "%' ";
                }

            } else if (by == 2 || by == 4 || by == 5) {
                qb += "FROM contract co "
                        + "INNER JOIN neigh n      ON n.id  = co.neigh_id "
                        + "INNER JOIN sector s     ON s.id  = n.sector_id "
                        + "INNER JOIN city c       ON c.id  = s.city_id "
                        + "INNER JOIN zone z       ON z.id  = c.zone_id "
                        + "LEFT JOIN employee e   ON e.id  = co.sower "
                        + "LEFT JOIN vehicle v    ON v.id = co.vehicle_id ";
                if (by == 2) {
                    qb += "WHERE co.ctr_type = '" + type + "' AND co.contract_num like '%" + find + "%' ";
                } else if (by == 4) {
                    qb += "WHERE co.ctr_type = '" + type + "' AND concat(co.address,\" \",n.name) like '%" + find + "%' ";
                } else if (by == 5) {
                    qb += "WHERE co.ctr_type = '" + type + "' AND co.phones like '%" + find + "%' ";
                }
            }

            qb += " LIMIT " + ((page * 40)) + ", 40 ";
            r.data = new MySQLQuery(qb).getRecords(conn);
        } else {
            r.data = new Object[0][0];
        }
        return r;
    }
}
