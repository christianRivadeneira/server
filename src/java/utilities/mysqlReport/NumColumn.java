package utilities.mysqlReport;

public class NumColumn extends Column {

    @Override
    public String getName() {
        return "No.";
    }

    @Override
    public int getWidth() {
        return 7;
    }
}
