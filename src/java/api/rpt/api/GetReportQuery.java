package api.rpt.api;

import api.rpt.model.RptInfo;
import api.rpt.api.dataTypes.DataType;
import api.rpt.model.RptCubeCond;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptCubeTbl;
import api.rpt.model.RptRptFld;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static utilities.MySQLQuery.getEnumOptionsAsMatrix;

public class GetReportQuery {

    public static void findDepTbls(RptCubeTbl tbl, List<RptCubeTbl> allTbls, List<RptCubeTbl> reqTbls) {
        if (!reqTbls.contains(tbl)) {
            reqTbls.add(tbl);
        }

        if (tbl.tbls == null || tbl.tbl.length() == 0) {
            tbl.setTables(allTbls);
        }

        for (RptCubeTbl rTbl : tbl.tbls) {
            //findDepTbls(RptCubeTbl.find(rTbl.id, allTbls), allTbls, reqTbls);
            findDepTbls(rTbl, allTbls, reqTbls);
        }
    }

    public static String getColQuery(RptCubeFld cFld) throws Exception {
        String hb = "";
        switch (cFld.fldType) {
            case "fld": {
                RptCubeTbl tbl = cFld.tbls[0];
                if (cFld.dataType.equals("enum")) {
                    String[][] opts = getEnumOptionsAsMatrix(cFld.enumOpts);
                    hb += ("CASE " + tbl.alias + "." + cFld.name + System.lineSeparator());
                    for (String[] optRow : opts) {
                        hb += ("WHEN '" + optRow[0] + "' THEN '" + optRow[1] + "'" + System.lineSeparator());
                    }
                    hb += ("END");
                } else {
                    hb += cFld.type.getFunction(tbl.alias + "." + cFld.name);
                }
            }
            break;
            case "custom":
                String q = cFld.query;
                for (int j = 0; j < cFld.tbls.length; j++) {
                    q = q.replaceAll("@" + (j + 1), cFld.tbls[j].alias);
                }
                hb += cFld.type.getFunction(q);
                break;
            default:
                throw new Exception("Tipo no reconocido: " + cFld.fldType);
        }
        return hb;
    }

    public static String getColQueryJoin(RptRptFld rFld, RptInfo info) throws Exception {
        if (rFld.fldId == null) {
            if (rFld.kpiName != null) {
                return (composeKpi(rFld, info));
            } else if (rFld.fxName != null) {
                return (composeFx(rFld, info));
            } else {
                throw new RuntimeException();
            }
        }

        String str = "";
        String fldName = getColQuery(rFld.getCubeFld());
        String dist = rFld.oper.equals("cnt_dist") ? "DISTINCT " : "";

        switch (rFld.oper) {
            case "sum":
                str = String.format("SUM(%s)", fldName);
                break;
            case "cnt":
            case "cnt_dist":
                String oper = null;
                if (rFld.filtType != null) {
                    switch (rFld.filtType) {
                        case "less":
                            oper = String.format("%s < %d", fldName, rFld.getFiltList().get(0));
                            break;
                        case "less_e":
                            oper = String.format("%s <= %d", fldName, rFld.getFiltList().get(0));
                            break;
                        case "great":
                            oper = String.format("%s > %d", fldName, rFld.getFiltList().get(0));
                            break;
                        case "great_e":
                            oper = String.format("%s >= %d", fldName, rFld.getFiltList().get(0));
                            break;
                        case "eq":
                            oper = String.format("%s = %d", fldName, rFld.getFiltList().get(0));
                            break;
                        case "n_eq":
                            oper = String.format("%s <> %d", fldName, rFld.getFiltList().get(0));
                            break;
                        case "lst":
                            PlainBuilder pb = new PlainBuilder();
                            addList(rFld, pb);
                            oper = pb.toString();
                            break;
                        case "bet":
                            oper = String.format("%s BETWEEN %d AND %d", fldName, rFld.getFiltList().get(0), rFld.getFiltList().get(1));
                            break;
                        default:
                            break;
                    }
                    str = String.format("COUNT(%sIF(%s, 1, NULL))", dist, oper);
                } else {
                    str = String.format("COUNT(%s%s)", dist, fldName);
                }
                break;
            case "max":
                str = String.format("MAX(%s)", fldName);
                break;
            case "min":
                str = String.format("MIN(%s)", fldName);
                break;
            case "avg":
                str = String.format("AVG(%s)", fldName);
                break;
            case "desv":
                str = String.format("STD(%s)", fldName);
                break;
            default:
                throw new RuntimeException("Operación no reconocida: " + rFld.oper);
        }
        return str;
    }

