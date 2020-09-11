/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Maria
 */
public class Who {
    
    
    public List<CoreElement> elements = new ArrayList<CoreElement>();
    public List<String> arrayEl = new ArrayList<String>();
    public List<Integer> positions = new ArrayList<Integer>();
    public float Begin;
    public float End;
    
    @Override
    public String toString(){
        return "ArrayEl: " + arrayEl.toString() + "positions:" + positions.toString() + "\nBegin: " + Begin +  "\nEnd: " + End + "\n";
    }
    
}
