/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.timelineRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 *
 * @author mnavas
 */
public class Timeline {
    
    public TreeMap<Integer, TreeMap<Integer, Box>> boxes = new TreeMap<Integer, TreeMap<Integer, Box>> ();
    
    @Override
    public String toString(){
        String res = "                    <div class=\"timeline\">\n" ;
        for(Integer k : boxes.keySet()){
            res = res + "<div class=\"timeline__group\">\n" +
"                            <span class=\"timeline__year\">" + k + "</span>\n" ;
            ArrayList<Box> arrboxes = new ArrayList<Box>(boxes.get(k).values());
            for(Box b : arrboxes){
                res = res + b.toString();
            }        
            
            res = res + "                    </div>\n";
        }
        return res;
    }
}
