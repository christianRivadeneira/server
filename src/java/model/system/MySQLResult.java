package model.system;


public class MySQLResult {
    private byte[] resBin;
    private int cols;
    private int rows;
    
    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public byte[] getResBin() {
        return resBin;
    }

    public void setResBin(byte[] resBin) {
        this.resBin = resBin;
    }
        
}
