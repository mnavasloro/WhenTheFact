/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.tagger.docHandler.HTMLMerger;
import static oeg.tagger.extractors.writer.writeFile;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author mnavas
 */
public class testHTMLmerger {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
//            String inputHTML2 = "<div><p class=\"s32B251D\"><span class=\"sB8D990E2\">&nbsp;</span></p><p class=\"s32B251D\"><span class=\"sB8D990E2\">&nbsp;</span></p><p class=\"s32B251D\"><span class=\"sB8D990E2\">&nbsp;</span></p><p class=\"s3CA103CC\"><span class=\"sB8D990E2\">THIRD SECTION</span></p><p class=\"s89688EC0\"><a name=\"To\"><span class=\"s7D2086B4\">CASE OF </span></a><span class=\"s7D2086B4\">MURDALOVY v. RUSSIA</span></p><p class=\"sAB870285\"><span class=\"s6B621B36\">(</span><span class=\"s6B621B36\">Application no.</span><span class=";
//            String inputTIMEX = "   THIRD SECTIONCASE OF MURDALOVY v. RUSSIA(<Event argument=\"core\" type=\"TNfacts\" tid=\"t1\">Application</Event> no. 51933/08)  JUDGMENT Art 2 (substantive and procedural) â?¢ Life â?¢ Abduction by police and subsequent disappearance of applicantsâ?? relative during counter-terrorist operation in Chechnya â?¢ Lack of effective investigationArt 3 (substantive and procedural) â?¢ Torture â?¢ Loss of victim status â?¢ Adequate compensation awarded â?¢ Investigation found to be effective â?¢ Prison";
            String inputHTML2 = IOUtils.toString(new FileInputStream(new File("C:\\apache-tomcat-9.0.14\\bin\\bh1.txt")), "UTF-8");
            String inputTIMEX = IOUtils.toString(new FileInputStream(new File("C:\\apache-tomcat-9.0.14\\bin\\ev1.txt")), "UTF-8");
            
//           String response= new String(inputTIMEX.getBytes(Charset.forName("UTF-8")), Charset.forName("Windows-1252"));
////           String response= new String(inputTIMEX.getBytes("ISO-8859-1"),"UTF-8");
//             System.out.println(response);
            String inputHTML = HTMLMerger.mergeHTML(inputHTML2, inputTIMEX);
            
            
            
            
            String stylestring = "";
                String pattern = "<(style|script)>[\\s\\S]*<\\/(style|script)>";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(inputHTML);
                while (m.find()) {
                    stylestring = stylestring + m.group();
                    
                }
                inputHTML = inputHTML.replaceAll(pattern, "");
            
            
            
            
//                       inputHTML= new String(inputHTML.getBytes(Charset.forName("UTF-8")), Charset.forName("Windows-1252"));
//           inputHTML = new String(inputHT ML.getBytes("UTF-8"),"ISO-8859-1");

            if (!writeFile(inputHTML, "C:\\Users\\mnavas\\Desktop\\ds.html")) {
            System.out.println("ERROR WHILE SAVING IN" + "C:\\Users\\mnavas\\Desktop\\ds.html");
        }
//            String txt = Jsoup.parse(inputHTML).text();
//            String txt = inputHTML.replaceAll("<[^>]*>", "");
            System.out.println(inputHTML);
        } catch (Exception ex) {
            Logger.getLogger(testHTMLmerger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
