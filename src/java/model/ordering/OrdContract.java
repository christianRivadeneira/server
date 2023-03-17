/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "ord_contract")
@NamedQueries({
    @NamedQuery(name = "OrdContract.findAll", query = "SELECT o FROM OrdContract o")})
public class OrdContract implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "address")
    private String address;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "phones")
    private String phones;
    @Column(name = "own")
    private Boolean own;
    @Column(name = "establish_id")
    private Integer establishId;
    @Column(name = "energy_id")
    private Integer energyId;
    @Column(name = "neigh_id")
    private Integer neighId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "people")
    private int people;
    @Column(name = "client_id")
    private Integer clientId;
    @Size(max = 512)
    @Column(name = "notes")
    private String notes;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "state")
    private String state;
    @Column(name = "closed_pending_date")
    @Temporal(TemporalType.DATE)
    private Date closedPendingDate;
    @Size(max = 1024)
    @Column(name = "closed_pending_notes")
    private String closedPendingNotes;
    @Column(name = "city_id")
    private Integer cityId;
    @Column(name = "creator_id")
    private Integer creatorId;
    @Column(name = "created_date")
    @Temporal(TemporalType.DATE)
    private Date createdDate;
    @Size(max = 5)
    @Column(name = "created_from")
    private String createdFrom;
    @Size(max = 255)
    @Column(name = "document")
    private String document;
    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;
    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;
    @Size(max = 8)
    @Column(name = "cli_type")
    private String cliType;

    public OrdContract() {
    }

    public OrdContract(Integer id) {
        this.id = id;
    }

    public OrdContract(Integer id, String address, String phones, int people, String state) {
        this.id = id;
        this.address = address;
        this.phones = phones;
        this.people = people;
        this.state = state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public Boolean getOwn() {
        return own;
    }

    public void setOwn(Boolean own) {
        this.own = own;
    }

    public Integer getEstablishId() {
        return establishId;
    }

    public void setEstablishId(Integer establishId) {
        this.establishId = establishId;
    }

    public Integer getEnergyId() {
        return energyId;
    }

    public void setEnergyId(Integer energyId) {
        this.energyId = energyId;
    }

    public Integer getNeighId() {
        return neighId;
    }

    public void setNeighId(Integer neighId) {
        this.neighId = neighId;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getClosedPendingDate() {
        return closedPendingDate;
    }

    public void setClosedPendingDate(Date closedPendingDate) {
        this.closedPendingDate = closedPendingDate;
    }

    public String getClosedPendingNotes() {
        return closedPendingNotes;
    }

    public void setClosedPendingNotes(String closedPendingNotes) {
        this.closedPendingNotes = closedPendingNotes;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(String createdFrom) {
        this.createdFrom = createdFrom;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCliType() {
        return cliType;
    }

    public void setCliType(String cliType) {
        this.cliType = cliType;
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
        if (!(object instanceof OrdContract)) {
            return false;
        }
        OrdContract other = (OrdContract) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdContract[ id=" + id + " ]";
    }
    
}
