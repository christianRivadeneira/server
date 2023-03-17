/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.bill.model;

import java.math.BigDecimal;

/**
 *
 * @author alder
 */
public class CreateBillClientRequest {

    public BillClientTank client;
    public BigDecimal initialReading;
    public String meterNum;

}
