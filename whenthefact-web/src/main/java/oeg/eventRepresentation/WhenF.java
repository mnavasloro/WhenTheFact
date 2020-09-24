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
public class WhenF {
    
    
    public int year;
    public int month;
    public int day;
    public String value;
    public String anchor;
    public float begin;
    public float end;
    
    @Override
    public String toString(){
        return "year: " + year + "\nType: " + month + "\nmonth: " + month +  "\nday: " + day +  "\nvalue: " + value +  "\nanchor: " + anchor +  "\nBegin: " + begin +  "\nEnd: " + end + "\n";
    }
    
}
