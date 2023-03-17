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
@Table(name = "ord_poll_option")
@NamedQueries({
    @NamedQuery(name = "OrdPollOption.findAll", query = "SELECT o FROM OrdPollOption o")})
public class OrdPollOption implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "poll_question_id")
    private int pollQuestionId;
    @Basic(optional = false)
    @Column(name = "text")
    private String text;
    @Basic(optional = false)
    @Column(name = "ordinal")
    private int ordinal;
    @Basic(optional = false)
    @Column(name = "line_break")
    private boolean lineBreak;

    public OrdPollOption() {
    }

    public OrdPollOption(Integer id) {
        this.id = id;
    }

    public OrdPollOption(Integer id, int pollQuestionId, String text, int ordinal, boolean lineBreak) {
        this.id = id;
        this.pollQuestionId = pollQuestionId;
        this.text = text;
        this.ordinal = ordinal;
        this.lineBreak = lineBreak;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getPollQuestionId() {
        return pollQuestionId;
    }

    public void setPollQuestionId(int pollQuestionId) {
        this.pollQuestionId = pollQuestionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(boolean lineBreak) {
        this.lineBreak = lineBreak;
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
        if (!(object instanceof OrdPollOption)) {
            return false;
        }
        OrdPollOption other = (OrdPollOption) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPollOption[id=" + id + "]";
    }

}
