package api.rpt.model;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RptInfo implements Serializable {

    public static String[] HIDDEN = new String[]{"rows", "cols", "joins", "filts", "allFilts", "dims", "cubeTbls", "cubeConds"};

//si se aumentan campos actualizar el hashCode
    public RptCube cube;
    public RptRpt rpt;
    public List<RptRptFld> allrFlds;
    public List<RptCubeTbl> cubeTbls;
    public List<RptCubeFld> cubeFlds;
    public List<RptCubeCond> cubeConds;

    public List<RptRptFld> rows;
    public List<RptRptFld> cols;
    public List<RptRptFld> joins;
    public List<RptRptFld> filts;
    public List<RptRptFld> allFilts;

    public List<RptRptFld> dims;
    
    public RptInfo(int rptId, Connection ep) throws Exception {
        cube = RptCube.getSelectByRptIdQuery(rptId, ep);
        rpt = new RptRpt().select(rptId, ep);
        allrFlds = RptRptFld.getByRptQuery(rptId, ep);
        cubeTbls = RptCubeTbl.getByRptQuery(rptId, ep);
        cubeFlds = RptCubeFld.getByRptQuery(rptId, ep);
        cubeConds = RptCubeCond.getByRptQuery(rptId, ep);

        for (RptCubeTbl cubeTbl : cubeTbls) {
            cubeTbl.setTables(cubeTbls);
        }

        for (RptCubeFld cubeFld : cubeFlds) {
            cubeFld.setTables(cubeTbls);
        }

        for (RptCubeCond cubeCond : cubeConds) {
            cubeCond.setTables(cubeTbls);
        }

        for (RptRptFld fld : allrFlds) {
            if (fld.fldId != null) {
                fld.setCubeFld(RptCubeFld.find(fld.fldId, cubeFlds));
            }
        }
        rows = RptRptFld.findFlds(allrFlds, "row");
        cols = RptRptFld.findFlds(allrFlds, "col");
        joins = RptRptFld.findFlds(allrFlds, "join");
        allFilts = RptRptFld.findFlds(allrFlds, "filt");
        
        RptRptFld[] adims = new RptRptFld[rows.size() + cols.size()];
        System.arraycopy(rows.toArray(), 0, adims, 0, rows.size());
        System.arraycopy(cols.toArray(), 0, adims, rows.size(), cols.size());
        dims = new ArrayList<>();
        Collections.addAll(dims, adims);

        List<RptRptFld> lFilts = new ArrayList<>();
        for (RptRptFld allrFld : allFilts) {
            if (allrFld.filtJson != null) {
                lFilts.add(allrFld);
            }
        }
        filts = lFilts;        
    }

    public boolean sortableByValues() {
        switch (rpt.type) {
            case "clustered":
            case "stacked":
            case "stacked100":
            case "line":
                return rows.isEmpty();
            case "pie":
            case "donnut":
                return false;
            case "pivot":
                return cols.isEmpty();
            case "table":
                return false;
            default:
                throw new RuntimeException("Type " + rpt.type + " no recognized");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RptInfo) {
            return obj.hashCode() == hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.cube);
        hash = 13 * hash + Objects.hashCode(this.rpt);
        hash = 13 * hash + Objects.hashCode(this.allrFlds);
        hash = 13 * hash + Objects.hashCode(this.cubeTbls);
        hash = 13 * hash + Objects.hashCode(this.cubeFlds);
        hash = 13 * hash + Objects.hashCode(this.cubeConds);
        hash = 13 * hash + Objects.hashCode(this.rows);
        hash = 13 * hash + Objects.hashCode(this.cols);
        hash = 13 * hash + Objects.hashCode(this.joins);
        hash = 13 * hash + Objects.hashCode(this.filts);
        hash = 13 * hash + Objects.hashCode(this.allFilts);
        hash = 13 * hash + Objects.hashCode(this.dims);
        return hash;
    } 
}
