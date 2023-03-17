/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.marketing;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Coding2
 */
@Entity
@Table(name = "cylinder_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CylinderType.findAll", query = "SELECT c FROM CylinderType c"),
    @NamedQuery(name = "CylinderType.findById", query = "SELECT c FROM CylinderType c WHERE c.id = :id"),
    @NamedQuery(name = "CylinderType.findByName", query = "SELECT c FROM CylinderType c WHERE c.name = :name"),
    @NamedQuery(name = "CylinderType.findByCapacity", query = "SELECT c FROM CylinderType c WHERE c.capacity = :capacity")})
public class CylinderType implements Serializable {
   private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "name")
    private String name;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "capacity")
    private BigDecimal capacity;
    
    @Column(name = "kg")
    private Short kg;
    @Column(name = "lb")
    private Short lb;
    @Basic(optional = false)
    @NotNull
    @Column(name = "pref")
    private boolean pref;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(name = "type")
    private String type;

    public CylinderType() {
    }

    public CylinderType(Integer id) {
        this.id = id;
    }

    public CylinderType(Integer id, String name, BigDecimal capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public void setCapacity(BigDecimal capacity) {
        this.capacity = capacity;
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
        if (!(object instanceof CylinderType)) {
            return false;
        }
        CylinderType other = (CylinderType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.marketing.CylinderType[ id=" + id + " ]";
    }

    public Short getKg() {
        return kg;
    }

    public void setKg(Short kg) {
        this.kg = kg;
    }

    public Short getLb() {
        return lb;
    }

    public void setLb(Short lb) {
        this.lb = lb;
    }

    public boolean getPref() {
        return pref;
    }

    public void setPref(boolean pref) {
        this.pref = pref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }    
}
