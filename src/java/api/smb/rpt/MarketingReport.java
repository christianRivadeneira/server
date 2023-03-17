package api.smb.rpt;

import api.sys.model.City;
import api.sys.model.Neigh;
import api.sys.model.Sector;
import api.sys.model.Zone;
import api.trk.model.CylinderType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import model.marketing.lists.CylinderAreaCount;
import model.marketing.lists.CylinderTypeCount;
import model.marketing.lists.MonthlySeedingListItem1;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

public class MarketingReport {

    //Contracts
    public static MySQLReport getContractReport(Integer zone, Integer city, Integer sector, Integer neigh, Date begin, Date end, int tdate, String sower, String writer, String veh, Boolean checked, int anull, int cyls, boolean moreThan, int typeNeigh, int program, boolean data, int deposit, Boolean showAddFields, Connection conn) throws Exception {
        //typeNeigh 0 Todos los barrios, 1 Barrios Urbanos, 2 Barrios Rurales
        try {
            MySQLReport rep = new MySQLReport("Exportar Contratos de Clientes Afiliados", "", "exp_cts_afils", MySQLQuery.now(conn));
            String title;

            if (zone != null) {
                title = "Zona: " + new MySQLQuery("SELECT name FROM zone WHERE id = ?1").setParam(1, zone).getAsString(conn);
                if (city != null) {
                    title += " Poblado: " + new MySQLQuery("SELECT name FROM city WHERE id = ?1").setParam(1, city).getAsString(conn);
                    if (sector != null) {
                        title += " Sector: " + new Sector().select(sector, conn).name;
                        if (neigh != null) {
                            title += " Barrio: " +new Neigh().select(neigh, conn).name;
                        } else {
                            title += " Barrios: Todos";
                        }
                    } else {
                        title += " Sector: Todos Barrios: Todos";
                    }
                } else {
                    title += " Poblado: Todas Sector: Todos Barrios: Todos";
                }
            } else {
                title = "Zona: Todas Poblado: Todas Sector: Todos Barrios: Todos";
            }
            rep.setVerticalFreeze(5);
            rep.setHorizontalFreeze(4);
            rep.getSubTitles().add(title);
            rep.setZoomFactor(80);
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            //rep.getFormats().get(0).setWrap(true);
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3

            String str1
                    = "SELECT "
                    + "c.id, " //0
                    + "c.contract_num, " //1
                    + "c.document, " //3
                    + "concat(coalesce(c.first_name,' '),' ',coalesce(c.last_name,' '))," //2
                    + "c.address," //4
                    + "c.phones," //5
                    + "c.people," //6
                    + "IF(c.own,\"Propia\",\"Arrendada\"), " //7
                    + "e.name," //8
                    + "t.name," //9
                    + "ene.name, " //10
                    + "IF(c.order_avg = 1,'Preventa','Ninguno')," //11
                    + "c.sign_date, " //12
                    + "v.plate," //13
                    + "concat(sower.first_name,\" \",sower.last_name),"//14
                    + "sower.document, "//15
                    + "concat(writer.first_name,\" \",writer.last_name),"//16
                    + "writer.document, "//17
                    + "z.name, " //18
                    + "ct.name," //19
                    + "s.name, "//20
                    + "n.name, "//21
                    + "c.notes, " //22
                    + "anull.description, " //23
                    + "cancel.description," //24
                    + "c.cancel_date, " //25
                    + "c.deposit " //25
                    + "FROM contract c "
                    + "LEFT JOIN neigh n ON c.neigh_id = n.id ";
            if (typeNeigh == 1) {
                str1 += " AND n.type ='b_urb' ";
            } else if (typeNeigh == 2) {
                str1 += " AND n.type = 'b_rul' ";
            }
            str1 += "LEFT JOIN sector s ON n.sector_id = s.id "
                    + "LEFT JOIN city ct ON s.city_id = ct.id "
                    + "LEFT JOIN zone z ON ct.zone_id = z.id "
                    + "LEFT JOIN vehicle v ON c.vehicle_id = v.id "
                    + "LEFT JOIN employee sower ON c.sower = sower.id "
                    + "LEFT JOIN employee writer ON c.writer = writer.id "
                    + "LEFT JOIN establish e ON c.establish_id = e.id "
                    + "LEFT JOIN establish t ON  e.establish_id = t.id "
                    + "LEFT JOIN energy ene ON c.energy_id = ene.id "
                    + "LEFT JOIN cause anull ON c.anull_cause_id = anull.id "
                    + "LEFT JOIN cause cancel ON c.cancel_cause_id = cancel.id ";
            if (cyls > 0) {
                str1 += "INNER JOIN smb_ctr_cyl cyl ON cyl.contract_id = c.id AND cyl.action = 'd'";
            }
            String str2 = "c.ctr_type='afil' AND ";

            if (neigh != null) {
                str2 += "n.id=" + neigh + " AND ";
            } else if (sector != null) {
                str2 += "s.id=" + sector + " AND ";
            } else if (city != null) {
                str2 += "ct.id=" + city + " AND ";
            } else if (zone != null) {
                str2 += "z.id=" + zone + " AND ";
            }

            if (tdate == 0) {
                str2 += "?1 <= c.sign_date AND c.sign_date <= ?2 AND ";
            } else if (tdate == 1) {
                str2 += "?1 <= c.cancel_date AND c.cancel_date <= ?2 AND ";
            }

            if (sower != null) {
                str2 += "sower.document=" + sower + " AND ";
            }

            if (deposit == 1) {
                str2 += "c.deposit IS NOT NULL AND ";
            } else if (deposit == 2) {
                str2 += "c.deposit IS NULL AND ";
            }

            if (writer != null) {
                str2 += "writer.document=" + writer + " AND ";
            }

            if (veh != null) {
                str2 += "v.plate=?3 AND ";
            }

            if (checked != null) {
                if (checked) {
                    str2 += "c.checked = 1 AND ";
                } else {
                    str2 += "c.checked = 0 AND ";
                }
            }

            if (anull != 3) {
                if (anull == 1) {
                    str2 += "c.anull_cause_id IS NOT NULL AND ";
                } else {
                    str2 += "c.anull_cause_id IS NULL AND ";
                }
            }

            if (program == 2) {
                str2 += "c.order_avg = 1 ";
            }

            if (str2.length() > 0) {
                str2 = "WHERE " + str2.substring(0, str2.length() - 5);
                str1 += str2;
                if (cyls > 0) {
                    if (moreThan) {
                        str1 += " GROUP BY c.id HAVING sum(cyl.amount) > " + cyls + " ";
                    } else {
                        str1 += " GROUP BY c.id HAVING sum(cyl.amount) = " + cyls + " ";
                    }
                }
                str1 += " ORDER BY c.contract_num ASC ";
            } else {
                str1 += " ORDER BY c.contract_num ASC ";
            }

            MySQLQuery qContract = new MySQLQuery(str1);
            qContract.setParam(1, begin);
            qContract.setParam(2, end);
            qContract.setParam(3, veh);
            Object[][] contracts = qContract.getRecords(conn);
            MySQLQuery qCylCount = new MySQLQuery("SELECT type_id, SUM(amount) "
                    + "FROM smb_ctr_cyl "
                    + "WHERE contract_id = ?1 "
                    + "AND action = ?3 "
                    + "GROUP BY type_id");

            Table tbl = new Table("");
            tbl.getColumns().add(new Column("Contrato", 10, 0));//0
            tbl.getColumns().add(new Column("Documento", 15, 0));//2
            tbl.getColumns().add(new Column("Nombre", 30, 0));//1
            tbl.getColumns().add(new Column("Dirección", 29, 0));//3
            if (data) {
                tbl.getColumns().add(new Column("Teléfono", 15, 0));//4
                tbl.getColumns().add(new Column("Hab.", 8, 1));//5
                tbl.getColumns().add(new Column("Vivienda", 12, 0));//6
                tbl.getColumns().add(new Column("Establecimiento", 13, 0));//7
                tbl.getColumns().add(new Column("Tipo", 12, 0));//8
                tbl.getColumns().add(new Column("Comb.", 12, 0));//9
            }
            tbl.getColumns().add(new Column("Programa", 12, 0));//10****
            tbl.getColumns().add(new Column("Fecha", 12, 2));//11
            if (data) {
                tbl.getColumns().add(new Column("Vehículo", 12, 0));//12
                tbl.getColumns().add(new Column("Sembrador", 30, 0));//13
                tbl.getColumns().add(new Column("Cédula", 12, 0));//14
                tbl.getColumns().add(new Column("Digitador", 30, 0));//15
                tbl.getColumns().add(new Column("Cédula", 12, 0));//16
            }
            tbl.getColumns().add(new Column("Zona", 29, 0));//17
            tbl.getColumns().add(new Column("Poblado", 29, 0));//18
            if (data) {
                tbl.getColumns().add(new Column("Sector", 29, 0));//19
                tbl.getColumns().add(new Column("Barrio", 29, 0));//20
                tbl.getColumns().add(new Column("Notas", 29, 0));//21**
                tbl.getColumns().add(new Column("Anulación", 29, 0));//22
                tbl.getColumns().add(new Column("Cancelación", 29, 0));//23
                tbl.getColumns().add(new Column("Fec Cancel", 12, 2));//24
            }

            List<CylinderType> types = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("") + " "
                    + "FROM cylinder_type ORDER BY capacity ASC"), conn);

            for (CylinderType type : types) {
                tbl.getColumns().add(new Column("R", 8, 1));//0
                tbl.getColumns().add(new Column("D", 8, 1));//0
            }
            TableHeader header = new TableHeader();
            tbl.getHeaders().add(header);

            if (data) {
                int columns = (MySQLQuery.getAsBoolean(showAddFields) ? 11 : 6);//11 - 5 = 6; porque son los campos adicionales que se controlan por cfg 
                header.getColums().add(new HeaderColumn("Datos del Contrato", columns, 1));
                header.getColums().add(new HeaderColumn("Datos Fijos", 11, 1));
                header.getColums().add(new HeaderColumn("Anulación Cancelación", 3, 1));
            } else {
                header.getColums().add(new HeaderColumn("Datos del Contrato", 5, 1));
                header.getColums().add(new HeaderColumn("Datos Fijos", 3, 1));
            }

            for (CylinderType type : types) {
                header.getColums().add(new HeaderColumn(type.name, 2, 1));
            }

            header.getColums().add(new HeaderColumn("Deposito", 1, 2));
            tbl.getColumns().add(new Column("Deposito", 15, 3));//25

