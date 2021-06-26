package oeg.eventExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static oeg.auxiliary.timelineGeneration.generateTimeline;
import oeg.tagger.docHandler.*;
import static oeg.tagger.extractors.writer.writeFile;
import org.apache.commons.io.IOUtils;
import org.json.*;
import oeg.eventFRepresentation.Annotation2JSON;
import oeg.eventFRepresentation.EventF;
import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNE;
import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNEFrames;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Servlet that returns the javascript needed for the BRAT visualization of the
 * webpage
 *
 * @author mnavas
 */
public class annotateDoc extends HttpServlet {

    static ExtractorTIMEXKeywordBasedNEFrames etkb;
//    static ExtractorTIMEXKeywordBased etkb;
    static String pathpos;
    static String pathlemma;
    static String pathrules;
    static String pathser;
    static String pathfra;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods. Takes a text as an input. Offers the same text: a) either
     * removing the legal references b)
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8cd ..");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "X-PINGOTHER,Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
        response.addHeader("Access-Control-Expose-Headers", "xsrf-token");

        // We get the parameters
        request.setCharacterEncoding("UTF-8");
//        String inputURL = request.getParameter("inputURL");
//        String inputID = request.getParameter("inputID");

        String jsonString = IOUtils.toString(request.getInputStream());

        JSONObject json = new JSONObject(jsonString);
        String inputID = (String) json.get("id");
        String inputURL = null;
//        String inputHTML = (String) json.get("htmltext");
        try {
            inputURL = (String) json.get("wordfile");
        } catch (Exception e) {
        }

//        testhtmlout = testhtmlout + "\n\n" + inputHTML;
//        
//        if (!writeFile(testhtmlout, "r.html")) {
//            System.out.println("ERROR WHILE SAVING IN" + "r.html");
//        }
        response.setStatus(200);

        ServletContext context = getServletContext();

//        pathpos = context.getResource("/WEB-INF/classes/ixa-pipes/morph-models-1.5.0/es/es-pos-perceptron-autodict01-ancora-2.0.bin").getPath();
//        pathlemma = context.getResource("/WEB-INF/classes/ixa-pipes/morph-models-1.5.0/es/es-lemma-perceptron-ancora-2.0.bin").getPath();
        pathrules = context.getResource("/WEB-INF/classes/rules/rulesEN.txt").getPath();
        pathser = context.getResource("/WEB-INF/classes/events/events.ser").getPath();
        pathfra = context.getResource("/WEB-INF/classes/events/frames.ser").getPath();
//        String inputHTML = filesDownloader.htmlDownloader(inputID);

        // We call the tagger and return its output
//        System.out.println("----------\n" + inputHTML);
        String salida = null;
        if (inputURL != null) {
            salida = parseAndTag("", inputID, inputURL);
        } else {
            salida = parseAndTag("", inputID);
        }
