package web.marketing.cylSales;

import java.util.List;

public class WarningListWrapper implements WarningList {

    private final List<String> warns;

    public WarningListWrapper(List<String> warns) {
        this.warns = warns;
    }

    @Override
    public void addWarn(String warn) {
        warns.add(warn);
    }

}
