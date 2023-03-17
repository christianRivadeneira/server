/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Coding2
 */
@Entity
@Table(name = "ord_pqr_anul_cause")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrdPqrAnulCause.findAll", query = "SELECT o FROM OrdPqrAnulCause o"),
    @NamedQuery(name = "OrdPqrAnulCause.findById", query = "SELECT o FROM OrdPqrAnulCause o WHERE o.id = :id"),
    @NamedQuery(name = "OrdPqrAnulCause.findByType", query = "SELECT o FROM OrdPqrAnulCause o WHERE o.type = :type"),
    @NamedQuery(name = "OrdPqrAnulCause.findByDescription", query = "SELECT o FROM OrdPqrAnulCause o WHERE o.description = :description")})
public class OrdPqrAnulCause implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "description")
    private String description;

    public OrdPqrAnulCause() {
    }

    public OrdPqrAnulCause(Integer id) {
        this.id = id;
    }

    public OrdPqrAnulCause(Integer id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        if (!(object instanceof OrdPqrAnulCause)) {
            return false;
        }
        OrdPqrAnulCause other = (OrdPqrAnulCause) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPqrAnulCause[ id=" + id + " ]";
    }

}