//        String salida = parseAndTag(inputHTML, inputID, inputURL);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(salida);

    }

    public static String parseAndTag(String s, String inputID, String inputURL) {
//    public static String parseAndTag(String inputHTML2, String inputID, String inputURL) {

        File ev1 = new File("pretimex1.txt");
        File ev2 = new File("pretimex2.txt");
        File ev3 = new File("pretimex3.txt");
        File ev4 = new File("postimex.txt");

        File fh = filesDownloader.htmlDownloader(inputID, "https://hudoc.echr.coe.int/app/conversion/docx/html/body?library=ECHR&id=");

        String inputHTML2 = "";

        try {
            inputHTML2 = IOUtils.toString(new FileInputStream(fh), "UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(annotateDoc.class.getName()).log(Level.SEVERE, null, ex);
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

        if (!writeFile(inputHTML, ev2.getAbsolutePath())) {
            System.out.println("ERROR WHILE SAVING IN" + ev2.getAbsolutePath());
        }

        File word = filesDownloader.wordDownloader(inputURL, inputID);
        Date dct = null;
        try {
            if (etkb == null) {
//                                etkb = new ExtractorTIMEXKeywordBased(null, null, pathrules, "EN"); // We innitialize the tagger in Spanish

                etkb = new ExtractorTIMEXKeywordBasedNEFrames(null, null, pathrules, "EN", pathser, pathfra); // We innitialize the tagger in Spanish
//                etkb = new ExtractorTIMEXKeywordBasedNE(null, null, pathrules, "EN", pathser); // We innitialize the tagger in Spanish
            }

//            String inputURL = "https://cors-anywhere.herokuapp.com/https://hudoc.echr.coe.int/app/conversion/docx/html/body?library=ECHR&id=" + inputID;
//
//            String txt = inputHTML.replaceAll("<[^>]*>", "");
//
//            File word = filesDownloader.wordDownloader(inputURL, inputID);
            String output = etkb.annotate(txt, "2012-02-20", word);																				 
			
            //We send it to legalwhen
            JSONObject json = new JSONObject();
            json.put("id", inputID);
            json.put("inputText", output);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain;charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, json.toString());
            Request request2 = new Request.Builder()
                    //  .url("https://legalwhen.oeg-upm.net/namespace/kb/sparql")
                    .url("https://fromtimetotime.linkeddata.es/tolegalwhen")
                    .method("POST", body)
                    .addHeader("Content-Type", "text/plain;charset=UTF-8")
                    .build();
            Response response2 = client.newCall(request2).execute();

            if (response2.code() != 200) {
                System.out.println("ERROR ADDING THE DOCUMENT TO THE KNOWLEDGE GRAPH");
            }

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
            String auxwhen = "";
            String color = "class=\"highlighter_def\"";
            String contetRegex = m.group(3);
            contetRegex = contetRegex.replaceAll("\"", "");
            contetRegex = contetRegex.replaceAll(" ", "\n");
            if (m.group(2).contains("when")) {
                color = "style=\"padding: 6px;border-radius: 3px;background-color: #e6a132;color:#FFFFFF;\"";//Naranjita";
//                color = "id=\"annotate_" + m.group(4) + "\" style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\"";//DodgerBlue";
                auxwhen = "<span class= \"hash_link_tag\" id=\"annotate_" + m.group(4) + "\" style=\"visibility:hidden; overflow:hidden; margin-top: -100px; position: absolute;\"></span> ";
            } else if (m.group(2).contains("who")) {
                color = "style=\"padding: 6px;border-radius: 3px;background-color: #a583c9;color:#FFFFFF;\"";//Morado";
//                auxwhen="<span class= \"hash_link_tag\" id=\"annotate_" + m.group(4) + "\" style=\"visibility:hidden; overflow:hidden; margin-top: -100px; position: absolute;\"></span> ";
            } else if (m.group(2).contains("what")) {
                if (m.group(0).contains("procedure")) {
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

    public static String parseAndTag(String s, String inputID) {
//    public static String parseAndTag(String inputHTML2, String inputID, String inputURL) {

        File ev1 = new File("pretimex1.txt");
        File ev2 = new File("pretimex2.txt");
        File ev3 = new File("pretimex3.txt");
        File ev4 = new File("postimex.txt");

        File fh = filesDownloader.htmlDownloader(inputID, "https://eur-lex.europa.eu/legal-content/En/TXT/HTML/?uri=CELEX:");

        String inputHTML2 = "";

        try {
            inputHTML2 = IOUtils.toString(new FileInputStream(fh), "UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(annotateDoc.class.getName()).log(Level.SEVERE, null, ex);
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
        inputHTML = inputHTML.replaceAll("<a href=\"\\./\\.\\./\\.\\./\\.\\./\\.\\./legal-content/redirect", "<a href=\"https://eur-lex.europa.eu/legal-content/redirect");
        String txt = inputHTML.replaceAll("(<p class=\\\"count\\\" id=\\\"point\\d+\\\">)\\d+(<\\/p>)", "$1$2");

        txt = txt.replaceAll("<\\/p>", "\t");

        txt = txt.replaceAll("<\\/p>", "\t");
        txt = txt.replaceAll("<[^>]*>", "");
//            txt = txt.replaceAll("&nbsp;", "\t");
        txt = txt.replaceAll("&[^;]+;", "\t");
//String txt = Jsoup.parse(inputHTML).text();

        if (!writeFile(inputHTML, ev2.getAbsolutePath())) {
            System.out.println("ERROR WHILE SAVING IN" + ev2.getAbsolutePath());
        }

        Date dct = null;
        try {
            if (etkb == null) {
                etkb = new ExtractorTIMEXKeywordBasedNEFrames(null, null, pathrules, "EN", pathser, pathfra); // We innitialize the tagger in Spanish
            }


            String output = etkb.annotate(txt, "2012-02-20", fh);
            
            
            //We send it to legalwhen
            JSONObject json = new JSONObject();
            json.put("id", inputID);
            json.put("inputText", output);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain;charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, output);
            Request request2 = new Request.Builder()
                    .url("https://fromtimetotime.linkeddata.es/tolegalwhen")
                    .method("POST", body)
                    .addHeader("Content-Type", "text/plain;charset=UTF-8")
                    .build();
            Response response2 = client.newCall(request2).execute();
            
            

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

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
