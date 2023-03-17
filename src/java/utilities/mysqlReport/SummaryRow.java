package utilities.mysqlReport;

import java.util.ArrayList;
import java.util.List;

public class SummaryRow {

    private String name;
    private Integer merge;
    private final List<Integer> disabledIndexes;

    public SummaryRow() {
        this.disabledIndexes = new ArrayList<>();

    }

    public SummaryRow(String name, Integer merge) {
        this.disabledIndexes = new ArrayList<>();
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

    /**
     * No se genera celda de totales para la columna
     *
     * @param colIndex Índice de la columna
     */
    public void disableForColumn(int colIndex) {
        disabledIndexes.add(colIndex);
    }

    public boolean isColumnDisabled(int colIndex) {
        return disabledIndexes.contains(colIndex);
    }
}
