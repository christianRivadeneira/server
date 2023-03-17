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
@Table(name = "ord_vehicle_office")
@NamedQueries({
    @NamedQuery(name = "OrdVehicleOffice.findAll", query = "SELECT o FROM OrdVehicleOffice o")})
public class OrdVehicleOffice implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "vehicle_id")
    private int vehicleId;
    @Basic(optional = false)
    @Column(name = "office_id")
    private int officeId;
    @Column(name = "sector_id")
    private Integer sectorId;
    @Basic(optional = false)
    @Column(name = "radio")
    private boolean radio;
    @Basic(optional = false)
    @Column(name = "tank")
    private boolean tank;

    public OrdVehicleOffice() {
    }

    public OrdVehicleOffice(Integer id) {
        this.id = id;
    }

    public OrdVehicleOffice(Integer id, int vehicleId, int officeId, boolean radio, boolean tank) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.officeId = officeId;
        this.radio = radio;
        this.tank = tank;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getOfficeId() {
        return officeId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public Integer getSectorId() {
        return sectorId;
    }

    public void setSectorId(Integer sectorId) {
        this.sectorId = sectorId;
    }

    public boolean getRadio() {
        return radio;
    }

    public void setRadio(boolean radio) {
        this.radio = radio;
    }

    public boolean getTank() {
        return tank;
    }

    public void setTank(boolean tank) {
        this.tank = tank;
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
        if (!(object instanceof OrdVehicleOffice)) {
            return false;
        }
        OrdVehicleOffice other = (OrdVehicleOffice) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdVehicleOffice[id=" + id + "]";
    }

}
