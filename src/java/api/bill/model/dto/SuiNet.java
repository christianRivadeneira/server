package api.bill.model.dto;

import java.math.BigDecimal;
import java.util.Date;

public class SuiNet {

    public String clieNiu;//1
    public String daneCode;//2
    public String locationType;//6
    public String address;//7
    public String billNum;//8
    public Date billCreation;//9
    public Date spanBeg;//10
    public Date spanEnd;//11
    public String sector;//12
    public String readingType;//13
    public BigDecimal lastRead;//14
    public BigDecimal curRead;//15
    public BigDecimal factor;//16
    public BigDecimal consumoM3;//17
    public BigDecimal facCargoFijo;//18
    public BigDecimal tariff;//19
    public BigDecimal facConsumo;//20
    public BigDecimal rebillM3;//21
    public BigDecimal rebillVal;//22
    public BigDecimal moraAcumulada;//23
    public BigDecimal interesAcumulado;//24
    public BigDecimal subsidyValue;//26
    public BigDecimal contribValue;//26
    public BigDecimal subsidyPercent;//27
    public BigDecimal contribPercent;//27
    public BigDecimal conexion;//28
    public BigDecimal interesFinanciacionConexion;//29
    public BigDecimal suspReconect;//30
    public BigDecimal netCheckCost;//31
    public BigDecimal cutReconnect;//32
    public Date netCheckDate;//33
    public BigDecimal others;//34
    public Date limitDt;//35
    public Date suspDt;//36
    public BigDecimal total;//37
    public String cadInfo;
    public String cadastralCode;
    public Boolean icfbHome;
    public Boolean priorityHome;

}
