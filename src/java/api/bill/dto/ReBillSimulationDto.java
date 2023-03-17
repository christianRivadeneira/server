package api.bill.dto;

import api.bill.model.BillClieCau;
import java.math.BigDecimal;

public class ReBillSimulationDto {

    public BillClieCau orig;
    public BillClieCau reBill;
    public BillClieCau dif;

    public ReBillSimulationDto() {

    }

    public ReBillSimulationDto(final BillClieCau orig, final BillClieCau reBill) throws Exception {
        this.orig = orig;
        this.reBill = reBill;
        this.dif = new BillClieCau();
        this.dif.fixedCharge = reBill.fixedCharge.subtract(orig.fixedCharge);
        this.dif.m3NoSubs = reBill.m3NoSubs.subtract(orig.m3NoSubs);
        this.dif.m3Subs = reBill.m3Subs.subtract(orig.m3Subs);
        this.dif.valConsNoSubs = reBill.valConsNoSubs.subtract(orig.valConsNoSubs);
        this.dif.valConsSubs = reBill.valConsSubs.subtract(orig.valConsSubs);
        this.dif.valContrib = reBill.valContrib.subtract(orig.valContrib);
        this.dif.valExcContrib = reBill.valExcContrib.subtract(orig.valExcContrib);
        this.dif.valSubs = reBill.valSubs.subtract(orig.valSubs);

        if (this.dif.fixedCharge.add(this.dif.m3NoSubs).add(this.dif.m3Subs).add(this.dif.valConsNoSubs).add(this.dif.valConsSubs).add(this.dif.valContrib).add(this.dif.valExcContrib).add(this.dif.valSubs).compareTo(BigDecimal.ZERO) == 0) {
            throw new Exception("No hay ningún cambio");
        }

    }

}
