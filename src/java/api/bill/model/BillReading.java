package api.bill.model;

import api.BaseModel;
import api.Params;
import api.sys.model.SysCfg;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import model.billing.lists.BillBuildConsumCost;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillReading extends BaseModel<BillReading> {
//inicio zona de reemplazo

    public int spanId;
    public int clientTankId;
    public BigDecimal reading;
    public BigDecimal lastReading;
    public BigDecimal criticalReading;
    public BigDecimal lat;
    public BigDecimal lon;
    public Boolean inRadius;
    public boolean fromScan;
    public Integer empId;
    public Integer faultId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "span_id",
            "client_tank_id",
            "reading",
            "last_reading",
            "critical_reading",
            "lat",
            "lon",
            "in_radius",
            "from_scan",
            "emp_id",
            "fault_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, spanId);
        q.setParam(2, clientTankId);
        q.setParam(3, reading);
        q.setParam(4, lastReading);
        q.setParam(5, criticalReading);
        q.setParam(6, lat);
        q.setParam(7, lon);
        q.setParam(8, inRadius);
        q.setParam(9, fromScan);
        q.setParam(10, empId);
        q.setParam(11, faultId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        spanId = MySQLQuery.getAsInteger(row[0]);
        clientTankId = MySQLQuery.getAsInteger(row[1]);
        reading = MySQLQuery.getAsBigDecimal(row[2], false);
        lastReading = MySQLQuery.getAsBigDecimal(row[3], false);
        criticalReading = MySQLQuery.getAsBigDecimal(row[4], false);
        lat = MySQLQuery.getAsBigDecimal(row[5], false);
        lon = MySQLQuery.getAsBigDecimal(row[6], false);
        inRadius = MySQLQuery.getAsBoolean(row[7]);
        fromScan = MySQLQuery.getAsBoolean(row[8]);
        empId = MySQLQuery.getAsInteger(row[9]);
        faultId = MySQLQuery.getAsInteger(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_reading";
    }

    public static String getSelFlds(String alias) {
        return new BillReading().getSelFldsForAlias(alias);
    }

    public static List<BillReading> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillReading().getListFromQuery(q, conn);
    }

    public static List<BillReading> getList(Params p, Connection conn) throws Exception {
        return new BillReading().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillReading().deleteById(id, conn);
    }

    public static List<BillReading> getAll(Connection conn) throws Exception {
        return new BillReading().getAllList(conn);
    }

//fin zona de reemplazo
    public static BillReading getByClientSpan(int clientId, int spanId, Connection conn) throws Exception {
        Params p = new Params();
        p.param("client_tank_id", clientId);
        p.param("span_id", spanId);
        return new BillReading().select(p, conn);
    }

    //Consumo mensual en galones de un edificio para rotacion en estacionarios
    public static BigDecimal getBuildingMonthlyCons(int year, int month, int buildId, BillInstance inst, SysCfg sysCfg, Connection conn) throws Exception {
        BillSpan span = BillSpan.getByMonth(year, month, inst, conn);
        if (span == null) {
            return null;
        }
        Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(conn, span.id);
        MySQLPreparedQuery factorQ = BillBuildFactor.getFactorQuery(conn);
        MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(conn);

        MySQLPreparedQuery qClients = new MySQLPreparedQuery("SELECT (r.reading - r.last_reading), c.id "
                + " FROM bill_client_tank c "
                + " INNER JOIN bill_reading r ON r.client_tank_id = c.id and r.span_id = " + span.id + " "
                + " WHERE c.building_id = ?1", conn);

        BigDecimal buildFac = BillBuildFactor.getFactor(span.id, buildId, factorQ);
        BigDecimal buildM3Cons = BigDecimal.ZERO;
        qClients.setParameter(1, buildId);
        Object[][] clientsData = qClients.getRecords();
        if (clientsData.length > 0) {
            for (Object[] clientsRow : clientsData) {
                Integer clientId = (Integer) clientsRow[1];
                BigDecimal clientFac = BillClientFactor.getFactor(span.id, clientId, clientFactorQ);
                Integer listId = BillPriceSpan.getListId(conn, span.id, clientId);
                if (listId != null) {
                    BigDecimal usrM3Cons = (BigDecimal) (clientsRow[0] != null ? clientsRow[0] : BigDecimal.ZERO);
                    BigDecimal consVal = span.getConsVal(usrM3Cons, (clientFac == BigDecimal.ZERO ? buildFac : clientFac), prices.get(MySQLQuery.getAsInteger(listId)));
                    if (!sysCfg.skipMinCons || consVal.compareTo(span.minConsValue) >= 0) {
                        buildM3Cons = buildM3Cons.add(usrM3Cons.multiply((clientFac == BigDecimal.ZERO ? buildFac : clientFac)));
                    }
                }
            }
        }
        return buildM3Cons;//Valor
    }

    //Consumo mensual en galones por categoria para las ventas en estacionarios
    public static BigDecimal getCategMonthlyCons(int year, int month, int categId, BillInstance inst, SysCfg sysCfg, Connection conn) throws Exception {
        BillSpan span = BillSpan.getByMonth(year, month, inst, conn);
        if (span == null) {
            return null;
        }
        Object[][] builds = new MySQLQuery("SELECT id FROM bill_building WHERE tank_client_type_id = " + categId).getRecords(conn);
        BigDecimal sum = BigDecimal.ZERO;
        for (Object[] build : builds) {
            int buildId = MySQLQuery.getAsInteger(build[0]);
            sum = sum.add(getBuildingMonthlyCons(year, month, buildId, inst, sysCfg, conn));
        }
        return sum;
    }

    public static List<BillBuildConsumCost> getConsumCostsByBuilds(int spanId, int instId, SysCfg sysCfg, Connection conn) throws Exception {
        List<BillBuildConsumCost> res = new ArrayList<>();
        BillSpan sp = new BillSpan().select(spanId, conn);
        List<BillBuilding> buildings = BillBuilding.getAll(conn);
        Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(conn, spanId);
        MySQLPreparedQuery factorQ = BillBuildFactor.getFactorQuery(conn);
        MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(conn);

        MySQLPreparedQuery qClients = new MySQLPreparedQuery("SELECT (COALESCE(r.reading, r2.reading) - COALESCE(r.last_reading, r2.last_reading)), c.id "
                + " FROM bill_client_tank c "
                + " LEFT JOIN bill_reading r ON r.client_tank_id = c.id and r.span_id = " + spanId + " "
                + " LEFT JOIN bill_reading_bk r2 ON r2.client_tank_id = c.id AND r2.span_id = " + spanId + " "
                + " WHERE c.building_id = ?1", conn);

        for (BillBuilding build : buildings) {
            BigDecimal buildFac = BillBuildFactor.getFactor(spanId, build.id, factorQ);
            BigDecimal buildM3Cons = BigDecimal.ZERO;
            qClients.setParameter(1, build.id);
            Object[][] clientsData = qClients.getRecords();
            if (clientsData.length > 0) {
                for (Object[] clientsRow : clientsData) {
                    Integer clientId = (Integer) clientsRow[1];
                    BigDecimal clientFac = BillClientFactor.getFactor(spanId, clientId, clientFactorQ);
                    Integer listId = BillPriceSpan.getListId(conn, spanId, clientId);
                    if (listId != null) {
                        BigDecimal usrM3Cons = (BigDecimal) (clientsRow[0] != null ? clientsRow[0] : BigDecimal.ZERO);
                        BigDecimal consVal = sp.getConsVal(usrM3Cons, (clientFac == BigDecimal.ZERO ? buildFac : clientFac), prices.get(MySQLQuery.getAsInteger(listId)));
                        if (!sysCfg.skipMinCons || consVal.compareTo(sp.minConsValue) >= 0) {
                            buildM3Cons = buildM3Cons.add(usrM3Cons.multiply((clientFac == BigDecimal.ZERO ? buildFac : clientFac)));
                        }
                    }
                }
                BillBuildConsumCost bc = new BillBuildConsumCost();
                res.add(bc);
                bc.buildId = build.id;
                bc.consum = buildM3Cons.multiply(sp.getM3ToGalKte());
            }
        }
        return res;
    }

    public static BigDecimal getSixMonthsAvg(int clientId, int spanId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT AVG(cons) FROM ("
                + "SELECT r.reading - r.last_reading AS cons "
                + "FROM bill_reading r "
                + "WHERE r.client_tank_id = " + clientId + " AND r.span_id < " + spanId + " "
                + "ORDER BY r.span_id DESC LIMIT 6) AS l").getAsBigDecimal(conn, false);
    }

    public static Date getLastReadingDateByClient(int clientId, Connection conn) throws Exception {
        Integer spanId = new MySQLQuery("SELECT MAX(span_id) FROM bill_reading WHERE client_tank_id = ?1").setParam(1, clientId).getAsInteger(conn);
        if (spanId != null) {
            return new BillSpan().select(spanId, conn).endDate;
        }
        return null;
    }

}
