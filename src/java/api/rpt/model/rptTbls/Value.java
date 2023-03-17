package api.rpt.model.rptTbls;

public class Value {

    public Number value;
    public int seriesIndex;
    public int categoryIndex;

    public Value() {

    }

    public Value(Number value, int seriesIndex, int categoryIndex) {
        this.value = value;
        this.seriesIndex = seriesIndex;
        this.categoryIndex = categoryIndex;
    }
}
