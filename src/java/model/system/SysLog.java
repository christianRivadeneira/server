/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.system;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "sys_log")
@NamedQueries({
    @NamedQuery(name = "SysLog.findAll", query = "SELECT s FROM SysLog s")})
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "menuId")
    private Integer menuId;
    @Lob
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "session_id")
    private int sessionId;
    @Column(name = "class")
    private String class1;
    @Basic(optional = false)
    @Column(name = "log_when")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logWhen;

    public SysLog() {
    }

    public SysLog(Integer id) {
        this.id = id;
    }

    public SysLog(Integer id, int sessionId, Date logWhen) {
        this.id = id;
        this.sessionId = sessionId;
        this.logWhen = logWhen;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getClass1() {
        return class1;
    }

    public void setClass1(String class1) {
        this.class1 = class1;
    }

    public Date getLogWhen() {
        return logWhen;
    }

    public void setLogWhen(Date logWhen) {
        this.logWhen = logWhen;
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
        if (!(object instanceof SysLog)) {
            return false;
        }
        SysLog other = (SysLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.system.SysLog[id=" + id + "]";
    }

}
