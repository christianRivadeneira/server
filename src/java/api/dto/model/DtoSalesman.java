package api.dto.model;

import api.BaseModel;
import api.dto.dto.MoveSmanDto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import model.system.SessionLogin;
import utilities.Dates;
import utilities.MySQLQuery;

public class DtoSalesman extends BaseModel<DtoSalesman> {
//inicio zona de reemplazo

    public String idRegist;
    public int centerId;
    public String firstName;
    public String lastName;
    public String document;
    public boolean active;
    public Integer storeId;
    public Integer driverId;
    public Integer contractorId;
    public Integer distributorId;
    public String minasPass;
    public boolean training;
    public boolean asExp;
    public boolean scanLoad;
    public boolean offerOrders;
    public Integer liquidatorId;
    public String accCode;
    public boolean expOnlyOwnCyls;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "id_regist",
            "center_id",
            "first_name",
            "last_name",
            "document",
            "active",
            "store_id",
            "driver_id",
            "contractor_id",
            "distributor_id",
            "minas_pass",
            "training",
            "as_exp",
            "scan_load",
            "offer_orders",
            "liquidator_id",
            "acc_code",
            "exp_only_own_cyls"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, idRegist);
        q.setParam(2, centerId);
        q.setParam(3, firstName);
        q.setParam(4, lastName);
        q.setParam(5, document);
        q.setParam(6, active);
        q.setParam(7, storeId);
        q.setParam(8, driverId);
        q.setParam(9, contractorId);
        q.setParam(10, distributorId);
        q.setParam(11, minasPass);
        q.setParam(12, training);
        q.setParam(13, asExp);
        q.setParam(14, scanLoad);
        q.setParam(15, offerOrders);
        q.setParam(16, liquidatorId);
        q.setParam(17, accCode);
        q.setParam(18, expOnlyOwnCyls);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        idRegist = MySQLQuery.getAsString(row[0]);
        centerId = MySQLQuery.getAsInteger(row[1]);
        firstName = MySQLQuery.getAsString(row[2]);
        lastName = MySQLQuery.getAsString(row[3]);
        document = MySQLQuery.getAsString(row[4]);
        active = MySQLQuery.getAsBoolean(row[5]);
        storeId = MySQLQuery.getAsInteger(row[6]);
        driverId = MySQLQuery.getAsInteger(row[7]);
        contractorId = MySQLQuery.getAsInteger(row[8]);
        distributorId = MySQLQuery.getAsInteger(row[9]);
        minasPass = MySQLQuery.getAsString(row[10]);
        training = MySQLQuery.getAsBoolean(row[11]);
        asExp = MySQLQuery.getAsBoolean(row[12]);
        scanLoad = MySQLQuery.getAsBoolean(row[13]);
        offerOrders = MySQLQuery.getAsBoolean(row[14]);
        liquidatorId = MySQLQuery.getAsInteger(row[15]);
        accCode = MySQLQuery.getAsString(row[16]);
        expOnlyOwnCyls = MySQLQuery.getAsBoolean(row[17]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dto_salesman";
    }

    public static String getSelFlds(String alias) {
        return new DtoSalesman().getSelFldsForAlias(alias);
    }

    public static List<DtoSalesman> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DtoSalesman().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DtoSalesman().deleteById(id, conn);
    }

    public static List<DtoSalesman> getAll(Connection conn) throws Exception {
        return new DtoSalesman().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<DtoSalesman> getActive(Connection conn) throws Exception {
        return new DtoSalesman().getListFromQuery(new MySQLQuery("SELECT " + DtoSalesman.getSelFlds("s") + " FROM dto_salesman s WHERE s.active"), conn);
    }

    public static DtoSalesman findSalesman(List<DtoSalesman> salesmen, String document) {
        if (document == null) {
            return null;
        }
        for (DtoSalesman man : salesmen) {
            if ((man.document != null && man.document.equals(document)) || (man.idRegist != null && man.idRegist.equals(document))) {
                return man;
            }
        }
        return null;
    }

    public static synchronized void move(MoveSmanDto obj, SessionLogin sl, Connection conn) throws Exception {
        try {
            conn.setAutoCommit(false);

            DtoCenter cOrig = new DtoCenter().select(obj.sman.centerId, conn);
            DtoCenter cDest = new DtoCenter().select(obj.destCenterId, conn);

            Date begMonthOrig = Dates.getDatesBegEnd(cOrig.dDay)[0];
            Date begMonthDest = null;

            if (cDest.dDay != null) {
                begMonthDest = Dates.getDatesBegEnd(cDest.dDay)[0];
            }

            //suma fras del ministerio del antes del mes del cambio
            String changeMonthQ = "SELECT SUM(subsidy) "
                    + "FROM dto_sale "
                    + "WHERE "
                    + "date(dt) BETWEEN ?1 AND ?2 "
                    + "AND dto_liq_id IS NULL "
                    + "AND center_id = " + cOrig.id + " "
                    + "AND salesman_id = " + obj.sman.id;
            //Ids de facturas que se van a mover, requisito para el log de movimientos

            BigDecimal origChange = new MySQLQuery(changeMonthQ).setParam(1, begMonthOrig).setParam(2, cOrig.dDay).getAsBigDecimal(conn, true);
            MySQLQuery query = new MySQLQuery(changeMonthQ);
            if (cDest.dDay != null) {
                query.setParam(1, begMonthDest).setParam(2, cDest.dDay);
            } else {
                query.setParam(1, begMonthOrig).setParam(2, cOrig.dDay);
            }
            BigDecimal destChange = query.getAsBigDecimal(conn, true);
            Integer destSalesmanId = new MySQLQuery("SELECT id FROM dto_salesman "
                    + "WHERE document = \"" + obj.sman.document + "\" "
                    + "AND center_id = " + cDest.id + " AND active = 0").getAsInteger(conn);

            Object[][] movDetailData = new MySQLQuery("SELECT CONCAT(DATE_FORMAT(dt, '%y%m%d%H%i%s'), ' ', clie_doc) "
                    + "FROM dto_sale "
                    + "WHERE "
                    + "dto_liq_id IS NULL "
                    + "AND center_id = " + cOrig.id + " "
                    + "AND salesman_id = " + obj.sman.id).getRecords(conn);

            boolean newSalesman = false;

            //---------------- Log de movimiento de facturas --------------------------------------
            Object[][] saleIds = new MySQLQuery("SELECT id "
                    + "FROM dto_sale "
                    + "WHERE "
                    + "date(dt) < ?1 "
                    + "AND dto_liq_id IS NULL "
                    + "AND center_id = " + cOrig.id + " "
                    + "AND salesman_id = " + obj.sman.id).setParam(1, new Date()).getRecords(conn);
            if (saleIds.length > 0) {
                //MySQLBatch qq = new MySQLBatch();
                for (Object[] saleId : saleIds) {
                    new MySQLQuery("INSERT INTO dto_sales_move SET "
                            + "sale_id = " + MySQLQuery.getAsInteger(saleId[0]) + ", " + "sman_id = " + obj.sman.id + ", " + "mov_date = NOW()").executeInsert(conn);
                }
                //qq.sendData(ep());
            }
            //-------------------------------------------------------------------------------------

            StringBuilder sbMovDetailData = new StringBuilder("\\n\\rSe movieron fras por " + obj.movVal + ", fueron: (ymdHis)");
            for (Object[] movDetailRow : movDetailData) {
                sbMovDetailData.append("\\n\\r");
                sbMovDetailData.append(movDetailRow[0]);
            }
            String movDetail = sbMovDetailData.toString();

            if (destSalesmanId == null) {
                newSalesman = true;
                destSalesmanId = new MySQLQuery("INSERT INTO dto_salesman SET id_regist = \"" + obj.sman.idRegist + "\", first_name = \"" + obj.sman.firstName + "\", last_name = \"" + obj.sman.lastName + "\", document = \"" + obj.sman.document + "\" "
                        + ", center_id = " + cDest.id + ", "
                        + "active = 1, "
                        + "store_id = " + obj.sman.storeId + ", "
                        + "driver_id = " + obj.sman.driverId + ", "
                        + "contractor_id = " + obj.sman.contractorId + ", "
                        + "distributor_id = " + obj.sman.distributorId + ", "
                        + "minas_pass = '" + (obj.sman.minasPass != null ? obj.sman.minasPass : "") + "' ").executeInsert(conn);
            }

            new MySQLQuery("UPDATE dto_salesman SET active = 0 WHERE id = " + obj.sman.id).executeUpdate(conn);
            new MySQLQuery("UPDATE dto_salesman SET "
                    + "store_id = " + obj.sman.storeId + ", "
                    + "driver_id = " + obj.sman.driverId + ", "
                    + "contractor_id = " + obj.sman.contractorId + ", "
                    + "distributor_id = " + obj.sman.distributorId + ", "
                    + "active = 1 WHERE id = " + destSalesmanId).executeUpdate(conn);
            new MySQLQuery("UPDATE dto_center SET initial_balance_month = initial_balance_month - " + origChange + " WHERE id = " + cOrig.id).executeUpdate(conn);
            if (cDest.dDay != null) {
                new MySQLQuery("UPDATE dto_center SET initial_balance_month = initial_balance_month + " + destChange + " WHERE id = " + cDest.id).executeUpdate(conn);
            }
            new MySQLQuery("UPDATE dto_sale "
                    + "SET salesman_id = " + destSalesmanId + ", center_id = " + cDest.id + " "
                    + "WHERE "
                    + "center_id = " + cOrig.id + " "
                    + "AND salesman_id = " + obj.sman.id + " "
                    + "AND dto_liq_id IS NULL").executeUpdate(conn);

            DtoSalesmanLog logSalesmanOrig = new DtoSalesmanLog();
            logSalesmanOrig.salesmanId = obj.sman.id;
            logSalesmanOrig.type = "edit";
            logSalesmanOrig.notes = "Se desactivó por movimiento de " + cOrig.name + " a " + cDest.name + "." + System.lineSeparator() + movDetail;

            logSalesmanOrig.insert(logSalesmanOrig, sl.employeeId, conn);

            //log del empleado activado o creado
            DtoSalesmanLog logSalesmanDest = new DtoSalesmanLog();
            logSalesmanDest.salesmanId = destSalesmanId;
            logSalesmanDest.type = (newSalesman ? "new" : "edit");
            logSalesmanDest.notes = (newSalesman ? "Se creó" : "Se reactivó") + " por movimiento de " + cOrig.name + " a " + cDest.name + System.lineSeparator() + movDetail;
            DtoSalesmanLog insert = new DtoSalesmanLog();
            insert.insert(logSalesmanDest, sl.employeeId, conn);
            conn.commit();
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        }
    }
}
