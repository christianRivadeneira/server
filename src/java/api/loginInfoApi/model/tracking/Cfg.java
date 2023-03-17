package api.loginInfoApi.model.tracking;

import api.inv.model.InvFacCode;
import api.trk.model.CylinderType;
import java.math.BigDecimal;
import java.util.List;

public class Cfg {
    
    public boolean insertNif;
    public boolean showLabelLb;
    public BigDecimal kte;
    public boolean chkPrefill;
    public List<ChkQuestion> lstQuest;
    public List<TreatmentItem> lstTreatItems;
    public List<CylinderType> cylTypes;
    public List<InvFacCode> facCodes;
    
}