    public static String getColQuery(RptRptFld rFld) throws Exception {

        if (rFld.fldId != null) {
            return getColQuery(rFld.getCubeFld());
        } else {
            return "";
        }
    }

    public static void addFrom(PlainBuilder hb, List<RptCubeTbl> tbls) {
        hb.add("FROM").br();
        for (RptCubeTbl tbl : tbls) {
            if (tbl.type.equals("main")) {
                hb.add(tbl.tbl).sp();
                hb.add(tbl.alias);
            } else {
                if (tbl.type.equals("inner")) {
                    hb.add("INNER ");
                } else if (tbl.type.equals("left")) {
                    hb.add("LEFT ");
                }
                hb.add("JOIN ").add(tbl.tbl).sp().add(tbl.alias).add(" ON ");
                if (tbl.cond != null && !tbl.cond.isEmpty()) {
                    String cond = tbl.cond.replaceAll("@\\.", tbl.alias + ".");
                    for (int j = 0; j < tbl.tbls.length; j++) {
                        RptCubeTbl t = tbl.tbls[j];
                        cond = cond.replaceAll("@" + (j + 1) + "\\.", t.alias + ".");
                    }
                    hb.add(" " + cond);
                }
            }
            hb.br();
        }
    }

    public static void addList(RptRptFld filt, PlainBuilder hb) throws Exception {
        if (filt.getCubeFld().unique) {
            hb.add(filt.getCubeFld().tbls[0].alias).add(".id ");
        } else {
            hb.add(getColQuery(filt));
        }
        
        if (filt.getFiltList().size() == 1 && filt.getFiltList().get(0) == null) {
            hb.add(" IS NULL");
        } else {
            if (filt.getFiltList().size() == 1) {
                hb.add(" = ");
            } else {
                hb.add(" IN (");
            }

            for (int i = 0; i < filt.getFiltList().size(); i++) {
                if (filt.getCubeFld().unique) {
                    hb.add(filt.getFiltList().get(i).toString());
                } else {
                    hb.add(DataType.getType(filt.getCubeFld().dataType).getAsSQLString(filt.getFiltList().get(i)));
                }
                hb.add(",");
            }
            hb.remove(1);
            if (filt.getFiltList().size() > 1) {
                hb.add(")");
            }
        }
    }

    public static void addWhere(PlainBuilder hb, RptInfo info, Integer skipFiltId) throws Exception {
        if (!info.cubeConds.isEmpty() || info.filts.size() - (skipFiltId != null ? 1 : 0) > 0) {
            hb.add("WHERE ").br();
            for (RptCubeCond cond : info.cubeConds) {
                for (int i = 0; i < cond.tbls.length; i++) {
                    RptCubeTbl tbl = cond.tbls[i];
                    cond.query = cond.query.replaceAll("@" + (i + 1) + "\\.", tbl.alias + ".");
                }
                hb.add(cond.query);
                hb.add(" AND ");
            }
            for (RptRptFld filt : info.filts) {
                if (skipFiltId == null || !skipFiltId.equals(filt.id)) {
                    if (filt.filtType.equals("lst")) {
                        addList(filt, hb);
                        hb.add(" AND ");
                    } else {
                        hb.add(getColQuery(filt)).add(" ");
                        switch (filt.filtType) {
                            case "less":
                                hb.add("<");
                                break;
                            case "less_e":
                                hb.add("<=");
                                break;
                            case "great":
                                hb.add(">");
                                break;
                            case "great_e":
                                hb.add(">=");
                                break;
                            case "eq":
                                hb.add("=");
                                break;
                            case "n_eq":
                                hb.add("<>");
                                break;
                            case "bet":
                                hb.add("BETWEEN");
                                break;
                            default:
                                break;
                        }
                        hb.add(" ");
                        DataType dt = DataType.getType(filt.getCubeFld().dataType);
                        hb.add(dt.getAsSQLString(filt.getFiltList().get(0)));
                        if (filt.filtType.equals("bet")) {
                            hb.add(" AND ");
                            hb.add(dt.getAsSQLString(filt.getFiltList().get(1)));
                        }
                        hb.add(" AND ");
                    }
                }
            }
            hb.remove(4);
        }
    }

