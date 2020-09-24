package oeg.eventExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.eventRepresentation.CoreF;
import oeg.eventRepresentation.CoreElementF;
import oeg.eventRepresentation.EventF;
import oeg.eventRepresentation.WhenF;
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
                String pattern = "<Event_when tid=\"([^\"]+)\" type=\"([^\"]+)\" value=\"([^\"]+)\"[^>]*>([^<]*)<\\/Event_when>";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(inp2);
                StringBuffer sb = new StringBuffer(inp2.length());
                if (m.find()) {
                    JSONObject item = new JSONObject();
                    int end = (m.start() + m.group(4).length());
                    item.put("beginIndex", m.start());
                    item.put("endIndex", end);
                    item.put("anchorOf", m.group(4));
                    item.put("tid", m.group(1));
                    item.put("type", m.group(2));
                    item.put("value", m.group(3));

                    array.add(item);

                    m.appendReplacement(sb, m.group(4));
                    m.appendTail(sb);
                    inp2 = sb.toString();
                } else {
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
     * @param input String in EventF format
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
                    item.put("anchorOf", m.group(2));
                    item.put("tid", m.group(1));
                    if(m.group(0).contains("procedure")){
                    item.put("type", "procedure" );
                    } else if(m.group(0).contains("circumstance")){
                    item.put("type", "circumstance" );
                    }
//                    item.put("value", m.group(3) );

                    array.add(item);

                    m.appendReplacement(sb, m.group(2));
                    m.appendTail(sb);
                    inp2 = sb.toString();
                } else {
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
     * @param input String in EventF/TIMEX format
     * @return String in JSON
     */
    public ArrayList<EventF> getEvents(String input) {
        try {
//////            JSONObject jsonEvs = translateSentenceEvent(input);
//////            JSONObject jsonTimex = translateSentenceTIMEX(input);
//////            ArrayList<Event> finalevents = mergeJSONs(jsonTimex,jsonEvs);
//////            return finalevents;

            String inputCore = input.replaceAll("<Event [^>]+>", "").replaceAll("<Event_who [^>]+>", "").replaceAll("<Event_when [^>]+>", "").replaceAll("</Event_when>", "").replaceAll("</Event_who>", "").replaceAll("</Event>", "");
            String inputTimex = input.replaceAll("<Event [^>]+>", "").replaceAll("<Event_who [^>]+>", "").replaceAll("<Event_what [^>]+>", "").replaceAll("</Event_what>", "").replaceAll("</Event_who>", "").replaceAll("</Event>", "");
            String inputWho = input.replaceAll("<Event [^>]+>", "").replaceAll("<Event_what [^>]+>", "").replaceAll("<Event_when [^>]+>", "").replaceAll("</Event_when>", "").replaceAll("</Event_what>", "").replaceAll("</Event>", "");

            JSONObject jsonEvs = translateSentenceEvent(inputCore);
            JSONObject jsonTimex = translateSentenceTIMEX(inputTimex);
            JSONObject jsonWho = translateSentenceEvent(inputWho);
            ArrayList<EventF> finalevents = mergeJSONs(jsonTimex, jsonEvs);
            finalevents = mergeJSONWho(jsonWho, finalevents);
            // ADD TYPE TO THE CORES
//            finalevents = addTypesCore(input, finalevents);
            System.out.println("Event extraction done");
            return finalevents;
        } catch (Exception ex) {
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Converts a sentence @input with TIMEX and Events into JSON
     *
     * @param input String in EventF/TIMEX format
     * @return String in JSON
     */
    public ArrayList<EventF> mergeJSONWho(JSONObject jsonWho, ArrayList<EventF> jsonEvs) {
        try {
            JSONArray annWho = (JSONArray) jsonWho.get("annotations");

            for (Object a : annWho) {
                JSONObject item = (JSONObject) a;
                CoreF wh = new CoreF();
                for (EventF ev : jsonEvs) {
                    String id = (String) item.get("tid");
                    if (ev.eventId.equalsIgnoreCase(id)) {
                        CoreF core = new CoreF();
                        if (ev.who != null) {
                            core = ev.who;
                        }
                        CoreElementF ce = new CoreElementF();
                        ce.End = ((Integer) item.get("endIndex")).floatValue();
                        ce.Begin = ((Integer) item.get("beginIndex")).floatValue();
                        ce.anchor = (String) item.get("anchorOf");

                        core.elements.add(ce);
                        core.arrayEl.add(ce.anchor);
                        //TODO:end/begin, take smallest/biggest
                        ev.who = core;

                        jsonEvs.set(jsonEvs.indexOf(ev), ev);
                    }
                }
            }

            return jsonEvs;
        } catch (Exception ex) {
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Converts a sentence @input with TIMEX and Events into JSON
     *
     * @param input String in EventF/TIMEX format
     * @return String in JSON
     */
    public ArrayList<EventF> mergeJSONs(JSONObject jsonTimex, JSONObject jsonEvs) {
        try {
            List<EventF> evs = new ArrayList<EventF>();
            Map<String, EventF> evs2 = new HashMap<String, EventF>();
            JSONArray annTimex = (JSONArray) jsonTimex.get("annotations");
            JSONArray annEvs = (JSONArray) jsonEvs.get("annotations");

            for (Object a : annTimex) {
                JSONObject item = (JSONObject) a;
                EventF ev = new EventF();
                WhenF wh = new WhenF();
                wh.end = ((Integer) item.get("endIndex")).floatValue();
                wh.begin = ((Integer) item.get("beginIndex")).floatValue();
                wh.value = (String) item.get("value");
                wh.anchor = (String) item.get("anchorOf");
                //TODO: divide value more sofisticated
                if (wh.value.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
                    wh.year = Integer.valueOf(wh.value.substring(0, 4));
                    wh.month = Integer.valueOf(wh.value.substring(5, 7));
                    wh.day = Integer.valueOf(wh.value.substring(8, 10));
                } else if (wh.value.matches("\\d\\d\\d\\d-\\d\\d")) {
                    wh.year = Integer.valueOf(wh.value.substring(0, 4));
                    wh.month = Integer.valueOf(wh.value.substring(5, 7));
                    wh.day = 0;
                } else if (wh.value.matches("\\d\\d\\d\\d(-.)*")) {
                    wh.year = Integer.valueOf(wh.value.substring(0, 4));
                    wh.month = 0;
                    wh.day = 0;
                }

                ev.eventId = (String) item.get("tid");
                ev.link = "annotate_" + ev.eventId;
                ev.when = wh;
                evs.add(ev);
            }

            for (Object a : annEvs) {
                JSONObject item = (JSONObject) a;
                for (EventF ev : evs) {
                    String id = (String) item.get("tid");
                    if (ev.eventId.equalsIgnoreCase(id)) {
                        CoreF core = new CoreF();
                        if (ev.core != null) {
                            core = ev.core;
                        }
                        CoreElementF ce = new CoreElementF();
                        ce.End = ((Integer) item.get("endIndex")).floatValue();
                        ce.Begin = ((Integer) item.get("beginIndex")).floatValue();
                        ce.anchor = (String) item.get("anchorOf");
                        core.type = (String) item.get("type");

                        core.elements.add(ce);
                        core.arrayEl.add(ce.anchor);
                        //TODO:end/begin, take smallest/biggest
                        ev.core = core;

                        evs2.put(id, ev);
                    }
                }
            }

            ArrayList<EventF> res = new ArrayList<EventF>(evs2.values());

            return res;
        } catch (Exception ex) {
            Logger.getLogger(Annotation2JSON.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

//    public ArrayList<EventF> addTypesCore(String input, ArrayList<EventF> finalevents) {
//
//        String pattern = "<Event[^>]* tid=\\\"([^\\\"]+)\\\"[^>]* type=\\\"([^\\\"]+)\\\"[^>]*>";
//        ArrayList<EventF> finalevents2 = new ArrayList<EventF>();
//        Pattern p = Pattern.compile(pattern);
//        Matcher m = p.matcher(input);
//        while (m.find()) {
//            String id = m.group(1);
//            for (EventF e : finalevents) {
//                if (e.eventId.equalsIgnoreCase(id)) {
//                    e.type = m.group(2);
//                    finalevents2.add(e);
//                    break;
//                }
//            }
//        }
//
//        return finalevents2;
//    }

}
