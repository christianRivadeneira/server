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
 * @author developer5
 */
@Entity
@Table(name = "ord_pqr_other")
@NamedQueries({
    @NamedQuery(name = "OrdPqrOther.findAll", query = "SELECT o FROM OrdPqrOther o")})
public class OrdPqrOther implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "serial")
    private int serial;
    @Basic(optional = false)
    @NotNull
    @Column(name = "regist_date")
    @Temporal(TemporalType.DATE)
    private Date registDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "regist_hour")
    @Temporal(TemporalType.TIME)
    private Date registHour;
    @Basic(optional = false)
    @NotNull
    @Column(name = "regist_by")
    private int registBy;
    @Column(name = "index_id")
    private Integer indexId;
    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "build_id")
    private Integer buildId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "reason_id")
    private int reasonId;
    @Column(name = "anul_cause_id")
    private Integer anulCauseId;
    @Column(name = "pqr_poll_id")
    private Integer pqrPollId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "office_id")
    private int officeId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "enterprise_id")
    private int enterpriseId;
    @Column(name = "confirm_date")
    @Temporal(TemporalType.DATE)
    private Date confirmDate;
    @Column(name = "cancel_date")
    @Temporal(TemporalType.DATE)
    private Date cancelDate;
    @Column(name = "sui_transact_id")
    private Integer suiTransactId;
    @Column(name = "sui_causal_id")
    private Integer suiCausalId;
    @Column(name = "sui_rta_id")
    private Integer suiRtaId;
    @Column(name = "sui_notify_id")
    private Integer suiNotifyId;
    @Column(name = "satis_poll_id")
    private Integer satisPollId;
    @Size(max = 128)
    @Column(name = "resp_name")
    private String respName;

    public OrdPqrOther() {
    }

    public OrdPqrOther(Integer id) {
        this.id = id;
    }

    public OrdPqrOther(Integer id, int serial, Date registDate, Date registHour, int registBy, int reasonId, int officeId, int enterpriseId) {
        this.id = id;
        this.serial = serial;
        this.registDate = registDate;
        this.registHour = registHour;
        this.registBy = registBy;
        this.reasonId = reasonId;
        this.officeId = officeId;
        this.enterpriseId = enterpriseId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public Date getRegistDate() {
        return registDate;
    }

    public void setRegistDate(Date registDate) {
        this.registDate = registDate;
    }

    public Date getRegistHour() {
        return registHour;
    }

    public void setRegistHour(Date registHour) {
        this.registHour = registHour;
    }

    public int getRegistBy() {
        return registBy;
    }

    public void setRegistBy(int registBy) {
        this.registBy = registBy;
    }

    public Integer getIndexId() {
        return indexId;
    }

    public void setIndexId(Integer indexId) {
        this.indexId = indexId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getBuildId() {
        return buildId;
    }

    public void setBuildId(Integer buildId) {
        this.buildId = buildId;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
    }

    public Integer getAnulCauseId() {
        return anulCauseId;
    }

    public void setAnulCauseId(Integer anulCauseId) {
        this.anulCauseId = anulCauseId;
    }

    public Integer getPqrPollId() {
        return pqrPollId;
    }

    public void setPqrPollId(Integer pqrPollId) {
        this.pqrPollId = pqrPollId;
    }

    public int getOfficeId() {
        return officeId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public int getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(int enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Date getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(Date confirmDate) {
        this.confirmDate = confirmDate;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public Integer getSuiTransactId() {
        return suiTransactId;
    }

    public void setSuiTransactId(Integer suiTransactId) {
        this.suiTransactId = suiTransactId;
    }

    public Integer getSuiCausalId() {
        return suiCausalId;
    }

    public void setSuiCausalId(Integer suiCausalId) {
        this.suiCausalId = suiCausalId;
    }

    public Integer getSuiRtaId() {
        return suiRtaId;
    }

    public void setSuiRtaId(Integer suiRtaId) {
        this.suiRtaId = suiRtaId;
    }

    public Integer getSuiNotifyId() {
        return suiNotifyId;
    }

    public void setSuiNotifyId(Integer suiNotifyId) {
        this.suiNotifyId = suiNotifyId;
    }

    public Integer getSatisPollId() {
        return satisPollId;
    }

    public void setSatisPollId(Integer satisPollId) {
        this.satisPollId = satisPollId;
    }

    public String getRespName() {
        return respName;
    }

    public void setRespName(String respName) {
        this.respName = respName;
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
        if (!(object instanceof OrdPqrOther)) {
            return false;
        }
        OrdPqrOther other = (OrdPqrOther) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPqrOther[ id=" + id + " ]";
    }
    
}