            int[] remCols = null;
            if (!data) {
                remCols = new int[]{5, 6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25};
            } else if (!showAddFields) {
                remCols = new int[]{6, 7, 8, 9, 10};
            }

            tbl.setSummaryRow(new SummaryRow("Totales", data ? 25 : 8));
            if (contracts.length > 0) {
                for (int i = 0; i < contracts.length; i++) {
                    List<Object> row = new ArrayList<>();
                    for (int j = 1; j < 26; j++) {
                        boolean checkAdd = checkAdd(remCols, j);
                        if (checkAdd) {
                            System.err.println("");
                            row.add(contracts[i][j]);
                        }
                    }
                    qCylCount.setParam(1, contracts[i][0]);
                    qCylCount.setParam(3, "r");
                    Object[][] rCyls = qCylCount.getRecords(conn);
                    qCylCount.setParam(3, "d");
                    Object[][] dCyls = qCylCount.getRecords(conn);

                    for (CylinderType type : types) {
                        long r = 0;
                        for (int k = 0; k < rCyls.length; k++) {
                            if (((Integer) rCyls[k][0]).intValue() == type.id) {
                                r = MySQLQuery.getAsLong(rCyls[k][1]);
                                break;
                            }
                        }
                        row.add(r);

                        long d = 0;
                        for (int k = 0; k < dCyls.length; k++) {
                            if (((Integer) dCyls[k][0]).intValue() == type.id) {
                                d = MySQLQuery.getAsLong(dCyls[k][1]);
                                break;
                            }
                        }
                        row.add(d);
                    }
                    row.add(contracts[i][contracts[i].length - 1]);
                    tbl.addRow(row.toArray());
                }
                rep.getTables().add(tbl);
            }
            System.gc();
            return rep;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public static MySQLReport getCtrComoReport(Integer zone, Integer city, Integer sector, Integer neigh, Date begin, Date end, int tdate, String sower, String writer, String veh, Boolean checked, int anull, int cyls, boolean moreThan, int tneigh, int program, boolean data, Connection conn) throws Exception {

        MySQLReport rep = new MySQLReport("Exportar Contratos de Clientes en Comodato", "", "exp_cts_como", MySQLQuery.now(conn));
        String title;

        if (zone != null) {
            title = "Zona: " + new MySQLQuery("SELECT name FROM zone WHERE id = ?1").setParam(1, zone).getAsString(conn);
            if (city != null) {
                title += " Poblado: " + new MySQLQuery("SELECT name FROM city WHERE id = ?1").setParam(1, city).getAsString(conn);
                if (sector != null) {
                    title += " Sector: " + new Sector().select(sector, conn).name;
                    if (neigh != null) {
                        title += " Barrio: " + new Neigh().select(neigh, conn).name;
                    } else {
                        title += " Barrios: Todos";
                    }
                } else {
                    title += " Sector: Todos Barrios: Todos";
                }
            } else {
                title += " Poblado: Todos Sector: Todos Barrios: Todos";
            }
        } else {
            title = "Zona: Todas Poblado: Todos Sector: Todos Barrios: Todos";
        }
        rep.setVerticalFreeze(5);
        rep.setHorizontalFreeze(5);
        rep.getSubTitles().add(title);
        rep.setZoomFactor(80);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "##,##0"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$#,##0.00"));//1
        //'rut_nat','rut_jur',
        String str1
                = "SELECT "
                + "c.id, " //0
                + "c.contract_num, " //1
                + "c.cli_type, " //2
                + "c.document, " //3
                + "concat(coalesce(c.first_name,''),' ',coalesce(c.last_name,' ')), " //4
                + "c.address," //5
                + "c.phones," //6
                + "IF(c.order_avg = 1,'Preventa','Ninguno'), " //8
                + "c.como_beg_date, " //9
                + "c.como_end_date, "//10
                + "c.como_collect_date, "//11
                + "v.plate," //12
                + "concat(sower.first_name,\" \",sower.last_name),"//13
                + "sower.document, "//14
                + "concat(writer.first_name,\" \",writer.last_name),"//15
                + "writer.document, "//16
                + "z.name, " //17
                + "ct.name," //18
                + "s.name, "//19
                + "n.name, "//20
                + "c.notes, " //21
                + "anull.description, " //22
                + "cancel.description," //23
                + "c.cancel_date,"//24
                + "c.deposit "//25
                + "FROM contract c "
                + "LEFT JOIN neigh n ON c.neigh_id = n.id  "
                + "LEFT JOIN sector s ON n.sector_id = s.id "
                + "LEFT JOIN city ct ON s.city_id = ct.id "
                + "LEFT JOIN zone z ON ct.zone_id = z.id "
                + "LEFT JOIN vehicle v ON c.vehicle_id = v.id "
                + "LEFT JOIN employee sower ON c.sower = sower.id "
                + "LEFT JOIN employee writer ON c.writer = writer.id "
                + "LEFT JOIN cause anull ON c.anull_cause_id = anull.id "
                + "LEFT JOIN cause cancel ON c.cancel_cause_id = cancel.id ";
        if (cyls > 0) {
            str1 += "INNER JOIN smb_ctr_cyl cyl ON cyl.contract_id = c.id AND cyl.action = 'd'";
        }
        String str2 = "c.ctr_type='como' AND ";

        //filtro por Id del barrio
        if (neigh != null) {
            str2 += "n.id=" + neigh + " AND ";
        } else if (sector != null) {
            str2 += "s.id=" + sector + " AND ";
        } else if (city != null) {
            str2 += "ct.id=" + city + " AND ";
        } else if (zone != null) {
            str2 += "z.id=" + zone + " AND ";
        }
        //filtro por tipo de barrio
        if (tneigh == 1) {
            str2 += " n.type ='b_urb' AND ";
        } else if (tneigh == 2) {
            str2 += " n.type = 'b_rul' AND ";
        }

        //Inicio de Comodato, Fin de Comodato, Próxima Visita, Recolección, Cancelación
        if (tdate == 0) {
            str2 += "?1 <= c.como_beg_date AND c.como_beg_date <= ?2 AND ";
        } else if (tdate == 1) {
            str2 += "?1 <= c.como_end_date AND c.como_end_date <= ?2 AND ";
        } else if (tdate == 2) {
            str2 += "?1 <= c.como_visit_date AND c.como_visit_date <= ?2 AND ";
        } else if (tdate == 3) {
            str2 += "?1 <= c.como_collect_date AND c.como_collect_date <= ?2 AND ";
        } else if (tdate == 4) {
            str2 += "?1 <= c.cancel_date AND c.cancel_date <= ?2 AND ";
        }

        if (sower != null) {
            str2 += "sower.document=" + sower + " AND ";
        }

        if (writer != null) {
            str2 += "writer.document=" + writer + " AND ";
        }

        if (veh != null) {
            str2 += "v.plate=?3 AND ";
        }

        if (checked != null) {
            if (checked) {
                str2 += "c.checked = 1 AND ";
            } else {
                str2 += "c.checked = 0 AND ";
            }
        }

        if (anull == 0) {
            //no anulados
            str2 += "c.anull_cause_id IS NULL AND ";
        } else if (anull == 1) {
            //anulados
            str2 += "c.anull_cause_id IS NOT NULL AND ";
        }

        if (program == 2) {
            str2 += "c.order_avg = 1 ";
        }

        if (str2.length() > 0) {
            str2 = "WHERE " + str2.substring(0, str2.length() - 5);
            str1 += str2;
            if (cyls > 0) {
                if (moreThan) {
                    str1 += " GROUP BY c.id HAVING sum(cyl.amount) > " + cyls + " ";
                } else {
                    str1 += " GROUP BY c.id HAVING sum(cyl.amount) = " + cyls + " ";
                }
            }
            str1 += " ORDER BY c.contract_num ASC ";
        } else {
            str1 += " ORDER BY c.contract_num ASC ";
        }

        MySQLQuery qContract = new MySQLQuery(str1);
        qContract.setParam(1, begin);
        qContract.setParam(2, end);
        qContract.setParam(3, veh);
        Object[][] contracts = qContract.getRecords(conn);

        MySQLQuery qCylCount = new MySQLQuery("SELECT type_id, sum(amount) FROM smb_ctr_cyl WHERE contract_id = ?1 AND action = ?3 GROUP BY type_id");
        Table tbl = new Table("Contratos de Clientes");
        tbl.getColumns().add(new Column("Contrato", 10, 0));//0
        tbl.getColumns().add(new Column("Tipo Cli.", 13, 0));//1
        tbl.getColumns().add(new Column("Documento", 15, 0));//2
        tbl.getColumns().add(new Column("Nombre", 25, 0));//3
        tbl.getColumns().add(new Column("Dirección", 29, 0));//4
        tbl.getColumns().add(new Column("Teléfono", 15, 0));//5
        tbl.getColumns().add(new Column("Programa", 12, 0));//6
        tbl.getColumns().add(new Column("Inicio", 12, 2));//7
        tbl.getColumns().add(new Column("Fin", 12, 2));//8
        tbl.getColumns().add(new Column("Recolección", 13, 2));//9
        tbl.getColumns().add(new Column("Vehículo", 12, 0));//10*
        if (data) {
            tbl.getColumns().add(new Column("Sembrador", 30, 0));//11
            tbl.getColumns().add(new Column("Cédula", 12, 0));//12
            tbl.getColumns().add(new Column("Digitador", 30, 0));//13
            tbl.getColumns().add(new Column("Cédula", 12, 0));//14
            tbl.getColumns().add(new Column("Zona", 29, 0));//15
            tbl.getColumns().add(new Column("Poblado", 29, 0));//16
            tbl.getColumns().add(new Column("Sector", 29, 0));//17
            tbl.getColumns().add(new Column("Barrio", 29, 0));//18
            tbl.getColumns().add(new Column("Notas", 29, 0));//19
            tbl.getColumns().add(new Column("Anulación", 29, 0));//20
            tbl.getColumns().add(new Column("Cancelación", 29, 0));//21
            tbl.getColumns().add(new Column("Fec Cancel", 12, 2));//22
            tbl.getColumns().add(new Column("Valor", 15, 3));//13
        }

        List<CylinderType> types = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("") + " FROM cylinder_type ORDER BY capacity ASC"), conn);
        for (int i = 0; i < types.size(); i++) {
            tbl.getColumns().add(new Column("R", 8, 1));//0
            tbl.getColumns().add(new Column("D", 8, 1));//0
        }
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        if (data) {
            header.getColums().add(new HeaderColumn("Datos del Contrato", 10, 1));
            header.getColums().add(new HeaderColumn("Datos Fijos", 10, 1));
            header.getColums().add(new HeaderColumn("Anulación Cancelación", 3, 1));
            header.getColums().add(new HeaderColumn("Valor", 1, 2));
        } else {
            header.getColums().add(new HeaderColumn("Datos del Contrato", 10, 1));
            header.getColums().add(new HeaderColumn("Valor", 1, 2));
        }
        for (int i = 0; i < types.size(); i++) {
            header.getColums().add(new HeaderColumn(types.get(i).name, 2, 1));
        }

        tbl.setSummaryRow(new SummaryRow("Totales", data ? 24 : 11));
        if (contracts.length > 0) {
            for (int i = 0; i < contracts.length; i++) {
                List<Object> row = new ArrayList<>();
                int last = data ? 25 : 12;
                for (int j = 1; j < last; j++) {
                    row.add(contracts[i][j]);
                }
                qCylCount.setParam(1, contracts[i][0]);
                qCylCount.setParam(3, "r");
                Object[][] rCyls = qCylCount.getRecords(conn);
                qCylCount.setParam(3, "d");
                Object[][] dCyls = qCylCount.getRecords(conn);

                for (int j = 0; j < types.size(); j++) {
                    CylinderType type = types.get(j);
                    long r = 0;
                    for (int k = 0; k < rCyls.length; k++) {
                        if (((Integer) rCyls[k][0]).intValue() == type.id) {
                            r = MySQLQuery.getAsLong(rCyls[k][1]);
                            break;
                        }
                    }
                    row.add(r);

                    long d = 0;
                    for (int k = 0; k < dCyls.length; k++) {
                        if (((Integer) dCyls[k][0]).intValue() == type.id) {
                            d = MySQLQuery.getAsLong(dCyls[k][1]);
                            break;
                        }
                    }
                    row.add(d);
                }
                tbl.addRow(row.toArray());
            }
            rep.getTables().add(tbl);
        }
        System.gc();
        return rep;
    }

    public static List<CylinderAreaCount> getCylinderCountList(Integer zoneId, Integer cityId, Integer sectorId, Integer neighId, Integer year, Boolean cancel, Boolean checked, Date fBegin, Date fEnd, int typeNeigh, int afil, Connection conn) throws Exception {
        List<CylinderAreaCount> res = new ArrayList<>();
        List<CylinderType> types = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("") + " FROM cylinder_type ORDER BY capacity ASC"), conn);
        List<Zone> zones = null;
        List<City> cities = null;
        List<Sector> sectors = null;
        List<Neigh> districts = null;
        Neigh district = null;

        String qWhere = "";
        String whichDate;
        if (cancel == null) {
            whichDate = "sign_date";
            if (year != null) {
                qWhere += "WHERE YEAR(c.sign_date) = " + year + " ";
            } else {
                qWhere += "WHERE ?1 <= c.sign_date AND c.sign_date <= ?2  ";
            }
        } else if (cancel) {
            whichDate = "cancel_date";
            if (year != null) {
                qWhere += "WHERE YEAR(c.cancel_date) = " + year + " ";
            } else {
                qWhere += "WHERE ?1 <= c.cancel_date AND c.cancel_date <= ?2  ";
            }
        } else {
            whichDate = "sign_date";
            if (year != null) {
                qWhere += "WHERE YEAR(c.sign_date) = " + year + " ";
            } else {
                qWhere += "WHERE ?1 <= c.sign_date AND c.sign_date <= ?2  ";
            }
            qWhere += "AND c.cancel_cause_id IS NULL ";
        }

        if (checked != null) {
            if (checked) {
                qWhere += "AND c.checked = 1 ";
            } else {
                qWhere += "AND c.checked = 0 ";
            }
        }

        if (afil == 1) {
            qWhere += " AND c.ctr_type='afil' ";
        } else if (afil == 2) {
            qWhere += " AND c.ctr_type='como' ";
        }

        qWhere += " AND c.anull_cause_id IS NULL ";
        String qs;
        if (zoneId == null) {
            if (year != null) {
                qs = "SELECT ct.zone_id, cl.type_id, MONTH(c." + whichDate + ") as mon, sum(cl.amount) FROM ";
            } else {
                qs = "SELECT ct.zone_id, cl.type_id, 1, sum(cl.amount) FROM ";
            }
            qs += "contract c "
                    + "INNER JOIN smb_ctr_cyl cl ON cl.contract_id = c.id "
                    + "INNER JOIN neigh n ON c.neigh_id = n.id ";
            if (typeNeigh == 1) {
                qs += " AND n.type ='b_urb' ";
            } else if (typeNeigh == 2) {
                qs += " AND n.type= 'b_rul' ";
            }
            qs += "INNER JOIN sector s ON n.sector_id = s.id "
                    + "INNER JOIN city ct ON s.city_id = ct.id ";

            qs += qWhere;
            qs += " AND cl.action = 'd' ";

            if (year != null) {
                qs += "GROUP BY cl.type_id, ct.zone_id, mon "
                        + "ORDER BY zone_id ASC, type ASC, mon ASC ";
            } else {
                qs += "GROUP BY cl.type_id, ct.zone_id "
                        + "ORDER BY zone_id ASC, type ASC ";
            }
            zones = Zone.getAll(conn);
        } else if (cityId == null) {
            if (year != null) {
                qs = "SELECT ct.id, cl.type_id, MONTH(c." + whichDate + ") as mon, sum(cl.amount) FROM ";
            } else {
                qs = "SELECT ct.id, cl.type_id, 1, sum(cl.amount) FROM ";
            }
            qs += "contract c "
                    + "INNER JOIN smb_ctr_cyl cl ON cl.contract_id = c.id "
                    + "INNER JOIN neigh n ON c.neigh_id = n.id ";
            if (typeNeigh == 1) {
                qs += " AND n.type ='b_urb' ";
            } else if (typeNeigh == 2) {
                qs += " AND n.type= 'b_rul' ";
            }
            qs += "INNER JOIN sector s ON n.sector_id = s.id "
                    + "INNER JOIN city ct ON s.city_id = ct.id ";

            qs += qWhere;
            qs += " AND cl.action = 'd' AND ct.zone_id = " + zoneId + " ";

            if (year != null) {
                qs += "GROUP BY cl.type_id, ct.id,mon "
                        + "ORDER BY ct.id ASC, type ASC,mon ASC ";
            } else {
                qs += "GROUP BY cl.type_id, ct.id "
                        + "ORDER BY ct.id ASC, type ASC";
            }

            MySQLQuery mq = new MySQLQuery("SELECT c.name, c.zone_id, c.old_code, c.db_name, c.ct_real, c.dane_code, c.mun_id, c.pob_id, c.lat, c.lon, c.neigh_coords, c.id FROM city c WHERE c.zone_id = ?1").setParam(1, zoneId);
            cities = City.getList(mq, conn);
        } else if (sectorId == null) {
            if (year != null) {
                qs = "SELECT s.id, cl.type_id, MONTH(c." + whichDate + ") as mon, sum(cl.amount) FROM ";
            } else {
                qs = "SELECT s.id, cl.type_id, 1, sum(cl.amount) FROM ";
            }
            qs += "contract c "
                    + "INNER JOIN smb_ctr_cyl cl ON cl.contract_id = c.id "
                    + "INNER JOIN neigh n ON c.neigh_id = n.id ";
            if (typeNeigh == 1) {
                qs += " AND n.type ='b_urb' ";
            } else if (typeNeigh == 2) {
                qs += " AND n.type= 'b_rul' ";
            }
            qs += "INNER JOIN sector s ON n.sector_id = s.id ";

            qs += qWhere;
            qs += " AND cl.action = 'd' AND s.city_id = " + cityId + " ";

            if (year != null) {
                qs += "GROUP BY cl.type_id, s.id, mon "
                        + "ORDER BY s.id ASC, type ASC, mon ASC ";
            } else {
                qs += "GROUP BY cl.type_id, s.id "
                        + "ORDER BY s.id ASC, type ASC ";
            }
            sectors = Sector.getSectorsByCity(cityId, conn);
        } else if (neighId == null) {
            if (year != null) {
                qs = "SELECT n.id, cl.type_id, MONTH(c." + whichDate + ") as mon, sum(cl.amount) FROM ";
            } else {
                qs = "SELECT n.id, cl.type_id, 1, sum(cl.amount) FROM ";
            }
            qs += "contract c "
                    + "INNER JOIN smb_ctr_cyl cl ON cl.contract_id = c.id "
                    + "INNER JOIN neigh n ON c.neigh_id = n.id ";
            if (typeNeigh == 1) {
                qs += " AND n.type ='b_urb' ";
            } else if (typeNeigh == 2) {
                qs += " AND n.type= 'b_rul' ";
            }

            qs += qWhere;
            qs += " AND cl.action = 'd' AND n.sector_id = " + sectorId + " ";

            if (year != null) {
                qs += "GROUP BY cl.type_id, n.id, mon "
                        + "ORDER BY n.id ASC, type ASC, mon ASC ";
            } else {
                qs += "GROUP BY cl.type_id, n.id "
                        + "ORDER BY n.id ASC, type ASC ";
            }
            districts = Neigh.getNeighsBySector(sectorId, conn);
        } else {
            if (year != null) {
                qs = "SELECT c.neigh_id, cl.type_id, MONTH(c." + whichDate + ") as mon, sum(cl.amount) FROM ";
            } else {
                qs = "SELECT c.neigh_id, cl.type_id,1, sum(cl.amount) FROM ";
            }
            qs += "contract c "
                    + "INNER JOIN smb_ctr_cyl cl ON cl.contract_id = c.id ";

            qs += qWhere;
            qs += " AND cl.action = 'd' AND c.neigh_id = " + neighId + " ";

            if (year != null) {
                qs += "GROUP BY cl.type_id, c.neigh_id, mon "
                        + "ORDER BY c.neigh_id ASC, type ASC, mon ASC ";
            } else {
                qs += "GROUP BY cl.type_id, c.neigh_id "
                        + "ORDER BY c.neigh_id ASC, type ASC ";
            }
            district = new Neigh().select(neighId, conn);
        }

        MySQLQuery q = new MySQLQuery(qs);

        q.setParam(1, fBegin);
        q.setParam(2, fEnd);
        Object[][] l = q.getRecords(conn);
        for (Object[] row : l) {
            Integer idArea = (Integer) row[0];
            Integer cylType = (Integer) row[1];
            Integer month;
            if (year != null) {
                month = (Integer) row[2];
            } else {
                month = 1;
            }
            Integer count = ((BigDecimal) row[3]).intValue();

            CylinderAreaCount ac = null;
            for (int i = 0; i < res.size() && ac == null; i++) {
                if (res.get(i).getIdArea().equals(idArea)) {
                    ac = res.get(i);
                }
            }

            if (ac == null) {
                ac = new CylinderAreaCount();
                ac.setIdArea(idArea);
                if (zones != null) {
                    for (Zone zone : zones) {
                        if (zone.id == idArea) {
                            ac.setName(zone.name);
                            break;
                        }
                    }
                } else if (cities != null) {
                    for (City city : cities) {
                        if (city.id == (idArea)) {
                            ac.setName(city.name);
                            break;
                        }
                    }
                } else if (sectors != null) {
                    for (Sector sector : sectors) {
                        if (sector.id == idArea) {
                            ac.setName(sector.name);
                            break;
                        }
                    }
                } else if (districts != null) {
                    for (Neigh dist : districts) {
                        if (dist.id == idArea) {
                            ac.setName(dist.name);
                            break;
                        }
                    }
                } else {
                    ac.setName(district.name);
                }
                res.add(ac);
            }

            CylinderTypeCount cc = null;
            for (int i = 0; i < ac.getCounters().size() && cc == null; i++) {
                if (ac.getCounters().get(i).getCilinderType().equals(cylType)) {
                    cc = ac.getCounters().get(i);
                }
            }
            if (cc == null) {
                cc = new CylinderTypeCount();
                cc.setCilinderType(cylType);

                for (int i = 0; i < types.size(); i++) {
                    if (types.get(i).id == (cylType)) {
                        cc.setTypeName(types.get(i).name);
                        break;
                    }
                }
                ac.getCounters().add(cc);
            }
            cc.getCount().add(count);
            cc.getMonths().add(month);
        }
        return res;
    }

    public static List<MonthlySeedingListItem1> getMonthlySeeding(int year, int month, Integer zoneId, Integer cylTypeId, Integer sowerId, int anul, int canc, int afil, Connection conn) throws Exception {
        String whatDate;
        if (canc == 1) {
            whatDate = "cancel_date";
        } else {
            whatDate = "sign_date";
        }

        String qs = "SELECT extract(day from " + whatDate + ") as dia, cl.type_id, sum(amount) "
                + "FROM contract ct "
                + "INNER JOIN smb_ctr_cyl cl ON ct.id = cl.contract_id "
                + "INNER JOIN neigh ne ON ne.id = ct.neigh_id "
                + "INNER JOIN sector se ON se.id = ne.sector_id "
                + "INNER JOIN city cy ON cy.id = se.city_id "
                + "WHERE "
                + "extract(month from " + whatDate + ") = " + month + " AND extract(year from " + whatDate + ") = " + year + " "
                + "AND cl.action = 'd' AND ct.ctr_type='afil' ";
        if (anul == 1) {
            qs += "AND ct.anull_cause_id is not null ";
        } else {
            qs += "AND ct.anull_cause_id is null ";
        }

        if (canc == 1) {
            qs += "AND ct.cancel_cause_id is not null ";
        } else {
            qs += "AND ct.cancel_cause_id is null ";
        }

        if (zoneId != null) {
            qs += "AND cy.zone_id = " + zoneId + " ";
        }
        if (sowerId != null) {
            qs += "AND ct.sower = " + sowerId + " ";
        }
        if (cylTypeId != null) {
            qs += "AND cl.type_id = " + cylTypeId + " ";
        }

        qs += "group by dia, cl.type_id ";
        MySQLQuery q = new MySQLQuery(qs);
        Object[][] res = q.getRecords(conn);
        List<MonthlySeedingListItem1> rta = new ArrayList<>();
        rta.add(new MonthlySeedingListItem1(0));
        for (int i = 1; i <= 31; i++) {
            rta.add(new MonthlySeedingListItem1(i));
        }

        for (Object[] row : res) {
            int day = (Integer) row[0];
            int type = (Integer) row[1];
            long count = MySQLQuery.getAsLong(row[2]);
            rta.get(day).counters.add(new Object[]{type, count});
        }
        return rta;
    }

    //serie
    //contratos que faltan en una serie
    public static MySQLReport getCorrectsContracts(int zone, int city, int sector, int neigh, int state, Date begin, Date end, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Contratos en Revisión ", "", "cts_revs", MySQLQuery.now(conn));
        String title;
        if (zone > 0) {
            title = "Zona: " + new MySQLQuery("SELECT name FROM zone WHERE id = ?1").setParam(1, zone).getAsString(conn);
            if (city > 0) {
                title += " Poblado: " + new MySQLQuery("SELECT name FROM city WHERE id = ?1").setParam(1, city).getAsString(conn);
                if (sector > 0) {
                    title += " Sector: " + new Sector().select(sector, conn).name;
                    if (neigh > 0) {
                        title += " Barrio: " + new Neigh().select(neigh, conn).name;
                    } else {
                        title = " Barrios: Todos";
                    }
                } else {
                    title = " Sector: Todos Barrios: Todos";
                }
            } else {
                title = " Poblado: Todos Sector: Todos Barrios: Todos";
            }
        } else {
            title = "Zona: Todas Poblado: Todos Sector: Todos Barrios: Todos";
        }
        rep.getSubTitles().add(title);
        rep.setZoomFactor(80);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.setVerticalFreeze(4);

        String str = "SELECT "
                + "ct.contract_num, "//0
                + "concat(ifnull(ct.first_name,' '),' ',ifnull(ct.last_name,' ')), "//2
                + "ct.document, "//3
                + "ct.address, "
                + "n.name, "
                + "ct.phones, "//5
                + "IF(ct.anull_cause_id is null, IF(ct.correct_date is null,\"Pendiente\",\"Corregido\" ),\"Reemplazado\" ), "//6
                + "ct.review_date, "//7
                + "concat(eRev.first_name,\" \",eRev.last_name),"//
                + "ct.correct_date,  "
                + "concat(eCor.first_name,\" \",eCor.last_name),"//
                + "concat(e.first_name,\" \",e.last_name)"//8
                + "FROM "
                + "contract AS ct "
                + "INNER JOIN neigh AS n ON ct.neigh_id = n.id "
                + "INNER JOIN sector AS s ON n.sector_id = s.id "
                + "INNER JOIN city AS c ON s.city_id = c.id "
                + "INNER JOIN employee e ON ct.sower = e.id "
                + "INNER JOIN employee eRev ON ct.crea_review_id = eRev.id "
                + "LEFT JOIN employee eCor ON ct.mod_review_id = eCor.id "
                + "WHERE "
                + "ct.review_cause_id IS NOT NULL AND "
                + "?1 <= ct.review_date AND ct.review_date <= ?2 ";

        if (zone > 0) {
            str += " AND c.zone_id =" + zone + " ";
            if (city > 0) {
                str += " AND c.id =" + city + " ";
                if (sector > 0) {
                    str += " AND s.id =" + sector + " ";
                    if (neigh > 0) {
                        str += " AND n.id =" + neigh + " ";
                    }
                }
            }
        }

        if (state == 1) { //pendientes
            str += " AND ct.correct_date is null ";
        } else if (state == 2) { //Corregidos
            str += " AND ct.correct_date is not null AND ct.anull_cause_id is null ";
        } else if (state == 3) { //reemplazados
            str += " AND ct.correct_date is not null AND ct.anull_cause_id is not null ";
        }

        str += "  ORDER BY ct.contract_num ASC";

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.trimDate(begin));
        q.setParam(2, Dates.trimDate(end));

        Object[][] result = q.getRecords(conn);

        Table tbl = new Table("Contratos en Revisión");
        tbl.getColumns().add(new Column("No.", 8, 0));//0
        tbl.getColumns().add(new Column("Cliente", 25, 0));//0
        tbl.getColumns().add(new Column("Cédula", 12, 0));//0
        tbl.getColumns().add(new Column("Dirección", 25, 0));//0
        tbl.getColumns().add(new Column("Barrio", 20, 0));//0
        tbl.getColumns().add(new Column("Teléfono", 25, 0));//0
        tbl.getColumns().add(new Column("Estado", 15, 0));//0
        tbl.getColumns().add(new Column("Fec Rev", 12, 1));//0
        tbl.getColumns().add(new Column("Ingreso Rev", 25, 0));//0
        tbl.getColumns().add(new Column("Fec Correc", 12, 1));//0
        tbl.getColumns().add(new Column("Ingreso Correc", 25, 0));//0
        tbl.getColumns().add(new Column("Sembrador", 25, 0));//0

        if (result.length > 0) {
            for (int i = 0; i < result.length; i++) {
                tbl.addRow(result[i]);
            }
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getMissingContract(Long from_serie, Long to_serie, int afil, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Reporte Contratos sin Registrar", (String.format("Consulta Desde %d Hasta %d", from_serie, to_serie)), "Contratos", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.setZoomFactor(85);
        Table tb = new Table("");
        tb.getColumns().add(new Column("Número", 30, 0));

        String qs = "SELECT cast(contract_num as signed) FROM contract  "
                + "WHERE cast(contract_num as signed) >= ?1 "
                + "AND cast(contract_num <= ?2 as signed) "
                + "AND ctr_type=?3 "
                + "ORDER BY cast(contract_num as signed)";
        MySQLQuery q = new MySQLQuery(qs);
        q.setParam(1, from_serie);
        q.setParam(2, to_serie);
        if (afil == 1) {
            tb.setTitle("Afiliaciones");
            q.setParam(3, "afil");
        } else {
            tb.setTitle("Comodato");
            q.setParam(3, "como");
        }

        Object[] record = q.getRecord(conn);

        List<Object> l = new ArrayList<>();
        l.addAll(Arrays.asList(record));

        for (long i = from_serie; i <= to_serie; i++) {
            boolean found = false;
            for (int j = 0; j < l.size(); j++) {
                Long num = ((Long) l.get(j));
                if (i == num) {
                    l.remove(j);
                    found = true;
                } else if (i > num) {
                    break;
                }
            }
            if (!found) {
                tb.addRow(new Object[]{i});
            }
        }
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
            rep.getSubTitles().add(String.format("De %d contratos faltan %d ", (to_serie - from_serie) + 1, tb.getData().length));
        }
        return rep;
    }

    public static MySQLReport getClientsManyContracts(int zone, int city, int sector, int neigh, int type, int afil, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Clientes con Varias Afiliaciones ", "", "clts_afils", MySQLQuery.now(conn));
        String title;
        if (zone > 0) {
            title = "Zona: " + new MySQLQuery("SELECT name FROM zone WHERE id = ?1").setParam(1, zone).getAsString(conn);
            if (city > 0) {
                title += " Poblado: " + new MySQLQuery("SELECT name FROM city WHERE id = ?1").setParam(1, city).getAsString(conn);
                if (sector > 0) {
                    title += " Sector: " + new Sector().select(sector, conn).name;
                    if (neigh > 0) {
                        title += " Barrio: " + new Neigh().select(neigh, conn).name;
                    } else {
                        title = " Barrios: Todos";
                    }
                } else {
                    title = " Sector: Todos Barrios: Todos";
                }
            } else {
                title = " Poblado: Todas Sector: Todos Barrios: Todos";
            }
        } else {
            title = "Zona: Todos Poblados: Todas Sector: Todos Barrios: Todos";
        }
        rep.getSubTitles().add(title);
        rep.setZoomFactor(85);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
        rep.setVerticalFreeze(4);
        String str = "SELECT "
                + "GROUP_CONCAT(distinct concat(ifnull(ct.first_name,' '),' ',ifnull(ct.last_name,' '))), "//0
                + "GROUP_CONCAT(distinct ct.document), "//1
                + "GROUP_CONCAT(distinct ct.contract_num),"
                + "count(distinct ct.id) as cdor, "//2
                + "GROUP_CONCAT(distinct TRIM(CONCAT(ct.address, ' ', n.name)) SEPARATOR \"\n\"), "//3
                + "GROUP_CONCAT(distinct ct.phones), "
                + "GROUP_CONCAT(distinct c.name),"
                + "GROUP_CONCAT(distinct CAST(ct.id AS CHAR)) "
                + "FROM "
                + "contract AS ct "
                + "INNER JOIN neigh AS n ON n.id = ct.neigh_id "
                + "INNER JOIN sector AS s ON n.sector_id =s.id "
                + "INNER JOIN city AS c ON c.id = s.city_id "
                + "INNER JOIN zone AS z ON z.id=c.zone_id "
                + "WHERE ct.cancel_cause_id is null AND "
                + " ct.anull_cause_id is null ";
        if (afil == 1) {
            str += "AND ct.ctr_type ='afil' ";
        } else {
            str += "AND ct.ctr_type ='como' ";
        }

        if (zone > 0) {
            str += " AND z.id =" + zone + " ";
            if (city > 0) {
                str += " AND c.id =" + city + " ";
                if (sector > 0) {
                    str += " AND s.id =" + sector + " ";
                    if (neigh > 0) {
                        str += " AND n.id =" + neigh + " ";
                    }
                }
            }
        }

        if (type == 0) {
            str += "GROUP BY ct.document  Having count(ct.id) > 1 "
                    + "ORDER BY ct.document desc ";
        } else if (type == 1) {
            str += "GROUP BY CONCAT(ct.address, ' ', n.name) Having count(ct.id)>1 "
                    + "ORDER BY ct.address desc ";
        } else if (type == 2) {
            str += "GROUP BY ct.phones  Having count(ct.id)>1 "
                    + "ORDER BY ct.phones desc ";
        }

        MySQLQuery q = new MySQLQuery(str);
        Object[][] result = q.getRecords(conn);

        List<Column> cols = new ArrayList<>();
        List<CylinderType> types = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("") + " FROM cylinder_type ORDER BY capacity ASC"), conn);

        cols.add(new Column("Nombres", 30, 0));//0
        cols.add(new Column("Documento", 30, 0));//1
        cols.add(new Column("Contratos", 30, 0));//2
        cols.add(new Column("No Ctrs", 8, 1));//3
        cols.add(new Column("Direcciones", 50, 0));//5
        cols.add(new Column("Teléfonos", 25, 0));//4
        cols.add(new Column("Poblados", 25, 0));//4
        for (CylinderType typec : types) {
            cols.add(new Column(typec.name, 6, 1));
        }
        cols.add(new Column("Total", 8, 1));
        Table tbl = new Table("");
        tbl.setColumns(cols);

        if (result.length > 0) {
            for (int i = 0; i < result.length; i++) {
                Object[] row = new Object[7 + types.size() + 1];

                row[0] = result[i][0].toString();
                row[1] = result[i][1].toString();
                row[2] = result[i][2].toString();
                row[3] = result[i][3].toString();
                row[4] = result[i][4].toString();
                row[5] = result[i][5].toString();
                row[6] = result[i][6].toString();

                String aux = ((result[i][7]).toString());
                if (aux.substring(aux.length() - 1, aux.length()).equals(",")) {
                    aux = aux.substring(0, aux.length() - 1);
                }
                String qcyls = "SELECT "
                        + "sum(cyl.amount) "//0
                        + "FROM "
                        + "contract AS ctr "
                        + "INNER JOIN smb_ctr_cyl AS cyl ON ctr.id = cyl.contract_id "
                        + "WHERE "
                        + "ctr.id in (" + aux + ") AND "
                        + "cyl.action = 'd' AND "
                        + "cyl.type_id = ?2 AND ";
                if (afil == 1) {
                    qcyls += "ctr.ctr_type ='afil' ";
                } else {
                    qcyls += "ctr.ctr_type ='como' ";
                }
                MySQLQuery cylsQ = new MySQLQuery(qcyls);
                Long total = 0l;
                for (int j = 0; j < types.size(); j++) {
                    cylsQ.setParam(2, types.get(j).id);
                    Long c = MySQLQuery.getAsLong(cylsQ) != null ? MySQLQuery.getAsLong(cylsQ) : 0l;
                    row[7 + j] = c;
                    total += c;
                }
                row[7 + types.size()] = total;
                tbl.addRow(row);
            }
            if (!tbl.isEmpty()) {
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    public static MySQLReport getCylinderContractByEstablish(int zoneId, int city, int sector, Date begin, Date end, int afil, Connection conn) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        MySQLReport rep = new MySQLReport("Cilindros y Contratos por Establecimiento ", "Periodo " + df.format(begin) + " " + df.format(end), "cyl_cts_Est", MySQLQuery.now(conn));
        String title = "";
        Map<Integer, String> rows = new HashMap<>();
        Boolean forZones = null;
        if (city == 0 && sector == 0) {
            forZones = true;
            if (zoneId == 0) {
                List<Zone> zones = Zone.getAll(conn);
                for (Zone zone : zones) {
                    rows.put(zone.id, zone.name);
                }
                title = "Zona: Todas Sector: Todos Poblado: Todos ";
            } else {
                Zone z = Zone.findById(zoneId, conn);
                rows.put(z.id, z.name);
                title = "Zona: " + z.name + " Sector: Todos Poblado: Todos";
            }
        }

        if (zoneId > 0 && city > 0) {
            forZones = false;
            if (sector == 0) {
                List<Sector> sectors = Sector.getSectorsByCity(city, conn);
                for (Sector sector1 : sectors) {
                    rows.put(sector1.id, sector1.name);
                }
                title = "Zona: " + Zone.findById(zoneId, conn).name + " Sector: Todos Poblado: " + new MySQLQuery("SELECT name FROM city WHERE id = ?1").setParam(1, city).getAsString(conn);
            } else {
                Sector s = new Sector().select(sector, conn);
                rows.put(s.id, s.name);
                title = "Zona: " + Zone.findById(zoneId, conn).name + " Sector: " + s.name + " Poblado: " + new MySQLQuery("SELECT name FROM city WHERE id = ?1").setParam(1, city).getAsString(conn);
            }
        }

        if (forZones == null) {
            throw new Exception("Parámetros inconsistentes.");
        }

        rep.getSubTitles().add(title);
        rep.setZoomFactor(80);

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1

        Object[][] establishs = new MySQLQuery("SELECT name, id FROM establish WHERE establish_id IS NULL ORDER BY name asc").getRecords(conn);
        List<CylinderType> types = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("") + " FROM cylinder_type ORDER BY capacity ASC"), conn);

        String str = "SELECT "
                + "count(ct.id) "//0
                + "FROM "
                + "contract AS ct "
                + "INNER JOIN establish AS eSon ON ct.establish_id = eSon.id "
                + "INNER JOIN establish AS eDad ON eSon.establish_id = eDad.id ";
        if (forZones) {
            str += "INNER JOIN neigh AS n ON ct.neigh_id = n.id "
                    + "INNER JOIN sector AS s ON n.sector_id = s.id "
                    + "INNER JOIN city AS c ON s.city_id = c.id AND c.zone_id = ?1 "
                    + "WHERE ";
        } else {
            str += "INNER JOIN neigh AS n ON ct.neigh_id = n.id AND n.sector_id = ?1 "
                    + "WHERE ";
        }

        String sContracts = str + "ct.cancel_date IS NULL AND ";
        if (afil == 1) {
            sContracts += "ct.ctr_type='afil' AND ";
        } else {
            sContracts += "ct.ctr_type='como' AND ";
        }
        sContracts += "ct.anull_cause_id IS NULL AND "
                + "eDad.id = ?2 AND "
                + "ct.sign_date <= ?3 AND ct.sign_date >= ?4 "
                + "GROUP BY "
                + "eDad.id";

        String sCyls = "SELECT "
                + "sum(amount) "//0
                + "FROM "
                + "contract AS ct "
                + "INNER JOIN smb_ctr_cyl AS cyl ON cyl.contract_id = ct.id AND cyl.action = 'd' AND ct.sign_date <= ?4 AND ct.sign_date >= ?5 "
                + "INNER JOIN cylinder_type AS cylt ON cyl.type_id = cylt.id  AND cylt.id = ?1 "
                + "INNER JOIN establish AS eSon ON ct.establish_id = eSon.id "
                + "INNER JOIN establish AS eDad ON eSon.establish_id = eDad.id ";

        if (forZones) {
            sCyls += "INNER JOIN neigh AS n ON ct.neigh_id = n.id "
                    + "INNER JOIN sector AS s ON n.sector_id = s.id "
                    + "INNER JOIN city AS c ON s.city_id = c.id AND c.zone_id = ?2 "
                    + "WHERE ";
        } else {
            sCyls += "INNER JOIN neigh AS n ON ct.neigh_id = n.id AND n.sector_id = ?2 "
                    + "WHERE ";
        }

        sCyls += "ct.cancel_date IS NULL AND ";
        if (afil == 1) {
            sCyls += "ct.ctr_type='afil' AND ";
        } else {
            sCyls += "ct.ctr_type='como' AND ";
        }
        sCyls += "ct.anull_cause_id IS NULL AND "
                + "eDad.id = ?3 "
                + "GROUP BY "
                + "cylt.id";

        MySQLQuery qContract = new MySQLQuery(sContracts);
        qContract.setParam(3, end);
        qContract.setParam(4, begin);

        MySQLQuery qCyls = new MySQLQuery(sCyls);
        qCyls.setParam(4, end);
        qCyls.setParam(5, begin);

        Table mdl = new Table("");
        mdl.getColumns().add(new Column("Tipo", 12, 0));//0
        for (CylinderType type : types) {
            mdl.getColumns().add(new Column(type.name, 8, 1)); //0
        }
        mdl.getColumns().add(new Column("Total", 8, 1));//0
        mdl.getColumns().add(new Column("Contratos", 12, 1));//0

        TableHeader header = new TableHeader();
        mdl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Tipo", 1, 2));
        header.getColums().add(new HeaderColumn("Cilindros", types.size() + 1, 1));
        header.getColums().add(new HeaderColumn("Contratos", 1, 2));

        mdl.setSummaryRow(new SummaryRow("Totales", 1));
        Iterator<Entry<Integer, String>> it = rows.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, String> row = it.next();
            Table tbl = new Table(mdl);
            tbl.setTitle(row.getValue());
            for (Object[] esta : establishs) {
                List<Object> rowTable = new ArrayList<>();
                rowTable.add(MySQLQuery.getAsString(esta[0])); //tipo

                Long total = 0l;
                for (CylinderType type : types) {
                    qCyls.setParam(1, type.id);
                    qCyls.setParam(2, row.getKey());
                    qCyls.setParam(3, MySQLQuery.getAsInteger(esta[1]));
                    try {
                        rowTable.add(MySQLQuery.getAsLong(qCyls));//Cilindros
                        total += MySQLQuery.getAsLong(qCyls);
                    } catch (Exception ex) {
                        rowTable.add(0);
                        total += 0;
                    }
                }
                rowTable.add(total);//total

                qContract.setParam(1, row.getKey());
                qContract.setParam(2, MySQLQuery.getAsInteger(esta[1]));
                try {
                    rowTable.add(MySQLQuery.getAsLong(qContract));//contratos
                } catch (Exception ex) {
                    rowTable.add(0);
                }
                tbl.addRow(rowTable.toArray());
            }
            rep.getTables().add(tbl);
        }
        return rep;
    }

    //reportes diarios de siembra
    public static MySQLReport getDailyReportDiff(Date begin, Date end, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Diferencia Acumulado Diario ", "", "dif_acum", MySQLQuery.now(conn));
        rep.setZoomFactor(80);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.setVerticalFreeze(4);

        //lee los reportes diario de planillas
        String sRep = "SELECT sdr.id, city_id, sector_id, dist_id, rep_date "
                + "FROM "
                + "smb_daily_report AS sdr "
                + "WHERE "
                + "sdr.rep_date >= ?1 AND "
                + "sdr.rep_date <= ?2 ";
        MySQLQuery q = new MySQLQuery(sRep);
        q.setParam(1, Dates.trimDate(begin));
        q.setParam(2, Dates.trimDate(end));
        Object[][] result = q.getRecords(conn);

        //los datos de los barrios segun las planillas
        String dist = "SELECT ct.id, cr.amount, ct.name FROM "
                + "cylinder_type AS ct "//0
                + "LEFT JOIN smb_cyl_rep AS cr ON cr.cyl_type_id = ct.id AND cr.rep_id = ?1 "//1
                + "ORDER BY ct.capacity ASC";
        MySQLQuery qDist = new MySQLQuery(dist);

        //los datos de los sectores segun las planillas
        String sSector = "SELECT ct.id, l1.am, ct.name FROM "
                + "cylinder_type ct "//0
                + "LEFT JOIN ( "//1
                + "SELECT cyl_type_id, sum(amount) as am FROM "//2
                + "( "//3
                + "SELECT r.cyl_type_id, r.amount FROM smb_cyl_rep AS r "//4
                + "WHERE r.rep_id = ?1 "//5
                + " "//6
                + "UNION "//7
                + " "//8
                + "SELECT  cr.cyl_type_id, cr.amount FROM smb_cyl_rep AS cr "//9
                + "INNER JOIN smb_daily_report AS r ON cr.rep_id = r.id "//10
                + "INNER JOIN neigh AS n ON n.id = r.dist_id "//11
                + "WHERE n.sector_id = ?2 AND r.rep_date = ?3 ) AS l "//12
                + "GROUP BY cyl_type_id ) as l1 on ct.id = l1.cyl_type_id "//13
                + "ORDER BY ct.capacity ASC";

        MySQLQuery qSector = new MySQLQuery(sSector);

        //los datos de las ciudades segun las planillas
        String sCity = "SELECT ct.id, l1.am, ct.name FROM "
                + "cylinder_type ct "//0
                + "LEFT JOIN ( "//1
                + " "//2
                + "SELECT sum(amount) as am, cyl_type_id FROM ( "//3
                + "SELECT r.cyl_type_id, r.amount FROM smb_cyl_rep AS r "//4
                + "WHERE r.rep_id = ?1 "//5
                + " "//6
                + "UNION "//7
                + " "//8
                + "SELECT cr.cyl_type_id, cr.amount FROM smb_cyl_rep AS cr "//9
                + "INNER JOIN smb_daily_report AS r ON r.id = cr.rep_id "//10
                + "INNER JOIN sector AS s ON s.id = r.sector_id "//11
                + "WHERE s.city_id = ?2 AND r.rep_date = ?3 "//12
                + " "//13
                + "UNION "//14
                + " "//15
                + "SELECT "//16
                + "cr.cyl_type_id, cr.amount FROM "//17
                + "smb_cyl_rep AS cr "//18
                + "INNER JOIN smb_daily_report AS r ON r.id = cr.rep_id AND r.rep_date = ?3 "//19
                + "INNER JOIN neigh AS n ON n.id = r.dist_id "//20
                + "INNER JOIN sector AS s ON n.sector_id = s.id AND s.city_id = ?2 "//21
                + " "//22
                + ") AS l "//23
                + "GROUP BY cyl_type_id ) as l1 on ct.id = l1.cyl_type_id "//24
                + "ORDER BY ct.capacity ASC";

        MySQLQuery qCity = new MySQLQuery(sCity);

        //Datos de siembra de barrios
        String sowDist = "SELECT c.id, l.s, c.name FROM cylinder_type c "
                + "LEFT JOIN( "//0
                + "SELECT cylt.id, sum(cyl.amount) as s FROM contract AS ct "//1
                + "INNER JOIN smb_ctr_cyl AS cyl ON ct.id = cyl.contract_id "//2
                + "INNER JOIN cylinder_type AS cylt ON cyl.type_id = cylt.id , "//3
                + "cyl_value "//4
                + "WHERE ct.neigh_id = ?1 AND cyl.action = 'd' AND "
                + "ct.sign_date = ?2  "//5
                + "GROUP BY "//6
                + "cylt.id) as l on c.id = l.id "//7
                + "ORDER BY c.capacity ASC";
        MySQLQuery qsowDist = new MySQLQuery(sowDist);

        //Datos de siembra de sectores
        String sowSector = "SELECT c.id, l.s, c.name  FROM cylinder_type c "
                + "LEFT JOIN ( "//0
                + "SELECT cylt.id, sum(cyl.amount) AS s FROM contract AS ct "//1
                + "INNER JOIN smb_ctr_cyl AS cyl ON ct.id = cyl.contract_id "//2
                + "INNER JOIN cylinder_type AS cylt ON cyl.type_id = cylt.id "//3
                + "INNER JOIN neigh AS n ON ct.neigh_id = n.id "//4
                + "WHERE cyl.action = 'd' AND  n.sector_id = ?1 AND "//5
                + "ct.sign_date = ?2  "//5
                + "GROUP BY "//6
                + "cylt.id) as l on c.id = l.id "//7
                + "ORDER BY c.capacity ASC";
        MySQLQuery qsowSector = new MySQLQuery(sowSector);

        //Datos de siembra de ciudades
        String sowCity = "SELECT c.id, l.s, c.name FROM cylinder_type c "
                + "LEFT JOIN ( "//0
                + "SELECT cylt.id, sum(cyl.amount) AS s FROM contract AS ct "//1
                + "INNER JOIN smb_ctr_cyl AS cyl ON ct.id = cyl.contract_id "//2
                + "INNER JOIN cylinder_type AS cylt ON cyl.type_id = cylt.id "//3
                + "INNER JOIN neigh AS n ON ct.neigh_id = n.id "//4
                + "INNER JOIN sector AS s ON n.sector_id = s.id "//5
                + "WHERE cyl.action = 'd' AND s.city_id = ?1 AND "//6
                + "ct.sign_date = ?2 "//5
                + "GROUP BY "//7
                + "cylt.id) as l on c.id = l.id "//8
                + "ORDER BY c.capacity ASC";
        MySQLQuery qsowCity = new MySQLQuery(sowCity);

        String str = "SELECT "
                + "z.`name` "//0
                + "FROM "
                + "neigh AS n "
                + "INNER JOIN sector AS s ON n.sector_id = s.id "
                + "INNER JOIN city AS c ON s.city_id = c.id "
                + "INNER JOIN zone AS z ON c.zone_id = z.id "
                + "WHERE ";
        String strN = str + "n.id = ?1 limit 1 ";
        String strS = str + "s.id = ?1 limit 1 ";
        String strC = str + "c.id = ?1 limit 1 ";
        MySQLQuery qN = new MySQLQuery(strN);
        MySQLQuery qS = new MySQLQuery(strS);
        MySQLQuery qC = new MySQLQuery(strC);

        Table tbl = new Table("");
        tbl.getColumns().add(new Column("Zona", 25, 0));//0
        tbl.getColumns().add(new Column("Ubicación", 25, 0));//0
        tbl.getColumns().add(new Column("Siembra", 15, 0));//0
        tbl.getColumns().add(new Column("Planillas", 15, 0));//0
        tbl.getColumns().add(new Column("Fec Rep", 12, 1));//0
        if (result.length > 0) {
            int cDiff = 0;
            for (int i = 0; i < result.length; i++) {

                List<Object> row = new ArrayList<>();
                //datos de barrios
                if (result[i][3] != null) {
                    qDist.setParam(1, result[i][0]);
                    Object[][] rDist = qDist.getRecords(conn);

                    qsowDist.setParam(1, result[i][3]);
                    qsowDist.setParam(2, result[i][4]);
                    Object[][] rsowDist = qsowDist.getRecords(conn);
                    boolean dif = false;
                    String sow = "";
                    String planillas = "";
                    for (int j = 0; j < rsowDist.length; j++) {
                        Long vlrSow = (Long) (rsowDist[j][1] != null ? rsowDist[j][1] : 0l);
                        Integer vlrPlan = (Integer) (rDist[j][1] != null ? rDist[j][1] : 0);
                        if (vlrSow.intValue() != vlrPlan.intValue()) {
                            sow = sow + rsowDist[j][2] + " = " + (rsowDist[j][1] != null ? rsowDist[j][1] : "0") + "  \n";
                            planillas = planillas + rDist[j][2] + " = " + (rDist[j][1] != null ? rDist[j][1] : "0") + "  \n";
                            dif = true;
                        }
                    }
                    if (dif) {

                        Neigh neigh = new Neigh().select((Integer) result[i][3], conn);
                        qN.setParam(1, neigh.id);
                        Object[] res = qN.getRecord(conn);

                        for (int x = 0; x < res.length; x++) {
                            row.add(res[x]);
                        }
                        row.add(neigh.name);
                        row.add(sow);
                        row.add(planillas);
                        row.add(result[i][4]);
                        cDiff++;
                    }

                }

                //datos de sectores
                if (result[i][2] != null) {
                    qSector.setParam(1, result[i][0]);
                    qSector.setParam(2, result[i][2]);
                    qSector.setParam(3, result[i][4]);
                    Object[][] rSector = qSector.getRecords(conn);

                    qsowSector.setParam(1, result[i][2]);
                    qsowSector.setParam(2, result[i][4]);
                    Object[][] rsowSector = qsowSector.getRecords(conn);

                    boolean dif = false;
                    String sow = "";
                    String planillas = "";
                    for (int j = 0; j < rsowSector.length; j++) {

                        Long vlrSow = (Long) (rsowSector[j][1] != null ? rsowSector[j][1] : 0l);
                        BigDecimal vlrPlan = (BigDecimal) (rSector[j][1] != null ? rSector[j][1] : BigDecimal.ZERO);
                        if (vlrSow != vlrPlan.longValue()) {
                            sow = sow + rsowSector[j][2] + " = " + (rsowSector[j][1] != null ? rsowSector[j][1] : "0") + "  \n";
                            planillas = planillas + rSector[j][2] + " = " + (rSector[j][1] != null ? rSector[j][1] : "0") + "  \n";
                            dif = true;
                        }
                    }
                    if (dif) {
                        Sector sector = new Sector().select((Integer) result[i][2], conn);
                        qS.setParam(1, sector.id);
                        Object[] res = qS.getRecord(conn);

                        for (int x = 0; x < res.length; x++) {
                            row.add(res[x]);
                        }
                        row.add(sector.name);
                        row.add(sow);
                        row.add(planillas);
                        row.add(result[i][4]);
                        cDiff++;
                    }

                }

                if (result[i][1] != null) {
                    //datos de ciudades
                    qCity.setParam(1, result[i][0]);
                    qCity.setParam(2, result[i][1]);
                    qCity.setParam(3, result[i][4]);
                    Object[][] rCity = qCity.getRecords(conn);

                    qsowCity.setParam(1, result[i][1]);
                    qsowCity.setParam(2, result[i][4]);
                    Object[][] rsowCity = qsowCity.getRecords(conn);
                    boolean dif = false;
                    String sow = "";
                    String planillas = "";
                    for (int j = 0; j < rsowCity.length; j++) {
                        Long vlrSow = (Long) (rsowCity[j][1] != null ? rsowCity[j][1] : 0l);
                        BigDecimal vlrPlan = (BigDecimal) (rCity[j][1] != null ? rCity[j][1] : BigDecimal.ZERO);
                        if (vlrSow != vlrPlan.longValue()) {
                            sow = sow + rsowCity[j][2] + " = " + (rsowCity[j][1] != null ? rsowCity[j][1] : "0") + "  \n";
                            planillas = planillas + rCity[j][2] + " = " + (rCity[j][1] != null ? rCity[j][1] : "0") + "  \n";
                            dif = true;
                        }
                    }
                    if (dif) {
                        City city = City.findById((Integer) result[i][1], conn);
                        qC.setParam(1, city.id);
                        Object[] res = qC.getRecord(conn);

                        for (int x = 0; x < res.length; x++) {
                            row.add(res[x]);
                        }
                        row.add(city.name);
                        row.add(sow);
                        row.add(planillas);
                        row.add(result[i][4]);
                        cDiff++;
                    }

                }

                tbl.addRow(row.toArray());
            }
            if (cDiff > 0) {
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    public static MySQLReport getDailyReportByMonths(Date begin, Date end, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Acumulado Diario de Siembra", "", "cts", MySQLQuery.now(conn));
        rep.setHorizontalFreeze(2);
        String title = "";
        rep.getSubTitles().add(title);
        rep.setZoomFactor(80);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.LEFT, "#"));//1

        int minYear, maxYear, minMonth, maxMonth;

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(begin);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        minYear = gc.get(GregorianCalendar.YEAR);
        minMonth = gc.get(GregorianCalendar.MONTH);
        begin = Dates.trimDate(gc.getTime());

        gc.setTime(end);
        gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        maxYear = gc.get(GregorianCalendar.YEAR);
        maxMonth = gc.get(GregorianCalendar.MONTH);
        end = Dates.trimDate(gc.getTime());

        List<Date[]> months = new ArrayList<>();
        gc.setTime(begin);

        int nMonths = ((maxYear - minYear) * 12) + (maxMonth - minMonth + 1);
        do {
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            Date d1 = gc.getTime();
            gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
            Date d2 = gc.getTime();
            months.add(new Date[]{d1, d2});
            gc.add(GregorianCalendar.MONTH, 1);
        } while (months.size() < nMonths);

        MySQLQuery zonesQ = new MySQLQuery("SELECT DISTINCT * FROM ( "
                + "SELECT z.id, z.`name` FROM "//0
                + "smb_daily_report AS r "//1
                + "INNER JOIN neigh AS n ON n.id = r.dist_id "//2
                + "INNER JOIN sector AS s ON n.sector_id = s.id "//3
                + "INNER JOIN city AS c ON s.city_id = c.id "//4
                + "INNER JOIN zone AS z ON c.zone_id = z.id "//5
                + "WHERE ?1 <= r.rep_date  AND r.rep_date <= ?2 "//6
                + "UNION "//8
                + "SELECT z.id, z.`name` FROM "//10
                + "smb_daily_report AS r "//11
                + "INNER JOIN sector AS s ON s.id = r.sector_id "//12
                + "INNER JOIN city AS c ON s.city_id = c.id "//13
                + "INNER JOIN zone AS z ON c.zone_id = z.id "//14
                + "WHERE ?1 <= r.rep_date  AND r.rep_date <= ?2 "//15
                + "UNION "//17
                + "SELECT z.id, z.`name` FROM "//19
                + "smb_daily_report AS r "//20
                + "INNER JOIN city AS c ON c.id = r.city_id "//21
                + "INNER JOIN zone AS z ON c.zone_id = z.id "//22
                + "WHERE ?1 <= r.rep_date  AND r.rep_date <= ?2 "//23
                + ") AS l");

        MySQLQuery locatQ = new MySQLQuery("SELECT l.id, l.`name`, l.t FROM ( "
                + " "//0
                + "SELECT DISTINCT n.id, n.`name`, 'd' AS t FROM smb_daily_report AS r "//1
                + "INNER JOIN neigh AS n ON n.id = r.dist_id "//2
                + "INNER JOIN sector AS s ON n.sector_id = s.id "//3
                + "INNER JOIN city AS c ON s.city_id = c.id "//4
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2 AND c.zone_id = ?3 "//5
                + "UNION "//7
                + "SELECT DISTINCT s.id, s.`name`, 's' AS t FROM smb_daily_report AS r "//9
                + "INNER JOIN sector AS s ON s.id = r.sector_id "//10
                + "INNER JOIN city AS c ON s.city_id = c.id "//11
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2 AND c.zone_id = ?3 "//12
                + "UNION "//14
                + "SELECT DISTINCT c.id, c.`name`, 'c' AS t FROM smb_daily_report AS r "//16
                + "INNER JOIN city AS c ON c.id = r.city_id "//17
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2 AND c.zone_id = ?3 "//18
                + ") AS l ORDER BY l.`name`");

        //historicos por meses
        MySQLQuery distQH = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.dist_id = ?3 ");

        MySQLQuery sectQH = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.sector_id = ?3 ");

        MySQLQuery cityQH = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.city_id = ?3 ");

        //historicos totales por meses
        MySQLQuery distQHT = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.dist_id = ?3 ");

        MySQLQuery sectQHT = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.sector_id = ?3 ");

        MySQLQuery cityQHT = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.city_id = ?3 ");

        zonesQ.setParam(1, begin);
        zonesQ.setParam(2, end);

        locatQ.setParam(1, begin);
        locatQ.setParam(2, end);

        Object[][] zonesRes = zonesQ.getRecords(conn);

        SimpleDateFormat df = new SimpleDateFormat("MMM'\n'yyyy");

        for (Object[] zonesRow : zonesRes) {
            locatQ.setParam(3, zonesRow[0]);
            Object[][] locRes = locatQ.getRecords(conn);

            Table tbl = new Table(zonesRow[1].toString());
            tbl.getColumns().add(new Column("", 25, 0));//0
            TableHeader th = new TableHeader();
            th.getColums().add(new HeaderColumn(zonesRow[1].toString(), 1, 2));

            for (int j = 0; j < months.size(); j++) {
                Date[] month = months.get(j);
                th.getColums().add(new HeaderColumn(df.format(month[0]), 1, 2));
                tbl.getColumns().add(new Column("Total", 10, 1));
            }

            th.getColums().add(new HeaderColumn("Acumul.\nPlanillas", 1, 2));
            tbl.getColumns().add(new Column("Acumul.\nPlanillas", 12, 1));

            tbl.getHeaders().add(th);
            tbl.setSummaryRow(new SummaryRow("Total", 1));
            for (Object[] locRow : locRes) {
                List<Object> row = new ArrayList<>();
                row.add(locRow[1]);

                MySQLQuery cylQH = null;
                MySQLQuery cylQHT = null;

                if (locRow[2].toString().equals("d")) {
                    cylQH = distQH;
                    cylQHT = distQHT;
                } else if (locRow[2].toString().equals("s")) {

                    cylQH = sectQH;
                    cylQHT = sectQHT;
                } else if (locRow[2].toString().equals("c")) {

                    cylQH = cityQH;
                    cylQHT = cityQHT;
                }

                if (cylQH != null && cylQHT != null) {
                    cylQH.setParam(3, locRow[0]);
                    cylQHT.setParam(3, locRow[0]);

                    for (int k = 0; k < months.size(); k++) {
                        cylQH.setParam(1, months.get(k)[0]);
                        cylQH.setParam(2, months.get(k)[1]);
                        row.add(cylQH.getAsInteger(conn));
                    }
                    row.add(cylQHT.getAsInteger(conn));
                }
                tbl.addRow(row.toArray());
            }
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getDailyReport(Date date, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Acumulado Diario de Siembra", "", "cts", MySQLQuery.now(conn));
        rep.setHorizontalFreeze(2);
        String title = "";
        rep.getSubTitles().add(title);
        rep.setZoomFactor(80);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.LEFT, "#"));//1

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        Date begin = gc.getTime();
        List<CylinderType> types = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("") + " FROM cylinder_type ORDER BY capacity ASC"), conn);

        int maxDay = gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

        gc.set(GregorianCalendar.DAY_OF_MONTH, maxDay);
        Date end = gc.getTime();

        Date[][] months = new Date[6][2];
        for (int i = 0; i < months.length; i++) {
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            gc.add(GregorianCalendar.MONTH, - 1);
            months[months.length - 1 - i][0] = gc.getTime();
            gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
            months[months.length - 1 - i][1] = gc.getTime();
        }

        MySQLQuery maxRegQ = new MySQLQuery("SELECT MAX(EXTRACT(DAY FROM r.rep_date)) FROM smb_daily_report AS r "
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//0
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2");

        maxRegQ.setParam(1, begin);
        maxRegQ.setParam(2, end);

        int maxRegDay;
        try {
            maxRegDay = (maxRegQ.getAsLong(conn)).intValue();
        } catch (NullPointerException ex) {
            throw new Exception("No se hallaron datos");
        }

        MySQLQuery zonesQ = new MySQLQuery("SELECT DISTINCT * FROM ( "
                + "SELECT z.id, z.`name` FROM "//0
                + "smb_daily_report AS r "//1
                + "INNER JOIN neigh AS n ON n.id = r.dist_id "//2
                + "INNER JOIN sector AS s ON n.sector_id = s.id "//3
                + "INNER JOIN city AS c ON s.city_id = c.id "//4
                + "INNER JOIN zone AS z ON c.zone_id = z.id "//5
                + "WHERE ?1 <= r.rep_date  AND r.rep_date <= ?2 "//6
                + "UNION "//8
                + "SELECT z.id, z.`name` FROM "//10
                + "smb_daily_report AS r "//11
                + "INNER JOIN sector AS s ON s.id = r.sector_id "//12
                + "INNER JOIN city AS c ON s.city_id = c.id "//13
                + "INNER JOIN zone AS z ON c.zone_id = z.id "//14
                + "WHERE ?1 <= r.rep_date  AND r.rep_date <= ?2 "//15
                + "UNION "//17
                + "SELECT z.id, z.`name` FROM "//19
                + "smb_daily_report AS r "//20
                + "INNER JOIN city AS c ON c.id = r.city_id "//21
                + "INNER JOIN zone AS z ON c.zone_id = z.id "//22
                + "WHERE ?1 <= r.rep_date  AND r.rep_date <= ?2 "//23
                + ") AS l");

        MySQLQuery locatQ = new MySQLQuery("SELECT l.id, l.`name`, l.t FROM ( "
                + " "//0
                + "SELECT DISTINCT n.id, n.`name`, 'd' AS t FROM smb_daily_report AS r "//1
                + "INNER JOIN neigh AS n ON n.id = r.dist_id "//2
                + "INNER JOIN sector AS s ON n.sector_id = s.id "//3
                + "INNER JOIN city AS c ON s.city_id = c.id "//4
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2 AND c.zone_id = ?3 "//5
                + "UNION "//7
                + "SELECT DISTINCT s.id, s.`name`, 's' AS t FROM smb_daily_report AS r "//9
                + "INNER JOIN sector AS s ON s.id = r.sector_id "//10
                + "INNER JOIN city AS c ON s.city_id = c.id "//11
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2 AND c.zone_id = ?3 "//12
                + "UNION "//14
                + "SELECT DISTINCT c.id, c.`name`, 'c' AS t FROM smb_daily_report AS r "//16
                + "INNER JOIN city AS c ON c.id = r.city_id "//17
                + "WHERE ?1 <= r.rep_date AND r.rep_date <= ?2 AND c.zone_id = ?3 "//18
                + ") AS l ORDER BY l.`name`");

        MySQLQuery distQ = new MySQLQuery("SELECT l.am FROM  cylinder_type AS ct LEFT JOIN "
                + "(SELECT cr.cyl_type_id, SUM(cr.amount) AS am FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.dist_id = ?3 "//2
                + "GROUP BY cr.cyl_type_id) AS l ON l.cyl_type_id = ct.id ORDER BY ct.capacity ASC");

        MySQLQuery sectQ = new MySQLQuery("SELECT l.am FROM  cylinder_type AS ct LEFT JOIN "
                + "(SELECT cr.cyl_type_id, SUM(cr.amount) AS am FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.sector_id = ?3 "//2
                + "GROUP BY cr.cyl_type_id) AS l ON l.cyl_type_id = ct.id ORDER BY ct.capacity ASC");

        MySQLQuery cityQ = new MySQLQuery("SELECT l.am FROM  cylinder_type AS ct LEFT JOIN "
                + "(SELECT cr.cyl_type_id, SUM(cr.amount) AS am FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.city_id = ?3 "//2
                + "GROUP BY cr.cyl_type_id) AS l ON l.cyl_type_id = ct.id ORDER BY ct.capacity ASC");

        //historicos por meses
        MySQLQuery distQH = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.dist_id = ?3 ");

        MySQLQuery sectQH = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.sector_id = ?3 ");

        MySQLQuery cityQH = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.rep_date <= ?2 AND r.rep_date >= ?1 AND r.city_id = ?3 ");

        //historicos totales por meses
        MySQLQuery distQHT = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.dist_id = ?3 ");

        MySQLQuery sectQHT = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.sector_id = ?3 ");

        MySQLQuery cityQHT = new MySQLQuery("SELECT SUM(cr.amount) FROM smb_daily_report AS r "//0
                + "INNER JOIN smb_cyl_rep AS cr ON cr.rep_id = r.id "//1
                + "WHERE r.city_id = ?3 ");

        zonesQ.setParam(1, months[0][0]);
        zonesQ.setParam(2, end);

        locatQ.setParam(1, months[0][0]);
        locatQ.setParam(2, end);

        Object[][] zonesRes = zonesQ.getRecords(conn);

        SimpleDateFormat df = new SimpleDateFormat("MMM'\n'yyyy");

        for (Object[] zonesRow : zonesRes) {
            locatQ.setParam(3, zonesRow[0]);
            Object[][] locRes = locatQ.getRecords(conn);

            Table tbl = new Table(zonesRow[1].toString());
            tbl.getColumns().add(new Column("", 25, 0));//0
            TableHeader th = new TableHeader();
            th.getColums().add(new HeaderColumn(zonesRow[1].toString(), 1, 2));

            for (int j = 0; j < months.length; j++) {
                Date[] month = months[j];
                th.getColums().add(new HeaderColumn(df.format(month[0]), 1, 2));
                tbl.getColumns().add(new Column("Total", 10, 1));
            }

            for (int j = 1; j <= maxRegDay; j++) {
                th.getColums().add(new HeaderColumn(j + "", types.size(), 1));
                for (int k = 0; k < types.size(); k++) {
                    tbl.getColumns().add(new Column(types.get(k).name + "", 5, 1));//0
                }
            }

            th.getColums().add(new HeaderColumn("Total\nMes", 1, 2));
            tbl.getColumns().add(new Column("Total\nMes", 12, 1));

            th.getColums().add(new HeaderColumn("Proyec.\nMes", 1, 2));
            tbl.getColumns().add(new Column("Proyec.\nMes", 12, 1));

            th.getColums().add(new HeaderColumn("Acumul.\nPlanillas", 1, 2));
            tbl.getColumns().add(new Column("Acumul.\nPlanillas", 12, 1));

            tbl.getHeaders().add(th);
            tbl.setSummaryRow(new SummaryRow("Total", 1));

            for (Object[] locRow : locRes) {
                List<Object> row = new ArrayList<>();
                row.add(locRow[1]);

                MySQLQuery cylQ = null;
                MySQLQuery cylQH = null;
                MySQLQuery cylQHT = null;

                if (locRow[2].toString().equals("d")) {
                    cylQ = distQ;
                    cylQH = distQH;
                    cylQHT = distQHT;
                } else if (locRow[2].toString().equals("s")) {
                    cylQ = sectQ;
                    cylQH = sectQH;
                    cylQHT = sectQHT;
                } else if (locRow[2].toString().equals("c")) {
                    cylQ = cityQ;
                    cylQH = cityQH;
                    cylQHT = cityQHT;
                }
                cylQ.setParam(3, locRow[0]);
                cylQH.setParam(3, locRow[0]);
                cylQHT.setParam(3, locRow[0]);

                for (int k = 0; k < months.length; k++) {
                    cylQH.setParam(1, months[k][0]);
                    cylQH.setParam(2, months[k][1]);
                    row.add(cylQH.getAsInteger(conn));
                }

                for (int k = 1; k <= maxRegDay; k++) {
                    GregorianCalendar gcd = new GregorianCalendar();
                    gcd.setTime(date);
                    gcd.set(GregorianCalendar.DAY_OF_MONTH, k);
                    Date day = Dates.trimDate(gcd.getTime());
                    cylQ.setParam(1, day);
                    cylQ.setParam(2, day);
                    Object[][] cylRes = cylQ.getRecords(conn);
                    for (int l = 0; l < cylRes.length; l++) {
                        row.add(MySQLQuery.getAsInteger(cylRes[l][0]));
                    }
                }

                cylQ.setParam(1, begin);
                cylQ.setParam(2, end);
                //BigDecimal
                Object[][] cylRes = cylQ.getRecords(conn);
                int total = 0;
                for (int l = 0; l < cylRes.length; l++) {
                    total += (cylRes[l][0] != null ? MySQLQuery.getAsInteger(cylRes[l][0]) : 0);
                }
                row.add(total);
                row.add((int) (((double) total / (double) maxRegDay) * ((double) maxDay)));
                row.add(cylQHT.getAsInteger(conn));

                tbl.addRow(row.toArray());
            }
            rep.getTables().add(tbl);
        }
        return rep;
    }

    //mix
    private static boolean checkAdd(int[] remCols, int ind) {
        if (remCols != null) {
            for (int row : remCols) {
                if (row == ind) {
                    return false;
                }
            }
        }
        return true;
    }

}
