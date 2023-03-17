/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.sys.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Danny
 */
public class Person {
    public int peso;
    public String name;
    public boolean active = true;
    public Boolean ready = null;
    public List<String> alias;
    public List<School> schools;
    public BigDecimal salary;
    public double score;
    public Date bdate;
    public boolean playFootball = true;
}
