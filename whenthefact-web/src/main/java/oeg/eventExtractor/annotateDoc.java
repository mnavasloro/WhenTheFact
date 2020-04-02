package oeg.eventExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBased;
import oeg.tagger.docHandler.*;
import static oeg.tagger.extractors.writer.writeFile;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.*;
import org.jsoup.Jsoup;

/**
 * Servlet that returns the javascript needed for the BRAT visualization of the
 * webpage
 *
 * @author mnavas
 */
public class annotateDoc extends HttpServlet {

    static ExtractorTIMEXKeywordBased etkb;
    static String pathpos;
    static String pathlemma;
    static String pathrules;

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
        String inputHTML = (String) json.get("htmltext");
        String inputURL = (String) json.get("wordfile");

        response.setStatus(200);
        response.setContentType("text/html;charset=UTF-8");

        ServletContext context = getServletContext();

//        pathpos = context.getResource("/WEB-INF/classes/ixa-pipes/morph-models-1.5.0/es/es-pos-perceptron-autodict01-ancora-2.0.bin").getPath();
//        pathlemma = context.getResource("/WEB-INF/classes/ixa-pipes/morph-models-1.5.0/es/es-lemma-perceptron-ancora-2.0.bin").getPath();
        pathrules = context.getResource("/WEB-INF/classes/rules/rulesEN.txt").getPath();
//        String inputHTML = filesDownloader.htmlDownloader(inputID);

        // We call the tagger and return its output
        System.out.println("----------\n" + inputHTML);
        String salida = parseAndTag(inputHTML, inputID, inputURL);
        response.setContentType("text/plain");
        response.getWriter().println(salida);

    }

    public static String parseAndTag(String inputHTML2, String inputID, String inputURL) {
 
        File ev1 = new File("pretimex1.txt");
        File ev2 = new File("pretimex2.txt");
        File ev3 = new File("pretimex3.txt");
        File ev4 = new File("postimex.txt");
        
        System.out.println("Saving files in: " + ev1.getAbsolutePath());
        
        if (!writeFile(inputHTML2, ev1.getAbsolutePath())) {
            System.out.println("ERROR WHILE SAVING IN" + ev1.getAbsolutePath());
        }
        
        
         String stylestring = "";
                String pattern = "<(style|script)>[\\s\\S]*<\\/(style|script)>";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(inputHTML2);
                while (m.find()) {
                    stylestring = stylestring + m.group();
                    
                }
           
            String inputHTML = inputHTML2.replaceAll(pattern, "");
            
            
            
            
            String txt = inputHTML.replaceAll("<[^>]*>", ""); 
            txt = txt.replaceAll("&nbsp;", "\t");
//String txt = Jsoup.parse(inputHTML).text();

        if (!writeFile(inputHTML, ev2.getAbsolutePath())) {
            System.out.println("ERROR WHILE SAVING IN" + ev2.getAbsolutePath());
        }


            File word = filesDownloader.wordDownloader(inputURL, inputID);
        Date dct = null;
        try {
            if (etkb == null) {
                etkb = new ExtractorTIMEXKeywordBased(null, null, pathrules, "EN"); // We innitialize the tagger in Spanish
            }
            
//            String inputURL = "https://cors-anywhere.herokuapp.com/https://hudoc.echr.coe.int/app/conversion/docx/html/body?library=ECHR&id=" + inputID;
//
//            String txt = inputHTML.replaceAll("<[^>]*>", "");
//
//            File word = filesDownloader.wordDownloader(inputURL, inputID);
            String output = etkb.annotate(txt, "2012-02-20", word, word.getName());
            
        if (!writeFile(output, ev4.getAbsolutePath())) {
            System.out.println("ERROR WHILE SAVING IN" + ev4.getAbsolutePath());
        }
            System.out.println(output);

            output = HTMLMerger.mergeHTML(inputHTML, output);
            System.out.println(output);
            String out2 = createHighlights(output);
            System.out.println(stylestring + out2);
            return stylestring + out2;
//NO            return stylestring + new String(out2.getBytes(Charset.forName("UTF-8")), Charset.forName("Windows-1252"));
//            return stylestring + new String(out2.getBytes("ISO-8859-1"),"UTF-8");
 
        } catch (Exception ex) {
            System.err.print(ex.toString());
            return "";
        }
    }

    static public String createHighlights(String input) {
//        input2 = input2.replaceFirst(Pattern.quote("<?xml version=\"1.0\"?>\n" + "<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">\n" + "<TimeML>"), "");
        String input2 = input.replaceFirst(Pattern.quote("</TimeML>"), "");

        input2 = input2.replaceAll("<Event_core", "<span class=\"highlighter_def\" ");
        input2 = input2.replaceAll("<Event_what", "<span class=\"highlighter_def\" ");
        input2 = input2.replaceAll("<Event_when", "<span style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\" ");
        input2 = input2.replaceAll("<TIMEX3", "<span style=\"padding: 6px;border-radius: 3px;background-color: #3364b7;color:#FFFFFF;\" ");
//        input2 = input2.replaceAll("<Event_when", "<span class=\"highlighter_time\" ");
//        input2 = input2.replaceAll("<TIMEX3", "<span class=\"highlighter_time\" ");
        input2 = input2.replaceAll("<Event", "<span class=\"highlighter_def\" ");
        input2 = input2.replaceAll("</Event_core>", "</span>");
        input2 = input2.replaceAll("</Event_what>", "</span>");
        input2 = input2.replaceAll("</Event_when>", "</span>");
        input2 = input2.replaceAll("</TIMEX3>", "</span>");
        input2 = input2.replaceAll("</Event>", "</span>");
        input2 = input2.replaceAll("\\r?\\n", "<br>");

//        String pattern = "(<TIMEX3 ([^>]*)>)";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(input2);
//        StringBuffer sb = new StringBuffer();
//        while (m.find()) {
//            String color = "#7fa2ff";//"Orange";
////            String color = "rgba(255, 165, 0, 0.5)";//"Orange";
//            String contetRegex = m.group(2);
//            contetRegex = contetRegex.replaceAll("\"", "");
//            contetRegex = contetRegex.replaceAll(" ", "\n");
//            if (contetRegex.contains("SET")) {
//                color = "#ccb3ff";//DodgerBlue";
////                color = "rgba(135, 206, 235, 0.5)";//DodgerBlue";
//            } else if (contetRegex.contains("DURATION")) {
//                color = "#99ffeb"; //Tomato
////                color = "hsla(9, 100%, 64%, 0.5)"; //Tomato
//            } else if (contetRegex.contains("TIME")) {
//                color = "#ffbb99";//"MediumSeaGreen";
////                color = "rgba(102, 205, 170, 0.5)";//"MediumSeaGreen";
//            }
//
//            String aux2 = m.group(0);
//            aux2 = aux2.replace(">", "");
//
//            m.appendReplacement(sb, aux2.replaceFirst(Pattern.quote(aux2), "<span style=\"background-color:"
//                    + color + "\" title=\"" + contetRegex + "\">"));
//        }
//        m.appendTail(sb); // append the rest of the contents
//        
//        String saux = sb.toString();
//
//        return saux;
        return input2;
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
