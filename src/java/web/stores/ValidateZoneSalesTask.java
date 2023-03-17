package web.stores;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Singleton
@Startup
public class ValidateZoneSalesTask {

    private static final Logger LOG = Logger.getLogger(ValidateZoneSalesTask.class.getName());
    private Connection conn;

    @PostConstruct
    public void reset() {
        try {
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(hour = "1", minute = "1", dayOfWeek = "*", timezone = "UTC-5")
    protected void programedValidationSale() {
        try {
            System.out.println("Iniciando tarea programada Validacion Ventas");
            conn = MySQLCommon.getConnection("sigmads", null);

            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd 23:59:59");

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            String begDate = dateFormat1.format(cal.getTime());
            String endDate = dateFormat2.format(cal.getTime());

            MySQLQuery q = null;
            q = new MySQLQuery("DROP TEMPORARY TABLE IF EXISTS tbl_sales_day;");
            q.executeUpdate(conn);

            q = new MySQLQuery("CREATE TEMPORARY TABLE IF NOT EXISTS tbl_sales_day AS "
                    + "(SELECT  ts.id, ts.emp_id, ts.lat, ts.lon, ts.`date` "
                    + "FROM trk_sale ts WHERE ts.date "
                    + "BETWEEN '" + begDate + "' AND '" + endDate + "');");
            q.executeUpdate(conn);

            q = new MySQLQuery(" SELECT vo.sector_id, ts.lat, ts.lon,ts.id,vo.vehicle_id, ts.`date` FROM tbl_sales_day ts "
                    + "INNER JOIN driver_vehicle dv ON dv.driver_id = ts.emp_id AND dv.`end` IS NULL "
                    + "INNER JOIN ord_vehicle_office vo ON vo.vehicle_id = dv.vehicle_id AND vo.sector_id IS NOT NULL "
                    + "ORDER BY vo.sector_id; ");

            Object[][] dataSale = q.getRecords(conn);

            List<CoorSale> coors = new ArrayList<>();
            List<Integer> sector = new ArrayList<>();

            for (Object[] item : dataSale) {
                CoorSale coorSale = new CoorSale();
                coorSale.sectorId = MySQLQuery.getAsInteger(item[0]);
                coorSale.lat = MySQLQuery.getAsDouble(item[1]);
                coorSale.lon = MySQLQuery.getAsDouble(item[2]);
                coorSale.saleId = MySQLQuery.getAsInteger(item[3]);
                coorSale.vehicleId = MySQLQuery.getAsInteger(item[4]);
                coors.add(coorSale);
            }

            Iterator<CoorSale> it = coors.iterator();
            Integer aux = -1;
            while (it.hasNext()) {
                CoorSale current = it.next();
                if (!Objects.equals(current.sectorId, aux)) {
                    sector.add(current.sectorId);
                    aux = current.sectorId;
                }
            }

            Integer[] sectors = sector.toArray(new Integer[sector.size()]);
            Object[][] dataPoly = null;

            Map<Integer, SectorPolygon> mapOfObjects = new HashMap<>();
            List<Integer> coorExists = new ArrayList<>();

            for (int j = 0; j < sectors.length; j++) {
                dataPoly = new MySQLQuery("SELECT " + sectors[j] + ", lat, lon  FROM gps_polygon WHERE owner_id = " + sectors[j] + " ;").getRecords(conn);
                if (dataPoly.length > 2) {

                    double[] xPoints = new double[dataPoly.length];
                    double[] yPoints = new double[dataPoly.length];

                    for (int i = 0; i < dataPoly.length; i++) {
                        xPoints[i] = MySQLQuery.getAsDouble(dataPoly[i][1]);
                        yPoints[i] = MySQLQuery.getAsDouble(dataPoly[i][2]);
                    }

                    Path2D.Double d = new Path2D.Double();
                    d.moveTo(xPoints[0], yPoints[0]);
                    for (int i = 1; i < dataPoly.length; i++) {
                        d.lineTo(xPoints[i], yPoints[i]);
                    }
                    d.closePath();

                    Rectangle2D enveloped = d.getBounds2D();
                    SectorPolygon sp = new SectorPolygon(d, enveloped);

                    coorExists.add(MySQLQuery.getAsInteger(dataPoly[j][0]));
                    mapOfObjects.put(MySQLQuery.getAsInteger(dataPoly[j][0]), sp);
                }
            }

            for (Object[] dataSale1 : dataSale) {
                if (coorExists.contains(dataSale1[0])) {
                    SectorPolygon sp = mapOfObjects.get(MySQLQuery.getAsInteger(dataSale1[0]));
                    if (sp.enveloped.contains(MySQLQuery.getAsDouble(dataSale1[1]), MySQLQuery.getAsDouble(dataSale1[2]))) {
                        if (!sp.polygon.contains(MySQLQuery.getAsDouble(dataSale1[1]), MySQLQuery.getAsDouble(dataSale1[2]))) {

                            new MySQLQuery("INSERT INTO com_sale_out_zone "
                                    + "(sale_id , vehicle_id, `reg_date`) VALUES ( "
                                    + " " + MySQLQuery.getAsInteger(dataSale1[3]) + ", "
                                    + " " + MySQLQuery.getAsInteger(dataSale1[4]) + ", "
                                    + " ?1) "
                            ).setParam(1, MySQLQuery.getAsString(dataSale1[5])).executeInsert(conn);

                        }
                    } else {

                        new MySQLQuery("INSERT INTO com_sale_out_zone "
                                + "(sale_id , vehicle_id, `reg_date`) VALUES ( "
                                + " " + MySQLQuery.getAsInteger(dataSale1[3]) + ", "
                                + " " + MySQLQuery.getAsInteger(dataSale1[4]) + ", "
                                + " ?1) "
                        ).setParam(1, MySQLQuery.getAsString(dataSale1[5])).executeInsert(conn);
                    }
                }
            }

            //------------------
        } catch (Exception ex) {
            Logger.getLogger(ValidateZoneSalesTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }

    public class CoorSale {

        public Integer sectorId;
        public double lat;
        public double lon;
        public Integer saleId;
        public Integer vehicleId;

        public CoorSale(int sectorId, double lat, double lon, int saleId, int vehicleId) {
            this.sectorId = sectorId;
            this.lat = lat;
            this.lon = lon;
            this.saleId = saleId;
            this.vehicleId = vehicleId;
        }

        private CoorSale() {
        }
    }

    public class SectorPolygon {

        public Path2D.Double polygon;
        public Rectangle2D enveloped;

        private SectorPolygon(Path2D.Double d, Rectangle2D enveloped) {
            this.polygon = d;
            this.enveloped = enveloped;
        }
    }
}
