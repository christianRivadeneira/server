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
@Table(name = "ord_poll_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrdPollType.findAll", query = "SELECT o FROM OrdPollType o"),
    @NamedQuery(name = "OrdPollType.findById", query = "SELECT o FROM OrdPollType o WHERE o.id = :id"),
    @NamedQuery(name = "OrdPollType.findByName", query = "SELECT o FROM OrdPollType o WHERE o.name = :name"),
    @NamedQuery(name = "OrdPollType.findByEditable", query = "SELECT o FROM OrdPollType o WHERE o.editable = :editable")})
public class OrdPollType implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "editable")
    private boolean editable;

    public OrdPollType() {
    }

    public OrdPollType(Integer id) {
        this.id = id;
    }

    public OrdPollType(Integer id, String name, boolean editable) {
        this.id = id;
        this.name = name;
        this.editable = editable;
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

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
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
        if (!(object instanceof OrdPollType)) {
            return false;
        }
        OrdPollType other = (OrdPollType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPollType[ id=" + id + " ]";
    }

}
