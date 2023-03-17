package metadata.model;

import api.GridResult;
import api.MySQLCol;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import metadata.permission.PermissionChecker;
import utilities.MySQLQuery;

public class Grid {

    public static final String SIMPLE_TABLE = "SIMPLE_TABLE";
    public static final String MASTER_TABLE = "MASTER_TABLE";
    public static final String COMBO = "COMBO";

    public String name;
    public String label;
    public String type;
    public List<GridTable> tbls = new ArrayList<>();
    public List<GridFld> flds = new ArrayList<>();
    public List<GridCond> conds = new ArrayList<>();
    public List<GridOrderBy> orderByFlds = new ArrayList<>();
    public List<GridAction> accs = new ArrayList<>();
    public List<String> grpFldIds = new ArrayList<>();
    public String tblName;
    public GridRequest testRequest;
    public Integer testEmployeeId;

    public String sortAscById;
    public String sortDescById;

    public boolean sortable;

    public String dataPath;
    public String deletePath;

    public boolean containsFld(String fldId) {
        for (int i = 0; i < flds.size(); i++) {
            GridFld gFld = flds.get(i);
            if (gFld.fldId.equals(fldId)) {
                return true;
            }
        }
        return false;
    }

    public GridFld getGridFld(String fldId) {
        for (int i = 0; i < flds.size(); i++) {
            GridFld gFld = flds.get(i);
            if (gFld.fldId.equals(fldId)) {
                return gFld;
            }
        }
        return null;
    }

    public void addTbl(GridTable gTbl) throws Exception {
        tbls.add(gTbl);
        Table cTbl = Table.getByName(gTbl.tblName);
        for (int i = 0; i < cTbl.toStrFldIds.size(); i++) {
            Field fld = cTbl.getFieldById(cTbl.toStrFldIds.get(i));
            if (!fld.fk) {
                addFld(fld);
            }
        }
    }

    public void addFld(Field fld) throws Exception {
        GridFld gFld = new GridFld();
        gFld.fldId = fld.id;
        gFld.isKey = (fld.pk || fld.fk);
        if (!tblName.equals(fld.tblName)) {
            gFld.label = Table.getByName(fld.tblName).singular;
        }
        if (!gFld.isKey) {
            gFld.width = 50;
        }
        flds.add(gFld);
    }

    public GridResult getTestTable(Connection conn) throws Exception {
        return getApiTable(testRequest, testEmployeeId, conn);
    }

    public static GridResult getApiTable(GridRequest req, Integer employeeId, Connection conn) throws Exception {
        Grid grid = Table.getByName(req.tableName).getGridByName(req.gridName);

        GridResult rta = new GridResult();
        rta.cols = grid.getCols();
        MySQLQuery mq = new MySQLQuery(grid.getQuery(req, employeeId, null, conn));
        rta.data = mq.getRecords(conn);
        if (grid.sortable) {
            if (grid.sortAscById == null && grid.sortDescById == null) {
                rta.sortType = GridResult.SORT_DEFAULT;
            } else {
                String sortColId;
                if (grid.sortAscById != null) {
                    rta.sortType = GridResult.SORT_ASC;
                    sortColId = grid.sortAscById;
                } else {
                    rta.sortType = GridResult.SORT_DESC;
                    sortColId = grid.sortDescById;
                }
                int vis = 0;
                for (int i = 0; i < grid.flds.size(); i++) {
                    GridFld gFld = grid.flds.get(i);
                    Field fld = Field.getById(gFld.fldId);
                    if (sortColId.equals(fld.id)) {
                        break;
                    }
                    if (!gFld.isKey) {
                        vis++;
                    }
                }
                rta.sortColIndex = vis;
            }
        } else {
            rta.sortType = GridResult.SORT_NONE;
        }
        return rta;
    }

