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
public class School {
    public int id;
    public String name;
    public Date begin;
    public String tittle;
    public List<BigDecimal> grades;
}
