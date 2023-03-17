package api.rpt.model;

import java.sql.Connection;
import java.util.List;

public class CubeInfo {

    public RptCube cube;
    public List<RptCubeTbl> cubeTbls;
    public List<RptCubeFld> cubeFlds;
    public List<RptCubeCond> cubeConds;

    public CubeInfo() {

    }

    public CubeInfo(int cubeId, Connection ep) throws Exception {
        cube = new RptCube().select(cubeId, ep);
        cubeTbls = RptCubeTbl.getByCubeQuery(cubeId, ep);
        cubeFlds = RptCubeFld.getByCubeQuery(cubeId, ep);
        cubeConds = RptCubeCond.getByCubeQuery(cubeId, ep);

        for (RptCubeTbl cubeTbl : cubeTbls) {
            cubeTbl.setTables(cubeTbls);
        }

        for (RptCubeFld cubeFld : cubeFlds) {
            cubeFld.setTables(cubeTbls);
        }

        for (RptCubeCond cubeCond : cubeConds) {
            cubeCond.setTables(cubeTbls);
        }
    }
}
