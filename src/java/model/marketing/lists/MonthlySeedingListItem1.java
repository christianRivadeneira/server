package model.marketing.lists;

import java.util.ArrayList;
import java.util.List;

public class MonthlySeedingListItem1 {

    public int day;
    public List<Object[]> counters;

    public MonthlySeedingListItem1() {
    }

    public MonthlySeedingListItem1(int day) {
        this.day = day;
        this.counters = new ArrayList<>();
    }  
}
