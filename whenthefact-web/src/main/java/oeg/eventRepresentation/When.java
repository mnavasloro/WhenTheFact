/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventRepresentation;

/**
 *
 * @author Maria
 */
public class When {
    
    
    public String year;
    public String month;
    public String day;
    public String value;
    public String begin;
    public String end;
    
    @Override
    public String toString(){
        return "year: " + year + "\nType: " + month + "\nmonth: " + month +  "\nday: " + day +  "\nvalue: " + value +  "\nBegin: " + begin +  "\nEnd: " + end + "\n";
    }
    
}
