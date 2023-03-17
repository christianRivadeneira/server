package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import model.billing.constants.Accounts;
import utilities.Dates;
import utilities.MySQLQuery;

public class BillSpan extends BaseModel<BillSpan> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("'Consumos de' MMMM yyyy");

    public String getConsLabel() {
        return SDF.format(consMonth);
    }

//inicio zona de reemplazo
    public int oldCodPer;
    public Integer serial;
    public String state;
    public boolean readingsClosed;
    public Date beginDate;
    public Date endDate;
    public int adjust;
    public BigDecimal glpValue;
    public BigDecimal reconnect;
    public Date suspDate;
    public Date limitDate;
    public BigDecimal fixedCharge;
    public BigDecimal interes;
    public BigDecimal interesSrv;
    public BigDecimal power;
    public BigDecimal divisor;
    public String enterCode;
    public BigDecimal fac1;
    public BigDecimal fac2;
    public BigDecimal fac3;
    public BigDecimal fac4;
    public BigDecimal fac5;
    public BigDecimal galToKgKte;
    public String priceType;
    public BigDecimal minConsValue;
    public Integer cauLastId;
    public Integer closeFirstId;
    public Integer closeLastId;
    public BigDecimal dInvR;
    public BigDecimal dAomR;
    public BigDecimal dInvNr;
    public BigDecimal dAomNr;
    public BigDecimal p;
    public BigDecimal fpc;
    public BigDecimal fv;
    public BigDecimal fadj;
    public BigDecimal pms;
    public BigDecimal cglp;
    public BigDecimal t;
    public BigDecimal tv;
    public BigDecimal cuf;
    public BigDecimal contribR;
    public BigDecimal contribNr;
    public BigDecimal cEq1;
    public BigDecimal cEq2;
    public BigDecimal cuvR;
    public BigDecimal cuvNr;
    public BigDecimal rawTarif1;
    public BigDecimal finalTarif1;
    public BigDecimal rawTarif2;
    public BigDecimal finalTarif2;
    public BigDecimal vitalCons;
    public Date consMonth;
    public boolean covidEmergency;
    public boolean interestSet;
    public boolean costsSet;
    public boolean paramsDone;
    public BigDecimal subPerc1;
    public BigDecimal subPerc2;
    public BigDecimal dAomRTP;
    public BigDecimal cuvRTP;
    public BigDecimal cEq1TP;
    public BigDecimal cEq2TP;
    public BigDecimal rawTarif1TP;
    public BigDecimal rawTarif2TP;
    public BigDecimal finalTarif1TP;
    public BigDecimal finalTarif2TP;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "old_cod_per",
            "serial",
            "state",
            "readings_closed",
            "begin_date",
            "end_date",
            "adjust",
            "glp_value",
            "reconnect",
            "susp_date",
            "limit_date",
            "fixed_charge",
            "interes",
            "interes_srv",
            "power",
            "divisor",
            "enter_code",
            "fac1",
            "fac2",
            "fac3",
            "fac4",
            "fac5",
            "gal_to_kg_kte",
            "price_type",
            "min_cons_value",
            "cau_last_id",
            "close_first_id",
            "close_last_id",
            "d_inv_r",
            "d_aom_r",
            "d_inv_nr",
            "d_aom_nr",
            "p",
            "fpc",
            "fv",
            "fadj",
            "pms",
            "cglp",
            "t",
            "tv",
            "cuf",
            "contrib_r",
            "contrib_nr",
            "c_eq_1",
            "c_eq_2",
            "cuv_r",
            "cuv_nr",
            "raw_tarif_1",
            "final_tarif_1",
            "raw_tarif_2",
            "final_tarif_2",
            "vital_cons",
            "cons_month",
            "covid_emergency",
            "interest_set",
            "costs_set",
            "params_done",
            "sub_perc_1",
            "sub_perc_2",
            "d_aom_rtp",
            "cuv_r_t_p",
            "c_eq_1_t_p",
            "c_eq_2_t_p",
            "raw_tarif_1_t_p",
            "raw_tarif_2_t_p",
            "final_tarif_1_t_p",
            "final_tarif_2_t_p"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, oldCodPer);
        q.setParam(2, serial);
        q.setParam(3, state);
        q.setParam(4, readingsClosed);
        q.setParam(5, beginDate);
        q.setParam(6, endDate);
        q.setParam(7, adjust);
        q.setParam(8, glpValue);
        q.setParam(9, reconnect);
        q.setParam(10, suspDate);
        q.setParam(11, limitDate);
        q.setParam(12, fixedCharge);
        q.setParam(13, interes);
        q.setParam(14, interesSrv);
        q.setParam(15, power);
        q.setParam(16, divisor);
        q.setParam(17, enterCode);
        q.setParam(18, fac1);
        q.setParam(19, fac2);
        q.setParam(20, fac3);
        q.setParam(21, fac4);
        q.setParam(22, fac5);
        q.setParam(23, galToKgKte);
        q.setParam(24, priceType);
        q.setParam(25, minConsValue);
        q.setParam(26, cauLastId);
        q.setParam(27, closeFirstId);
        q.setParam(28, closeLastId);
        q.setParam(29, dInvR);
        q.setParam(30, dAomR);
        q.setParam(31, dInvNr);
        q.setParam(32, dAomNr);
        q.setParam(33, p);
        q.setParam(34, fpc);
        q.setParam(35, fv);
        q.setParam(36, fadj);
        q.setParam(37, pms);
        q.setParam(38, cglp);
        q.setParam(39, t);
        q.setParam(40, tv);
        q.setParam(41, cuf);
        q.setParam(42, contribR);
        q.setParam(43, contribNr);
        q.setParam(44, cEq1);
        q.setParam(45, cEq2);
        q.setParam(46, cuvR);
        q.setParam(47, cuvNr);
        q.setParam(48, rawTarif1);
        q.setParam(49, finalTarif1);
        q.setParam(50, rawTarif2);
        q.setParam(51, finalTarif2);
        q.setParam(52, vitalCons);
        q.setParam(53, consMonth);
        q.setParam(54, covidEmergency);
        q.setParam(55, interestSet);
        q.setParam(56, costsSet);
        q.setParam(57, paramsDone);
        q.setParam(58, subPerc1);
        q.setParam(59, subPerc2);
        q.setParam(60, dAomRTP);
        q.setParam(61, cuvRTP);
        q.setParam(62, cEq1TP);
        q.setParam(63, cEq2TP);
        q.setParam(64, rawTarif1TP);
        q.setParam(65, rawTarif2TP);
        q.setParam(66, finalTarif1TP);
        q.setParam(67, finalTarif2TP);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        oldCodPer = MySQLQuery.getAsInteger(row[0]);
        serial = MySQLQuery.getAsInteger(row[1]);
        state = MySQLQuery.getAsString(row[2]);
        readingsClosed = MySQLQuery.getAsBoolean(row[3]);
        beginDate = MySQLQuery.getAsDate(row[4]);
        endDate = MySQLQuery.getAsDate(row[5]);
        adjust = MySQLQuery.getAsInteger(row[6]);
        glpValue = MySQLQuery.getAsBigDecimal(row[7], false);
        reconnect = MySQLQuery.getAsBigDecimal(row[8], false);
        suspDate = MySQLQuery.getAsDate(row[9]);
        limitDate = MySQLQuery.getAsDate(row[10]);
        fixedCharge = MySQLQuery.getAsBigDecimal(row[11], false);
        interes = MySQLQuery.getAsBigDecimal(row[12], false);
        interesSrv = MySQLQuery.getAsBigDecimal(row[13], false);
        power = MySQLQuery.getAsBigDecimal(row[14], false);
        divisor = MySQLQuery.getAsBigDecimal(row[15], false);
        enterCode = MySQLQuery.getAsString(row[16]);
        fac1 = MySQLQuery.getAsBigDecimal(row[17], false);
        fac2 = MySQLQuery.getAsBigDecimal(row[18], false);
        fac3 = MySQLQuery.getAsBigDecimal(row[19], false);
        fac4 = MySQLQuery.getAsBigDecimal(row[20], false);
        fac5 = MySQLQuery.getAsBigDecimal(row[21], false);
        galToKgKte = MySQLQuery.getAsBigDecimal(row[22], false);
        priceType = MySQLQuery.getAsString(row[23]);
        minConsValue = MySQLQuery.getAsBigDecimal(row[24], false);
        cauLastId = MySQLQuery.getAsInteger(row[25]);
        closeFirstId = MySQLQuery.getAsInteger(row[26]);
        closeLastId = MySQLQuery.getAsInteger(row[27]);
        dInvR = MySQLQuery.getAsBigDecimal(row[28], false);
        dAomR = MySQLQuery.getAsBigDecimal(row[29], false);
        dInvNr = MySQLQuery.getAsBigDecimal(row[30], false);
        dAomNr = MySQLQuery.getAsBigDecimal(row[31], false);
        p = MySQLQuery.getAsBigDecimal(row[32], false);
        fpc = MySQLQuery.getAsBigDecimal(row[33], false);
        fv = MySQLQuery.getAsBigDecimal(row[34], false);
        fadj = MySQLQuery.getAsBigDecimal(row[35], false);
        pms = MySQLQuery.getAsBigDecimal(row[36], false);
        cglp = MySQLQuery.getAsBigDecimal(row[37], false);
        t = MySQLQuery.getAsBigDecimal(row[38], false);
        tv = MySQLQuery.getAsBigDecimal(row[39], false);
        cuf = MySQLQuery.getAsBigDecimal(row[40], false);
        contribR = MySQLQuery.getAsBigDecimal(row[41], false);
        contribNr = MySQLQuery.getAsBigDecimal(row[42], false);
        cEq1 = MySQLQuery.getAsBigDecimal(row[43], false);
        cEq2 = MySQLQuery.getAsBigDecimal(row[44], false);
        cuvR = MySQLQuery.getAsBigDecimal(row[45], false);
        cuvNr = MySQLQuery.getAsBigDecimal(row[46], false);
        rawTarif1 = MySQLQuery.getAsBigDecimal(row[47], false);
        finalTarif1 = MySQLQuery.getAsBigDecimal(row[48], false);
        rawTarif2 = MySQLQuery.getAsBigDecimal(row[49], false);
        finalTarif2 = MySQLQuery.getAsBigDecimal(row[50], false);
        vitalCons = MySQLQuery.getAsBigDecimal(row[51], false);
        consMonth = MySQLQuery.getAsDate(row[52]);
        covidEmergency = MySQLQuery.getAsBoolean(row[53]);
        interestSet = MySQLQuery.getAsBoolean(row[54]);
        costsSet = MySQLQuery.getAsBoolean(row[55]);
        paramsDone = MySQLQuery.getAsBoolean(row[56]);
        subPerc1 = MySQLQuery.getAsBigDecimal(row[57], false);
        subPerc2 = MySQLQuery.getAsBigDecimal(row[58], false);
        dAomRTP = MySQLQuery.getAsBigDecimal(row[59], false);
        cuvRTP = MySQLQuery.getAsBigDecimal(row[60], false);
        cEq1TP = MySQLQuery.getAsBigDecimal(row[61], false);
        cEq2TP = MySQLQuery.getAsBigDecimal(row[62], false);
        rawTarif1TP=MySQLQuery.getAsBigDecimal(row[63], false);
        rawTarif2TP=MySQLQuery.getAsBigDecimal(row[64], false);
        finalTarif1TP=MySQLQuery.getAsBigDecimal(row[65], false);
        finalTarif2TP=MySQLQuery.getAsBigDecimal(row[66], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_span";
    }

    public static String getSelFlds(String alias) {
        return new BillSpan().getSelFldsForAlias(alias);
    }

    public static List<BillSpan> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillSpan().getListFromQuery(q, conn);
    }

    public static List<BillSpan> getList(Params p, Connection conn) throws Exception {
        return new BillSpan().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillSpan().deleteById(id, conn);
    }

    public static List<BillSpan> getAll(Connection conn) throws Exception {
        return new BillSpan().getAllList(conn);
    }

