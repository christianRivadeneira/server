package api.rpt.model.rptTbls;

import api.rpt.model.RptInfo;
import api.rpt.api.dataTypes.DataType;
import api.rpt.model.RptRptFld;
import java.util.ArrayList;
import java.util.List;

public class PivotTable {

    public String[][] types;
    public Object[][] data;
    public int tblHeaderCols;
    public int tblHeaderRows;

    public List<Object[]> rowKeys = new ArrayList<>();
    public List<Object[]> colKeys = new ArrayList<>();

    public boolean extraRows = false;
    public boolean extraCols = false;

    public int joins;
    public String color;

    public PivotTable(RptInfo info, Object[][] rawData) throws Exception {
        //es tabla normal
        if (info.cols.isEmpty() && info.rows.size() > 0 && info.joins.isEmpty()) {
            createDataTable(info, rawData);
        } else {
            createPivotTable(info, rawData);
        }
        this.joins = info.joins.size();
        this.color = info.rpt.color;
    }

    private void createDataTable(RptInfo info, Object[][] rawData) {
        tblHeaderRows = 1;
        data = new Object[tblHeaderRows + rawData.length][tblHeaderCols + info.rows.size()];
        types = new String[tblHeaderRows + rawData.length][tblHeaderCols + info.rows.size()];

        for (int i = 0; i < info.cols.size(); i++) {
            for (int j = 0; j < tblHeaderCols; j++) {
                data[i][j] = info.cols.get(i).getCubeFld().dspName;
                types[i][j] = DataType.TYPE_STR.getName();
            }
        }

        String[] rowTypes = new String[info.rows.size()];
        for (int i = 0; i < info.rows.size(); i++) {
            data[tblHeaderRows - 1][i] = info.rows.get(i).getCubeFld().dspName;
            types[tblHeaderRows - 1][i] = DataType.TYPE_STR.getName();
            rowTypes[i] = info.rows.get(i).getCubeFld().dataType;
        }

        for (int i = 0; i < rawData.length; i++) {
            data[i + 1] = rawData[i];
            types[i + 1] = rowTypes;
        }
    }

//    El generador de Queries coloca los las filas y columnas en el group by y en el select 
//    En un cubo que tenga, tipo de vehículo, placa, zona de vendedor, vendedor, # de unidades y valor
//    si se pide un cubo que tenga: 
//    Columnas: tipo de vehículo, placa
//    Filas:  zona de vendedor, vendedor
//    Métricas: Suma(# de unidades), Suma(valor)
//    Generaría un Query: SELECT tipo, placa, zona, vendedor, SUM(unidades), SUM(valor) FROM cube GROUP BY tipo, placa, zona, vendedor
//    Generaría los datos:
//    
//    +--------+---------+---------+----------+---------------+------------+
//    |  tipo  |  placa  |  zona   | vendedor | SUM(unidades) | SUM(valor) |
//    +--------+---------+---------+----------+---------------+------------+
//    | camión | aul 102 | Pasto   | Jorge    |             5 |        500 |
//    | carro  | aum 123 | Ipiales | Pedro    |            10 |        900 |
//    | carro  | aum 123 | Pasto   | Miguel   |             2 |        200 |
//    | camión | aul 102 | Pasto   | José     |             4 |        400 |
//    +--------+---------+---------+----------+---------------+------------+
//    
//    Estos datos llegan en rawData y hay que armar una tabla pivote con ellos
    private void createPivotTable(RptInfo info, Object[][] rawData) throws Exception {
        //es tabla normal
        if (info.cols.isEmpty() && info.rows.size() > 0 && info.joins.isEmpty()) {

        } else {
            rowKeys = new ArrayList<>();
            colKeys = new ArrayList<>();
//    Colkeys y Rowkeys son select distinct de tipo de vehículo, placa y y select distinct zona, vendedor, es decir, los datos que se van a mostrar en
//    las filas y las columnas respectivamente, podría hallarse por Query, pero para mejor rendimiento se hace acá.
            for (Object[] row : rawData) {
                Object[] rowsKey = new Object[info.rows.size()];
                Object[] colsKey = new Object[info.cols.size()];
                System.arraycopy(row, 0, rowsKey, 0, rowsKey.length);
                System.arraycopy(row, rowsKey.length, colsKey, 0, colsKey.length);
                PivotKey.addKey(rowKeys, rowsKey);
                PivotKey.addKey(colKeys, colsKey);
            }

            if (!info.sortableByValues()) {
                PivotKey.sortMat(rowKeys);
                PivotKey.sortMat(colKeys);
            }

//Cuando se piden reportes con más de una métrica hay dos opciones
//colocarlas en vertical y aumentar una columna para títulos (extraRows)
//
//                       AUM 123  
// --------- ---------- --------- 
//  Pasto     Unidades   v1       
//            Valor      v2       
//  Ipiales   Unidades   v3     
//            Valor      v4       
//
// O acomodarlos en horizontal y aumentar una fila para títulos (extraCols)
//
//            AUM 123                 
// --------- ---------- ------- -- -- 
//            Unidades   Valor        
//  Pasto     v1         v2           
//  Ipiales   v3         v4           
//
            if (rowKeys.size() < colKeys.size()) {
                extraRows = true;
            } else {
                extraCols = true;
            }

            //header cols son columnas que hay que aumentar por el lado izquierdo para acomodar los títulos de lo que se pidió para filas
            //header rows son filas que hay que aumentar arriba para acomodar los títulos de lo que se pidió para columnas
            tblHeaderCols = info.rows.size() + (extraRows ? 1 : 0);
            tblHeaderRows = info.cols.size() + (extraCols ? 1 : 0);

            int totalRows = tblHeaderRows + (extraRows ? (rowKeys.size() * info.joins.size()) : rowKeys.size());
            int totalCols = tblHeaderCols + (extraCols ? (colKeys.size() * info.joins.size()) : colKeys.size());
            data = new Object[totalRows][totalCols];
            types = new String[totalRows][totalCols];
            String[] joinTypes = new String[info.joins.size()];

            for (int i = 0; i < info.joins.size(); i++) {
                if (info.joins.get(i).fldId != null) {
                    joinTypes[i] = RptRptFld.getDataType(info.joins.get(i));
                } else {
                    if (info.joins.get(i).fxName != null) {
                        joinTypes[i] = DataType.TYPE_DECIMAL.getName();
                    } else if (info.joins.get(i).kpiName != null) {
                        joinTypes[i] = DataType.TYPE_GAUGE.getName();
                    } else {
                        throw new RuntimeException();
                    }
                }
            }

            //Los rowsMap por cada combinación como: Pasto, Jorge o Pasto, Miguel, (rowKey) indica el número de fila que le corresponderá en el reporte
            //Los colsMap por cada combinación como: camión, aul 102, carro, aum 123  (colKey) indica el número de columna que le corresponderá en el reporte
            //luego se recorrerá cada uno de la filas del rawData se buscará el rowKey y el colKey que le corresponde, y eso dará la coordenadas para 
            //colocar el dato en la matríz final
            List<PivotKey> rowsMap = new ArrayList<>();
            //filas
            //aquí se llenan los rows y cols Maps y y se colocan y se llenan las cabeceras de filas y columnas, y las cabeceras de los joins
            for (int i = 0; i < rowKeys.size(); i++) {
                if (extraRows) {
                    //llenando los keys
                    for (int k = 0; k < info.joins.size(); k++) {
                        //llenando los keys
                        PivotKey key = new PivotKey(rowKeys.get(i), info.cols.size() + (i * info.joins.size()) + k + (extraCols ? 1 : 0), k);
                        rowsMap.add(key);
                        //llenando los títulos de las filas
                        for (int j = 0; j < rowKeys.get(i).length; j++) {
                            data[key.pos][j] = rowKeys.get(i)[j];
                            types[key.pos][j] = info.rows.get(j).getCubeFld().dataType;
                        }
                    }
                    //llenando los títulos de los joins
                    for (int k = 0; k < info.joins.size(); k++) {
                        RptRptFld join = info.joins.get(k);
                        data[info.cols.size() + (i * info.joins.size()) + k + (extraCols ? 1 : 0)][info.rows.size()] = join.getJoinDesc();
                        types[info.cols.size() + (i * info.joins.size()) + k + (extraCols ? 1 : 0)][info.rows.size()] = DataType.TYPE_STR.getName();
                    }
                } else {
                    //llenando los keys
                    PivotKey key = new PivotKey(rowKeys.get(i), info.cols.size() + i + (extraCols ? 1 : 0));
                    rowsMap.add(key);
                    //llenando los títulos de las filas
                    for (int j = 0; j < rowKeys.get(i).length; j++) {
                        data[key.pos][j] = rowKeys.get(i)[j];
                        types[key.pos][j] = info.rows.get(j).getCubeFld().dataType;
                    }
                }
            }

            List<PivotKey> colsMap = new ArrayList<>();
            //columnas
            for (int i = 0; i < colKeys.size(); i++) {
                if (extraCols) {
                    for (int k = 0; k < info.joins.size(); k++) {
                        PivotKey key = new PivotKey(colKeys.get(i), info.rows.size() + (i * info.joins.size()) + k + (extraRows ? 1 : 0), k);
                        colsMap.add(key);
                        for (int j = 0; j < info.cols.size(); j++) {
                            data[j][key.pos] = colKeys.get(i)[j];
                            types[j][key.pos] = info.cols.get(j).getCubeFld().dataType;
                        }
                    }
                    for (int k = 0; k < info.joins.size(); k++) {
                        RptRptFld join = info.joins.get(k);
                        data[info.cols.size()][info.rows.size() + (i * info.joins.size()) + k + (extraRows ? 1 : 0)] = join.getJoinDesc();
                        types[info.cols.size()][info.rows.size() + (i * info.joins.size()) + k + (extraRows ? 1 : 0)] = DataType.TYPE_STR.getName();
                    }
                } else {
                    PivotKey key = new PivotKey(colKeys.get(i), info.rows.size() + i + (extraRows ? 1 : 0));
                    colsMap.add(key);
                    for (int j = 0; j < info.cols.size(); j++) {
                        data[j][key.pos] = colKeys.get(i)[j];
                        types[j][key.pos] = info.cols.get(j).getCubeFld().dataType;
                    }
                }
            }

            for (int i = 0; i < info.cols.size(); i++) {
                for (int j = 0; j < tblHeaderCols; j++) {
                    data[i][j] = info.cols.get(i).getCubeFld().dspName;
                    types[i][j] = DataType.TYPE_STR.getName();
                }
            }

            for (int i = 0; i < info.rows.size(); i++) {
                data[tblHeaderRows - 1][i] = info.rows.get(i).getCubeFld().dspName;
                types[tblHeaderRows - 1][i] = DataType.TYPE_STR.getName();
            }

            for (Object[] row : rawData) {
                Object[] rowsKey = new Object[info.rows.size()];
                Object[] colsKey = new Object[info.cols.size()];
                Object[] joinKey = new Object[info.joins.size()];

                System.arraycopy(row, 0, rowsKey, 0, rowsKey.length);
                System.arraycopy(row, rowsKey.length, colsKey, 0, colsKey.length);
                System.arraycopy(row, rowsKey.length + colsKey.length, joinKey, 0, joinKey.length);

                if (extraRows) {
                    int colP = PivotKey.find(colsMap, colsKey, null).pos;
                    for (int j = 0; j < joinKey.length; j++) {
                        Object val = joinKey[j];
                        int rowP = PivotKey.find(rowsMap, rowsKey, j).pos;
                        data[rowP][colP] = val;
                        types[rowP][colP] = joinTypes[j];
                    }
                } else if (extraCols) {
                    int rowP = PivotKey.find(rowsMap, rowsKey, null).pos;
                    for (int j = 0; j < joinKey.length; j++) {
                        Object val = joinKey[j];
                        int colP = PivotKey.find(colsMap, colsKey, j).pos;
                        data[rowP][colP] = val;
                        types[rowP][colP] = joinTypes[j];
                    }
                } else {
                    Object val = joinKey[0];
                    int colP = PivotKey.find(colsMap, colsKey, null).pos;
                    int rowP = PivotKey.find(rowsMap, rowsKey, null).pos;
                    data[rowP][colP] = val;
                    types[rowP][colP] = joinTypes[0];
                }
            }
        }
    }

    private void arraycopy(Object[] orig, int origIndex, List dest, int destIndex, int lenght) {
        for (int i = origIndex, j = destIndex, k = 0; k < lenght; i++, j++, k++) {
            dest.add(j, orig[i]);
        }
    }
}
