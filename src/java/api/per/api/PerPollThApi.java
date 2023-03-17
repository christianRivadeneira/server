package api.per.api;

import api.BaseAPI;
import api.per.dto.pollTh.*;
import api.per.model.PerChild;
import api.per.model.PerEmerContact;
import api.per.model.PerEmpDoc;
import api.per.model.PerEmpElemDetail;
import api.per.model.PerEmpHobby;
import api.per.model.PerHobby;
import api.per.model.PerLog;
import api.sys.model.SysFrmValue;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import api.MySQLCol;
import utilities.MySQLQuery;
import utilities.logs.LogUtils;

@Path("/perPollTh")
public class PerPollThApi extends BaseAPI {

    @GET
    @Path("/getPersonalInfo")
    public Response getPersonalInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            PersonalInfo obj = getPersonalObj(empId, con);
            String hasGlasses = new MySQLQuery("SELECT v.data FROM sys_frm_value v "
                    + "INNER JOIN sys_frm_field f ON f.id = v.field_id "
                    + "WHERE f.`key` = 'med_glasses' AND owner_id = " + empId).getAsString(con);

            String category = new MySQLQuery("SELECT v.cmp_1 FROM per_emp_doc v "
                    + "INNER JOIN per_doc_type f ON f.id = v.doc_type_id "
                    + "WHERE f.short_name = 'Militar' AND emp_id = " + empId).getAsString(con);

            obj.hasGlasses = hasGlasses;

            if (obj.gender.equals("m")) {
                obj.military = category;
            } else {
                obj.military = "No aplica";
            }

            return Response.ok(obj).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getResidencyInfo")
    public Response getResidencyInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;

            ResidencyInfo obj = getResidencyObj(empId, con);

            String houseType = new MySQLQuery("SELECT v.data FROM sys_frm_value v "
                    + "INNER JOIN sys_frm_field f ON f.id = v.field_id "
                    + "WHERE f.`key` = 'house_type' AND owner_id = " + empId).getAsString(con);

            String stratum = new MySQLQuery("SELECT v.data FROM sys_frm_value v "
                    + "INNER JOIN sys_frm_field f ON f.id = v.field_id "
                    + "WHERE f.`key` = 'stratum' AND owner_id = " + empId).getAsString(con);

            obj.houseType = houseType;
            obj.stratum = stratum;
            return Response.ok(obj).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getStudyInfo")
    public Response getStudyInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;

            String empData = new MySQLQuery("SELECT p.sc_level "
                    + "FROM per_employee p WHERE p.id = " + empId).getAsString(con);
            StudyInfo obj = new StudyInfo();
            obj.scLevel = empData;
            obj.scName = getSysValue(empId, "sc_name", con);
            obj.isStuding = getSysValue(empId, "is_studing", con);
            obj.extraTypeStudy = getSysValue(empId, "extra_study_type", con);
            obj.extraStudy = getSysValue(empId, "extra_study_name", con);
            return Response.ok(obj).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getHobbiesInfo")
    public Response getHobbiesInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;