//fin zona de reemplazo
    public BigDecimal getConsVal(BigDecimal m3Cons, BigDecimal factor, BigDecimal price) throws Exception {
        BigDecimal val;
        switch (priceType) {
            case "gal":
                val = m3Cons.multiply(factor).multiply(getM3ToGalKte()).multiply(price);
                break;
            case "kg":
                val = m3Cons.multiply(factor).multiply(getM3ToGalKte()).multiply(galToKgKte).multiply(price);
                break;
            default:
                throw new Exception("Tipo de precio desconocido: " + priceType);
        }
        return val.setScale(2, RoundingMode.HALF_EVEN);
    }

    BigDecimal m3ToGalKte = null;

    public BigDecimal getM3ToGalKte() {
        if (m3ToGalKte == null) {
            m3ToGalKte = NullorZeroAsOne(fac1).multiply(NullorZeroAsOne(fac2)).multiply(NullorZeroAsOne(fac3)).multiply(NullorZeroAsOne(fac4)).multiply(NullorZeroAsOne(fac5));
        }
        return m3ToGalKte;
    }

    private BigDecimal NullorZeroAsOne(BigDecimal v) {
        if (v == null || v.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return v;
    }

    /*   public static BillSpan[] getAll(Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM bill_span ORDER BY begin_date DESC").getRecords(conn);
        BillSpan[] rta = new BillSpan[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }*/
    public static BillSpan[] getAllOldCode0(Connection conn) throws Exception {
        List<BillSpan> lst = getList(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_span WHERE old_cod_per = 0 ORDER BY begin_date DESC"), conn);
        return lst.toArray(new BillSpan[lst.size()]);

    }

    public static BillSpan[] findSpanNotConsu(Connection conn) throws Exception {
        List<BillSpan> lst = getList(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_span WHERE state <> 'cons' and old_cod_per = 0 ORDER BY begin_date DESC"), conn);
        return lst.toArray(new BillSpan[lst.size()]);
    }

    //Metodo funciona en fac redes
    public static BillSpan getByMonth(String dbName, Date month, Connection conn) throws Exception {
        return new BillSpan().select(new MySQLQuery("SELECT " + getSelFlds("") + ", id "
                + "FROM " + dbName + ".bill_span "
                + "WHERE DATE(cons_month) = DATE(?1)").setParam(1, month), conn);
    }

    public static BillSpan getByState(String state, Connection conn) throws Exception {
        BillSpan bill = new BillSpan().select(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_span WHERE state = ?1").setParam(1, state), conn);
        System.out.println("COLUMNAS DE BD "+getSelFlds(""));
        if (bill == null) {
            throw new Exception("Aún no se inicia la facturación en este instancia");
        }
        return bill;
    }

    public static void setConsMonth(BillSpan reca) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(reca.endDate);
        int s2 = gc.get(GregorianCalendar.DAY_OF_MONTH);
        gc.setTime(reca.beginDate);
        int m1Days = gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        int s1 = m1Days - s2;

        if (s1 < s2) {
            //segundo
            gc.setTime(reca.endDate);
        } else {
            //primero
            gc.setTime(reca.beginDate);
            throw new Exception("El mes de consumo debe coincidir con el mes de facturación");
        }

        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        reca.consMonth = Dates.trimDate(gc.getTime());
    }

    private static int getMonthsSinceYearZero(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return (gc.get(GregorianCalendar.YEAR) * 12) + gc.get(GregorianCalendar.MONTH);
    }

    public static void calculateNetParams(BillSpan cons, BillInstance inst, Connection conn, Connection instConn) throws Exception {
        BigDecimal hundred = new BigDecimal(100);
        setConsMonth(cons);

        BillSpan reca = new BillSpan().select(cons.id - 1, instConn);
        BillSpan cart = new BillSpan().select(cons.id - 2, instConn);

        if (getMonthsSinceYearZero(reca.consMonth) != getMonthsSinceYearZero(cons.consMonth) - 1) {
            throw new Exception("El mes debe ser consistente con el periodo anterior.");
        }

        int s1Users = 0;
        int s2Users = 0;

        BigDecimal s1M3 = BigDecimal.ZERO;
        BigDecimal s2M3 = BigDecimal.ZERO;

        boolean firstTime = true;

        List<BillInstance> insts = BillInstance.getBillingByMarket(inst.marketId, conn);
        for (int i = 0; i < insts.size(); i++) {
            BillInstance in = insts.get(i);
            in.useInstance(conn);
            BillSpan instCons = BillSpan.getByMonth(in.db, cons.consMonth, conn);
            if (instCons == null) {
                throw new Exception(in.name + " no tiene un periodo para " + new SimpleDateFormat("MMMM/yyyy").format(cons.consMonth) + ".\nDebe realizar el proceso de cierre en esa instancia.");
            }
            if (!instCons.state.equals("cons")) {
                throw new Exception("El periodo " + new SimpleDateFormat("MMMM/yyyy").format(cons.consMonth) + " en " + in.name + " no está en consumo.");
            }
            BillSpan instReca = new BillSpan().select(instCons.id - 1, conn);

            s1Users += new MySQLQuery("SELECT COUNT(*) FROM bill_clie_cau WHERE span_id = ?1 AND stratum = 1 AND m3_subs > 0").setParam(1, instReca.id).getAsInteger(conn);
            s2Users += new MySQLQuery("SELECT COUNT(*) FROM bill_clie_cau WHERE span_id = ?1 AND stratum = 2 AND m3_subs > 0").setParam(1, instReca.id).getAsInteger(conn);

            s1M3 = add(s1M3, new MySQLQuery("SELECT SUM(m3_subs) FROM bill_clie_cau WHERE span_id = ?1 AND stratum = 1 AND m3_subs > 0").setParam(1, instReca.id).getAsBigDecimal(conn, true));
            s2M3 = add(s2M3, new MySQLQuery("SELECT SUM(m3_subs) FROM bill_clie_cau WHERE span_id = ?1 AND stratum = 2 AND m3_subs > 0").setParam(1, instReca.id).getAsBigDecimal(conn, true));

            if (new MySQLQuery("SELECT COUNT(*) > 3 FROM bill_span").getAsBoolean(conn)) {
                firstTime = false;
            }
        }

        new MySQLQuery("USE sigma;").executeUpdate(conn);

        BillMarket market = new BillMarket().select(inst.marketId, conn);
        System.out.println("2- Procedimiento calcular ceq1 "+inst.marketId);
        BillPriceIndex baseInd = BillPriceIndex.getByMonth(market.baseMonth, conn);
        BillPriceIndex recaInd = BillPriceIndex.getByMonth(reca.consMonth, conn);
        BillPriceIndex cartInd = BillPriceIndex.getByMonth(cart.consMonth, conn);

        cons.dAomNr = mult(market.dAomBaseNr, div(recaInd.ipc, baseInd.ipc));
        cons.dAomR = mult(market.dAomBaseR, div(recaInd.ipc, baseInd.ipc));
        cons.dInvNr = mult(market.dInvBaseNr, div(recaInd.ipp, baseInd.ipp));
        cons.dInvR = mult(market.dInvBaseR, div(recaInd.ipp, baseInd.ipp));
        cons.dAomRTP=mult(market.sumGobAomR, div(recaInd.ipp, baseInd.ipp));
        cons.cuf = mult(
                mult(market.cfBase, pow(new BigDecimal("0.99875"), Dates.getMonths(market.cfProdBaseMonth, reca.consMonth) + 1)),
                div(recaInd.ipc, baseInd.ipc)
        );
        System.out.println("3- Procedimiento calculo variables "+cons.dAomR+ " su principal es "+market.dAomBaseR+ " reca ipc es "+recaInd.ipc+ " base ipc es "+baseInd.ipc);
        System.out.println("4- Procedimiento calculo variables "+market.sumGobAomR+ " Resultado "+cons.dAomRTP);
        System.out.println("4.1- Procedimiento calculo variables "+market.dAomBaseR+ " Resultado "+cons.dAomR);
        BigDecimal g = div(cons.pms, cons.cglp);
        BigDecimal t = add(cons.t, cons.tv);
        BigDecimal c = mult(div(add(g, t), sub(BigDecimal.ONE, div100(cons.p))), cons.fv);
        
        BigDecimal dR = add(cons.dAomR, cons.dInvR);
        System.out.println("4.2- Procedimiento calculo la suma que tanto se buscó "+dR +" sus valores "+cons.dAomR+" el segundo "+cons.dInvR);
        BigDecimal dRTP= add(cons.dAomR, cons.dAomRTP);
        System.out.println("4.3- Procedimiento calculo la suma que tanto se buscó "+dRTP +" sus valores "+cons.dAomRTP+" el segundo "+cons.dInvR);
        BigDecimal dNr = add(cons.dAomNr, cons.dInvNr);
        System.out.println("ESTO ES DR "+dR +"Esto es drTP "+dRTP);
        cons.cuvR = add(c, mult(dR, cons.fpc));
        cons.cuvNr = add(c, mult(dNr, cons.fpc));
        cons.cuvRTP=add(c, mult(dRTP, cons.fpc));
        System.out.println("5- Procedimiento calculo variables cuvR "+cons.cuvR+" y esta es cuvRTP "+cons.cuvRTP);
        BigDecimal avgS1 = s1Users == 0 ? cons.vitalCons : div(s1M3, new BigDecimal(s1Users));
        BigDecimal avgS2 = s2Users == 0 ? cons.vitalCons : div(s2M3, new BigDecimal(s2Users));

        //se simplicó la fórmula
        
        cons.cEq1 = cons.cuvR.add(div(cons.cuf, avgS1));
        cons.cEq2 = cons.cuvR.add(div(cons.cuf, avgS2));
        
        cons.cEq1TP = cons.cuvRTP.add(div(cons.cuf,avgS1));
        cons.cEq2TP = cons.cuvRTP.add(div(cons.cuf, avgS2));
        System.out.println("6- Resultado deseado estrato 1bajo "+cons.cEq1+" Estrato segundo "+ cons.cEq2);
        System.out.println("6.1- Resultado deseado estrato 1bajo tp "+cons.cEq1TP+" Estrato segundo tp "+ cons.cEq2TP);
        if (firstTime) {
            cons.subPerc1 = new BigDecimal(50);
            cons.rawTarif1 = mult(cons.cEq1, sub(BigDecimal.ONE, div100(cons.subPerc1)));
            cons.subPerc2 = new BigDecimal(40);
            cons.rawTarif2 = mult(cons.cEq2, sub(BigDecimal.ONE, div100(cons.subPerc2)));
            cons.rawTarif1TP = mult(cons.cEq1TP, sub(BigDecimal.ONE, div100(cons.subPerc1)));
            cons.rawTarif2TP = mult(cons.cEq2TP, sub(BigDecimal.ONE, div100(cons.subPerc2)));
            System.out.println("7- Calculos subsidios sub1 "+cons.subPerc1+" sub 2 "+cons.subPerc2);
            System.out.println("7.1- Calculos subsidios raw tarifa1 "+cons.rawTarif1+" calculo raw tarifa2 "+cons.rawTarif2);
            System.out.println("7.2- Calculos subsidios raw tarifa1tp "+cons.rawTarif1TP+" calculo raw tarifa2tp "+cons.rawTarif2TP);
        } else {
            if (cons.covidEmergency) {
                cons.rawTarif1 = mult(reca.rawTarif1, min(div(recaInd.ipc, cartInd.ipc), div(cons.cEq1, reca.cEq1)));
                cons.rawTarif2 = mult(reca.rawTarif2, min(div(recaInd.ipc, cartInd.ipc), div(cons.cEq2, reca.cEq2)));
                cons.rawTarif1TP = reca.rawTarif1TP!=null?mult(reca.rawTarif1TP, min(div(recaInd.ipc, cartInd.ipc), div(cons.cEq1TP, reca.cEq1TP))):null;
                cons.rawTarif2TP = reca.rawTarif2TP!=null?mult(reca.rawTarif2TP, min(div(recaInd.ipc, cartInd.ipc), div(cons.cEq2TP, reca.cEq2TP))):null;
                /*if(reca.rawTarif1TP==null){
                    cons.rawTarif1TP = mult(cons.cEq1TP, sub(BigDecimal.ONE, div100(cons.subPerc1)));
                }
                if (reca.rawTarif2TP==null){
                    cons.rawTarif2TP = mult(cons.cEq2TP, sub(BigDecimal.ONE, div100(cons.subPerc2)));
                }*/
                System.out.println("8- Calculo para covid raw1 "+cons.rawTarif1+" raw2 "+cons.rawTarif2+" raw1tp "+cons.rawTarif1TP+" raw2tp "+cons.rawTarif2TP);
            } else {
                cons.rawTarif1 = mult(reca.rawTarif1, div(recaInd.ipc, cartInd.ipc));
                cons.rawTarif2 = mult(reca.rawTarif2, div(recaInd.ipc, cartInd.ipc));
                cons.rawTarif1TP = reca.rawTarif1TP!=null?mult(reca.rawTarif1TP, div(recaInd.ipc, cartInd.ipc)):null;
                cons.rawTarif2TP = reca.rawTarif2TP!=null?mult(reca.rawTarif2TP, div(recaInd.ipc, cartInd.ipc)):null;
                /*if(reca.rawTarif1TP==null){
                    cons.rawTarif1TP = mult(cons.cEq1TP, sub(BigDecimal.ONE, div100(cons.subPerc1)));
                }
                if (reca.rawTarif2TP==null){
                    cons.rawTarif2TP = mult(cons.cEq2TP, sub(BigDecimal.ONE, div100(cons.subPerc2)));
                }*/
                System.out.println("8.1- Calculo para covid raw1 "+cons.rawTarif1+" raw2 "+cons.rawTarif2+" raw1tp "+cons.rawTarif1TP+" raw2tp "+cons.rawTarif2TP);
            }

            cons.subPerc1 = mult(sub(BigDecimal.ONE, div(cons.rawTarif1, cons.cEq1)), hundred);
            //cons.subPerc1 = mult(sub(BigDecimal.ONE, div(cons.rawTarif1TP, cons.cEq1TP)), hundred);
            System.out.println("9- Calculo de un subsidio "+cons.subPerc1);
            if (cons.covidEmergency) {
                cons.subPerc1 = new BigDecimal(50);
                cons.rawTarif1 = mult(cons.cEq1, sub(BigDecimal.ONE, div100(cons.subPerc1)));
                cons.rawTarif1TP = mult(cons.cEq1TP, sub(BigDecimal.ONE, div100(cons.subPerc1)));
            }

            cons.subPerc2 = mult(sub(BigDecimal.ONE, div(cons.rawTarif2, cons.cEq2)), hundred);
            //cons.subPerc2 = mult(sub(BigDecimal.ONE, div(cons.rawTarif2TP, cons.cEq2TP)), hundred);
            if (cons.covidEmergency) {
                cons.subPerc2 = new BigDecimal(40);
                cons.rawTarif2 = mult(cons.cEq2, sub(BigDecimal.ONE, div100(cons.subPerc2)));
                cons.rawTarif2TP = mult(cons.cEq2TP, sub(BigDecimal.ONE, div100(cons.subPerc2)));
            }
        }

        //la adición de un 10% adicional de subsidio por emergencia covid es desactiva por petición de mg.
        /*if (cons.covidEmergency) {
            cons.subPerc1 = add(cons.subPerc1, new BigDecimal(10));
            cons.subPerc2 = add(cons.subPerc2, new BigDecimal(10));
            cons.finalTarif1 = mult(cons.cEq1, sub(BigDecimal.ONE, div100(cons.subPerc1)));
            cons.finalTarif2 = mult(cons.cEq2, sub(BigDecimal.ONE, div100(cons.subPerc2)));
        } else {
            cons.finalTarif1 = cons.rawTarif1;
            cons.finalTarif2 = cons.rawTarif2;
        }*/
        cons.finalTarif1 = cons.rawTarif1;
        cons.finalTarif2 = cons.rawTarif2;
        cons.finalTarif1TP = cons.rawTarif1TP;
        cons.finalTarif2TP = cons.rawTarif2TP;
        if(!market.tarifaPlena){
            cons.dAomRTP=null;
            cons.cuvRTP=null;
            cons.cEq1TP=null;
            cons.cEq2TP=null;
            cons.rawTarif1TP=null;
            cons.rawTarif2TP=null;
            cons.finalTarif1TP=null;
            cons.finalTarif2TP=null;
        }
    }

    private static BigDecimal pow(BigDecimal a, int b) {
        return a.pow(b);
    }

    private static BigDecimal sub(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    private static BigDecimal mult(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    private static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    private static BigDecimal div100(BigDecimal a) {
        return div(a, new BigDecimal(100));
    }

    private static BigDecimal div(BigDecimal a, BigDecimal b) {
        return a.divide(b, 4, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) {
            return a;
        } else {
            return b;
        }
    }

    @Override
    public int insert(Connection conn) throws Exception {
        throw new RuntimeException("Use insertWithId instead");
    }

    public void insertWithId(Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + getTblName() + " SET id =  " + id + ", " + getSetFlds());
        prepareQuery(q);
        q.executeUpdate(conn);
    }

    public static boolean isSpanCaused(String dbName, int spanId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*) > 0 FROM  " + dbName + ".bill_transaction t WHERE t.trans_type_id = 1 AND t.bill_span_id = " + spanId).getAsBoolean(conn);
    }

    public static BillSpan getByBuilding(String state, int buildId, BillInstance inst, Connection conn) throws Exception {
        if (inst.siteBilling) {
            if (new MySQLQuery("SELECT SUM(span_closed) > 0 FROM bill_client_tank c WHERE c.bill_client_tank = ?1").setParam(1, buildId).getAsBoolean(conn)) {
                switch (state) {
                    case "reca":
                        return BillSpan.getByState("cons", conn);
                    case "cons":
                        throw new Exception("Debe cerrar el periodo primero.");
                    default:
                        throw new Exception("Unsupported state: " + state);
                }
            } else {
                return BillSpan.getByState(state, conn);
            }
        } else {
            return BillSpan.getByState(state, conn);
        }

    }

    public static BillSpan getByClient(String state, Integer clientId, BillInstance inst, Connection conn) throws Exception {
        if (clientId == null) {
            return BillSpan.getByState(state, conn);
        } else {
            if (inst.siteBilling) {
                Boolean spanClosed = new MySQLQuery("SELECT span_closed FROM bill_client_tank c WHERE c.id = ?1").setParam(1, clientId).getAsBoolean(conn);
                if (spanClosed == null) {
                    throw new Exception("El cliente no existe");
                }
                if (spanClosed) {
                    switch (state) {
                        case "reca":
                            return BillSpan.getByState("cons", conn);
                        case "cons":
                            throw new Exception("El periodo no existe aún.\nDebe finalizar la facturación en sitio.");
                        default:
                            throw new Exception("Unsupported state: " + state);
                    }
                } else {
                    return BillSpan.getByState(state, conn);
                }
            } else {
                return BillSpan.getByState(state, conn);
            }
        }
    }

    public static List<BillSpan> getAll(Connection conn, boolean onlyWithReadings) throws Exception {
        MySQLQuery q;
        if (onlyWithReadings) {
            q = new MySQLQuery("SELECT " + getSelFlds("") + ", bill_span.id "
                    + "FROM bill_span "
                    + "WHERE bill_span.id in (SELECT DISTINCT r.span_id FROM bill_reading r) ORDER BY id DESC");
        } else {
            q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_span ORDER BY id DESC");
        }
        return new BillSpan().getListFromQuery(q, conn);
    }

    public static List<BillSpan> getNoConsum(Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_span WHERE state <> 'cons' and old_cod_per = 0 ORDER BY id DESC");
        return new BillSpan().getListFromQuery(q, conn);
    }

    public static List<BillSpan> getSui(Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_span WHERE (state <> 'cons' or (state = 'cons' and readings_closed)) and old_cod_per = 0 ORDER BY id DESC");
        return new BillSpan().getListFromQuery(q, conn);
    }

    public static BillSpan getByMonth(int year, int month, BillInstance inst, Connection conn) throws Exception {
        if (inst.isNetInstance()) {
            return new BillSpan().select(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM " + inst.db + ".bill_span WHERE MONTH(cons_month) = ?1 AND YEAR(cons_month) = ?2").setParam(1, month).setParam(2, year), conn);
        } else {
            return new BillSpan().select(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM " + inst.db + ".bill_span WHERE MONTH(end_date) = ?1 AND YEAR(end_date) = ?2").setParam(1, month).setParam(2, year), conn);
        }
    }

    public static BillSpan getOpenPricesSpan(BillInstance inst, Connection conn) throws Exception {
        BillSpan cons = getByState("cons", conn);
        if (isPricesListOpen(cons, inst, conn)) {
            return cons;
        }
        BillSpan reca = getByState("reca", conn);
        if (isPricesListOpen(reca, inst, conn)) {
            return reca;
        }
        return null;
    }

    public static boolean isParamsOpen(BillSpan span, BillInstance inst, Connection conn) throws Exception {
        String state = new MySQLQuery("SELECT state FROM bill_span WHERE id = ?1").setParam(1, span.id).getAsString(conn);
        switch (state) {
            case "cons":
                if (inst.siteBilling) {
                    return new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_client_tank c WHERE c.span_closed AND c.active").getAsBoolean(conn);
                } else {
                    return true;
                }
            case "reca":
                MySQLQuery q = new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_transaction t WHERE t.bill_span_id = ?1");
                q.setParam(1, span.id);
                return q.getAsBoolean(conn);
            default:
                return false;
        }
    }
    
   
    public static boolean isPricesListOpen(BillSpan span, BillInstance inst, Connection conn) throws Exception {
        String state = new MySQLQuery("SELECT state FROM bill_span WHERE id = ?1").setParam(1, span.id).getAsString(conn);
        switch (state) {
            case "cons":
                if (inst.siteBilling) {
                    return new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_client_tank c WHERE c.span_closed AND c.active").getAsBoolean(conn);
                } else {
                    if (inst.isTankInstance()) {
                        return !span.readingsClosed;
                    } else {
                        return true;
                    }
                }
            case "reca":
                MySQLQuery q = new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_transaction t WHERE t.bill_span_id = ?1");
                q.setParam(1, span.id);
                return q.getAsBoolean(conn);
            default:
                return false;
        }
    }

    public static boolean isInterOpen(int spanId, BillInstance inst, Connection conn) throws Exception {
        String state = new MySQLQuery("SELECT state FROM bill_span WHERE id = ?1").setParam(1, spanId).getAsString(conn);
        switch (state) {
            case "reca":
                //boolean trans = new MySQLQuery("SELECT COUNT(*) > 0 FROM bill_transaction t WHERE t.bill_span_id = ?1").setParam(1, spanId).getAsBoolean(conn);
                //el primer periodo no hay que aplicar esta validación
                //trans = true;
                //para saber si ya se causaron los intereses
                MySQLQuery interQ = new MySQLQuery("SELECT COUNT(*) > 0 FROM bill_transaction t WHERE t.bill_span_id = ?1 AND t.account_deb_id IN (?2, ?3);");
                interQ.setParam(1, spanId);
                interQ.setParam(2, Accounts.C_INT_GLP);
                interQ.setParam(3, Accounts.C_INT_SRV);
                boolean inter = interQ.getAsBoolean(conn);
                return !inter;
            default:
                return false;
        }
    }
}
