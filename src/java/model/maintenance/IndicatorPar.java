/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.maintenance;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "indicator_par")
@NamedQueries({
    @NamedQuery(name = "IndicatorPar.findAll", query = "SELECT i FROM IndicatorPar i"),
    @NamedQuery(name = "IndicatorPar.findById", query = "SELECT i FROM IndicatorPar i WHERE i.id = :id"),
    @NamedQuery(name = "IndicatorPar.findByName", query = "SELECT i FROM IndicatorPar i WHERE i.name = :name"),
    @NamedQuery(name = "IndicatorPar.findByDesc1", query = "SELECT i FROM IndicatorPar i WHERE i.desc1 = :desc1"),
    @NamedQuery(name = "IndicatorPar.findByDesc2", query = "SELECT i FROM IndicatorPar i WHERE i.desc2 = :desc2"),
    @NamedQuery(name = "IndicatorPar.findByType", query = "SELECT i FROM IndicatorPar i WHERE i.type = :type"),
    @NamedQuery(name = "IndicatorPar.findByBound", query = "SELECT i FROM IndicatorPar i WHERE i.bound = :bound")})
public class IndicatorPar implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    public Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    public String name;
    @Basic(optional = false)
    @Column(name = "desc1")
    public String desc1;
    @Basic(optional = false)
    @Column(name = "desc2")
    public String desc2;
    @Basic(optional = false)
    @Column(name = "type")
    public String type;
    @Basic(optional = false)
    @Column(name = "bound")
    public int bound;
    @Basic(optional = false)
    @Lob
    @Column(name = "sql1")
    public String sql1;
    @Basic(optional = false)
    @Lob
    @Column(name = "sql2")
    public String sql2;

    public IndicatorPar() {
    }

    public IndicatorPar(Integer id) {
        this.id = id;
    }

    public IndicatorPar(Integer id, String name, String desc1, String desc2, String type, int bound, String sql1, String sql2) {
        this.id = id;
        this.name = name;
        this.desc1 = desc1;
        this.desc2 = desc2;
        this.type = type;
        this.bound = bound;
        this.sql1 = sql1;
        this.sql2 = sql2;
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
        if (!(object instanceof IndicatorPar)) {
            return false;
        }
        IndicatorPar other = (IndicatorPar) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.maintenance.IndicatorPar[id=" + id + "]";
    }

}
