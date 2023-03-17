package api.loginInfoApi.model.sales;

import api.com.model.ComAppQuestion;
import api.com.model.ComSalesAppProfCfg;
import api.trk.dto.Load;
import api.trk.model.CylinderType;
import java.util.List;

public class SalesLoginInfo {

    public String msg;
    public String entMinasId;
    public boolean giveSubs;
    public boolean inTraining;
    public String minasPass;
    public boolean isExp;
    public boolean salesApp;
    public boolean scanLoad;
    public List<Load> cylInv;
    public List<Load> cylRelInv;
    public List<CylinderType> cylTypes;
    public String plate;
    public GpsConfig gpsConfig;
    public List<ComAppQuestion> lstQuestion;
    public String lastLogin;
    public int serverTimeZone;

    public Double docPicRate;
    public boolean phantomNif;
    public int offlineTime;
    public boolean lockCylSale;
    public boolean getFromBiable;
    public boolean sigmaValidations;

    public List<ComSalesAppProfCfg> profiles;
    
    public boolean expOnlyOwnCyls;
    public int smanId;
}
