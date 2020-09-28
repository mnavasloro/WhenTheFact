/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventExtractor;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import oeg.eventRepresentation.EventF;
import oeg.timelineRepresentation.Box;
import oeg.timelineRepresentation.Timeline;

/**
 *
 * @author mnavas
 */
public class timelineGeneration {
    
    public static String generateTimeline(ArrayList<EventF> events){
        String res;
        Timeline tl = new Timeline();
        
        TreeMap<Integer,ArrayList<EventF>> map = new TreeMap<Integer,ArrayList<EventF>>();
        
        // We look for the years
        for(EventF ev : events){
            if(!ev.core.elements.isEmpty()){ //If the event is empty, we do not care about it                
                int year = ev.when.year;
                    ArrayList<EventF> auxarr = new ArrayList<EventF>();
                if(map.containsKey(year)){
                    auxarr = map.get(year);
                }
                auxarr.add(ev);
                map.put(year, auxarr);   
            }         
        }
        
        
        // Once we have them divided into and ordered by years,
        // we transform them into ordered boxes
        TreeMap<Integer, TreeMap<Integer, Box>> boxesaux = new TreeMap<Integer, TreeMap<Integer, Box>> ();

        for(Entry<Integer,ArrayList<EventF>> entry : map.entrySet()){
            boxesaux.put(entry.getKey(), orderBoxes(entry.getValue()));
        }
        
        // We create the html
        tl.boxes = boxesaux;  
        res = tl.toString();
        
        System.out.println("Timeline generation done");
        return res;
        
    }
    
    public static TreeMap<Integer, Box> orderBoxes(ArrayList<EventF> evs){
        TreeMap<Integer, Box> res = new TreeMap<Integer, Box>();
        for(EventF ev : evs){
            res.put(ev.when.month*100 + ev.when.day, new Box(ev));
        }
        
        return res;        
    }
}