            Object[][] hobbyData = new MySQLQuery(
                    "SELECT h.id, h.name, h.type "
                    + "FROM per_hobby h "
                    + "WHERE show_in_poll ").getRecords(con);
            List<PerHobby> lstHobbies = null;
            if (hobbyData != null && hobbyData.length > 0) {
                lstHobbies = new ArrayList<>();
                for (Object[] hobby : hobbyData) {
                    PerHobby hobbyInfo = new PerHobby();
                    hobbyInfo.id = MySQLQuery.getAsInteger(hobby[0]);
                    hobbyInfo.name = MySQLQuery.getAsString(hobby[1]);
                    hobbyInfo.type = MySQLQuery.getAsString(hobby[2]);
                    lstHobbies.add(hobbyInfo);
                }

                Object[][] hobbyEmpData = new MySQLQuery(
                        "SELECT h.id, h.name "
                        + "FROM per_emp_hobby ph "
                        + "INNER JOIN per_hobby h ON h.id = ph.hobby_id "
                        + "WHERE ph.emp_id = " + empId).getRecords(con);
                List<PerHobby> lstHobbiesEmp = null;
                if (hobbyEmpData != null && hobbyEmpData.length > 0) {
                    lstHobbiesEmp = new ArrayList<>();
                    for (Object[] hobby : hobbyEmpData) {
                        PerHobby hobbyInfo = new PerHobby();
                        hobbyInfo.id = MySQLQuery.getAsInteger(hobby[0]);
                        hobbyInfo.name = MySQLQuery.getAsString(hobby[1]);
                        lstHobbiesEmp.add(hobbyInfo);
                    }
                }

                HobbyInfo obj = new HobbyInfo();
                obj.otherSport = getSysValue(empId, "other_sport", con);
                obj.otherHobby = getSysValue(empId, "other_hobby", con);
                obj.hobbies = lstHobbies;
                obj.hobbiesEmp = lstHobbiesEmp;
                return Response.ok(obj).build();

            } else {
                throw new Exception("No se encontró información del empleado");
            }

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getPerElementInfo")
    public Response getPerElementInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;

            HashMap<Integer, String> values = new HashMap<>();

            Object[][] empData = new MySQLQuery(
                    "SELECT ed.id, d.size "
                    + "FROM per_emp_elem_detail d "
                    + "INNER JOIN per_element_detail ed ON ed.id = d.per_elem_det_id "
                    + "WHERE d.per_emp_id =  " + empId
            ).getRecords(con);

            for (Object[] empRow : empData) {
                values.put(MySQLQuery.getAsInteger(empRow[0]), MySQLQuery.getAsString(empRow[1]));
            }

            Integer elementId = new MySQLQuery("SELECT element_id "
                    + "FROM per_employee WHERE id = " + empId).getAsInteger(con);

            List<PerElementInfo> lstElems = null;
            Object[][] elemsData = new MySQLQuery("SELECT id, name FROM per_element").getRecords(con);

            if (elemsData != null && elemsData.length > 0) {
                lstElems = new ArrayList<>();
                for (Object[] row : elemsData) {
                    PerElementInfo elemRow = new PerElementInfo();
                    elemRow.id = MySQLQuery.getAsInteger(row[0]);
                    elemRow.name = MySQLQuery.getAsString(row[1]);
                    List<PerElementDetailInfo> details = null;

                    Object[][] detailData = new MySQLQuery("SELECT id, name FROM per_element_detail WHERE per_element_id = " + elemRow.id).getRecords(con);

                    if (detailData != null && detailData.length > 0) {
                        details = new ArrayList<>();
                        for (Object[] detailRow : detailData) {
                            PerElementDetailInfo detInfo = new PerElementDetailInfo();
                            detInfo.id = MySQLQuery.getAsInteger(detailRow[0]);
                            detInfo.name = MySQLQuery.getAsString(detailRow[1]);
                            detInfo.size = values.get(detInfo.id);
                            details.add(detInfo);
                        }
                    }

                    elemRow.details = details;
                    lstElems.add(elemRow);
                }
            }
            PerElementTotalInfo obj = new PerElementTotalInfo();
            obj.elements = lstElems;
            obj.elementId = elementId;
            return Response.ok(obj).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getChildInfo")
    public Response getChildInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            MySQLQuery mq = new MySQLQuery("SELECT " + PerChild.getSelFlds("") + ", id "
                    + "FROM per_child c WHERE c.employee_id = " + empId
            );
            List<PerChild> list = PerChild.getList(mq, con);
            return Response.ok(list).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getEmerContactInfo")
    public Response getEmerContactInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;

            String q = "SELECT " + PerEmerContact.getSelFlds("") + ",id FROM per_emer_contact "
                    + "WHERE per_emp_id = " + empId + " "
                    + "ORDER BY id DESC LIMIT 1";

