package oeg.tagger.main;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static oeg.auxiliary.timelineGeneration.generateTimeline;
import oeg.eventFRepresentation.Annotation2JSON;
import oeg.eventFRepresentation.EventF;
import oeg.tagger.docHandler.HTMLMerger;
import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNEFrames;
import static oeg.tagger.extractors.writer.writeFile;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 * Test of the functionality of the servlets
 *
 * @author mnavas
 */
public class MainDown {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        try {

            String jsonString = "{\"id\":\"62013CJ0249\"}"; // Our test json


            JSONObject json = new JSONObject(jsonString);
            String inputID = (String) json.get("id");
            
            
            String salida = parseAndTag("", inputID);
            
            System.out.println(salida);

        } catch (Exception ex) {
            System.out.print(ex.toString());
        }

    }

    
    public static String parseAndTag(String s, String inputID) {
//    public static String parseAndTag(String inputHTML2, String inputID, String inputURL) {

        File ev1 = new File("pretimex1.txt");
        File ev2 = new File("pretimex2.txt");
        File ev3 = new File("pretimex3.txt");
        File ev4 = new File("postimex.txt");
        
        

//        File fh = filesDownloader.htmlDownloader(inputID, "https://eur-lex.europa.eu/legal-content/En/TXT/HTML/?uri=CELEX:");

        File fh = new File("62013CJ0249.html");
        String inputHTML2 = "";

        try {
            inputHTML2 = IOUtils.toString(new FileInputStream(fh), "UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(MainDown.class.getName()).log(Level.SEVERE, null, ex);
        }

//        System.out.println("Saving files in: " + ev1.getAbsolutePath());
//        
//        if (!writeFile(inputHTML2, ev1.getAbsolutePath())) {
//            System.out.println("ERROR WHILE SAVING IN" + ev1.getAbsolutePath());
//        }
//        
        StringBuilder sb = new StringBuilder("");
        String pattern = "<(style|script)>[\\s\\S]*<\\/(style|script)>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(inputHTML2);
        while (m.find()) {
            sb.append(m.group());

        }
        String stylestring = sb.toString();

        String inputHTML = inputHTML2.replaceAll(pattern, "");
        String txt = inputHTML.replaceAll("(<p class=\\\"count\\\" id=\\\"point\\d+\\\">)\\d+(<\\/p>)", "$1$2");
                
        txt = txt.replaceAll("<\\/p>", "\t");
        

        txt = txt.replaceAll("<\\/p>", "\t");
        txt = txt.replaceAll("<[^>]*>", "");
//            txt = txt.replaceAll("&nbsp;", "\t");
        txt = txt.replaceAll("&[^;]+;", "\t");
//String txt = Jsoup.parse(inputHTML).text();

        if (!writeFile(txt, ev2.getAbsolutePath())) {
            System.out.println("ERROR WHILE SAVING IN" + ev2.getAbsolutePath());
        }

        Date dct = null;
        try {
            ExtractorTIMEXKeywordBasedNEFrames etkb;
//            if (etkb == null) {
//                                etkb = new ExtractorTIMEXKeywordBased(null, null, pathrules, "EN"); // We innitialize the tagger in Spanish

                etkb = new ExtractorTIMEXKeywordBasedNEFrames("EN"); // We innitialize the tagger in Spanish
//                etkb = new ExtractorTIMEXKeywordBasedNE(null, null, pathrules, "EN", pathser); // We innitialize the tagger in Spanish
//            }

//            String inputURL = "https://cors-anywhere.herokuapp.com/https://hudoc.echr.coe.int/app/conversion/docx/html/body?library=ECHR&id=" + inputID;
//
//            String txt = inputHTML.replaceAll("<[^>]*>", "");
//
//            File word = filesDownloader.wordDownloader(inputURL, inputID);
            String output = etkb.annotate(txt, "2012-02-20", fh);

            // We add the json for the timeline
            Annotation2JSON t2j = new Annotation2JSON();
            ArrayList<EventF> events = t2j.getEvents(output);
            String finaltimeline = generateTimeline(events);
            
            // We just maintain the events that can be converted to a timeline

            if (!writeFile(output, ev4.getAbsolutePath())) {
                System.out.println("ERROR WHILE SAVING IN" + ev4.getAbsolutePath());
            }
                System.out.println("CORRECLY SAVED IN " + ev4.getAbsolutePath());
                
//            System.out.println(output);
            

            output = HTMLMerger.prepareHTML(output);
            
            output = HTMLMerger.mergeHTML(inputHTML, output);
            
            output = output.replaceAll("EVENTTOKENINI", "&#9658;");
            output = output.replaceAll("EVENTTOKENEND", "&#9668;");
//            output = output.replaceAll("EVENTTOKENINI", "&lceil;");
//            output = output.replaceAll("EVENTTOKENEND", "&rceil;");
//            output = output.replaceAll("<Event [^>]+>", "&lceil;");
//            output = output.replaceAll("</Event>", "&rceil;");
            
            System.out.println(output);
            String out2 = createHighlights(output, events);
            System.out.println(stylestring + out2);
            return stylestring + out2 + finaltimeline;
//NO            return stylestring + new String(out2.getBytes(Charset.forName("UTF-8")), Charset.forName("Windows-1252"));
//            return stylestring + new String(out2.getBytes("ISO-8859-1"),"UTF-8");

        } catch (Exception ex) {
            System.err.print(ex.toString());
            return "";
        }
    }


    static public String createHighlights(String input, ArrayList<EventF> events) {
//        input2 = input2.replaceFirst(Pattern.quote("<?xml version=\"1.0\"?>\n" + "<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">\n" + "<TimeML>"), "");
        String input2 = input.replaceFirst(Pattern.quote("</TimeML>"), "");

//        input2 = input2.replaceAll("<Event_core", "<span class=\"highlighter_def\" ");
//        input2 = input2.replaceAll("<Event_what", "<span class=\"highlighter_def\" ");
//        input2 = input2.replaceAll("<Event_when", "<span style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\" ");
//        input2 = input2.replaceAll("<TIMEX3", "<span style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\" ");
//
//        input2 = input2.replaceAll("<Event", "<span class=\"highlighter_def\" ");
//        input2 = input2.replaceAll("</Event_core>", "</span>");
        input2 = input2.replaceAll("</Event_what>", "</span>");
        input2 = input2.replaceAll("</Event_when>", "</span>");
        input2 = input2.replaceAll("</Event_who>", "</span>");
        input2 = input2.replaceAll("</TIMEX3>", "</span>");
//        input2 = input2.replaceAll("</Event>", "</span>");
        input2 = input2.replaceAll("\\r?\\n", "<br>");

        String pattern = "(<(Event_when|Event_what|Event_who) ([^>]*tid=\"([^\"]*)\"([^>]*))>)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input2);
        StringBuffer sb = new StringBuffer();
        
        while (m.find()) {
            String auxwhen="";
            String color = "class=\"highlighter_def\"";
            String contetRegex = m.group(3);
            contetRegex = contetRegex.replaceAll("\"", "");
            contetRegex = contetRegex.replaceAll(" ", "\n");
            if (m.group(2).contains("when")) {
                color = "style=\"padding: 6px;border-radius: 3px;background-color: #e6a132;color:#FFFFFF;\"";//Naranjita";
//                color = "id=\"annotate_" + m.group(4) + "\" style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\"";//DodgerBlue";
                auxwhen="<span class= \"hash_link_tag\" id=\"annotate_" + m.group(4) + "\" style=\"visibility:hidden; overflow:hidden; margin-top: -100px; position: absolute;\"></span> ";
            } else if (m.group(2).contains("who")) {
                color = "style=\"padding: 6px;border-radius: 3px;background-color: #a583c9;color:#FFFFFF;\"";//Morado";
//                auxwhen="<span class= \"hash_link_tag\" id=\"annotate_" + m.group(4) + "\" style=\"visibility:hidden; overflow:hidden; margin-top: -100px; position: absolute;\"></span> ";
            } else if (m.group(2).contains("what")) {                
                        if(m.group(0).contains("procedure")){
                            color = "style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\"";//DodgerBlue";              
                        }
                        
                    
//                for(EventF e : events){
//                    if(e.eventId.equalsIgnoreCase(m.group(4))){
//                        if(e.type.equalsIgnoreCase("procedure")){
//                            color = "style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\"";//DodgerBlue";              
//                        }
//                        break;
//                    }
//                }
//                auxwhen="<span class= \"hash_link_tag\" id=\"annotate_" + m.group(4) + "\" style=\"visibility:hidden; overflow:hidden; margin-top: -100px; position: absolute;\"></span> ";
            } 
//            else if (m.group(2).contains("EventF")) {
//                color = "style=\"padding: 6px;border-radius: 3px;background-color: #b4c6d6;color:#FFFFFF;\"";//Gris";
////                auxwhen="<span class= \"hash_link_tag\" id=\"annotate_" + m.group(4) + "\" style=\"visibility:hidden; overflow:hidden; margin-top: -100px; position: absolute;\"></span> ";
//            }
            String aux2 = m.group(0);
            aux2 = aux2.replace(">", "");

            m.appendReplacement(sb, aux2.replaceFirst(Pattern.quote(aux2), auxwhen + "<span "
                    + color + " title=\"" + contetRegex + "\">"));
        }
        m.appendTail(sb); // append the rest of the contents

        return sb.toString();
//        return input2;
    }
}
