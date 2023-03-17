/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.indicator;

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
@Table(name = "ind_value")
@NamedQueries({
    @NamedQuery(name = "IndValue.findAll", query = "SELECT i FROM IndValue i")})
public class IndValue implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "employee_id")
    private int employeeId;
    @Basic(optional = false)
    @Column(name = "scale_id")
    private int scaleId;
    @Basic(optional = false)
    @Column(name = "agency_id")
    private int agencyId;
    @Basic(optional = false)
    @Column(name = "notes")
    private String notes;
    @Column(name = "v1")
    private Double v1;
    @Column(name = "v2")
    private Double v2;
    @Column(name = "v3")
    private Double v3;
    @Column(name = "v4")
    private Double v4;
    @Column(name = "v5")
    private Double v5;
    @Basic(optional = false)
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Basic(optional = false)
    @Column(name = "modif_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifDate;
    @Basic(optional = false)
    @Column(name = "creator")
    private int creator;
    @Basic(optional = false)
    @Column(name = "modifier")
    private int modifier;
    @Basic(optional = false)
    @Column(name = "when_value")
    @Temporal(TemporalType.DATE)
    private Date whenValue;

    public IndValue() {
    }

    public IndValue(Integer id) {
        this.id = id;
    }

    public IndValue(Integer id, int employeeId, int scaleId, int agencyId, String notes, Date creationDate, Date modifDate, int creator, int modifier, Date whenValue) {
        this.id = id;
        this.employeeId = employeeId;
        this.scaleId = scaleId;
        this.agencyId = agencyId;
        this.notes = notes;
        this.creationDate = creationDate;
        this.modifDate = modifDate;
        this.creator = creator;
        this.modifier = modifier;
        this.whenValue = whenValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getScaleId() {
        return scaleId;
    }

    public void setScaleId(int scaleId) {
        this.scaleId = scaleId;
    }

    public int getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(int agencyId) {
        this.agencyId = agencyId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getV1() {
        return v1;
    }

    public void setV1(Double v1) {
        this.v1 = v1;
    }

    public Double getV2() {
        return v2;
    }

    public void setV2(Double v2) {
        this.v2 = v2;
    }

    public Double getV3() {
        return v3;
    }

    public void setV3(Double v3) {
        this.v3 = v3;
    }

    public Double getV4() {
        return v4;
    }

    public void setV4(Double v4) {
        this.v4 = v4;
    }

    public Double getV5() {
        return v5;
    }

    public void setV5(Double v5) {
        this.v5 = v5;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifDate() {
        return modifDate;
    }

    public void setModifDate(Date modifDate) {
        this.modifDate = modifDate;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public Date getWhenValue() {
        return whenValue;
    }

    public void setWhenValue(Date whenValue) {
        this.whenValue = whenValue;
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
        if (!(object instanceof IndValue)) {
            return false;
        }
        IndValue other = (IndValue) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndValue[id=" + id + "]";
    }

}
