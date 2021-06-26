/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventFRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Maria
 */
public class CoreF {
    
    
    public List<CoreElementF> elements = new ArrayList<CoreElementF>();
    public List<String> arrayEl = new ArrayList<String>();
    public float Begin;
    public float End;    
    public String type;
    public String prov;
    
    @Override
    public String toString(){
        return "ArrayEl: " + arrayEl.toString() + "\nProv: " + prov + "\nType: " + type  + "\nBegin: " + Begin +  "\nEnd: " + End + "\n";
    }
    
}
