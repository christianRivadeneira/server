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
@Table(name = "ind_span")
@NamedQueries({
    @NamedQuery(name = "IndSpan.findAll", query = "SELECT i FROM IndSpan i")})
public class IndSpan implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "span_month")
    private int spanMonth;
    @Basic(optional = false)
    @Column(name = "span_year")
    private int spanYear;

    public IndSpan() {
    }

    public IndSpan(Integer id) {
        this.id = id;
    }

    public IndSpan(Integer id, int spanMonth, int spanYear) {
        this.id = id;
        this.spanMonth = spanMonth;
        this.spanYear = spanYear;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getSpanMonth() {
        return spanMonth;
    }

    public void setSpanMonth(int spanMonth) {
        this.spanMonth = spanMonth;
    }

    public int getSpanYear() {
        return spanYear;
    }

    public void setSpanYear(int spanYear) {
        this.spanYear = spanYear;
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
        if (!(object instanceof IndSpan)) {
            return false;
        }
        IndSpan other = (IndSpan) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndSpan[id=" + id + "]";
    }

}
