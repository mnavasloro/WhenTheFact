package oeg.eventExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.eventRepresentation.Core;
import oeg.eventRepresentation.CoreElement;
import oeg.eventRepresentation.Event;
import oeg.eventRepresentation.When;
import org.slf4j.LoggerFactory;
import org.json.simple.*;

/**
 * Class that converts a TIMEX annotation into a NIF annotation
 *
 * @author mnavas
 */
public class Annotation2JSON {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Annotation2JSON.class);
    
    /**
     * Initializes a instance of the converter
     *
     * @return an instance of the converter
     */
    public Annotation2JSON() {
        init();
    }

    public void init() {

    }

    /**
     * Converts a sentence @intput in TIMEX format into JSON
     *
     * @param input String in TIMEX format
     * @return JSON
     */
    public JSONObject translateSentenceTIMEX(String input) {
        try {
            String inp2 = input;
            
            JSONObject json = new JSONObject();

            JSONArray array = new JSONArray();
            
            while (!inp2.isEmpty()) {
                String pattern = "<TIMEX3 tid=\"([^\"]+)\" type=\"([^\"]+)\" value=\"([^\"]+)\"[^>]*>([^<]*)<\\/TIMEX3>";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(inp2);
                StringBuffer sb = new StringBuffer(inp2.length());
                if (m.find()) {
                    JSONObject item = new JSONObject();
                    int end = (m.start() + m.group(4).length());
                    item.put("beginIndex", m.start());
                    item.put("endIndex", end);
                    item.put("anchorOf", m.group(4) );
                    item.put("tid", m.group(1) );
                    item.put("type", m.group(2) );
                    item.put("value", m.group(3) );
                    
                    array.add(item);
                                       
                    m.appendReplacement(sb, m.group(4));
                    m.appendTail(sb);
                    inp2 = sb.toString();
                } 
                else {
                    break;
                }
                
                
            }
//            String message = array.toString();            
            json.put("text", inp2);
            json.put("annotations", array);
            return json;

        } catch (Exception ex) {
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
    /**
     * Converts a sentence @intput in TIMEX format into JSON
     *
     * @param input String in Event format
     * @return String in JSON
     */
    public JSONObject translateSentenceEvent(String input) {
        try {
            String inp2 = input;
            
            JSONObject json = new JSONObject();

            JSONArray array = new JSONArray();
            
            while (!inp2.isEmpty()) {
                String pattern = "<Event[^>]* tid=\\\"([^\\\"]+)\\\"[^>]*>([^<]*)<\\/Event[^>]*>";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(inp2);
                StringBuffer sb = new StringBuffer(inp2.length());
                if (m.find()) {
                    JSONObject item = new JSONObject();
                    int end = (m.start() + m.group(2).length());
                    item.put("beginIndex", m.start());
                    item.put("endIndex", end);
                    item.put("anchorOf", m.group(2) );
                    item.put("tid", m.group(1) );
//                    item.put("type", m.group(2) );
//                    item.put("value", m.group(3) );
                    
                    array.add(item);
                                       
                    m.appendReplacement(sb, m.group(2));
                    m.appendTail(sb);
                    inp2 = sb.toString();
                } 
                else {
                    break;
                }
                
                
            }
//            String message = array.toString();            
            json.put("text", inp2);
            json.put("annotations", array);
            return json;

        } catch (Exception ex) {
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     * Converts a sentence @input with TIMEX and Events into JSON
     *
     * @param input String in Event/TIMEX format
     * @return String in JSON
     */
    public ArrayList<Event> getEvents(String input){
        try {
            JSONObject jsonEvs = translateSentenceEvent(input);
            JSONObject jsonTimex = translateSentenceTIMEX(input);
            ArrayList<Event> finalevents = mergeJSONs(jsonTimex,jsonEvs);
            return finalevents;
        } catch(Exception ex){
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
     /**
     * Converts a sentence @input with TIMEX and Events into JSON
     *
     * @param input String in Event/TIMEX format
     * @return String in JSON
     */
    public ArrayList<Event> mergeJSONs(JSONObject jsonTimex, JSONObject jsonEvs){
     try {
            List<Event> evs = new ArrayList<Event>();
            Map<String,Event> evs2 = new HashMap<String,Event>();
            JSONArray annTimex = (JSONArray) jsonTimex.get("annotations");
            JSONArray annEvs= (JSONArray) jsonEvs.get("annotations");
            
            for(Object a : annTimex){
                JSONObject item = (JSONObject) a;
                Event ev = new Event();
                When wh = new When();
                wh.end = ((Integer) item.get("endIndex")).floatValue();
                wh.begin = ((Integer) item.get("beginIndex")).floatValue();
                wh.value = (String) item.get("value");     
                wh.anchor = (String) item.get("anchorOf");     
                //TODO: divide value more sofisticated
                if(wh.value.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")){
                    wh.year = Integer.valueOf(wh.value.substring(0, 4));
                    wh.month = Integer.valueOf(wh.value.substring(5, 7));
                    wh.day = Integer.valueOf(wh.value.substring(8, 10));
                } else if(wh.value.matches("\\d\\d\\d\\d-\\d\\d")){
                    wh.year = Integer.valueOf(wh.value.substring(0, 4));
                    wh.month = Integer.valueOf(wh.value.substring(5, 7));
                    wh.day = 0;
                }  else if(wh.value.matches("\\d\\d\\d\\d(-.)*")){
                    wh.year = Integer.valueOf(wh.value.substring(0, 4));
                    wh.month = 0;
                    wh.day = 0;
                }                
                
                ev.EventId = (String) item.get("tid");
                ev.Link = "annotate_" + ev.EventId;
                ev.When = wh;
                evs.add(ev);
            }
            
            for(Object a : annEvs){
                JSONObject item = (JSONObject) a;
                for(Event ev : evs){
                    String id = (String) item.get("tid");
                    if(ev.EventId.equalsIgnoreCase(id)){
                        Core core = new Core();
                        if(ev.Core != null){
                            core = ev.Core;
                        }
                        CoreElement ce = new CoreElement();
                        ce.End = ((Integer) item.get("endIndex")).floatValue();
                        ce.Begin = ((Integer) item.get("beginIndex")).floatValue();
                        ce.anchor = (String) item.get("anchorOf");
                        
                        core.elements.add(ce);
                        core.arrayEl.add(ce.anchor);
                        //TODO:end/begin, take smallest/biggest
                        ev.Core = core;
                       
                        evs2.put(id,ev);
                    }
                }
            }           
            
            ArrayList<Event> res = new ArrayList<Event>(evs2.values());
            
            return res;
        } catch(Exception ex){
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