    private MySQLCol[] getCols() throws Exception {
        MySQLCol[] cols = new MySQLCol[flds.size()];
        for (int i = 0; i < flds.size(); i++) {
            GridFld gFld = flds.get(i);
            Field fld = Field.getById(gFld.fldId);
            Integer format;
            if (gFld.isKey) {
                format = MySQLCol.TYPE_KEY;
            } else {
                String sFormat = gFld.format != null ? gFld.format : fld.format;
                if (sFormat == null) {
                    int a = 1;
                }
                format = MySQLCol.getConstFromStr(sFormat);

            }
            String lbl = gFld.label != null ? gFld.label : (fld.label == null ? fld.name : fld.label);

            if (format.equals(MySQLCol.TYPE_KEY)) {
                cols[i] = new MySQLCol(MySQLCol.TYPE_KEY);
            } else if (format.equals(MySQLCol.TYPE_TEXT)) {
                cols[i] = new MySQLCol(MySQLCol.TYPE_TEXT, gFld.width, lbl);
            } else if (MySQLCol.isDate(format) || MySQLCol.isTime(format)) {
                cols[i] = new MySQLCol(format, gFld.width, lbl);
            } else if (MySQLCol.isDecimal(format)) {
                cols[i] = new MySQLCol(format, gFld.width, lbl);
            } else if (MySQLCol.isInteger(format)) {
                cols[i] = new MySQLCol(format, gFld.width, lbl);
            } else if (format.equals(MySQLCol.TYPE_BOOLEAN)) {
                cols[i] = new MySQLCol(MySQLCol.TYPE_BOOLEAN, gFld.width, lbl);
            } else if (format.equals(MySQLCol.TYPE_ENUM)) {
                cols[i] = new MySQLCol(MySQLCol.TYPE_ENUM, gFld.width, lbl, fld.emunOpts);
            } else {
                throw new RuntimeException("Unsupported Format: " + format);
            }

            if (gFld.align != null) {
                switch (gFld.align) {
                    case GridFld.LEFT:
                        cols[i].align = MySQLCol.LEFT;
                        break;
                    case GridFld.RIGHT:
                        cols[i].align = MySQLCol.RIGHT;
                        break;
                    case GridFld.CENTER:
                        cols[i].align = MySQLCol.CENTER;
                        break;
                    default:
                        throw new RuntimeException();
                }
            }

            cols[i].editable = gFld.editable;
            cols[i].showZeros = gFld.showZero;
            cols[i].toString = gFld.toString;

        }
        return cols;

    }

