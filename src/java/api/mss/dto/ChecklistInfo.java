package api.mss.dto;

import java.util.List;

public class ChecklistInfo {

    public int reviewId;
    public int itemId;
    public String itemName;
    public boolean isChecked;
    public List<ChecklistInfo> items;
}
