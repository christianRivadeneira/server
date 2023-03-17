package utilities.reportHelper;

public class SummaryRow {
    private String name;
    private Integer merge;

    public SummaryRow(){
        
    }

    public SummaryRow(String name, Integer merge){
        this.name = name;
        this.merge = merge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMerge() {
        return merge;
    }

    public void setMerge(Integer merge) {
        this.merge = merge;
    }

}
