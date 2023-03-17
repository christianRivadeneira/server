package utilities.mysqlReport;

public class NumHeaderColumn extends HeaderColumn {

    public NumHeaderColumn(int rowSpan) {
        super(1, rowSpan);
    }

    @Override
    public void setRowSpan(int rowSpan) {
        super.setRowSpan(rowSpan);
    }

    @Override
    public int getRowSpan() {
        return super.getRowSpan();
    }

    @Override
    public int getColSpan() {
        return 1;
    }

    @Override
    public String getColName() {
        return "No.";
    }
}
