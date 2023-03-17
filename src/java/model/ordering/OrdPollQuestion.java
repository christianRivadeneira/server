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
@Table(name = "ord_poll_question")
@NamedQueries({
    @NamedQuery(name = "OrdPollQuestion.findAll", query = "SELECT o FROM OrdPollQuestion o")})
public class OrdPollQuestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ord_poll_version_id")
    private int ordPollVersionId;
    @Basic(optional = false)
    @Column(name = "text")
    private String text;
    @Basic(optional = false)
    @Column(name = "multiple")
    private boolean multiple;
    @Basic(optional = false)
    @Column(name = "ordinal")
    private int ordinal;
    @Basic(optional = false)
    @Column(name = "mandatory")
    private boolean mandatory;
    @Basic(optional = false)
    @Column(name = "box_width")
    private int boxWidth;
    @Basic(optional = false)
    @Column(name = "show_in_sep")
    private boolean showInSep;

    public OrdPollQuestion() {
    }

    public OrdPollQuestion(Integer id) {
        this.id = id;
    }

    public OrdPollQuestion(Integer id, int ordPollVersionId, String text, boolean multiple, int ordinal, boolean mandatory, int boxWidth, boolean showInSep) {
        this.id = id;
        this.ordPollVersionId = ordPollVersionId;
        this.text = text;
        this.multiple = multiple;
        this.ordinal = ordinal;
        this.mandatory = mandatory;
        this.boxWidth = boxWidth;
        this.showInSep = showInSep;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOrdPollVersionId() {
        return ordPollVersionId;
    }

    public void setOrdPollVersionId(int ordPollVersionId) {
        this.ordPollVersionId = ordPollVersionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public int getBoxWidth() {
        return boxWidth;
    }

    public void setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
    }

    public boolean getShowInSep() {
        return showInSep;
    }

    public void setShowInSep(boolean showInSep) {
        this.showInSep = showInSep;
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
        if (!(object instanceof OrdPollQuestion)) {
            return false;
        }
        OrdPollQuestion other = (OrdPollQuestion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdPollQuestion[id=" + id + "]";
    }

}
