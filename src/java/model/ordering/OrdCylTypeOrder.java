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
@Table(name = "ord_cyl_type_order")
@NamedQueries({
    @NamedQuery(name = "OrdCylTypeOrder.findAll", query = "SELECT o FROM OrdCylTypeOrder o")})
public class OrdCylTypeOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "order_id")
    private int orderId;
    @Basic(optional = false)
    @Column(name = "cylinder_type_id")
    private int cylinderTypeId;
    @Basic(optional = false)
    @Column(name = "amount")
    private int amount;

    public OrdCylTypeOrder() {
    }

    public OrdCylTypeOrder(Integer id) {
        this.id = id;
    }

    public OrdCylTypeOrder(Integer id, int orderId, int cylinderTypeId, int amount) {
        this.id = id;
        this.orderId = orderId;
        this.cylinderTypeId = cylinderTypeId;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCylinderTypeId() {
        return cylinderTypeId;
    }

    public void setCylinderTypeId(int cylinderTypeId) {
        this.cylinderTypeId = cylinderTypeId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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
        if (!(object instanceof OrdCylTypeOrder)) {
            return false;
        }
        OrdCylTypeOrder other = (OrdCylTypeOrder) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ordering.OrdCylTypeOrder[id=" + id + "]";
    }

}
