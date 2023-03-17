package api.rpt.api;

import api.BaseModel;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptRptFld;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import utilities.json.JSONDecoder;
import utilities.json.JSONEncoder;

public abstract class BaseFilter extends BaseModel<RptRptFld> {

    private RptCubeFld cFld;
    private List<Object> filtList;

    public abstract int getId();

    public void setCubeFld(RptCubeFld fld) {
        this.cFld = fld;
    }

    public RptCubeFld getCubeFld() {
        return cFld;
    }

    public List<Object> getFiltList() throws Exception {
        if (filtList == null) {
            readFiltList();
        }
        return filtList;
    }

    public void setFiltList(List<Object> filtList) {
        this.filtList = filtList;
    }

    public abstract String getFiltType();

    public abstract void setFiltType(String filtType);

    public abstract String getFiltDesc();

    public abstract void setFiltDesc(String filtDesc);

    public abstract String getBinFilt();

    public abstract void setBinFilt(String bin);

    protected void readFiltList() throws Exception {
        if (getBinFilt() != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(getBinFilt().getBytes());
            setFiltList(new JSONDecoder().getList(bais, Object.class));
        }
    }

    protected void writeFileList() throws Exception {
        if (getFiltList() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JSONEncoder.encode(getFiltList(), baos, false);
            baos.close();
            setBinFilt(new String(baos.toByteArray()));
        } else {
            setBinFilt(null);
        }
    }
}
