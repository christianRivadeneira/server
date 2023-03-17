package api.trk.dto;

import api.trk.model.TrkCylLoad;
import java.util.List;

public class ExpInventory {

    public int virtualTripId;
    public List<Load> inventory;
    public List<Load> trasInventory;
    public List<TrkCylLoad> lstCyls;
    public List<TrkCylLoad> lstTrasCyls;
    
}
