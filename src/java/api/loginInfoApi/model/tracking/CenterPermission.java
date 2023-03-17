package api.loginInfoApi.model.tracking;

import api.inv.model.InvProfCfg;
import api.sys.model.Profile;
import java.sql.Connection;
import utilities.MySQLQuery;

public class CenterPermission {

    public boolean nif;
    public boolean outFactory;
    public boolean qualityTest;
    public int centerId;
    public String nameCenter;
    public int calTestGoals;

    public CenterPermission() {
    }

    public CenterPermission(Object[] row) {
        centerId = (row[0] != null ? MySQLQuery.getAsInteger(row[0]) : -1);
        nameCenter = (row[1] != null ? MySQLQuery.getAsString(row[1]) : "Desconocido");
        calTestGoals = (row[2] != null ? MySQLQuery.getAsInteger(row[2]) : 0);
    }

    public static CenterPermission[] getByEmployeeId(int id, Connection conn) throws Exception {
        Profile[] profs = Profile.getProfiles(new MySQLQuery(Profile.getProfilesMobileByEmployeeQuery(id, InvProfCfg.MODULE_ID)).getRecords(conn));
        Object[][] data = null;
        if (id == 1) {
            data = new MySQLQuery("SELECT id, name, cal_test_goals FROM inv_center where active=1").getRecords(conn);
        } else {
            data = new MySQLQuery("SELECT ec.center_id, c.name, c.cal_test_goals FROM inv_emp_center ec INNER JOIN inv_center c ON ec.center_id=c.id WHERE ec.employee_id = " + id + " and c.active=1").getRecords(conn);
        }

        boolean appNif = false;
        boolean appOutFactory = false;
        boolean appQualityTest = false;

        if (id == 1) {
            appNif = true;
            appOutFactory = true;
            appQualityTest = true;
        } else {
            if (profs.length == 0) {
                throw new Exception("No tiene autorización a ningún perfíl. Debe comunicarse con el encargado de sistemas.");
            }

            for (int i = 0; i < profs.length; i++) {
                Object[][] dataProf = new MySQLQuery(InvProfCfg.getFromProfileQuery(profs[i].id)).getRecords(conn);
                if ((dataProf == null || dataProf.length == 0) || dataProf[0].length == 0) {
                    throw new Exception("No se han configurado los permisos\nComuniquese con el administrador");
                }
                InvProfCfg profCfg = InvProfCfg.getFromRow(dataProf[0]);

                if (profCfg.appNif) {
                    appNif = true;
                }
                if (profCfg.appOutFactory) {
                    appOutFactory = true;
                }
                if (profCfg.appQualityTest) {
                    appQualityTest = true;
                }
            }
        }

        CenterPermission[] rta = new CenterPermission[data.length];
        for (int i = 0; i < rta.length; i++) {
            rta[i] = new CenterPermission(data[i]);
            rta[i].nif = appNif;
            rta[i].outFactory = appOutFactory;
            rta[i].qualityTest = appQualityTest;
        }
        return rta;
    }

}
