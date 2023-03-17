package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillClieCau extends BaseModel<BillClieCau> {

//inicio zona de reemplazo

    public int clientId;
    public int spanId;
    public boolean priorityHome;
    public boolean icfbHome;
    public Integer stratum;
    public String sector;
    public BigDecimal meterFactor;
    public BigDecimal m3Subs;
    public BigDecimal valConsSubs;
    public BigDecimal m3NoSubs;
    public BigDecimal valConsNoSubs;
    public BigDecimal valSubs;
    public BigDecimal valContrib;
    public BigDecimal valExcContrib;
    public BigDecimal fixedCharge;
    public BigDecimal contribInterest;
    public String activity;
    public String ciiuCode;
    public BigDecimal valConsSubsTP;
    public BigDecimal valConsNoSubsTP;
    public BigDecimal valSubsTP;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "span_id",
            "priority_home",
            "icfb_home",
            "stratum",
            "sector",
            "meter_factor",
            "m3_subs",
            "val_cons_subs",
            "m3_no_subs",
            "val_cons_no_subs",
            "val_subs",
            "val_contrib",
            "val_exc_contrib",
            "fixed_charge",
            "contrib_interest",
            "activity",
            "ciiu_code",
            "val_cons_subs_t_p",
            "val_cons_no_subs_t_p",
            "val_subs_t_p"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, spanId);
        q.setParam(3, priorityHome);
        q.setParam(4, icfbHome);
        q.setParam(5, stratum);
        q.setParam(6, sector);
        q.setParam(7, meterFactor);
        q.setParam(8, m3Subs);
        q.setParam(9, valConsSubs);
        q.setParam(10, m3NoSubs);
        q.setParam(11, valConsNoSubs);
        q.setParam(12, valSubs);
        q.setParam(13, valContrib);
        q.setParam(14, valExcContrib);
        q.setParam(15, fixedCharge);
        q.setParam(16, contribInterest);
        q.setParam(17, activity);
        q.setParam(18, ciiuCode);
        q.setParam(19, valConsSubsTP);
        q.setParam(20, valConsNoSubsTP);
        q.setParam(21, valSubsTP);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        spanId = MySQLQuery.getAsInteger(row[1]);
        priorityHome = MySQLQuery.getAsBoolean(row[2]);
        icfbHome = MySQLQuery.getAsBoolean(row[3]);
        stratum = MySQLQuery.getAsInteger(row[4]);
        sector = MySQLQuery.getAsString(row[5]);
        meterFactor = MySQLQuery.getAsBigDecimal(row[6], false);
        m3Subs = MySQLQuery.getAsBigDecimal(row[7], false);
        valConsSubs = MySQLQuery.getAsBigDecimal(row[8], false);
        m3NoSubs = MySQLQuery.getAsBigDecimal(row[9], false);
        valConsNoSubs = MySQLQuery.getAsBigDecimal(row[10], false);
        valSubs = MySQLQuery.getAsBigDecimal(row[11], false);
        valContrib = MySQLQuery.getAsBigDecimal(row[12], false);
        valExcContrib = MySQLQuery.getAsBigDecimal(row[13], false);
        fixedCharge = MySQLQuery.getAsBigDecimal(row[14], false);
        contribInterest = MySQLQuery.getAsBigDecimal(row[15], false);
        activity = MySQLQuery.getAsString(row[16]);
        ciiuCode = MySQLQuery.getAsString(row[17]);
        valConsSubsTP =MySQLQuery.getAsBigDecimal(row[18], false);
        valConsNoSubsTP=MySQLQuery.getAsBigDecimal(row[19], false);
        valSubsTP=MySQLQuery.getAsBigDecimal(row[20], false);
        
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_clie_cau";
    }

    public static String getSelFlds(String alias) {
        return new BillClieCau().getSelFldsForAlias(alias);
    }

    public static List<BillClieCau> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillClieCau().getListFromQuery(q, conn);
    }

    public static List<BillClieCau> getList(Params p, Connection conn) throws Exception {
        return new BillClieCau().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillClieCau().deleteById(id, conn);
    }

    public static List<BillClieCau> getAll(Connection conn) throws Exception {
        return new BillClieCau().getAllList(conn);
    }

