/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.mto.model;

/**
 *
 * @author Old Joey
 */
public class EmployeeTripsInfoDto {
    public String type;
    public String label;
    
    public EmployeeTripsInfoDto()
    {
    }
    public EmployeeTripsInfoDto(String t, String l)
    {
        this.type = t;
        this.label = l;
    }
}
