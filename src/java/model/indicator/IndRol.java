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
 * @author CodingHouse
 */
@Entity
@Table(name = "ind_rol")
@NamedQueries({
    @NamedQuery(name = "IndRol.findAll", query = "SELECT i FROM IndRol i"),
    @NamedQuery(name = "IndRol.findById", query = "SELECT i FROM IndRol i WHERE i.id = :id"),
    @NamedQuery(name = "IndRol.findByAdminId", query = "SELECT i FROM IndRol i WHERE i.adminId = :adminId"),
    @NamedQuery(name = "IndRol.findBySuperId", query = "SELECT i FROM IndRol i WHERE i.superId = :superId"),
    @NamedQuery(name = "IndRol.findByFromSpanId", query = "SELECT i FROM IndRol i WHERE i.fromSpanId = :fromSpanId"),
    @NamedQuery(name = "IndRol.findByAgencyId", query = "SELECT i FROM IndRol i WHERE i.agencyId = :agencyId"),
    @NamedQuery(name = "IndRol.findByPermission", query = "SELECT i FROM IndRol i WHERE i.permission = :permission"),
    @NamedQuery(name = "IndRol.findByBeginDate", query = "SELECT i FROM IndRol i WHERE i.beginDate = :beginDate"),
    @NamedQuery(name = "IndRol.findByEndDate", query = "SELECT i FROM IndRol i WHERE i.endDate = :endDate"),
    @NamedQuery(name = "IndRol.findByUntilDate", query = "SELECT i FROM IndRol i WHERE i.untilDate = :untilDate")})
public class IndRol implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "admin_id")
    private Integer adminId;
    @Column(name = "super_id")
    private Integer superId;
    @Basic(optional = false)
    @Column(name = "from_span_id")
    private int fromSpanId;
    @Basic(optional = false)
    @Column(name = "agency_id")
    private int agencyId;
    @Basic(optional = false)
    @Column(name = "permission")
    private boolean permission;
    @Column(name = "begin_date")
    @Temporal(TemporalType.DATE)
    private Date beginDate;
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Column(name = "until_date")
    @Temporal(TemporalType.DATE)
    private Date untilDate;

    public IndRol() {
    }

    public IndRol(Integer id) {
        this.id = id;
    }

    public IndRol(Integer id, int fromSpanId, int agencyId, boolean permission) {
        this.id = id;
        this.fromSpanId = fromSpanId;
        this.agencyId = agencyId;
        this.permission = permission;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getSuperId() {
        return superId;
    }

    public void setSuperId(Integer superId) {
        this.superId = superId;
    }

    public int getFromSpanId() {
        return fromSpanId;
    }

    public void setFromSpanId(int fromSpanId) {
        this.fromSpanId = fromSpanId;
    }

    public int getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(int agencyId) {
        this.agencyId = agencyId;
    }

    public boolean getPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
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
        if (!(object instanceof IndRol)) {
            return false;
        }
        IndRol other = (IndRol) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndRol[id=" + id + "]";
    }

}
