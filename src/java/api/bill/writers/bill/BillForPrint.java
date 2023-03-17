package api.bill.writers.bill;

import api.bill.model.BillClieCau;
import api.bill.model.BillClieRebill;
import api.bill.model.BillClientTank;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class BillForPrint {

    public String priceList;
    public String buildingName;
    public String address;
    public String clieDoc;
    public String cliePhone;
    public String clieApto;
    public Integer billBuildingId;
    public BigDecimal factor;
    public BigDecimal currRead;
    public BigDecimal lastRead;
    public BigDecimal gplPrice;
    public List<Double> consumos;
    public BigDecimal total;
    public String billNum;
    public boolean isTotal;
    public Date creationDate;
    public int months;
    public String clientName;
    public String meter;
    public String instNum;
    public String clieCode;
    public int billId;
    public int spanId;
    public String buildingType;
    public List<LineForPrint> glpLines;
    public List<LineForPrint> noGlpLines;
    public List<LineForPrint> detLines;
    public DtoSrvToPrint[] srvs;
    public DtoRangeToPrint[] ranges;
    public String lastPayDate;
    public String lastPayBank;
    public BigDecimal lastPayValue;
    public String sectorType;
    public Integer stratum;    
    public Date lastInstCheck;
    public Date minInstCheck;
    public Date maxInstCheck;
    public Date lastMeterCheck;
    public Date minMeterCheck;
    public Date maxMeterCheck;
    public String readingFaultDescription;
    public String clientStatus;
    public BillClieCau cau;
    public BillClieRebill rebill;
    public String ipli;
    public String io;
    public int instId;
    public BigDecimal resultFinal;
    public boolean tarifaPlena;
    public BigDecimal c3;
    public BigDecimal c4;
    public BigDecimal c5;
    public BigDecimal agua;
    public boolean review=false;
    public Date dateReview;
    public String nameReview;
    
    public void setClient(BillClientTank c) {
        clientName = c.firstName + (c.lastName != null ? " " + c.lastName : "");
        instNum = c.numInstall;
        clieDoc = c.doc != null ? c.doc : "";
        cliePhone = c.phones != null ? c.phones : "";
        stratum = c.stratum;
        clieCode = c.code;

        if (c.active && c.discon) {
            clientStatus = "Suspendido";
        } else if (c.active && !c.discon) {
            clientStatus = "Activo";
        } else if (!c.active && c.discon) {
            clientStatus = "Retirado";
        } else if (!c.active && !c.discon) {
            clientStatus = "Retirado";
        }
    }
    
    public void setResultFinal(BigDecimal resultado){
        resultFinal=resultado;
    }
    public void setTarifaPlena(boolean esTarifa){
        tarifaPlena=esTarifa;
    }
    public void setCalidad(BigDecimal c3p, BigDecimal c4p, BigDecimal c5p, BigDecimal aguap){
        this.c3=c3p;
        this.c4=c4p;
        this.c5=c5p;
        this.agua=aguap;
    }
    
    
    public void setReview(Date pDateReview, String pNameReview, boolean pReview){
        this.dateReview=pDateReview;
        this.nameReview=pNameReview;
        this.review=pReview;
    }
}
