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
@Table(name = "ind_indicator")
@NamedQueries({
    @NamedQuery(name = "IndIndicator.findAll", query = "SELECT i FROM IndIndicator i")})
public class IndIndicator implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "indicator_type_id")
    private int indicatorTypeId;
    @Basic(optional = false)
    @Column(name = "indicator_area_id")
    private int indicatorAreaId;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Column(name = "short_name")
    private String shortName;
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "active")
    private boolean active;
    @Column(name = "st1")
    private String st1;
    @Column(name = "st2")
    private String st2;
    @Column(name = "st3")
    private String st3;
    @Column(name = "st4")
    private String st4;
    @Column(name = "st5")
    private String st5;
    @Column(name = "nb1")
    private Double nb1;
    @Column(name = "nb2")
    private Double nb2;
    @Column(name = "nb3")
    private Double nb3;
    @Column(name = "nb4")
    private Double nb4;
    @Column(name = "nb5")
    private Double nb5;
    @Column(name = "desc1")
    private String desc1;
    @Column(name = "desc2")
    private String desc2;
    @Column(name = "desc3")
    private String desc3;
    @Column(name = "desc4")
    private String desc4;
    @Column(name = "desc5")
    private String desc5;
    @Column(name = "bool1")
    private Boolean bool1;
    @Column(name = "bool2")
    private Boolean bool2;
    @Column(name = "bool3")
    private Boolean bool3;
    @Column(name = "bool4")
    private Boolean bool4;
    @Column(name = "bool5")
    private Boolean bool5;

    public IndIndicator() {
    }

    public IndIndicator(Integer id) {
        this.id = id;
    }

    public IndIndicator(Integer id, int indicatorTypeId, int indicatorAreaId, String name, String description, boolean active) {
        this.id = id;
        this.indicatorTypeId = indicatorTypeId;
        this.indicatorAreaId = indicatorAreaId;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIndicatorTypeId() {
        return indicatorTypeId;
    }

    public void setIndicatorTypeId(int indicatorTypeId) {
        this.indicatorTypeId = indicatorTypeId;
    }

    public int getIndicatorAreaId() {
        return indicatorAreaId;
    }

    public void setIndicatorAreaId(int indicatorAreaId) {
        this.indicatorAreaId = indicatorAreaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSt1() {
        return st1;
    }

    public void setSt1(String st1) {
        this.st1 = st1;
    }

    public String getSt2() {
        return st2;
    }

    public void setSt2(String st2) {
        this.st2 = st2;
    }

    public String getSt3() {
        return st3;
    }

    public void setSt3(String st3) {
        this.st3 = st3;
    }

    public String getSt4() {
        return st4;
    }

    public void setSt4(String st4) {
        this.st4 = st4;
    }

    public String getSt5() {
        return st5;
    }

    public void setSt5(String st5) {
        this.st5 = st5;
    }

    public Double getNb1() {
        return nb1;
    }

    public void setNb1(Double nb1) {
        this.nb1 = nb1;
    }

    public Double getNb2() {
        return nb2;
    }

    public void setNb2(Double nb2) {
        this.nb2 = nb2;
    }

    public Double getNb3() {
        return nb3;
    }

    public void setNb3(Double nb3) {
        this.nb3 = nb3;
    }

    public Double getNb4() {
        return nb4;
    }

    public void setNb4(Double nb4) {
        this.nb4 = nb4;
    }

    public Double getNb5() {
        return nb5;
    }

    public void setNb5(Double nb5) {
        this.nb5 = nb5;
    }

    public String getDesc1() {
        return desc1;
    }

    public void setDesc1(String desc1) {
        this.desc1 = desc1;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public String getDesc3() {
        return desc3;
    }

    public void setDesc3(String desc3) {
        this.desc3 = desc3;
    }

    public String getDesc4() {
        return desc4;
    }

    public void setDesc4(String desc4) {
        this.desc4 = desc4;
    }

    public String getDesc5() {
        return desc5;
    }

    public void setDesc5(String desc5) {
        this.desc5 = desc5;
    }

    public Boolean getBool1() {
        return bool1;
    }

    public void setBool1(Boolean bool1) {
        this.bool1 = bool1;
    }

    public Boolean getBool2() {
        return bool2;
    }

    public void setBool2(Boolean bool2) {
        this.bool2 = bool2;
    }

    public Boolean getBool3() {
        return bool3;
    }

    public void setBool3(Boolean bool3) {
        this.bool3 = bool3;
    }

    public Boolean getBool4() {
        return bool4;
    }

    public void setBool4(Boolean bool4) {
        this.bool4 = bool4;
    }

    public Boolean getBool5() {
        return bool5;
    }

    public void setBool5(Boolean bool5) {
        this.bool5 = bool5;
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
        if (!(object instanceof IndIndicator)) {
            return false;
        }
        IndIndicator other = (IndIndicator) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.indicator.IndIndicator[id=" + id + "]";
    }

}
