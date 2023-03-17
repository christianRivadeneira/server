/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.ordering;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "ord_tank_order")
@NamedQueries({
    @NamedQuery(name = "OrdTankOrder.findAll", query = "SELECT o FROM OrdTankOrder o")})
public class OrdTankOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "day")
    @Temporal(TemporalType.DATE)
    private Date day;
    @Basic(optional = false)
    @Column(name = "office_id")
    private int officeId;
    @Basic(optional = false)
    @Column(name = "taken_by_id")
    private int takenById;
    @Column(name = "assig_by_id")
    private Integer assigById;
    @Column(name = "confirmed_by_id")
    private Integer confirmedById;
    @Column(name = "cancelled_by")
    private Integer cancelledBy;
    @Column(name = "enterprise_id")
    private Integer enterpriseId;
    @Column(name = "tank_client_id")
    private Integer tankClientId;
    @Column(name = "cancel_cause_id")
    private Integer cancelCauseId;
    @Column(name = "justif")
    private String justif;
    @Column(name = "complain")
    private String complain;
    @Column(name = "driver_id")
    private Integer driverId;
    @Column(name = "vehicle_id")
    private Integer vehicleId;
    @Basic(optional = false)
    @Column(name = "taken_hour")
    @Temporal(TemporalType.TIME)
    private Date takenHour;
    @Column(name = "assig_hour")
    @Temporal(TemporalType.TIME)
    private Date assigHour;
    @Column(name = "confirm_hour")
    @Temporal(TemporalType.TIME)
    private Date confirmHour;
    @Column(name = "poll_id")
    private Integer pollId;
    @Basic(optional = false)
    @Column(name = "lost")
    private boolean lost;
    @Column(name = "orig_day")
    @Temporal(TemporalType.DATE)
    private Date origDay;

    public OrdTankOrder() {
    }

    public OrdTankOrder(Integer id) {
        this.id = id;
    }

    public OrdTankOrder(Integer id, Date day, int officeId, int takenById, Date takenHour, boolean lost) {
        this.id = id;
        this.day = day;
        this.officeId = officeId;
        this.takenById = takenById;
        this.takenHour = takenHour;
        this.lost = lost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getOfficeId() {
        return officeId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public int getTakenById() {
        return takenById;
    }

    public void setTakenById(int takenById) {
        this.takenById = takenById;
    }

    public Integer getAssigById() {
        return assigById;
    }

    public void setAssigById(Integer assigById) {
        this.assigById = assigById;
    }

    public Integer getConfirmedById() {
        return confirmedById;
    }

    public void setConfirmedById(Integer confirmedById) {
        this.confirmedById = confirmedById;
    }

    public Integer getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(Integer cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Integer getTankClientId() {
        return tankClientId;
    }

    public void setTankClientId(Integer tankClientId) {
        this.tankClientId = tankClientId;
    }

    public Integer getCancelCauseId() {
        return cancelCauseId;
    }

    public void setCancelCauseId(Integer cancelCauseId) {
        this.cancelCauseId = cancelCauseId;
    }

    public String getJustif() {
        return justif;
    }

    public void setJustif(String justif) {
        this.justif = justif;
    }

    public String getComplain() {
        return complain;
    }

    public void setComplain(String complain) {
        this.complain = complain;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Date getTakenHour() {
        return takenHour;
    }

    public void setTakenHour(Date takenHour) {
        this.takenHour = takenHour;
    }

    public Date getAssigHour() {
        return assigHour;
    }

    public void setAssigHour(Date assigHour) {
        this.assigHour = assigHour;
    }

    public Date getConfirmHour() {
        return confirmHour;
    }

    public void setConfirmHour(Date confirmHour) {
        this.confirmHour = confirmHour;
    }

    public Integer getPollId() {
        return pollId;
    }

    public void setPollId(Integer pollId) {
        this.pollId = pollId;
    }

    public boolean getLost() {
        return lost;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public Date getOrigDay() {
        return origDay;
    }

    public void setOrigDay(Date origDay) {
        this.origDay = origDay;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrdTankOrder)) {
            return false;
        }
        OrdTankOrder other = (OrdTankOrder) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdTankOrder[id=" + id + "]";
    }

}
