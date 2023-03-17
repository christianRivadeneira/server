package model.menu;

import javax.json.JsonObjectBuilder;
import utilities.JsonUtils;

public class Credential {

    private int employeeId;
    private String sessionId;
    private Integer daysLeftPasswordExpiration;
    private String token;
    private String projectNum;

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int id) {
        this.employeeId = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getDaysLeftPasswordExpiration() {
        return daysLeftPasswordExpiration;
    }

    public void setDaysLeftPasswordExpiration(Integer daysLeftPasswordExpiration) {
        this.daysLeftPasswordExpiration = daysLeftPasswordExpiration;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProjectNum() {
        return projectNum;
    }

    public void setProjectNum(String projectNum) {
        this.projectNum = projectNum;
    }

    public void toJson(JsonObjectBuilder ob) {
        JsonUtils.addInt(ob, "employeeId", getEmployeeId());
        JsonUtils.addString(ob, "sessionId", getSessionId());
        JsonUtils.addInt(ob, "daysLeftPasswordExpiration", getDaysLeftPasswordExpiration());
        JsonUtils.addString(ob, "token", getToken());
        JsonUtils.addString(ob, "projectNum", getProjectNum());
    }
}