//fin zona de reemplazo
    public BillClieCau() {
        meterFactor = BigDecimal.ONE;
        m3Subs = BigDecimal.ZERO;
        valConsSubs = BigDecimal.ZERO;
        m3NoSubs = BigDecimal.ZERO;
        valConsNoSubs = BigDecimal.ZERO;
        valSubs = BigDecimal.ZERO;
        valContrib = BigDecimal.ZERO;
        valExcContrib = BigDecimal.ZERO;
        contribInterest = BigDecimal.ZERO;
        fixedCharge = BigDecimal.ZERO;
        valConsSubsTP=BigDecimal.ZERO;
        valConsNoSubsTP=BigDecimal.ZERO;
        valSubsTP=BigDecimal.ZERO;
    }

    public static BillClieCau calc(BillClientTank client, BillSpan cons, BillMeter meter, BigDecimal rawM3Amount) {
        BillClieCau cau = new BillClieCau();
        BigDecimal m3Amount;
        if (meter != null && (meter.factor != null && meter.factor.compareTo(BigDecimal.ONE) != 0)) {
            cau.meterFactor = meter.factor;
            m3Amount = rawM3Amount.multiply(meter.factor);
        } else {
            m3Amount = rawM3Amount.multiply(cons.fadj);
        }

        //cargo fijo
        if (!client.sectorType.equals("r") || client.stratum > 2) {
            cau.fixedCharge = cons.cuf;
        }

        boolean subsidy = client.sectorType.equals("r") && (client.stratum == 1 || client.stratum == 2);
        if (subsidy || client.icfbHome || client.priorityHome) {
            //con subsidio
            if (m3Amount.compareTo(cons.vitalCons) <= 0) {
                cau.m3Subs = m3Amount;
                cau.m3NoSubs = BigDecimal.ZERO;
            } else {
                cau.m3Subs = cons.vitalCons;
                cau.m3NoSubs = m3Amount.subtract(cons.vitalCons);
            }

            if (cau.m3Subs.compareTo(BigDecimal.ZERO) > 0) {
                if (null == client.stratum) {
                    throw new RuntimeException("");
                } else {
                    int stratum = (client.icfbHome || client.priorityHome) ? 1 : client.stratum;
                    switch (stratum) {
                        case 1:
                            cau.valConsSubs = cau.m3Subs.multiply(cons.cEq1).setScale(4, RoundingMode.HALF_EVEN);
                            cau.valConsSubsTP =cons.cEq1TP!=null?cau.m3Subs.multiply(cons.cEq1TP).setScale(4, RoundingMode.HALF_EVEN):null;
                            break;
                        case 2:
                            cau.valConsSubs = cau.m3Subs.multiply(cons.cEq2).setScale(4, RoundingMode.HALF_EVEN);
                            cau.valConsSubsTP =cons.cEq2TP!=null?cau.m3Subs.multiply(cons.cEq2TP).setScale(4, RoundingMode.HALF_EVEN):null;
                            break;
                        default:
                            throw new RuntimeException("");
                    }
                }
                cau.valSubs = cau.valConsSubs.multiply(client.stratum == 1 ? cons.subPerc1 : (client.stratum == 2 ? cons.subPerc2 : null)).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN);
                cau.valSubsTP=cau.valConsSubsTP!=null?cau.valConsSubsTP.multiply(client.stratum == 1 ? cons.subPerc1 : (client.stratum == 2 ? cons.subPerc2 : null)).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN):null;
                cau.valConsNoSubs = cau.m3NoSubs.multiply(cons.cuvR).setScale(4, RoundingMode.HALF_EVEN);
                cau.valConsNoSubsTP = cons.cuvRTP!=null?cau.m3NoSubs.multiply(cons.cuvRTP).setScale(4, RoundingMode.HALF_EVEN):null;
                System.out.println("10- valConsNoSubs = ?: "+cau.valConsNoSubs+" valconSub: "+cau.valConsSubs +" valSubs = definitivo: "+cau.valSubs);
                System.out.println("10- valConsNoSubs = ?: "+cau.valConsNoSubsTP+" valconSub: "+cau.valConsSubsTP +" valSubs = definitivo: "+cau.valSubsTP);
            }
        } else {
            //sin subsidio
            cau.m3NoSubs = m3Amount;
            cau.valConsNoSubs = cau.m3NoSubs.multiply(client.sectorType.equals("r") ? cons.cuvR : cons.cuvNr).setScale(4, RoundingMode.HALF_EVEN);
            if (client.sectorType.equals("c") || client.sectorType.equals("i") || (client.sectorType.equals("r") && (client.stratum == 5 || client.stratum == 6))) {
                //tbn se paga contribución sobre el cargo fijo
                BigDecimal contrib = cau.fixedCharge.multiply(client.sectorType.equals("r") ? cons.contribR : cons.contribNr).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN);
                contrib = contrib.add(cau.valConsNoSubs.multiply(client.sectorType.equals("r") ? cons.contribR : cons.contribNr).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));
                if (contrib.compareTo(BigDecimal.ZERO) > 0) {
                    if (client.skipContrib) {
                        cau.valExcContrib = contrib;
                    } else {
                        cau.valContrib = contrib;
                    }
                }
            }
            cau.valConsNoSubsTP=null;
            cau.valConsSubsTP=null;
            cau.valSubsTP=null;
        }

        cau.clientId = client.id;
        cau.sector = client.sectorType;
        cau.stratum = client.stratum;
        cau.spanId = cons.id;
        cau.activity = client.exemptActivity;
        cau.icfbHome = client.icfbHome;
        cau.priorityHome = client.priorityHome;
        cau.ciiuCode  = client.ciiu;
        return cau;
    }
    
    /*
    public static List<BillClieCau> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
    public static BillClieCau getByClientSpan(int clientId, int spanId, Connection conn) throws Exception {
        return new BillClieCau().select(new Params("clientId", clientId).param("spanId", spanId), conn);
    }

    public static List<BillClieCau> getBySpan(int spanId, Connection conn) throws Exception {
        return new BillClieCau().getListFromParams(new Params("spanId", spanId), conn);
    }
    
    public static BigDecimal resultadoTarifaPlena(String docu,int id,Connection conn){
        try{
        BigDecimal valConsumo = new MySQLQuery("SELECT val_cons_subs \n" +
        "FROM bill_clie_cau b inner join bill_client_tank c on c.id = b.client_id \n" +
        "WHERE c.doc = ?1 AND b.id = ?2;").setParam(1, docu).setParam(2, id).getAsBigDecimal(conn, false);

        BigDecimal valConsumoSub = new MySQLQuery("SELECT val_subs \n" +
        "FROM bill_clie_cau b inner join bill_client_tank c on c.id = b.client_id \n" +
        "WHERE c.doc = ?1 AND b.id = ?2;").setParam(1, docu).setParam(2, id).getAsBigDecimal(conn, false);

        BigDecimal valConsumoTP = new MySQLQuery("SELECT val_cons_subs_t_p \n" +
        "FROM bill_clie_cau b inner join bill_client_tank c on c.id = b.client_id \n" +
        "WHERE c.doc = ?1 AND b.id = ?2;").setParam(1, docu).setParam(2, id).getAsBigDecimal(conn, false);

        BigDecimal valConsumoSubTP = new MySQLQuery("SELECT val_subs_t_p \n" +
        "FROM bill_clie_cau b inner join bill_client_tank c on c.id = b.client_id \n" +
        "WHERE c.doc = ?1 AND b.id = ?2;").setParam(1, docu).setParam(2, id).getAsBigDecimal(conn, false);

        BigDecimal resulConsumo= valConsumo.subtract(valConsumoSub);
        BigDecimal resulConsumoTP= valConsumoTP.subtract(valConsumoSubTP);

        BigDecimal finalTP=resulConsumoTP.subtract(resulConsumo);
        return finalTP;
        }
        catch (Exception e){
            return BigDecimal.ZERO;
        }
    }
}
