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
 * @author Mario
 */
@Entity
@Table(name = "ind_plan")
@NamedQueries({
    @NamedQuery(name = "IndPlan.findAll", query = "SELECT i FROM IndPlan i")})
public class IndPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "span_id")
    private int spanId;
    @Basic(optional = false)
    @Column(name = "programs_score_admin")
    private int programsScoreAdmin;
    @Basic(optional = false)
    @Column(name = "programs_score_super")
    private int programsScoreSuper;

    public IndPlan() {
    }

    public IndPlan(Integer id) {
        this.id = id;
    }

    public IndPlan(Integer id, int spanId, int programsScoreAdmin, int programsScoreSuper) {
        this.id = id;
        this.spanId = spanId;
        this.programsScoreAdmin = programsScoreAdmin;
        this.programsScoreSuper = programsScoreSuper;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getSpanId() {
        return spanId;
    }

    public void setSpanId(int spanId) {
        this.spanId = spanId;
    }

    public int getProgramsScoreAdmin() {
        return programsScoreAdmin;
    }

    public void setProgramsScoreAdmin(int programsScoreAdmin) {
        this.programsScoreAdmin = programsScoreAdmin;
    }

    public int getProgramsScoreSuper() {
        return programsScoreSuper;
    }

    public void setProgramsScoreSuper(int programsScoreSuper) {
        this.programsScoreSuper = programsScoreSuper;
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
        if (!(object instanceof IndPlan)) {
            return false;
        }
        IndPlan other = (IndPlan) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndPlan[id=" + id + "]";
    }

}
