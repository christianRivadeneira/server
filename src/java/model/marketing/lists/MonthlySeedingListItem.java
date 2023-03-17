package model.marketing.lists;

import java.util.HashMap;

public class MonthlySeedingListItem {

    private int day;
    private HashMap<Integer, Long> counters;

    public MonthlySeedingListItem() {
    }

    public MonthlySeedingListItem(int day) {
        this.day = day;
        this.counters = new HashMap<Integer, Long>();
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public HashMap<Integer, Long> getCounters() {
        return counters;
    }

    public void setCounters(HashMap<Integer, Long> counters) {
        this.counters = counters;
    }
}