            PerEmerContact emer = new PerEmerContact().getPerEmerContact(new MySQLQuery(q), con);
            return Response.ok(emer).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getVehicleInfo")
    public Response getVehicleInfo() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            VehicleInfo obj = new VehicleInfo();
            obj.category = getSysValue(empId, "category_lic", con);
            obj.ownerVehicle = getSysValue(empId, "owner_veh", con);
            obj.plate = getSysValue(empId, "plate_veh", con);
            return Response.ok(obj).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getPollInfo")
    public Response getPollInfo() {
        try (Connection con = getConnection()) {
            String lbl = new MySQLQuery("SELECT c.lbl_poll_th "
                    + "FROM per_cfg c WHERE c.id = 1 ").getAsString(con);
            return Response.ok(lbl).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setPersonalInfo")
    public Response setPersonalInfo(PersonalInfo obj) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;

            PersonalInfo orig = getPersonalObj(empId, con);

            MySQLQuery update = new MySQLQuery(
                    "UPDATE per_employee SET "
                    + "document = ?1, "
                    + "first_name = ?2, "
                    + "last_name = ?3, "
                    + "blood_type = ?4, "
                    + "mail = ?5, "
                    + "person_mail = ?6, "
                    + "phones = ?7, "
                    + "cel_phones = ?8, "
                    + "person_phones = ?9, "
                    + "b_date = ?10, "
                    + "b_city = ?11, "
                    + "gender = ?12, "
                    + "allergies = ?13 "
                    + "WHERE id = ?14 "
            );

            update.setParam(1, obj.document);
            update.setParam(2, obj.firstName);
            update.setParam(3, obj.lastName);
            update.setParam(4, obj.rh);
            update.setParam(5, obj.mail);
            update.setParam(6, obj.personMail);
            update.setParam(7, obj.phone);
            update.setParam(8, obj.celPhone);
            update.setParam(9, obj.personPhone);
            update.setParam(10, obj.bDate);
            update.setParam(11, obj.bCity);
            update.setParam(12, obj.gender);
            update.setParam(13, obj.allergies);
            update.setParam(14, empId);

            update.executeUpdate(con);

            HashMap<String, Integer> map = getDinamicFields(con);
            updateSysValue(empId, map.get("med_glasses"), obj.hasGlasses, con);

            Integer typeId = new MySQLQuery("SELECT id FROM per_doc_type "
                    + "WHERE short_name = 'Militar' ").getAsInteger(con);
            if (typeId != null) {
                int idUpd = new MySQLQuery("UPDATE per_emp_doc v "
                        + "INNER JOIN per_doc_type f ON f.id = v.doc_type_id "
                        + "SET v.cmp_1 = '" + obj.military + "' "
                        + "WHERE f.short_name = 'Militar' AND emp_id = " + empId).executeUpdate(con);

                if (idUpd == 0) {
                    PerEmpDoc per = new PerEmpDoc();
                    per.cmp1 = obj.military;
                    per.empId = empId;
                    per.docTypeId = typeId;
                    per.state = "na";
                    per.insert(con);
                }
            }

            String logs = getLogsPersonalInfo(orig, obj);
            if (logs != null) {
                PerLog log = new PerLog();
                log.createLogNs(empId, PerLog.PER_EMPLOYEE, logs, 1, con);
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setResidencyInfo")
    public Response setResidencyInfo(ResidencyInfo obj) {
        try {
            try (Connection con = getConnectionThPoll()) {
                int empId = getTokenThPoll().ipe;

                ResidencyInfo orig = getResidencyObj(empId, con);

                MySQLQuery update = new MySQLQuery(
                        "UPDATE per_employee SET "
                        + "cur_city = ?1, "
                        + "neigh = ?2, "
                        + "address = ?3, "
                        + "civil_status = ?4, "
                        + "couple = ?5, "
                        + "couple_phone = ?6 "
                        + "WHERE id = ?7 "
                );

                update.setParam(1, obj.curCity);
                update.setParam(2, obj.neigh);
                update.setParam(3, obj.address);
                update.setParam(4, obj.civilStatus);
                update.setParam(5, obj.coupleName);
                update.setParam(6, obj.couplePhone);
                update.setParam(7, empId);
                update.executeUpdate(con);

                HashMap<String, Integer> map = getDinamicFields(con);
                updateSysValue(empId, map.get("house_type"), obj.houseType, con);
                updateSysValue(empId, map.get("stratum"), obj.stratum, con);

                String logs = getLogsResidencyInfo(orig, obj);
                if (logs != null) {
                    PerLog log = new PerLog();
                    log.createLogNs(empId, PerLog.PER_EMPLOYEE, logs, 1, con);
                }

                return Response.ok(obj).build();
            } catch (Exception ex) {
                return createResponse(ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(PerPollThApi.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @PUT
    @Path("/setVehicleInfo")
    public Response setVehicleInfo(VehicleInfo obj) {
        try {
            try (Connection con = getConnectionThPoll()) {
                int empId = getTokenThPoll().ipe;

                HashMap<String, Integer> map = getDinamicFields(con);

                updateSysValue(empId, map.get("category_lic"), obj.category, con);
                updateSysValue(empId, map.get("owner_veh"), obj.ownerVehicle, con);
                updateSysValue(empId, map.get("plate_veh"), obj.plate, con);

                return Response.ok(obj).build();
            } catch (Exception ex) {
                return createResponse(ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(PerPollThApi.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @PUT
    @Path("/setStudyInfo")
    public Response setStudyInfo(StudyInfo obj) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            String oldScLevel = new MySQLQuery("SELECT p.sc_level "
                    + "FROM per_employee p WHERE p.id = " + empId).getAsString(con);

            new MySQLQuery("UPDATE per_employee SET sc_level = ?1 "
                    + "WHERE id = " + empId).setParam(1, obj.scLevel).executeUpdate(con);

            HashMap<String, Integer> map = getDinamicFields(con);

            updateSysValue(empId, map.get("sc_name"), obj.scName, con);
            updateSysValue(empId, map.get("is_studing"), obj.isStuding, con);
            updateSysValue(empId, map.get("extra_study_type"), obj.extraTypeStudy, con);
            updateSysValue(empId, map.get("extra_study_name"), obj.extraStudy, con);

            PerLog log = new PerLog();
            if (oldScLevel == null && obj.scLevel != null) {
                log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se removió Nivel de Escolaridad", 1, con);
            } else if (oldScLevel != null && !oldScLevel.equals(obj.scLevel)) {
                log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se editó Nivel de Escolaridad", 1, con);
            } else if (oldScLevel != null && obj.scLevel == null) {
                log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se agregó Nivel de Escolaridad", 1, con);
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setEmerContactInfo")
    public Response setEmerContactInfo(PerEmerContact obj) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            if (obj != null) {
                if (obj.id != 0) {
                    obj.update(con);
                } else {
                    obj.perEmpId = empId;
                    obj.insert(con);
                }
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setHobbiesInfo")
    public Response setHobbiesInfo(HobbyInfo obj) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            if (obj != null && obj.hobbiesId != null && obj.hobbiesId.size() > 0) {
                new MySQLQuery("DELETE FROM per_emp_hobby WHERE "
                        + "emp_id = " + empId).executeDelete(con);
                for (int i = 0; i < obj.hobbiesId.size(); i++) {
                    PerEmpHobby peh = new PerEmpHobby();
                    peh.empId = empId;
                    peh.hobbyId = obj.hobbiesId.get(i);
                    peh.insert(con);
                }
            } else {
                new MySQLQuery("DELETE FROM per_emp_hobby WHERE emp_id = " + empId).executeDelete(con);
            }

            if (obj != null) {
                HashMap<String, Integer> map = getDinamicFields(con);
                updateSysValue(empId, map.get("other_hobby"), obj.otherHobby, con);
                updateSysValue(empId, map.get("other_sport"), obj.otherSport, con);
            }

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getPerElement")
    public Response getPerElement() {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            PerElementTotalInfo obj = new PerElementTotalInfo();
            obj.jacketSize = getSysValue(empId, "jacket_size", con);
            obj.pantSize = getSysValue(empId, "pant_size", con);
            obj.shirtSize = getSysValue(empId, "shirt_size", con);
            obj.shoesSize = getSysValue(empId, "shoes_size", con);
            return Response.ok(obj).build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setPerElement")
    public Response setPerElement(PerElementTotalInfo obj) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            HashMap<String, Integer> map = getDinamicFields(con);
            updateSysValue(empId, map.get("jacket_size"), obj.jacketSize, con);
            updateSysValue(empId, map.get("shirt_size"), obj.shirtSize, con);
            updateSysValue(empId, map.get("pant_size"), obj.pantSize, con);
            updateSysValue(empId, map.get("shoes_size"), obj.shoesSize, con);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setPerElementInfo")
    public Response setPerElementInfo(PerElementTotalInfo totalInfo) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            Integer oldElementId = new MySQLQuery("SELECT element_id "
                    + "FROM per_employee WHERE id = " + empId).getAsInteger(con);
            PerLog log = new PerLog();
            if (oldElementId != null) {
                if (!oldElementId.equals(totalInfo.elementId)) {//borrar e insertar
                    new MySQLQuery("UPDATE per_employee SET element_id = " + totalInfo.elementId + " "
                            + "WHERE id = " + empId).executeUpdate(con);
                    new MySQLQuery("DELETE FROM per_emp_elem_detail "
                            + "WHERE per_emp_id = " + empId).executeDelete(con);
                    for (int i = 0; i < totalInfo.elements.size(); i++) {
                        if (totalInfo.elementId == totalInfo.elements.get(i).id) {
                            List<PerElementDetailInfo> details = totalInfo.elements.get(i).details;
                            if (details != null && details.size() > 0) {
                                for (int j = 0; j < details.size(); j++) {
                                    PerEmpElemDetail obj = new PerEmpElemDetail();
                                    obj.perEmpId = empId;
                                    obj.size = details.get(j).size;
                                    obj.perElemDetId = details.get(j).id;
                                    obj.insert(con);
                                }
                            } else {
                                break;
                            }

                        }
                    }
                    log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se cambió tipo de dotación", 1, con);
                } else if (totalInfo.elementId == null) {//borrar todo
                    new MySQLQuery("DELETE FROM per_emp_elem_detail "
                            + "WHERE per_emp_id = " + empId).executeDelete(con);
                    new MySQLQuery("UPDATE per_employee SET element_id = null "
                            + "WHERE id = " + empId).executeUpdate(con);
                    log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se removió dotación", 1, con);
                } else {//editar tallas
                    for (int i = 0; i < totalInfo.elements.size(); i++) {
                        if (totalInfo.elementId == totalInfo.elements.get(i).id) {
                            List<PerElementDetailInfo> details = totalInfo.elements.get(i).details;
                            if (details != null && details.size() > 0) {
                                for (int j = 0; j < details.size(); j++) {
                                    MySQLQuery mq = new MySQLQuery("UPDATE per_emp_elem_detail "
                                            + "SET size = ?1 WHERE "
                                            + "per_emp_id = " + empId + " AND "
                                            + "per_elem_det_id = " + details.get(j).id);
                                    mq.setParam(1, details.get(j).size);
                                    int idUpd = mq.executeUpdate(con);
                                    if (idUpd == 0) {
                                        PerEmpElemDetail obj = new PerEmpElemDetail();
                                        obj.perEmpId = empId;
                                        obj.size = details.get(j).size;
                                        obj.perElemDetId = details.get(j).id;
                                        obj.insert(con);
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se editó tallas de dotación", 1, con);
                }
            } else {//nuevo

                new MySQLQuery("UPDATE per_employee SET element_id = " + totalInfo.elementId + " "
                        + "WHERE id = " + empId).executeUpdate(con);
                for (int i = 0; i < totalInfo.elements.size(); i++) {
                    if (oldElementId == totalInfo.elements.get(i).id) {
                        List<PerElementDetailInfo> details = totalInfo.elements.get(i).details;
                        for (int j = 0; j < details.size(); j++) {
                            PerElementDetailInfo get = details.get(j);

                            PerEmpElemDetail obj = new PerEmpElemDetail();
                            obj.perEmpId = empId;
                            obj.size = get.size;
                            obj.perElemDetId = get.id;
                            obj.insert(con);
                        }
                    }
                }
                log.createLogNs(empId, PerLog.PER_EMPLOYEE, "Se agregó dotación", 1, con);
            }

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/setChildInfo")
    public Response setChildInfo(List<PerChild> children) {
        try (Connection con = getConnectionThPoll()) {
            int empId = getTokenThPoll().ipe;
            if (children != null && children.size() > 0) {
                new MySQLQuery("DELETE FROM per_child WHERE "
                        + "employee_id = " + empId).executeDelete(con);
                for (int i = 0; i < children.size(); i++) {
                    PerChild obj = children.get(i);
                    PerChild pc = new PerChild();
                    pc.employeeId = empId;
                    pc.firstName = obj.firstName;
                    pc.lastName = obj.lastName;
                    pc.gender = obj.gender;
                    pc.scLevel = obj.scLevel;
                    pc.birth = obj.birth;
                    pc.notes = obj.notes;
                    pc.insert(con);
                }
            } else {
                new MySQLQuery("DELETE FROM per_child WHERE employee_id = " + empId).executeDelete(con);
            }

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private HashMap getDinamicFields(Connection con) throws Exception {
        HashMap<String, Integer> map = new HashMap<>();
        Object[][] data = new MySQLQuery("SELECT f.key, f.id FROM sys_frm_field f WHERE "
                + "f.key IS NOT NULL AND f.type_id = 4").getRecords(con);
        for (Object[] data1 : data) {
            map.put(MySQLQuery.getAsString(data1[0]), MySQLQuery.getAsInteger(data1[1]));
        }
        return map;
    }

    private void updateSysValue(int empId, Integer fieldId, String value, Connection con) throws Exception {
        if (fieldId != null) {
            String q = "SELECT " + SysFrmValue.getSelFlds("") + " FROM sys_frm_value "
                    + "WHERE field_id = " + fieldId + " AND owner_id = " + empId;
            SysFrmValue sys = new SysFrmValue().getSysFrmValue(new MySQLQuery(q), con);
            if (sys != null) {
                if (value != null && !value.isEmpty()) {
                    sys.data = value;
                    sys.update(con);
                } else {
                    SysFrmValue.delete(sys.id, con);
                }
            } else {
                if (value != null && !value.isEmpty()) {
                    sys = new SysFrmValue();
                    sys.data = value;
                    sys.ownerId = empId;
                    sys.fieldId = fieldId;
                    sys.insert(con);
                }
            }
        } else {
            throw new Exception("No se encuentra creado el dato dinamico");
        }
    }

    private String getSysValue(int empId, String fieldName, Connection con) throws Exception {
        String value = new MySQLQuery("SELECT v.data FROM sys_frm_value v "
                + "INNER JOIN sys_frm_field f ON f.id = v.field_id "
                + "WHERE f.`key` = '" + fieldName + "' AND owner_id = " + empId).getAsString(con);
        return value;
    }

    private String getLogsPersonalInfo(PersonalInfo orig, PersonalInfo obj) throws Exception {
        if (orig != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Se editó el empleado:");
            int nov = 0;
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Nombres", orig.firstName, obj.firstName);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Apellidos", orig.lastName, obj.lastName);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "RH", orig.rh, obj.rh);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Correo Corporativo", orig.mail, obj.mail);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Correo Personal", orig.personMail, obj.personMail);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Teléfono", orig.phone, obj.phone);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Celular Coorporativo", orig.celPhone, obj.celPhone);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Celular Personal", orig.personPhone, obj.personPhone);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY, "Fecha Nacimiento", orig.bDate, obj.bDate);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Lugar Nacimiento", orig.bCity, obj.bCity);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Allergias", orig.allergies, obj.allergies);

            if (nov > 0) {
                return sb.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private String getLogsResidencyInfo(ResidencyInfo orig, ResidencyInfo obj) throws Exception {
        if (orig != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Se editó el empleado:");
            int nov = 0;

            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Ciudad Residencia", orig.curCity, obj.curCity);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Barrio Residencia", orig.neigh, obj.neigh);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Dirección", orig.address, obj.address);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Estado Civil", orig.civilStatus, obj.civilStatus);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Nombre Pareja", orig.coupleName, obj.coupleName);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Teléfono Pareja", orig.couplePhone, obj.couplePhone);

            if (nov > 0) {
                return sb.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private PersonalInfo getPersonalObj(int empId, Connection con) throws Exception {
        Object[] empData = new MySQLQuery(
                "SELECT "
                + "p.document, "
                + "p.first_name, "
                + "p.last_name, "
                + "p.blood_type, "
                + "p.mail, "
                + "p.person_mail, "
                + "p.phones, "
                + "p.cel_phones, "
                + "p.person_phones, "
                + "p.b_date, "
                + "p.b_city, "
                + "p.gender, "
                + "p.allergies "
                + "FROM per_employee p WHERE p.id = " + empId
        ).getRecord(con);
        if (empData != null) {
            PersonalInfo orig = new PersonalInfo();
            orig.document = MySQLQuery.getAsString(empData[0]);
            orig.firstName = MySQLQuery.getAsString(empData[1]);
            orig.lastName = MySQLQuery.getAsString(empData[2]);
            orig.rh = MySQLQuery.getAsString(empData[3]);
            orig.mail = MySQLQuery.getAsString(empData[4]);
            orig.personMail = MySQLQuery.getAsString(empData[5]);
            orig.phone = MySQLQuery.getAsString(empData[6]);
            orig.celPhone = MySQLQuery.getAsString(empData[7]);
            orig.personPhone = MySQLQuery.getAsString(empData[8]);
            orig.bDate = MySQLQuery.getAsDate(empData[9]);
            orig.bCity = MySQLQuery.getAsString(empData[10]);
            orig.gender = MySQLQuery.getAsString(empData[11]);
            orig.allergies = MySQLQuery.getAsString(empData[12]);
            return orig;
        } else {
            return null;
        }
    }

    private ResidencyInfo getResidencyObj(int empId, Connection con) throws Exception {
        Object[] empData = new MySQLQuery(
                "SELECT "
                + "p.cur_city, "
                + "p.neigh, "
                + "p.address, "
                + "p.civil_status, "
                + "p.couple, "
                + "p.couple_phone "
                + "FROM per_employee p WHERE p.id = " + empId
        ).getRecord(con);

        if (empData != null) {
            ResidencyInfo orig = new ResidencyInfo();
            orig.curCity = MySQLQuery.getAsString(empData[0]);
            orig.neigh = MySQLQuery.getAsString(empData[1]);
            orig.address = MySQLQuery.getAsString(empData[2]);
            orig.civilStatus = MySQLQuery.getAsString(empData[3]);
            orig.coupleName = MySQLQuery.getAsString(empData[4]);
            orig.couplePhone = MySQLQuery.getAsString(empData[5]);
            return orig;
        } else {
            return null;
        }
    }

}