    public static void sortTbls(List<RptCubeTbl> tbls) {
        Collections.sort(tbls, new Comparator<RptCubeTbl>() {
            @Override
            public int compare(RptCubeTbl o1, RptCubeTbl o2) {
                return Integer.compare(o1.place, o2.place);
            }
        });
    }

    public static String composeKpi(RptRptFld join, RptInfo info) throws Exception {

        String r1 = null;
        String r2 = null;
        String r3 = null;

        switch (join.kpiType) {
            case "low":
                r1 = "ok";
                r2 = "warn";
                r3 = "error";
                break;
            case "high":
                r1 = "error";
                r2 = "warn";
                r3 = "ok";
                break;
            case "bet":
                r1 = "error";
                r2 = "ok";
                r3 = "error";
                break;
            default:
                break;
        }

        String v = getColQueryJoin(RptRptFld.find(join.kpiValId, info.joins), info);
        String l1;
        String l2;

        if (join.kpiL1Kte != null) {
            l1 = join.kpiL1Kte.toString();
        } else {
            l1 = getColQueryJoin(RptRptFld.find(join.kpiL1Id, info.joins), info);
        }

        if (join.kpiL2Kte != null) {
            l2 = join.kpiL2Kte.toString();
        } else {
            l2 = getColQueryJoin(RptRptFld.find(join.kpiL2Id, info.joins), info);
        }
        return "IF(" + v + " IS NOT NULL, IF(" + v + " <= " + l1 + ", '" + r1 + "', IF(" + v + " >= " + l2 + ", '" + r3 + "', '" + r2 + "')), NULL)";
    }

    public static String composeFx(RptRptFld join, RptInfo info) throws Exception {
        String fx = join.fx;
        for (RptRptFld fld : info.joins) {
            if (fld.fx == null && fld.kpiName == null) {
                fx = fx.replaceAll("@" + fld.id + "@", "(" + getColQueryJoin(fld, info) + ")");
            }
        }
        return "(" + fx + ")";
    }

    public static String getRptQuery(RptInfo info) throws Exception {
        List<RptCubeTbl> tbls = new ArrayList<>();
        for (RptRptFld fld : info.allrFlds) {
            if (fld.getCubeFld() != null) {
                for (RptCubeTbl tbl : fld.getCubeFld().tbls) {
                    findDepTbls(tbl, info.cubeTbls, tbls);
                }
            }
        }
        for (RptCubeCond cond : info.cubeConds) {
            for (RptCubeTbl tbl : cond.tbls) {
                findDepTbls(RptCubeTbl.find(tbl.id, info.cubeTbls), info.cubeTbls, tbls);
            }
        }
        sortTbls(tbls);

        //'row','col','join','filt'
        PlainBuilder hb = new PlainBuilder();
        hb.add("SELECT").br();
        for (RptRptFld fld : info.dims) {
            hb.add(getColQuery(fld)).add(",").br();
        }

        for (RptRptFld joinFld : info.joins) {

            hb.add(getColQueryJoin(joinFld, info)).add(",").br();

        }
        
        hb.remove(1).removeBr().br();        
        addFrom(hb, tbls);
        addWhere(hb, info, null);

        hb.br().add("GROUP BY ").br();
        for (RptRptFld fld : info.dims) {
            if (fld.getCubeFld().unique) {
                hb.add(fld.getCubeFld().tbls[0].alias).add(".id").add(", ");
            } else {
                hb.add(getColQuery(fld)).add(", ");
            }
        }
        hb.remove(2);

        boolean sort = false;

        if (info.sortableByValues()) {
            for (RptRptFld met : info.joins) {
                if (met.sort != null && !met.sort.equals("none")) {
                    sort = true;
                    break;
                }
            }
        }

        if (sort) {
            hb.br().add("ORDER BY ").br();
            for (RptRptFld met : info.joins) {
                if (met.sort != null && !met.sort.equals("none")) {
                    hb.add(getColQueryJoin(met, info)).add(" ").add(met.sort).add(", ");
                }
            }
            hb.remove(2);
        }

        if (info.sortableByValues() && info.rpt.limitRows != null) {
            hb.br().add("LIMIT ").add(info.rpt.limitRows.toString()).br();
        }
        return hb.toString();
    }
}
