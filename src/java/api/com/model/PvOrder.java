/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.com.model;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Fabian
 */
public class PvOrder {
    public Integer id;
    public Integer storeId;
    public String store;
    public Date dateOrder;
    public String notes;
    public Integer takenId;
    public List<PvOrderInv> load;    
}
