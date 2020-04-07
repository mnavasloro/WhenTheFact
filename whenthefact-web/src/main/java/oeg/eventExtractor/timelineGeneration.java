/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventExtractor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import oeg.eventRepresentation.Event;
import oeg.timelineRepresentation.Box;
import oeg.timelineRepresentation.Timeline;

/**
 *
 * @author mnavas
 */
public class timelineGeneration {
    
    public static String generateTimeline(ArrayList<Event> events){
        String res;
        Timeline tl = new Timeline();
        
        TreeMap<Integer,ArrayList<Event>> map = new TreeMap<Integer,ArrayList<Event>>();
        
        // We look for the years
        for(Event ev : events){
            if(!ev.Core.elements.isEmpty()){ //If the event is empty, we do not care about it                
                int year = Integer.parseInt(ev.When.year);
                    ArrayList<Event> auxarr = new ArrayList<Event>();
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

        for(Integer year : map.keySet()){
            boxesaux.put(year, orderBoxes(map.get(year)));
        }
        
        // We create the html
        tl.boxes = boxesaux;  
        res = tl.toString();
        return res;
        
    }
    
    public static TreeMap<Integer, Box> orderBoxes(ArrayList<Event> evs){
        TreeMap<Integer, Box> res = new TreeMap<Integer, Box>();
        for(Event ev : evs){
            res.put(Integer.parseInt(ev.When.month)*100 + Integer.parseInt(ev.When.day), new Box(ev));
        }
        
        return res;        
    }
}
