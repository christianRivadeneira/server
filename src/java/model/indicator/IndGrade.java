/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.indicator;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "ind_grade")
@NamedQueries({
    @NamedQuery(name = "IndGrade.findAll", query = "SELECT i FROM IndGrade i")})
public class IndGrade implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "scale_id")
    private int scaleId;
    @Basic(optional = false)
    @Column(name = "from_grade")
    private BigDecimal fromGrade;
    @Basic(optional = false)
    @Column(name = "to_grade")
    private BigDecimal toGrade;
    @Column(name = "score_admin")
    private BigDecimal scoreAdmin;
    @Column(name = "score_super")
    private BigDecimal scoreSuper;
    @Basic(optional = false)
    @Column(name = "color")
    private int color;

    public IndGrade() {
    }

    public IndGrade(Integer id) {
        this.id = id;
    }

    public IndGrade(Integer id, int scaleId, BigDecimal fromGrade, BigDecimal toGrade, int color) {
        this.id = id;
        this.scaleId = scaleId;
        this.fromGrade = fromGrade;
        this.toGrade = toGrade;
        this.color = color;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getScaleId() {
        return scaleId;
    }

    public void setScaleId(int scaleId) {
        this.scaleId = scaleId;
    }

    public BigDecimal getFromGrade() {
        return fromGrade;
    }

    public void setFromGrade(BigDecimal fromGrade) {
        this.fromGrade = fromGrade;
    }

    public BigDecimal getToGrade() {
        return toGrade;
    }

    public void setToGrade(BigDecimal toGrade) {
        this.toGrade = toGrade;
    }

    public BigDecimal getScoreAdmin() {
        return scoreAdmin;
    }

    public void setScoreAdmin(BigDecimal scoreAdmin) {
        this.scoreAdmin = scoreAdmin;
    }

    public BigDecimal getScoreSuper() {
        return scoreSuper;
    }

    public void setScoreSuper(BigDecimal scoreSuper) {
        this.scoreSuper = scoreSuper;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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
        if (!(object instanceof IndGrade)) {
            return false;
        }
        IndGrade other = (IndGrade) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndGrade[id=" + id + "]";
    }

}
