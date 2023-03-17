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
@Table(name = "ord_poll_version")
@NamedQueries({
    @NamedQuery(name = "OrdPollVersion.findAll", query = "SELECT o FROM OrdPollVersion o")})
public class OrdPollVersion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ord_poll_type_id")
    private int ordPollTypeId;
    @Basic(optional = false)
    @Column(name = "last")
    private boolean last;
    @Basic(optional = false)
    @Column(name = "since")
    @Temporal(TemporalType.DATE)
    private Date since;

    public OrdPollVersion() {
    }

    public OrdPollVersion(Integer id) {
        this.id = id;
    }

    public OrdPollVersion(Integer id, int ordPollTypeId, boolean last, Date since) {
        this.id = id;
        this.ordPollTypeId = ordPollTypeId;
        this.last = last;
        this.since = since;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOrdPollTypeId() {
        return ordPollTypeId;
    }

    public void setOrdPollTypeId(int ordPollTypeId) {
        this.ordPollTypeId = ordPollTypeId;
    }

    public boolean getLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
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
        if (!(object instanceof OrdPollVersion)) {
            return false;
        }
        OrdPollVersion other = (OrdPollVersion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPollVersion[id=" + id + "]";
    }

}
