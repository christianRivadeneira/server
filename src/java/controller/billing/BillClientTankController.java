package controller.billing;

import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillClientTank;
import api.bill.model.dto.DataClientRequest;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillClientTankController {

    public static MySQLPreparedQuery getCartBySpanQuery(int cartAcc, Connection conn) throws SQLException {
        return getCartBySpanQuery(cartAcc, null, conn);
    }

    public static MySQLPreparedQuery getCartBySpanQuery(int cartAcc, Integer cartAcc2, Connection conn) throws SQLException {
        String cond;
        if (cartAcc2 != null) {
            cond = "account_deb_id IN (" + cartAcc + "," + cartAcc2 + ")";
        } else {
            cond = "account_deb_id = " + cartAcc + "";
        }
        return new MySQLPreparedQuery("SELECT t.bill_span_id, sum(t.value) FROM bill_transaction t where t.cli_tank_id = ?1 AND " + cond + " group by t.bill_span_id ORDER BY t.bill_span_id DESC", conn);
    }

    public static int getDebtsMonthsClient(int clientId, int cartAcc, Connection conn) throws Exception {
        MySQLPreparedQuery debtsQ = BillClientTankController.getCartBySpanQuery(cartAcc, conn);
        MySQLQuery credQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2");
        MySQLQuery debQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2");
        credQ.setParam(1, cartAcc);
        credQ.setParam(2, clientId);
        debQ.setParam(1, cartAcc);
        debQ.setParam(2, clientId);
        BigDecimal debit = debQ.getAsBigDecimal(conn, true);
        BigDecimal credit = credQ.getAsBigDecimal(conn, true);
        BigDecimal value = debit.subtract(credit);
        return getDebtMonths(clientId, debtsQ, conn, value);

    }

    public static int getDebtMonths(int clientId, MySQLPreparedQuery debtsQ, Connection conn, BigDecimal totalDebt) throws Exception {
        if (totalDebt.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        debtsQ.setParameter(1, clientId);
        Object[][] debts = debtsQ.getRecords();
        int months = 0;
        for (int i = 0; i < debts.length && totalDebt.compareTo(BigDecimal.ZERO) > 0; i++) {
            Object[] debtRow = debts[i];
            BigDecimal debt = (BigDecimal) debtRow[1];
            totalDebt = totalDebt.subtract(debt);
            months++;
        }
        return months;
    }

    public static GridResult getTankClientsPage(DataClientRequest req, Connection conn) throws Exception {
        List<MySQLCol> cols = new ArrayList<>();
        cols.add(new MySQLCol(MySQLCol.TYPE_KEY));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 90, "Cód. Edificio"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Edificio"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Num. Instalación"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Ref. Pago"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Nombres"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Documento"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 130, "Medidor"));
        cols.add(new MySQLCol(MySQLCol.TYPE_BOOLEAN, 60, "Activo"));
        GridResult gr = new GridResult();
        gr.cols = cols.toArray(new MySQLCol[cols.size()]);
        String filter = req.getFiltQuery();
        if (req.page >= 0) {
            String qb = "SELECT c.id, b.old_id, b.name, c.num_install, c.code, CONCAT(c.first_name,' ', COALESCE(c.last_name,'')), c.doc, IFNULL((SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1), ''), c.active FROM bill_client_tank c "
                    + "INNER JOIN bill_building b ON b.id = c.building_id WHERE " + filter + " LIMIT " + (req.page * 30) + ", 30";

            Object[][] data = new MySQLQuery(qb).getRecords(conn);
            for (Object[] row : data) {
                row[1] = BillSpanController.zeroFill(MySQLQuery.getAsString(row[1]), 3);
            }
            gr.data = data;
        } else {
            gr.data = new Object[][]{};
        }
        return gr;
    }

    public static GridResult getNetClientsPage(DataClientRequest req, Connection conn) throws Exception {
        List<MySQLCol> cols = new ArrayList<>();
        cols.add(new MySQLCol(MySQLCol.TYPE_KEY));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 190, "Dirección"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 120, "Barrio"));
        cols.add(new MySQLCol(MySQLCol.TYPE_ENUM, 75, "Sector", new BillClientTank().getEnumOpts("sector_type")));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 35, "Estrato"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 130, "Código"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Nombres"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Documento"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 130, "Medidor"));
        cols.add(new MySQLCol(MySQLCol.TYPE_BOOLEAN, 60, "Activo"));
        GridResult gr = new GridResult();
        gr.cols = cols.toArray(new MySQLCol[cols.size()]);

        String filter = req.getFiltQuery();
        if (req.page >= 0) {
            String qb = "SELECT "
                    + "c.id, "
                    + "c.address, "
                    + "n.name, "
                    + "c.sector_type, "
                    + "c.stratum, "
                    + "c.code, "
                    + "CONCAT(c.first_name,' ', COALESCE(c.last_name,'')), "
                    + "c.doc, "
                    + "IFNULL((SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1), ''), "
                    + "c.active "
                    + "FROM bill_client_tank c "
                    + "LEFT JOIN sigma.neigh n ON n.id = c.neigh_id "
                    + "WHERE " + filter + " LIMIT " + (req.page * 30) + ", 30";
            gr.data = new MySQLQuery(qb).getRecords(conn);
        } else {
            gr.data = new Object[][]{};
        }
        gr.sortColIndex = 4;
        gr.sortType = GridResult.SORT_ASC;
        return gr;

    }

    public static int getTankClientsPageCount(DataClientRequest req, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*) "
                + "FROM bill_client_tank c "
                + "INNER JOIN bill_building b ON b.id = c.building_id WHERE " + req.getFiltQuery()).getAsInteger(conn);
    }

    public static int getNetClientsPageCount(DataClientRequest req, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*) "
                + "FROM bill_client_tank c "
                + "WHERE " + req.getFiltQuery()).getAsInteger(conn);
    }

    public static GridResult getTankProspect(boolean converted, Connection conn) throws Exception {

        List<MySQLCol> cols = new ArrayList<>();
        cols.add(new MySQLCol(MySQLCol.TYPE_KEY));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 90, "Cód. Edificio"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Edificio"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Nombres"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Documento"));
        cols.add(new MySQLCol(MySQLCol.TYPE_BOOLEAN, 60, "Convertido"));
        cols.add(new MySQLCol(MySQLCol.TYPE_BOOLEAN, 60, "Activo"));
        GridResult gr = new GridResult();
        gr.cols = cols.toArray(new MySQLCol[cols.size()]);

        Object[][] data = new MySQLQuery("SELECT p.id, b.old_id, b.name, "
                + "CONCAT(p.first_name,' ', COALESCE(p.last_name,'')), "
                + "p.doc, "
                + "p.converted, "
                + "p.active "
                + "FROM bill_prospect p "
                + "INNER JOIN bill_building b ON b.id = p.building_id "
                + "WHERE p.converted = ?1").setParam(1, converted).getRecords(conn);
        for (Object[] row : data) {
            row[1] = BillSpanController.zeroFill(MySQLQuery.getAsString(row[1]), 3);
        }
        gr.data = data;

        return gr;
    }

    public static GridResult getNetProspect(boolean converted, Connection conn) throws Exception {
        List<MySQLCol> cols = new ArrayList<>();
        cols.add(new MySQLCol(MySQLCol.TYPE_KEY));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Documento"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Nombres"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 190, "Dirección"));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 120, "Barrio"));
        cols.add(new MySQLCol(MySQLCol.TYPE_ENUM, 75, "Sector", new BillClientTank().getEnumOpts("sector_type")));
        cols.add(new MySQLCol(MySQLCol.TYPE_TEXT, 35, "Estrato"));
        cols.add(new MySQLCol(MySQLCol.TYPE_BOOLEAN, 60, "Convertido"));
        cols.add(new MySQLCol(MySQLCol.TYPE_BOOLEAN, 60, "Activo"));
        GridResult gr = new GridResult();
        gr.cols = cols.toArray(new MySQLCol[cols.size()]);
        gr.data = new MySQLQuery("SELECT "
                + "c.id, "
                + "c.doc, "
                + "CONCAT(c.first_name,' ', COALESCE(c.last_name,'')), "
                + "c.address, "
                + "n.name, "
                + "c.sector_type, "
                + "c.stratum, "
                + "c.converted, "
                + "c.active "
                + "FROM bill_prospect c "
                + "LEFT JOIN sigma.neigh n ON n.id = c.neigh_id "
                + "WHERE "
                + "c.converted = ?1").setParam(1, converted) .getRecords(conn);
        gr.sortColIndex = 1;
        gr.sortType = GridResult.SORT_ASC;
        return gr;

    }
}
