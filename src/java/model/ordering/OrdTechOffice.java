/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.ordering;

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
@Table(name = "ord_tech_office")
@NamedQueries({
    @NamedQuery(name = "OrdTechOffice.findAll", query = "SELECT o FROM OrdTechOffice o")})
public class OrdTechOffice implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "office_id")
    private int officeId;
    @Basic(optional = false)
    @Column(name = "technician_id")
    private int technicianId;

    public OrdTechOffice() {
    }

    public OrdTechOffice(Integer id) {
        this.id = id;
    }

    public OrdTechOffice(Integer id, int officeId, int technicianId) {
        this.id = id;
        this.officeId = officeId;
        this.technicianId = technicianId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOfficeId() {
        return officeId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public int getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(int technicianId) {
        this.technicianId = technicianId;
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
        if (!(object instanceof OrdTechOffice)) {
            return false;
        }
        OrdTechOffice other = (OrdTechOffice) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdTechOffice[id=" + id + "]";
    }

}
