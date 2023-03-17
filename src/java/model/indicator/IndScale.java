/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.indicator;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author CodingHouse
 */
@Entity
@Table(name = "ind_scale")
@NamedQueries({
    @NamedQuery(name = "IndScale.findAll", query = "SELECT i FROM IndScale i"),
    @NamedQuery(name = "IndScale.findById", query = "SELECT i FROM IndScale i WHERE i.id = :id"),
    @NamedQuery(name = "IndScale.findByPlanId", query = "SELECT i FROM IndScale i WHERE i.planId = :planId"),
    @NamedQuery(name = "IndScale.findByProgrammingId", query = "SELECT i FROM IndScale i WHERE i.programmingId = :programmingId"),
    @NamedQuery(name = "IndScale.findByIndicatorId", query = "SELECT i FROM IndScale i WHERE i.indicatorId = :indicatorId"),
    @NamedQuery(name = "IndScale.findByAdmin", query = "SELECT i FROM IndScale i WHERE i.admin = :admin"),
    @NamedQuery(name = "IndScale.findBySuper1", query = "SELECT i FROM IndScale i WHERE i.super1 = :super1")})
public class IndScale implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "plan_id")
    private Integer planId;
    @Column(name = "programming_id")
    private Integer programmingId;
    @Basic(optional = false)
    @Column(name = "indicator_id")
    private int indicatorId;
    @Column(name = "admin")
    private Boolean admin;
    @Column(name = "super")
    private Boolean super1;

    public IndScale() {
    }

    public IndScale(Integer id) {
        this.id = id;
    }

    public IndScale(Integer id, int indicatorId) {
        this.id = id;
        this.indicatorId = indicatorId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getProgrammingId() {
        return programmingId;
    }

    public void setProgrammingId(Integer programmingId) {
        this.programmingId = programmingId;
    }

    public int getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(int indicatorId) {
        this.indicatorId = indicatorId;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getSuper1() {
        return super1;
    }

    public void setSuper1(Boolean super1) {
        this.super1 = super1;
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
        if (!(object instanceof IndScale)) {
            return false;
        }
        IndScale other = (IndScale) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndScale[id=" + id + "]";
    }

}
