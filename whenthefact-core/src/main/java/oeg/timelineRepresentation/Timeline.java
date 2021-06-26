/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.timelineRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author mnavas
 */
public class Timeline {
    
    public TreeMap<Integer, TreeMap<Integer, Box>> boxes = new TreeMap<Integer, TreeMap<Integer, Box>> ();
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("                    <div class=\"timeline\">\n");
//        for(Integer k : boxes.keySet()){
        for(Entry<Integer, TreeMap<Integer, Box>> entry : boxes.entrySet()){
            Integer k = entry.getKey();
            sb.append("<div class=\"timeline__group\">\n" +
"                            <span class=\"timeline__year\">" + k + "</span>\n") ;
            ArrayList<Box> arrboxes = new ArrayList<Box>(entry.getValue().values());
            for(Box b : arrboxes){
                sb.append(b.toString());
            }        
            
            sb.append("                    </div>\n");
        }
        sb.append("                    </div>\n");
        return sb.toString();
    }
}
