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
@Table(name = "ord_poll")
@NamedQueries({
    @NamedQuery(name = "OrdPoll.findAll", query = "SELECT o FROM OrdPoll o")})
public class OrdPoll implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "poll_version_id")
    private int pollVersionId;
    @Basic(optional = false)
    @Column(name = "answer")
    private String answer;
    @Column(name = "notes")
    private String notes;

    public OrdPoll() {
    }

    public OrdPoll(Integer id) {
        this.id = id;
    }

    public OrdPoll(Integer id, int pollVersionId, String answer) {
        this.id = id;
        this.pollVersionId = pollVersionId;
        this.answer = answer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getPollVersionId() {
        return pollVersionId;
    }

    public void setPollVersionId(int pollVersionId) {
        this.pollVersionId = pollVersionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        if (!(object instanceof OrdPoll)) {
            return false;
        }
        OrdPoll other = (OrdPoll) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPoll[id=" + id + "]";
    }

}
