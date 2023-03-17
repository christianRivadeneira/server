package api.bill.writers.bill;

import api.bill.model.BillClieCau;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DtoRangeToPrint {

    public String label;
    public BigDecimal cons;
    public BigDecimal vunit;
    public BigDecimal total;

    private static final DecimalFormat DF = new DecimalFormat(".00");

    public static DtoRangeToPrint[] getRanges(BillClieCau cau, BillSpan reca, BillInstance inst) {
        if (inst.isTankInstance()) {
            return new DtoRangeToPrint[]{};
        }

        if (cau.m3Subs.compareTo(BigDecimal.ZERO) > 0) {
            DtoRangeToPrint r1 = new DtoRangeToPrint();
            r1.label = "Menor o igual a " + DF.format(reca.vitalCons) + "m3";
            r1.cons = cau.m3Subs;
            r1.vunit = cau.stratum == 1 ? reca.cEq1 : reca.cEq2;
            r1.total = r1.cons.multiply(r1.vunit);

            DtoRangeToPrint r2 = new DtoRangeToPrint();
            r2.label = "Mayor a " + DF.format(reca.vitalCons) + "m3";
            r2.cons = cau.m3NoSubs;

            r2.vunit = cau.sector.equals("r") ? reca.cuvR : reca.cuvNr;
            r2.total = r2.cons.multiply(r2.vunit);
            return new DtoRangeToPrint[]{r1, r2};
        } else {
            DtoRangeToPrint r = new DtoRangeToPrint();
            r.label = "Consumo";
            r.cons = cau.m3NoSubs;
            r.vunit = cau.sector.equals("r") ? reca.cuvR : reca.cuvNr;
            r.total = r.cons.multiply(r.vunit);
            return new DtoRangeToPrint[]{r};
        }
    }

    public static void setRanges(BillForPrint bill, BillSpan reca, BillInstance inst) {
        bill.ranges = getRanges(bill.cau, reca, inst);
    }
}
