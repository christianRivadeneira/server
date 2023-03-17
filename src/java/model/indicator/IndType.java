/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.indicator;

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
@Table(name = "ind_type")
@NamedQueries({
    @NamedQuery(name = "IndType.findAll", query = "SELECT i FROM IndType i")})
public class IndType implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "user_input")
    private boolean userInput;
    @Basic(optional = false)
    @Column(name = "logic_class")
    private String logicClass;
    @Basic(optional = false)
    @Column(name = "definition_form_class")
    private String definitionFormClass;
    @Basic(optional = false)
    @Column(name = "value_form_class")
    private String valueFormClass;
    @Basic(optional = false)
    @Column(name = "scale_form_class")
    private String scaleFormClass;

    public IndType() {
    }

    public IndType(Integer id) {
        this.id = id;
    }

    public IndType(Integer id, String name, String description, boolean userInput, String logicClass, String definitionFormClass, String valueFormClass, String scaleFormClass) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userInput = userInput;
        this.logicClass = logicClass;
        this.definitionFormClass = definitionFormClass;
        this.valueFormClass = valueFormClass;
        this.scaleFormClass = scaleFormClass;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getUserInput() {
        return userInput;
    }

    public void setUserInput(boolean userInput) {
        this.userInput = userInput;
    }

    public String getLogicClass() {
        return logicClass;
    }

    public void setLogicClass(String logicClass) {
        this.logicClass = logicClass;
    }

    public String getDefinitionFormClass() {
        return definitionFormClass;
    }

    public void setDefinitionFormClass(String definitionFormClass) {
        this.definitionFormClass = definitionFormClass;
    }

    public String getValueFormClass() {
        return valueFormClass;
    }

    public void setValueFormClass(String valueFormClass) {
        this.valueFormClass = valueFormClass;
    }

    public String getScaleFormClass() {
        return scaleFormClass;
    }

    public void setScaleFormClass(String scaleFormClass) {
        this.scaleFormClass = scaleFormClass;
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
        if (!(object instanceof IndType)) {
            return false;
        }
        IndType other = (IndType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndType[id=" + id + "]";
    }

}
