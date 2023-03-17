package api.ess.model.dto;

import api.ess.model.EssBuilding;
import api.ess.model.EssUnit;
import java.util.List;

public class EssLoginInfo {

    public boolean isAdmin;
    public boolean isResident;
    public List<EssBuilding> buildings;
    public List<EssUnit> units;

}