    public String getQuery(GridRequest req, Integer empId, Integer id, Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sb.append("SELECT ");
        for (int i = 0; i < flds.size(); i++) {
            GridFld gFld = flds.get(i);
            Field fld = Field.getById(gFld.fldId);
            Table tbl = Table.getByName(fld.tblName);
            if (gFld.oper != null) {
                sb.append(gFld.oper).append("(");
            }
            sb.append(tbl.name).append(".").append(fld.name);
            if (gFld.oper != null) {
                sb.append(")");
            }
            if (i < flds.size() - 1) {
                sb.append(", ");
            } else {
                sb.append(" ");
            }
        }
        sb.append("\nFROM ");
        for (int i = 0; i < tbls.size(); i++) {
            GridTable gt = tbls.get(i);
            if (gt.fldId != null) {
                Field f = Field.getById(gt.fldId);
                if (gt.type != null) {
                    sb.append("\n").append(gt.type.equals(GridTable.INNER) ? "INNER" : "LEFT").append(" ");
                } else {
                    sb.append("\n").append(!f.nullable ? "INNER" : "LEFT").append(" ");
                }
                sb.append(" JOIN ");
                sb.append(gt.tblName).append(" ON ");
                sb.append(f.tblName).append(".").append(f.name).append(" = ").append(f.fkTblName).append(".id ");

                if (gt.conds != null && !gt.conds.isEmpty()) {
                    for (int j = 0; j < gt.conds.size(); j++) {
                        GridTableCond c = gt.conds.get(j);
                        Field cFld = Field.getById(c.fldId);
                        sb.append("AND ").append(cFld.tblName).append(".").append(cFld.name);
                        switch (c.cond) {
                            case GridTableCond.IS_FALSE:
                                sb.append(" = 0");
                                break;
                            case GridTableCond.IS_TRUE:
                                sb.append(" = 1");
                                break;
                            case GridTableCond.IS_NOT_NULL:
                                sb.append(" IS NOT NULL");
                                break;
                            case GridTableCond.IS_NULL:
                                sb.append(" IS NULL");
                                break;
                            case GridTableCond.FIXED:
                                params.add(req.ints.get(0));
                                sb.append(" = ?").append(params.size());
                                break;
                            default:
                                throw new RuntimeException();
                        }
                    }
                }

            } else {
                sb.append(gt.tblName).append(" ");
            }
        }
        
        if (id != null) {
            sb.append("\nWHERE ").append(req.tableName).append(".id = ").append(id).append(" ");
        } else if (!conds.isEmpty()) {
            sb.append("\nWHERE ");
            for (int i = 0; i < conds.size(); i++) {
                GridCond cond = conds.get(i);
                Field fld = Field.getById(cond.fldId);
                sb.append(fld.tblName).append(".").append(fld.name);

                switch (cond.comparison) {
                    case GridCond.EQUALS:
                    case GridCond.NOT_EQUALS:
                        switch (cond.slotType) {
                            case GridCond.INT:
                                if (req.ints.isEmpty()) {
                                    throw new Exception("No se encontro parametros del grid en la posicion " + cond.slot);
                                }
                                params.add(req.ints.get(cond.slot));
                                break;
                            case GridCond.BOOL:
                                params.add(req.bools.get(cond.slot));
                                break;
                            case GridCond.DATE:
                                params.add(req.dates.get(cond.slot));
                                break;
                            case GridCond.STRING:
                                params.add(req.strings.get(cond.slot));
                                break;
                            default:
                                throw new RuntimeException();
                        }
                        if (cond.comparison.equals(GridCond.EQUALS)) {
                            sb.append(" = ?").append(params.size());
                        } else {
                            sb.append(" <> ?").append(params.size());
                        }
                        break;
                    case GridCond.IS_TRUE:
                        sb.append(" = 1");
                        break;
                    case GridCond.IS_FALSE:
                        sb.append(" = 0");
                        break;
                    case GridCond.IS_NULL:
                        sb.append(" IS NULL");
                        break;
                    case GridCond.IS_NOT_NULL:
                        sb.append(" IS NOT NULL");
                        break;
                    case GridCond.ON_DAY:
                        sb.append(" BETWEEN ?").append(params.size() + 1).append(" AND ?").append(params.size() + 2);
                        getSpanFromDay(req.dates.get(cond.slot), params);
                        break;
                    case GridCond.ON_MONTH:
                        sb.append(" BETWEEN ?").append(params.size() + 1).append(" AND ?").append(params.size() + 2);
                        getSpanFromMonth(req.dates.get(cond.slot), params);
                        break;
                    case GridCond.ON_YEAR:
                        sb.append(" BETWEEN ?").append(params.size() + 1).append(" AND ?").append(params.size() + 2);
                        getSpanFromYear(req.dates.get(cond.slot), params);
                        break;
                    case GridCond.PERMISSION:
                        PermissionChecker checker = (PermissionChecker) Class.forName("metadata.permission." + cond.permissionChecker).newInstance();
                        String ids = PermissionChecker.getAsString(checker.checkByEmployee(empId, req.profileId, conn));
                        if (ids == null) {
                            sb.append(" IS NOT NULL");
                        } else {
                            sb.append(" IN (").append(ids).append(")");
                        }

                        break;
                    case GridCond.FIXED:
                        Field fixedFld = Field.getById(cond.fldId);
                        switch (fixedFld.type) {
                            case Field.ENUM:
                                sb.append(" = '").append(cond.fixedEnum).append("'");
                                break;
                            case Field.BIG_INTEGER:
                            case Field.INTEGER:
                                sb.append(" = ").append(cond.fixedInt).append("");
                                break;
                            default:
                                throw new RuntimeException();
                        }
                        break;
                    case GridCond.LIKE:
                        String like = req.strings.get(cond.slot);
                        if (!like.contains("%")) {
                            like = "%" + like.replace(" ", "%") + "%";
                        }
                        params.add(like);
                        sb.append(" LIKE ?").append(params.size());
                        break;
                    default:
                        throw new RuntimeException();
                }
                if (i < conds.size() - 1) {
                    sb.append(" AND ");
                } else {
                    sb.append(" ");
                }
            }
        }

        if (!grpFldIds.isEmpty()) {
            sb.append("\nGROUP BY ");
            for (int i = 0; i < grpFldIds.size(); i++) {
                Field fld = Field.getById(grpFldIds.get(i));
                sb.append(fld.tblName).append(".").append(fld.name);
                if (i < grpFldIds.size() - 1) {
                    sb.append(", ");
                } else {
                    sb.append(" ");
                }
            }
        }

        if (!orderByFlds.isEmpty()) {
            sb.append("\nORDER BY ");
            for (int i = 0; i < orderByFlds.size(); i++) {
                GridOrderBy gob = orderByFlds.get(i);
                Field fld = Field.getById(gob.fldId);
                sb.append(fld.tblName).append(".").append(fld.name).append(" ");
                sb.append(gob.type.equals(GridOrderBy.ASC) ? "ASC" : (gob.type.equals(GridOrderBy.DESC) ? "DESC" : null));
                if (i < orderByFlds.size() - 1) {
                    sb.append(", ");
                } else {
                    sb.append(" ");
                }
            }
        }

        MySQLQuery q = new MySQLQuery(sb.toString());
        for (int i = 0; i < params.size(); i++) {
            q.setParam(i + 1, params.get(i));
        }
        return q.getParametrizedQuery();
    }

    private void getSpanFromDay(Date d, List<Object> params) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date d1 = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.add(GregorianCalendar.MILLISECOND, -1);
        Date d2 = gc.getTime();
        params.add(d1);
        params.add(d2);
    }

    private void getSpanFromMonth(Date d, List<Object> params) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date d1 = gc.getTime();
        gc.add(GregorianCalendar.MONTH, 1);
        Date d2 = gc.getTime();
        params.add(d1);
        params.add(d2);
    }

    private void getSpanFromYear(Date d, List<Object> params) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date d1 = gc.getTime();
        gc.add(GregorianCalendar.YEAR, 1);
        Date d2 = gc.getTime();
        params.add(d1);
        params.add(d2);
    }

}
